package org.stackgraph.graph;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.stackgraph.event.GraphEvent;

public class GraphTestBase {

	Graph graph = new Graph();

	public void verifyStates(Component component, State own, State derived,
			Stream<NamedState> checked) {
		assertEquals(own, component.own());
		assertEquals(derived, component.derived());
		checked.forEach(s -> assertEquals(s.value(),
				component.getCheckStated(s.name()).get().value()));
	}

	public void applyTraceAndVerify(GraphEvent... events) {
		Stream.of(events).forEach(e -> verifyTrace(graph.applyTrace(e), e));
	}

	public void verifyTrace(List<StateTrace> traces, GraphEvent... events) {
		int i = 0;
		for (StateTrace trace : traces) {
			Set<? extends GraphEvent> e = trace.getNext();
			for (GraphEvent ge : e) {
				if (i < events.length) {
					assertEquals(events[i++], ge);
				} else {
					fail("not enough expected events");
				}
			}
		}
		if (i != events.length) {
			fail("not enough actual events");
		}

	}

	public void applyTraceAndVerifyImpact(GraphEvent event,
			Stream<Stream<GraphEvent>> impact) {
		verifyTraceImpact(graph.applyTrace(event), impact);

	}

	public void verifyTraceImpact(List<StateTrace> traces,
			Stream<Stream<GraphEvent>> impact) {
		List<Stream<GraphEvent>> list = impact.collect(toList());
		IntStream.range(0, Math.max(traces.size(), list.size())).forEach(i -> {
			Set<? extends GraphEvent> e = traces.get(i).getNext();
			if (i < list.size()) {
				Set<GraphEvent> ig = list.get(i).collect(toSet());
				assertEquals(ig, e);
			} else {
				fail("Expected set = " + e);
			}
		});

	}

}
