/**
 *
 * @author shainer
 */

import java.io.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Properties;

public class SemSim
{
    public static void main(String[] args)
    {
        String inputFile = null;
        String outputFile = null;
        int paramIndex;
        
        if ((paramIndex = findParameter(args, "--parameters")) != -1) {
            trainSystem(args, paramIndex);
        } else {
            if (args.length > 0) {
                inputFile = args[0];
            }

            if (args.length > 1) {
                outputFile = args[1];
            }

            TweetParser tp = new TweetParser(inputFile, outputFile);
            tp.parse();
            List<SentencePair> pairs = tp.getSentencePairs();

            SimilarityTester m = new SimilarityTester();
            m.loadModel("similarity.model"); // EXAMPLE!

            double[] similarities = m.getSimilarities(pairs);
            tp.writeSimilarities(similarities);
        }
    }
    
    public static int findParameter(String[] args, String p)
    {
        for (int i = 0; i < args.length; i++) {
            if (p.equals(args[i])) {
                return i;
            }
        }
        
        return -1;
    }
    
    public static void trainSystem(String[] args, int paramIndex)
    {
        Properties prop = new Properties();
        String propertyFile;
        
        if (paramIndex == args.length - 1) {
            System.err.println(":: Missing properties file");
            System.exit(-1);
        }
                
        propertyFile = args[paramIndex+1];
      
        try {
            prop.load( new FileInputStream(propertyFile) );
        } catch (IOException fnf) {
            System.err.println(":: Error accessing properties file " + fnf.getMessage());
            return;
        }

        boolean samplesPresent = (prop.getProperty("trainfiles") != null);
        boolean outputFeatures = (prop.getProperty("featureoutput") != null);
        boolean inputFeatures = (prop.getProperty("featureinput") != null);

        if (badPropertiesFile(samplesPresent, outputFeatures, inputFeatures)) {
            System.err.println(":: The property file has an invalid format. Please refer to the README for instructions.");
            System.exit(-1);
        }

        SimilarityLearner sl = new SimilarityLearner();
        List<TrainingSample> samples = new LinkedList<>();

        if (samplesPresent) {
            String[] trainingFiles = prop.getProperty("trainfiles").split(" ");
            samples = sl.extractFeatures(trainingFiles);
        }

        if (inputFeatures) {
            String featureInput = prop.getProperty("featureinput");
            samples = sl.readFeatures(featureInput);
        }

        if (outputFeatures) {
            String featureOutput = prop.getProperty("featureoutput");
            sl.writeFeatures(samples, featureOutput);
        } else {
            sl.learnModel(samples);
        }
    }
    
    private static boolean badPropertiesFile(boolean samples, boolean outputF, boolean inputF)
    {
        return (!samples && !inputF && !outputF) || (samples && inputF) || (inputF && outputF) || (outputF && !samples && !inputF);
    }

    public static String readStdin()
    {
        String content = "";
        String tmp;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                tmp = reader.readLine();

                if (tmp.isEmpty()) {
                    break;
                }

                content += tmp + "\n";
            }
        } catch (IOException ex) {
            System.err.println(":: Error reading from stdin: " + ex.getMessage());
            System.exit(-1);
        }

        return content;
    }
    
} 

