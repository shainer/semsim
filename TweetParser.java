
import cmu.arktweetnlp.Tagger;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import cmu.arktweetnlp.Tagger.TaggedToken;
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
        List<TaggedToken> tweetQuestionTokens = new LinkedList<>();
        this.sentencePairs = new LinkedList<>();

        try {
            Object obj;
            
            if (inputFile == null) {
                obj = json.parse( new StringReader( IOUtils.readJSONFromStdin() ));
            } else {
                obj = json.parse( new FileReader(inputFile) );
            }
            
            JSONObject jsonObject = (JSONObject)obj;
            
            JSONArray tweetNormalizedTokens = (JSONArray)jsonObject.get("normalized_tokens");
            JSONArray tweetPOSTags = (JSONArray)jsonObject.get("pos");
            for (int i = 0; i < tweetNormalizedTokens.size(); i++) {
                TaggedToken tt = new TaggedToken();
                tt.tag = (String)tweetPOSTags.get(i);
                tt.token = (String)tweetNormalizedTokens.get(i);
                
                tweetQuestionTokens.add(tt);
            }

            JSONArray questions = (JSONArray)jsonObject.get("qpairs");
            for (Object p : questions) {
                JSONObject qaPair = (JSONObject)p;
                String question = (String)qaPair.get("question");
                sentencePairs.add( new SentencePair(tweetQuestionTokens, question, tagger) );
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
