package main;

import japa.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import commit.coupling.analyzer.CommitCouplingAnalyzer;
import commit.retriever.Commit;
import commit.retriever.CommitRetriever;
import commit.retriever.Diff;
import commit.retriever.UnifiedDiffParser;

public class Main {
	public static void main(String[] args) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		/*
		//testing commit parser
		//TODO: move args
		String owner = "kolauren";
		String repo = "rolling_bunnies";
		//master
		String branch = "master";
		
		CommitRetriever p = new CommitRetriever(owner, repo, branch);
		
		//print compact and pretty json
		File json = new File("output/"+owner+"_"+repo+"_commits.json");
		File jsonPretty = new File("output/"+owner+"_"+repo+"_pretty_commits.json");
		
		//clear old file
		FileUtils.writeStringToFile(json, "", "utf-8", false);
		FileUtils.writeStringToFile(jsonPretty, "", "utf-8", false);
		
		Gson gson = new Gson();
		Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
		
		Collection<Commit> commits = p.getCommits();
		
		FileUtils.writeStringToFile(json, gson.toJson(commits), true);
		FileUtils.writeStringToFile(jsonPretty, gsonPretty.toJson(commits), true); 
		*/
		
		/**
		 * Testing diff parser
		 */
		
		String unifiedDiff="@@ -9,21 +9,19 @@\n \n import com.google.gson.Gson;\n import com.google.gson.GsonBuilder;\n+\n import commit.parser.Commit;\n+import commit.parser.CommitParser;\n import commit.parser.GitHubDao;\n \n public class Main {\n \tpublic static void main(String[] args) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {\n \t\t//testing commit parser\n-\t\t/**\n \t\t//TODO: move args\n \t\tString owner = \"kolauren\";\n \t\tString repo = \"rolling_bunnies\";\n-\t\tString user = \"pammil\";\n-\t\tString password = \"5cd8f20e47dfc2ffc846e82c652450c61f0a41a9\";\n \t\t\n-\t\t//basic authentication\n-\t\tGitHubDao githubDao = new GitHubDao(user, password);\n+\t\tCommitParser p = new CommitParser();\n \t\t\n \t\t//print compact and pretty json\n \t\tFile json = new File(\"output/\"+owner+\"_\"+repo+\"_commits.json\");\n@@ -36,10 +34,9 @@ public static void main(String[] args) throws IOException, IllegalAccessExceptio\n \t\tGson gson = new Gson();\n \t\tGson gsonPretty = new GsonBuilder().setPrettyPrinting().create();\n \t\t\n-\t\tCollection<Commit> commits = githubDao.getCommits(owner, repo);\n+\t\tCollection<Commit> commits = p.getCommits(owner, repo);\n \t\t\n \t\tFileUtils.writeStringToFile(json, gson.toJson(commits), true);\n \t\tFileUtils.writeStringToFile(jsonPretty, gsonPretty.toJson(commits), true);\n-\t\t**/\n \t}\n }\n\\ No newline at end of file";
	
		Diff difff = UnifiedDiffParser.parse(unifiedDiff);
		System.out.println("Added+++++++++++++");
		for(int number : difff.getAddedLines().keySet()) {
			System.out.println(number+" : "+difff.getAddedLine(number));
		}
		System.out.println("Removed-------------");
		for(int number :difff.getRemovedLines().keySet()) {
			System.out.println(number+" : "+difff.getRemovedLine(number));
		}
		
		try {
			CommitCouplingAnalyzer.getTypeOneCoupling(null);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
