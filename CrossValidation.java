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
    }
}
