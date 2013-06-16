/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import cmu.arktweetnlp.Tagger.TaggedToken;
import java.util.Map;
import java.io.File;
import java.net.URL;
import java.io.IOException;
import edu.cmu.lti.ws4j.WS4J;
import edu.cmu.lti.ws4j.util.*;
import edu.cmu.lti.ws4j.impl.*;

/**
 * @author shainer
 */
public class FeatureCollector
{
    private SentencePair sp;
    
    public FeatureCollector()
    {}
    
    public HashMap<String, Double> features(SentencePair sp)
    {
        this.sp = sp;
        HashMap<String, Double> featureMap = new HashMap<String, Double>();
        
        double[] overlaps = ngramOverlaps();
        double[] numberOverlaps = numberOverlaps();
        featureMap.put("UniGramOverlap", overlaps[0]);
        featureMap.put("BiGramOverlap", overlaps[1]);
        featureMap.put("TriGramOverlap", overlaps[2]);
        featureMap.put("NumberOverlap1", numberOverlaps[0]);
        featureMap.put("NumberOverlap2", numberOverlaps[1]);
        featureMap.put("NumberOverlap3", numberOverlaps[2]);
        featureMap.put("CapitalizedOverlap", capitalizedOverlap());
        featureMap.put("StockOverlap", stockOverlap());
        featureMap.put("WordNetOverlap", wordnetOverlap());
        
        printMap(featureMap);
        
        return featureMap;
    }
    
    private void printMap(HashMap<String, Double> map)
    {
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            String string = entry.getKey();
            Double double1 = entry.getValue();
            
            System.out.println(string + ": " + double1);
        }
    }
    
    private double wordnetOverlap()
    {        
        return harmonicMean(pwn(sp.s1, sp.s2), pwn(sp.s2, sp.s1));
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
            
            String token = tt.token;
            res += wordnetScore(token, s2);
        }
        
        res /= s2.size();
        return res;
    }
    
    private double wordnetScore(String word1, List<TaggedToken> sentence)
    {
        double maxScore = 0.0;
        
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
    
    private double[] ngramOverlaps()
    {
        double[] overlaps = new double[3];
        
        Set<String> s1unigrams = new HashSet<String>();
        Set<String> s1bigrams = new HashSet<String>();
        Set<String> s1trigrams = new HashSet<String>();
        Set<String> s2unigrams = new HashSet<String>();
        Set<String> s2bigrams = new HashSet<String>();
        Set<String> s2trigrams = new HashSet<String>();
        
        for (int i = 0; i < sp.s1.size(); i++) {
            String token1 = sp.s1.get(i).token;
            String token2 = "", token3 = "";
            s1unigrams.add(token1);
            
            if (i < sp.s1.size() - 1) {
                token2 = sp.s1.get(i+1).token;
                s1bigrams.add(token1 + " " + token2);
            }
            
            if (i < sp.s1.size() - 2) {
                token3 = sp.s1.get(i+2).token;
                s1trigrams.add(token1 + " " + token2 + " " + token3);
            }
        }
        
        for (int i = 0; i < sp.s2.size(); i++) {
            String token1 = sp.s2.get(i).token;
            String token2 = "", token3 = "";
            s2unigrams.add(token1);
            
            if (i < sp.s2.size() - 1) {
                token2 = sp.s2.get(i+1).token;
                s2bigrams.add(token1 + " " + token2);
            }
            
            if (i < sp.s2.size() - 2) {
                token3 = sp.s2.get(i+2).token;
                s2trigrams.add(token1 + " " + token2 + " " + token3);
            }
        }
        
        overlaps[0] = setOverlap(s1unigrams, s2unigrams);
        overlaps[1] = setOverlap(s1bigrams, s2bigrams);
        overlaps[2] = setOverlap(s1trigrams, s2trigrams);
        
        return overlaps;
    }

    private double[] numberOverlaps()
    {
        Set<Double> n1 = numberTokens(sp.s1);
        Set<Double> n2 = numberTokens(sp.s2);        
        double[] results = new double[3];
        
        results[0] = Math.log(1 + n1.size() + n2.size());
        results[1] = (n1.containsAll(n2) || n2.containsAll(n1)) ? 1.0 : 0.0;
        results[2] = 1.0;
        results[2] /= (n1.size() + n2.size());
        
        n1.retainAll(n2);
        results[2] *= n1.size() * 2;
        
        return results;
    }
    
    private Set<Double> numberTokens(List<TaggedToken> sentence)
    {
        Set<Double> n = new HashSet<Double>();
        
        for (TaggedToken t : sentence) {
            if (t.tag.equals("$")) {
                n.add( Double.parseDouble(t.token) );
            }
        }
        
        return n;
        
    }
    
    private double capitalizedOverlap()
    {
        Set<String> capTokens1 = findCapitalizedWords(sp.s1);
        Set<String> capTokens2 = findCapitalizedWords(sp.s2);
        return setOverlap(capTokens1, capTokens2);
    }
    
    private Set<String> findCapitalizedWords(List<TaggedToken> sentence)
    {
        Set<String> cap = new HashSet<String>();
        
        for (TaggedToken tt : sentence) {
            String token = tt.token;
            
            if (token.length() > 1 && Character.isUpperCase( token.charAt(0) )) {
                cap.add(token);
            }
        }
        
        return cap;
    }

    private double stockOverlap()
    {
        Set<String> stockItems1 = findStockItems(sp.s1);
        Set<String> stockItems2 = findStockItems(sp.s2);
        
        return setOverlap(stockItems1, stockItems2);
    }
   
    private Set<String> findStockItems(List<TaggedToken> sentence)
    {
        Set<String> stockItems = new HashSet<String>();
        
        for (int i = 0; i < sentence.size(); i++) {
            String token = sentence.get(i).token;
            
            if (isUpper(token)) {
                if (token.startsWith(".") || (i > 0 && sentence.get(i-1).token.equals("."))) {
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
    
    private <T> double setOverlap(Set<T> set1, Set<T> set2)
    {
        double overlap = (set1.size() + set2.size());
        set1.retainAll(set2);
        overlap /= set1.size();
        overlap = Math.pow(overlap, -1.0);
        overlap *= 2;
        
        return overlap;
    }

}
