/*
 * Training sample: array of features and target score
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
