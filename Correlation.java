/*
 * Pearson correlation from two sets of similarity scores. 
 * 
 * Copyright (C) 2013 Lisa Vitolo <lisavitolo90@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Creative Commons
 * Attribution-NonCommercial-ShareAlike 3.0 license.
 * You should have received a copy of the license with this product.
 * Otherwise, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

import java.math.BigDecimal;

/* 
 * NOTE: boxing and unboxing don't work automatically for arrays as they do for primitive types. Since
 * the arrays usually have thousands of elements, I avoided expensive copies from one type to the other
 * by redefining the same method twice.
 */
public class Correlation
{
    public static double getPearsonCorrelation(Double[] scores1, Double[] scores2)
    {
        double corr;
        double sum_sq_x = 0;
        double sum_sq_y = 0;
        double sum_coproduct = 0;
        
        double mean_x = scores1[0];
        double mean_y = scores2[0];
        
        for(int i = 2; i < scores1.length + 1; i++) {
            double sweep = Double.valueOf(i-1) / i;
            double delta_x = scores1[i-1] - mean_x;
            double delta_y = scores2[i-1] - mean_y;
            sum_sq_x += delta_x * delta_x * sweep;
            sum_sq_y += delta_y * delta_y * sweep;
            sum_coproduct += delta_x * delta_y * sweep;
            mean_x += delta_x / i;
            mean_y += delta_y / i;
        }
        
        double pop_sd_x = (double) Math.sqrt(sum_sq_x/scores1.length);
        double pop_sd_y = (double) Math.sqrt(sum_sq_y/scores1.length);
        double cov_x_y = sum_coproduct / scores1.length;
        
        corr = cov_x_y / (pop_sd_x*pop_sd_y);
        
        /* Round to 3 decimal digits */
        BigDecimal bd = new BigDecimal(corr);
        bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }
    
    public static double getPearsonCorrelation(double[] scores1, double[] scores2)
    {
        double corr;
        double sum_sq_x = 0;
        double sum_sq_y = 0;
        double sum_coproduct = 0;
        
        double mean_x = scores1[0];
        double mean_y = scores2[0];
        
        for(int i = 2; i < scores1.length + 1; i++) {
            double sweep = Double.valueOf(i-1) / i;
            double delta_x = scores1[i-1] - mean_x;
            double delta_y = scores2[i-1] - mean_y;
            sum_sq_x += delta_x * delta_x * sweep;
            sum_sq_y += delta_y * delta_y * sweep;
            sum_coproduct += delta_x * delta_y * sweep;
            mean_x += delta_x / i;
            mean_y += delta_y / i;
        }
        
        double pop_sd_x = (double) Math.sqrt(sum_sq_x/scores1.length);
        double pop_sd_y = (double) Math.sqrt(sum_sq_y/scores1.length);
        double cov_x_y = sum_coproduct / scores1.length;
        
        corr = cov_x_y / (pop_sd_x*pop_sd_y);
        
        /* Round to 3 decimal digits */
        BigDecimal bd = new BigDecimal(corr);
        bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }
}
 
