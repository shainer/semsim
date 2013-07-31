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
            parseProperties(args, paramIndex);
        } else {
            paramIndex = -2;
        }

        String inputFile = null;
        String outputFile = null;

        if (args.length > (paramIndex + 2)) {
            inputFile = args[paramIndex + 2];
        }

        if (args.length > (paramIndex + 3)) {
            outputFile = args[paramIndex + 3];
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
    
    private static void parseProperties(String[] args, int paramIndex)
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
        
        String google = prop.getProperty("google-corpus");
        String lsa = prop.getProperty("lsa");
        String sim = prop.getProperty("similarity-model");
        
        if (google != null) {
            File f = new File(google);
            
            if (!f.exists() || !f.isDirectory()) {
                System.err.println("The google corpus path \"" + google + "\" doesn't exist.");
                System.exit(-1);
            }
            
            Constants.setGoogleCorpusFolder(google);
        }
        
        if (lsa != null) {
            File f = new File(lsa);
            
            if (!f.exists()) {
                System.err.println("The LSA matrix path \"" + lsa + "\" doesn't exist.");
                System.exit(-1);
            }
            
            Constants.setLsaMatrixPath(lsa, Integer.parseInt(prop.getProperty("lsa-size", "100")));
        }
        
        if (sim != null) {
            File f = new File(sim);
            
            if (!f.exists()) {
                System.err.println("The similarity model path \"" + sim + "\" doesn't exist.");
                System.exit(-1);
            }
            
            Constants.setSimilarityModelPath(sim);
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
} 

