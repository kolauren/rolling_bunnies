package change.impact.graph.json;

import java.util.List;

import com.google.common.collect.Lists;

import change.impact.graph.ChangeImpactGraph;
import change.impact.graph.CommitGraph;
import change.impact.graph.CommitMethod;

public class JsonBuilder {
	public static List<JsonCommitGraph> build(List<CommitGraph> commitGraphs) {
		List<JsonCommitGraph> jcgs = Lists.newArrayList();
		
		for(CommitGraph commitGraph : commitGraphs) {
			JsonCommitGraph jcg = new JsonCommitGraph();
			jcgs.add(jcg);

			jcg.commit_SHA = commitGraph.getCommitSHA();

			for(ChangeImpactGraph cig : commitGraph.getGraphs()) {
				JsonChangeImpactGraph jcig = new JsonChangeImpactGraph();
				jcg.dependency_graphs.add(jcig);

				jcig.method_id = cig.getRoot().getMethod().getId();

				for(CommitMethod node : cig.getNodes()) {
					JsonAdjacencyList jal = new JsonAdjacencyList();
					jcig.adjacency_list.add(jal);

					jal.method_id = node.getMethod().getId();
					jal.method_name = node.getMethod().getName();
					jal.class_name = node.getMethod().getClassName();
					jal.status = node.getStatus().getStatusName();
					jal.adjacent.addAll(cig.getAdjacentNodes(node));
				}
			}
		}
		return jcgs;
	}
}
