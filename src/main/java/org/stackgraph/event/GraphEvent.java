package org.stackgraph.event;

import java.io.Serializable;
import java.util.Map;

import org.stackgraph.graph.Component;
import org.stackgraph.graph.Impact;

public interface GraphEvent extends Serializable {
	
	Impact applyWithTrace(Map<String, Component> components);
	
	void apply(Map<String, Component> components);
	
	void reverse(Map<String, Component> components);
	
}

