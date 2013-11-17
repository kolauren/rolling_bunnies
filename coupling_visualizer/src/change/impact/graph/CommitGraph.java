package change.impact.graph;

import java.util.Collection;

import com.google.common.collect.Lists;

public class CommitGraph {
	private String commit_SHA;
	private Collection<ChangeImpactGraph> dependency_graphs;
	
	public CommitGraph() {
		dependency_graphs = Lists.newArrayList();
	}
	
	public String getCommit_SHA() {
		return commit_SHA;
	}
	public void setCommit_SHA(String commit_SHA) {
		this.commit_SHA = commit_SHA;
	}
	public Collection<ChangeImpactGraph> getGraphs() {
		return dependency_graphs;
	}
	public void setGraphs(Collection<ChangeImpactGraph> graphs) {
		this.dependency_graphs = graphs;
	}
	public void addGraph(ChangeImpactGraph graph) {
		dependency_graphs.add(graph);
	}
}
