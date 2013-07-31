/*
 * Entry point for using the application. 
 * 
 * Copyright (C) 2013 Lisa Vitolo <lisavitolo90@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Creative Commons
 * Attribution-NonCommercial-ShareAlike 3.0 license.
 * You should have received a copy of the license with this product.
 * Otherwise, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
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
        
        /* Initialize the Stanford NLP with the needed annotators (ssplit is mandatory) */
        Properties prop = new Properties();
        prop.put("annotators", "tokenize, ssplit, pos, lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(prop);
        System.out.println(":: Stanford NLP pipeline initialized correctly.");
        
        /* If a properties file is passed, read and set options from it. */
        if ((paramIndex = findArgument(args, "--parameters")) != -1) {
            parseProperties(args, paramIndex);
        } else {
            paramIndex = -2;
        }

        String inputFile = null;
        String outputFile = null;

        /*
         * Take options "after" the properties file, or at args[0] and args[1] if there was
         * no properties file.
         */
        if (args.length > (paramIndex + 2)) {
            inputFile = args[paramIndex + 2];
        }

        if (args.length > (paramIndex + 3)) {
            outputFile = args[paramIndex + 3];
        }
        
        /* Parses a JSON tweet and gets the sentence pairs */
        System.out.print(":: Parsing input tweet... ");
        TweetParser tp = new TweetParser(inputFile, outputFile, pipeline);
        tp.parse();
        List<SentencePair> pairs = tp.getSentencePairs();
        System.out.println("OK");

        /* Load the pre-existing model from file */
        SimilarityTest m = new SimilarityTest(pipeline);

        /* Gets the similarity scores and writes them on the JSON output file */
        System.out.print(":: Computing and writing similarities on output... ");
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
        
        /*
         * If paths were passed and they exist, set them for the application to use, otherwise
         * do nothing (keeping the default values).
         * 
         * The application often assumes the files are formatted as it requires. Passing files with
         * an unrecognized format may result in unexpected errors and behaviours later. 
         */
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
            
            /* 100 is the default size of our LSA vector */
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
    private static int findArgument(String[] args, String p)
    {
        for (int i = 0; i < args.length; i++) {
            if (p.equals(args[i])) {
                return i;
            }
        }
        
        return -1;
    }
} 

