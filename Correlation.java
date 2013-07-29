import java.math.BigDecimal;

/*
 * Computing Pearson correlation from two sets of similarity scores
 */
public class Correlation
{
    public static double getPearsonCorrelation(Double[] scores1,Double[] scores2)
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
 
