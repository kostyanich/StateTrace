package org.stackgraph.graph;

import java.util.Comparator;

public enum State {
	NO_DATA(0), CLEAR(1), WARNING(2), ALERT(3);

	private int priority;

	State(int priority) {
		this.priority = priority;
	}

	public static boolean warnAndHigh(State own) {
		return own == WARNING || own == ALERT;
	}

	public int priority() {
		return priority;
	}

	public static Comparator<State> byPriorityDesc() {
		return new Comparator<State>() {
			public int compare(State o1, State o2) {
				return o2.priority - o1.priority;
			}

		};
	}

}
