package change.impact.graph.driver;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

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
		//fill in this info
		String owner = "kolauren";
		String repo = "rolling_bunnies";
		String branch = "master";
		
		//run the analysis
		//it will save json to the output folder
		
		long time = System.currentTimeMillis();
		File jsonPretty = new File("output/"+owner+"_"+repo+"/"+time+"/commits.json");
		File jsonGraphs = new File("output/"+owner+"_"+repo+"/"+time+"/graphs.json");
		
		CommitRetriever p = new CommitRetriever(owner, repo, branch);
		List<Commit> commits = p.getCommits();
		
		ChangeImpactGraphGenerator graphGenerator = new ChangeImpactGraphGenerator();
		List<CommitGraph> commitGraphs = graphGenerator.generate(commits, 1);
		
		List<JsonCommitGraph> jsonCommitGraphs = JsonBuilder.build(commitGraphs);
		
		Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
		FileUtils.writeStringToFile(jsonPretty, gsonPretty.toJson(commits), true); 
		FileUtils.writeStringToFile(jsonGraphs, gsonPretty.toJson(jsonCommitGraphs), true);
	}
}
