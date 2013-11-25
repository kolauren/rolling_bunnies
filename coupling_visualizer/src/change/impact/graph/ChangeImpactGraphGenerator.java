package change.impact.graph;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import change.impact.graph.commit.Commit;

public class ChangeImpactGraphGenerator {

	public List<CommitGraph> generate(List<Commit> commits, int combineNumCommits, int max) throws MalformedURLException, IOException {
		int stopAtNum = commits.size() < max ? commits.size() : max;
		List<CommitGraph> commitGraphs = Lists.newArrayList();
		RepositoryState repositoryState = new RepositoryState();

		for(int i=0; i<stopAtNum; i+=combineNumCommits) {
			System.out.println(commits.get(i).getSha());

			CommitGraph commitGraph = new CommitGraph();
			Set<Method> changedMethods = Sets.newHashSet();

			for(int j=0; j<combineNumCommits && i+j < stopAtNum; j++) {
				Commit commit = commits.get(i+j);

				if(commitGraph.getCommitSHA() == null)
					commitGraph.setCommitSHA(commit.getSha());
				else
					commitGraph.setCommitSHA(commitGraph.getCommitSHA()+"_"+commit.getSha());

				changedMethods.addAll(repositoryState.update(commit));
			}
			Collection<ChangeImpactGraph> graphs = generateGraphsForChangedMethods(changedMethods, repositoryState.getCurrentMethodDependencies());
			commitGraph.setGraphs(graphs);
			commitGraphs.add(commitGraph);
		}
		return commitGraphs;
	}

	private Collection<ChangeImpactGraph> generateGraphsForChangedMethods(Set<Method> changedMethods, Map<Method, Set<Method>> currentMethodDependencies) {
		Collection<ChangeImpactGraph> graphs = Lists.newArrayList();

		//generate dependency graph for each method
		for(Method root : changedMethods) {
			ChangeImpactGraph graph = new ChangeImpactGraph();
			Queue<Method> frontier = Queues.newArrayDeque();
			Set<Method> frontierSet = Sets.newHashSet();
			//cycle check
			Set<Method> visited = Sets.newHashSet();

			frontier.add(root);
			frontierSet.add(root);

			//BFS
			while(!frontier.isEmpty()) {
				Method method = frontier.poll();
				frontierSet.remove(method);

				CommitMethod commitMethod = new CommitMethod();
				commitMethod.setMethod(method);

				if(changedMethods.contains(method))
					commitMethod.setStatus(ChangeStatus.CHANGED);
				else 
					commitMethod.setStatus(ChangeStatus.UNAFFECTED);

				if(visited.isEmpty())
					graph.setRoot(commitMethod);

				Set<Method> dependencies = currentMethodDependencies.get(method);
				graph.setAdjacentNodes(commitMethod, filterMethodIDs(dependencies));

				visited.add(method);

				for(Method dependency : dependencies) {
					if(!frontierSet.contains(dependency) && !visited.contains(dependency)) {
						frontier.add(dependency);
						frontierSet.add(dependency);
					}
				}
			}
			calculateChangeImpact(graph);
			graphs.add(graph);
		}
		return graphs;
	}

	//propagate affected, and changed, status' up the graph
	//stop when nothing changes
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

	private Set<String> filterMethodIDs(Set<Method> methods) {
		Set<String> methodIDs = Sets.newHashSet();
		for(Method method : methods) {
			methodIDs.add(method.getId());
		}
		return methodIDs;
	}
}
