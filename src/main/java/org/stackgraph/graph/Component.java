package org.stackgraph.graph;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.stackgraph.graph.NamedState.DERIVED;
import static org.stackgraph.graph.NamedState.OWN;
import static org.stackgraph.graph.State.NO_DATA;
import static org.stackgraph.graph.State.byPriorityDesc;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.stackgraph.event.StateChanged;

public class Component implements Serializable {
	private static final long serialVersionUID = -2049860240876588312L;
	
	private final String name;
	private final Set<Component> outbound = new HashSet<Component>();
	private final Set<Component> inbound = new HashSet<Component>();
	private final Set<NamedState> checkStates = new HashSet<>();
	private NamedState derived = new NamedState(DERIVED, NO_DATA);
	private NamedState own = new NamedState(OWN, NO_DATA);

	public Component(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}

	public void dependsOn(Component from) {
		linkInbound(from);
		from.linkOutbound(this);
	}

	public void unlinkDependsOn(Component from) {
		unlinkInbound(from);
		from.unlinkOutbound(this);
	}


	public void linkInbound(Component component) {
		inbound.add(component);
	}

	public void unlinkInbound(Component from) {
		inbound.remove(from);
	}
	
	public void linkOutbound(Component component) {
		outbound.add(component);
	}

	public void unlinkOutbound(Component to) {
		outbound.remove(to);
	}

	public void unlink() {
		unlinkInbound();
		unlinkOutbound();
	}
	
	public Set<Component> unlinkInbound() {
		inbound.stream().forEach(from -> from.unlinkOutbound(this));
		return inbound;
	}

	public Set<Component> unlinkOutbound() {
		outbound.stream().forEach(to -> to.unlinkInbound(this));		
		return outbound;
	}

	public void stateChanged(NamedState state) {
		if (state.equals(own)) {
			own = state;
		} else if (state.equals(derived)) {
			derived = state;
		} else {
			checkStates.add(state);
		}
	}

	public Stream<StateChanged> refreshedStates() {
		Optional<StateChanged> ownState = refreshOwn();
		Optional<StateChanged> derivedState = refreshDerived();
		return of(ownState, derivedState).filter(Optional::isPresent)
										 .map(Optional::get);
	}

	public Optional<StateChanged> refreshDerived() {
		State highestOf = highestOf(concat(of(own.value()), derivedStates())
										     .filter(State::warnAndHigh));
		if (highestOf != derived.value()) {
			derived = derived.newState(highestOf);
			return Optional.of(new StateChanged(this.name, derived));
		} else {
			return Optional.empty();
		}
	}

	private Optional<StateChanged> refreshOwn() {
		State highestOf = highestOf(checkedStates());
		if (highestOf != own.value()) {
			own = own.newState(highestOf);
			return Optional.of(new StateChanged(this.name, own));
		} else {
			return Optional.empty();
		}
	}

	private State highestOf(Stream<State> states) {
		return concat(of(NO_DATA), states).sorted(byPriorityDesc()).findFirst().get();
	}
	
	public Stream<State> checkedStates() {
		return checkStates.stream().map(NamedState::value);
	}
	
	public Stream<State> derivedStates() {
		return inbound.stream().map(Component::derived);
	}

	public Stream<NamedState> checkedNamedStates() {
		return checkStates.stream();
	}

	public State derived() {
		return derived.value();
	}

	public State own() {
		return own.value();
	}

	public Optional<NamedState> getCheckStated(String name) {
		return checkStates.stream()
						  .filter(c -> c.name().equals(name))
						  .findFirst();
	}

	public Set<Component> outbound() {
		return new HashSet<Component>(outbound);
	}

	public Set<Component> inbound() {
		return new HashSet<Component>(inbound);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Component other = (Component) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String inboundDesc = inbound.stream().map(c->c.name()).collect(Collectors.joining(", "));
		String outboundDesc = outbound.stream().map(c->c.name()).collect(Collectors.joining(", "));
		return "Component [name=" + name + ", checkStates=" + checkStates
				+ ", derived=" + derived + ", own=" + own 
				+ ", in=" + inboundDesc + ", out=" + outboundDesc + "]";
	}





}
