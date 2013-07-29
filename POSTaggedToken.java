/*
 * Container class for a token with a Part-of-Speech tag assigned to it.
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
        return token + "_" + tag;
    }
}
