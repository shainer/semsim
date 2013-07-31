/*
 * Entry point.
 */

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.*;
import java.util.List;
import java.util.Properties;

public class SemanticSimilarity
{
    public static void main(String[] args)
    {        
        int paramIndex;
        
        Properties prop = new Properties();
        prop.put("annotators", "tokenize, ssplit, pos, lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(prop);
        System.out.println(":: Stanford NLP pipeline initialized correctly.");
        
        /* If a properties file is passed, it must contains options for training the system. */
        if ((paramIndex = findParameter(args, "--parameters")) != -1) {
            trainSystem(args, paramIndex, pipeline);
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
            System.out.print(":: Parsing input tweet... ");
            TweetParser tp = new TweetParser(inputFile, outputFile, pipeline);
            tp.parse();
            List<SentencePair> pairs = tp.getSentencePairs();
            System.out.println("OK");

            /* Load the pre-existing model from file */
            SimilarityTest m = new SimilarityTest(pipeline);

            System.out.print(":: Computing and writing similarities on output... ");
            /* Gets the results and writes them on the JSON output file */
            double[] similarities = m.getSimilarities(pairs);
            tp.writeSimilarities(similarities);
            System.out.println("OK");
        }
    }
    
    /*
     * Reads the properties file and launches the correct step of the training.
     * Refer to the README to know which steps are available and which parameter are required for each.
     */
    private static void trainSystem(String[] args, int paramIndex, StanfordCoreNLP nlp)
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
        } catch (IOException io) {
            System.err.println("Error accessing properties file: " + io.getLocalizedMessage());
            System.exit(-1);
        }
        
        String trainingFiles = prop.getProperty("trainfiles");
        
        if (trainingFiles == null) {
            System.err.println("The property file has an invalid format. Please see the README for instructions.");
            System.exit(-1);
        }

        SimilarityLearner sl = new SimilarityLearner(nlp);
        List<TrainingSample> samples = sl.extractFeatures( trainingFiles.split(" ") );
        sl.learnModel(samples);
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
} 

