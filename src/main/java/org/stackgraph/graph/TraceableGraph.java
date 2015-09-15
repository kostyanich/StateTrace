package org.stackgraph.graph;

import java.util.List;
import java.util.Optional;

import org.stackgraph.store.Journal;

public class TraceableGraph {

	public static TraceableGraph empty(Journal journal) {
		return new TraceableGraph(journal);
	}

	private final Journal journal;
	private final Optional<Graph> graph;
	private final Optional<List<StateTrace>> traces;
	private final int traceStart;
	private int globalPosition = 0;

	private TraceableGraph(Journal journal) {
		this(journal, null, null, 0, 0);
	}

	private TraceableGraph(Journal journal, Graph graph,
			List<StateTrace> traces, int globalPosition, int traceStart) {
		this.journal = journal;
		this.graph = Optional.ofNullable(graph);
		this.traces = Optional.ofNullable(traces);
		this.globalPosition = globalPosition;
		this.traceStart = traceStart;
	}


	public Optional<TraceableGraph> moveAt(int position) {
		return this.journal.graphAt(position, (pos, graph, traces) -> {
			if (position <= pos) {
				TraceableGraph value = new TraceableGraph(journal, graph,
						traces, position, pos);
				value.reverse(pos - position);
				return Optional.of(value);
			} else {
				return Optional.empty();
			}
		});
	}

	private void reverse(int pos) {
		traces.ifPresent(t -> graph.ifPresent(g -> t.stream().limit(pos)
				.forEach(s -> s.reverse(g))));
	}

	public Optional<TraceableGraph> back() {
		int tracePos = backwardPosition(globalPosition - 1);
		Optional<TraceableGraph> repositioned = 
				traces.filter(t -> tracePos < t.size())
					  .map(t -> t.get(tracePos))
					  .flatMap(s -> graph.map(g -> {
							s.reverse(g);
							globalPosition--;
							return TraceableGraph.this;
					  }));
		if (repositioned.isPresent()) {
			return repositioned;
		} else {
			return moveAt(globalPosition - 1);
		}
	}

	private int backwardPosition(int pos) {
		return traceStart - pos - 1;
	}

	public Optional<TraceableGraph> forward() {
		int tracePos = forwardPosition(globalPosition + 1);
		Optional<TraceableGraph> repositioned = traces
				.filter(t -> tracePos >= 0)
				.map(t -> t.get(tracePos))
				.flatMap(s -> graph.map(g -> {
					s.apply(g);
					globalPosition++;
					return TraceableGraph.this;
				}));
		if (repositioned.isPresent()) {
			return repositioned;
		} else {
			return moveAt(globalPosition + 1);
		}
	}

	private int forwardPosition(int pos) {
		return traceStart - pos;
	}

	public void printGraph() {
		graph.ifPresent(g -> System.out.println(g.toString()));
	}

	public Optional<Graph> getGraph() {
		return graph;
	}
	
	
}
