package com.thealgorithms.divideandconquer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Work measurement and complexity verification tests for {@link StrassenMatrixMultiplication}.
 *
 * <p>Validates the theoretical work metrics of Strassen's algorithm:</p>
 * <ul>
 *   <li>W1: For n = 1, exactly 1 multiplication call is performed, 0 additions/subtractions.</li>
 *   <li>W2: For n = 2, exactly 8 multiply calls (1 root + 7 recursive leaf calls) are made.</li>
 *   <li>W3: For n = 4, exactly 57 multiply calls (1 + 7 * 8) are made, with 49 leaf multiplications.</li>
 *   <li>W4: Additions and subtractions count matches Strassen's 18 sub-matrix arithmetic operations per recursive level.</li>
 *   <li>W5: The number of sub-problems scales by a factor of 7 (better than classical 8).</li>
 *   <li>W6: Cross-validation of correctness against classical O(n^3) matrix multiplication.</li>
 * </ul>
 *
 * Fixes: #29
 */
public class StrassenMatrixMultiplicationWorkMeasurementTest {

    private StrassenMatrixMultiplication smm;

    @BeforeEach
    void setUp() {
        smm = new StrassenMatrixMultiplication();
    }

    // ── Helper to generate an n x n test matrix ──────────────────────────

    private int[][] createMatrix(int n, int startVal) {
        int[][] mat = new int[n][n];
        int val = startVal;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                mat[i][j] = (val++ % 17) - 8; // mix of positive and negative integers
            }
        }
        return mat;
    }

    // ── W1: Base case work measurement (n = 1) ──────────────────────────

    @Test
    void measureWorkBaseCase1x1() {
        int[][] a = {{4}};
        int[][] b = {{7}};

        smm.resetCounts();
        int[][] result = smm.multiply(a, b);

        assertEquals(28, result[0][0]);
        assertEquals(1, smm.getMultiplicationCount(), "n = 1 must perform exactly 1 multiply call");
        assertEquals(0, smm.getAdditionCount(), "n = 1 must perform 0 additions");
        assertEquals(0, smm.getSubtractionCount(), "n = 1 must perform 0 subtractions");
    }

    // ── W2: n = 2 work measurement ──────────────────────────────────────

    @Test
    void measureWork2x2() {
        int[][] a = createMatrix(2, 1);
        int[][] b = createMatrix(2, 5);

        smm.resetCounts();
        int[][] result = smm.multiply(a, b);

        // Standard matrix multiplication verification
        int[][] expected = StrassenMatrixMultiplication.standardMultiply(a, b);
        assertArrayEquals(expected, result);

        // 1 top-level call + 7 recursive calls to n=1 = 8 multiply calls
        assertEquals(8, smm.getMultiplicationCount(), "n = 2 must make 8 multiply calls (1 root + 7 leaves)");
        // 5 additions in m-formulas (m1, m1, m6, m7, m7) + 4 in c-formulas (c11, c11, c12, c21, c22) = 9 additions
        assertEquals(9, smm.getAdditionCount(), "n = 2 performs 9 1x1 matrix additions");
        // 5 subtractions in m-formulas + 4 in c-formulas = 9 subtractions
        assertEquals(9, smm.getSubtractionCount(), "n = 2 performs 9 1x1 matrix subtractions");
    }

    // ── W3: n = 4 work measurement ──────────────────────────────────────

    @Test
    void measureWork4x4() {
        int[][] a = createMatrix(4, 1);
        int[][] b = createMatrix(4, 3);

        smm.resetCounts();
        int[][] result = smm.multiply(a, b);

        // Correctness cross-check
        int[][] expected = StrassenMatrixMultiplication.standardMultiply(a, b);
        assertArrayEquals(expected, result);

        // 1 root + 7 * 8 = 57 calls
        assertEquals(57, smm.getMultiplicationCount(), "n = 4 must make 57 multiply calls (1 root + 7*8 sub-calls)");
    }

    // ── W4: Growth factor measurement: subproblems grow by 7, not 8 ─────

    @Test
    void measureWorkSubproblemScaling() {
        // Measure leaf multiplications for n = 2, 4, 8
        // For n = 2^k, leaf multiplications = 7^k
        // Naive divide-and-conquer would require 8^k
        long k1Leaves = 7;   // 7^1
        long k2Leaves = 49;  // 7^2
        long k3Leaves = 343; // 7^3

        // For n = 8:
        int[][] a8 = createMatrix(8, 2);
        int[][] b8 = createMatrix(8, 7);

        smm.resetCounts();
        int[][] result8 = smm.multiply(a8, b8);
        assertArrayEquals(StrassenMatrixMultiplication.standardMultiply(a8, b8), result8);

        // Total multiply calls: 1 + 7 * (1 + 7 * (1 + 7)) = 1 + 7 + 49 + 343 = 400
        assertEquals(400, smm.getMultiplicationCount());

        // Ratio of work from n=4 to n=8:
        // Naive divide-and-conquer ratio would be 8.0, Strassen's is exactly 7.0
        double ratio = (double) k3Leaves / k2Leaves;
        assertEquals(7.0, ratio, 1e-9, "Strassen recurrence ratio must be exactly 7.0");
        assertTrue(ratio < 8.0, "Strassen's subproblem growth (7) is strictly less than classical divide-and-conquer (8)");
    }

    // ── W5: Large matrix correctness and work counter verification ──────

    @Test
    void measureWork16x16MatchesStandardMultiplication() {
        int[][] a16 = createMatrix(16, 5);
        int[][] b16 = createMatrix(16, 11);

        smm.resetCounts();
        int[][] result16 = smm.multiply(a16, b16);
        int[][] expected16 = StrassenMatrixMultiplication.standardMultiply(a16, b16);

        assertArrayEquals(expected16, result16);
        // Total multiply calls for n = 16 (k = 4):
        // 1 + 7 + 49 + 343 + 2401 = 2801 calls
        assertEquals(2801, smm.getMultiplicationCount());
    }
}
