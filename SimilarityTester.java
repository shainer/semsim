import cmu.arktweetnlp.Tagger;
import java.io.IOException;
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
    private Tagger tagger;
    
    public SimilarityTester(String model)
    {
        this.fc = new FeatureCollector();
        this.tagger = new Tagger();
        
        try {
            this.tagger.loadModel( Properties.getTaggerModelPath() );
        } catch (IOException e) {
            System.err.println("Error loading model for POS tagging: " + e.getLocalizedMessage());
        }
        
        loadModel(model);
    }
    
    /* TODO: load model from file */
    public void loadModel(String modelFile)
    {}
    
    public double getSimilarity(SentencePair sp)
    {
        System.out.println(":: Getting similarity for pair " + sp);
        double[] features = fc.features(sp);
        
        /* feed features to libSVM and get answer */
        return 0.0;
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
    
    public void printSimilaritiesFromFile(String filepath)
    {        
        for (String line : IOUtils.readlines(filepath)) {
            String[] fields = line.split("\t");
            
            SentencePair sp = new SentencePair(fields[0], fields[1], tagger);
            double rightAnswer = Double.parseDouble(fields[2]);
            double answer = getSimilarity(sp);
            
            System.out.println(fields[0]);
            System.out.println(fields[1]);
            System.out.println(":: Given answer: " + answer);
            System.out.println(":: Correct answer: " + rightAnswer);
            System.out.println();
        }
    }
}
