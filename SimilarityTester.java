
import java.util.List;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author shainer
 */
public class SimilarityTester
{
    private FeatureCollector fc;
    
    public SimilarityTester()
    {
        this.fc = new FeatureCollector();
    }
    
    /* TODO: load model from file and use it for getSimilarity */
    public void loadModel(String modelFile)
    {}
    
    public double getSimilarity(SentencePair sp)
    {
        System.out.println(":: Getting similarity for pair " + sp);
        double[] features = fc.features(sp);
        return 0.0; /* TODO: use loaded model to get answer from features */
    }
    
    public double[] getSimilarities(List<SentencePair> sps)
    {
        System.out.println(":: Getting similarities for " + sps.size() + " pairs");
        double[] sims = new double[ sps.size() ];
        int simIndex = 0;
        
        for (SentencePair sp : sps) {
            sims[simIndex++] = getSimilarity(sp);
        }
        
        return sims;
    }
}
