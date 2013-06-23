/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.List;
import java.util.LinkedList;
import cmu.arktweetnlp.Tagger.TaggedToken;
import cmu.arktweetnlp.Tagger;

/**
 *
 * @author shainer
 */
public class SentencePair
{
    public List<TaggedToken> s1;
    public List<TaggedToken> s2;

    public SentencePair()
    {}
    
    public SentencePair(List<TaggedToken> s1, List<TaggedToken> s2)
    {
        this.s1 = s1;
        this.s2 = s2;
    }
    
    public SentencePair(List<TaggedToken> s1, String s2, Tagger tagger)
    {
        this.s1 = s1;
        this.s2 = preprocess(s2, tagger);
    }
    
    public SentencePair(String s1, String s2, Tagger tagger)
    {
        this.s1 = preprocess(s1, tagger);
        this.s2 = preprocess(s2, tagger);
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
    
    private List<TaggedToken> preprocess(String s, Tagger t)
    {
        return t.tokenizeAndTag(s);
    }
} 

