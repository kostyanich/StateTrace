package org.stackgraph.event;

import static org.stackgraph.graph.StateTrace.traceOf;
import static org.stackgraph.graph.Impact.*;
import static java.util.stream.Stream.*;

import java.util.Map;

import org.stackgraph.graph.Component;
import org.stackgraph.graph.Impact;


public class ComponentAdded implements GraphEvent {
	private static final long serialVersionUID = -2832443954648271035L;
	private final String target;
	public ComponentAdded(String target) {
		this.target = target;
	}
	public String getTarget() {
		return target;
	}
	@Override
	public Impact applyWithTrace(Map<String, Component> components) {
		apply(components);
		return single(traceOf(of(this)));
	}
	@Override
	public void apply(Map<String, Component> components) {
		components.put(target, new Component(target));
	}
	@Override
	public void reverse(Map<String, Component> components) {
		new ComponentRemoved(target).apply(components);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		ComponentAdded other = (ComponentAdded) obj;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "ComponentAdded [target=" + target + "]";
	}
	
}
