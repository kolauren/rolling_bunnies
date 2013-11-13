package change.impact.graph.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

//misc utils used for parsing
public class Utils {
	public static String getHtml(String url) throws IOException {
		URL someUrl = new URL(url);
        BufferedReader in = new BufferedReader(
        new InputStreamReader(someUrl.openStream()));

        String inputLine;
        StringBuffer sb = new StringBuffer();
        while ((inputLine = in.readLine()) != null)
            sb.append(inputLine);
        in.close();
        
        return sb.toString();
	}
}
