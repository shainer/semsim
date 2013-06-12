
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

/**
 *
 * @author shainer
 */
public class TweetParser
{
    private String inputFile;
    private String outputFile;
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
        List<TaggedToken> tweetQuestionTokens = new LinkedList<TaggedToken>();
        this.sentencePairs = new LinkedList<SentencePair>();

        try {
            Object obj = json.parse( new FileReader(inputFile) );
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
                sentencePairs.add( new SentencePair(tweetQuestionTokens, preprocess(question)) );
            }
            
        } catch (IOException e) {
            System.err.println(":: IO Error: " + e);
        } catch (ParseException e) {
            System.err.println(":: Parse error: " + e);
        }
    }
    
    public List<SentencePair> getSentencePairs()
    {
        return sentencePairs;
    }
    
    public void writeSimilarities(double[] similarities)
    {
        JSONParser json = new JSONParser();
        
        try {
            Object obj = json.parse( new FileReader(inputFile) );
            JSONObject jsonObject = (JSONObject)obj;
            JSONArray questions = (JSONArray)jsonObject.get("qpairs");
            String tweet = (String)jsonObject.get("text");
            int index = 0;
            
            for (Object p : questions) {
                JSONObject qaPair = (JSONObject)p;
                qaPair.put("similarity", similarities[index++]);
            }
            
            FileWriter writer = new FileWriter(outputFile);
            writer.write(jsonObject.toJSONString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.err.println(":: IO Error: " + e);
        } catch (ParseException e) {
            System.err.println(":: Parse error: " + e);
        }

    }
    
    private List<TaggedToken> preprocess(String sentence)
    {
        return tagger.tokenizeAndTag(sentence);
    }
}
