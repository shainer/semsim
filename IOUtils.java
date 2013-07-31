import java.util.List;
import java.util.LinkedList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

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
}
