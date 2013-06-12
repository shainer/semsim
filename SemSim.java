/**
 *
 * @author shainer
 */

import java.io.*;
import java.util.List;

public class SemSim
{
    public static void main(String[] args) throws IOException
    {
        String inputFile = null;
        String outputFile = null;

        if (args.length > 0) {
            inputFile = args[0];
        }

        if (args.length > 1) {
            outputFile = args[1];
        }
        
        TweetParser tp = new TweetParser(inputFile, outputFile);
        tp.parse();
        List<SentencePair> pairs = tp.getSentencePairs();
        
        SimilarityLearner m = new SimilarityLearner();
        m.learnModel("train");
        
        double[] similarities = new double[pairs.size()];
        int index = 0;
        
        for (SentencePair sp : pairs) {
            similarities[index++] = m.getSimilarity(sp);
        }
        
        tp.writeSimilarities(similarities);
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

