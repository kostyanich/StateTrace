package org.stackgraph.graph;

import static java.util.stream.Stream.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.stream.Stream;

import org.junit.Test;
import org.stackgraph.event.ComponentAdded;
import org.stackgraph.event.ComponentRemoved;
import org.stackgraph.event.DependencyAdded;
import org.stackgraph.event.DependencyRemoved;
import org.stackgraph.event.StateChanged;

public class GraphTest extends GraphTestBase {

	static final String STATE1 = "state1";
	static final String COMP1 = "comp1";
	static final String COMP2 = "comp2";
	static final String COMP3 = "comp3";

	ComponentAdded addedComp1 = new ComponentAdded(COMP1);
	ComponentAdded addedComp2 = new ComponentAdded(COMP2);
	ComponentAdded addedComp3 = new ComponentAdded(COMP3);

	ComponentRemoved removedComp1 = new ComponentRemoved(COMP1);
	ComponentRemoved removedComp2 = new ComponentRemoved(COMP2);
	ComponentRemoved removedComp3 = new ComponentRemoved(COMP3);

	DependencyAdded dep1to2 = new DependencyAdded(COMP1, COMP2);
	DependencyAdded dep1to3 = new DependencyAdded(COMP1, COMP3);
	DependencyAdded dep2to3 = new DependencyAdded(COMP2, COMP3);
	DependencyAdded dep3to1 = new DependencyAdded(COMP3, COMP1);

	DependencyRemoved dep1to2removed = new DependencyRemoved(COMP1, COMP2);
	DependencyRemoved dep2to3removed = new DependencyRemoved(COMP2, COMP3);
	DependencyRemoved dep3to1removed = new DependencyRemoved(COMP3, COMP1);

	NamedState state1_alerted = new NamedState(STATE1, State.ALERT);
	NamedState state1_warn = new NamedState(STATE1, State.WARNING);
	NamedState state1_clear = new NamedState(STATE1, State.CLEAR);
	NamedState state1_nodata = new NamedState(STATE1, State.NO_DATA);
	NamedState derived_alerted = new NamedState(NamedState.DERIVED, State.ALERT);
	NamedState derived_warn = new NamedState(NamedState.DERIVED, State.WARNING);
	NamedState derived_clear = new NamedState(NamedState.DERIVED, State.CLEAR);
	NamedState derived_nodata = new NamedState(NamedState.DERIVED,
			State.NO_DATA);
	NamedState own_alerted = new NamedState(NamedState.OWN, State.ALERT);
	NamedState own_clear = new NamedState(NamedState.OWN, State.CLEAR);
	NamedState own_warn = new NamedState(NamedState.OWN, State.WARNING);

	StateChanged comp1_state1_alerted = new StateChanged(COMP1, state1_alerted);
	StateChanged comp1_state1_warn = new StateChanged(COMP1, state1_warn);
	StateChanged comp1_state1_clear = new StateChanged(COMP1, state1_clear);
	StateChanged comp1_state1_nodata = new StateChanged(COMP1, state1_nodata);

	StateChanged comp2_state1_alerted = new StateChanged(COMP2, state1_alerted);

	StateChanged comp1_own_alerted = new StateChanged(COMP1, new NamedState(
			NamedState.OWN, State.ALERT));
	StateChanged comp1_own_clear = new StateChanged(COMP1, own_clear);
	StateChanged comp1_own_warn = new StateChanged(COMP1, own_warn);

	StateChanged comp1_derived_alerted = new StateChanged(COMP1,
			derived_alerted);
	StateChanged comp1_derived_clear = new StateChanged(COMP1, derived_clear);
	StateChanged comp1_derived_warn = new StateChanged(COMP1, derived_warn);

	StateChanged comp2_own_alerted = new StateChanged(COMP2, new NamedState(
			NamedState.OWN, State.ALERT));

	StateChanged comp2_derived_warn = new StateChanged(COMP2, derived_warn);
	StateChanged comp2_derived_alerted = new StateChanged(COMP2,
			derived_alerted);

	StateChanged comp3_derived_alerted = new StateChanged(COMP3,
			derived_alerted);
	StateChanged comp3_derived_warn = new StateChanged(COMP3, derived_warn);
	StateChanged comp3_derived_nodata = new StateChanged(COMP3, derived_nodata);

	@Test
	public void testAddComponent() {
		applyTraceAndVerify(addedComp1);

		assertEquals(
				1,
				graph.stream()
						.filter(c -> of(COMP1).anyMatch(
								name -> c.name().equals(name))).count());

	}

	@Test
	public void testRemoveComponent() {
		applyTraceAndVerify(addedComp1, removedComp1);

		assertEquals(
				0,
				graph.stream()
						.filter(c -> of(COMP1).anyMatch(
								name -> c.name().equals(name))).count());

	}

	@Test
	public void testAdd2Components() {
		applyTraceAndVerify(addedComp1, addedComp2);

		assertEquals(
				2,
				graph.stream()
						.filter(c -> of(COMP1, COMP2).anyMatch(
								name -> c.name().equals(name))).count());
	}

