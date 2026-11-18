package com.thealgorithms.backtracking;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CombinationTest {

    @Test
    void testNegativeElement() {
        Integer[] array = {1, 2};
        assertThrows(IllegalArgumentException.class, () -> { Combination.combination(array, -1); });
    }

    @Test
    void testNoElement() {
        List<TreeSet<Integer>> result = Combination.combination(new Integer[] {1, 2}, 0);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testLengthOne() {
        List<TreeSet<Integer>> result = Combination.combination(new Integer[] {1, 2}, 1);
        assertEquals(1, result.get(0).iterator().next());
        assertEquals(2, result.get(1).iterator().next());
    }

    @Test
    void testLengthTwo() {
        List<TreeSet<Integer>> result = Combination.combination(new Integer[] {1, 2}, 2);
        Integer[] arr = result.get(0).toArray(new Integer[2]);
        assertEquals(1, arr[0]);
        assertEquals(2, arr[1]);
    }

    @Test
    void testCombinationsWithStrings() {
        List<TreeSet<String>> result = Combination.combination(new String[] {"a", "b", "c"}, 2);
        assertEquals(3, result.size());
        assertTrue(result.contains(new TreeSet<>(Set.of("a", "b"))));
        assertTrue(result.contains(new TreeSet<>(Set.of("a", "c"))));
        assertTrue(result.contains(new TreeSet<>(Set.of("b", "c"))));
    }

    private static int binomial(int m, int n) {
        if (n < 0 || n > m) {
            return 0;
        }
        int k = Math.min(n, m - n);
        int res = 1;
        for (int i = 1; i <= k; i++) {
            res = res * (m - k + i) / i;
        }
        return res;
    }

    private static int compareTreeSets(TreeSet<Integer> a, TreeSet<Integer> b) {
        Iterator<Integer> itA = a.iterator();
        Iterator<Integer> itB = b.iterator();
        while (itA.hasNext() && itB.hasNext()) {
            int cmp = Integer.compare(itA.next(), itB.next());
            if (cmp != 0) {
                return cmp;
            }
        }
        return Integer.compare(a.size(), b.size());
    }

    @Test
    @DisplayName("Invariant fixture: smallest valid input and ordinary unsorted input")
    void testFixtureSmallestAndOrdinaryInvariants() {
        // Preconditions from Combination.java (pinned lines):
        // line 24 (n >= 0), lines 28-30 (n == 0 -> empty list),
        // lines 31-32 (arr cloned before Arrays.sort -> no input mutation),
        // line 49 (n > arr.length -> empty list).
        Integer[] smallestEmpty = new Integer[] {};
        List<TreeSet<Integer>> emptyRes = Combination.combination(smallestEmpty, 0);
        assertEquals(List.of(), emptyRes);

        Integer[] smallestOne = new Integer[] {42};
        List<TreeSet<Integer>> singleRes = Combination.combination(smallestOne, 1);
        List<TreeSet<Integer>> expectedSingle = List.of(new TreeSet<>(Set.of(42)));
        assertEquals(expectedSingle, singleRes);
        assertArrayEquals(new Integer[] {42}, smallestOne);

        Integer[] ordinary = new Integer[] {3, 1, 2};
        Integer[] ordinarySnapshot = ordinary.clone();
        List<TreeSet<Integer>> ordinaryRes = Combination.combination(ordinary, 2);
        List<TreeSet<Integer>> expectedOrdinary = List.of(
            new TreeSet<>(Set.of(1, 2)),
            new TreeSet<>(Set.of(1, 3)),
            new TreeSet<>(Set.of(2, 3))
        );
        assertEquals(expectedOrdinary, ordinaryRes);
        assertArrayEquals(ordinarySnapshot, ordinary, "Input array must not be mutated");

        // Fresh equivalent (reordered) input produces identical result
        List<TreeSet<Integer>> equivalentRes = Combination.combination(new Integer[] {2, 3, 1}, 2);
        assertEquals(expectedOrdinary, equivalentRes);

        System.out.println("CASE|smallest_empty|arr=[],n=0|expected=[]|actual=" + emptyRes);
        System.out.println("CASE|smallest_single|arr=[42],n=1|expected=[[42]]|actual=" + singleRes);
        System.out.println("CASE|ordinary_unsorted|arr=[3, 1, 2],n=2|expected=[[1, 2], [1, 3], [2, 3]]|actual=" + ordinaryRes);
    }

    @Test
    @DisplayName("Invariant property: 120 deterministic seeded cases preserve shape, cardinality, ordering, and immutability")
    void testSeededPropertyInvariants() {
        // Fixed seed for deterministic bounded generation over distinct comparable integers.
        Random rng = new Random(45034L);
        int totalCases = 120;

        for (int c = 0; c < totalCases; c++) {
            int m = rng.nextInt(9); // m in [0, 8]
            int n = rng.nextInt(m + 2); // n in [0, m + 1]

            List<Integer> pool = new ArrayList<>();
            for (int v = -10; v <= 20; v++) {
                pool.add(v);
            }
            Collections.shuffle(pool, rng);
            Integer[] input = pool.subList(0, m).toArray(new Integer[0]);
            Integer[] snapshot = input.clone();

            List<Integer> permutedList = new ArrayList<>(Arrays.asList(input));
            Collections.shuffle(permutedList, rng);
            Integer[] equivalentInput = permutedList.toArray(new Integer[0]);

            List<TreeSet<Integer>> result = Combination.combination(input, n);
            List<TreeSet<Integer>> equivalentResult = Combination.combination(equivalentInput, n);

            // 1. Input immutability
            assertArrayEquals(snapshot, input, "Case " + c + ": input array mutated");

            // 2. Fresh equivalent input yields identical output
            assertEquals(result, equivalentResult, "Case " + c + ": equivalent reordered input diverged");

            // 3. Exact cardinality
            int expectedCount = (n == 0 || n > m) ? 0 : binomial(m, n);
            assertEquals(expectedCount, result.size(), "Case " + c + ": expected C(" + m + "," + n + ")=" + expectedCount);

            // 4. Membership, size, uniqueness, and lexicographical ordering
            Set<Integer> domain = new HashSet<>(Arrays.asList(snapshot));
            Set<TreeSet<Integer>> seen = new HashSet<>();
            TreeSet<Integer> prev = null;
            for (TreeSet<Integer> combo : result) {
                assertEquals(n, combo.size(), "Case " + c + ": combination size must equal n");
                assertTrue(domain.containsAll(combo), "Case " + c + ": combination contains element outside input");
                assertTrue(seen.add(combo), "Case " + c + ": duplicate combination detected");
                if (prev != null) {
                    assertTrue(compareTreeSets(prev, combo) < 0, "Case " + c + ": combinations must be in strict lexicographical order");
                }
                prev = combo;
            }
        }

        System.out.println("CASE|seeded_property_120|seed=45034,cases=120,m=0..8,n=0..m+1|expected=all_120_pass|actual=all_120_pass");
    }
}
