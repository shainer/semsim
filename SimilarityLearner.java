/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import libsvm.*;
import java.io.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import cmu.arktweetnlp.Tagger;
import java.util.Arrays;

/**
 * Class to extract features in order to train a similarity model
 */
public class SimilarityLearner
{
    private FeatureCollector fc;
    private Tagger tagger;
    private svm svr;
    
    public SimilarityLearner(boolean featureCollection)
    {
        if (featureCollection) {
            System.out.print(":: Initializing feature collector with LSA... ");
            fc = new FeatureCollector("word-frequencies.txt");
            System.out.println("OK.");

            System.out.print(":: Initializing tokenizer and POS tagger... ");
            tagger = new Tagger();

            try {
                tagger.loadModel( Defines.getTaggerModelPath() );
                System.out.println("OK.");
            } catch (IOException e) {
                System.err.println("\nError loading model for POS tagging: " + e.getMessage());
                System.exit(-1);
            }
        }
    }

    /* Returns a list of samples (features + target) for each line in the supplied training files */
    public List<TrainingSample> extractFeatures(String[] trainingFiles)
    {
        System.out.println(":: Extracting features from files (this may take a lot of time!)... ");
        List<TrainingSample> features = new LinkedList<>();
        
        try {
            for (int i = 0; i < trainingFiles.length; i++) {
                features.addAll( getSamples(trainingFiles[i]) );
            }
        } catch (IOException e) {
            System.err.println("Error reading sample files: " + e.getMessage());
        }
        
        System.out.println("DONE.");
        return features;
    }
    
    /* Writes features on an output file in our format */
    public void writeFeatures(List<TrainingSample> samples, String featureFile)
    {
        System.out.print(":: Writing features on " + featureFile + " ... ");
        
        try {
            BufferedWriter bw = new BufferedWriter( new FileWriter(featureFile) );
            
            for (TrainingSample sample : samples) {
                for (int i = 0; i < sample.features.length; i++) {
                    bw.write( sample.features[i] + "\t" );
                }
                
                bw.write(sample.target + "\n");
            }
            
            bw.close();
            System.out.println("OK");
        } catch (IOException e) {
            System.err.println("\nError writing feature file: " + e.getMessage());
        }
    }
    
    /* Reads pre-computed features from feature files and creates samples */
    public List<TrainingSample> readFeatures(String[] featureFiles)
    {
        System.out.print(":: Reading features from files... ");
        List<TrainingSample> samples = new LinkedList<>();
        List<String> featureLines = new LinkedList<>();
        
        for (int i = 0; i < featureFiles.length; i++) {
            featureLines.addAll( IOUtils.readlines(featureFiles[i]) );
        }
        
        for (String line : featureLines) {
            String[] fields = line.split("\t");
            double target;
            double[] features = new double[ Defines.getFeatureNumber() ];
            int i;

            for (i = 0; i < fields.length - 1; i++) {
                features[i] = Double.parseDouble( fields[i] );
            }

            target = Double.parseDouble( fields[i] );
            TrainingSample sample = new TrainingSample(features, target);
            samples.add(sample);
        }
        
        System.out.println("OK");
        return samples;
    }
    
    /* Actual learning model */
    public void learnModel(List<TrainingSample> features)
    {
        System.out.println("\nLearning process begins!");
        
//        System.out.print(":: Scaling features... ");
//        scale(features);
//        System.out.println("OK.");
        
        svm_problem problem = buildSVMProblem(features);
        svm_parameter parameter = Defines.getSVMParameters();
        parameter.C = Defines.getBestC();
        parameter.gamma = Defines.getBestGamma();
        parameter.p = Defines.getBestP();
        
        svm_model model;
        
        System.out.print(":: Training model with optimal parameters... ");
        model = svm.svm_train(problem, parameter);
        System.out.println("OK.");
        
        try {
            System.out.print(":: Saving model on file... ");
            svm.svm_save_model(Defines.getSimilarityModelPath(), model);
        } catch (IOException io) {
            System.err.println("\n:: Error saving similarity model: " + io.getMessage());
        }
        
        System.out.println("OK.");
    }
    
    public svm_problem buildSVMProblem(List<TrainingSample> samples)
    {
        svm_problem problem = new svm_problem();
        double[] targetArray = new double[ samples.size() ];
        svm_node[][] featureMatrix = new svm_node[ samples.size() ][ Defines.getFeatureNumber() ];
        
        int sampleIndex = 0;
        for (TrainingSample sample : samples) {
            targetArray[sampleIndex] = sample.target;
            
            for (int j = 0; j < Defines.getFeatureNumber(); j++) {
                featureMatrix[sampleIndex][j] = new svm_node();
                featureMatrix[sampleIndex][j].index = j+1;
                featureMatrix[sampleIndex][j].value = sample.features[j];
            }
            
            sampleIndex++;
        }
        
        problem.l = samples.size();
        problem.y = Arrays.copyOf(targetArray, targetArray.length);
        
        problem.x = new svm_node[ samples.size()][ Defines.getFeatureNumber() ];
        for (int i = 0; i < featureMatrix.length; i++) {
            problem.x[i] = Arrays.copyOf(featureMatrix[i], featureMatrix[i].length);
        }
        
        return problem;
    }
    
    private void scale(List<TrainingSample> features)
    {
        double[] featureMaxs = new double[ Defines.getFeatureNumber() ];
        int i = 0;
        
        for (String line : IOUtils.readlines("train/maxs.txt")) {
            featureMaxs[i++] = Double.parseDouble(line);
        }
        
        for (TrainingSample sample: features) {
            for (int j = 0; j < Defines.getFeatureNumber(); j++) {
                sample.features[j] /= featureMaxs[j];
            }
        }
    }
    
    /* Utility method: gets samples from file lines */
    private List<TrainingSample> getSamples(String sampleFile) throws IOException
    {
        List<TrainingSample> samples = new LinkedList<>();
        List<SentencePair> pairs = new LinkedList<>();
        List<Double> targets = new LinkedList<>();
            
        for (String line : IOUtils.readlines(sampleFile)) {
            String[] fields = line.split("\t");

            SentencePair sp = new SentencePair(fields[0], fields[1], tagger);            
            double target = Double.parseDouble(fields[2]);
            pairs.add(sp);
            targets.add(target);
        }

        System.out.println("Collected " + pairs.size() + " sentence pairs from " + sampleFile);
        
        Iterator<SentencePair> spIt;
        Iterator<Double> targetIt;

        for (spIt = pairs.iterator(), targetIt = targets.iterator(); spIt.hasNext() && targetIt.hasNext();) {
            double[] features = fc.features( spIt.next() );
            TrainingSample sample = new TrainingSample(features, targetIt.next());
            samples.add(sample);
        }
        
        return samples;
    }
}
