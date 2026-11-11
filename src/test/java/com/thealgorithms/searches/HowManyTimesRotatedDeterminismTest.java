package com.thealgorithms.searches;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * Determinism checks for {@link HowManyTimesRotated#rotated(int[])}.
 *
 * <p>Source facts (HowManyTimesRotated.java):
 * <ul>
 * <li>line 13: the array cannot contain duplicates (precondition for every fixture below)</li>
 * <li>line 45: {@code rotated(int[] a)} is the only logic; its sole input is {@code a}</li>
 * <li>lines 46-47: {@code low = 0}, {@code high = a.length - 1} are local state</li>
 * <li>line 49: the loop is skipped when {@code a.length == 1}, so line 58 returns 0</li>
 * <li>line 58: the result is the index of the minimum element</li>
 * </ul>
 * Expected values are calculated by hand (index of the minimum), not by calling the method.
 */
public class HowManyTimesRotatedDeterminismTest {

    // Smallest valid input. Precondition: line 49 loop is not entered when length == 1.
    private static int[] smallest() {
        return new int[] {7};
    }

    // Ordinary input. Precondition: distinct values (line 13); minimum 2 is at index 2.
    private static int[] ordinary() {
        return new int[] {15, 17, 2, 3, 5};
    }

    // Another valid input. Precondition: distinct values (line 13); minimum 2 is at index 4.
    private static int[] other() {
        return new int[] {11, 12, 15, 18, 2, 5, 6, 8};
    }

    @Test
    public void smallestInputRepeatedCallsGiveSameResult() {
        assertEquals(0, HowManyTimesRotated.rotated(smallest()));
        assertEquals(0, HowManyTimesRotated.rotated(smallest()));
    }

    @Test
    public void ordinaryInputRepeatedCallsGiveSameResult() {
        assertEquals(2, HowManyTimesRotated.rotated(ordinary()));
        assertEquals(2, HowManyTimesRotated.rotated(ordinary()));
    }

    @Test
    public void earlierCallsDoNotAffectLaterResult() {
        assertEquals(2, HowManyTimesRotated.rotated(ordinary()));
        assertEquals(4, HowManyTimesRotated.rotated(other()));
        assertEquals(0, HowManyTimesRotated.rotated(smallest()));
        // Same ordinary fixture again, after other valid inputs ran in between.
        assertEquals(2, HowManyTimesRotated.rotated(ordinary()));
    }

    @Test
    public void inputArrayIsNotMutated() {
        int[] input = ordinary();
        int[] expectedContents = {15, 17, 2, 3, 5};
        HowManyTimesRotated.rotated(input);
        assertArrayEquals(expectedContents, input);

        int[] single = smallest();
        HowManyTimesRotated.rotated(single);
        assertArrayEquals(new int[] {7}, single);
    }

    @Test
    public void sameArrayInstanceCanBeReused() {
        int[] shared = ordinary();
        assertEquals(2, HowManyTimesRotated.rotated(shared));
        assertEquals(2, HowManyTimesRotated.rotated(shared));
    }

    /** rotated() has no external dependency: it must not read stdin (only main() does, lines 34-39). */
    @Test
    public void resultDoesNotDependOnStandardInput() {
        InputStream original = System.in;
        try {
            System.setIn(new ByteArrayInputStream(new byte[0]));
            assertEquals(2, HowManyTimesRotated.rotated(ordinary()));
            System.setIn(new ByteArrayInputStream("999 1 2 3".getBytes()));
            assertEquals(2, HowManyTimesRotated.rotated(ordinary()));
        } finally {
            System.setIn(original);
        }
    }
}

