
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.List;
import java.util.Properties;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author shainer
 */
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
