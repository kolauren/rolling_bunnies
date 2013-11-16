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

import change.impact.graph.ast.parser.ASTWrapper;
import change.impact.graph.ast.parser.ASTExplorer;
import change.impact.graph.commit.Commit;

public class DependencyGraphGenerator {
	//filepath -> ast
	private Map<String,ASTWrapper> currentASTs;
	private Map<String,ASTWrapper> previousASTs;
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
			ASTWrapper previousAST = currentASTs.get(clazz);
			previousASTs.put(clazz, previousAST);
			//update current AST
			String url = commit.getDiff(clazz).getNewCode();
			ASTWrapper currentAST = ASTExplorer.generateAST(url);
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
			ASTWrapper currentAST = currentASTs.get(clazz);
			Map<Integer,String> addedLines = commit.getDiff(clazz).getAddedLines();
			Map<Method, Set<Method>> newAdjacentNodes = ASTExplorer.getMethodsCalledByMethodsInLines(addedLines, currentAST);
			for(Method m : newAdjacentNodes.keySet()) {
				if(isExistingMethod(m.getId())) {
					for(Method adjacent : newAdjacentNodes.get(m)) {
						if(isProjectMethod(adjacent.getId())) {
							
						}
					}
				}
			}
			//generate dependency graph for methods with removed lines
		}
	}
	
	private boolean isExistingMethod(String id) {
		return currentMethods.containsKey(id);
	}

	private boolean isProjectMethod(String id) {
		return getASTforMethod(id) != null;
	}
	/**
	 * returns null if method is not part of project
	 * @param method
	 * @return
	 */
	private ASTWrapper getASTforMethod(String id) {
		return null;
	}
}
