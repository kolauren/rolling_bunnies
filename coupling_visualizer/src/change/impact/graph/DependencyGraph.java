package change.impact.graph;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DependencyGraph implements AdjacencyList<String, String> {
	private Map<String,Set<String>> adjacencyList;
	private String methodID;
	//method id -> method
	private Map<String, Method> methods;

	public DependencyGraph() {
		adjacencyList = Maps.newHashMap();
		methods = Maps.newHashMap();
	}
	
	public String getMethodID() {
		return methodID;
	}

	public void setMethodName(String methodName) {
		this.methodID = methodName;
	}
	
	public void addMethod(String id, Method method) {
		methods.put(id, method);
	}
	
	public Method getMethod(String id) {
		return methods.get(id);
	}
	
	public void setAdjacentNodes(String node, Set<String> adjacentNodes) {
		adjacencyList.put(node, adjacentNodes);
	}
	
	@Override
	public Collection<String> getNodes() {
		return adjacencyList.keySet();
	}

	@Override
	public Collection<String> getAdjacentNodes(String node) {
		return adjacencyList.get(node);
	}

	@Override
	public boolean isAdjacent(String node) {
		return adjacencyList.get(node).isEmpty();
	}

	@Override
	public boolean addAdjacentNode(String node, String adjacent) {
		Set<String> adjacencies = adjacencyList.get(node);
		if(adjacencies == null) {
			adjacencies = Sets.newHashSet();
			adjacencyList.put(node, adjacencies);
		}
		return adjacencies.add(adjacent);
	}
}
