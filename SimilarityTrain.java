/*
 * Separated application for training a system.
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
import java.util.List;
import java.util.Properties;

public class SimilarityTrain
{
    public static void main(String[] args)
    {
        if (args.length == 0) {
            System.err.println("Usage: java SimilarityTrain <train file(s)>");
            System.exit(-1);
        }
        
        Properties prop = new Properties();
        prop.put("annotators", "tokenize, ssplit, pos, lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(prop);
        System.out.println(":: Stanford NLP pipeline initialized correctly.");
        
        SimilarityLearner sl = new SimilarityLearner(pipeline);
        List<TrainingSample> samples = sl.extractFeatures(args);
        sl.learnModel(samples);
    }
}
