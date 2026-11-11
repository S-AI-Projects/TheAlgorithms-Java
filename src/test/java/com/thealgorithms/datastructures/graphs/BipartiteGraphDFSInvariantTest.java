package com.thealgorithms.datastructures.graphs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Invariant / property tests for {@link BipartiteGraphDFS}.
 *
 * <p>Source facts ({@code BipartiteGraphDFS.java}):
 * <ul>
 * <li>line 32: private constructor {@code BipartiteGraphDFS()}</li>
 * <li>lines 51-66: {@code bipartite(v, adj, color, node)} assigns {@code color[node] = 1} when
 * {@code -1} (lines 52-54), expands {@code adj.get(node)} (line 55), marks uncolored neighbors
 * {@code color[it] = 1 - color[node]} before recursing (lines 56-60), and returns {@code false}
 * on same-color conflict (lines 61-63)</li>
 * <li>lines 79-90: {@code isBipartite(v, adj)} initializes {@code color} to {@code -1}
 * (lines 80-81) and invokes {@code bipartite} for each unvisited component, returning
 * {@code false} immediately when a component is non-bipartite (lines 82-88)</li>
 * </ul>
 *
 * <p>Invariant: every vertex is colored at most once ({@code -1 -> {0, 1}}), so reachable nodes
 * are expanded at most once and cycles cannot cause an infinite walk.
 */
public class BipartiteGraphDFSInvariantTest {

    private static final long FIXED_SEED = 20261111L;
    private static final int GENERATED_CASES = 120;

    /**
     * Subclass of {@code ArrayList<ArrayList<Integer>>} that records every vertex index passed to
     * {@link #get(int)} by {@code BipartiteGraphDFS.bipartite(..., node)} without altering behavior.
     */
    private static final class TrackingAdjacencyList extends ArrayList<ArrayList<Integer>> {
        private static final long serialVersionUID = 1L;
        private final transient List<Integer> expandedOrder = new ArrayList<>();
        private final transient int[] expansionCounts;

        TrackingAdjacencyList(int v) {
            super(v);
            this.expansionCounts = new int[v];
            for (int i = 0; i < v; i++) {
                super.add(new ArrayList<>());
            }
        }

        void addDirectedEdge(int from, int to) {
            super.get(from).add(to);
        }

        void addUndirectedEdge(int u, int w) {
            super.get(u).add(w);
            if (u != w) {
                super.get(w).add(u);
            }
        }

        @Override
        public ArrayList<Integer> get(int index) {
            expandedOrder.add(index);
            expansionCounts[index]++;
            return super.get(index);
        }

        List<ArrayList<Integer>> snapshot() {
            List<ArrayList<Integer>> copy = new ArrayList<>(size());
            for (int i = 0; i < size(); i++) {
                copy.add(new ArrayList<>(super.get(i)));
            }
            return copy;
        }
    }

