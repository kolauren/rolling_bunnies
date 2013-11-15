package change.impact.graph;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.stmt.BlockStmt;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import change.impace.graph.ast.parser.ASTparser;
import change.impact.graph.commit.Commit;

public class DependencyGraphGenerator {
	private Map<String,CompilationUnit> currentASTs;
	
	public DependencyGraphGenerator() {
		currentASTs = Maps.newHashMap();
	}
	
	public List<CommitGraph> generate(Collection<Commit> commits) throws MalformedURLException, ParseException, IOException {
		List<CommitGraph> commitGraphs = Lists.newArrayList();
		for(Commit commit : commits) {
			CommitGraph commitGraph = new CommitGraph();
			commitGraph.setCommit_SHA(commit.getSha());
			commitGraph.setGraphs(generateGraphsForChangedMethods(commit));
		}
		return commitGraphs;
	}
	
	//TODO: generate ASTs for new/modified classes first. When tracing removed line, 
	//use OLD ast to find method container then use CURRENT ast for building dependencies
	public Collection<DependencyGraph> generateGraphsForChangedMethods(Commit commit) throws MalformedURLException, ParseException, IOException {
		generateCurrentASTs(commit);
		//generate dependency graphs for all modified methods in every class
		for(String clazz : commit.getDiffs().keySet()) {
			//keep track of graphs already generated for a method 
			Map<String,DependencyGraph> generated = Maps.newHashMap();
			//generate dependency graph for method with added lines
			for(int lineNumber : commit.getDiff(clazz).getAddedLines().keySet()) {
				CompilationUnit currentAST = currentASTs.get(clazz);
			}
			//generate dependency graph for methods with removed lines
			for(int lineNumber : commit.getDiff(clazz).getRemovedLines().keySet()) {
				
			}
		}
		return null;
	}
	
	private Set<String> findAdjacencies(MethodDeclaration method) {
		return null;
	}

	public void generateCurrentASTs(Commit commit) throws MalformedURLException, IOException, ParseException {
		for(String clazz : commit.getDiffs().keySet()) {
			InputStream currentCodeSource = null;
			try {
				currentCodeSource = new URL(commit.getDiff(clazz).getNewCode()).openStream();
			} finally {
				currentCodeSource.close();
			}
			CompilationUnit currentAST = JavaParser.parse(currentCodeSource);
			currentASTs.put(clazz, currentAST);
		}
	}
	

	
}
