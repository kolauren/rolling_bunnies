package githubapiwhatdo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.RepositoryCommit;

import com.google.gson.*;

public class ParseCommitUtils {
	public static List<String> getJavaFileNames(RepositoryCommit commit) throws IOException {
		List<String> filenames = new ArrayList<String>();
		String url = commit.getUrl();
		
		JsonObject jsonObj = getJsonFromUrl(url);
		JsonArray fileArray = jsonObj.get("files").getAsJsonArray();
		for(int i=0; i<fileArray.size(); i++) {
			JsonObject file = fileArray.get(i).getAsJsonObject();
			String filename = file.get("filename").getAsString();
			if(filename.matches(".*\\.java"))
				filenames.add(filename);
		}

		return filenames;
	}
	
	//USE BASIC AUTHENTICATION OR GET RATE LIMITED
	
	public static JsonObject getJsonFromUrl(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		InputStream is = (InputStream) url.getContent();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = null;
		StringBuffer sb = new StringBuffer();
		while((line = br.readLine()) != null){
			sb.append(line);
		}
		String htmlContent = sb.toString();
		
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObj = jsonParser.parse(htmlContent).getAsJsonObject();
		
		return jsonObj;
	}
}