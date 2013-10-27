package githubapiwhatdo;

import java.util.List;
import org.eclipse.egit.github.core.RepositoryCommit;
import com.google.gson.Gson;

public class CommitParser {

	public CommitParser() {
	}

	public static List<String> getJavaFileNames(RepositoryCommit c) {
		return null;
	}
	
	// parses a commit to JSON
	public String commitToJSON(Commit c) {
		Gson gson = new Gson();
		return gson.toJson(c);
	}

}
