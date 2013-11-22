package change.impact.graph;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import change.impact.graph.ast.parser.ASTWrapper;
import change.impact.graph.ast.parser.ASTExplorer;
import change.impact.graph.commit.Commit;

public class ChangeImpactGraphGenerator {
	//filepath -> ast
	private Map<String,ASTWrapper> currentASTs;
	private Map<String,ASTWrapper> previousASTs;
	//method id -> [method_id]
	private Map<String,Set<String>> currentAdjacencyList;
	//method id -> Method
	//never removes a method once its added
	private Map<String,Method> currentMethods;

	private Map<String,ASTWrapper> backupASTs;
	private Map<String,ASTWrapper> backupBackupASTs;

	public ChangeImpactGraphGenerator() {
		currentASTs = Maps.newHashMap();
		previousASTs = Maps.newHashMap();
		currentAdjacencyList = Maps.newHashMap();
		currentMethods = Maps.newHashMap();
		backupASTs = Maps.newHashMap();
		backupBackupASTs = Maps.newHashMap();
	}

	public List<CommitGraph> generate(List<Commit> commits, int numCommits) throws MalformedURLException, IOException {
		List<CommitGraph> commitGraphs = Lists.newArrayList();

		for(int i=0; i<commits.size(); i+=numCommits) {
			System.out.println(commits.get(i).getSha());
			CommitGraph commitGraph = new CommitGraph();
			Set<String> changedMethods = Sets.newHashSet();
			for(int j=0; j<numCommits && i+j < commits.size(); j++) {
				Commit commit = commits.get(i+j);

				if(commitGraph.getCommit_SHA() == null)
					commitGraph.setCommit_SHA(commit.getSha());
				else
					commitGraph.setCommit_SHA(commitGraph.getCommit_SHA()+"_"+commit.getSha());

				changedMethods.addAll(updateState(commit));
			}
			commitGraph.setGraphs(generateGraphsForChangedMethods(changedMethods));
			commitGraphs.add(commitGraph);
		}
		return commitGraphs;
	}

	Set<String> updateState(Commit commit) throws MalformedURLException, IOException {
		updateNewProjectName(commit);
		updateASTs(commit);
		Set<String> changedMethods = updateCurrentAdjacencyListAndMethods(commit);
		return changedMethods;
	}

	private Collection<ChangeImpactGraph> generateGraphsForChangedMethods(Set<String> changedMethods) {
		Collection<ChangeImpactGraph> graphs = Lists.newArrayList();

		//generate dependency graph for each method
		for(String rootID : changedMethods) {
			ChangeImpactGraph graph = new ChangeImpactGraph();
			Queue<String> frontier = Queues.newPriorityQueue();
			Set<String> frontierSet = Sets.newHashSet();
			//cycle check
			Set<String> visited = Sets.newHashSet();

			frontier.add(rootID);
			frontierSet.add(rootID);


			//BFS
			while(!frontier.isEmpty()) {
				String nodeID = frontier.poll();
				frontierSet.remove(nodeID);

				CommitMethod node = new CommitMethod();
				node.setMethod(currentMethods.get(nodeID));

				if(changedMethods.contains(nodeID)) {
					node.setStatus(ChangeStatus.CHANGED);
				} else {
					node.setStatus(ChangeStatus.UNAFFECTED);
				}

				if(visited.isEmpty())
					graph.setRoot(node);

				Set<String> adjacentNodes = currentAdjacencyList.get(nodeID);
				graph.setAdjacentNodes(node, adjacentNodes);

				visited.add(nodeID);

				for(String adjacentNode : adjacentNodes) {
					if(!frontierSet.contains(adjacentNode) && !visited.contains(adjacentNode)) {
						frontier.add(adjacentNode);
						frontierSet.add(adjacentNode);
					}
				}
			}
			calculateChangeImpact(graph);
			graphs.add(graph);
		}
		return graphs;
	}

