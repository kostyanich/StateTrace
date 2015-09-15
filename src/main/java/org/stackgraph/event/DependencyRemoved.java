package org.stackgraph.event;

import static java.util.Collections.singleton;
import static java.util.stream.Stream.of;
import static org.stackgraph.graph.StateTrace.traceOf;

import java.util.Map;

import org.stackgraph.graph.Component;
import org.stackgraph.graph.Impact;


public class DependencyRemoved implements GraphEvent {
	private static final long serialVersionUID = 5511670472667680342L;
	private final String from;
	private final String to;
	
	public DependencyRemoved(String from, String to) {
		if (from.equals(to)){
			throw new IllegalArgumentException("Cannot depend on itself");
		}
		this.from = from;
		this.to = to;
	}

	@Override
	public Impact applyWithTrace(Map<String, Component> components) {
		apply(components);
		Component toComponent = components.get(to);
		return new Impact(traceOf(of(this)), singleton(toComponent)); 
	}

	@Override
	public void apply(Map<String, Component> components) {
		Component fromComponent = components.get(from);
		Component toComponent = components.get(to);		
		toComponent.unlinkDependsOn(fromComponent);
	}

	@Override
	public void reverse(Map<String, Component> components) {
		new DependencyAdded(from, to).apply(components);
	}
	
	public String getFrom() {
		return from;
	}
	
	public String getTo() {
		return to;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		DependencyRemoved other = (DependencyRemoved) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DependencyRemoved [from=" + from + ", to=" + to + "]";
	}
	
	
}
