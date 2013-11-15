package change.impact.graph;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DependencyGraph implements AdjacencyList<String, String> {
	private Map<String,Set<String>> adjacencyList;
	private String methodName;
	private Map<String, Method> methods;

	public DependencyGraph() {
		adjacencyList = Maps.newHashMap();
		methods = Maps.newHashMap();
	}
	
	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	public void addMethod(String methodName, Method method) {
		methods.put(methodName, method);
	}
	
	public Method getMethod(String methodName) {
		return methods.get(methodName);
	}
	
	public void setAdjacencies(String node, Set<String> adjacencies) {
		adjacencyList.put(node, adjacencies);
	}
	
	@Override
	public Collection<String> getNodes() {
		return adjacencyList.keySet();
	}

	@Override
	public Collection<String> getAdjacencies(String node) {
		return adjacencyList.get(node);
	}

	@Override
	public boolean isAdjacent(String node) {
		return adjacencyList.get(node).isEmpty();
	}

	@Override
	public boolean addAdjacency(String node, String adjacent) {
		Set<String> adjacencies = adjacencyList.get(node);
		if(adjacencies == null) {
			adjacencies = Sets.newHashSet();
			adjacencyList.put(node, adjacencies);
		}
		return adjacencies.add(adjacent);
	}
}