	private void calculateChangeImpact(ChangeImpactGraph graph) {
		boolean statusChanged = true;
		while(statusChanged) {
			statusChanged = false;
			Collection<CommitMethod> nodes = graph.getNodes();
			for(CommitMethod node : nodes) {
				if(node.getStatus() == ChangeStatus.UNAFFECTED) {
					Set<String> adjacentIDs = graph.getAdjacentNodes(node);
					for(String adjacentID : adjacentIDs) {
						CommitMethod adjacentMethod = graph.getCommitMethod(adjacentID);
						ChangeStatus status = adjacentMethod.getStatus();
						if(status == ChangeStatus.AFFECTED || status == ChangeStatus.CHANGED) {
							node.setStatus(ChangeStatus.AFFECTED);
							statusChanged = true;
							break;
						}
					}
				}
			}
		}
	}

	//store method ids only
	private Set<String> filterID(Set<Method> methods) {
		//the method was removed so it has a null method invocation set
		if(methods == null)
			return null;

		Set<String> ids = Sets.newHashSet();
		for(Method method : methods) {
			boolean unique = ids.add(method.getId());
			assert(unique);
		}
		return ids;
	}

	private Map<String,Set<String>> filterID(Map<Method, Set<Method>> methodMap) {
		Map<String,Set<String>> idMap = Maps.newHashMap();

		for(Method m : methodMap.keySet()) {
			idMap.put(m.getId(), filterID(methodMap.get(m)));
		}

		return idMap;
	}

	private void updateASTs(Commit commit) throws MalformedURLException, IOException {
		//add new and modified ASTs
		Set<String> addedModifiedRenamed = Sets.newHashSet();
		addedModifiedRenamed.addAll(commit.getAddedJavaFiles());
		addedModifiedRenamed.addAll(commit.getModifiedJavaFiles());
		addedModifiedRenamed.addAll(commit.getRenamedJavaFiles());

		for(String newFileName : addedModifiedRenamed) {
			ASTWrapper previousAST = null;
			if(commit.isFileRenamed(newFileName)) {
				String oldFileName = commit.getOldFileName(newFileName);
				previousAST = currentASTs.get(oldFileName);

				// merge; rename already happened in previous commit
				ASTWrapper futurePreviousAST = previousASTs.get(newFileName);
				if(futurePreviousAST != null) {
					continue;
				}

				currentASTs.remove(oldFileName);
				previousASTs.remove(oldFileName);
			} else {
				//added files will return null
				previousAST = currentASTs.get(newFileName);
				currentASTs.remove(newFileName);
				previousASTs.remove(newFileName);
			}
			//			TODO: this is a hack to deal with branch merging
			//			if(oldName != null && previousAST == null) {
			//				previousAST = backupASTs.get(oldName);
			//				if(previousAST == null) {
			//					previousAST = backupBackupASTs.get(oldName);
			//
			//				}
			//			}

			//update the ASTs
			previousASTs.put(newFileName, previousAST);
			String url = commit.getDiff(newFileName).getRawCodeURL();
			ASTWrapper currentAST = ASTExplorer.generateAST(url, newFileName);
			currentASTs.put(newFileName, currentAST);
		}

		//TODO: clean up methods?
		for(String newFileName : commit.getRemovedJavaFiles()) {
			ASTWrapper previousAST = currentASTs.get(newFileName);
			//			//TODO: this is a hack to deal with removed, branch merging
			//			if(previousAST == null)
			//				previousAST = ASTExplorer.generateAST(commit.getDiff(clazz).getRawCodeURL(), clazz);
			previousASTs.put(newFileName, previousAST);
			currentASTs.remove(newFileName);
		}
	}

	//updates ASTs with project directory change ONLY (no modified code)
	private void updateNewProjectName(Commit commit) {
		for(String newFileName : commit.getRenamedProject()) {
			String oldFileName = commit.getOldFileName(newFileName);
			updateNewProjectNameAST(newFileName, oldFileName);
		}
	}

