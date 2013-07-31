
import cmu.arktweetnlp.Tagger;

/*
 * Container class for a token with a Part-of-Speech tag assigned to it.
 */
public class POSTaggedToken
{
    public String token;
    public String tag;
    public String lemma;
    
    public POSTaggedToken(String token, String tag, String lemma)
    {
        this(token, tag);
        this.lemma = lemma;
    }
    
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
            
            /* Case-insensitive comparison */
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
        return token + "_" + tag + "_" + lemma;
    }
}
