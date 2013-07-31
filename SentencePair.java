/*
 * Builds and stores an unlabeled sentence pair (input for our system).
 * 
 * Copyright (C) 2013 Lisa Vitolo <lisavitolo90@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Creative Commons
 * Attribution-NonCommercial-ShareAlike 3.0 license.
 * You should have received a copy of the license with this product.
 * Otherwise, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
 */
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.util.Iterator;

public class SentencePair
{
    public List<POSTaggedToken> s1;
    public List<POSTaggedToken> s2;
    
    private StanfordCoreNLP nlp;
    
    /*
     * This builds the sentence from partial information. For the tweet question we already have tokens,
     * POS tags and lemmas, while we use the Stanford CoreNLP parser for the question extracted from
     * Q&A databases.
     */
    public SentencePair(List<String> s1Tokens, List<String> s1Tags, List<String> s1Lemmas, String s2Text, StanfordCoreNLP nlp)
    {
        this.nlp = nlp;
        this.s1 = new LinkedList<>();
        this.s2 = new LinkedList<>();
        
        for (int i = 0; i < s1Tokens.size(); i++) {
            POSTaggedToken tt = new POSTaggedToken( s1Tokens.get(i), s1Tags.get(i), s1Lemmas.get(i) );
            this.s1.add(tt);
        }
       
        createSentence(s2Text, this.s2);
        preprocess();
        lemmatize(this.s2);
        
        preprocess();
    }
    
    public SentencePair(String text1, String text2, StanfordCoreNLP nlp)
    {
        this.s1 = new LinkedList<>();
        this.s2 = new LinkedList<>();
        this.nlp = nlp;
        
        createSentence(text1, this.s1);
        createSentence(text2, this.s2);
        preprocess();
        lemmatize(this.s1);
        lemmatize(this.s2);        
    }
    
    private void createSentence(String text, List<POSTaggedToken> sentence)
    {
        Annotation d = new Annotation(text);
        nlp.annotate(d);
        
        for (CoreMap ss : d.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : ss.get(CoreAnnotations.TokensAnnotation.class)) {
                sentence.add( new POSTaggedToken(token.toString(), translateTag(token.tag())) );
            }
        }
    }
    
    private void lemmatize(List<POSTaggedToken> sentence)
    {
        String text = "";
        
        for (POSTaggedToken tt : sentence) {
            text += tt.token + " ";
        }
        
        Annotation d = new Annotation(text);
        nlp.annotate(d);
        
        for (CoreMap ss : d.get(CoreAnnotations.SentencesAnnotation.class)) {
            Iterator<CoreLabel> itToken = ss.get(CoreAnnotations.TokensAnnotation.class).iterator();
            ListIterator<POSTaggedToken> itSentence = sentence.listIterator();
            
            while (itToken.hasNext() && itSentence.hasNext()) {
                CoreLabel token = itToken.next();
                POSTaggedToken tt = itSentence.next();
                tt.lemma = token.lemma();
                
                itSentence.set(tt);
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
        
        if (tag.equals("$") || tag.equals(",") || tag.equals("(") || tag.equals(")") || tag.equals(".") || tag.equals(":")) {
            return ",";
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
        return p;
    }
    
    private void preprocess()
    {
        preprocessSentence(this.s1);
        preprocessSentence(this.s2);
        
        mergeCompounds(this.s1, this.s2);
        mergeCompounds(this.s2, this.s1);
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
        String[] stopWords = Constants.getStopWords();

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

                if (!string.isEmpty()) {
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
