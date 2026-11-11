package com.thealgorithms.strings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Constructor;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class IsomorphicTest {

    /**
     * Independent reference oracle for string isomorphism.
     * Uses canonical first-occurrence index pattern comparison rather than hash maps or sets.
     * Two strings are isomorphic if and only if for all i, the first appearance index of s[i] in s
     * equals the first appearance index of t[i] in t.
     */
    public static boolean referenceOracle(String s, String t) {
        if (s == null || t == null || s.length() != t.length()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.indexOf(s.charAt(i)) != t.indexOf(t.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @ParameterizedTest
    @MethodSource("inputs")
    public void testCheckStrings(String str1, String str2, Boolean expected) {
        assertEquals(expected, Isomorphic.areIsomorphic(str1, str2));
        assertEquals(expected, Isomorphic.areIsomorphic(str2, str1));
    }

    private static Stream<Arguments> inputs() {
        return Stream.of(
            Arguments.of("", "", Boolean.TRUE),
            Arguments.of("", "a", Boolean.FALSE),
            Arguments.of("aaa", "aa", Boolean.FALSE),
            Arguments.of("abbbbaac", "kffffkkd", Boolean.TRUE),
            Arguments.of("xyxyxy", "bnbnbn", Boolean.TRUE),
            Arguments.of("ghjknnmm", "wertpopo", Boolean.FALSE),
            Arguments.of("aaammmnnn", "ggghhhbbj", Boolean.FALSE)
        );
    }

    /**
     * Smallest valid input fixture: two empty strings.
     * Preconditions established at:
     * - Isomorphic.java line 32 (s.length() != t.length() check; here 0 == 0)
     * - Isomorphic.java lines 39-54 (loop 0..<0 does not execute)
     * - Isomorphic.java line 56 (returns true)
     */
    @Test
    public void testSmallestValidInputEmptyStrings() {
        String s = "";
        String t = "";
        boolean expected = true;

        boolean oracleResult = referenceOracle(s, t);
        assertEquals(expected, oracleResult, "Oracle must calculate true for empty strings.");

        boolean actualResult = Isomorphic.areIsomorphic(s, t);
        assertEquals(expected, actualResult, "Isomorphic.areIsomorphic must return true for smallest valid input (empty strings).");
    }

    /**
     * Ordinary input fixture: "egg" and "add".
     * Preconditions established at:
     * - Isomorphic.java line 32 (both lengths equal 3)
     * - Isomorphic.java lines 43-53 (1-to-1 bijective mapping 'e' -> 'a', 'g' -> 'd')
     * - Isomorphic.java line 56 (returns true)
     */
    @Test
    public void testOrdinaryInputRepresentativeWorkflow() {
        String s = "egg";
        String t = "add";
        boolean expected = true;

        boolean oracleResult = referenceOracle(s, t);
        assertEquals(expected, oracleResult, "Oracle must confirm isomorphism for 'egg' and 'add'.");

        boolean actualResult = Isomorphic.areIsomorphic(s, t);
        assertEquals(expected, actualResult, "Isomorphic.areIsomorphic must return true for ordinary input 'egg' and 'add'.");
    }

    /**
     * Cross-check against independent oracle across 22 diverse test cases.
     * Verifies mutual consistency, symmetry, and explicit expected values without calling the function under test inside the oracle.
     */
    @ParameterizedTest
    @MethodSource("crossCheckTestCases")
    public void testCrossCheckAgainstIndependentOracle(String s, String t, boolean expected) {
        boolean oracleResult = referenceOracle(s, t);
        assertEquals(expected, oracleResult, () -> String.format("Oracle mismatch for inputs '%s' and '%s'", s, t));

        boolean actualResult = Isomorphic.areIsomorphic(s, t);
        assertEquals(expected, actualResult, () -> String.format("Isomorphic mismatch for inputs '%s' and '%s'", s, t));

        // Isomorphism must be symmetric
        assertEquals(expected, Isomorphic.areIsomorphic(t, s), () -> String.format("Symmetry mismatch for inputs '%s' and '%s'", t, s));
    }

    private static Stream<Arguments> crossCheckTestCases() {
        return Stream.of(
            Arguments.of("", "", true),
            Arguments.of("a", "b", true),
            Arguments.of("a", "a", true),
            Arguments.of("ab", "cd", true),
            Arguments.of("ab", "aa", false),
            Arguments.of("aa", "ab", false),
            Arguments.of("egg", "add", true),
            Arguments.of("foo", "bar", false),
            Arguments.of("bar", "foo", false),
            Arguments.of("paper", "title", true),
            Arguments.of("badc", "baba", false),
            Arguments.of("abc", "def", true),
            Arguments.of("aba", "cdc", true),
            Arguments.of("aba", "cdd", false),
            Arguments.of("abba", "cddc", true),
            Arguments.of("abba", "cdcd", false),
            Arguments.of("123", "456", true),
            Arguments.of("1122", "3344", true),
            Arguments.of("1122", "3434", false),
            Arguments.of("hello", "world", false),
            Arguments.of("listen", "silent", true),
            Arguments.of("banana", "pomomo", true),
            Arguments.of("xyz", "xy", false)
        );
    }

    /**
     * Domain boundary fixture: length mismatch precondition.
     * Precondition established at:
     * - Isomorphic.java line 32 (s.length() != t.length() immediately returns false)
     */
    @Test
    public void testDomainBoundaryLengthMismatch() {
        String s = "abcdef";
        String t = "abc";
        assertFalse(Isomorphic.areIsomorphic(s, t), "Strings of different lengths cannot be isomorphic.");
        assertFalse(referenceOracle(s, t), "Oracle must also return false for differing lengths.");
    }

    /**
     * Reflection fixture for private constructor contract.
     * Preconditions established at:
     * - Isomorphic.java lines 21-22
     */
    @Test
    public void testPrivateConstructorContract() throws Exception {
        Constructor<Isomorphic> constructor = Isomorphic.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Isomorphic instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
