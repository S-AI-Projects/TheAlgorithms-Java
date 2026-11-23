package com.thealgorithms.greedyalgorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Invariant tests for {@link ActivitySelection}.
 *
 * These tests formally verify the fundamental invariants that must always hold
 * for any execution of the activity selection algorithm:
 *
 * <ul>
 *   <li>INV-1 (Non-Overlapping): No two selected activities overlap in time.
 *       For consecutively selected activities in chronological order,
 *       start(k+1) >= end(k).</li>
 *   <li>INV-2 (Index Validity & Uniqueness): Every selected index is in [0, n-1],
 *       and no index appears more than once.</li>
 *   <li>INV-3 (End-Time Monotonicity): In greedy selection order, end times
 *       are non-decreasing.</li>
 *   <li>INV-4 (Cardinality Bounds): 1 <= selected.size() <= n for any n >= 1.</li>
 *   <li>INV-5 (Complete Selection for Disjoint Activities): If all activities
 *       are mutually non-overlapping, all n activities must be selected.</li>
 *   <li>INV-6 (Singleton Selection for Mutually Overlapping Activities):
 *       If all activities overlap with one another, exactly 1 activity is selected.</li>
 * </ul>
 *
 * Fixes: #24
 */
public class ActivitySelectionInvariantTest {

    /**
     * Helper to verify that no two activities in the selected list overlap.
     */
    private void assertNoOverlap(int[] startTimes, int[] endTimes, ArrayList<Integer> selected) {
        for (int i = 0; i < selected.size(); i++) {
            for (int j = i + 1; j < selected.size(); j++) {
                int idxA = selected.get(i);
                int idxB = selected.get(j);

                int startA = startTimes[idxA];
                int endA = endTimes[idxA];
                int startB = startTimes[idxB];
                int endB = endTimes[idxB];

                // Two intervals [startA, endA) and [startB, endB) overlap if
                // max(startA, startB) < min(endA, endB)
                boolean overlap = Math.max(startA, startB) < Math.min(endA, endB);
                assertFalse(overlap, String.format(
                        "Activities %d [%d, %d) and %d [%d, %d) must not overlap",
                        idxA, startA, endA, idxB, startB, endB));
            }
        }
    }

    /**
     * Helper to verify that all indices are valid and distinct.
     */
    private void assertValidAndUniqueIndices(int n, ArrayList<Integer> selected) {
        Set<Integer> seen = new HashSet<>();
        for (int idx : selected) {
            assertTrue(idx >= 0 && idx < n,
                    "Selected index " + idx + " must be in range [0, " + (n - 1) + "]");
            assertTrue(seen.add(idx), "Duplicate activity index selected: " + idx);
        }
    }

    // ── INV-1: Non-Overlapping Invariant ────────────────────────────────

    @Test
    void invariantNonOverlappingClassicDataset() {
        int[] start = {1, 3, 0, 5, 8, 5};
        int[] end = {2, 4, 6, 7, 9, 9};

        ArrayList<Integer> selected = ActivitySelection.activitySelection(start, end);

        assertNoOverlap(start, end, selected);
    }

    @Test
    void invariantNonOverlappingRandomizedIntervals() {
        int[] start = {10, 12, 20, 15, 25, 30, 35, 40};
        int[] end = {20, 25, 30, 28, 35, 45, 40, 50};

        ArrayList<Integer> selected = ActivitySelection.activitySelection(start, end);

        assertNoOverlap(start, end, selected);
    }

    // ── INV-2: Index Validity & Uniqueness ───────────────────────────────

    @Test
    void invariantIndicesAreValidAndDistinct() {
        int[] start = {1, 3, 0, 5, 8, 5};
        int[] end = {2, 4, 6, 7, 9, 9};

        ArrayList<Integer> selected = ActivitySelection.activitySelection(start, end);

        assertValidAndUniqueIndices(start.length, selected);
    }

    // ── INV-3: End-Time Monotonicity ────────────────────────────────────

    @Test
    void invariantEndTimesAreNonDecreasing() {
        int[] start = {1, 3, 0, 5, 8, 5};
        int[] end = {2, 4, 6, 7, 9, 9};

        ArrayList<Integer> selected = ActivitySelection.activitySelection(start, end);

        for (int i = 1; i < selected.size(); i++) {
            int prevEnd = end[selected.get(i - 1)];
            int currEnd = end[selected.get(i)];
            assertTrue(currEnd >= prevEnd,
                    "End times of selected activities must be monotonically non-decreasing");
        }
    }

    // ── INV-4: Cardinality Bounds ───────────────────────────────────────

    @Test
    void invariantCardinalityBounds() {
        int[] start = {1, 2, 3, 4, 5};
        int[] end = {3, 4, 5, 6, 7};

        ArrayList<Integer> selected = ActivitySelection.activitySelection(start, end);

        assertTrue(selected.size() >= 1, "Must select at least 1 activity");
        assertTrue(selected.size() <= start.length, "Cannot select more than n activities");
    }

    // ── INV-5: All Mutually Disjoint Activities Selected ────────────────

    @Test
    void invariantAllSelectedWhenNoOverlapsExist() {
        // Activities: [0, 2), [2, 4), [4, 6), [6, 8), [8, 10)
        int[] start = {0, 2, 4, 6, 8};
        int[] end = {2, 4, 6, 8, 10};

        ArrayList<Integer> selected = ActivitySelection.activitySelection(start, end);

        assertEquals(5, selected.size(), "When all activities are disjoint, all must be selected");
        assertNoOverlap(start, end, selected);
        assertValidAndUniqueIndices(start.length, selected);
    }

    // ── INV-6: Exactly One Selected When All Mutually Overlap ───────────

    @Test
    void invariantSingleSelectedWhenAllMutuallyOverlap() {
        // All activities span across timestamp 5: [0, 10), [1, 9), [2, 8), [3, 7)
        int[] start = {0, 1, 2, 3};
        int[] end = {10, 9, 8, 7};

        ArrayList<Integer> selected = ActivitySelection.activitySelection(start, end);

        assertEquals(1, selected.size(), "When all activities conflict, only 1 must be selected");
        assertNoOverlap(start, end, selected);
    }

    // ── Edge: Single Activity Invariant ─────────────────────────────────

    @Test
    void invariantSingleActivitySelected() {
        int[] start = {5};
        int[] end = {10};

        ArrayList<Integer> selected = ActivitySelection.activitySelection(start, end);

        assertEquals(1, selected.size());
        assertEquals(0, selected.get(0));
        assertNoOverlap(start, end, selected);
    }

    // ── Edge: Adjacent boundary sharing start == end is valid non-overlap ─

    @Test
    void invariantBackToBackActivitiesDoNotOverlap() {
        // Activity 1 ends at 5, Activity 2 starts at 5 -> valid non-overlap
        int[] start = {0, 5};
        int[] end = {5, 10};

        ArrayList<Integer> selected = ActivitySelection.activitySelection(start, end);

        assertEquals(2, selected.size(), "Consecutive activities touching at boundary must both be selected");
        assertNoOverlap(start, end, selected);
    }
}
