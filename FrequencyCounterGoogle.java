
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author shainer
 */
public class FrequencyCounterGoogle extends FrequencyCounter
{
    private Map<String, BigInteger> cache;
    
    public FrequencyCounterGoogle()
    {
        this.cache = new HashMap<>();
    }
    
    public @Override void createCache(List<SentencePair> pairs)
    {
        System.out.println(":: Building cache");
        destroyCache();
        Map<String, Set<String> > groups = new HashMap<>();
        
        for (SentencePair sentencePair : pairs) {
            for (POSTaggedToken tt : sentencePair.s1) {
                String token = tt.token.toLowerCase();
                String word = token + "_" + translateTag(tt.tag);
                
                int initialIndex = 0;
                while (initialIndex < token.length() && !Character.isLetter( token.charAt(initialIndex) )) {
                    initialIndex++;
                }
                
                if (initialIndex == token.length()) {
                    continue;
                }
                
                Set<String> set;
                String key;
                if (token.substring(initialIndex).length() == 1) {
                    key = "a";
                } else {
                    key = token.substring(initialIndex, initialIndex+2);
                }
                
                set = groups.get(key);
                if (set == null) {
                    set = new HashSet<>();
                }

                set.add(word);
                groups.put(key, set);
            }
        }
        
        for (Map.Entry<String, Set<String>> entry : groups.entrySet()) {
            String string = entry.getKey();
            Set<String> set = entry.getValue();
         
            try {
                String f = "googlebooks/" + string.charAt(0) + "/" + string;
                
                if (string.length() == 1) {
                    f = "googlebooks/oneLetter";
                }
                
                BufferedReader br = new BufferedReader( new FileReader(f) );
                String line;
                
                while ((line = br.readLine()) != null) {
                    String[] fields = line.split("\t");
                    String[] wordPieces = fields[0].split("_");
                    String word = wordPieces[0].toLowerCase();
                    
                    if (wordPieces.length == 2) {
                        word += "_" + wordPieces[1];
                    }

                    if (set.contains(word)) {
                        cache.put(word, new BigInteger(fields[1]));
                    }
                }
            
                br.close();
                
            } catch (IOException e) {
                System.err.println(":: Could not read from frequency files: " + e.getMessage());
            }
        }
    }
    
    public @Override void destroyCache()
    {
        cache.clear();
    }
    
    public @Override BigInteger getFrequencyCount(String token, String tag)
    {
        token = token.toLowerCase();
        String translatedTag = translateTag(tag);
        String word = token + "_" + translatedTag;
        
        if (!cache.isEmpty()) {
            
            if (cache.containsKey(word)) {
                return cache.get(word);
            }
        }
        
        String filename;
        
        int initialIndex = 0;
        while (!Character.isLetter( token.charAt(initialIndex) )) {
            initialIndex++;
        }
        
        if (token.substring(initialIndex).length() == 1) {
            filename = "oneLetter";
        } else {
            String initial = token.substring(initialIndex, initialIndex+1);
            String initials = token.substring(initialIndex, initialIndex+2);
            filename = initial + "/" + initials;
        }
        BigInteger frequencyCount = null;
        
        try {
            String filePath = "/home/shainer/source/semnlp/googlebooks/" + filename;
            BufferedReader br = new BufferedReader( new FileReader(filePath) );
            String line;
            
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\t");
                String[] w = fields[0].split("_");
                w[0] = w[0].toLowerCase();
                
                if (w.length == 2 && w[0].equals(token) && w[1].equals(translatedTag)) {
                    frequencyCount = new BigInteger( fields[1] );
                    break;
                }
            }
            
            br.close();
        } catch (IOException e) {
            return new BigInteger("0");
        }
        
        if (frequencyCount == null) {
            frequencyCount = new BigInteger("0");
        }
        
        cache.put(word, frequencyCount);
        return frequencyCount;
    }
    
    public @Override BigInteger getTotalCount()
    {
        return new BigInteger("468491999592");
    }
    
    private String translateTag(String tag)
    {
        if (tag.equals("V")) {
            return "VERB";
        }
        
        if (tag.equals("D")) {
            return "DET";
        }
        
        if (tag.equals("&")) {
            return "CONJ";
        }
        
        if (tag.equals("P")) {
            return "ADP";
        }
        
        if (tag.equals("A")) {
            return "ADJ";
        }
        
        if (tag.equals("R")) {
            return "ADV";
        }
        
        if (tag.equals("O")) {
            return "PRON";
        }
        
        if (tag.equals("N") || tag.equals("S") || tag.equals("Z") || tag.equals("M")) {
            return "NOUN";
        }
        
        return "PRT";
    }
}
