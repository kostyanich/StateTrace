package org.stackgraph.graphgenerator;

import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.stackgraph.event.ComponentAdded;
import org.stackgraph.event.ComponentRemoved;
import org.stackgraph.event.DependencyAdded;
import org.stackgraph.event.DependencyRemoved;
import org.stackgraph.event.GraphEvent;
import org.stackgraph.event.StateChanged;
import org.stackgraph.graph.Component;
import org.stackgraph.graph.Graph;
import org.stackgraph.graph.NamedState;
import org.stackgraph.graph.State;

public class RandomGraphGenerator {
	
	private static final List<State> allStateValues = of(State.NO_DATA, State.ALERT, State.CLEAR, State.WARNING)
												 		.collect(toList());

	private final int vertexes;
	private final int edgeConnectivity;
	private final int numberOfStates;
	private final double probabilityOfStateChange = 0.5;
	private int seq;
	private Graph graph = new Graph();
	private Random random = new Random();
	
	public RandomGraphGenerator(int vertexes, int edgeConnectivity, int numberOfStates) {
		this.vertexes = vertexes;
		this.edgeConnectivity = edgeConnectivity;
		this.numberOfStates = numberOfStates;
		this.seq = vertexes;
	}
	
	public Graph generate() {
		
		IntStream.range(0, vertexes).forEach(i -> graph.applyTrace(new ComponentAdded(valueOf(i))));
		IntStream.range(0, vertexes).forEach(from -> {
			random.ints(0, vertexes)
				  .filter(to -> to != from)
				  .limit(edgeConnectivity)
				  .forEach(t -> graph.applyTrace(new DependencyAdded(valueOf(from), valueOf(t))));
		});

		IntStream.range(0, vertexes).forEach(from -> {
			random.ints(0, numberOfStates)
				  .limit(numberOfStates)
				  .forEach(s -> graph.applyTrace(new StateChanged(valueOf(from),
						  					new NamedState(valueOf(s), State.NO_DATA))));
		});
		return graph;
	}
	
	public GraphEvent randomEvent() {
		Optional<GraphEvent> event = Optional.empty();
		int attemts = 0;
		do {
			event = generateEvent(random.nextDouble());
		} while(!event.isPresent() || attemts++ > 10);
//		System.out.println(event.get());
		return event.get();
	}
	
	private Optional<GraphEvent> generateEvent(double p) {
		if (p < probabilityOfStateChange) {
			return stateChanged();
		} else {
			p = random.nextDouble();
			if (p <= 0.25) {
				return componentAdded();
			} else if (p <= 0.5) {
				return componentRemoved();
			} else if (p <= 0.75) {
				return dependencyAdded();
			} else {
				return dependencyRemoved();
			}		
		}
	}
	
	private Optional<GraphEvent> stateChanged() {
		List<Component> componentWithStates = graph.stream().filter(c -> c.checkedStates().count() > 0)
															.collect(toList());
		if (componentWithStates.size() > 0 ) {
			Component randomComponent = randomComponent(componentWithStates.stream());
			NamedState randomState = randomState(randomComponent);			
			State randomNewStateValue = randomNewState(randomState);
			
			return Optional.of(new StateChanged(randomComponent.name(), 
										new NamedState(randomState.name(), randomState.value(), randomNewStateValue)));
		} else {
			return Optional.empty();
		}
	}
	private State randomNewState(NamedState randomState) {
		List<State> filtered = allStateValues.stream().filter(s -> s != randomState.value())
													  .collect(toList());
		State randomNewValue = filtered.get(random.nextInt(filtered.size()));
		return randomNewValue;
	}
	private NamedState randomState(Component randomComponent) {
		List<NamedState> states = randomComponent.checkedNamedStates()
												 .collect(toList());
		NamedState randomState = states.get(random.nextInt(states.size()));
		return randomState;
	}

	private Optional<GraphEvent> componentAdded() {
		return Optional.of(new ComponentAdded(valueOf(seq++)));
	}

	private Optional<GraphEvent> componentRemoved() {
		String next = getNotRemovedComponent();
		return Optional.of(new ComponentRemoved(String.valueOf(next)));
	}
	
	private String getNotRemovedComponent() {
		return randomComponent(graph.stream()).name();
	}
	
	private Component randomComponent(Stream<Component> stream) {
		List<Component> components = stream.collect(toList());
		return components.get(random.nextInt(components.size()));
	}

	private Optional<GraphEvent> dependencyRemoved() {
		List<Object> removed = Stream.concat(
								graph.stream().flatMap(to -> 
										to.inbound().stream().map(
												from -> new DependencyRemoved(from.name(), to.name()))),
								graph.stream().flatMap(from -> 
										from.outbound().stream().map(
												to -> new DependencyRemoved(from.name(), to.name()))))
											.distinct().collect(toList());
		if (removed.size() > 0) {
			return Optional.of((GraphEvent)removed.get(random.nextInt(removed.size())));
		} else {
			return Optional.empty();
		}
	}
	
	private Optional<GraphEvent> dependencyAdded() {
		Component comp = randomComponent(graph.stream());
		List<Component> chosen = graph.stream().filter(c -> {
			return !c.inbound().contains(comp) 
					&& !c.outbound().contains(comp)
					&& !c.equals(comp);
		}).collect(toList());
		if (chosen.size() > 0) {
			Component randomComponent = chosen.get(random.nextInt(chosen.size()));
			return Optional.of(new DependencyAdded(comp.name(), randomComponent.name()));
		} else {
			return Optional.empty();
		}
	}
	
}
