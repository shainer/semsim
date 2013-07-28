/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import libsvm.svm_parameter;

/**
 *
 * @author shainer
 */
public class Defines
{
    private static final int FEATURE_SIZE = 19;
    private static final int LSA_VECTOR_SIZE = 100;
    private static final String[] stopWords = {"i", "a", "about", "an", "are", "as", "at", "be", "by", "for", "from",
                                               "how", "in", "is", "it", "of", "on", "or", "that", "the", "this", "to",
                                               "was", "what", "when", "where", "who", "will", "with", "the", "'s", "did",
                                               "have", "has", "had", "were", "'ll"};
    private static final double tolerance = 0.6;
    
    private static final String taggerModelPath = "taggerModel";
    private static final String similarityModelPath = "similarityModel.txt";
    
    private static final int CROSS_VALIDATION_FOLD = 10;
    private static final double[] C_VALUES = {1, 2, 5, 10, 20, 50, 100, 200, 500, 1000};
    private static final double[] P_VALUES = {1, 0.5, 0.2, 0.1, 0.05, 0.02, 0.01};
    private static final double[] G_VALUES = {2, 1, 0.5, 0.2, 0.1, 0.05, 0.02, 0.01, 0.005, 0.002};
    
    private static final double C = 1;
    private static final double P = 0.02;
    private static final double G = 2;
    
    private static StanfordCoreNLP nlp;
    
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
    
    public static String getSimilarityModelPath()
    {
        return similarityModelPath;
    }
    
    public static int getLSAVectorSize()
    {
        return LSA_VECTOR_SIZE;
    }
    
    public static double getTolerance()
    {
        return tolerance;
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
    
    public static int getValidationFold()
    {
        return CROSS_VALIDATION_FOLD;
    }
    
    public static double[] getCValues()
    {
        return C_VALUES;
    }
    
    public static double[] getPValues()
    {
        return P_VALUES;
    }
    
    public static double[] getGammaValues()
    {
        return G_VALUES;
    }
    
    public static double getBestC()
    {
        return C;
    }
    
    public static double getBestP()
    {
        return P;
    }
    
    public static double getBestGamma()
    {
        return G;
    }
    
    public static void setStanford(StanfordCoreNLP n)
    {
        nlp = n;
    }
    
    public static StanfordCoreNLP getStanford()
    {
        return nlp;
    }
}
