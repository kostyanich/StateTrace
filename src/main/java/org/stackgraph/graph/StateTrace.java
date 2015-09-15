package org.stackgraph.graph;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.stackgraph.event.GraphEvent;

public class StateTrace implements Serializable {
		private static final long serialVersionUID = 680313514779217977L;
	
	@SafeVarargs
	public static List<StateTrace> traceOf(Stream<? extends GraphEvent> ... events) {
		List<StateTrace> traces = new ArrayList<StateTrace>();
		of(events).map(s -> s.collect(toSet()))
						 .filter(s -> s.size() > 0)
						 .forEach(s -> traces.add(new StateTrace(s)));
		return traces;
	}

	public static StateTrace traceOfOne(Stream<? extends GraphEvent> events) {
		return new StateTrace(events.collect(toSet()));
	}

	public static StateTrace traceOfOne(Set<? extends GraphEvent> events) {
		return new StateTrace(events);
	}
	
	private Set<? extends GraphEvent> next;
	
	public StateTrace (Set<? extends GraphEvent> next) {
		this.next = next;
	}

	public void apply(Graph graph) {
		next.stream().forEach(e -> graph.apply(e));
	}
	
	public void reverse(Graph graph) {
		next.stream().forEach(e -> graph.reverse(e));
	}

	public Set<? extends GraphEvent> getNext() {
		return next;
	}


	
}
