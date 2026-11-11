package com.thealgorithms.graph;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GomoryHuTreeTest {

    @Test
    @DisplayName("Single node graph")
    void singleNode() {
        int[][] cap = {{0}};
        int[][] res = GomoryHuTree.buildTree(cap);
        int[] parent = res[0];
        int[] weight = res[1];
        assertEquals(-1, parent[0]);
        assertEquals(0, weight[0]);
    }

    @Test
    @DisplayName("Triangle undirected graph with known min-cuts")
    void triangleGraph() {
        // 0-1:3, 1-2:2, 0-2:4
        int[][] cap = new int[3][3];
        cap[0][1] = 3;
        cap[1][0] = 3;
        cap[1][2] = 2;
        cap[2][1] = 2;
        cap[0][2] = 4;
        cap[2][0] = 4;

        int[][] tree = GomoryHuTree.buildTree(cap);
        // validate all pairs via path-min-edge equals maxflow
        validateAllPairs(cap, tree);
    }

    @Test
    @DisplayName("Random small undirected graphs compare to EdmondsKarp")
    void randomSmallGraphs() {
        Random rng = new Random(42);
        for (int n = 2; n <= 6; n++) {
            for (int iter = 0; iter < 10; iter++) {
                int[][] cap = randSymmetricMatrix(n, 0, 5, rng);
                int[][] tree = GomoryHuTree.buildTree(cap);
                validateAllPairs(cap, tree);
            }
        }
    }

    private static int[][] randSymmetricMatrix(int n, int lo, int hi, RandomGenerator rng) {
        int[][] a = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int w = rng.nextInt(hi - lo + 1) + lo;
                a[i][j] = w;
                a[j][i] = w;
            }
        }
        // zero diagonal
        for (int i = 0; i < n; i++) {
            a[i][i] = 0;
        }
        return a;
    }

    private static void validateAllPairs(int[][] cap, int[][] tree) {
        int n = cap.length;
        int[] parent = tree[0];
        int[] weight = tree[1];

        // build adjacency list of tree without generic array creation
        List<List<int[]>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            g.add(new ArrayList<>());
        }
        for (int v = 1; v < n; v++) {
            int u = parent[v];
            int w = weight[v];
            g.get(u).add(new int[] {v, w});
            g.get(v).add(new int[] {u, w});
        }

        for (int s = 0; s < n; s++) {
            for (int t = s + 1; t < n; t++) {
                int treeVal = minEdgeOnPath(g, s, t);
                int flowVal = EdmondsKarp.maxFlow(cap, s, t);
                assertEquals(flowVal, treeVal, "pair (" + s + "," + t + ")");
            }
        }
    }

    private static int minEdgeOnPath(List<List<int[]>> g, int s, int t) {
        // BFS to record parent and edge weight along the path, since it's a tree, unique path exists
        int n = g.size();
        int[] parent = new int[n];
        int[] edgeW = new int[n];
        Arrays.fill(parent, -1);
        Queue<Integer> q = new ArrayDeque<>();
        q.add(s);
        parent[s] = s;
        while (!q.isEmpty()) {
            int u = q.poll();
            if (u == t) {
                break;
            }
            for (int[] e : g.get(u)) {
                int v = e[0];
                int w = e[1];
                if (parent[v] == -1) {
                    parent[v] = u;
                    edgeW[v] = w;
                    q.add(v);
                }
            }
        }
        int cur = t;
        int ans = Integer.MAX_VALUE;
        while (cur != s) {
            ans = Math.min(ans, edgeW[cur]);
            cur = parent[cur];
        }
        return ans == Integer.MAX_VALUE ? 0 : ans;
    }

    @Test
    @DisplayName("Smallest valid input fixture (1x1 matrix, GomoryHuTree.java:40-42)")
    void testSmallestValidInputFixture() {
        // Precondition established at GomoryHuTree.java:73-86 and branched at GomoryHuTree.java:40 (n == 1)
        int[][] cap = {{0}};
        int[][] tree = GomoryHuTree.buildTree(cap);

        assertNotNull(tree);
        assertEquals(2, tree.length);
        assertArrayEquals(new int[] {-1}, tree[0]);
        assertArrayEquals(new int[] {0}, tree[1]);
    }

    @Test
    @DisplayName("Ordinary valid input fixture (2x2 matrix, GomoryHuTree.java:38-51)")
    void testOrdinaryInputFixture() {
        // Precondition established at GomoryHuTree.java:73-86 (valid non-negative symmetric 2x2 matrix)
        int[][] cap = {{0, 7}, {7, 0}};
        int[][] tree = GomoryHuTree.buildTree(cap);

        assertNotNull(tree);
        assertEquals(2, tree.length);
        assertArrayEquals(new int[] {-1, 0}, tree[0]);
        assertArrayEquals(new int[] {0, 7}, tree[1]);
    }

    @Test
    @DisplayName("Documented ordinary triangle input fixture (3x3 matrix, GomoryHuTree.java:38-51)")
    void testDocumentedOrdinaryTriangleInputFixture() {
        // Precondition established at GomoryHuTree.java:73-86 (valid 3x3 undirected triangle graph)
        int[][] cap = {
            {0, 3, 4},
            {3, 0, 2},
            {4, 2, 0}
        };
        int[][] tree = GomoryHuTree.buildTree(cap);

        assertNotNull(tree);
        assertEquals(2, tree.length);
        assertArrayEquals(new int[] {-1, 0, 0}, tree[0]);
        assertArrayEquals(new int[] {0, 5, 6}, tree[1]);
    }

    @Test
    @DisplayName("Failure path: null capacity matrix throws IllegalArgumentException (GomoryHuTree.java:73-74)")
    void testFailureNullCapacityMatrixThrowsIllegalArgumentException() {
        // Precondition: cap == null checked at GomoryHuTree.java:73, thrown at line 74
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            GomoryHuTree.buildTree(null);
        });
        assertEquals("Capacity matrix must not be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Failure path: empty capacity matrix throws IllegalArgumentException (GomoryHuTree.java:73-74)")
    void testFailureEmptyCapacityMatrixThrowsIllegalArgumentException() {
        // Precondition: cap.length == 0 checked at GomoryHuTree.java:73, thrown at line 74
        int[][] emptyCap = new int[0][0];
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            GomoryHuTree.buildTree(emptyCap);
        });
        assertEquals("Capacity matrix must not be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Failure path: null row in matrix throws IllegalArgumentException (GomoryHuTree.java:78-79)")
    void testFailureNullRowThrowsIllegalArgumentException() {
        // Precondition: cap[i] == null checked at GomoryHuTree.java:78, thrown at line 79
        int[][] nullRowCap = new int[][] {null};
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            GomoryHuTree.buildTree(nullRowCap);
        });
        assertEquals("Capacity matrix must be square", exception.getMessage());
    }

    @Test
    @DisplayName("Failure path: non-square matrix throws IllegalArgumentException (GomoryHuTree.java:78-79)")
    void testFailureNonSquareCapacityMatrixThrowsIllegalArgumentException() {
        // Precondition: cap[i].length != n checked at GomoryHuTree.java:78, thrown at line 79
        int[][] nonSquareCap = {{1, 2}};
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            GomoryHuTree.buildTree(nonSquareCap);
        });
        assertEquals("Capacity matrix must be square", exception.getMessage());
    }

    @Test
    @DisplayName("Failure path: negative capacity throws exception and preserves caller state with subsequent success (GomoryHuTree.java:82-83)")
    void testFailureNegativeCapacityThrowsExceptionAndPreservesCallerStateWithSubsequentSuccess() {
        // Precondition: cap[i][j] < 0 checked at GomoryHuTree.java:82, thrown at line 83
        int[][] invalidMatrix = {{-1}};
        int originalValue = invalidMatrix[0][0];

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            GomoryHuTree.buildTree(invalidMatrix);
        });
        assertEquals("Capacities must be non-negative", exception.getMessage());

        // Verify caller-owned state remains unmodified
        assertEquals(originalValue, invalidMatrix[0][0]);

        // Verify subsequent valid call succeeds without side effects
        int[][] ordinaryCap = {{0, 7}, {7, 0}};
        int[][] tree = GomoryHuTree.buildTree(ordinaryCap);
        assertArrayEquals(new int[] {-1, 0}, tree[0]);
        assertArrayEquals(new int[] {0, 7}, tree[1]);
    }

    @Test
    @DisplayName("Caller-owned state immutability across exceptions and successful runs")
    void testCallerOwnedOrdinaryStateUnmutatedAcrossExceptionsAndExecutions() {
        int[][] callerMatrix = {
            {0, 3, 4},
            {3, 0, 2},
            {4, 2, 0}
        };
        int[][] snapshot = new int[][] {
            Arrays.copyOf(callerMatrix[0], 3),
            Arrays.copyOf(callerMatrix[1], 3),
            Arrays.copyOf(callerMatrix[2], 3)
        };

        // Trigger failure with a negative capacity matrix
        int[][] invalidMatrix = {{0, -5}, {-5, 0}};
        assertThrows(IllegalArgumentException.class, () -> GomoryHuTree.buildTree(invalidMatrix));

        // Call buildTree with valid matrix
        int[][] tree = GomoryHuTree.buildTree(callerMatrix);
        assertArrayEquals(new int[] {-1, 0, 0}, tree[0]);
        assertArrayEquals(new int[] {0, 5, 6}, tree[1]);

        // Assert caller matrix content is untouched
        for (int i = 0; i < 3; i++) {
            assertArrayEquals(snapshot[i], callerMatrix[i]);
        }
    }

    @Test
    @DisplayName("Verify GomoryHuTree private constructor via reflection")
    void testPrivateConstructorContract() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<GomoryHuTree> constructor = GomoryHuTree.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        GomoryHuTree instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
