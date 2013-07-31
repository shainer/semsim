/*
 * Container class for a training sample (feature array + similarity score).
 * 
 * Copyright (C) 2013 Lisa Vitolo <lisavitolo90@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Creative Commons
 * Attribution-NonCommercial-ShareAlike 3.0 license.
 * You should have received a copy of the license with this product.
 * Otherwise, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

public class TrainingSample
{
    public double[] features;
    public double target;
    
    public TrainingSample(double[] features, double target)
    {
        this.features = features;
        this.target = target;
    }
    
    @Override public String toString()
    {
        String s = "[";
        
        for (int i = 0; i < features.length; i++) {
            s += features[i] + ", ";
        }
        
        s += "] -> " + target + "\n";
        return s;
    }
}
