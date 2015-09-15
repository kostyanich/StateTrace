package org.stackgraph.graph;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.stackgraph.graph.StateTrace.traceOfOne;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.stackgraph.event.StateChanged;

public class Impact {
	
	public static Impact single(List<StateTrace> traces) {
		return new Impact(traces, emptySet());
	}

	public static Impact empty() {
		return new Impact(emptyList(), emptySet());
	}

	private final Set<Component> components;
	private final List<StateTrace> traces;

	public Impact(List<StateTrace> traces, Set<Component> component) {
		this.components = component;
		this.traces = traces;
	}

	public Impact(List<StateTrace> traces, Component component) {
		this(traces, singleton(component));
	}

	public List<StateTrace> findTraces() {
		Optional<Impact> next = Optional.of(this);
		do {
			Impact impact = next.get();
			next = impact.nextIteration();
		} while (next.isPresent());
		return traces;
	}
	
	public Optional<Impact> nextIteration() {
		Set<StateChanged> events = components.stream()
											 .flatMap(Component::refreshedStates)
											 .collect(toSet());
		if (events.size() == 0) {
			return Optional.empty();
		} else {
			traces.add(traceOfOne(events));
			
			Set<String> impactedNames = events.stream().map(StateChanged::getTarget)
													   .collect(toSet());
			Stream<Component> impacted = components.stream().filter(c -> impactedNames.contains(c.name()));
			Set<Component> next = impacted.flatMap(c -> c.outbound().stream()).collect(toSet());
			return Optional.of(new Impact(traces, next));
		}
	}

}
