/*
 * Interface for classes that return frequency counts.
 * 
 * Copyright (C) 2013 Lisa Vitolo <lisavitolo90@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Creative Commons
 * Attribution-NonCommercial-ShareAlike 3.0 license.
 * You should have received a copy of the license with this product.
 * Otherwise, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
 */
import java.math.BigInteger;

public abstract interface FrequencyCounter
{
    public abstract BigInteger getFrequencyCount(String token, String tag);
    public abstract BigInteger getTotalCount();
}
