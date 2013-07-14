/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import libsvm.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import cmu.arktweetnlp.Tagger;

/**
 *
 * @author shainer
 */
public class SimilarityLearner
{
    private FeatureCollector fc;
    private Tagger tagger;
    private svm svr;
    
    public SimilarityLearner()
    {
        System.out.print(":: Initializing LSA information... ");
        LSA lsa = new LSA();
        System.out.println("OK.");
        
        System.out.print(":: Initializing feature collector... ");
        fc = new FeatureCollector("word-frequencies.txt", lsa);
        System.out.println("OK.");
        
        System.out.print(":: Initializing tokenizer and POS tagger... ");
        tagger = new Tagger();
        
        try {
            tagger.loadModel( Properties.getTaggerModelPath() );
            System.out.println("OK.");
        } catch (IOException e) {
            System.err.println("\nError loading model for POS tagging: " + e.getMessage());
            System.exit(-1);
        }
    }

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
            double[] features = new double[ Properties.getFeatureNumber() ];
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
    
    public void learnModel(List<TrainingSample> features)
    {
        System.out.print(":: Learning model from " + features.size() + " samples... ");
        svm_problem problem = new svm_problem();
        double[] targetArray = new double[ features.size() ];
        svm_node[][] featureMatrix = new svm_node[ features.size() ][ Properties.getFeatureNumber() ];
        
        int sampleIndex = 0;
        for (TrainingSample sample : features) {
            targetArray[sampleIndex] = sample.target;
            
            for (int j = 0; j < Properties.getFeatureNumber(); j++) {
                featureMatrix[sampleIndex][j] = new svm_node();
                featureMatrix[sampleIndex][j].index = j+1;
                featureMatrix[sampleIndex][j].value = sample.features[j];
            }
            
            sampleIndex++;
        }
        
        problem.l = features.size();
        problem.y = targetArray;
        problem.x = featureMatrix;
        System.out.println("OK (fake!)");
        /* Scale features */
        /* Cross validation to decide the best parameters of the kernel */
        /* train the model */
        /* store the model */
    }
    
    private List<TrainingSample> getSamples(String sampleFile) throws IOException
    {
        List<TrainingSample> samples = new LinkedList<>();
        List<SentencePair> pairs = new LinkedList<>();
        List<Double> targets = new LinkedList<>();
        List<String> sampleLines = IOUtils.readlines(sampleFile);
        
        BufferedReader br = new BufferedReader( new FileReader(sampleFile) );
        String line;
            
        while ((line = br.readLine()) != null) {
            String[] fields = line.split("\t");

            SentencePair sp = new SentencePair(fields[0], fields[1], tagger);
            double target = Double.parseDouble(fields[2]);
            pairs.add(sp);
            targets.add(target);
        }

        br.close();
        System.out.println("Collected " + pairs.size() + " sentence pairs from " + sampleFile);
        
        Iterator<SentencePair> spIt;
        Iterator<Double> targetIt;
        
        for (spIt = pairs.iterator(), targetIt = targets.iterator(); spIt.hasNext() && targetIt.hasNext();) {
            double[] featureCopy = new double[ Properties.getFeatureNumber() ];
            double[] features = fc.features( spIt.next() );
            featureCopy = Arrays.copyOf(features, features.length);
            
            TrainingSample sample = new TrainingSample(featureCopy, targetIt.next());
            samples.add(sample);
        }
        
        return samples;
    }
}
