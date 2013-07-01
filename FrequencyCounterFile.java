/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.math.BigInteger;

/**
 *
 * @author shainer
 */
public class FrequencyCounterFile extends FrequencyCounter
{
    private Map<String, BigInteger> frequencyCounts;
    private BigInteger totalCount;
    
    public FrequencyCounterFile(String file)
    {
        frequencyCounts = new HashMap<>();
        List<String> entries = IOUtils.readlines(file);
        
        for (String entry : entries) {
            String[] fields = entry.split(" ");
            
            if (fields.length > 1) {
                frequencyCounts.put(fields[0], new BigInteger(fields[1]));
            } else {
                totalCount = new BigInteger(fields[0]);
            }
        }
    }
    
    public @Override BigInteger getFrequencyCount(String token, String tag)
    {
        BigInteger c = frequencyCounts.get(token);
        
        if (c == null) {
            return new BigInteger("0");
        }
        
        return c;
    }
    
    public @Override BigInteger getTotalCount()
    {
        return totalCount;
    }
    
    public @Override void createCache(List<SentencePair> sps) {}
    public @Override void destroyCache() {}
}
