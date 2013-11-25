package change.impact.graph;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Lists;

public class CommitGraph {
	private String commitSHA;
	private Collection<ChangeImpactGraph> graphs;
	private Map<String, String> renameMap;
	private Collection<String> removedIDs;
	 
	public CommitGraph() {
		graphs = Lists.newArrayList();
	}
	
	public String getCommitSHA() {
		return commitSHA;
	}
	public void setCommitSHA(String commitSHA) {
		this.commitSHA = commitSHA;
	}
	public Collection<ChangeImpactGraph> getGraphs() {
		return graphs;
	}
	public void setGraphs(Collection<ChangeImpactGraph> graphs) {
		this.graphs = graphs;
	}
	public void addGraph(ChangeImpactGraph graph) {
		graphs.add(graph);
	}

	public Map<String, String> getRenameMap() {
		return renameMap;
	}

	public void setRenameMap(Map<String, String> renameMap) {
		this.renameMap = renameMap;
	}

	public Collection<String> getRemovedIDs() {
		return removedIDs;
	}

	public void setRemovedIDs(Collection<String> removedIDs) {
		this.removedIDs = removedIDs;
	}
	
}
