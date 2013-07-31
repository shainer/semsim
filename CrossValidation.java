/*
 * Separated application for performing cross validation.
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
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import libsvm.*;


public class CrossValidation
{
    public static void main(String[] args)
    {
        if (args.length == 0) {
            System.out.println("Usage: CrossValidation <sample files>");
            System.exit(-1);
        }
        
        /* Suppress all output from libsvm */
        svm_print_interface iface = new svm_print_interface() {
            @Override public void print(String string) {}
        };
        svm.svm_set_print_string_function(iface);
        
        /* Initialize the Stanford NLP pipeline */
        Properties prop = new Properties();
        prop.put("annotators", "tokenize, ssplit, pos, lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(prop);
        System.out.println(":: Stanford NLP pipeline initialized correctly.");
        
        String[] files = { args[0] };
        SimilarityLearner sl = new SimilarityLearner(pipeline);

        List<TrainingSample> samples = sl.extractFeatures(files);
        svm_problem problem = sl.buildSVMProblem(samples);
        
        svm_parameter parameters = Constants.getSVMParameters();
        double[] C_values = Constants.getCValues();
        double[] P_values = Constants.getPValues();
        double[] G_values = Constants.getGammaValues();

        double bestCorr = Double.MIN_VALUE;
        double bestC = 0.0;
        double bestP = 0.0;
        double bestGamma = 0.0;
        
        double[] targets = new double[ samples.size() ];
        double[] gs = new double[ samples.size() ]; /* gold standard scores provided with the samples */     
        int i = 0;
        
        for (Iterator<TrainingSample> it = samples.iterator(); it.hasNext(); ) {
            gs[i++] = it.next().target;
        }
        
        System.out.println(":: Starting cross validation.");
        
        for (int iC = 0; iC < C_values.length; iC++) {
            parameters.C = C_values[iC];
            
            for (int iP = 0; iP < P_values.length; iP++) {
                parameters.p = P_values[iP];
                
                for (int iG = 0; iG < G_values.length; iG++) {
                    parameters.gamma = G_values[iG];
                    
                    System.out.println("Trying C = " + parameters.C + ", P = " + parameters.p + ", G = " + parameters.gamma);
                    
                    svm.svm_cross_validation(problem, parameters, Constants.getValidationFold(), targets);
                    double corr = Correlation.getPearsonCorrelation(targets, gs);
                    
                    if (corr > bestCorr) {
                        System.out.println(":: New best correlation is " + corr);
                        bestCorr = corr;
                        bestC = C_values[iC];
                        bestP = P_values[iP];
                        bestGamma = G_values[iG];
                    }
                }
            }
        }
        
        System.out.println(":: Cross validation finished.");
        System.out.println("C: " + bestC);
        System.out.println("P: " + bestP);
        System.out.println("Gamma: " + bestGamma);
        System.out.println("Best correlation is " + bestCorr);
    }
}
