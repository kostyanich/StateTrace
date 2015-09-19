package org.stackgraph.event;

import static org.stackgraph.graph.StateTrace.traceOf;
import static java.util.stream.Stream.*;

import java.util.Map;

import org.stackgraph.graph.Component;
import org.stackgraph.graph.Impact;
import org.stackgraph.graph.NamedState;

public class StateChanged implements GraphEvent {
	private static final long serialVersionUID = -923090348250912218L;
	private final String target;
	private final NamedState newState;

	public StateChanged(String target, NamedState newState) {
		this.target = target;
		this.newState = newState;
	}

	@Override
	public Impact applyWithTrace(Map<String, Component> components) {
		apply(components);
		Component component = components.get(target);
		return new Impact(traceOf(of(this), component.refreshedStates()),
				component.outbound());
	}

	@Override
	public void apply(Map<String, Component> components) {
		Component component = components.get(target);
		component.stateChanged(newState);
	}

	@Override
	public void reverse(Map<String, Component> components) {
		new StateChanged(target, newState.reverse()).apply(components);
	}

	public String getTarget() {
		return target;
	}

	public NamedState getNewState() {
		return newState;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((newState == null) ? 0 : newState.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
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
		StateChanged other = (StateChanged) obj;
		if (newState == null) {
			if (other.newState != null)
				return false;
		} else if (!newState.equals(other.newState))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StateChanged [target=" + target + ", newState=" + newState
				+ "]";
	}

}
