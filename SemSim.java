/*
 * Entry point.
 */

import java.io.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Properties;

public class SemSim
{
    public static void main(String[] args)
    {
        int paramIndex;
        
        /* If a properties file is passed, it must contains options for training the system. */
        if ((paramIndex = findParameter(args, "--parameters")) != -1) {
            trainSystem(args, paramIndex);
        } else { /* otherwise, proceed with using the system */
            String inputFile = null;
            String outputFile = null;
            
            if (args.length > 0) {
                inputFile = args[0];
            }

            if (args.length > 1) {
                outputFile = args[1];
            }

            /* Parses a JSON tweet */
            TweetParser tp = new TweetParser(inputFile, outputFile);
            tp.parse();
            List<SentencePair> pairs = tp.getSentencePairs();

            /* Load the pre-existing model from file */
            SimilarityTester m = new SimilarityTester("modelfile");

            /* Gets the results and writes them on the JSON output file */
            double[] similarities = m.getSimilarities(pairs);
            tp.writeSimilarities(similarities);
        }
    }
    
    /*
     * Gets the index of a string in the command line array, or -1 if it
     * doesn't exist.
     */
    private static int findParameter(String[] args, String p)
    {
        for (int i = 0; i < args.length; i++) {
            if (p.equals(args[i])) {
                return i;
            }
        }
        
        return -1;
    }
    
    /*
     * Reads the properties file and launches the correct step of the training.
     * Refer to the README to know which steps are available and which parameter are required for each.
     */
    private static void trainSystem(String[] args, int paramIndex)
    {
        Properties prop = new Properties();
        String propertyFile;
        
        /* No file path has been passed */
        if (paramIndex == args.length - 1) {
            System.err.println("Missing properties file after --parameters");
            System.exit(-1);
        }
                
        propertyFile = args[paramIndex+1];
      
        try {
            prop.load( new FileInputStream(propertyFile) );
        } catch (IOException fnf) {
            System.err.println("Error accessing properties file " + fnf.getMessage());
            return;
        }

        boolean samplesPresent = (prop.getProperty("trainfiles") != null);
        boolean outputFeatures = (prop.getProperty("featureoutput") != null);
        boolean inputFeatures = (prop.getProperty("featureinputs") != null);

        if (badPropertiesFile(samplesPresent, outputFeatures, inputFeatures)) {
            System.err.println("The property file has an invalid format. Please refer to the README for instructions.");
            System.exit(-1);
        }

        SimilarityLearner sl = new SimilarityLearner(!outputFeatures);
        List<TrainingSample> samples = new LinkedList<>();

        /* If sample files are specified, extract features for those samples */
        if (samplesPresent) {
            String[] trainingFiles = prop.getProperty("trainfiles").split(" ");
            samples = sl.extractFeatures(trainingFiles);
        }

        /* If input feature files have been passed, read the precomputed features from them */
        if (inputFeatures) {
            String[] featureInputs = prop.getProperty("featureinputs").split(" ");
            samples = sl.readFeatures(featureInputs);
        }

        /* 
         * If an "output file" has been passed, the features we computed must be stored there
         * for later use.
         */
        if (outputFeatures) {
            String featureOutput = prop.getProperty("featureoutput");
            sl.writeFeatures(samples, featureOutput);
        } else { /* otherwise, use them straight away to learn the model */
            sl.learnModel(samples);
        }
    }
    
    /*
     * Checks whether we have the correct parameters. The parameters available define the training
     * step we are performing, so only some combinations are allowed.
     */
    private static boolean badPropertiesFile(boolean samples, boolean outputF, boolean inputF)
    {
        return (!samples && !inputF && !outputF) || (samples && inputF) || (inputF && outputF) || (outputF && !samples && !inputF);
    }
} 

