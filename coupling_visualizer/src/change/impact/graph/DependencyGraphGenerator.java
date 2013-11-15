package change.impact.graph;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import change.impact.graph.ast.parser.AST;
import change.impact.graph.ast.parser.ASTparser;
import change.impact.graph.commit.Commit;

public class DependencyGraphGenerator {
	//filepath -> ast
	private Map<String,AST> currentASTs;
	private Map<String,AST> previousASTs;
	private Map<String,Set<String>> currentAdjacencyList;
	private Map<String,Method> currentMethods;
	
	public DependencyGraphGenerator() {
		currentASTs = Maps.newHashMap();
	}
	
	public List<CommitGraph> generate(Collection<Commit> commits) throws MalformedURLException, IOException {
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
	private Collection<DependencyGraph> generateGraphsForChangedMethods(Commit commit) throws MalformedURLException, IOException {
		updateASTs(commit);
		
		//generate dependency graphs for all modified methods in every class
		
		return null;
	}

	//store method ids only
	private Set<String> filterID(Set<Method> methods) {
		Set<String> ids = Sets.newHashSet();
		for(Method method : methods) {
			boolean unique = ids.add(method.getId());
			assert(unique);
		}
		return ids;
	}

	public void updateASTs(Commit commit) throws MalformedURLException, IOException {
		//add new and modified ASTs
		Iterable<String> addedOrModified = Iterables.concat(commit.getAddedJavaFiles(), commit.getModifiedJavaFiles());
		for(String clazz : addedOrModified) {
			//update previous AST
			AST previousAST = currentASTs.get(clazz);
			previousASTs.put(clazz, previousAST);
			//update current AST
			String url = commit.getDiff(clazz).getNewCode();
			AST currentAST = ASTparser.generateAST(url);
			currentASTs.put(clazz, currentAST);
		}
		
		for(String clazz : commit.getRemovedJavaFiles()) {
			previousASTs.put(clazz, currentASTs.get(clazz));
			currentASTs.remove(clazz);
		}
	}
	
	private void updateCurrentAdjacencyListAndMethods(Commit commit) {
		for(String clazz : commit.getDiffs().keySet()) {
			//generate dependency graph for method with added lines
			for(int lineNumber : commit.getDiff(clazz).getAddedLines().keySet()) {
				AST currentAST = currentASTs.get(clazz);
			}
			//generate dependency graph for methods with removed lines
			for(int lineNumber : commit.getDiff(clazz).getRemovedLines().keySet()) {
				
			}
		}
	}

	/**
	 * returns null if method is not part of project
	 * @param method
	 * @return
	 */
	private AST getASTforMethod(Method method) {
		return null;
	}
	
}
