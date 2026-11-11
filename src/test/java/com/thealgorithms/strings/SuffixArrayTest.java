package com.thealgorithms.strings;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Test;

class SuffixArrayTest {

    @Test
    void testEmptyString() {
        int[] result = SuffixArray.buildSuffixArray("");
        assertArrayEquals(new int[] {}, result, "Empty string should return empty suffix array");
    }

    @Test
    void testSingleCharacter() {
        int[] result = SuffixArray.buildSuffixArray("a");
        assertArrayEquals(new int[] {0}, result, "Single char string should return [0]");
    }

    @Test
    void testDistinctCharacters() {
        int[] result = SuffixArray.buildSuffixArray("abc");
        assertArrayEquals(new int[] {0, 1, 2}, result, "Suffixes already in order for distinct chars");
    }

    @Test
    void testBananaExample() {
        int[] result = SuffixArray.buildSuffixArray("banana");
        assertArrayEquals(new int[] {5, 3, 1, 0, 4, 2}, result, "Suffix array of 'banana' should be [5,3,1,0,4,2]");
    }

    @Test
    void testStringWithDuplicates() {
        int[] result = SuffixArray.buildSuffixArray("aaaa");
        assertArrayEquals(new int[] {3, 2, 1, 0}, result, "Suffix array should be descending indices for 'aaaa'");
    }

    @Test
    void testRandomString() {
        int[] result = SuffixArray.buildSuffixArray("mississippi");
        assertArrayEquals(new int[] {10, 7, 4, 1, 0, 9, 8, 6, 3, 5, 2}, result, "Suffix array for 'mississippi' should match expected");
    }

    /**
     * Smallest valid input fixture: empty string "".
     * Preconditions established at:
     * - SuffixArray.java line 54 (int n = text.length(); n == 0)
     * - SuffixArray.java lines 60-93 (loops do not run for n = 0)
     * - SuffixArray.java line 94 (returns empty int[] array)
     */
    @Test
    void testSmallestValidInputFixture() {
        String text = "";
        int[] expected = new int[] {};

        int[] actual = SuffixArray.buildSuffixArray(text);
        assertArrayEquals(expected, actual, "Smallest valid input (empty string) must return an empty array.");
    }

    /**
     * Ordinary input & documented worked example fixture: text = "banana".
     * Validates intermediate state transitions and asserts sorted suffixes.
     * Preconditions established at:
     * - SuffixArray.java line 54 (n = 6)
     * - SuffixArray.java lines 60-64 (initial ASCII ranks: [98, 97, 110, 97, 110, 97])
     * - SuffixArray.java lines 66-93 (doubling step k=1, then k=2 where all ranks become unique)
     * - SuffixArray.java lines 90-92 (decisive transition: rank[suffixArray[5]] == 5 triggers early break)
     */
    @Test
    void testDocumentedWorkedExampleBananaExecutionAndDisplayedOutput() {
        String text = "banana";
        int[] expectedSuffixArray = new int[] {5, 3, 1, 0, 4, 2};

        int[] actualSuffixArray = SuffixArray.buildSuffixArray(text);
        assertArrayEquals(expectedSuffixArray, actualSuffixArray, "Documented example 'banana' must produce [5, 3, 1, 0, 4, 2].");

        // Verify that the resulting suffix substrings are in strictly increasing lexicographical order
        String[] expectedSuffixStrings = new String[] {
            "a", "ana", "anana", "banana", "na", "nana"
        };
        assertEquals(expectedSuffixStrings.length, actualSuffixArray.length);

        for (int i = 0; i < actualSuffixArray.length; i++) {
            final int rankIndex = i;
            String suffix = text.substring(actualSuffixArray[i]);
            assertEquals(expectedSuffixStrings[i], suffix, () -> "Suffix at rank " + rankIndex + " must match expected substring.");
            if (i > 0) {
                String prevSuffix = text.substring(actualSuffixArray[i - 1]);
                assertTrue(prevSuffix.compareTo(suffix) < 0, "Suffixes must be in strictly increasing lexicographical order.");
            }
        }
    }

    /**
     * Boundary fixture: null input precondition check.
     * Preconditions established at:
     * - SuffixArray.java line 54 (text.length() dereference throws NullPointerException)
     */
    @Test
    void testNullInputThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> SuffixArray.buildSuffixArray(null));
    }

    /**
     * Reflection fixture for private constructor contract.
     * Preconditions established at:
     * - SuffixArray.java lines 46-47
     */
    @Test
    void testPrivateConstructorContract() throws Exception {
        Constructor<SuffixArray> constructor = SuffixArray.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        SuffixArray instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
