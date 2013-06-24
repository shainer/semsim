
import java.util.List;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author shainer
 */
public class Properties
{
    private static final int FEATURE_SIZE = 14;
    private static final String[] stopWords = {"i", "a", "about", "an", "are", "as", "at", "be", "by", "for", "from",
                                               "how", "in", "is", "it", "of", "on", "or", "that", "the", "this", "to",
                                               "was", "what", "when", "where", "who", "will", "with", "the", "'s", "did",
                                               "have", "has", "had", "were", "'ll"};
    
    public static int getFeatureNumber()
    {
        return FEATURE_SIZE;
    }
    
    public static String[] getStopWords()
    {
        return stopWords;
    }
}
