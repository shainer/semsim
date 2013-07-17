/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import libsvm.svm_parameter;

/**
 *
 * @author shainer
 */
public class Properties
{
    private static final int FEATURE_SIZE = 16;
    private static final int LSA_VECTOR_SIZE = 100;
    private static final String[] stopWords = {"i", "a", "about", "an", "are", "as", "at", "be", "by", "for", "from",
                                               "how", "in", "is", "it", "of", "on", "or", "that", "the", "this", "to",
                                               "was", "what", "when", "where", "who", "will", "with", "the", "'s", "did",
                                               "have", "has", "had", "were", "'ll"};
    
    private static final String taggerModelPath = "taggerModel";
    
    public static int getFeatureNumber()
    {
        return FEATURE_SIZE;
    }
    
    public static String[] getStopWords()
    {
        return stopWords;
    }
    
    public static String getTaggerModelPath()
    {
        return taggerModelPath;
    }
    
    public static int getLSAVectorSize()
    {
        return LSA_VECTOR_SIZE;
    }
    
    public static svm_parameter getSVMParameters()
    {
        svm_parameter param = new svm_parameter();
        
        param.svm_type = svm_parameter.EPSILON_SVR;
        param.kernel_type = svm_parameter.RBF;
        param.cache_size = 10.0;
        param.eps = 0.001;
        
        return param;
    }
}