	private void updateNewProjectNameAST(String newFileName, String oldFileName) {
		ASTWrapper previousAST = previousASTs.get(oldFileName);
		ASTWrapper currentAST = currentASTs.get(oldFileName);

		ASTWrapper futurePreviousAST = previousASTs.get(newFileName);

		// this commit is a merge; files already updated
		// in a previous commit
		if(null == previousAST && null == currentAST && futurePreviousAST != null)
			return;

		previousAST.setSourceLoc(newFileName);
		currentAST.setSourceLoc(newFileName);

		previousASTs.put(newFileName, previousAST);
		currentASTs.put(newFileName, currentAST);

		previousASTs.remove(oldFileName);
		currentASTs.remove(oldFileName);
	}

	// updates the currentAdjacencyList and currentMethods
	// returns a list of changed methods
	private Set<String> updateCurrentAdjacencyListAndMethods(Commit commit) {
		Set<String> changedMethods = Sets.newHashSet();
		for(String newFileName : commit.getDiffs().keySet()) {
			// if file was renamed, update method ID's in the adjacency list
			// and method list
			if(commit.isFileRenamed(newFileName)) {
				String oldFileName = commit.getOldFileName(newFileName);
				updateMethodIDsInAdjacencyList(newFileName, oldFileName);
				updateMethodIDsInMethodList(newFileName, oldFileName);
			}
			// find modified modified methods mapped to a set of its method invocations
			Map<Method, Set<Method>> adjacentNodes = Maps.newHashMap();

			// map added lines to modified methods -> {method invocations}
			Map<Integer,String> addedLines = commit.getDiff(newFileName).getAddedLines();
			List<Integer> addedLineNumbers = Lists.newArrayList();
			addedLineNumbers.addAll(addedLines.keySet());
			ASTWrapper currentAST = currentASTs.get(newFileName);
			if(!addedLineNumbers.isEmpty())
				adjacentNodes.putAll(ASTExplorer.getMethodInvocations(addedLineNumbers, currentASTs, currentAST));

			// map removed lines to modified methods -> {method invocations}
			Map<Integer,String> removedLines = commit.getDiff(newFileName).getRemovedLines();
			List<Integer> removedLineNumbers = Lists.newArrayList();
			removedLineNumbers.addAll(removedLines.keySet());
			ASTWrapper previousAST = previousASTs.get(newFileName);
			if(!removedLines.isEmpty())
				adjacentNodes.putAll(ASTExplorer.getMethodInvocations(removedLineNumbers, currentASTs, previousAST)); 

			// update methods
			updateCurrentMethods(adjacentNodes);

			// update the adjacency list
			Map<String, Set<String>> strAdjacentNodes = filterID(adjacentNodes);
			
			// method -> null
			// means the method was deleted
			Set<String> removedNodes = Sets.newHashSet();
			for(String node : strAdjacentNodes.keySet()) {
				Set<String> strAdjacentNodesSet = strAdjacentNodes.get(node);
				if(strAdjacentNodesSet == null) 
					removedNodes.add(node);
				else 
					currentAdjacencyList.put(node, strAdjacentNodesSet);
			}

			//clean up removed methods in adjacency list and method list
			removeNodesFromAdjacencyList(removedNodes);
			removeMethods(removedNodes);

			//list all the modified methods; deleted aren't counted
			for(String methodID : strAdjacentNodes.keySet()) {
				if(strAdjacentNodes.get(methodID) != null) {
					changedMethods.add(methodID);
				}
			}
		}

		return changedMethods;
	}
	
