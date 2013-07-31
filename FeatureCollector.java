import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ListIterator;
import edu.cmu.lti.ws4j.WS4J;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;

/*
 * Feature extraction.
 */
public class FeatureCollector
{
    private SentencePair sp;    
    private FrequencyCounter counter;
    private LSA lsa;
    private HashMap<POSTaggedToken, Double> icwMap;
    
    private double[] features;
    private final int FEATURE_SIZE = Constants.getFeatureNumber();
    private int featureIndex = 0;
    
    /*
     * When training using the Semeval-2012 samples, the frequency counts we need are all available
     * in a (relatively) small text file, so they are taken from there.
     */
    public FeatureCollector(String frequencyFile)
    {
        this.counter = new FrequencyCounterFile(frequencyFile);
        this.features = new double[FEATURE_SIZE];
        this.lsa = new LSA();
        this.icwMap = new HashMap<>();
    }
    
    /*
     * When extracting features for a generic sentence pair, we need to look into the bigger Google
     * NGram corpus.
     */
    public FeatureCollector()
    {
        this.counter = new FrequencyCounterGoogle();
        this.features = new double[FEATURE_SIZE];
        this.lsa = new LSA();
        this.icwMap = new HashMap<>();
    }
    
    /*
     * Actual feature extraction.
     */
    public double[] features(SentencePair sp)
    {
        this.sp = sp;
        featureIndex = 0;
        features = new double[ Constants.getFeatureNumber() ];
                
        /* Feature 1, 2 */
        capitalizedOverlap();
        /* Feature 3, 4 */
        stockOverlap();
        
        /* From now on, case ceases to matter, so we simply lower-case all tokens */
        for (ListIterator<POSTaggedToken> it = sp.s1.listIterator(); it.hasNext();) {
            POSTaggedToken tt = it.next();
            tt.token = tt.token.toLowerCase();
            it.set(tt);
        }
        
        for (ListIterator<POSTaggedToken> it = sp.s2.listIterator(); it.hasNext();) {
            POSTaggedToken tt = it.next();
            tt.token = tt.token.toLowerCase();
            it.set(tt);
        }
        
        /* Feature 5, 6, 7 */
        ngramOverlaps(false);
        
        /* Feature 8, 9, 10 */
        ngramOverlaps(true);
        
        /* Feature 11, 12, 13 */
        numberOverlaps();
        /* Feature 14 */
        wordnetOverlap();
        /* Feature 15, 16 */
        weightedWords();
        /* Feature 17 */
        sentenceLength();
        /* Feature 18, 19 */
        vectorSpaceSimilarity();
        
        /* Rounds features values at their third decimal digit. */
        for (int i = 0; i < features.length; i++) {
            BigDecimal bd = new BigDecimal( features[i] );
            bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);
            features[i] = bd.doubleValue();
        }
        
        return Arrays.copyOf(features, features.length);
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
     * Taken from Takelab implementation
     */
    private void sentenceLength()
    {
        int size1 = sp.s1.size();
        int size2 = sp.s2.size();
        features[featureIndex++] = Math.abs(size1 - size2) / (Math.max(size1, size2) + Math.exp(-5));
    } 
    
    /*
     * Weighted Word Overlap
     * Normalized Differences, feature (C)
     */
    private void weightedWords()
    {        
        double s1TotalContent = 0.0;
        double s2TotalContent = 0.0;
        double sharedContent = 0.0;
        
        for (POSTaggedToken tt : sp.s1) {
            double icw = informationContent(tt);
            s1TotalContent += icw;
            
            if (sp.s2.contains(tt)) {
                sharedContent += icw;
            }
        }
        
        for (POSTaggedToken tt : sp.s2) {
            double icw = informationContent(tt);
            s2TotalContent += icw;
            
            if (sp.s1.contains(tt)) {
                sharedContent += icw;
            }
        }
    
        double wwo = harmonicMean(sharedContent / s1TotalContent, sharedContent / s2TotalContent);
        features[featureIndex++] = wwo;
        
        /* Normalized difference of aggregated word contents */
        features[featureIndex++] = Math.abs(s1TotalContent - s2TotalContent) / (Math.max(s2TotalContent, s1TotalContent) + Math.exp(-5));
    }
    
    private double informationContent(POSTaggedToken tt)
    {
        /* Hit in our little cache */
        if (icwMap.containsKey(tt)) {
            return icwMap.get(tt);
        }
        
        BigInteger totalFrequencyCount = counter.getTotalCount();
        
        /* See the FrequencyCounter* classes */
        BigInteger frequency = counter.getFrequencyCount(tt.token, tt.tag);
                
        if (frequency.doubleValue() == 0) {
            return 0;
        }
        
        double icw = Math.log(totalFrequencyCount.doubleValue() / frequency.doubleValue());
        icwMap.put(tt, icw);
        return icw;
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
        return (d1 + d2 > 0 ) ? ((2 * d1 * d2) / (d1 + d2)) : 0.0;
    }
    
    private double pwn(List<POSTaggedToken> s1, List<POSTaggedToken> s2)
    {
        double res = 0.0;
        
        for (POSTaggedToken tt : s1) {
            if (tt.tag.equals("$")) {
                continue;
            }

            res += wordnetScore(tt.token, s2);
        }
        
        res /= (double)s2.size();
        return res;
    }
    
