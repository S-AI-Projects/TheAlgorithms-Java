package com.thealgorithms.datastructures.graphs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Repeatable verification tests for {@code minKey()} in {@link PrimMST}.
 *
 * These tests ensure deterministic, idempotent, and repeatable verification
 * of the core vertex selection helper:
 *
 * <ul>
 *   <li>R1 (Idempotence & Repeatability): Repeated invocations with identical state
 *       always produce identical outputs.</li>
 *   <li>R2 (Position Independence): Finds the minimum vertex regardless of whether
 *       the minimum is at the first, middle, or last position.</li>
 *   <li>R3 (All In MST): Returns -1 when all vertices are already marked in mstSet.</li>
 *   <li>R4 (All Max Value): Returns -1 when all unvisited vertices have key Integer.MAX_VALUE.</li>
 *   <li>R5 (Deterministic Tie-Breaking): Uses strict less-than inequality to break ties
 *       in favor of the lowest index deterministically.</li>
 *   <li>R6 (Non-Mutation): minKey does not alter key[] or mstSet[].</li>
 *   <li>R7 (Step-by-Step Simulation): Verifies repeatable step-by-step MST vertex selection.</li>
 * </ul>
 *
 * Fixes: #25
 */
public class PrimMSTRepeatableTest {

    private PrimMST primMST;

    @BeforeEach
    void setUp() {
        primMST = new PrimMST();
    }

    // ── R1: Repeatability across multiple invocations ────────────────────

    @RepeatedTest(10)
    void minKeyIsRepeatableAndDeterministic() {
        int[] key = {4, 1, 7, 2, 9};
        Boolean[] mstSet = {false, false, false, false, false};

        int result = primMST.minKey(key, mstSet);

        assertEquals(1, result, "minKey must deterministically return index 1");
    }

    // ── R2: Position Independence (Parameterized) ───────────────────────

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4})
    void minKeyFindsMinimumAtAnyIndex(int targetIndex) {
        int[] key = {10, 10, 10, 10, 10};
        key[targetIndex] = 1; // Make targetIndex strictly the minimum
        Boolean[] mstSet = {false, false, false, false, false};

        int selected = primMST.minKey(key, mstSet);

        assertEquals(targetIndex, selected, "Must find minimum at index " + targetIndex);
    }

    // ── R3: All vertices already in MST ─────────────────────────────────

    @Test
    void minKeyReturnsMinusOneWhenAllInMST() {
        int[] key = {1, 2, 3, 4, 5};
        Boolean[] mstSet = {true, true, true, true, true};

        int result = primMST.minKey(key, mstSet);

        assertEquals(-1, result, "When all vertices are in mstSet, minKey must return -1");
    }

    // ── R4: All vertices have Integer.MAX_VALUE ─────────────────────────

    @Test
    void minKeyReturnsMinusOneWhenAllKeysAreMaxValue() {
        int[] key = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
        Boolean[] mstSet = {false, false, false, false, false};

        int result = primMST.minKey(key, mstSet);

        assertEquals(-1, result, "When all keys are Integer.MAX_VALUE, minKey must return -1");
    }

    // ── R5: Deterministic Tie-Breaking (Lowest Index Wins) ──────────────

    @Test
    void minKeyTieBreaksToLowestIndexDeterministically() {
        // Indices 1, 3, 4 share the minimum value 2
        int[] key = {5, 2, 8, 2, 2};
        Boolean[] mstSet = {false, false, false, false, false};

        // Run multiple times to verify tie-breaking stability
        for (int i = 0; i < 20; i++) {
            int result = primMST.minKey(key, mstSet);
            assertEquals(1, result, "Tie-breaking must consistently pick the lowest index (1)");
        }
    }

    // ── R6: Non-Mutation of Input Arrays ────────────────────────────────

    @Test
    void minKeyDoesNotMutateInputs() {
        int[] key = {3, 1, 4, 1, 5};
        int[] keyCopy = key.clone();
        Boolean[] mstSet = {false, false, true, false, false};
        Boolean[] mstSetCopy = mstSet.clone();

        primMST.minKey(key, mstSet);

        assertArrayEquals(keyCopy, key, "key array must not be modified by minKey");
        assertArrayEquals(mstSetCopy, mstSet, "mstSet array must not be modified by minKey");
    }

    // ── R7: Step-by-step MST Construction Simulation ────────────────────

    @Test
    void minKeySimulationMatchesExpectedMSTProgression() {
        int[] key = {0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
        Boolean[] mstSet = {false, false, false, false, false};

        // Step 1: index 0 has key 0
        int u1 = primMST.minKey(key, mstSet);
        assertEquals(0, u1);
        mstSet[u1] = true;

        // Simulate updating neighbors: vertex 1 with weight 2, vertex 3 with weight 6
        key[1] = 2;
        key[3] = 6;

        // Step 2: minimum among unvisited is vertex 1 (key 2)
        int u2 = primMST.minKey(key, mstSet);
        assertEquals(1, u2);
        mstSet[u2] = true;

        // Simulate updating neighbors: vertex 2 with weight 3, vertex 4 with weight 5
        key[2] = 3;
        key[4] = 5;

        // Step 3: minimum among unvisited is vertex 2 (key 3)
        int u3 = primMST.minKey(key, mstSet);
        assertEquals(2, u3);
        mstSet[u3] = true;

        // Step 4: minimum between vertex 4 (5) and vertex 3 (6) is vertex 4
        int u4 = primMST.minKey(key, mstSet);
        assertEquals(4, u4);
        mstSet[u4] = true;

        // Step 5: only vertex 3 remains
        int u5 = primMST.minKey(key, mstSet);
        assertEquals(3, u5);
        mstSet[u5] = true;

        // Step 6: no vertices remain
        int u6 = primMST.minKey(key, mstSet);
        assertEquals(-1, u6);
    }

    // ── R8: Single unvisited vertex ─────────────────────────────────────

    @Test
    void minKeyWithSingleUnvisitedVertex() {
        int[] key = {10, 20, 5, 40, 50};
        Boolean[] mstSet = {true, true, false, true, true};

        int result = primMST.minKey(key, mstSet);

        assertEquals(2, result, "Must select the single unvisited vertex at index 2");
    }
}
