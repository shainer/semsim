/*
 * Useful I/O operations.
 * 
 * Copyright (C) 2013 Lisa Vitolo <lisavitolo90@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Creative Commons
 * Attribution-NonCommercial-ShareAlike 3.0 license.
 * You should have received a copy of the license with this product.
 * Otherwise, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

import java.util.List;
import java.util.LinkedList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class IOUtils
{
    /* Reads a text file as a list of rows. */
    public static List<String> readlines(String input)
    {
        List<String> lines = new LinkedList<>();
        
        try {
            BufferedReader br = new BufferedReader( new FileReader(input) );
            String line;
            
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            
            br.close();
        } catch (IOException e) {
            System.err.println("Could not read \"" + input + "\": " + e.getLocalizedMessage());
            System.exit(-1);
        }
        
        return lines;
    }
}
