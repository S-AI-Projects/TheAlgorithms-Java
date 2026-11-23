package com.thealgorithms.matrix;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Cross-check tests for {@link MirrorOfMatrix}.
 *
 * Instead of hardcoding expected outputs, these tests verify the
 * mathematical properties that must hold for any correct mirror
 * (horizontal reflection) implementation:
 *
 * <ul>
 *   <li>P1 (Involution): mirror(mirror(M)) == M — mirroring twice returns the original.</li>
 *   <li>P2 (Dimension preservation): the mirrored matrix has the same dimensions as the input.</li>
 *   <li>P3 (Element mapping): mirroredMatrix[i][j] == originalMatrix[i][numCols - 1 - j].</li>
 *   <li>P4 (Row sum invariance): each row's sum is unchanged by mirroring.</li>
 *   <li>P5 (Identity for single-column): a single-column matrix mirrors to itself.</li>
 *   <li>P6 (Symmetric matrix): a row-wise palindromic matrix mirrors to itself.</li>
 * </ul>
 *
 * Fixes: #23
 */
class MirrorOfMatrixCrossCheckTest {

    // ── P1: Involution property — mirror(mirror(M)) == M ────────────────

    @Test
    void crossCheckInvolution3x3() {
        double[][] original = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        double[][] mirrored = MirrorOfMatrix.mirrorMatrix(original);
        double[][] doubleMirrored = MirrorOfMatrix.mirrorMatrix(mirrored);
        assertArrayEquals(original, doubleMirrored, "Mirroring twice must return the original matrix");
    }

    @Test
    void crossCheckInvolution2x4() {
        double[][] original = {{1, 2, 3, 4}, {5, 6, 7, 8}};
        double[][] mirrored = MirrorOfMatrix.mirrorMatrix(original);
        double[][] doubleMirrored = MirrorOfMatrix.mirrorMatrix(mirrored);
        assertArrayEquals(original, doubleMirrored, "Involution must hold for rectangular matrices");
    }

    @Test
    void crossCheckInvolution1x1() {
        double[][] original = {{42}};
        double[][] mirrored = MirrorOfMatrix.mirrorMatrix(original);
        double[][] doubleMirrored = MirrorOfMatrix.mirrorMatrix(mirrored);
        assertArrayEquals(original, doubleMirrored, "Involution must hold for 1x1 matrices");
    }

    // ── P2: Dimension preservation ──────────────────────────────────────

    @Test
    void crossCheckDimensionsPreserved() {
        double[][] original = {{1, 2, 3}, {4, 5, 6}};
        double[][] mirrored = MirrorOfMatrix.mirrorMatrix(original);

        assertEquals(original.length, mirrored.length, "Number of rows must be preserved");
        for (int i = 0; i < original.length; i++) {
            assertEquals(original[i].length, mirrored[i].length,
                    "Number of columns in row " + i + " must be preserved");
        }
    }

    @Test
    void crossCheckDimensionsPreservedLargeMatrix() {
        int rows = 50;
        int cols = 100;
        double[][] original = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                original[i][j] = i * cols + j;
            }
        }
        double[][] mirrored = MirrorOfMatrix.mirrorMatrix(original);
        assertEquals(rows, mirrored.length);
        for (int i = 0; i < rows; i++) {
            assertEquals(cols, mirrored[i].length);
        }
    }

    // ── P3: Element mapping — mirroredMatrix[i][j] == original[i][cols-1-j] ─

    @Test
    void crossCheckElementMapping3x3() {
        double[][] original = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        double[][] mirrored = MirrorOfMatrix.mirrorMatrix(original);
        int numCols = original[0].length;

        for (int i = 0; i < original.length; i++) {
            for (int j = 0; j < numCols; j++) {
                assertEquals(original[i][numCols - 1 - j], mirrored[i][j],
                        String.format("Element at [%d][%d] must equal original[%d][%d]", i, j, i, numCols - 1 - j));
            }
        }
    }

    @Test
    void crossCheckElementMapping4x5() {
        double[][] original = {
            {1, 2, 3, 4, 5},
            {6, 7, 8, 9, 10},
            {11, 12, 13, 14, 15},
            {16, 17, 18, 19, 20}
        };
        double[][] mirrored = MirrorOfMatrix.mirrorMatrix(original);
        int numCols = original[0].length;

        for (int i = 0; i < original.length; i++) {
            for (int j = 0; j < numCols; j++) {
                assertEquals(original[i][numCols - 1 - j], mirrored[i][j]);
            }
        }
    }

    // ── P4: Row sum invariance ──────────────────────────────────────────

    @Test
    void crossCheckRowSumInvariance() {
        double[][] original = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        double[][] mirrored = MirrorOfMatrix.mirrorMatrix(original);

        for (int i = 0; i < original.length; i++) {
            double originalRowSum = 0;
            double mirroredRowSum = 0;
            for (int j = 0; j < original[i].length; j++) {
                originalRowSum += original[i][j];
                mirroredRowSum += mirrored[i][j];
            }
            assertEquals(originalRowSum, mirroredRowSum, 1e-10,
                    "Row " + i + " sum must be invariant under mirroring");
        }
    }

    // ── P5: Identity for single-column matrices ─────────────────────────

    @Test
    void crossCheckSingleColumnIdentity() {
        double[][] original = {{1}, {2}, {3}, {4}};
        double[][] mirrored = MirrorOfMatrix.mirrorMatrix(original);
        assertArrayEquals(original, mirrored, "Single-column matrix must mirror to itself");
    }

    // ── P6: Palindromic rows mirror to themselves ───────────────────────

    @Test
    void crossCheckPalindromicRowsAreFixedPoints() {
        double[][] original = {{1, 2, 1}, {3, 4, 3}, {5, 6, 5}};
        double[][] mirrored = MirrorOfMatrix.mirrorMatrix(original);
        assertArrayEquals(original, mirrored, "A matrix with palindromic rows must mirror to itself");
    }

    // ── Additional: does not modify the original matrix ─────────────────

    @Test
    void crossCheckOriginalNotMutated() {
        double[][] original = {{1, 2, 3}, {4, 5, 6}};
        double[][] copy = {{1, 2, 3}, {4, 5, 6}};
        MirrorOfMatrix.mirrorMatrix(original);
        assertArrayEquals(copy, original, "mirrorMatrix must not modify the original matrix");
    }
}
