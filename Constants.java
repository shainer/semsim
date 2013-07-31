import libsvm.svm_parameter;

public class Constants
{
    private static final int FEATURE_SIZE = 19;
    private static final String[] stopWords = {"i", "a", "about", "an", "are", "as", "at", "be", "by", "for", "from",
                                               "how", "in", "is", "it", "of", "on", "or", "that", "the", "this", "to",
                                               "was", "what", "when", "where", "who", "will", "with", "the", "'s", "did",
                                               "have", "has", "had", "were", "'ll"};
    private static final String wordFrequenciesPath = "word-frequencies.txt";
    
    /*
     * These paths and parameters are all configurable using a Properties file, so
     * we provide also setters and they are not declared final. These below are the
     * default values.
     */
    private static String googleCorpusFolder = "googlebooks/";
    private static String similarityModelPath = "similarityModel.txt";
    private static String lsaMatrixPath = "lsa_matrix.txt";
    private static int LSA_VECTOR_SIZE = 100;
    
    /* Constants for CrossValidation.java */
    private static final int CROSS_VALIDATION_FOLD = 10;
    private static final double[] C_VALUES = {1, 2, 5, 10, 20, 50, 100, 200, 500, 1000};
    private static final double[] P_VALUES = {1, 0.5, 0.2, 0.1, 0.05, 0.02, 0.01};
    private static final double[] G_VALUES = {2, 1, 0.5, 0.2, 0.1, 0.05, 0.02, 0.01, 0.005, 0.002};
    
    /* Optimal parameters for the regressor */
    private static final double C = 1;
    private static final double P = 0.02;
    private static final double G = 2;
        
    public static int getFeatureNumber()
    {
        return FEATURE_SIZE;
    }
    
    public static String[] getStopWords()
    {
        return stopWords;
    }
    
    public static String getGoogleCorpusFolder()
    {
        return googleCorpusFolder;
    }
    
    public static String getWordFrequenciesPath()
    {
        return wordFrequenciesPath;
    }
    
    public static String getSimilarityModelPath()
    {
        return similarityModelPath;
    }
    
    public static String getLSAMatrixPath()
    {
        return lsaMatrixPath;
    }
    
    public static int getLSAVectorSize()
    {
        return LSA_VECTOR_SIZE;
    }
    
    public static svm_parameter getSVMParameters()
    {
        svm_parameter param = new svm_parameter();
        
        /* 
         * These parameters are taken from the Takelab suggestions.
         * See report for an explanation of their meaning.
         */
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
    
    public static void setGoogleCorpusFolder(String f)
    {
        googleCorpusFolder = f;
    }
    
    public static void setLsaMatrixPath(String matrixFile, int vectorSize)
    {
        lsaMatrixPath = matrixFile;
        LSA_VECTOR_SIZE = vectorSize;
    }
    
    public static void setSimilarityModelPath(String f)
    {
        similarityModelPath = f;
    }
}
