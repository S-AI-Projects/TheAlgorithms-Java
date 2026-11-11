package com.thealgorithms.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Failure-path and empty-result regression tests for {@link HierholzerAlgorithm}.
 *
 * <p>Source preconditions ({@code HierholzerAlgorithm.java}):
 * <ul>
 * <li>lines 37-39: {@code HierholzerAlgorithm(Map)} stores {@code graph}, or {@code new HashMap<>()}
 * when {@code graph == null} (line 38)</li>
 * <li>lines 49-61: {@code hasEulerianCircuit()} returns {@code true} on empty {@code graph}
 * (lines 50-52), returns {@code false} when any vertex has odd degree (lines 54-58), and
 * otherwise delegates to {@code isCoherentlyConnected()} (line 60)</li>
 * <li>lines 73-76: {@code findEulerianCircuit()} returns {@code Collections.emptyList()} when
 * {@code !hasEulerianCircuit()} (absent-result sentinel)</li>
 * <li>lines 78-81: {@code tempGraph} copies adjacency lists so caller-owned {@code graph} is
 * never mutated</li>
 * <li>lines 94-99: when no vertex has edges ({@code startVertex == -1}), returns
 * {@code Collections.emptyList()} if {@code graph.isEmpty()} (lines 95-97) or a singleton list
 * for an isolated vertex (line 98)</li>
 * <li>lines 140-143: {@code isCoherentlyConnected()} returns {@code false} if any vertex with
 * non-empty edges was not visited from {@code startNode}</li>
 * </ul>
 */
public class HierholzerAlgorithmFailureTest {

    private static Map<Integer, LinkedList<Integer>> copyGraph(Map<Integer, LinkedList<Integer>> src) {
        Map<Integer, LinkedList<Integer>> copy = new HashMap<>();
        for (Map.Entry<Integer, LinkedList<Integer>> e : src.entrySet()) {
            copy.put(e.getKey(), new LinkedList<>(e.getValue()));
        }
        return copy;
    }

    private static void logCase(String id, String input, Object expected, Object actual) {
        System.out.println("[R45-T018] " + id + " | input=" + input
            + " | expected=" + expected + " | actual=" + actual);
    }

    /**
     * Smallest valid inputs and empty/null fallback branches (lines 38, 50-52, 95-98):
     * {@code null} and empty {@code {}} yield {@code Collections.emptyList()}; single isolated
     * vertex {@code {0=[]}} yields {@code [0]}.
     */
    @Test
    public void nullEmptyAndSingleIsolatedVertexFallbacksMatchContract() {
        HierholzerAlgorithm fromNull = new HierholzerAlgorithm(null);
        assertTrue(fromNull.hasEulerianCircuit());
        List<Integer> nullCircuit = fromNull.findEulerianCircuit();
        logCase("fallback-null-graph", "graph=null", Collections.emptyList(), nullCircuit);
        assertEquals(Collections.emptyList(), nullCircuit);

        Map<Integer, LinkedList<Integer>> emptyMap = new HashMap<>();
        HierholzerAlgorithm fromEmpty = new HierholzerAlgorithm(emptyMap);
        assertTrue(fromEmpty.hasEulerianCircuit());
        List<Integer> emptyCircuit = fromEmpty.findEulerianCircuit();
        logCase("fallback-empty-graph", "graph={}", Collections.emptyList(), emptyCircuit);
        assertEquals(Collections.emptyList(), emptyCircuit);
        assertTrue(emptyMap.isEmpty());

        Map<Integer, LinkedList<Integer>> singleVertex = new HashMap<>();
        singleVertex.put(0, new LinkedList<>());
        HierholzerAlgorithm fromIsolated = new HierholzerAlgorithm(singleVertex);
        assertTrue(fromIsolated.hasEulerianCircuit());
        List<Integer> isolatedCircuit = fromIsolated.findEulerianCircuit();
        logCase("smallest-isolated-vertex", "graph={0=[]}", List.of(0), isolatedCircuit);
        assertEquals(List.of(0), isolatedCircuit);
        assertEquals(Map.of(0, new LinkedList<>()), singleVertex);
    }

