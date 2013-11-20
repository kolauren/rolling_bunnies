package change.impact.graph.json;

import java.util.List;

import com.google.common.collect.Lists;

//filters objects for json output
public class JsonCommitGraph {
	String commit_SHA;
	List<JsonChangeImpactGraph> dependency_graphs;

	public JsonCommitGraph() {
		dependency_graphs = Lists.newArrayList();
	}
}
