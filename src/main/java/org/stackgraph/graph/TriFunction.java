package org.stackgraph.graph;

import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface TriFunction<R> {

	Optional<R> apply(int pos, Graph graph, List<StateTrace> stateTrace);

}
