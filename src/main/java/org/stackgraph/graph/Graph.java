package org.stackgraph.graph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.stackgraph.event.GraphEvent;


public class Graph implements Serializable {
	private static final long serialVersionUID = -8521182332939778317L;
	
	private Map<String, Component> components = new HashMap<String, Component>();


	public void apply(GraphEvent graphEvent) {
		graphEvent.apply(components);
	}

	public void reverse(GraphEvent graphEvent) {
		graphEvent.reverse(components);
	}

	public List<StateTrace> applyTrace(GraphEvent graphEvent) {
		Impact impact = graphEvent.applyWithTrace(components);
		return impact.findTraces();
	}

    public Stream<Component> stream() {
        return components.values().stream();        
    }

    public Component get(String name) {
    	return components.get(name);
    }

	@Override
	public String toString() {
		return "Graph [components=" + components + "]";
	}
    
    

}
