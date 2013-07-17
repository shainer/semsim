/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;

import cmu.arktweetnlp.Tagger;

/**
 *
 * @author shainer
 */
public class SentencePair
{
    public List<POSTaggedToken> s1;
    public List<POSTaggedToken> s2;
    
    public SentencePair(List<String> s1Tokens, List<String> s1Tags, String s2, Tagger tagger)
    {
        this.s1 = new LinkedList<>();
        this.s2 = new LinkedList<>();
        
        for (int i = 0; i < s1Tokens.size(); i++) {
            POSTaggedToken tt = new POSTaggedToken( (String)s1Tokens.get(i), (String)s1Tokens.get(i) );
            this.s1.add(tt);
        }
       
        for (Tagger.TaggedToken tt : tagger.tokenizeAndTag(s2)) {
            this.s2.add( new POSTaggedToken(tt) );
        }
        
        preprocess();
    }
    
    public SentencePair(String s1, String s2, Tagger tagger)
    {
        this.s1 = new LinkedList<>();
        this.s2 = new LinkedList<>();

        for (Tagger.TaggedToken tt : tagger.tokenizeAndTag(s1)) {
            this.s1.add( new POSTaggedToken(tt) );
        }
        
        for (Tagger.TaggedToken tt : tagger.tokenizeAndTag(s2)) {
            this.s2.add( new POSTaggedToken(tt) );
        }
        
        preprocess();
    }
    
    @Override public String toString()
    {
        String p = "";
        
        for (POSTaggedToken tt : s1) {
            p += tt + ", ";
        }
        
        p += "\n";
        
        for (POSTaggedToken tt : s2) {
            p += tt + ", ";
        }
        
        p += "\n";
        
        return p;
    }
    
    private void preprocess()
    {
        //System.out.println(this);
        preprocessSentence(this.s1);
        preprocessSentence(this.s2);
        
        mergeCompounds(this.s1, this.s2);
        mergeCompounds(this.s2, this.s1);
        System.out.println("OK.");
        //System.out.println(this);
    }
    
    private void preprocessSentence(List<POSTaggedToken> sentence)
    {
        for (ListIterator<POSTaggedToken> it = sentence.listIterator(); it.hasNext();) {
            POSTaggedToken tt = it.next();
            
            tt = stripAngularBrackets(tt);
            tt = removeHyphensSlashes(tt);
            tt = removeStopWords(tt);
            
            if (tt.token.isEmpty()) {
                it.remove();
            } else {
                it.set(tt);
            }
        }
        
        expandVerbAbbreviations(sentence);
    }
    
    private POSTaggedToken stripAngularBrackets(POSTaggedToken tt)
    {
        String token = tt.token;

        while (token.startsWith("<") || token.startsWith(">")) {
            token = token.substring(1);
        }

        while (token.endsWith("<") || token.endsWith(">")) {
            token = token.substring(0, token.length() - 2);
        }

        tt.token = token;
        return tt;
    }
        
    private POSTaggedToken removeHyphensSlashes(POSTaggedToken tt)
    {
        String s = tt.token;

        s = s.replaceAll("-", "");
        s = s.replaceAll("/", "");
        tt.token = s;
        
        return tt;
    }

    private POSTaggedToken removeStopWords(POSTaggedToken tt)
    {
        String[] stopWords = Properties.getStopWords();

        for (int i = 0; i < stopWords.length; i++) {
            if (stopWords[i].equals( tt.token.toLowerCase() )) {
                tt.token = "";
                return tt;
            }
        }
        
        return tt;
    }
    
    private void expandVerbAbbreviations(List<POSTaggedToken> sentence)
    {
        for (ListIterator<POSTaggedToken> it = sentence.listIterator(); it.hasNext();) {
            POSTaggedToken tt = it.next();
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
                
                it.add( new POSTaggedToken("not", "R") );
            } else if (string.endsWith("'m")) {
                int index = string.lastIndexOf("'");
                
                string = string.substring(0, index);
                
                if (string.isEmpty()) {
                    it.remove();
                } else {
                    tt.token = string;
                    it.set(tt);
                }
                
                it.add( new POSTaggedToken("am", "V") );
            }
        }
    }
    
    private void mergeCompounds(List<POSTaggedToken> sentence1, List<POSTaggedToken> sentence2)
    {
        for (ListIterator<POSTaggedToken> it = sentence1.listIterator(); it.hasNext();) {
            POSTaggedToken tt1 = it.next();
            
            if (it.hasNext()) {
                POSTaggedToken tt2 = it.next();
                String compound = tt1.token + tt2.token;
                String tag = containsTokenWithTag(sentence2, compound);
                
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
    
    private String containsTokenWithTag(List<POSTaggedToken> list, String s)
    {
        for (POSTaggedToken tt : list) {
            if (tt.token.equals(s)) {
                return tt.tag;
            }
        }
        
        return "";
    }
}
