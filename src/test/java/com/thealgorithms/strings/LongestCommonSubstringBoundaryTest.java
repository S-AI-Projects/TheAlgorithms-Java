package com.thealgorithms.strings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import org.junit.jupiter.api.Test;

/**
 * Boundary tests for {@link LongestCommonSubstring}.
 *
 * These tests define and verify the supported boundary conditions of the
 * longestCommonSubstring() method:
 *
 * <ul>
 *   <li>B1: Both inputs null → returns ""</li>
 *   <li>B2: One input null → returns ""</li>
 *   <li>B3: One or both inputs empty → returns ""</li>
 *   <li>B4: Single-character inputs (match and mismatch)</li>
 *   <li>B5: Identical strings → returns the entire string</li>
 *   <li>B6: Completely disjoint strings → returns ""</li>
 *   <li>B7: Match at the very start of both strings</li>
 *   <li>B8: Match at the very end of both strings</li>
 *   <li>B9: One string is a complete substring of the other</li>
 *   <li>B10: Maximum-length boundary stress test (large inputs)</li>
 * </ul>
 *
 * Fixes: #22
 */
public class LongestCommonSubstringBoundaryTest {

    // ── B1: Both inputs null ───────────────────────────────────────────

    @Test
    void boundaryBothNull() {
        assertEquals("", LongestCommonSubstring.longestCommonSubstring(null, null));
    }

    // ── B2: One input null ─────────────────────────────────────────────

    @Test
    void boundaryFirstNull() {
        assertEquals("", LongestCommonSubstring.longestCommonSubstring(null, "hello"));
    }

    @Test
    void boundarySecondNull() {
        assertEquals("", LongestCommonSubstring.longestCommonSubstring("hello", null));
    }

    // ── B3: Empty string inputs ────────────────────────────────────────

    @Test
    void boundaryBothEmpty() {
        assertEquals("", LongestCommonSubstring.longestCommonSubstring("", ""));
    }

    @Test
    void boundaryFirstEmpty() {
        assertEquals("", LongestCommonSubstring.longestCommonSubstring("", "abc"));
    }

    @Test
    void boundarySecondEmpty() {
        assertEquals("", LongestCommonSubstring.longestCommonSubstring("abc", ""));
    }

    // ── B4: Single-character inputs ────────────────────────────────────

    @Test
    void boundarySingleCharMatch() {
        assertEquals("x", LongestCommonSubstring.longestCommonSubstring("x", "x"));
    }

    @Test
    void boundarySingleCharMismatch() {
        assertEquals("", LongestCommonSubstring.longestCommonSubstring("x", "y"));
    }

    @Test
    void boundarySingleCharVsLongerString() {
        assertEquals("a", LongestCommonSubstring.longestCommonSubstring("a", "xyzaz"));
    }

    // ── B5: Identical strings ──────────────────────────────────────────

    @Test
    void boundaryIdenticalStrings() {
        assertEquals("hello", LongestCommonSubstring.longestCommonSubstring("hello", "hello"));
    }

    @Test
    void boundaryIdenticalSingleChar() {
        assertEquals("z", LongestCommonSubstring.longestCommonSubstring("z", "z"));
    }

    // ── B6: Completely disjoint strings ────────────────────────────────

    @Test
    void boundaryCompletelyDisjoint() {
        assertEquals("", LongestCommonSubstring.longestCommonSubstring("abc", "xyz"));
    }

    @Test
    void boundaryDisjointLongerStrings() {
        assertEquals("", LongestCommonSubstring.longestCommonSubstring("abcdef", "ghijkl"));
    }

    // ── B7: Match at the very start ────────────────────────────────────

    @Test
    void boundaryMatchAtStart() {
        assertEquals("abc", LongestCommonSubstring.longestCommonSubstring("abcXYZ", "abcMNO"));
    }

    // ── B8: Match at the very end ──────────────────────────────────────

    @Test
    void boundaryMatchAtEnd() {
        assertEquals("xyz", LongestCommonSubstring.longestCommonSubstring("ABCxyz", "MNOxyz"));
    }

    // ── B9: One string is a complete substring of the other ────────────

    @Test
    void boundaryOneIsSubstringOfOther() {
        assertEquals("short", LongestCommonSubstring.longestCommonSubstring("short", "a_short_string"));
    }

    @Test
    void boundaryLongerContainsShorter() {
        assertEquals("inner", LongestCommonSubstring.longestCommonSubstring("the_inner_part", "inner"));
    }

    // ── B10: Large input stress test (boundary of practical size) ──────

    @Test
    void boundaryLargeIdenticalStrings() {
        String large = "A".repeat(5000);
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            assertEquals(large, LongestCommonSubstring.longestCommonSubstring(large, large));
        });
    }

    @Test
    void boundaryLargeDisjointStrings() {
        String a = "A".repeat(5000);
        String b = "B".repeat(5000);
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            assertEquals("", LongestCommonSubstring.longestCommonSubstring(a, b));
        });
    }

    @Test
    void boundaryLargeWithSmallCommonSubstring() {
        // Common part is exactly 10 chars, embedded in large surrounding noise
        String common = "BOUNDARY10";
        String a = "X".repeat(2000) + common + "X".repeat(2000);
        String b = "Y".repeat(2000) + common + "Y".repeat(2000);
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            assertEquals(common, LongestCommonSubstring.longestCommonSubstring(a, b));
        });
    }

    // ── Edge: Strings that differ only in the last character ───────────

    @Test
    void boundaryDifferOnlyInLastChar() {
        assertEquals("abcde", LongestCommonSubstring.longestCommonSubstring("abcdeX", "abcdeY"));
    }

    // ── Edge: Strings that differ only in the first character ──────────

    @Test
    void boundaryDifferOnlyInFirstChar() {
        assertEquals("bcdef", LongestCommonSubstring.longestCommonSubstring("Xbcdef", "Ybcdef"));
    }
}
