/*
 * Entry point.
 */

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.List;
import java.util.Properties;

public class SemanticSimilarity
{
    public static void main(String[] args)
    {
        if (args.length == 0) {
            System.err.println("No parameter passed. Refer to the README for instructions.");
            System.exit(-1);
        }
        
        int paramIndex;
        
        Properties prop = new Properties();
        prop.put("annotators", "tokenize, ssplit, pos, lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(prop);
        System.out.println(":: Stanford NLP pipeline initialized correctly.");
        
        if ((paramIndex = findParameter(args, "--training")) != -1) {
            trainSystem(args, paramIndex, pipeline);
        } else { /* proceed with testing the system */
            SimilarityTest m = new SimilarityTest(pipeline);
            m.correlationsFromFiles(args);
        }
    }
    
    /*
     * Returns the index of a string in the command line array, or -1 if it
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
    
    /*
     * Train a similarity system from the given training files.
     * See README for knowing about the format of training files we recognize.
     */
    private static void trainSystem(String[] args, int paramIndex, StanfordCoreNLP nlp)
    {        
        /* No file has been passed */
        if (paramIndex == args.length - 1) {
            System.err.println("No training file after --training");
            System.exit(-1);
        }
        
        /* Copy all the arguments after the --training string */
        String[] trainingFiles = new String[args.length - paramIndex - 1];
        int t = 0;
        
        for (int i = paramIndex + 1; i < args.length; i++) {
            trainingFiles[t++] = args[i];
        }


        /* Extract samples, learn and store the model */
        SimilarityLearner sl = new SimilarityLearner(nlp);
        List<TrainingSample> samples = sl.extractFeatures(trainingFiles);
        sl.learnModel(samples);
    }
} 

