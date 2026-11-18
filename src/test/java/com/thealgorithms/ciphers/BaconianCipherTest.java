package com.thealgorithms.ciphers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BaconianCipherTest {

    BaconianCipher baconianCipher = new BaconianCipher();

    @Test
    void baconianCipherEncryptTest() {
        // given
        String plaintext = "MEET AT DAWN";

        // when
        String cipherText = baconianCipher.encrypt(plaintext);

        // then
        assertEquals("ABBAAAABAAAABAABAABBAAAAABAABBAAABBAAAAABABBAABBAB", cipherText);
    }

    @Test
    void baconianCipherDecryptTest() {
        // given
        String ciphertext = "ABBAAAABAAAABAABAABBAAAAABAABBAAABBAAAAABABBAABBAB";

        // when
        String plainText = baconianCipher.decrypt(ciphertext);

        // then
        assertEquals("MEETATDAWN", plainText);
    }

    @Test
    @DisplayName("Failure/empty-result paths: empty string, non-letter input, and null followed by valid recovery calls")
    void baconianCipherEncryptFailureAndEmptyResultPathsTest() {
        // Source lines establishing preconditions and branches (BaconianCipher.java pinned lines):
        // line 42 (plaintext.toUpperCase().replaceAll("[^A-Z]", "") -> NPE on null; strips non-letters to ""),
        // lines 44-48 (empty sanitized string skips loop and returns ""),
        // lines 21-31 (BACONIAN_MAP lookup for [A-Z]).
        BaconianCipher cipher = new BaconianCipher();

        // 1. Valid empty-result path (total non-null contract): smallest empty input ""
        String emptyResult = cipher.encrypt("");
        assertEquals("", emptyResult);

        // 2. Valid empty-result path: non-letter-only input strips to "" at line 42
        String nonLetterResult = cipher.encrypt("123!@# ");
        assertEquals("", nonLetterResult);

        // 3. Failure branch at line 42: null plaintext throws NullPointerException
        NullPointerException npe = assertThrows(NullPointerException.class, () -> cipher.encrypt(null));

        // 4. Subsequent valid calls on the same instance still succeed (smallest "A" and ordinary "Hello, World!")
        String smallestValid = cipher.encrypt("A");
        assertEquals("AAAAA", smallestValid);

        // Independent encoding of "HELLOWORLD":
        // H=AABBB, E=AABAA, L=ABABB, L=ABABB, O=ABBBA, W=BABBA, O=ABBBA, R=BAAAB, L=ABABB, D=AAABB
        String expectedOrdinary = "AABBBAABAAABABBABABBABBBABABBAABBBABAAABABABBAAABB";
        String ordinaryValid = cipher.encrypt("Hello, World!");
        assertEquals(expectedOrdinary, ordinaryValid);

        System.out.println("CASE|empty_string|input=''|expected=''|actual='" + emptyResult + "'");
        System.out.println("CASE|non_letter_only|input='123!@# '|expected=''|actual='" + nonLetterResult + "'");
        System.out.println("CASE|null_failure|input=null|expected=NullPointerException|actual=" + npe.getClass().getSimpleName());
        System.out.println("CASE|recovery_smallest|input='A'|expected='AAAAA'|actual='" + smallestValid + "'");
        System.out.println("CASE|recovery_ordinary|input='Hello, World!'|expected='" + expectedOrdinary + "'|actual='" + ordinaryValid + "'");
    }
}
