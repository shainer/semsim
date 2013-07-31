/*
 * Token with a Part-of-Speech tag and (optionally) a lemma.
 * 
 * Copyright (C) 2013 Lisa Vitolo <lisavitolo90@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Creative Commons
 * Attribution-NonCommercial-ShareAlike 3.0 license.
 * You should have received a copy of the license with this product.
 * Otherwise, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
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
        this.lemma = "";
    }
    
    public POSTaggedToken()
    {
        this("", "", "");
    }
    
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
