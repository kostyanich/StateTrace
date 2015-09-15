package org.stackgraph.store;

import java.util.List;
import java.util.Optional;

import org.stackgraph.graph.Graph;
import org.stackgraph.graph.StateTrace;
import org.stackgraph.graph.TriFunction;

public interface JournalStore {
	void add(int position, Graph graph);
	
	void add(int position, Graph graph, List<StateTrace> stateChange);
	
	<R> Optional<R> lookup(int position, TriFunction<R> trace);
}
