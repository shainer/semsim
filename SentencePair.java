/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;

import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Tagger.TaggedToken;

import org.json.simple.JSONArray;

/**
 *
 * @author shainer
 */
public class SentencePair
{
    public List<TaggedToken> s1;
    public List<TaggedToken> s2;
    
    public SentencePair(List<String> s1Tokens, List<String> s1Tags, String s2, Tagger tagger)
    {
        this.s1 = new LinkedList<>();
        this.s2 = new LinkedList<>();
        
        for (int i = 0; i < s1Tokens.size(); i++) {
            TaggedToken tt = new TaggedToken();
            tt.token = (String)s1Tokens.get(i);
            tt.tag = (String)s1Tags.get(i);
            this.s1.add(tt);
        }
        
        this.s2 = tagger.tokenizeAndTag(s2);
        preprocess();
    }
    
    public SentencePair(String s1, String s2, Tagger tagger)
    {
        this.s1 = new LinkedList<>();
        this.s2 = new LinkedList<>();
        this.s1 = tagger.tokenizeAndTag(s1);
        this.s2 = tagger.tokenizeAndTag(s2);
        
        preprocess();
    }
    
    public String toString()
    {
        String p = "";
        
        for (TaggedToken tt : s1) {
            p += tt.token + "_" + tt.tag + ", ";
        }
        
        p += "\n";
        
        for (TaggedToken tt : s2) {
            p += tt.token + "_" + tt.tag + ", ";
        }
        
        p += "\n";
        
        return p;
    }
    
    private void preprocess()
    {
        removeHyphensSlashes(this.s1);
        removeHyphensSlashes(this.s2);
        expandVerbAbbreviations(this.s1);
        expandVerbAbbreviations(this.s2);
        removeStopWords(this.s1);
        removeStopWords(this.s2);
        
        mergeCompounds(this.s1, this.s2);
        mergeCompounds(this.s2, this.s1);
        System.out.println(this);
    }
    
    private void removeHyphensSlashes(List<TaggedToken> list)
    {
        for (ListIterator<TaggedToken> it = list.listIterator(); it.hasNext();) {
            TaggedToken tt = it.next();
            String s = tt.token;
            
            s = s.replaceAll("-", "");
            s = s.replaceAll("/", "");
            
            if (s.isEmpty()) {
                it.remove();
            } else {
                tt.token = s;
                it.set(tt);
            }
        }
    }
    
    private void expandVerbAbbreviations(List<TaggedToken> list)
    {
        for (ListIterator<TaggedToken> it = list.listIterator(); it.hasNext();) {
            TaggedToken tt = it.next();
            String string = tt.token;
            
            if (string.endsWith("n't")) {
                int index = string.lastIndexOf("n");
                string = string.substring(0, index);
                
                if (string.isEmpty()) {
                    it.remove();
                } else {
                    tt.token = string;
                    it.set(tt);
                }
                
                TaggedToken newTT = new TaggedToken();
                newTT.token = "not";
                newTT.tag = "R";
                it.add(newTT);
            } else if (string.endsWith("'m")) {
                int index = string.lastIndexOf("'");
                
                string = string.substring(0, index);
                
                if (string.isEmpty()) {
                    it.remove();
                } else {
                    tt.token = string;
                    it.set(tt);
                }
                
                TaggedToken newTT = new TaggedToken();
                newTT.token = "am";
                newTT.tag = "V";
                it.add(newTT);
            }
        }
    }

    private void removeStopWords(List<TaggedToken> list)
    {
        String[] stopWords = Properties.getStopWords();
        
        for (ListIterator<TaggedToken> it = list.listIterator(); it.hasNext();) {
            TaggedToken tt = it.next();

            for (int i = 0; i < stopWords.length; i++) {
                if (stopWords[i].equals( tt.token.toLowerCase() )) {
                    it.remove();
                    break;
                }
            }
        }
    }
    
    private void mergeCompounds(List<TaggedToken> list1, List<TaggedToken> list2)
    {
        for (ListIterator<TaggedToken> it = list1.listIterator(); it.hasNext();) {
            TaggedToken tt1 = it.next();
            
            if (it.hasNext()) {
                TaggedToken tt2 = it.next();
                String compound = tt1.token + tt2.token;
                String tag = containsTokenWithTag(list2, compound);
                
                if (!tag.isEmpty()) {
                    it.remove();
                    tt1.token = compound;
                    tt1.tag = tag;
                    
                    it.previous();
                    it.set(tt1);
                } else {
                    it.previous();
                }
            }
        }
    }
    
    private String containsTokenWithTag(List<TaggedToken> list, String s)
    {
        for (TaggedToken tt : list) {
            if (tt.token.equals(s)) {
                return tt.tag;
            }
        }
        
        return "";
    }
} 

