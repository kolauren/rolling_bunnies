package change.impact.graph;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DependencyGraph implements AdjacencyList<CommitMethod, String> {
	private Map<CommitMethod,Set<String>> adjacencyList;
	private CommitMethod root;

	
	public Map<CommitMethod, Set<String>> getAdjacencyList() {
		return adjacencyList;
	}

	public void setAdjacencyList(Map<CommitMethod, Set<String>> adjacencyList) {
		this.adjacencyList = adjacencyList;
	}

	public CommitMethod getRoot() {
		return root;
	}

	public void setRoot(CommitMethod root) {
		this.root = root;
	}

	public void setAdjacentNodes(CommitMethod node, Set<String> adjacentNodes) {
		adjacencyList.put(node, adjacentNodes);
	}
	
	@Override
	public Collection<CommitMethod> getNodes() {
		return adjacencyList.keySet();
	}

	@Override
	public Collection<String> getAdjacentNodes(CommitMethod node) {
		return adjacencyList.get(node);
	}

	@Override
	public boolean addAdjacentNode(CommitMethod node, String adjacent) {
		//lazy init
		if(adjacencyList == null)
			adjacencyList = Maps.newHashMap();
		
		Set<String> adjacentNodes = adjacencyList.get(node);
		if(adjacentNodes == null) {
			adjacentNodes = Sets.newHashSet();
			adjacencyList.put(node, adjacentNodes);
		}
		return adjacentNodes.add(adjacent);
	}
}
