/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.util.List;
import java.util.LinkedList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author shainer
 */
public class IOUtils
{
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
    
    public static String readJSONFromStdin()
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
