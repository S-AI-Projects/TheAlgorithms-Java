package com.thealgorithms.matrix;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LUDecompositionTest {

    @Test
    public void testLUDecomposition() {
        double[][] a = {{4, 3}, {6, 3}};

        // Perform LU decomposition
        LUDecomposition.LU lu = LUDecomposition.decompose(a);
        double[][] l = lu.l;
        double[][] u = lu.u;

        // Reconstruct a from l and u
        double[][] reconstructed = multiplyMatrices(l, u);

        // Assert that reconstructed matrix matches original a
        for (int i = 0; i < a.length; i++) {
            assertArrayEquals(a[i], reconstructed[i], 1e-9);
        }
    }

    // Helper method to multiply two matrices
    private double[][] multiplyMatrices(double[][] a, double[][] b) {
        int n = a.length;
        double[][] c = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    c[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return c;
    }

    private record ReferenceCase(String name, double[][] expectedL, double[][] expectedU) {
    }

    @Test
    @DisplayName("Cross-check 24 small square matrices against independent L*U reference oracle")
    public void testCrossCheckAgainstIndependentReferenceOracle() {
        // Note: LUDecomposition.decompose only supports square n x n matrices with non-zero
        // leading principal minors (LUDecomposition.java pinned lines 6, 34, 40-42, 63).
        // Rectangular matrices are outside the documented square-matrix domain.
        // Because unit-lower-triangular L and upper-triangular U with non-zero diagonal are unique,
        // constructing A = expectedL * expectedU provides an independent reference oracle without
        // invoking LUDecomposition.decompose.
        ReferenceCase[] cases = new ReferenceCase[] {
            // 1x1 boundary cases
            new ReferenceCase("1x1_positive", new double[][] {{1}}, new double[][] {{5}}),
            new ReferenceCase("1x1_negative", new double[][] {{1}}, new double[][] {{-3.5}}),
            new ReferenceCase("1x1_zero", new double[][] {{1}}, new double[][] {{0}}),
            // 2x2 cases (including zero-containing, [[1,2],[3,4]], and representative [[4,3],[6,3]])
            new ReferenceCase("2x2_identity", new double[][] {{1, 0}, {0, 1}}, new double[][] {{1, 0}, {0, 1}}),
            new ReferenceCase("2x2_zero_upper", new double[][] {{1, 0}, {2, 1}}, new double[][] {{2, 0}, {0, 3}}),
            new ReferenceCase("2x2_zero_lower", new double[][] {{1, 0}, {0, 1}}, new double[][] {{1, 2}, {0, 4}}),
            new ReferenceCase("2x2_standard_1234", new double[][] {{1, 0}, {3, 1}}, new double[][] {{1, 2}, {0, -2}}),
            new ReferenceCase("2x2_representative_4363", new double[][] {{1, 0}, {1.5, 1}}, new double[][] {{4, 3}, {0, -1.5}}),
            new ReferenceCase("2x2_negative_pivots", new double[][] {{1, 0}, {-2, 1}}, new double[][] {{-2, 5}, {0, -4}}),
            new ReferenceCase("2x2_fractional", new double[][] {{1, 0}, {0.5, 1}}, new double[][] {{2, -1}, {0, 2.5}}),
            new ReferenceCase("2x2_zero_final_pivot", new double[][] {{1, 0}, {2, 1}}, new double[][] {{3, 1}, {0, 0}}),
            new ReferenceCase("2x2_diagonal_neg", new double[][] {{1, 0}, {0, 1}}, new double[][] {{-7, 0}, {0, 9}}),
            // 3x3 cases (including zero-containing and sparse)
            new ReferenceCase("3x3_identity", new double[][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}}, new double[][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}}),
            new ReferenceCase("3x3_zero_containing", new double[][] {{1, 0, 0}, {0, 1, 0}, {2, 0, 1}}, new double[][] {{2, 1, 0}, {0, 3, 1}, {0, 0, 5}}),
            new ReferenceCase("3x3_dense_int", new double[][] {{1, 0, 0}, {2, 1, 0}, {-1, 3, 1}}, new double[][] {{1, 2, -1}, {0, 3, 2}, {0, 0, 4}}),
            new ReferenceCase("3x3_upper_only", new double[][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}}, new double[][] {{4, -2, 1}, {0, 5, 3}, {0, 0, -2}}),
            new ReferenceCase("3x3_lower_unit_diag", new double[][] {{1, 0, 0}, {-1, 1, 0}, {2, -3, 1}}, new double[][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}}),
            new ReferenceCase("3x3_fractional", new double[][] {{1, 0, 0}, {0.5, 1, 0}, {-0.25, 1.5, 1}}, new double[][] {{4, 2, 0}, {0, 2, -2}, {0, 0, 3}}),
            new ReferenceCase("3x3_negative_entries", new double[][] {{1, 0, 0}, {-2, 1, 0}, {1, -1, 1}}, new double[][] {{-3, 1, -2}, {0, -2, 4}, {0, 0, -5}}),
            new ReferenceCase("3x3_sparse_zeros", new double[][] {{1, 0, 0}, {3, 1, 0}, {0, -2, 1}}, new double[][] {{2, 0, 1}, {0, 4, 0}, {0, 0, 3}}),
            // 4x4 cases
            new ReferenceCase("4x4_diagonal_zero_off", new double[][] {{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}}, new double[][] {{2, 0, 0, 0}, {0, -3, 0, 0}, {0, 0, 5, 0}, {0, 0, 0, 7}}),
            new ReferenceCase("4x4_banded", new double[][] {{1, 0, 0, 0}, {1, 1, 0, 0}, {0, 2, 1, 0}, {0, 0, -1, 1}}, new double[][] {{2, 1, 0, 0}, {0, 3, 1, 0}, {0, 0, 4, 1}, {0, 0, 0, 5}}),
            new ReferenceCase("4x4_dense_small", new double[][] {{1, 0, 0, 0}, {-1, 1, 0, 0}, {2, -1, 1, 0}, {1, 2, -1, 1}}, new double[][] {{1, 2, 0, -1}, {0, 2, 1, 3}, {0, 0, -3, 2}, {0, 0, 0, 4}}),
            new ReferenceCase("4x4_fractional", new double[][] {{1, 0, 0, 0}, {0.5, 1, 0, 0}, {0, -0.5, 1, 0}, {0.25, 0, 0.5, 1}}, new double[][] {{4, 0, 2, 0}, {0, 2, 0, 2}, {0, 0, 2, -1}, {0, 0, 0, 3}})
        };

        for (ReferenceCase tc : cases) {
            double[][] inputA = multiplyMatrices(tc.expectedL(), tc.expectedU());
            double[][] inputSnapshot = new double[inputA.length][];
            for (int i = 0; i < inputA.length; i++) {
                inputSnapshot[i] = inputA[i].clone();
            }

            LUDecomposition.LU actual = LUDecomposition.decompose(inputA);
            int n = inputA.length;

            // Check dimensions
            assertEquals(n, actual.l.length, tc.name() + ": L row count");
            assertEquals(n, actual.u.length, tc.name() + ": U row count");
            for (int i = 0; i < n; i++) {
                assertEquals(n, actual.l[i].length, tc.name() + ": L col count row " + i);
                assertEquals(n, actual.u[i].length, tc.name() + ": U col count row " + i);
                assertArrayEquals(tc.expectedL()[i], actual.l[i], 1e-9, tc.name() + ": L row " + i);
                assertArrayEquals(tc.expectedU()[i], actual.u[i], 1e-9, tc.name() + ": U row " + i);
                assertArrayEquals(inputSnapshot[i], inputA[i], 0.0, tc.name() + ": input mutated row " + i);
            }

            // Check reconstruction L * U == A
            double[][] reconstructed = multiplyMatrices(actual.l, actual.u);
            for (int i = 0; i < n; i++) {
                assertArrayEquals(inputSnapshot[i], reconstructed[i], 1e-9, tc.name() + ": reconstruction row " + i);
            }

            System.out.println("CASE|" + tc.name() + "|n=" + n + ",A=" + Arrays.deepToString(inputSnapshot) + "|expectedL=" + Arrays.deepToString(tc.expectedL()) + ",expectedU=" + Arrays.deepToString(tc.expectedU()) + "|actualL=" + Arrays.deepToString(actual.l) + ",actualU=" + Arrays.deepToString(actual.u));
        }
    }
}
