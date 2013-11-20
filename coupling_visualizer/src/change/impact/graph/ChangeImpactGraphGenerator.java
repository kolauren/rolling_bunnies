package change.impact.graph;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

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
		addedModifiedRenamed.addAll(commit.getRenamedJavaFiles().keySet());

		for(String clazz : addedModifiedRenamed) {
			String oldName = commit.getOldFileName(clazz);
			String previousName = oldName == null ? clazz : oldName;
			//update previous AST
			ASTWrapper previousAST = currentASTs.get(previousName);
			//TODO: this is a hack to deal with branch merging
			if(oldName != null && previousAST == null) {
				previousAST = backupASTs.get(oldName);
				if(previousAST == null) {
					previousAST = backupBackupASTs.get(oldName);
					//probably regex'd some comment that had class definition in it
					if(previousAST == null && commit.getModifiedJavaFiles().contains(clazz)) {
						previousAST = currentASTs.get(clazz);
						oldName = clazz;
						commit.getRenamedJavaFiles().put(clazz, null);
						previousName = clazz;
					}
				}
			}



			previousASTs.put(previousName, previousAST);
			//update current AST
			//project rename only; code is the same
			if(commit.getDiff(clazz).isProjectRename()) {
				currentASTs.put(clazz, previousAST);
			} else {
				String url = commit.getDiff(clazz).getRawCodeURL();
				ASTWrapper currentAST = ASTExplorer.generateAST(url, clazz);
				currentASTs.put(clazz, currentAST);
			}
			//TODO: clean up old ASTs
			if(oldName != null && !oldName.equals(clazz)) {
				backupBackupASTs.put(oldName, backupASTs.get(oldName));
				backupASTs.put(oldName, currentASTs.get(oldName));
				currentASTs.remove(oldName);
			}
		}

		for(String clazz : commit.getRemovedJavaFiles()) {
			ASTWrapper previousAST = currentASTs.get(clazz);
			//TODO: this is a hack to deal with removed, branch merging
			if(previousAST == null)
				previousAST = ASTExplorer.generateAST(commit.getDiff(clazz).getRawCodeURL(), clazz);
			previousASTs.put(clazz, previousAST);
			currentASTs.remove(clazz);
		}
	}

	private Set<String> updateCurrentAdjacencyListAndMethods(Commit commit) {
		Set<String> changedMethods = Sets.newHashSet();
		for(String clazz : commit.getDiffs().keySet()) {
			//generate dependency graph for method with added lines
			Map<Method, Set<Method>> adjacentNodes = Maps.newHashMap();

			//do added lines
			Map<Integer,String> addedLines = commit.getDiff(clazz).getAddedLines();
			List<Integer> addedLineNumbers = Lists.newArrayList();
			addedLineNumbers.addAll(addedLines.keySet());
			ASTWrapper currentAST = currentASTs.get(clazz);
			if(!addedLineNumbers.isEmpty())
				adjacentNodes.putAll(ASTExplorer.getMethodInvocations(addedLineNumbers, currentASTs, currentAST, null));

			//do removed lines
			Map<Integer,String> removedLines = commit.getDiff(clazz).getRemovedLines();
			List<Integer> removedLineNumbers = Lists.newArrayList();
			removedLineNumbers.addAll(removedLines.keySet());
			String fileName = clazz;
			//check if file was renamed
			String oldFilePath = commit.getOldFileName(clazz);
			String currFilePath = null;
			if(oldFilePath != null) {
				fileName = oldFilePath;
				currFilePath = clazz;
			}
			//refer to the previousAST using the old filename
			ASTWrapper previousAST = previousASTs.get(fileName);
			if(!removedLines.isEmpty())
				adjacentNodes.putAll(ASTExplorer.getMethodInvocations(removedLineNumbers, currentASTs, previousAST, currFilePath)); 

			//TODO: do we want to remove deleted methods?
			updateCurrentMethods(adjacentNodes);

			Map<String, Set<String>> strAdjacentNodes = filterID(adjacentNodes);
			//update the adjacency list
			Set<String> removedNodes = Sets.newHashSet();
			for(String node : strAdjacentNodes.keySet()) {
				Set<String> strAdjacentNodesSet = strAdjacentNodes.get(node);
				if(strAdjacentNodesSet == null) 
					removedNodes.add(node);
				else 
					currentAdjacencyList.put(node, strAdjacentNodesSet);

			}
			//clean up removed methods
			//TODO: remove methods from removed files
			removeNodesFromAdjacencyList(removedNodes);

			//deleted methods do not get a graph
			for(String methodID : strAdjacentNodes.keySet()) {
				if(strAdjacentNodes.get(methodID) != null) {
					changedMethods.add(methodID);
				}
			}
		}

		return changedMethods;
	}

	//adds all methods from the map into the currentMethods
	private void updateCurrentMethods(Map<Method, Set<Method>> methodMap) {
		for(Method node : methodMap.keySet()) {
			currentMethods.put(node.getId(), node);
			Set<Method> adjacentNodes = methodMap.get(node);
			//method was deleted
			if(adjacentNodes == null) {
				currentMethods.remove(node.getId());
			} else {
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
				if(node == removedNode)
					currentAdjacencyList.remove(removedNode);
				else
					adjacentNodes.remove(removedNode);
			}
		}
	}
}
