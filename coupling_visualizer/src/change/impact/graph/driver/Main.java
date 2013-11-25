package change.impact.graph.driver;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import change.impact.graph.ChangeImpactGraphGenerator;
import change.impact.graph.CommitGraph;
import change.impact.graph.commit.Commit;
import change.impact.graph.commit.CommitRetriever;
import change.impact.graph.json.JsonBuilder;
import change.impact.graph.json.JsonCommitGraph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Main {
	public static void main(String[] args) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		//load properties from file
		Properties properties = new Properties();
		properties.load(new FileInputStream("input/github_config.properties"));

		//fill in this info in the properties file
		//github basic authentification:
		String user = properties.getProperty("user");
		String password = properties.getProperty("password");
		//target repo:
		String owner = properties.getProperty("owner"); 
		String repo = properties.getProperty("repo");
		String branch = properties.getProperty("branch");
		//number of commits to combine in a timeframe and maximum number of commits to analyze
		int combine = Integer.valueOf(properties.getProperty("combine"));
		int max = Integer.valueOf(properties.getProperty("max"));
		
		//run the analysis
		//it will save json to the output folder
		
		long time = System.currentTimeMillis();
		File jsonPretty = new File("output/"+owner+"_"+repo+"/"+time+"/commits.json");
		File jsonGraphs = new File("output/"+owner+"_"+repo+"/"+time+"/graphs.json");
		
		CommitRetriever p = new CommitRetriever(user, password, owner, repo, branch);
		List<Commit> commits = p.getCommits();
		
		ChangeImpactGraphGenerator graphGenerator = new ChangeImpactGraphGenerator();
		List<CommitGraph> commitGraphs = graphGenerator.generate(commits, combine, max);
		
		List<JsonCommitGraph> jsonCommitGraphs = JsonBuilder.build(commitGraphs);
		
		Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
		FileUtils.writeStringToFile(jsonPretty, gsonPretty.toJson(commits), true); 
		FileUtils.writeStringToFile(jsonGraphs, gsonPretty.toJson(jsonCommitGraphs), true);
	}
}