    private double wordnetScore(String word1, List<POSTaggedToken> sentence)
    {
        double maxScore = 0.0;
        
        for (POSTaggedToken tt : sentence) {
            if (tt.token.equals(word1)) {
                return 1.0;
            }
        }
        
        for (POSTaggedToken tt : sentence) {
            String otherWord = tt.token;
            double score = WS4J.runPATH(word1, otherWord); /* path-len similarity in WordNet */
            
            if (score > maxScore) {
                maxScore = score;
            }
        }
        
        return maxScore;
    }
    
    /*
     * NGram Overlap(s)
     */
    private void ngramOverlaps(boolean lemmatized)
    {
        Set<String> s1unigrams = new HashSet<>();
        Set<String> s1bigrams = new HashSet<>();
        Set<String> s1trigrams = new HashSet<>();
        Set<String> s2unigrams = new HashSet<>();
        Set<String> s2bigrams = new HashSet<>();
        Set<String> s2trigrams = new HashSet<>();
        
        for (int i = 0; i < sp.s1.size(); i++) {
            String token1 = (lemmatized) ? sp.s1.get(i).lemma : sp.s1.get(i).token;
            String token2 = "", token3;
            s1unigrams.add(token1);
            
            if (i < sp.s1.size() - 1) {
                token2 = (lemmatized) ? sp.s1.get(i+1).lemma : sp.s1.get(i+1).token;;
                s1bigrams.add(token1 + " " + token2);
            }
            
            if (i < sp.s1.size() - 2) {
                token3 = (lemmatized) ? sp.s1.get(i+2).lemma : sp.s1.get(i+2).token;;
                s1trigrams.add(token1 + " " + token2 + " " + token3);
            }
        }
        
        for (int i = 0; i < sp.s2.size(); i++) {
            String token1 = (lemmatized) ? sp.s2.get(i).lemma : sp.s2.get(i).token;
            String token2 = "", token3 = "";
            s2unigrams.add(token1);
            
            if (i < sp.s2.size() - 1) {
                token2 = (lemmatized) ? sp.s2.get(i+1).lemma : sp.s2.get(i+1).token;;
                s2bigrams.add(token1 + " " + token2);
            }
            
            if (i < sp.s2.size() - 2) {
                token3 = (lemmatized) ? sp.s2.get(i+2).lemma : sp.s2.get(i+2).token;;
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

            /* As specified in the paper, numbers like 6.23 and 6.2333 are considered equal */
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
    
    private Set<Double> numberTokens(List<POSTaggedToken> sentence)
    {
        Set<Double> n = new HashSet<>();
        
        for (POSTaggedToken t : sentence) {
            
            /* This tag is used also for tokens like "four", so we need to catch the exception */
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
    
    private Set<String> findCapitalizedWords(List<POSTaggedToken> sentence)
    {
        Set<String> cap = new HashSet<>();
        
        for (int i = 0; i < sentence.size(); i++) {
            String token = sentence.get(i).token;
            
            /* Doesn't confuse a capitalized word with a stock item, treated by another feature */
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
   
    private Set<String> findStockItems(List<POSTaggedToken> sentence)
    {
        Set<String> stockItems = new HashSet<>();
        
        for (int i = 0; i < sentence.size(); i++) {
            String token = sentence.get(i).token;
            
            /* Here we pay attention to the fact that we are not sure the "." was placed in the same token */
            if (isUpper(token) && isWord(token)) {
                if (token.startsWith(".") || (i > 0 && sentence.get(i-1).token.equals("."))) {
                    stockItems.add(token);
                }
            }
        }
        
        return stockItems;
    }
    
    /*
     * Vector Space Sentence Similarity
     */
    private void vectorSpaceSimilarity()
    {
        double[] U1 = sentenceVector(sp.s1, false);
        double[] U2 = sentenceVector(sp.s2, false);
        double[] U1Weighted = sentenceVector(sp.s1, true);
        double[] U2Weighted = sentenceVector(sp.s2, true);
        
        features[featureIndex++] = dotProduct(U1, U2) / (double)(U1.length * U2.length);
        features[featureIndex++] = dotProduct(U1Weighted, U2Weighted) / (double)(U1Weighted.length * U2Weighted.length);
    }
    
    private double[] sentenceVector(List<POSTaggedToken> sentence, boolean weighted)
    {
        double[] V = new double[ Constants.getLSAVectorSize() ];
        
        for (POSTaggedToken tt : sentence) {
            double[] word = lsa.getWordVector(tt);
            
            /* The information content is surely in cache, so no overhead added here */
            double icw = informationContent(tt);
            
            for (int i = 0; i < V.length; i++) {
                if (weighted) {
                    word[i] *= icw;
                }
                
                V[i] += word[i];
            }
        }
        
        return V;
    }
    
    private double dotProduct(double[] v1, double[] v2)
    {
        double sum = 0.0;
        
        for (int i = 0; i < v1.length; i++) {
            sum += (v1[i] * v2[i]);
        }
        
        return sum;
    }
    
    /* An overlap is a measure between 0 and 1, where 1 indicates two identical sets */
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
}
