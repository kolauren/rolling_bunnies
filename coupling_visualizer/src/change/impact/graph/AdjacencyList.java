package change.impact.graph;

import java.util.Collection;

public interface AdjacencyList<K,A> {
	Collection<K> getNodes();
	Collection<A> getAdjacencies(K node);
	boolean isAdjacent(K node);
	boolean addAdjacency(String node, String adjacent);
}
