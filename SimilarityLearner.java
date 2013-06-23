/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.HashMap;
import libsvm.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Tagger.TaggedToken;

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
        fc = new FeatureCollector();
        tagger = new Tagger();
        
        try {
            tagger.loadModel("src/taggerModel");
        } catch (IOException e) {
            System.err.println("Error loading model for POS tagging: " + e.getLocalizedMessage());
        }
    }
    
    private List<TrainingSample> getSamples(String sampleFile)
    {
        List<TrainingSample> samples = new LinkedList<>();
        List<SentencePair> pairs = new LinkedList<>();
        List<Double> targets = new LinkedList<>();
        List<String> sampleLines = IOUtils.readlines(sampleFile);
        
        try {
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
        } catch (IOException e) {
            System.err.println(":: Error reading " + sampleFile + ": " + e.getMessage());
        }
        
        System.out.println(":: Collected " + pairs.size() + " sentence pairs");
        
        fc.initialize(pairs);
        Iterator<SentencePair> spIt;
        Iterator<Double> targetIt;
        
        for (spIt = pairs.iterator(), targetIt = targets.iterator(); spIt.hasNext() && targetIt.hasNext();) {
            double[] featureCopy = new double[ Properties.getFeatureNumber() ];
            double[] features = fc.features( spIt.next() );
            featureCopy = Arrays.copyOf(features, features.length);
            
            TrainingSample sample = new TrainingSample(featureCopy, targetIt.next());
            samples.add(sample);
        }
        
        fc.deinitialize();
        return samples;
    }
    
    public void learnModel(List<TrainingSample> features)
    {
        System.out.println("LEARN MODEL FROM " + features.size() + " SAMPLES");
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
        
        /* Cross validation if no file present, scaling of features */
        /* train and store the model */
    }

    public List<TrainingSample> extractFeatures(String[] trainingFiles)
    {
        System.out.println("EXTRACTING FEATURES FROM FILES");
        List<TrainingSample> features = new LinkedList<>();
        
        for (int i = 0; i < trainingFiles.length; i++) {
            features.addAll( getSamples(trainingFiles[i]) );
        }
        
        return features;
    }
    
    public void writeFeatures(List<TrainingSample> samples, String featureFile)
    {
        System.out.println(":: Writing features on file " + featureFile);
        
        try {
            BufferedWriter bw = new BufferedWriter( new FileWriter(featureFile) );
            
            for (TrainingSample sample : samples) {
                for (int i = 0; i < sample.features.length; i++) {
                    bw.write( sample.features[i] + "\t" );
                }
                
                bw.write(sample.target + "\n");
            }
            
            bw.close();
        } catch (IOException e) {
            System.err.println(":: Error writing feature file: " + e.getMessage());
        }
    }
    
    public List<TrainingSample> readFeatures(String featureFile)
    {
        System.out.println(":: Reading features from " + featureFile);
        List<TrainingSample> samples = new LinkedList<>();
        List<String> featureLines = IOUtils.readlines(featureFile);
        
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
        
        return samples;
    }
}