	//TODO: make constants
	private String[] generateIDreplacementParts(String newFileName, String oldFileName) {
		String regex = "\\w*?/src/(?<packageName>[\\w/]*/)?(?<className>\\w+)\\.java";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(oldFileName);

		m.find();

		String oldPackageName = m.group("packageName");
		if(oldPackageName != null)
			oldPackageName = oldPackageName.replace("/", ".");
		else
			oldPackageName = "NOPACKAGENAME";
		String oldClassName = m.group("className");

		m = p.matcher(newFileName);
		m.find();

		String newPackageName = m.group("packageName");
		if(newPackageName != null)
			newPackageName = newPackageName.replace("/", ".");
		else
			newPackageName = "NOPACKAGENAME";
		String newClassName = m.group("className");

		String delimiter = "-";
		String oldIDpart = oldPackageName+delimiter+oldClassName;
		String newIDpart = newPackageName+delimiter+newClassName;

		//TODO: this should be an object
		return new String[]{ oldIDpart, newIDpart };
	}
	
	private void updateMethodIDsInAdjacencyList(String newFileName, String oldFileName) {
		String[] idParts = generateIDreplacementParts(newFileName, oldFileName);
		String oldIDpart = idParts[0];
		String newIDpart = idParts[1];
	
		Map<String, Set<String>> newAdjacencyList = Maps.newHashMap();

		for(String node : currentAdjacencyList.keySet()) {
			//strings are immutable 
			Set<String> newAdjacentNodes = Sets.newHashSet();
			for(String adjacentNode : currentAdjacencyList.get(node)) {
				// this should match the way method ids are generated by ASTExplorer
				if(adjacentNode.matches(oldIDpart)) {
					String newID = adjacentNode.replace(oldIDpart, newIDpart);
					newAdjacentNodes.add(newID);
				} else {
					newAdjacentNodes.add(adjacentNode);
				}
			}

			String newIDnode = node.replace(oldIDpart, newIDpart);
			newAdjacencyList.put(newIDnode, newAdjacentNodes);
		}
		
		currentAdjacencyList = newAdjacencyList;
	}
	
	private void updateMethodIDsInMethodList(String newFileName, String oldFileName) {
		String[] idParts = generateIDreplacementParts(newFileName, oldFileName);
		String oldIDpart = idParts[0];
		String newIDpart = idParts[1];
		String delimiter = "-";
		
		String newPackageName = newIDpart.split(delimiter)[0];
		String newClassName = newIDpart.split(delimiter)[1];
		
		Map<String, Method> newMethodList = Maps.newHashMap();
		for(String id : currentMethods.keySet()) {
			Method m = currentMethods.get(id);

			if(id.matches(oldIDpart)) {
				m.setPackageName(newPackageName);
				m.setClassName(newClassName);
				
				String newID = id.replace(oldIDpart, newIDpart);
				m.setId(newID);
				
				newMethodList.put(newID, m);
			} else {
				newMethodList.put(id, m);
			}
		}
		
		currentMethods = newMethodList;
	}

	// method -> null		method was removed or renamed (can't tell)
	// method -> {}			method was modified but has no method invocations in body
	// method -> {...}		method was modified and contains method invocations in body
	private void updateCurrentMethods(Map<Method, Set<Method>> methodMap) {
		for(Method node : methodMap.keySet()) {
			Set<Method> adjacentNodes = methodMap.get(node);
			if(adjacentNodes == null) {
				currentMethods.remove(node.getId());
			} else {
				currentMethods.put(node.getId(), node);
				for(Method adjacentNode : methodMap.get(node)) {
					currentMethods.put(adjacentNode.getId(), adjacentNode);
				}
			}
		}
	}

	//removes the node from the adjacency list
	private void removeNodesFromAdjacencyList(Set<String> removedNodes) {
		for(String removedNode : removedNodes) {
			for(String node : currentAdjacencyList.keySet()) {
				Set<String> adjacentNodes = currentAdjacencyList.get(node);
				adjacentNodes.remove(removedNode);
			}
			currentAdjacencyList.remove(removedNode);
		}
	}
	
	private void removeMethods(Set<String> removedNodes) {
		for(String id : removedNodes) {
			currentMethods.remove(id);
		}
	}
}
