package org.stackgraph.graph;

import java.io.Serializable;

public class NamedState implements Serializable {
	public static String OWN = "own";
	public static String DERIVED = "derived";

	private static final long serialVersionUID = -3235817794424805989L;
	private final String name;
	private final State value;
	private final State oldValue;

	public NamedState(String name) {
		this(name, State.NO_DATA);
	}

	public NamedState(String name, State value) {
		this(name, State.NO_DATA, value);
	}

	public NamedState(String name, State oldValue, State value) {
		this.name = name;
		this.value = value;
		this.oldValue = oldValue;
	}

	public NamedState newState(State state) {
		return new NamedState(name, this.value, state);
	}

	public State value() {
		return value;
	}

	public String name() {
		return name;
	}

	public NamedState reverse() {
		return new NamedState(name, value, oldValue);
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
		NamedState other = (NamedState) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NamedState [name=" + name + ", value=" + value + "]";
	}

}
