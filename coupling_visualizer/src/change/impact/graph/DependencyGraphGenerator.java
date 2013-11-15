package change.impact.graph;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.CompilationUnit;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import change.impact.graph.commit.Commit;

public class DependencyGraphGenerator {
	//filepath -> ast
	private Map<String,CompilationUnit> currentASTs;
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
		updateCurrentASTs(commit);
		
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

	//store method ids only
	private Set<String> filterID(Set<Method> methods) {
		Set<String> ids = Sets.newHashSet();
		for(Method method : methods) {
			boolean unique = ids.add(method.getId());
			assert(unique);
		}
		return ids;
	}

	public void updateCurrentASTs(Commit commit) throws MalformedURLException, IOException {
		//add new and modified ASTs
		Iterable<String> addedOrModified = Iterables.concat(commit.getAddedJavaFiles(), commit.getModifiedJavaFiles());
		for(String clazz : addedOrModified) {
			InputStream currentCodeSource = null;
			try {
				currentCodeSource = new URL(commit.getDiff(clazz).getNewCode()).openStream();
			} finally {
				currentCodeSource.close();
			}
			//CompilationUnit currentAST = JavaParser.parse(currentCodeSource);
			//currentASTs.put(clazz, currentAST);
		}
		
		for(String clazz : commit.getRemovedJavaFiles()) {
			currentASTs.remove(clazz);
		}
	}
	
	private void updateCurrentAdjacencyListAndMethods(Commit commit) {
		
	}

	/**
	 * returns null if method is not part of project
	 * @param method
	 * @return
	 */
	private CompilationUnit getASTforMethod(Method method) {
		return null;
	}
	
}
