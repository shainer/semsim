
import cmu.arktweetnlp.Tagger;
import java.io.FileReader;
import java.io.IOException;
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
    private Tagger tagger;
        
    public TweetParser(String inputFile, String outputFile)
    {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        
        this.tagger = new Tagger();
        
        try {
            this.tagger.loadModel("src/taggerModel");
        } catch (IOException e) {
            System.err.println("Error loading model for POS tagging: " + e.getLocalizedMessage());
        }
    }
    
    public void parse()
    {
        JSONParser json = new JSONParser();
        this.sentencePairs = new LinkedList<>();

        try {
            Object obj;
            
            if (inputFile == null) {
                obj = json.parse( new StringReader( IOUtils.readJSONFromStdin() ));
            } else {
                obj = json.parse( new FileReader(inputFile) );
            }
            
            JSONObject jsonObject = (JSONObject)obj;
            List<String> tweetPOSTags = new LinkedList<>();
            List<String> tweetNormalizedTokens = new LinkedList<>();
            JSONArray tweetTokens = (JSONArray)jsonObject.get("tokens");
            
            for (Object t : tweetTokens) {
                JSONObject token = (JSONObject)t;
                tweetNormalizedTokens.add( (String)token.get("normalized") );
                tweetPOSTags.add( (String)token.get("pos") );
            }
            
            JSONArray questions = (JSONArray)jsonObject.get("qpairs");            
            for (Object p : questions) {
                JSONObject qaPair = (JSONObject)p;
                String question = (String)qaPair.get("question");
                sentencePairs.add( new SentencePair(tweetNormalizedTokens, tweetPOSTags, question, tagger) );
            }
            
            this.inputJSON = obj;
        } catch (IOException e) {
            System.err.println(":: IO Error: " + e);
        } catch (ParseException e) {
            System.err.println(":: Parse error: " + e);
        }
    }
    
    public void writeSimilarities(double[] similarities)
    {        
        JSONParser json = new JSONParser();
        
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
            
            writer.println(jsonObject.toJSONString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.err.println(":: IO Error: " + e);
        }

    }
    
    public List<SentencePair> getSentencePairs()
    {
        return sentencePairs;
    }
}