    private static boolean independentBfsBipartiteOracle(int v, List<ArrayList<Integer>> adj) {
        int[] color = new int[v];
        Arrays.fill(color, -1);
        for (int start = 0; start < v; start++) {
            if (color[start] != -1) {
                continue;
            }
            color[start] = 0;
            Queue<Integer> queue = new ArrayDeque<>();
            queue.add(start);
            while (!queue.isEmpty()) {
                int curr = queue.poll();
                for (int next : adj.get(curr)) {
                    if (color[next] == -1) {
                        color[next] = 1 - color[curr];
                        queue.add(next);
                    } else if (color[next] == color[curr]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void logCase(String id, String input, Object expected, Object actual) {
        System.out.println("[R45-T014] " + id + " | input=" + input
            + " | expected=" + expected + " | actual=" + actual);
    }

    /**
     * Required fixture: {@code A->B, B->C, C->A} (vertices 0, 1, 2) and isolated {@code D}
     * (vertex 3). Starting at {@code A} (0), the cycle terminates with each reachable node
     * expanded once ({@code [0, 1, 2]}), returns {@code false}, and never visits {@code D} (3).
     */
    @Test
    public void cycleWithIsolatedVertexTerminatesOncePerNodeWithoutVisitingIsolatedD() {
        TrackingAdjacencyList directedFixture = new TrackingAdjacencyList(4);
        directedFixture.addDirectedEdge(0, 1); // A -> B
        directedFixture.addDirectedEdge(1, 2); // B -> C
        directedFixture.addDirectedEdge(2, 0); // C -> A
        // vertex 3 (D) is isolated

        boolean result = BipartiteGraphDFS.isBipartite(4, directedFixture);
        logCase("fixture-directed-cycle-isolated-D",
            "A(0)->B(1),B(1)->C(2),C(2)->A(0),isolated D(3)",
            "bipartite=false,visited=[0,1,2],D_visits=0",
            "bipartite=" + result + ",visited=" + directedFixture.expandedOrder
                + ",D_visits=" + directedFixture.expansionCounts[3]);

        assertFalse(result);
        assertEquals(List.of(0, 1, 2), directedFixture.expandedOrder);
        assertEquals(1, directedFixture.expansionCounts[0]);
        assertEquals(1, directedFixture.expansionCounts[1]);
        assertEquals(1, directedFixture.expansionCounts[2]);
        assertEquals(0, directedFixture.expansionCounts[3]);

        TrackingAdjacencyList undirectedFixture = new TrackingAdjacencyList(4);
        undirectedFixture.addUndirectedEdge(0, 1);
        undirectedFixture.addUndirectedEdge(1, 2);
        undirectedFixture.addUndirectedEdge(2, 0);

        boolean undirectedResult = BipartiteGraphDFS.isBipartite(4, undirectedFixture);
        logCase("fixture-undirected-cycle-isolated-D",
            "A(0)-B(1)-C(2)-A(0),isolated D(3)",
            "bipartite=false,visited=[0,1,2],D_visits=0",
            "bipartite=" + undirectedResult + ",visited=" + undirectedFixture.expandedOrder
                + ",D_visits=" + undirectedFixture.expansionCounts[3]);

        assertFalse(undirectedResult);
        assertEquals(List.of(0, 1, 2), undirectedFixture.expandedOrder);
        assertEquals(0, undirectedFixture.expansionCounts[3]);
    }

    /**
     * Smallest failing non-bipartite example (self-loop {@code 0->0}) and smallest bipartite
     * even cycle ({@code 0-1-2-3-0} + isolated {@code 4}).
     */
    @Test
    public void smallestCycleBoundariesTerminateWithAtMostOneExpansionPerNode() {
        TrackingAdjacencyList selfLoop = new TrackingAdjacencyList(1);
        selfLoop.addDirectedEdge(0, 0);
        boolean selfLoopResult = BipartiteGraphDFS.isBipartite(1, selfLoop);
        logCase("smallest-failing-self-loop", "v=1,edge=(0->0)",
            "bipartite=false,visited=[0]",
            "bipartite=" + selfLoopResult + ",visited=" + selfLoop.expandedOrder);
        assertFalse(selfLoopResult);
        assertEquals(List.of(0), selfLoop.expandedOrder);

        TrackingAdjacencyList evenCyclePlusIsolated = new TrackingAdjacencyList(5);
        evenCyclePlusIsolated.addUndirectedEdge(0, 1);
        evenCyclePlusIsolated.addUndirectedEdge(1, 2);
        evenCyclePlusIsolated.addUndirectedEdge(2, 3);
        evenCyclePlusIsolated.addUndirectedEdge(3, 0);
        // vertex 4 isolated

        boolean evenResult = BipartiteGraphDFS.isBipartite(5, evenCyclePlusIsolated);
        logCase("bipartite-even-cycle-plus-isolated",
            "v=5,cycle=(0-1-2-3-0),isolated=4",
            "bipartite=true,maxVisits=1",
            "bipartite=" + evenResult + ",visited=" + evenCyclePlusIsolated.expandedOrder);
        assertTrue(evenResult);
        assertEquals(List.of(0, 1, 2, 3, 4), evenCyclePlusIsolated.expandedOrder);
        for (int i = 0; i < 5; i++) {
            assertEquals(1, evenCyclePlusIsolated.expansionCounts[i]);
        }
    }

    /**
     * Deterministic property test across 120 bounded valid graphs generated with fixed seed
     * {@code 20261111L}: verifies at-most-once vertex expansion (cycle termination), no input
     * mutation, and exact agreement with an independent BFS two-coloring oracle.
     */
    @Test
    public void seededGeneratedGraphsSatisfySingleVisitAndOracleAgreement() {
        Random rng = new Random(FIXED_SEED);
        int bipartiteCount = 0;
        int nonBipartiteCount = 0;

        for (int caseIdx = 0; caseIdx < GENERATED_CASES; caseIdx++) {
            int v = 1 + rng.nextInt(12);
            TrackingAdjacencyList adj = new TrackingAdjacencyList(v);
            boolean[][] seenEdge = new boolean[v][v];
            int maxEdges = v * (v - 1) / 2;
            int targetEdges = maxEdges == 0 ? 0 : rng.nextInt(maxEdges + 1);
            for (int e = 0; e < targetEdges; e++) {
                int u = rng.nextInt(v);
                int w = rng.nextInt(v);
                if (u != w && !seenEdge[u][w]) {
                    seenEdge[u][w] = true;
                    seenEdge[w][u] = true;
                    adj.addUndirectedEdge(u, w);
                }
            }

            List<ArrayList<Integer>> before = adj.snapshot();
            boolean expected = independentBfsBipartiteOracle(v, before);
            boolean actual = BipartiteGraphDFS.isBipartite(v, adj);

            if (expected) {
                bipartiteCount++;
            } else {
                nonBipartiteCount++;
            }

            assertEquals(expected, actual, "Oracle mismatch on seeded case " + caseIdx);
            assertEquals(before, adj.snapshot(), "Input mutated on seeded case " + caseIdx);
            for (int node = 0; node < v; node++) {
                assertTrue(adj.expansionCounts[node] <= 1,
                    "Node " + node + " expanded more than once on seeded case " + caseIdx);
                if (expected) {
                    assertEquals(1, adj.expansionCounts[node],
                        "Bipartite graph must expand every node once on seeded case " + caseIdx);
                }
            }
        }

        logCase("seeded-property-120",
            "seed=" + FIXED_SEED + ",cases=" + GENERATED_CASES + ",v in [1..12]",
            "all_agree=true,maxVisits<=1",
            "bipartite=" + bipartiteCount + ",nonBipartite=" + nonBipartiteCount + ",maxVisits<=1");
        assertTrue(bipartiteCount > 0 && nonBipartiteCount > 0);
    }
}
