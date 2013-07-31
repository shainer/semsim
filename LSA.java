/*
 * Managing LSA vectors for the Vector Similarity feature. The LSA matrix
 * was courtesy of Emanuele Bastianelli.
 * 
 * Copyright (C) 2013 Lisa Vitolo <lisavitolo90@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Creative Commons
 * Attribution-NonCommercial-ShareAlike 3.0 license.
 * You should have received a copy of the license with this product.
 * Otherwise, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

/*
 * Vectors are stored in an external text file. The matrix is completely stored in RAM in the
 * constructor, since vectors are not sorted by word and it's faster than doing consecutive lookups
 * in an external file.
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
     * To avoid the overhead of multiple calls to split() or other string parsing functions, I perform
     * the parsing while reading the file. The BufferedReader class takes care of internal buffering, and
     * so this function takes less than 30 seconds on a common laptop.
     * 
     * The parsing is performed with a little DFA, so we expect every line to comply to a very
     * restricted format. See lsa_matrix.txt .
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
     * Another internal tagset, translated to our own
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
