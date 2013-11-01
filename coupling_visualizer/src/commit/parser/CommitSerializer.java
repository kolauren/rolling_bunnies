package commit.parser;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CommitSerializer {
	public static String toJson(List<Commit> commits, boolean pretty) {
		Gson gson;
		if(pretty)
			gson = new GsonBuilder().setPrettyPrinting().create();
		else
			gson = new Gson();		
		return gson.toJson(commits);
	}
}
