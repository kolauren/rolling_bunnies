package change.impact.graph.json;

import java.util.List;

import com.google.common.collect.Lists;

public class JsonChangeImpactGraph {
	String method_id;
	List<JsonAdjacencyList> adjacency_list;
	
	public JsonChangeImpactGraph() {
		adjacency_list = Lists.newArrayList();
	}
}
