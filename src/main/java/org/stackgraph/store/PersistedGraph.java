package org.stackgraph.store;

public class PersistedGraph {

	private final byte[] serialized;

	public PersistedGraph(byte[] serialized) {
		this.serialized = serialized;
	}

	public byte[] getSerialized() {
		return serialized;
	}

}
