package org.stackgraph.store;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

import org.stackgraph.graph.Graph;
import org.stackgraph.graph.StateTrace;
import org.stackgraph.graph.TriFunction;

public class InMemoryStore implements JournalStore {

	private TreeMap<Integer, PersistedGraph> store = new TreeMap<Integer, PersistedGraph>();

	public InMemoryStore() {
	}
	
	@Override
	public void add(int position, Graph graph) {
		serialize(graph, emptyList()).ifPresent(
				bytes -> store.put(position, new PersistedGraph(bytes)));
	}

	@Override
	public void add(int position, Graph graph, List<StateTrace> stateTrace) {
		serialize(graph, stateTrace).ifPresent(
				bytes -> store.put(position, new PersistedGraph(bytes)));
	}

	
	@Override
	public <R> Optional<R> lookup(int position, TriFunction<R> trace) {
		return ofNullable(store.ceilingEntry(position))
				.filter(e -> position <= e.getKey())
				.flatMap(e-> deserialize(e.getKey(), e.getValue().getSerialized(), trace));
	}

	private Optional<byte[]> serialize(Graph graph, List<StateTrace> stateTrace) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
			oos.writeObject(graph);
			oos.writeObject(stateTrace);
			oos.flush();
			return Optional.of(bos.toByteArray());
		} catch (IOException e) {
			return Optional.empty();
		}
	}

	@SuppressWarnings("unchecked")
	private <R> Optional<R> deserialize(int position, byte[] buf, TriFunction< R> trace) {
		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buf))) {
			Graph graph = (Graph) ois.readObject();
			List<StateTrace> stateTrace = (List<StateTrace>) ois.readObject();
			return trace.apply(position, graph, stateTrace);
		} catch (ClassNotFoundException | IOException e) {
			return Optional.empty();
		}
	}

}
