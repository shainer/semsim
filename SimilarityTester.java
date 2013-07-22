import cmu.arktweetnlp.Tagger;
import java.io.IOException;
import java.util.List;
import libsvm.*;

/**
 *
 * @author shainer
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
            this.tagger.loadModel( Properties.getTaggerModelPath() );
            System.out.println("OK.");
            
            System.out.print(":: Loading similarity model from file... ");
            this.model = svm.svm_load_model( Properties.getSimilarityModelPath() );
            System.out.println("OK.");
        } catch (IOException e) {
            System.err.println("Error loading models: " + e.getLocalizedMessage());
        }        
    }
    
    public double getSimilarity(SentencePair sp)
    {
        //System.out.println(":: Getting similarity for pair " + sp);
        double[] features = fc.features(sp);
        svm_node[] node = new svm_node[ Properties.getFeatureNumber() ];
        
        for (int i = 0; i < features.length; i++) {
            node[i] = new svm_node();
            node[i].index = i+1;
            node[i].value = features[i];
        }
        
        double sim = svm.svm_predict(model, node);
        return (5.0 * (sim + 61.0) / (double)(10.0 + 61.0));
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
        
        for (String line : IOUtils.readlines(filepath)) {
            lineCount++;
            String[] fields = line.split("\t");
            
            SentencePair sp = new SentencePair(fields[0], fields[1], tagger);
            double rightAnswer = Double.parseDouble(fields[2]);
            double answer = getSimilarity(sp);
            
            if (Math.abs(answer - rightAnswer) <= 0.8) {
                good++;
            }
            
            System.out.println(fields[0]);
            System.out.println(fields[1]);
            //System.out.println(":: Given answer: " + answer);
            //System.out.println(":: Correct answer: " + rightAnswer);
            System.out.println();
        }
        
        System.out.println(good + " over " + lineCount);
    }
}
