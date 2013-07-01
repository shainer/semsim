/*
 * Feature extraction module.
 */

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import cmu.arktweetnlp.Tagger.TaggedToken;
import edu.cmu.lti.ws4j.WS4J;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.Iterator;

/**
 * @author shainer
 */
public class FeatureCollector
{
    private SentencePair sp;    
    private FrequencyCounter counter;
    
    private double[] features;
    private final int FEATURE_SIZE = Properties.getFeatureNumber();
    private int featureIndex = 0;
    
    /*
     * When training using the SemCor samples, the frequency counts we need are all available
     * in a (relatively) small file, so they are taken from there.
     */
    public FeatureCollector(String frequencyFile)
    {
        this.counter = new FrequencyCounterFile(frequencyFile);
        this.features = new double[FEATURE_SIZE];
    }
    
    /*
     * When extracting features for a generic sentence pair, we need to look into the Google
     * NGram corpus.
     */
    public FeatureCollector()
    {
        this.counter = new FrequencyCounterGoogle();
        this.features = new double[FEATURE_SIZE];
    }
    
    /*
     * When extracting features for a huge number of sentence pairs, this builds a cache of
     * the frequency counts that speeds up the lookup in the Google Corpus. For the "file" corpus,
     * these methods do nothing.
     */
    public void initialize(List<SentencePair> sps)
    {
        this.counter.createCache(sps);
    } 
    
    public void deinitialize()
    {
        this.counter.destroyCache();
    }
    
    /*
     * Actual feature extraction.
     */
    public double[] features(SentencePair sp)
    {
        this.sp = sp;
        featureIndex = 0;
        
        ngramOverlaps();
        numberOverlaps();
        capitalizedOverlap();
        stockOverlap();
        wordnetOverlap();
        weightedWords();
        sentenceLength();
        
        /* Rounds features values at their third decimal digit. */
        for (int i = 0; i < features.length; i++) {
            if (!Double.isNaN(features[i]) && !Double.isInfinite(features[i])) {
                BigDecimal bd = new BigDecimal( features[i] );
                bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);
                features[i] = bd.doubleValue();
            }
        }
        
