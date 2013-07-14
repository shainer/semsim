/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import cmu.arktweetnlp.Tagger.TaggedToken;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author shainer
 */
public class LSA
{
    private Map<TaggedToken, ArrayList<Double> > lsa;
    
    public LSA()
    {
        lsa = new HashMap<>();
        
        List<String> lines = new LinkedList<>();
        
        try {
            BufferedReader br = new BufferedReader( new FileReader("lsa_matrix.txt") );
            parseFile(br);
            System.out.println("finished");
            
            br.close();
        } catch (IOException e) {
            System.err.println("Could not read \"lsa_matrix.txt\": " + e.getLocalizedMessage());
            System.exit(-1);
        }
    }
    
    void parseFile(BufferedReader br) throws IOException
    {
        int c;
        String currentToken = "";
        
        TaggedToken tt = new TaggedToken();
        ArrayList<Double> currentVector = new ArrayList<>();

        int countTab = 0;
        int state = 0;
        int line = 1;
        
        while ((c = br.read()) != -1) {
            char ch = (char)c;
            
            switch (state) {
                case 0:
                    if (ch == ':') {
                        tt.token = currentToken;
                        br.read();
                        tt.tag = "";
                        tt.tag += (char)br.read();
                        
                        currentToken = "";
                        state = 1;
                    } else {
                        currentToken += ch;
                    }
                    break;
                    
                case 1:
                    if (ch == '\t') {
                        countTab++;
                        
                        if (countTab == 3) {
                            currentToken = "";
                            state = 2;
                        }
                    }
                    break;
                    
                case 2:
                        if (ch == ',') {
                            currentVector.add( Double.parseDouble(currentToken) );
                            currentToken = "";
                        } else if (ch == '\n') {
                            System.out.println(line++);
                            currentVector.add( Double.parseDouble(currentToken) );
                            lsa.put(tt, currentVector);

                            currentToken = "";
                            tt = new TaggedToken();
                            countTab = 0;
                            currentVector.clear();
                            state = 0;
                        } else {
                            currentToken += ch;
                        }
            }
        }
    }
}