	@Test
	public void testStateChanged() {
		applyTraceAndVerify(addedComp1);

		Component comp1 = graph.get(COMP1);
		verifyStates(graph.get(COMP1), State.NO_DATA, State.NO_DATA,
				Stream.empty());

		assertFalse(comp1.getCheckStated(STATE1).isPresent());

		applyTraceAndVerifyImpact(
				comp1_state1_alerted,
				Stream.of(Stream.of(comp1_state1_alerted),
						Stream.of(comp1_own_alerted, comp1_derived_alerted)));

		verifyStates(graph.get(COMP1), State.ALERT, State.ALERT,
				Stream.of(state1_alerted));
	}

	@Test
	public void testOwnStateHighesOfAllCheckedNodata() {
		applyTraceAndVerify(addedComp1);

		applyTraceAndVerifyImpact(comp1_state1_nodata,
				Stream.of(Stream.of(comp1_state1_nodata)));

		verifyStates(graph.get(COMP1), State.NO_DATA, State.NO_DATA,
				Stream.of(state1_nodata));
	}

	@Test
	public void testOwnStateHighesOfAllCheckedClear() {
		applyTraceAndVerify(addedComp1);

		applyTraceAndVerifyImpact(
				comp1_state1_clear,
				Stream.of(Stream.of(comp1_state1_clear),
						Stream.of(comp1_own_clear)));

		verifyStates(graph.get(COMP1), State.CLEAR, State.NO_DATA,
				Stream.of(state1_clear));
	}

	@Test
	public void testOwnStateHighesOfAllCheckedWarn() {
		applyTraceAndVerify(addedComp1);

		applyTraceAndVerifyImpact(
				comp1_state1_warn,
				Stream.of(Stream.of(comp1_state1_warn),
						Stream.of(comp1_own_warn, comp1_derived_warn)));

		verifyStates(graph.get(COMP1), State.WARNING, State.WARNING,
				Stream.of(state1_warn));
	}

	@Test
	public void testOwnStateChangedNoTrace() {
		applyTraceAndVerify(addedComp1);

		Component comp1 = graph.get(COMP1);

		assertEquals(State.NO_DATA, comp1.own());

		graph.apply(new StateChanged(COMP1, new NamedState(NamedState.OWN,
				State.ALERT)));

		assertEquals(State.ALERT, comp1.own());
	}

	@Test
	public void testAdd2ComponentsAndDependency() {
		applyTraceAndVerify(addedComp1, addedComp2, dep1to2);

		Component comp1 = graph.get(COMP1);
		assertEquals(COMP2, comp1.outbound().stream().findFirst().get().name());

		Component comp2 = graph.get(COMP2);

		assertEquals(COMP1, comp2.inbound().stream().findFirst().get().name());
	}

	@Test
	public void testDerivedStateChanged() {
		applyTraceAndVerify(addedComp1, addedComp2, dep1to2);

		verifyStates(graph.get(COMP1), State.NO_DATA, State.NO_DATA,
				Stream.empty());

		applyTraceAndVerifyImpact(comp1_state1_alerted, Stream.of(
				Stream.of(comp1_state1_alerted),
				Stream.of(comp1_own_alerted, comp1_derived_alerted),
				Stream.of(comp2_derived_alerted)));

		verifyStates(graph.get(COMP1), State.ALERT, State.ALERT,
				Stream.of(state1_alerted));
		verifyStates(graph.get(COMP2), State.NO_DATA, State.ALERT,
				Stream.empty());
	}

	@Test
	public void testDerivedStateNotChangedOnClear() {
		applyTraceAndVerify(addedComp1, addedComp2, dep1to2);

		verifyStates(graph.get(COMP1), State.NO_DATA, State.NO_DATA,
				Stream.empty());

		applyTraceAndVerifyImpact(
				comp1_state1_clear,
				Stream.of(Stream.of(comp1_state1_clear),
						Stream.of(comp1_own_clear)));

		verifyStates(graph.get(COMP1), State.CLEAR, State.NO_DATA,
				Stream.of(state1_clear));
		verifyStates(graph.get(COMP2), State.NO_DATA, State.NO_DATA,
				Stream.empty());
	}

	@Test
	public void testDerivedStatePropagated() {
		applyTraceAndVerify(addedComp1, addedComp2, addedComp3, dep1to2,
				dep2to3);

		verifyStates(graph.get(COMP1), State.NO_DATA, State.NO_DATA,
				Stream.empty());
		verifyStates(graph.get(COMP2), State.NO_DATA, State.NO_DATA,
				Stream.empty());
		verifyStates(graph.get(COMP3), State.NO_DATA, State.NO_DATA,
				Stream.empty());

		applyTraceAndVerifyImpact(comp1_state1_alerted, Stream.of(
				Stream.of(comp1_state1_alerted),
				Stream.of(comp1_own_alerted, comp1_derived_alerted),
				Stream.of(comp2_derived_alerted),
				Stream.of(comp3_derived_alerted)));

		verifyStates(graph.get(COMP1), State.ALERT, State.ALERT,
				Stream.of(state1_alerted));
		verifyStates(graph.get(COMP2), State.NO_DATA, State.ALERT,
				Stream.empty());
		verifyStates(graph.get(COMP3), State.NO_DATA, State.ALERT,
				Stream.empty());
	}

