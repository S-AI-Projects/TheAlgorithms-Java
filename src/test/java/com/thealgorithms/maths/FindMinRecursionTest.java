package com.thealgorithms.maths;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class FindMinRecursionTest {

    @ParameterizedTest
    @MethodSource("inputStream")
    void numberTests(int expected, int[] input) {
        Assertions.assertEquals(expected, FindMinRecursion.min(input));
    }

    private static Stream<Arguments> inputStream() {
        return Stream.of(Arguments.of(5, new int[] {5, 5, 5, 5, 5}), Arguments.of(-1, new int[] {-1, 0}), Arguments.of(-10, new int[] {-10, -9, -8, -7, -6, -5, -4, -3, -2, -1}), Arguments.of(-4, new int[] {3, -2, 3, 9, -4, -4, 8}), Arguments.of(3, new int[] {3}));
    }

    @Test
    public void testFindMaxThrowsExceptionForEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> FindMinRecursion.min(new int[] {}));
    }

    @Test
    @DisplayName("Determinism: smallest valid input repeats identically across fresh and interleaved runs")
    void testSmallestValidInputRepeatabilityAndIsolation() {
        // Preconditions from FindMinRecursion.java (pinned lines):
        // line 18 (non-empty array), line 21 (low == high base case), line 40 (full-range wrapper 0..length-1).
        // External dependencies: none (pure function).
        int[] first = new int[] {7};
        int[] second = new int[] {7};
        int[] intervening = new int[] {-100, 50, 0};
        int[] third = new int[] {7};

        int run1 = FindMinRecursion.min(first);
        int run2 = FindMinRecursion.min(second);
        int other = FindMinRecursion.min(intervening);
        int run3 = FindMinRecursion.min(third);
        int rangeRun = FindMinRecursion.min(new int[] {7}, 0, 0);

        assertEquals(7, run1);
        assertEquals(7, run2);
        assertEquals(-100, other);
        assertEquals(7, run3);
        assertEquals(7, rangeRun);
        assertArrayEquals(new int[] {7}, first, "Input array must not be mutated");
        assertArrayEquals(new int[] {7}, third, "Input array must not be mutated");

        System.out.println("CASE|smallest_repeatability|input=[7],intervening=[-100, 50, 0]|expected=run1:7,run2:7,other:-100,run3:7,range:7|actual=run1:" + run1 + ",run2:" + run2 + ",other:" + other + ",run3:" + run3 + ",range:" + rangeRun);
    }

    @Test
    @DisplayName("Determinism: ordinary input repeats identically after intervening call and preserves caller array")
    void testOrdinaryInputRepeatabilityAndIsolation() {
        // Preconditions from FindMinRecursion.java (pinned lines):
        // line 18 (array.length > 0), lines 25-28 (0 <= low <= high < array.length, midpoint split),
        // line 30 (Math.min of left and right halves).
        // External dependencies: none (pure function).
        int[] first = new int[] {4, -2, 9, -7, 3};
        int[] snapshot = first.clone();
        int[] second = new int[] {4, -2, 9, -7, 3};
        int[] intervening = new int[] {Integer.MIN_VALUE, 12, 99};
        int[] third = new int[] {4, -2, 9, -7, 3};

        int run1 = FindMinRecursion.min(first);
        int run2 = FindMinRecursion.min(second);
        int other = FindMinRecursion.min(intervening);
        int run3 = FindMinRecursion.min(third);
        int subrange = FindMinRecursion.min(first, 0, 2);

        assertEquals(-7, run1);
        assertEquals(-7, run2);
        assertEquals(Integer.MIN_VALUE, other);
        assertEquals(-7, run3);
        assertEquals(-2, subrange);
        assertArrayEquals(snapshot, first, "Input array must not be mutated");
        assertArrayEquals(snapshot, second, "Second input array must not be mutated");
        assertArrayEquals(snapshot, third, "Third input array must not be mutated");

        System.out.println("CASE|ordinary_repeatability|input=" + Arrays.toString(snapshot) + ",subrange=[0,2]|expected=run1:-7,run2:-7,run3:-7,subrange:-2|actual=run1:" + run1 + ",run2:" + run2 + ",run3:" + run3 + ",subrange:" + subrange);
    }
}
