package com.thealgorithms.ciphers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AutokeyCipherTest {

    Autokey autokeyCipher = new Autokey();

    @Test
    void autokeyEncryptTest() {
        // given
        String plaintext = "MEET AT DAWN";
        String keyword = "QUEEN";

        // when
        String cipherText = autokeyCipher.encrypt(plaintext, keyword);

        // then
        assertEquals("CYIXNFHEPN", cipherText);
    }

    @Test
    void autokeyDecryptTest() {
        // given
        String ciphertext = "CYIX NF HEPN";
        String keyword = "QUEEN";

        // when
        String plainText = autokeyCipher.decrypt(ciphertext, keyword);

        // then
        assertEquals("MEETATDAWN", plainText);
    }

    private static void logCase(String id, String input, String expected, String actual) {
        System.out.println("[R45-T020] " + id + " | input=" + input
            + " | expected=" + expected + " | actual=" + actual);
    }

    /**
     * Mutation-resistant regression for {@link Autokey#encrypt(String, String)}.
     *
     * <p>Source preconditions ({@code Autokey.java}):
     * <ul>
     * <li>lines 31-32: uppercases {@code plaintext} and strips {@code [^A-Z]} (line 31);
     * uppercases {@code keyword} (line 32)</li>
     * <li>lines 34-35: {@code extendedKey = keyword + sanitizedPlaintext}</li>
     * <li>line 39: loop runs for {@code 0 <= i < plaintext.length()}</li>
     * <li>line 43: {@code (plainChar - 'A' + keyChar - 'A') % 26 + 'A'} wraps modulo 26</li>
     * </ul>
     */
    @Test
    void encryptSmallestOrdinaryAndBoundaryInputsDetectMutants() {
        // Smallest valid inputs: empty after sanitization (line 31 & line 39) and 1-char minimal
        String emptyActual = autokeyCipher.encrypt("", "A");
        logCase("smallest-empty", "(\"\", \"A\")", "", emptyActual);
        assertEquals("", emptyActual);

        String strippedEmptyActual = autokeyCipher.encrypt("123!", "A");
        logCase("smallest-nonalpha", "(\"123!\", \"A\")", "", strippedEmptyActual);
        assertEquals("", strippedEmptyActual);

        // Minimal 1-char plaintext protects loop bound `i < plaintext.length()` (line 39)
        String minCharActual = autokeyCipher.encrypt("A", "B");
        logCase("smallest-one-char", "(\"A\", \"B\")", "B", minCharActual);
        assertEquals("B", minCharActual);

        // Wrap-around boundary: ('Z' - 'A' + 'B' - 'A') % 26 + 'A' = 26 % 26 + 'A' = 'A' (line 43)
        String wrapActual = autokeyCipher.encrypt("Z", "B");
        logCase("boundary-mod-26-wrap", "(\"Z\", \"B\")", "A", wrapActual);
        assertEquals("A", wrapActual);

        // Minimal autokey extension: keyword "D" (len 1) + plaintext "BC" -> extendedKey "DBC" (line 35)
        // i=0: B(1)+D(3)=E(4); i=1: C(2)+B(1)=D(3)
        String autokeyActual = autokeyCipher.encrypt("BC", "D");
        logCase("boundary-autokey-extension", "(\"BC\", \"D\")", "ED", autokeyActual);
        assertEquals("ED", autokeyActual);

        // Ordinary input with lowercase, spaces, and 12-char plaintext > 5-char keyword:
        // "attack at dawn" -> "ATTACKATDAWN", key "queen" -> "QUEENATTACKATDAWN" -> "QNXEPKTMDCGN"
        String origPlain = "attack at dawn";
        String origKey = "queen";
        String ordinaryActual = autokeyCipher.encrypt(origPlain, origKey);
        logCase("ordinary-lowercase-spaced", "(\"attack at dawn\", \"queen\")",
            "QNXEPKTMDCGN", ordinaryActual);
        assertEquals("QNXEPKTMDCGN", ordinaryActual);
        assertEquals(12, ordinaryActual.length());

        // Fresh equivalent sanitized call produces identical output; inputs remain unmutated
        String freshEquivalent = autokeyCipher.encrypt("ATTACKATDAWN", "QUEEN");
        assertEquals("QNXEPKTMDCGN", freshEquivalent);
        assertEquals("attack at dawn", origPlain);
        assertEquals("queen", origKey);
        assertEquals("ATTACKATDAWN", autokeyCipher.decrypt(ordinaryActual, origKey));
    }
}
