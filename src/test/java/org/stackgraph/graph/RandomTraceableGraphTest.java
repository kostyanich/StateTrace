package org.stackgraph.graph;

import static java.util.stream.IntStream.range;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.stackgraph.graphgenerator.RandomGraphGenerator;
import org.stackgraph.store.InMemoryStore;
import org.stackgraph.store.Journal;

public class RandomTraceableGraphTest {

	private static final int VERTEX = 100;
	private static final int COMPONENTS = 50;
	private static final int STATES = 5;

	private int numberOfGraphs = 20;
	private int numberOfEvents = 200;
	private int journalBlockSize = 10;

	@Test
	public void testRandomGraphTrace() {
		range(0, numberOfGraphs).forEach(
				i -> {
					RandomGraphGenerator generator = new RandomGraphGenerator(
							VERTEX, COMPONENTS, STATES);
					Graph graph = generator.generate();

					Journal journal = new Journal(new InMemoryStore(),
							journalBlockSize);

					range(0, numberOfEvents).forEach(
							j -> {
								List<StateTrace> traces = graph
										.applyTrace(generator.randomEvent());
								journal.add(graph, traces);
								new VerifyProperties(graph).verify();
							});
					journal.flush(graph);

					forwardTrace(journal);
					backwardTrace(journal);

					System.out.println("graph #" + i + " verified.");
				});

	}

	private void forwardTrace(Journal journal) {
		Optional<TraceableGraph> g = TraceableGraph.empty(journal).moveAt(0);
		for (int i = 0; i < journal.maxPosition(); i++) {
			g = g.flatMap(TraceableGraph::forward);
			assertTrue("Position " + i + " is not available", g.isPresent());
			g.ifPresent(tg -> {
				new VerifyProperties(tg.getGraph().get()).verify();
			});
		}
		g = g.flatMap(TraceableGraph::forward);
		assertFalse(g.isPresent());
	}

	private void backwardTrace(Journal journal) {
		Optional<TraceableGraph> g = TraceableGraph.empty(journal).moveAt(
				journal.maxPosition());

		for (int i = journal.maxPosition() - 1; i >= 0; i--) {
			g = g.flatMap(TraceableGraph::back);
			assertTrue("Position " + i + " is not available", g.isPresent());
			g.ifPresent(tg -> {
				new VerifyProperties(tg.getGraph().get()).verify();
			});

		}
		g = g.flatMap(TraceableGraph::back);
		assertFalse(g.isPresent());
	}

}
