import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import libsvm.*;

public class SimilarityTest
{
    private FeatureCollector fc;
    private svm_model model;
    private StanfordCoreNLP nlp;
    
    public SimilarityTest(StanfordCoreNLP nlp)
    {
        System.out.print(":: Initializing feature collector with LSA... ");
        this.fc = new FeatureCollector();
        System.out.println("OK.");
        
        this.nlp = nlp;
        
        try {            
            System.out.print(":: Loading similarity model from file... ");
            this.model = svm.svm_load_model( Constants.getSimilarityModelPath() );
            System.out.println("OK.");
        } catch (IOException e) {
            System.err.println("Error loading model: " + e.getLocalizedMessage());
        }        
    }
    
    public double getSimilarity(SentencePair sp)
    {
        double[] features = fc.features(sp);
        svm_node[] node = new svm_node[ Constants.getFeatureNumber() ];
        
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
    
    public void correlationsFromFiles(String[] filepaths)
    {
        /* Used to compute the mean of all the correlations */
        double[] correlations = new double[filepaths.length];
        
        /* Used to compute the total Pearson correlation */
        ArrayList<Double> allAnswers = new ArrayList<>();
        ArrayList<Double> allGsAnswers = new ArrayList<>();
        
        for (int p = 0; p < filepaths.length; p++) {
            String filepath = filepaths[p];
            ArrayList<Double> answers = new ArrayList<>();
            ArrayList<Double> gsAnswers = new ArrayList<>();

            for (String line : IOUtils.readlines(filepath)) {
                String[] fields = line.split("\t");

                SentencePair sp = new SentencePair(fields[0], fields[1], nlp);
                double rightAnswer = Double.parseDouble(fields[2]);
                double answer = getSimilarity(sp);

                answers.add(answer);
                gsAnswers.add(rightAnswer);
            }

            allAnswers.addAll(answers);
            allGsAnswers.addAll(gsAnswers);
            
            Double[] scores1 = answers.toArray( new Double[answers.size()] ); 
            Double[] scores2 = gsAnswers.toArray( new Double[gsAnswers.size()] );
            double corr = Correlation.getPearsonCorrelation(scores1, scores2);
            correlations[p] = corr;
            
            System.out.println("Pearson correlation for \"" + filepath + "\": " + corr);
        }
        
        Double[] scores1 = allAnswers.toArray( new Double[allAnswers.size()] );
        Double[] scores2 = allGsAnswers.toArray( new Double[allGsAnswers.size()] );
        double totCorr = Correlation.getPearsonCorrelation(scores1, scores2);
        
        System.out.println("Mean Pearson correlation: " + average(correlations));
        System.out.println("Total Pearson correlation: " + totCorr);
    }
    
    /* Simple unweighted average */
    private double average(double[] values)
    {
        double av = 0.0;
        
        for (int i = 0; i < values.length; i++) {
            av += values[i];
        }
        
        return (av / (double)values.length);
    }
}
