package change.impact.graph;

import java.util.Collection;

public interface AdjacencyList<K,A> {
	Collection<K> getNodes();
	Collection<A> getAdjacentNodes(K node);
	boolean addAdjacentNode(K node, A adjacent);
}
