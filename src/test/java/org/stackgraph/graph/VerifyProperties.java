package org.stackgraph.graph;

import static java.util.stream.Stream.concat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.stream.Stream;

public class VerifyProperties {

	private Graph graph;
	
	public VerifyProperties(Graph graph) {
		this.graph = graph;
	}

	public void verify() {
		verifyNoSelfConnection(graph);
		verifyNoOrpahs(graph);
		verifyOwnBasedOnCheckStates(graph);
		verifyDerivedState(graph);
	}


	private void verifyNoSelfConnection(Graph graph) {
		graph.stream().forEach(c -> {
			assertTrue("Self connection " + c, !c.inbound().contains(c) && !c.outbound().contains(c));
		});
	}

	private void verifyOwnBasedOnCheckStates(Graph graph) {
		graph.stream().forEach(
				c -> {
					assertEquals("Own state is not consistent " + c,
							c.own(),
							c.checkedStates().max(Comparator.comparingInt(State::priority))
											 .orElse(State.NO_DATA));
				});
	}

	private void verifyDerivedState(Graph graph) {
		graph.stream().forEach(
				c -> {
					assertEquals("Derived state is not consistent." + c,
							c.derived(),
							concat(
									Stream.of(c.own()),
									c.inbound().stream().map(Component::derived)
														.filter(State::warnAndHigh))
														.max(Comparator.comparingInt(State::priority))
														.orElse(State.NO_DATA));
				});
	}
	
	private void verifyNoOrpahs(Graph graph) {
		graph.stream().forEach(
				c -> {
					concat(c.inbound().stream(), c.outbound().stream())
						  .forEach(i -> {
							  assertNotNull("Orphan component " + i, graph.get(i.name()));
						  });							  
				});
	}


}
