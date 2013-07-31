import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.PrintStream;
import java.io.StringReader;

/**
 *
 * @author shainer
 */
public class TweetParser
{
    private String inputFile;
    private String outputFile;
    private Object inputJSON;
    private List<SentencePair> sentencePairs;
    private StanfordCoreNLP nlp;
        
    public TweetParser(String inputFile, String outputFile, StanfordCoreNLP nlp)
    {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.nlp = nlp;
    }
    
    public void parse()
    {
        JSONParser json = new JSONParser();
        this.sentencePairs = new LinkedList<>();

        try {
            Object obj;
            
            if (inputFile == null) {
                obj = json.parse( new StringReader( readJSONFromStdin() ));
            } else {
                obj = json.parse( new FileReader(inputFile) );
            }
            
            JSONObject jsonObject = (JSONObject)obj;
            
            List<String> tweetPOSTags = new LinkedList<>();
            List<String> tweetNormalizedTokens = new LinkedList<>();
            List<String> tweetLemmas = new LinkedList<>();
            JSONArray tweetTokens = (JSONArray)jsonObject.get("tokens");
            
            for (Object t : tweetTokens) {
                JSONObject token = (JSONObject)t;
                tweetNormalizedTokens.add( (String)token.get("normalized") );
                tweetPOSTags.add( (String)token.get("pos") );
                tweetLemmas.add( (String)token.get("lemma") );
            }
            
            JSONArray questions = (JSONArray)jsonObject.get("qpairs");            
            for (Object p : questions) {
                JSONObject qaPair = (JSONObject)p;
                String question = (String)qaPair.get("question");
                sentencePairs.add( new SentencePair(tweetNormalizedTokens, tweetPOSTags, tweetLemmas, question, nlp) );
            }
            
            this.inputJSON = obj;
        } catch (IOException e) {
            System.err.println(":: IO Error: " + e);
            System.exit(-1);
        } catch (ParseException e) {
            System.err.println(":: Parse error: " + e);
            System.exit(-1);
        }
    }
    
    public void writeSimilarities(double[] similarities)
    {        
        try {
            Object obj = this.inputJSON;
            JSONObject jsonObject = (JSONObject)obj;
            JSONArray questions = (JSONArray)jsonObject.get("qpairs");
            String tweet = (String)jsonObject.get("text");
            int index = 0;
            
            for (Object p : questions) {
                JSONObject qaPair = (JSONObject)p;
                qaPair.put("similarity", similarities[index++]);
            }
            
            PrintStream writer;
            
            if (outputFile != null) {
                writer = new PrintStream(outputFile);
            } else {
                writer = System.out;
            }
            
            writer.println( goodJSONFormat(jsonObject.toJSONString()) );
            writer.close();
        } catch (IOException e) {
            System.err.println(":: Error reading from JSON file: " + e.getLocalizedMessage());
        }

    }
    
    public List<SentencePair> getSentencePairs()
    {
        return sentencePairs;
    }
    
    private String goodJSONFormat(String jsonOutput)
    {
        char[] str = jsonOutput.toCharArray();
        String formatted = "";
        
        for (int i = 0; i < str.length; i++) {
            char ch = str[i];
            
            formatted += ch;
            
            if (ch == ',') {
                formatted += "\n";
            }
        }
        
        return formatted;
    }
    
    private static String readJSONFromStdin()
    {
        String content = "";
        String tmp;
        int parenCounter = 0;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                tmp = reader.readLine();
                content += tmp + "\n";

                if (tmp.contains("{")) {
                    parenCounter++;
                }
                if (tmp.contains("}")) {
                    parenCounter--;
                    
                    if (parenCounter == 0) {
                        content += "\n";
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            System.err.println(":: Error reading from stdin: " + ex.getMessage());
            System.exit(-1);
        }

        return content;
    }
}
