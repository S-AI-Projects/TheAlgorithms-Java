package com.thealgorithms.others;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class BoyerMooreTest {

    @ParameterizedTest
    @MethodSource("inputStreamWithExistingMajority")
    void checkWhenMajorityExists(int expected, int[] input) {
        Assertions.assertEquals(expected, BoyerMoore.findMajorityElement(input).get());
    }

    private static Stream<Arguments> inputStreamWithExistingMajority() {
        return Stream.of(Arguments.of(5, new int[] {5, 5, 5, 2}), Arguments.of(10, new int[] {10, 10, 20}), Arguments.of(10, new int[] {10, 20, 10}), Arguments.of(10, new int[] {20, 10, 10}), Arguments.of(4, new int[] {1, 4, 2, 4, 4, 5, 4}), Arguments.of(-1, new int[] {-1}));
    }

    @ParameterizedTest
    @MethodSource("inputStreamWithoutMajority")
    void checkWhenMajorityExists(int[] input) {
        Assertions.assertFalse(BoyerMoore.findMajorityElement(input).isPresent());
    }

    private static Stream<Arguments> inputStreamWithoutMajority() {
        return Stream.of(Arguments.of(new int[] {10, 10, 20, 20, 30, 30}), Arguments.of(new int[] {10, 20, 30, 40, 50}), Arguments.of(new int[] {1, 2}), Arguments.of(new int[] {}));
    }

    @Test
    @DisplayName("Mutation-resistant contract: smallest inputs, strict-majority boundary tie, ordinary input, and immutability")
    void testStrictMajorityBoundaryAndNullContractDetectsMutants() {
        // Source lines establishing preconditions and protected conditions (BoyerMoore.java pinned lines):
        // line 23 (array == null || array.length == 0 -> Optional.empty()),
        // lines 45-51 (findCandidate voting & reset when count == 0),
        // lines 63-68 (countOccurrences verification),
        // line 79 (isMajority strict inequality: 2 * count > totalCount).

        // Smallest valid inputs: null, empty, and single-element
        Optional<Integer> nullActual = BoyerMoore.findMajorityElement(null);
        assertEquals(Optional.empty(), nullActual, "null input must return Optional.empty()");

        int[] empty = new int[] {};
        Optional<Integer> emptyActual = BoyerMoore.findMajorityElement(empty);
        assertEquals(Optional.empty(), emptyActual, "empty input must return Optional.empty()");

        int[] single = new int[] {7};
        Optional<Integer> singleActual = BoyerMoore.findMajorityElement(single);
        assertEquals(Optional.of(7), singleActual, "single-element [7] has strict majority 7");
        assertArrayEquals(new int[] {7}, single, "single-element input must not be mutated");

        // Minimal strict-majority tie (count == n / 2 -> 2 * count == totalCount):
        // Detects line 79 mutant (2 * count >= totalCount).
        int[] minimalTie = new int[] {1, 2};
        Optional<Integer> minimalTieActual = BoyerMoore.findMajorityElement(minimalTie);
        assertEquals(Optional.empty(), minimalTieActual, "[1, 2] has no strict majority (> 2/2)");
        assertArrayEquals(new int[] {1, 2}, minimalTie, "minimalTie must not be mutated");

        int[] fourTie = new int[] {4, 5, 4, 5};
        Optional<Integer> fourTieActual = BoyerMoore.findMajorityElement(fourTie);
        assertEquals(Optional.empty(), fourTieActual, "[4, 5, 4, 5] has count 2 == 4/2, not a strict majority");

        // Ordinary input with candidate cancellation/resets + fresh equivalent copy + immutability
        int[] ordinary = new int[] {2, 1, 2, 3, 2};
        int[] snapshot = ordinary.clone();
        Optional<Integer> ordinaryActual = BoyerMoore.findMajorityElement(ordinary);
        Optional<Integer> freshEquivalent = BoyerMoore.findMajorityElement(new int[] {2, 1, 2, 3, 2});
        assertEquals(Optional.of(2), ordinaryActual, "[2, 1, 2, 3, 2] has strict majority 2 (count 3 > 5/2)");
        assertEquals(ordinaryActual, freshEquivalent, "Fresh equivalent input must return identical result");
        assertArrayEquals(snapshot, ordinary, "Ordinary input array must not be mutated");

        System.out.println("CASE|smallest_null|input=null|expected=Optional.empty|actual=" + nullActual);
        System.out.println("CASE|smallest_empty|input=[]|expected=Optional.empty|actual=" + emptyActual);
        System.out.println("CASE|smallest_single|input=[7]|expected=Optional[7]|actual=" + singleActual);
        System.out.println("CASE|minimal_tie|input=[1, 2]|expected=Optional.empty|actual=" + minimalTieActual);
        System.out.println("CASE|even_tie|input=[4, 5, 4, 5]|expected=Optional.empty|actual=" + fourTieActual);
        System.out.println("CASE|ordinary_majority|input=" + Arrays.toString(snapshot) + "|expected=Optional[2]|actual=" + ordinaryActual);
    }
}
