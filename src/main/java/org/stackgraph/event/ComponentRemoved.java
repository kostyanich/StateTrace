package org.stackgraph.event;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.stackgraph.graph.Impact.empty;
import static org.stackgraph.graph.StateTrace.traceOf;
import static java.util.Optional.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.stackgraph.graph.Component;
import org.stackgraph.graph.Impact;
import org.stackgraph.graph.StateTrace;



public class ComponentRemoved implements GraphEvent {
	private static final long serialVersionUID = 2017531858514444296L;
	private String target;

	public ComponentRemoved(String target) {
		this.target = target;
	}
	
	public String getTarget() {
		return target;
	}

	@Override
	public Impact applyWithTrace(Map<String, Component> components) {
		return removeComponent(components, (c) ->{
			Set<Component> in = c.inbound();
			Set<Component> out = c.outbound();
			c.unlink();
			List<StateTrace> traces = traceOf(
						    concat(
								   in.stream().map(from -> new DependencyRemoved(from.name(), target)),
								   out.stream().map(to -> new DependencyRemoved(target, to.name()))
							),
							of(new ComponentRemoved(target)));

			return new Impact(traces, out);
		});
	}

	private Impact removeComponent(Map<String, Component> components, 
			Function<Component, Impact> func) {
		return ofNullable(components.remove(target))
					   .flatMap(c-> Optional.of(func.apply(c)))
					   .orElse(Impact.empty());		
	}
	@Override
	public void apply(Map<String, Component> components) {
		removeComponent(components, (c) -> empty());
	}

	@Override
	public void reverse(Map<String, Component> components) {
		new ComponentAdded(target).apply(components);
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
		ComponentRemoved other = (ComponentRemoved) obj;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ComponentRemoved [target=" + target + "]";
	}

}
