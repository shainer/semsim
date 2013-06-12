/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.HashMap;

/**
 *
 * @author shainer
 */
public class SimilarityLearner
{
    private FeatureCollector fc;
    
    public SimilarityLearner()
    {
        fc = new FeatureCollector();
    }
        
    public void learnModel(String directoryPath)
    {
        // to be implemented
    }
    
    public double getSimilarity(SentencePair sp)
    {
        HashMap<String, Double> fs = fc.features(sp);
        return 0.0;
    }
    
}