    /**
     * Smallest odd-degree failure input ({@code {0=[1], 1=[0]}}, degree 1 at line 55):
     * asserts {@code hasEulerianCircuit() == false} (line 56) and
     * {@code findEulerianCircuit() == []} (line 75), verifies caller-owned {@code graph} stays
     * intact, and verifies an ordinary valid call succeeds afterwards.
     */
    @Test
    public void oddDegreeFailureReturnsEmptyAndPreservesCallerStateBeforeValidCall() {
        Map<Integer, LinkedList<Integer>> oddGraph = new HashMap<>();
        oddGraph.put(0, new LinkedList<>(Collections.singletonList(1)));
        oddGraph.put(1, new LinkedList<>(Collections.singletonList(0)));
        Map<Integer, LinkedList<Integer>> snapshotBefore = copyGraph(oddGraph);

        HierholzerAlgorithm failedAlgo = new HierholzerAlgorithm(oddGraph);
        boolean hasCircuit = failedAlgo.hasEulerianCircuit();
        List<Integer> failedResult = failedAlgo.findEulerianCircuit();
        logCase("failure-odd-degree", "{0=[1], 1=[0]}",
            "hasCircuit=false,circuit=[]", "hasCircuit=" + hasCircuit + ",circuit=" + failedResult);

        assertFalse(hasCircuit);
        assertEquals(Collections.emptyList(), failedResult);
        assertEquals(snapshotBefore, oddGraph);

        // Ordinary valid call succeeds after the failure case (and caller-owned graph remains valid).
        Map<Integer, LinkedList<Integer>> validTriangle = new HashMap<>();
        validTriangle.put(0, new LinkedList<>(Arrays.asList(1, 2)));
        validTriangle.put(1, new LinkedList<>(Arrays.asList(0, 2)));
        validTriangle.put(2, new LinkedList<>(Arrays.asList(0, 1)));
        Map<Integer, LinkedList<Integer>> triangleSnapshot = copyGraph(validTriangle);

        HierholzerAlgorithm validAlgo = new HierholzerAlgorithm(validTriangle);
        assertTrue(validAlgo.hasEulerianCircuit());
        List<Integer> validCircuit = validAlgo.findEulerianCircuit();
        logCase("post-failure-valid-triangle", "{0=[1,2], 1=[0,2], 2=[0,1]}",
            "[0, 1, 2, 0]", validCircuit);

        assertEquals(List.of(0, 1, 2, 0), validCircuit);
        assertEquals(triangleSnapshot, validTriangle);
        // Repeated call on the same instance succeeds because tempGraph (lines 78-81) copied lists.
        assertEquals(List.of(0, 1, 2, 0), validAlgo.findEulerianCircuit());
    }

    /**
     * Disconnected even-degree failure input (two disjoint triangles; passes lines 54-58, fails
     * connectivity check at lines 141-142): asserts empty result, unmutated caller state, and a
     * subsequent successful ordinary call.
     */
    @Test
    public void disconnectedEvenDegreeFailureReturnsEmptyBeforeSubsequentValidCall() {
        Map<Integer, LinkedList<Integer>> disconnected = new HashMap<>();
        disconnected.put(0, new LinkedList<>(Arrays.asList(1, 2)));
        disconnected.put(1, new LinkedList<>(Arrays.asList(0, 2)));
        disconnected.put(2, new LinkedList<>(Arrays.asList(0, 1)));
        disconnected.put(3, new LinkedList<>(Arrays.asList(4, 5)));
        disconnected.put(4, new LinkedList<>(Arrays.asList(3, 5)));
        disconnected.put(5, new LinkedList<>(Arrays.asList(3, 4)));
        Map<Integer, LinkedList<Integer>> snapshotBefore = copyGraph(disconnected);

        HierholzerAlgorithm failedAlgo = new HierholzerAlgorithm(disconnected);
        boolean hasCircuit = failedAlgo.hasEulerianCircuit();
        List<Integer> failedResult = failedAlgo.findEulerianCircuit();
        logCase("failure-disconnected-even", "2 disjoint triangles {0,1,2} & {3,4,5}",
            "hasCircuit=false,circuit=[]", "hasCircuit=" + hasCircuit + ",circuit=" + failedResult);

        assertFalse(hasCircuit);
        assertEquals(Collections.emptyList(), failedResult);
        assertEquals(snapshotBefore, disconnected);

        // Connect the caller-owned graph into a valid single Eulerian component or run a fresh valid one.
        Map<Integer, LinkedList<Integer>> validTriangle = new HashMap<>();
        validTriangle.put(0, new LinkedList<>(Arrays.asList(1, 2)));
        validTriangle.put(1, new LinkedList<>(Arrays.asList(0, 2)));
        validTriangle.put(2, new LinkedList<>(Arrays.asList(0, 1)));
        HierholzerAlgorithm validAlgo = new HierholzerAlgorithm(validTriangle);
        List<Integer> validCircuit = validAlgo.findEulerianCircuit();
        logCase("post-disconnected-valid", "{0=[1,2], 1=[0,2], 2=[0,1]}",
            "[0, 1, 2, 0]", validCircuit);

        assertTrue(validAlgo.hasEulerianCircuit());
        assertEquals(List.of(0, 1, 2, 0), validCircuit);
    }
}
