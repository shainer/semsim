/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.math.BigInteger;
import java.util.List;

/**
 *
 * @author shainer
 */
public abstract class FrequencyCounter
{    
    public FrequencyCounter()
    {}
    
    public abstract void createCache(List<SentencePair> pairs);
    public abstract void destroyCache();
    public abstract BigInteger getFrequencyCount(String token, String tag);
    public abstract BigInteger getTotalCount();
}
