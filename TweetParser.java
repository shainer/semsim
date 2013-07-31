/*
 * Handles I/O with tweets expressed as JSON files.
 * 
 * Copyright (C) 2013 Lisa Vitolo <lisavitolo90@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Creative Commons
 * Attribution-NonCommercial-ShareAlike 3.0 license.
 * You should have received a copy of the license with this product.
 * Otherwise, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

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
    
    /*
     * Creates sentence pairs from the input tweet. Each sentence pair contains the main question
     * and one of the questions in the "qpairs" array.
     */
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
            
            /*
             * For the user question, gets all the needed information at this stage,
             * without having to use external tools. Normalized tokens are used to reduce the
             * negative effects of misspelled words.
             */
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
        } catch (ParseException e) { /* malformed JSON file */
            System.err.println(":: Parse error: " + e);
            System.exit(-1);
        }
    }
    
    /*
     * Writes similarity scores in the output JSON file. We add a new field "similarity" in each
     * "qpairs" object with the score.
     */
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
            
            /*
             * By default, the JSON string is compressed in one line, so I add some newlines for
             * readability. The resulting string is still a valid JSON object.
             */
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
    
    /*
     * When no input file is supplied, reads a JSON string from stdin. The JSON object
     * terminates when all the open parentheses "{" have been closed and an empty line is
     * inserted.
     */
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
