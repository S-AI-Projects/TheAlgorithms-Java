package com.thealgorithms.graph;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Hopcroft–Karp algorithm.
 *
 * @author ptzecher
 */
class HopcroftKarpTest {

    private static List<List<Integer>> adj(int nLeft) {
        List<List<Integer>> g = new ArrayList<>(nLeft);
        for (int i = 0; i < nLeft; i++) { // braces required by Checkstyle
            g.add(new ArrayList<>());
        }
        return g;
    }

    @Test
    @DisplayName("Empty graph has matching 0")
    void emptyGraph() {
        List<List<Integer>> g = adj(3);
        HopcroftKarp hk = new HopcroftKarp(3, 4, g);
        assertEquals(0, hk.maxMatching());
    }

    @Test
    @DisplayName("Single edge gives matching 1")
    void singleEdge() {
        List<List<Integer>> g = adj(1);
        g.get(0).add(0);
        HopcroftKarp hk = new HopcroftKarp(1, 1, g);
        assertEquals(1, hk.maxMatching());

        int[] leftMatch = hk.getLeftMatches();
        int[] rightMatch = hk.getRightMatches();
        assertEquals(0, leftMatch[0]);
        assertEquals(0, rightMatch[0]);
    }

    @Test
    @DisplayName("Disjoint edges match perfectly")
    void disjointEdges() {
        // L0-R0, L1-R1, L2-R2
        List<List<Integer>> g = adj(3);
        g.get(0).add(0);
        g.get(1).add(1);
        g.get(2).add(2);

        HopcroftKarp hk = new HopcroftKarp(3, 3, g);
        assertEquals(3, hk.maxMatching());

        int[] leftMatch = hk.getLeftMatches();
        int[] rightMatch = hk.getRightMatches();
        for (int i = 0; i < 3; i++) {
            assertEquals(i, leftMatch[i]);
            assertEquals(i, rightMatch[i]);
        }
    }

    @Test
    @DisplayName("Complete bipartite K(3,4) matches min(3,4)=3")
    void completeK34() {
        int nLeft = 3;
        int nRight = 4; // split declarations
        List<List<Integer>> g = adj(nLeft);
        for (int u = 0; u < nLeft; u++) {
            g.get(u).addAll(Arrays.asList(0, 1, 2, 3));
        }
        HopcroftKarp hk = new HopcroftKarp(nLeft, nRight, g);
        assertEquals(3, hk.maxMatching());

        // sanity: no two left vertices share the same matched right vertex
        int[] leftMatch = hk.getLeftMatches();
        boolean[] used = new boolean[nRight];
        for (int u = 0; u < nLeft; u++) {
            int v = leftMatch[u];
            if (v != -1) {
                assertFalse(used[v]);
                used[v] = true;
            }
        }
    }

    @Test
    @DisplayName("Rectangular, sparse graph")
    void rectangularSparse() {
        // Left: 5, Right: 2
        // edges: L0-R0, L1-R1, L2-R0, L3-R1  (max matching = 2)
        List<List<Integer>> g = adj(5);
        g.get(0).add(0);
        g.get(1).add(1);
        g.get(2).add(0);
        g.get(3).add(1);
        // L4 isolated

        HopcroftKarp hk = new HopcroftKarp(5, 2, g);
        assertEquals(2, hk.maxMatching());

        int[] leftMatch = hk.getLeftMatches();
        int[] rightMatch = hk.getRightMatches();

        // Check consistency: if leftMatch[u]=v then rightMatch[v]=u
        for (int u = 0; u < 5; u++) {
            int v = leftMatch[u];
            if (v != -1) {
                assertEquals(u, rightMatch[v]);
            }
        }
    }

    @Test
    @DisplayName("Layering advantage case (short augmenting paths)")
    void layeringAdvantage() {
        // Left 4, Right 4
        List<List<Integer>> g = adj(4);
        g.get(0).addAll(Arrays.asList(0, 1));
        g.get(1).addAll(Arrays.asList(1, 2));
        g.get(2).addAll(Arrays.asList(2, 3));
        g.get(3).addAll(Arrays.asList(0, 3));

        HopcroftKarp hk = new HopcroftKarp(4, 4, g);
        assertEquals(4, hk.maxMatching()); // perfect matching exists
    }

    @Test
    @DisplayName("Contract: smallest valid input (0 left, 0 right) returns 0 and empty match arrays")
    void contractSmallestEmptyPartitions() {
        // Preconditions from HopcroftKarp.java: lines 33-34 (nLeft >= 0, nRight >= 0),
        // lines 37-38 (initial pairU/pairV = -1), lines 44-51 (bfs returns false when nLeft == 0).
        List<List<Integer>> g = adj(0);
        HopcroftKarp hk = new HopcroftKarp(0, 0, g);

        int actualMatching = hk.maxMatching();
        int[] actualLeft = hk.getLeftMatches();
        int[] actualRight = hk.getRightMatches();

        assertEquals(0, actualMatching, "Expected matching size 0 for (0, 0, [])");
        assertArrayEquals(new int[0], actualLeft, "Expected empty left matches");
        assertArrayEquals(new int[0], actualRight, "Expected empty right matches");
        System.out.println("CASE|smallest_0x0|nLeft=0,nRight=0,adj=[]|expected=0,[],[]|actual=" + actualMatching + "," + Arrays.toString(actualLeft) + "," + Arrays.toString(actualRight));
    }

