/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author shainer
 */
public class LSA
{
    private Map<POSTaggedToken, double[] > lsa;
    
    public LSA()
    {
        lsa = new HashMap<>();
        
        try {
            BufferedReader br = new BufferedReader( new FileReader("lsa_matrix.txt") );
            parseFile(br);
            
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
        
        POSTaggedToken tt = new POSTaggedToken();
        double[] currentVector = new double[ Properties.getLSAVectorSize() ];
        int currentVectorIndex = 0;

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
                        
                        char next = (char)br.read();
                        
                        if (next == '\t') {
                            countTab++;
                        } else {
                            tt.tag += translateTag(next);
                        }
                        
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
                            currentVector[currentVectorIndex++] = Double.parseDouble(currentToken);
                            currentToken = "";
                        } else if (ch == '\n') {
                            currentVector[currentVectorIndex++] = Double.parseDouble(currentToken);
                            lsa.put(tt, currentVector);

                            currentToken = "";
                            tt = new POSTaggedToken();
                            countTab = 0;
                            
                            currentVectorIndex = 0;
                            currentVector = new double[ Properties.getLSAVectorSize() ];
                            
                            state = 0;
                        } else {
                            currentToken += ch;
                        }
            }
        }
    }
    
    public double[] getWordVector(POSTaggedToken tt)
    {
        double[] vector;
        tt.token = tt.token.toLowerCase();
        
        if (!lsa.containsKey(tt)) {
            vector = new double[ Properties.getLSAVectorSize() ];
        } else {
            vector = lsa.get(tt);
        }
        
        return vector;
    }
    
    private char translateTag(char tag)
    {        
        switch (tag)
        {
            case 'p':
                tag = 'O';
                break;
                
            case 'c':
                tag = '&';
                break;
                
            case 'm':
                tag = 'V';
                break;
                
            case 'w':
                tag = 'R';
                break;
                
            case 'i':
                tag = 'P';
                break;
                
            case 'j':
                tag = 'A';
                break;
                
            case '#':
                tag = ',';
                break;
                
            default:
                tag = Character.toUpperCase(tag);
                break;
        }
        
        return tag;
    }
}
