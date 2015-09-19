package org.stackgraph.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.stackgraph.graph.Graph;
import org.stackgraph.graph.StateTrace;
import org.stackgraph.graph.TriFunction;

public class Journal {

	private final JournalStore store;
	private final int blockSize;
	private int currentPosition = 0;
	private List<StateTrace> cumulative = new ArrayList<StateTrace>();

	public Journal(JournalStore store, int blockSize) {
		this.store = store;
		this.blockSize = blockSize;
	}

	public void add(Graph graph, List<StateTrace> traces) {
		cumulative.addAll(traces);
		currentPosition += traces.size();
		if (cumulative.size() >= blockSize) {
			store(graph);
		}
	}

	private void store(Graph graph) {
		Collections.reverse(cumulative);
		store.add(currentPosition, graph, cumulative);
		cumulative = new ArrayList<StateTrace>();
	}

	public <R> Optional<R> graphAt(int position, TriFunction<R> combine) {
		if (0 <= position && position <= currentPosition) {
			return store.lookup(position, combine);
		} else {
			return Optional.empty();
		}
	}

	public void flush(Graph graph) {
		if (!cumulative.isEmpty()) {
			store(graph);
		}
	}

	public int maxPosition() {
		return currentPosition;
	}

}
