/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.List;
import libsvm.*;

/**
 *
 * @author shainer
 */
public class CrossValidation
{
    public static void main(String[] args)
    {
        if (args.length != 1) {
            System.out.println(":: Usage: java CrossValidation <sample file>");
            System.exit(-1);
        }
        
        String[] files = { args[0] };
        SimilarityLearner sl = new SimilarityLearner(true);
        
        List<TrainingSample> samples = sl.extractFeatures(files);
        svm_problem problem = sl.buildSVMProblem(samples);
        svm_parameter parameters = Properties.getSVMParameters();
        double[] realTargets = problem.y;
        double[] validatedTargets = new double[ realTargets.length ];
        
        double[] C_values = Properties.getCValues();
        double[] P_values = Properties.getPValues();
        double[] G_values = Properties.getGammaValues();
        
        int best = 0;
        double bestC = 0.0;
        double bestP = 0.0;
        double bestGamma = 0.0;
        
        System.out.println(":: Starting cross validation.");
        
        for (int iC = 0; iC < C_values.length; iC++) {
            parameters.C = C_values[iC];
            
            for (int iP = 0; iP < P_values.length; iP++) {
                parameters.p = P_values[iP];
                
                for (int iG = 0; iG < G_values.length; iG++) {
                    parameters.gamma = G_values[iG];
                    
                    svm.svm_cross_validation(problem, parameters, Properties.getValidationFold(), validatedTargets);
                    int correctCount = compareResults(realTargets, validatedTargets);
                    
                    if (correctCount > best) {
                        bestC = C_values[iC];
                        bestP = P_values[iP];
                        bestGamma = G_values[iG];
                    }
                    System.out.println("ROUND FINISHED.");
                }
            }
        }
        
        System.out.println(":: Cross validation finished.");
        System.out.println("C: " + bestC);
        System.out.println("P: " + bestP);
        System.out.println("Gamma: " + bestGamma);
    }
    
    public static int compareResults(double[] real, double[] cross)
    {
        int correct = 0;
        
        for (int i = 0; i < real.length; i++) {
            if (real[i] - cross[i] <= Properties.getTolerance()) {
                correct++;
            }
        }
        
        return correct;
    }
}