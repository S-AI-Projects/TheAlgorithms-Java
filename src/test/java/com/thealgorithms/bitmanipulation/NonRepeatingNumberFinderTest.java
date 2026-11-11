package com.thealgorithms.bitmanipulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test case for Non Repeating Number Finder.
 * Validates the functionality and refactored XOR reduction responsibility.
 *
 * @author Bama Charan Chhandogi (https://github.com/BamaCharanChhandogi)
 */
class NonRepeatingNumberFinderTest {

    @ParameterizedTest
    @MethodSource("testCases")
    void testNonRepeatingNumberFinder(int[] arr, int expected) {
        assertEquals(expected, NonRepeatingNumberFinder.findNonRepeatingNumber(arr));
    }

    private static Arguments[] testCases() {
        return new Arguments[] {
            Arguments.of(new int[] {1, 2, 1, 2, 6}, 6),
            Arguments.of(new int[] {1, 2, 1, 2}, 0), // All numbers repeat
            Arguments.of(new int[] {12}, 12), // Single non-repeating number
            Arguments.of(new int[] {3, 5, 3, 4, 4}, 5), // More complex case
            Arguments.of(new int[] {7, 8, 7, 9, 8, 10, 10}, 9), // Non-repeating in the middle
            Arguments.of(new int[] {0, -1, 0, -1, 2}, 2), // Testing with negative numbers
            Arguments.of(new int[] {Integer.MAX_VALUE, 1, 1}, Integer.MAX_VALUE), // Edge case with max int
            Arguments.of(new int[] {2, 2, 3, 3, 4, 5, 4}, 5), // Mixed duplicates
            Arguments.of(new int[] {}, 0) // Edge case: empty array
        };
    }

    /**
     * Smallest valid input fixture: empty array.
     * Preconditions established at:
     * - NonRepeatingNumberFinder.java line 26 (@return ... or 0 if the array is empty)
     * - NonRepeatingNumberFinder.java line 39 (xorSum initialized to 0)
     */
    @Test
    void testSmallestValidInputEmptyArrayFixture() {
        int[] arr = new int[] {};
        int expected = 0;

        int actual = NonRepeatingNumberFinder.findNonRepeatingNumber(arr);
        assertEquals(expected, actual, "Empty array must return 0 per documented specification.");
    }

    /**
     * Smallest valid non-empty input fixture: single element array.
     * Preconditions established at:
     * - NonRepeatingNumberFinder.java line 29 (findNonRepeatingNumber delegates to reduceXor)
     * - NonRepeatingNumberFinder.java lines 39-43 (single loop iteration: 0 ^ 12 = 12)
     */
    @Test
    void testSmallestValidInputSingleElementFixture() {
        int[] arr = new int[] {12};
        int expected = 12;

        int actual = NonRepeatingNumberFinder.findNonRepeatingNumber(arr);
        assertEquals(expected, actual, "Single-element array must return the element itself.");
    }

    /**
     * Ordinary input fixture: [2, 3, 5, 2, 3] from class documentation example.
     * Step-by-step XOR reduction trace:
     * 0 ^ 2 = 2
     * 2 ^ 3 = 1
     * 1 ^ 5 = 4
     * 4 ^ 2 = 6
     * 6 ^ 3 = 5
     * Preconditions established at:
     * - NonRepeatingNumberFinder.java lines 29-31
     * - NonRepeatingNumberFinder.java lines 39-44 (reduceXor loop)
     */
    @Test
    void testOrdinaryInputDocumentationFixture() {
        int[] arr = new int[] {2, 3, 5, 2, 3};
        int expected = 5;

        int actual = NonRepeatingNumberFinder.findNonRepeatingNumber(arr);
        assertEquals(expected, actual, "Must return 5 as the unique non-repeating element.");
    }

    /**
     * Domain boundary fixture: negative numbers and cancellation.
     * Preconditions established at:
     * - NonRepeatingNumberFinder.java lines 39-43
     */
    @Test
    void testDomainBoundaryNegativeNumbers() {
        int[] arr = new int[] {-42, 99, -42};
        int expected = 99;

        assertEquals(expected, NonRepeatingNumberFinder.findNonRepeatingNumber(arr));
    }

    /**
     * Boundary fixture: null input precondition.
     * Preconditions established at:
     * - NonRepeatingNumberFinder.java line 40 (looping over null array triggers NullPointerException)
     */
    @Test
    void testNullArrayThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> NonRepeatingNumberFinder.findNonRepeatingNumber(null));
    }

    /**
     * Reflection fixture for private constructor contract.
     * Preconditions established at:
     * - NonRepeatingNumberFinder.java lines 19-20
     */
    @Test
    void testPrivateConstructorContract() throws Exception {
        Constructor<NonRepeatingNumberFinder> constructor = NonRepeatingNumberFinder.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        NonRepeatingNumberFinder instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
