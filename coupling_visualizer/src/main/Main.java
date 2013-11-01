package main;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import commit.parser.Commit;
import commit.parser.GitHubDao;

public class Main {
	public static void main(String[] args) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		//testing commit parser
		/**
		//TODO: move args
		String owner = "kolauren";
		String repo = "rolling_bunnies";
		String user = "pammil";
		String password = "5cd8f20e47dfc2ffc846e82c652450c61f0a41a9";
		
		//basic authentication
		GitHubDao githubDao = new GitHubDao(user, password);
		
		//print compact and pretty json
		File json = new File("output/"+owner+"_"+repo+"_commits.json");
		File jsonPretty = new File("output/"+owner+"_"+repo+"_pretty_commits.json");
		
		//clear old file
		FileUtils.writeStringToFile(json, "", "utf-8", false);
		FileUtils.writeStringToFile(jsonPretty, "", "utf-8", false);
		
		Gson gson = new Gson();
		Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
		
		Collection<Commit> commits = githubDao.getCommits(owner, repo);
		
		FileUtils.writeStringToFile(json, gson.toJson(commits), true);
		FileUtils.writeStringToFile(jsonPretty, gsonPretty.toJson(commits), true);
		**/
	}
}