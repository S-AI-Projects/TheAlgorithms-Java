package com.thealgorithms.strings;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class KasaiAlgorithmTest {

    @Test
    public void testKasaiBanana() {
        String text = "banana";
        // Suffixes:
        // 0: banana
        // 1: anana
        // 2: nana
        // 3: ana
        // 4: na
        // 5: a
        //
        // Sorted Suffixes:
        // 5: a
        // 3: ana
        // 1: anana
        // 0: banana
        // 4: na
        // 2: nana
        int[] suffixArr = {5, 3, 1, 0, 4, 2};

        int[] expectedLcp = {1, 3, 0, 0, 2, 0};

        assertArrayEquals(expectedLcp, KasaiAlgorithm.kasai(text, suffixArr));
    }

    @Test
    public void testKasaiAaaa() {
        String text = "aaaa";
        // Sorted Suffixes:
        // 3: a
        // 2: aa
        // 1: aaa
        // 0: aaaa
        int[] suffixArr = {3, 2, 1, 0};
        int[] expectedLcp = {1, 2, 3, 0};

        assertArrayEquals(expectedLcp, KasaiAlgorithm.kasai(text, suffixArr));
    }

    @Test
    public void testKasaiEmptyString() {
        assertArrayEquals(new int[0], KasaiAlgorithm.kasai("", new int[0]));
    }

    @Test
    public void testKasaiSingleChar() {
        assertArrayEquals(new int[] {0}, KasaiAlgorithm.kasai("A", new int[] {0}));
    }

    @Test
    public void testKasaiNullTextOrSuffixArray() {
        assertThrows(IllegalArgumentException.class, () -> KasaiAlgorithm.kasai(null, new int[] {0}));
        assertThrows(IllegalArgumentException.class, () -> KasaiAlgorithm.kasai("A", null));
    }

    @Test
    public void testKasaiInvalidSuffixArrayLength() {
        assertThrows(IllegalArgumentException.class, () -> KasaiAlgorithm.kasai("A", new int[] {0, 1}));
    }

    @Test
    public void testKasaiInvalidSuffixArrayIndex() {
        assertThrows(IllegalArgumentException.class, () -> KasaiAlgorithm.kasai("A", new int[] {1})); // Out of bounds
        assertThrows(IllegalArgumentException.class, () -> KasaiAlgorithm.kasai("A", new int[] {-1})); // Out of bounds
    }

    @Test
    @DisplayName("Smallest valid input fixture (empty string, KasaiAlgorithm.java:55-56)")
    public void testSmallestValidInputFixtureEmptyPrecondition() {
        // Preconditions: non-null text/suffixArr (KasaiAlgorithm.java:48), matching length n=0 (KasaiAlgorithm.java:52), branched at line 55
        String text = "";
        int[] suffixArr = new int[0];
        int[] expectedLcp = new int[0];

        int[] actualLcp = KasaiAlgorithm.kasai(text, suffixArr);
        assertArrayEquals(expectedLcp, actualLcp);
    }

    @Test
    @DisplayName("Smallest non-empty valid input fixture (single char, KasaiAlgorithm.java:51,75)")
    public void testSmallestValidInputFixtureSingleCharPrecondition() {
        // Preconditions: non-null text/suffixArr (KasaiAlgorithm.java:48), n=1 (KasaiAlgorithm.java:51), rank == n-1 (line 75)
        String text = "A";
        int[] suffixArr = {0};
        int[] expectedLcp = {0};

        int[] actualLcp = KasaiAlgorithm.kasai(text, suffixArr);
        assertArrayEquals(expectedLcp, actualLcp);
    }

    @Test
    @DisplayName("Documented ordinary input fixture (abracadabra, KasaiAlgorithm.java:48-95)")
    public void testDocumentedOrdinaryAbracadabraFixture() {
        // Preconditions: text != null && suffixArr != null (KasaiAlgorithm.java:48), length 11 (line 52), valid indices (line 63)
        String text = "abracadabra";
        int[] suffixArr = {10, 7, 0, 3, 5, 8, 1, 4, 6, 9, 2};
        int[] expectedLcp = {1, 4, 1, 1, 0, 3, 0, 0, 0, 2, 0};

        int[] actualLcp = KasaiAlgorithm.kasai(text, suffixArr);
        assertArrayEquals(expectedLcp, actualLcp);
    }

    @Test
    @DisplayName("Representative regression testing linear scaling and independent oracle cross-check")
    public void testKasaiRepresentativeScalingFixture() {
        // Deterministic pseudo-random string with fixed seed for repeatability
        int n = 120;
        Random rng = new Random(42L);
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append((char) ('a' + rng.nextInt(4)));
        }
        String text = sb.toString();
        int[] suffixArr = SuffixArray.buildSuffixArray(text);

        // Precondition checks: non-null, matching length n
        assertEquals(n, suffixArr.length);

        int[] lcp = KasaiAlgorithm.kasai(text, suffixArr);
        assertEquals(n, lcp.length);
        assertEquals(0, lcp[n - 1], "Last element of LCP array must always be 0 (KasaiAlgorithm.java:44,75)");

        // Independent oracle: naive character-by-character longest common prefix check
        for (int rank = 0; rank < n - 1; rank++) {
            int idx1 = suffixArr[rank];
            int idx2 = suffixArr[rank + 1];
            int expectedCommonPrefix = 0;
            while (idx1 + expectedCommonPrefix < n && idx2 + expectedCommonPrefix < n && text.charAt(idx1 + expectedCommonPrefix) == text.charAt(idx2 + expectedCommonPrefix)) {
                expectedCommonPrefix++;
            }
            assertEquals(expectedCommonPrefix, lcp[rank], "Mismatch at rank " + rank);
        }
    }

    @Test
    @DisplayName("Verify KasaiAlgorithm private constructor via reflection")
    public void testPrivateConstructorContract() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<KasaiAlgorithm> constructor = KasaiAlgorithm.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        KasaiAlgorithm instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