        //printFeatures();
        return features;
    }
    
    private void printFeatures()
    {
        System.out.println(":: Features for sentences :: ");
        System.out.println(sp);
        
        for (int i = 0; i < FEATURE_SIZE; i++) {
            System.out.println(features[i]);
        }
    }
    
    /*
     * Normalized differences, feature (A)
     */
    private void sentenceLength()
    {
        features[featureIndex++] = (double)Math.abs(sp.s2.size() - sp.s1.size());
    } 
    
    /*
     * Weighted Word Overlap
     * Normalized Differences, feature (C)
     */
    private void weightedWords()
    {
        BigInteger totalFrequencyCount = counter.getTotalCount();
        
        double s1TotalContent = 0.0;
        double s2TotalContent = 0.0;
        double sharedContent = 0.0;
        
        for (TaggedToken tt : sp.s1) {
            double icw = informationContent(tt, totalFrequencyCount);
            s1TotalContent += icw;
            
            if (sentenceContains(sp.s2, tt)) {
                sharedContent += icw;
            }
        }
        
        for (TaggedToken tt : sp.s2) {
            double icw = informationContent(tt, totalFrequencyCount);
            s2TotalContent += icw;
            
            if (sentenceContains(sp.s1, tt)) {
                sharedContent += icw;
            }
        }
    
        double wwo = harmonicMean(sharedContent / s1TotalContent, sharedContent / s2TotalContent);
        features[featureIndex++] = wwo;
        features[featureIndex++] = Math.abs(s2TotalContent - s1TotalContent);
    }
    
    private double informationContent(TaggedToken tt, BigInteger totalFrequencyCount)
    {
        char[] uselessTags = {'#', '@', '~', 'U', 'E', '$', ',', 'G'};
        
        for (int i = 0; i < uselessTags.length; i++) {
            if (tt.tag.charAt(0) == uselessTags[i]) {
                return 0.0;
            }
        }
        
        BigInteger frequency = counter.getFrequencyCount(tt.token, tt.tag);
        
        if (frequency.doubleValue() == 0) {
            return 0;
        }
        
        return Math.log(totalFrequencyCount.doubleValue() / frequency.doubleValue());
    }
    
    private boolean sentenceContains(List<TaggedToken> s, TaggedToken tt)
    {
        for (TaggedToken sTT : s) {
            if (sTT.token.equals(tt.token) && sTT.tag.equals(tt.tag)) {
                return true;
            }
        }
        
        return false;
    }
    
    /*
     * WordNet-Augmented Word Overlap
     */
    private void wordnetOverlap()
    {        
        features[featureIndex++] = harmonicMean(pwn(sp.s1, sp.s2), pwn(sp.s2, sp.s1));
    }
    
    private double harmonicMean(double d1, double d2)
    {
        double mean = 2.0;
        mean /= ((1.0 / d1) + (1.0 / d2));
        return mean;
    }
    
    private double pwn(List<TaggedToken> s1, List<TaggedToken> s2)
    {
        double res = 0.0;
        
        for (TaggedToken tt : s1) {
            if (tt.tag.equals(",") || tt.tag.equals("$")) {
                continue;
            }

            res += wordnetScore(tt.token.toLowerCase(), s2);
        }
        
        res /= s2.size();
        return res;
    }
    
    private double wordnetScore(String word1, List<TaggedToken> sentence)
    {
        double maxScore = 0.0;
        word1 = word1.toLowerCase();
        
        for (TaggedToken tt : sentence) {
            if (tt.token.equals(word1)) {
                return 1.0;
            }
        }
        
        for (TaggedToken tt : sentence) {
            String otherWord = tt.token;
            double score = WS4J.runPATH(word1, otherWord);
            
            if (score > maxScore) {
                maxScore = score;
            }
        }
        
        return maxScore;
    }
    
    /*
     * NGram Overlap(s)
     */
    private void ngramOverlaps()
    {
        Set<String> s1unigrams = new HashSet<>();
        Set<String> s1bigrams = new HashSet<>();
        Set<String> s1trigrams = new HashSet<>();
        Set<String> s2unigrams = new HashSet<>();
        Set<String> s2bigrams = new HashSet<>();
        Set<String> s2trigrams = new HashSet<>();
        
        for (int i = 0; i < sp.s1.size(); i++) {
            String token1 = sp.s1.get(i).token.toLowerCase();
            String token2 = "", token3;
            s1unigrams.add(token1);
            
            if (i < sp.s1.size() - 1) {
                token2 = sp.s1.get(i+1).token.toLowerCase();
                s1bigrams.add(token1 + " " + token2);
            }
            
            if (i < sp.s1.size() - 2) {
                token3 = sp.s1.get(i+2).token.toLowerCase();
                s1trigrams.add(token1 + " " + token2 + " " + token3);
            }
        }
        
        for (int i = 0; i < sp.s2.size(); i++) {
            String token1 = sp.s2.get(i).token.toLowerCase();
            String token2 = "", token3 = "";
            s2unigrams.add(token1);
            
            if (i < sp.s2.size() - 1) {
                token2 = sp.s2.get(i+1).token.toLowerCase();
                s2bigrams.add(token1 + " " + token2);
            }
            
            if (i < sp.s2.size() - 2) {
                token3 = sp.s2.get(i+2).token.toLowerCase();
                s2trigrams.add(token1 + " " + token2 + " " + token3);
            }
        }
        
        features[featureIndex++] = setOverlap(s1unigrams, s2unigrams);
        features[featureIndex++] = setOverlap(s1bigrams, s2bigrams);
        features[featureIndex++] = setOverlap(s1trigrams, s2trigrams);
    }

    /*
     * Numbers Overlap
     */
    private void numberOverlaps()
    {
        Set<Double> n1 = numberTokens(sp.s1);
        Set<Double> n2 = numberTokens(sp.s2);        
        double[] results = new double[3];
        
        results[0] = Math.log(1 + n1.size() + n2.size());
        results[1] = (modifiedContainsAll(n1, n2)) || (modifiedContainsAll(n2, n1)) ? 1.0 : 0.0;
        results[2] = 1.0;
        results[2] /= (n1.size() + n2.size());
        
        Set<Double> nIntersect = new HashSet<>();
        
        for (Iterator<Double> it = n1.iterator(); it.hasNext(); ) {
            Double d = it.next();
            
            if (modifiedContains(n2, d)) {
                nIntersect.add(d);
            }
        }
        
        results[2] *= nIntersect.size() * 2;
        
        if (Double.isNaN(results[2])) {
            results[2] = 0.0;
        }
        
        features[featureIndex++] = results[0];
        features[featureIndex++] = results[1];
        features[featureIndex++] = results[2];
    }
    
    private boolean modifiedContains(Set<Double> n1, Double d)
    {
        String number = d.toString();
        
        for (Iterator<Double> it = n1.iterator(); it.hasNext(); ) {
            Double d1 = it.next();
            
            if (d1 == d) {
                return true;
            }

            String number1 = d1.toString();

            if (number1.startsWith(number) || number.startsWith(number1) ) {
                return true;
            }

        }
        
        return false;
    }
    
    private boolean modifiedContainsAll(Set<Double> n1, Set<Double> n2)
    {
        if (n1.isEmpty() || n2.isEmpty()) {
            return false;
        }
        
        for (Iterator<Double> it = n1.iterator(); it.hasNext(); ) {
            Double d = it.next();
            
            if (!modifiedContains(n2, d)) {
                return false;
            }
        }
        
        return true;
    }
    
    private Set<Double> numberTokens(List<TaggedToken> sentence)
    {
        Set<Double> n = new HashSet<>();
        
        for (TaggedToken t : sentence) {
            if (t.tag.equals("$")) {
                try {
                    n.add( Double.parseDouble(t.token) );
                } catch (NumberFormatException nfe) {
                    continue;
                }
            }
        }
        
        return n;
    }
    
    /*
     * Named Entity Features: overlap of capitalized words
     */
    private void capitalizedOverlap()
    {
        Set<String> capTokens1 = findCapitalizedWords(sp.s1);
        Set<String> capTokens2 = findCapitalizedWords(sp.s2);
        double presence = (!capTokens1.isEmpty() || !capTokens2.isEmpty()) ? 1.0 : 0.0;
        
        features[featureIndex++] = setOverlap(capTokens1, capTokens2);
        features[featureIndex++] = presence;
    }
    
    private Set<String> findCapitalizedWords(List<TaggedToken> sentence)
    {
        Set<String> cap = new HashSet<>();
        
        for (int i = 0; i < sentence.size(); i++) {
            String token = sentence.get(i).token;
            
            if (token.length() > 1 && Character.isUpperCase( token.charAt(0) ) &&
                    (i == 0 || !sentence.get(i-1).token.equals("."))) {
                cap.add(token);
            }
        }
        
        return cap;
    }

    /*
     * Named Entity Features: overlap of stock index symbols
     */
    private void stockOverlap()
    {
        Set<String> stockItems1 = findStockItems(sp.s1);
        Set<String> stockItems2 = findStockItems(sp.s2);
        double presence = (!stockItems1.isEmpty() || !stockItems2.isEmpty()) ? 1.0 : 0.0;
        
        features[featureIndex++] = setOverlap(stockItems1, stockItems2);
        features[featureIndex++] = presence;
    }
   
    private Set<String> findStockItems(List<TaggedToken> sentence)
    {
        Set<String> stockItems = new HashSet<>();
        
        for (int i = 0; i < sentence.size(); i++) {
            String token = sentence.get(i).token;
            
            if (isUpper(token) && isWord(token)) {
                if (token.startsWith(".") || (i > 0 && sentence.get(i-1).token.equals("."))) {
                    System.out.println("\"" + token + "\" is a stock item");
                    stockItems.add(token);
                }
            }
        }
        
        return stockItems;
    }
    
    private boolean isUpper(String s)
    {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLetter( s.charAt(i) ) && Character.isLowerCase( s.charAt(i) )) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isWord(String s)
    {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLetter( s.charAt(i) )) {
                return true;
            }
        }
        
        return false;
    }
    
    private <T> double setOverlap(Set<T> set1, Set<T> set2)
    {
        double overlap = (set1.size() + set2.size());
        set1.retainAll(set2);

        if (!set1.isEmpty()) {
            overlap /= set1.size();
            overlap = Math.pow(overlap, -1.0);
            overlap *= 2;
        } else {
            overlap = 0;
        }
        
        return overlap;
    }

}
