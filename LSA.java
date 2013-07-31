import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

/*
 * Reads LSA vectors from our matrix.
 */
public class LSA
{
    private Map<POSTaggedToken, double[] > lsa;
    
    public LSA()
    {
        lsa = new HashMap<>();
        
        try {
            BufferedReader br = new BufferedReader( new FileReader( Constants.getLSAMatrixPath() ) );
            parseFile(br);
            
            br.close();
        } catch (IOException e) {
            System.err.println("Could not read \"" + Constants.getLSAMatrixPath() + "\": " + e.getLocalizedMessage());
            System.exit(-1);
        }
    }
    
    /*
     * Reads the entire vector in memory immediately. Since we parse as we read and BufferedReader takes
     * care of internal buffering, this function takes less than 30 seconds on a common laptop.
     */
    void parseFile(BufferedReader br) throws IOException
    {
        int c;
        String currentToken = "";
        
        POSTaggedToken tt = new POSTaggedToken();
        double[] currentVector = new double[ Constants.getLSAVectorSize() ];
        int currentVectorIndex = 0;

        int countTab = 0;
        int state = 0;

        /* Little DFA to parse the matrix line character by character */
        while ((c = br.read()) != -1) {
            char ch = (char)c;
            
            switch (state) {
                case 0:
                    /* We reached the end of the word */
                    if (ch == ':') {
                        tt.token = currentToken; /* save it in the current tagged token object */
                        br.read(); /* discards the second ":" */
                        tt.tag = "";
                        
                        char next = (char)br.read(); /* next character is the POS tag */
                        
                        /* Handles little discrepancies between lines */
                        if (next == '\t') {
                            countTab++;
                        } else {
                            tt.tag += translateTag(next);
                        }
                        
                        currentToken = "";
                        state = 1; /* Next step! */
                    } else {
                        currentToken += ch;
                    }
                    break;
                    
                case 1:
                    /* Here we simply count 3 tabs before we go on, ignoring everything else */
                    if (ch == '\t') {
                        countTab++;
                        
                        if (countTab == 3) {
                            currentToken = "";
                            state = 2;
                        }
                    }
                    break;
                    
                /* Here we read the 100 elements of the vector, separated by commas */
                case 2:
                        if (ch == ',') {
                            currentVector[currentVectorIndex++] = Double.parseDouble(currentToken);
                            currentToken = "";
                        } else if (ch == '\n') { /* end of line */
                            currentVector[currentVectorIndex++] = Double.parseDouble(currentToken);
                            lsa.put(tt, currentVector);

                            /* Restores state variables to the initial state for next line */
                            currentToken = "";
                            tt = new POSTaggedToken();
                            countTab = 0;
                            
                            currentVectorIndex = 0;
                            currentVector = new double[ Constants.getLSAVectorSize() ];
                            
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
            vector = new double[ Constants.getLSAVectorSize() ]; /* a null vector */
        } else {
            vector = Arrays.copyOf(lsa.get(tt), Constants.getLSAVectorSize()); /* returns a copy */
        }
        
        return vector;
    }
    
    /*
     * Another internal tagset to translate to our own
     */
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
