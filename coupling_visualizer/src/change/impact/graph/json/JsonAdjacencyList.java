package change.impact.graph.json;

import java.util.Set;

import com.google.common.collect.Sets;

public class JsonAdjacencyList {
	String method_id;
	String method_name;
	String class_name;
	String status;
	Set<String> adjacent;
	
	public JsonAdjacencyList() {
		adjacent = Sets.newHashSet();
	}
}
