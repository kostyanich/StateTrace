package org.stackgraph.graph;

import static java.util.stream.Stream.of;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.stackgraph.graph.StateTrace.traceOf;

import java.util.Optional;

import org.junit.Test;
import org.stackgraph.event.ComponentRemoved;
import org.stackgraph.event.DependencyRemoved;
import org.stackgraph.store.InMemoryStore;
import org.stackgraph.store.Journal;
import org.stackgraph.store.JournalStore;

public class TraceableGraphTest {
	TraceableGraph traceableGraph;
	JournalStore journalStore = new InMemoryStore();
	Journal journal = new Journal(journalStore, 1);
	
	@Test
	public void testEmpty() {
		traceableGraph = TraceableGraph.empty(journal);
		
		assertFalse(traceableGraph.forward().isPresent());
		assertFalse(traceableGraph.back().isPresent());
		assertFalse(traceableGraph.moveAt(0).isPresent());
	}
	
	@Test
	public void testOneNodeGraphTrace() {
		journal.add(new Graph(), traceOf(of(new ComponentRemoved("1"))));
		
		Optional<TraceableGraph> g = TraceableGraph.empty(journal).moveAt(0);
		
		assertTrue(g.isPresent());		
		g.ifPresent(TraceableGraph::printGraph);
		
		g = g.flatMap(TraceableGraph::forward);
		assertTrue(g.isPresent());
		g.ifPresent(TraceableGraph::printGraph);
		
		assertFalse(g.flatMap(TraceableGraph::forward).isPresent());
		g.ifPresent(TraceableGraph::printGraph);
		
		g = g.flatMap(TraceableGraph::back);
		assertTrue(g.isPresent());
		g.ifPresent(TraceableGraph::printGraph);
		
		g = g.flatMap(TraceableGraph::back);
		assertFalse(g.isPresent());
	}
	
	@Test
	public void testGraphTrace() {
		journal.add(new Graph(), traceOf(of(new ComponentRemoved("1")),
										 of(new ComponentRemoved("2"))));
		
		traceForward();
		traceBackward();

	}

	@Test
	public void testGraph2ConnectedComponents() {		
		journal.add(new Graph(), traceOf(of(new DependencyRemoved("1", "2")),
										 of(new ComponentRemoved("1")),
										 of(new ComponentRemoved("2"))));
		
		traceForward();
		traceBackward();
		
	}

	@Test
	public void testGraphWith3ConnectedComponents() {
		journal = new Journal(journalStore, 2);
		journal.add(new Graph(), traceOf(of(new DependencyRemoved("3", "2")),
				 						 of(new ComponentRemoved("3")),
				 						 of(new DependencyRemoved("1", "2")),
										 of(new ComponentRemoved("1")),
										 of(new ComponentRemoved("2"))));

		traceForward();
		traceBackward();

	}

	private void traceBackward() {
		Optional<TraceableGraph> g = TraceableGraph.empty(journal).moveAt(journal.maxPosition());
		g.ifPresent(TraceableGraph::printGraph);
		
		for (int i = journal.maxPosition() - 1; i >= 0; i--) {
			g = g.flatMap(TraceableGraph::back);
			assertTrue(g.isPresent());
			g.ifPresent(TraceableGraph::printGraph);
		}
		g = g.flatMap(TraceableGraph::back);
		assertFalse(g.isPresent());
	}

	private void traceForward() {
		Optional<TraceableGraph> g = TraceableGraph.empty(journal).moveAt(0);
		g.ifPresent(TraceableGraph::printGraph);		
		for (int i = 0; i < journal.maxPosition(); i++) {
			g = g.flatMap(TraceableGraph::forward);
			assertTrue(g.isPresent());
			g.ifPresent(TraceableGraph::printGraph);
		}
		
		g = g.flatMap(TraceableGraph::forward);
		assertFalse(g.isPresent());
	}

}
