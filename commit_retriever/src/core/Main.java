package core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.io.FileUtils;

public class Main {
	public static void main(String[] args) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		//TODO: move args
		String owner = "kolauren";
		String repo = "rolling_bunnies";
		GitHubDao githubDao = new GitHubDao("pammil", "5cd8f20e47dfc2ffc846e82c652450c61f0a41a9");
		File json = new File("output/"+owner+"_"+repo+"_commits.json");
		
		//clear old file
		FileUtils.writeStringToFile(json, "", "utf-8", false);
		for(Commit commit : githubDao.getCommits(owner, repo, 3)) {
			String data = CommitSerializer.commitToJSON(commit, true);
			FileUtils.writeStringToFile(json, data, true);
		}
	}
}