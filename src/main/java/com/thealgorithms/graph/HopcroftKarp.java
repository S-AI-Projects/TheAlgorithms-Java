package com.thealgorithms.graph;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

/**
 * Hopcroft–Karp algorithm for maximum bipartite matching.
 *
 * Left part: vertices [0,nLeft-1], Right part: [0,nRight-1].
 * Adjacency list: for each left vertex u, list right vertices v it connects to.
 *
 * Time complexity: O(E * sqrt(V)).
 *
 * @see <a href="https://en.wikipedia.org/wiki/Hopcroft%E2%80%93Karp_algorithm">
 *      Wikipedia: Hopcroft–Karp algorithm</a>
 * @author ptzecher
 */
public class HopcroftKarp {

    private final int nLeft;
    private final List<List<Integer>> adj;

    private final int[] pairU;
    private final int[] pairV;
    private final int[] dist;

    /**
     * Constructs a Hopcroft-Karp solver for a bipartite graph.
     *
     * <p>Contract:
     * <ul>
     *   <li>Preconditions: {@code nLeft >= 0}, {@code nRight >= 0}, {@code adj != null} with at least
     *       {@code nLeft} non-null adjacency lists, and every right vertex index {@code v} in
     *       {@code adj.get(u)} satisfies {@code 0 <= v < nRight}.</li>
     *   <li>Initial state: all entries of {@code pairU} and {@code pairV} start at {@code -1} (unmatched).</li>
     *   <li>Mutation: {@code adj} is only read and never mutated; {@link #maxMatching()} updates the
     *       internal matching arrays and returns the number of newly matched edges.</li>
     * </ul>
     *
     * @param nLeft number of vertices in the left partition ({@code [0, nLeft - 1]})
     * @param nRight number of vertices in the right partition ({@code [0, nRight - 1]})
     * @param adj adjacency list mapping each left vertex {@code u} to its incident right vertices
     */
    public HopcroftKarp(int nLeft, int nRight, List<List<Integer>> adj) {
        this.nLeft = nLeft;
        this.adj = adj;

        this.pairU = new int[nLeft];
        this.pairV = new int[nRight];
        this.dist = new int[nLeft];

        Arrays.fill(pairU, -1);
        Arrays.fill(pairV, -1);
    }

    /** Returns the size of the maximum matching. */
    public int maxMatching() {
        int matching = 0;
        while (bfs()) {
            for (int u = 0; u < nLeft; u++) {
                if (pairU[u] == -1 && dfs(u)) {
                    matching++;
                }
            }
        }
        return matching;
    }

    // BFS to build layers
    private boolean bfs() {
        Queue<Integer> queue = new ArrayDeque<>();
        Arrays.fill(dist, -1);

        for (int u = 0; u < nLeft; u++) {
            if (pairU[u] == -1) {
                dist[u] = 0;
                queue.add(u);
            }
        }

        boolean foundAugPath = false;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v : adj.get(u)) {
                int matchedLeft = pairV[v];
                if (matchedLeft == -1) {
                    foundAugPath = true;
                } else if (dist[matchedLeft] == -1) {
                    dist[matchedLeft] = dist[u] + 1;
                    queue.add(matchedLeft);
                }
            }
        }
        return foundAugPath;
    }

    // DFS to find augmenting paths within the BFS layering
    private boolean dfs(int u) {
        for (int v : adj.get(u)) {
            int matchedLeft = pairV[v];
            if (matchedLeft == -1 || (dist[matchedLeft] == dist[u] + 1 && dfs(matchedLeft))) {
                pairU[u] = v;
                pairV[v] = u;
                return true;
            }
        }
        dist[u] = -1;
        return false;
    }

    public int[] getLeftMatches() {
        return pairU.clone();
    }

    public int[] getRightMatches() {
        return pairV.clone();
    }
}
