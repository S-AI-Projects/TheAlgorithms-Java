package com.thealgorithms.dynamicprogramming;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit and example verification tests for {@link ClimbingStairs}.
 *
 * Fixes: #27
 */
public class ClimbingStairsTest {

    @Test
    void testBaseCases() {
        assertEquals(0, ClimbingStairs.numberOfWays(0), "0 steps should have 0 ways");
        assertEquals(1, ClimbingStairs.numberOfWays(1), "1 step should have 1 way");
    }

    @ParameterizedTest
    @CsvSource({
        "2, 2",
        "3, 3",
        "4, 5",
        "5, 8",
        "6, 13",
        "7, 21",
        "8, 34",
        "9, 55",
        "10, 89"
    })
    void testKnownValues(int n, int expectedWays) {
        assertEquals(expectedWays, ClimbingStairs.numberOfWays(n),
                String.format("For n = %d, expected %d ways", n, expectedWays));
    }

    @Test
    void testFibonacciRecurrenceInvariant() {
        // For any n >= 3: numberOfWays(n) == numberOfWays(n-1) + numberOfWays(n-2)
        for (int n = 3; n <= 25; n++) {
            int waysN = ClimbingStairs.numberOfWays(n);
            int waysNMinus1 = ClimbingStairs.numberOfWays(n - 1);
            int waysNMinus2 = ClimbingStairs.numberOfWays(n - 2);
            assertEquals(waysNMinus1 + waysNMinus2, waysN,
                    "Recurrence relation ways(n) = ways(n-1) + ways(n-2) must hold for n = " + n);
        }
    }

    @Test
    void testLargerInput() {
        // n = 30 -> 1346269
        assertEquals(1346269, ClimbingStairs.numberOfWays(30));
    }

    @Test
    void testMainMethodExecutesWithoutError() {
        assertDoesNotThrow(() -> ClimbingStairs.main(new String[0]),
                "The runnable main demonstration should execute without exceptions");
    }
}
