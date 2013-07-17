
import cmu.arktweetnlp.Tagger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author shainer
 */
public class POSTaggedToken
{
    public String token;
    public String tag;
    
    public POSTaggedToken(String token, String tag)
    {
        this.token = token;
        this.tag = tag;
    }
    
    public POSTaggedToken(Tagger.TaggedToken tt)
    {
        this.token = tt.token;
        this.tag = tt.tag;
    }
    
    public POSTaggedToken()
    {}
    
    @Override public boolean equals(Object o)
    {
        if (o instanceof POSTaggedToken) {
            POSTaggedToken other = (POSTaggedToken)o;
            
            return (token.toLowerCase().equals(other.token.toLowerCase()) && tag.equals(other.tag));
        }
        
        return false;
    }
    
    @Override public int hashCode()
    {
        return (token.hashCode() + tag.hashCode());
    }
    
    @Override public String toString()
    {
        return token + "_" + tag;
    }
}
