import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/*
 * When we don't know the words we are dealing with, we use the Google NGram Corpus, a bit
 * preprocessed to avoid slowing down too much (see report).
 */
public class FrequencyCounterGoogle implements FrequencyCounter
{
    private Map<String, BigInteger> cache;
    
    public FrequencyCounterGoogle()
    {
        this.cache = new HashMap<>();
    }
    
    public @Override BigInteger getFrequencyCount(String token, String tag)
    {
        token = token.toLowerCase();
        String translatedTag = translateTag(tag);
        String word = token + "_" + translatedTag;
        
        if (cache.containsKey(word)) {
            return cache.get(word);
        }
        
        String filename;
        
        int initialIndex = 0;
        
        /* When considering initials, skips punctuactions if they exist */
        while (!Character.isLetter( token.charAt(initialIndex) )) {
            initialIndex++;
            
            if (initialIndex == token.length()) {
                return new BigInteger("0");
            }
        }
        
        if (token.substring(initialIndex).length() == 1) {
            filename = "oneLetter"; /* special file for one-letter words */
        } else {
            /* Take the initial to know the directory, and two initials to know the file */
            String initial = token.substring(initialIndex, initialIndex+1);
            String initials = token.substring(initialIndex, initialIndex+2);
            filename = initial + "/" + initials;
        }
        BigInteger frequencyCount = null;
        
        try {
            String filePath = Constants.getGoogleCorpusFolder() + filename;
            BufferedReader br = new BufferedReader( new FileReader(filePath) );
            String line;
            
            /* Here we need linear search, since the files are not sorted lexicographically */
            while ((line = br.readLine()) != null) {
                /* Split into token and tag */
                String[] fields = line.split("\t");
                String[] w = fields[0].split("_");
                
                if (w.length == 2 && w[0].equals(token) && w[1].equals(translatedTag)) {
                    frequencyCount = new BigInteger( fields[1] );
                    break;
                }
            }
            
            br.close();
        } catch (IOException e) {
            /*
             * NOTE: it's expected that we get here for "file not found" errors. This is because my
             * corpus was reduced in order to contain only words present in an online English dictionary.
             * So initials with non-English letters or numbers in them don't correspond to any file.
             */
            return new BigInteger("0");
        }
        
        /* Nothing was found */
        if (frequencyCount == null) {
            frequencyCount = new BigInteger("0");
        }
        
        cache.put(word, frequencyCount);
        return frequencyCount;
    }
    
    public @Override BigInteger getTotalCount()
    {
        /* Taken from the corpus "totals" file */
        return new BigInteger("468491999592");
    }
    
    /* The corpus uses a quite particular tagset */
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
        
        if (tag.equals("R") || tag.equals("X")) {
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
