import cmu.arktweetnlp.Tagger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import libsvm.*;

/**
 * NOTE: use printSimilaritiesFromFile, it's quicker
 */
public class SimilarityTester
{
    private FeatureCollector fc;
    private Tagger tagger;
    private svm_model model;
    
    public SimilarityTester()
    {        
        System.out.print(":: Initializing feature collector with LSA... ");
        this.fc = new FeatureCollector();
        System.out.println("OK.");
        
        System.out.print(":: Initializing tokenizer and POS tagger... ");
        this.tagger = new Tagger();
        
        try {
            this.tagger.loadModel( Defines.getTaggerModelPath() );
            System.out.println("OK.");
            
            System.out.print(":: Loading similarity model from file... ");
            this.model = svm.svm_load_model( Defines.getSimilarityModelPath() );
            System.out.println("OK.");
        } catch (IOException e) {
            System.err.println("Error loading models: " + e.getLocalizedMessage());
        }        
    }
    
    public double getSimilarity(SentencePair sp)
    {
        double[] features = fc.features(sp);
        svm_node[] node = new svm_node[ Defines.getFeatureNumber() ];
        
        for (int i = 0; i < features.length; i++) {
            node[i] = new svm_node();
            node[i].index = i+1;
            node[i].value = features[i];
        }
        
        /* Scaling similarity ? */
        double sim = svm.svm_predict(model, node);
        //sim *= (5.0 / 8.0);
        //sim = (5.0 * (sim + 1.0)) / 8.0;

        if (sim < 0.0) {
            sim = 0.0;
        } else if (sim > 5.0) {
            sim = 5.0;
        }
        
        return sim;
    }
    
    public double[] getSimilarities(List<SentencePair> sps)
    {
        //System.out.println(":: Getting similarities for " + sps.size() + " pairs");
        double[] sims = new double[ sps.size() ];
        int simIndex = 0;
        
        for (SentencePair sp : sps) {
            sims[simIndex++] = getSimilarity(sp);
        }
        
        return sims;
    }
    
    public void printSimilaritiesFromFile(String filepath)
    {
        int good = 0;
        int lineCount = 0;
        double maxSim = Double.MIN_VALUE;
        double minSim = Double.MAX_VALUE;
        
        for (String line : IOUtils.readlines(filepath)) {
            lineCount++;
            String[] fields = line.split("\t");

            SentencePair sp = new SentencePair(fields[0], fields[1], tagger);
            double rightAnswer = Double.parseDouble(fields[2]);
            double answer = getSimilarity(sp);
            
            System.out.println(answer);

            if (answer > maxSim) { maxSim = answer; }
            if (answer < minSim) { minSim = answer; }

            /* TODO: lower this tolerance value! */
            if (Math.abs(answer - rightAnswer) <= Defines.getTolerance()) {
                good++;
            }
        }
        
        System.out.println(good + " over " + lineCount);
        System.out.println("Max: " + maxSim + ", min: " + minSim);
    }
}