    @Test
    @DisplayName("Contract: ordinary 3x3 graph with unique maximum matching returns exact pairs")
    void contractOrdinaryUniqueMatching() {
        // Preconditions from HopcroftKarp.java: lines 33-34 (nLeft=3, nRight=3),
        // line 69 (adj.get(u) valid for u in [0, 3)), line 70 (v in [0, 3)).
        // Independent derivation: L1->R0 is forced (only edge of L1), forcing L0->R1 and L2->R2.
        List<List<Integer>> g = adj(3);
        g.get(0).addAll(Arrays.asList(0, 1));
        g.get(1).add(0);
        g.get(2).addAll(Arrays.asList(1, 2));

        HopcroftKarp hk = new HopcroftKarp(3, 3, g);
        int actualMatching = hk.maxMatching();
        int[] actualLeft = hk.getLeftMatches();
        int[] actualRight = hk.getRightMatches();

        assertEquals(3, actualMatching, "Expected unique maximum matching of size 3");
        assertArrayEquals(new int[] {1, 0, 2}, actualLeft, "Expected left matches [1, 0, 2]");
        assertArrayEquals(new int[] {1, 0, 2}, actualRight, "Expected right matches [1, 0, 2]");
        System.out.println("CASE|ordinary_3x3|nLeft=3,nRight=3,adj=[[0,1],[0],[1,2]]|expected=3,[1, 0, 2],[1, 0, 2]|actual=" + actualMatching + "," + Arrays.toString(actualLeft) + "," + Arrays.toString(actualRight));
    }

    @Test
    @DisplayName("Contract: repeated edges in adjacency list still cap matching at 1 per right vertex")
    void contractRepeatedEdgesBoundary() {
        // Preconditions from HopcroftKarp.java: line 69-70 (duplicate v=0 entries within [0, nRight)),
        // lines 86-89 (once R0 is matched to L0, duplicate edges to R0 cannot match L1).
        List<List<Integer>> g = adj(2);
        g.get(0).addAll(Arrays.asList(0, 0));
        g.get(1).addAll(Arrays.asList(0, 0));

        HopcroftKarp hk = new HopcroftKarp(2, 2, g);
        int actualMatching = hk.maxMatching();
        int[] actualLeft = hk.getLeftMatches();
        int[] actualRight = hk.getRightMatches();

        assertEquals(1, actualMatching, "Expected matching size 1 when all repeated edges target R0");
        assertArrayEquals(new int[] {0, -1}, actualLeft, "Expected left matches [0, -1]");
        assertArrayEquals(new int[] {0, -1}, actualRight, "Expected right matches [0, -1]");
        System.out.println("CASE|repeated_edges|nLeft=2,nRight=2,adj=[[0,0],[0,0]]|expected=1,[0, -1],[0, -1]|actual=" + actualMatching + "," + Arrays.toString(actualLeft) + "," + Arrays.toString(actualRight));
    }

    @Test
    @DisplayName("Contract: observable mutation isolation (input adj unchanged, defensive match clones, internal state updates)")
    void contractObservableMutationBehavior() {
        // Preconditions from HopcroftKarp.java: lines 37-38 (initial -1 state),
        // lines 42-51 (internal pairU/pairV mutated during maxMatching; repeat call returns 0),
        // lines 96-102 (getLeftMatches/getRightMatches return defensive clones).
        List<List<Integer>> g = adj(2);
        g.get(0).addAll(Arrays.asList(0, 1));
        g.get(1).add(0);
        List<List<Integer>> snapshot = List.of(List.of(0, 1), List.of(0));

        HopcroftKarp hk = new HopcroftKarp(2, 2, g);

        // Before maxMatching(): all vertices are unmatched (-1).
        assertArrayEquals(new int[] {-1, -1}, hk.getLeftMatches());
        assertArrayEquals(new int[] {-1, -1}, hk.getRightMatches());

        int firstRun = hk.maxMatching();
        assertEquals(2, firstRun);

        // Input adjacency list must not be mutated.
        assertEquals(snapshot, g, "Input adjacency list must remain unmodified");

        // Returned match arrays must be defensive clones.
        int[] leftCopy = hk.getLeftMatches();
        int[] rightCopy = hk.getRightMatches();
        assertArrayEquals(new int[] {1, 0}, leftCopy);
        assertArrayEquals(new int[] {1, 0}, rightCopy);
        leftCopy[0] = -99;
        rightCopy[0] = -99;
        assertArrayEquals(new int[] {1, 0}, hk.getLeftMatches(), "Mutating getLeftMatches() must not affect internal pairU");
        assertArrayEquals(new int[] {1, 0}, hk.getRightMatches(), "Mutating getRightMatches() must not affect internal pairV");

        // Calling maxMatching() again finds 0 additional augmenting paths while keeping matches intact.
        int secondRun = hk.maxMatching();
        assertEquals(0, secondRun, "Second maxMatching() call should return 0 additional matches");
        assertArrayEquals(new int[] {1, 0}, hk.getLeftMatches());
        assertArrayEquals(new int[] {1, 0}, hk.getRightMatches());
        System.out.println("CASE|mutation_isolation|nLeft=2,nRight=2,adj=[[0,1],[0]]|expected=first:2,second:0,adjUnchanged:true,clonesIsolated:true|actual=first:" + firstRun + ",second:" + secondRun + ",adjUnchanged:" + snapshot.equals(g) + ",clonesIsolated:true");
    }
}
