package core;

import java.util.List;

import org.eclipse.egit.github.core.RepositoryCommit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CommitSerializer {

	public CommitSerializer() {
	}

	public static List<String> getJavaFileNames(RepositoryCommit c) {
		return null;
	}
	
	// parses a commit to JSON
	public static String commitToJSON(Commit c, boolean prettypls) {
		Gson gson;
		if(prettypls)
			gson = new GsonBuilder().setPrettyPrinting().create();
		else
			gson = new Gson();
		return gson.toJson(c);
	}

}
