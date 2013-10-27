package githubapiwhatdo;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class Main {
	public static void main(String[] args) throws IOException {
		//TODO: move args
		String owner = "iambchan";
		String repo = "Team-02";
		GitHubDao githubDao = new GitHubDao("pammil", "5cd8f20e47dfc2ffc846e82c652450c61f0a41a9");
		File json = new File("output/"+owner+"_"+repo+"_commits.json");
		for(Commit commit : githubDao.getCommits(owner, repo, 3)) {
			String data = CommitParser.commitToJSON(commit);
			FileUtils.writeStringToFile(json, data, true);
		}
	}
}