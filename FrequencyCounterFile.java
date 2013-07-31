/*
 * Frequency counts from an external text file.
 * 
 * Copyright (C) 2013 Lisa Vitolo <lisavitolo90@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Creative Commons
 * Attribution-NonCommercial-ShareAlike 3.0 license.
 * You should have received a copy of the license with this product.
 * Otherwise, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
 *
 */
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.math.BigInteger;

/*
 * The Takelab authors have grouped together the frequency counts for all the words found in the
 * training files in one text file. Here we read from that file in order to speed up things.
 * 
 * The frequency file doesn't use any POS tag.
 */
public class FrequencyCounterFile implements FrequencyCounter
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
                totalCount = new BigInteger(fields[0]); /* the first row is the total count */
            }
        }
    }
    
    public @Override BigInteger getFrequencyCount(String token, String tag)
    {
        BigInteger c = frequencyCounts.get(token);
        
        /* This should not happen unless you use customized training files. */
        if (c == null) {
            return new BigInteger("0");
        }
        
        return c;
    }
    
    public @Override BigInteger getTotalCount()
    {
        return totalCount;
    }
}
