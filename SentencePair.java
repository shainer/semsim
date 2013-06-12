/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.List;
import cmu.arktweetnlp.Tagger.TaggedToken;

/**
 *
 * @author shainer
 */
public class SentencePair
{
    public List<TaggedToken> s1;
    public List<TaggedToken> s2;

    public SentencePair(List<TaggedToken> s1, List<TaggedToken> s2)
    {
        this.s1 = s1;
        this.s2 = s2;
    }
} 

