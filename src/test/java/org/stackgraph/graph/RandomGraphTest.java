package org.stackgraph.graph;

import static java.util.stream.IntStream.range;

import org.junit.Test;
import org.stackgraph.graphgenerator.RandomGraphGenerator;

public class RandomGraphTest extends GraphTestBase {

	private static final int VERTEX = 100;
	private static final int COMPONENTS = 50;
	private static final int STATES = 5;

	private int numberOfGraphs = 20;
	private int numberOfEvents = 200;

	@Test
	public void traceTest() {
		range(0, numberOfGraphs).forEach(
				i -> {
					RandomGraphGenerator generator = new RandomGraphGenerator(
							VERTEX, COMPONENTS, STATES);
					graph = generator.generate();

					range(0, numberOfEvents).forEach(j -> {
						graph.applyTrace(generator.randomEvent());
						new VerifyProperties(graph).verify();
					});

					System.out.println("graph #" + i + " verified.");
				});
	}

}
