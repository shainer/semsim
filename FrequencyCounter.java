/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.math.BigInteger;

/**
 *
 * @author shainer
 */
public class FrequencyCounter
{
    public FrequencyCounter()
    {}
    
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
    
    public BigInteger getFrequencyCount(String token, String tag)
    {
        token = token.toLowerCase();
        String filename = "";
        
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
        String translatedTag = translateTag(tag);
        BigInteger frequencyCount = null;
        
        try {
            String filePath = "/home/shainer/source/semnlp/googlebooks/" + filename;
            BufferedReader br = new BufferedReader( new FileReader(filePath) );
            String line;
            
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\t");
                String[] word = fields[0].split("_");
                word[0] = word[0].toLowerCase();
                
                if (word.length == 2 && word[0].equals(token) && word[1].equals(translatedTag)) {
                    frequencyCount = new BigInteger( fields[1] );
                }
            }
            
            br.close();
        } catch (IOException e) {
            return new BigInteger("0");
        }
        
        if (frequencyCount == null) {
            frequencyCount = new BigInteger("0");
        }
        
        return frequencyCount;
    }
}