	@Test
	public void testDerivedStateCyclic() {
		applyTraceAndVerify(addedComp1, addedComp2, addedComp3, dep1to2,
				dep2to3, dep3to1);

		verifyStates(graph.get(COMP1), State.NO_DATA, State.NO_DATA,
				Stream.empty());
		verifyStates(graph.get(COMP2), State.NO_DATA, State.NO_DATA,
				Stream.empty());
		verifyStates(graph.get(COMP3), State.NO_DATA, State.NO_DATA,
				Stream.empty());

		applyTraceAndVerifyImpact(comp1_state1_alerted, Stream.of(
				Stream.of(comp1_state1_alerted),
				Stream.of(comp1_own_alerted, comp1_derived_alerted),
				Stream.of(comp2_derived_alerted),
				Stream.of(comp3_derived_alerted)));

		verifyStates(graph.get(COMP1), State.ALERT, State.ALERT,
				Stream.of(state1_alerted));
		verifyStates(graph.get(COMP2), State.NO_DATA, State.ALERT,
				Stream.empty());
		verifyStates(graph.get(COMP3), State.NO_DATA, State.ALERT,
				Stream.empty());
	}

	@Test
	public void testDerivedIsHighestOfAllStates() {
		applyTraceAndVerify(addedComp1, addedComp2, addedComp3, dep1to3,
				dep2to3);

		verifyStates(graph.get(COMP1), State.NO_DATA, State.NO_DATA,
				Stream.empty());
		verifyStates(graph.get(COMP2), State.NO_DATA, State.NO_DATA,
				Stream.empty());
		verifyStates(graph.get(COMP3), State.NO_DATA, State.NO_DATA,
				Stream.empty());

		applyTraceAndVerifyImpact(
				comp1_state1_warn,
				Stream.of(Stream.of(comp1_state1_warn),
						Stream.of(comp1_own_warn, comp1_derived_warn),
						Stream.of(comp3_derived_warn)));

		applyTraceAndVerifyImpact(comp2_state1_alerted, Stream.of(
				Stream.of(comp2_state1_alerted),
				Stream.of(comp2_own_alerted, comp2_derived_alerted),
				Stream.of(comp3_derived_alerted)));

		verifyStates(graph.get(COMP1), State.WARNING, State.WARNING,
				Stream.of(state1_warn));
		verifyStates(graph.get(COMP2), State.ALERT, State.ALERT,
				Stream.of(state1_alerted));
		verifyStates(graph.get(COMP3), State.NO_DATA, State.ALERT,
				Stream.empty());
	}

	@Test
	public void testRemoveConnectedComponent() {
		testDerivedStatePropagated();

		applyTraceAndVerifyImpact(
				removedComp2,
				Stream.of(Stream.of(dep1to2removed, dep2to3removed),
						Stream.of(removedComp2),
						Stream.of(comp3_derived_nodata)));
	}

	@Test
	public void testDerivedStateCyclicTricky() {
		applyTraceAndVerify(addedComp1, addedComp2, addedComp3, dep1to2,
				dep2to3, dep3to1);

		verifyStates(graph.get(COMP1), State.NO_DATA, State.NO_DATA,
				Stream.empty());
		verifyStates(graph.get(COMP2), State.NO_DATA, State.NO_DATA,
				Stream.empty());
		verifyStates(graph.get(COMP3), State.NO_DATA, State.NO_DATA,
				Stream.empty());

		applyTraceAndVerifyImpact(comp1_state1_alerted, Stream.of(
				Stream.of(comp1_state1_alerted),
				Stream.of(comp1_own_alerted, comp1_derived_alerted),
				Stream.of(comp2_derived_alerted),
				Stream.of(comp3_derived_alerted)));

		verifyStates(graph.get(COMP1), State.ALERT, State.ALERT,
				Stream.of(state1_alerted));
		verifyStates(graph.get(COMP2), State.NO_DATA, State.ALERT,
				Stream.empty());
		verifyStates(graph.get(COMP3), State.NO_DATA, State.ALERT,
				Stream.empty());

		applyTraceAndVerifyImpact(dep3to1removed,
				Stream.of(Stream.of(dep3to1removed)));

		verifyStates(graph.get(COMP1), State.ALERT, State.ALERT,
				Stream.of(state1_alerted));
		verifyStates(graph.get(COMP2), State.NO_DATA, State.ALERT,
				Stream.empty());
		verifyStates(graph.get(COMP3), State.NO_DATA, State.ALERT,
				Stream.empty());

	}

}
