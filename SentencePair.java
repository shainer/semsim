/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 *
 * @author shainer
 */
public class SentencePair
{
    public List<POSTaggedToken> s1;
    public List<POSTaggedToken> s2;
    
    public List<POSTaggedToken> sLemma1;
    public List<POSTaggedToken> sLemma2;
    
    public SentencePair(String s1, String s2, StanfordCoreNLP nlp)
    {
        this.s1 = new LinkedList<>();
        this.s2 = new LinkedList<>();
        this.sLemma1 = new LinkedList<>();
        this.sLemma2 = new LinkedList<>();
        
        Annotation d1 = new Annotation(s1);
        Annotation d2 = new Annotation(s2);
        
        nlp.annotate(d1);
        for (CoreMap sentence : d1.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                POSTaggedToken tt = new POSTaggedToken();
                tt.token = token.toString();
                tt.tag = translateTag(token.tag());
                this.s1.add(tt);
            }
        }
        
        nlp.annotate(d2);
        
        for (CoreMap sentence : d2.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                POSTaggedToken tt = new POSTaggedToken();
                tt.token = token.toString();
                tt.tag = translateTag(token.tag());
                this.s2.add(tt);
            }
        }
        
        preprocess();
        s1 = "";
        s2 = "";
        
        for (POSTaggedToken tt : this.s1) {
            s1 += tt.token + " ";
        }
        for (POSTaggedToken tt : this.s2) {
            s2 += tt.token + " ";
        }
        
        d1 = new Annotation(s1);
        nlp.annotate(d1);
        for (CoreMap sentence : d1.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                this.sLemma1.add( new POSTaggedToken(token.lemma(), token.tag()) );
            }
        }
        
        d2 = new Annotation(s2);
        nlp.annotate(d2);
        
        for (CoreMap sentence : d2.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                this.sLemma2.add( new POSTaggedToken(token.lemma(), token.tag()));
            }
        }
    }
    
    private String translateTag(String tag)
    {
        if (tag.equals("NN") || tag.equals("NNS")) {
            return "N";
        }
        
        if (tag.startsWith("V") || tag.equals("MD")) {
            return "V";
        }
        
        if (tag.equals("NNP") || tag.equals("NNPS")) {
            return "^";
        }
        
        if (tag.equals("RB") || tag.equals("RBR") || tag.equals("RBS") || tag.equals("WRB")) {
            return "R";
        }
        
        if (tag.equals("PDT") || tag.equals("X")) {
            return "X";
        }
        
        if (tag.equals("IN") || tag.equals("TO")) {
            return "P";
        }
        
        if (tag.equals("CC")) {
            return "&";
        }
        
        if (tag.equals("CD")) {
            return "$";
        }
        
        if (tag.startsWith("J")) {
            return "A";
        }
        
        if (tag.equals("PRP") || tag.equals("WP")) {
            return "O";
        }
        
        if (tag.equals("RP")) {
            return "T";
        }
        
        if (tag.equals("UH")) {
            return "!";
        }
        
        if (tag.equals("DT") || tag.equals("PRP$") || tag.equals("WDT") || tag.equals("WP$")) {
            return "D";
        }
        
        return "G";
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
        
        for (POSTaggedToken tt: sLemma1) {
            p += tt.token + " ";
        }
        
        p += "\n";
        
        for (POSTaggedToken tt : sLemma2) {
            p += tt.token + " ";
        }
        
        p += "\n";
        return p;
    }
    
    /* Preprocessing as described in the paper */
    private void preprocess()
    {
        preprocessSentence(this.s1);
        preprocessSentence(this.s2);
        
        /* Since it may involve pairs of tokens at once, it needs to be done separately */
        mergeCompounds(this.s1, this.s2);
        mergeCompounds(this.s2, this.s1);
    }
    
    private void preprocessSentence(List<POSTaggedToken> sentence)
    {
        /* For each token in the sentence, apply all steps to it */
        for (ListIterator<POSTaggedToken> it = sentence.listIterator(); it.hasNext();) {
            POSTaggedToken tt = it.next();
            
            tt = stripAngularBrackets(tt);
            tt = removeHyphensSlashes(tt);
            tt = removeStopWords(tt);
            
            if (tt.token.isEmpty()) {
                it.remove(); /* remove the token from the list */
            } else {
                it.set(tt); /* replace with the modified token at the same position */
            }
        }
        
        /* This may also create new tokens */
        expandVerbAbbreviations(sentence);
    }
    
    /*
     * Remove all initial and final angular brackets from the token. The token may result empty
     * if it consisted only of that character (depending on the tokenization, this could be common).
     */
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

    /*
     * Stopwords are words that are so common in the English language that they are not
     * considered in the feature collection, because they add no information about
     * the meaning of the sentence.
     */
    private POSTaggedToken removeStopWords(POSTaggedToken tt)
    {
        String[] stopWords = Constants.getStopWords();

        for (int i = 0; i < stopWords.length; i++) {
            if (stopWords[i].equals( tt.token.toLowerCase() )) {
                tt.token = "";
                return tt;
            }
        }
        
        return tt;
    }
    
    /*
     * Here we expand "n't" in "not" ("don't" -> "do not") and "'m" in "am"
     */
    private void expandVerbAbbreviations(List<POSTaggedToken> sentence)
    {
        for (ListIterator<POSTaggedToken> it = sentence.listIterator(); it.hasNext();) {
            POSTaggedToken tt = it.next();
            String string = tt.token;
            
            if (string.endsWith("n't")) {
                int index = string.lastIndexOf("n");
                string = string.substring(0, index);

                if (!string.isEmpty()) { /* this should always be true */
                    tt.token = string;
                    it.set(tt);
                }
                
                it.add( new POSTaggedToken("not", "R") );
                
            } else if (string.endsWith("'m")) {
                int index = string.lastIndexOf("'");
                
                string = string.substring(0, index);
                
                if (!string.isEmpty()) {
                    tt.token = string;
                    it.set(tt);
                }
                
                it.add( new POSTaggedToken("am", "V") );
            }            
        }
    }
    
    /*
     * If a compound word appears as two consecutive tokens in one sentence, but as one token in the other,
     * we merge it in a single token in the first sentence too.
     */
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
