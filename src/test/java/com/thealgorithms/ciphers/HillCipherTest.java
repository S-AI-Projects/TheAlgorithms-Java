package com.thealgorithms.ciphers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HillCipherTest {

    HillCipher hillCipher = new HillCipher();

    @Test
    void hillCipherEncryptTest() {
        // given
        String message = "ACT"; // Plaintext message
        int[][] keyMatrix = {{6, 24, 1}, {13, 16, 10}, {20, 17, 15}}; // Encryption key matrix

        // when
        String cipherText = hillCipher.encrypt(message, keyMatrix);

        // then
        assertEquals("POH", cipherText);
    }

    @Test
    void hillCipherDecryptTest() {
        // given
        String cipherText = "POH"; // Ciphertext message
        int[][] inverseKeyMatrix = {{8, 5, 10}, {21, 8, 21}, {21, 12, 8}}; // Decryption (inverse key) matrix

        // when
        String plainText = hillCipher.decrypt(cipherText, inverseKeyMatrix);

        // then
        assertEquals("ACT", plainText);
    }

    @Test
    @DisplayName("Worked example: smallest 1x1 inputs execute and match independent calculation")
    void hillCipherEncryptSmallestValidInputsTest() {
        // Preconditions from HillCipher.java (pinned lines):
        // line 7 (message != null, sanitized to [A-Z]), lines 8-9 & 73-75 & 82 (1x1 matrix with det % 26 != 0),
        // line 16 (empty sanitized message skips loop), line 30 ((3 * 1) % 26 == 3 -> 'D').
        int[][] key1x1 = {{3}};

        String emptyActual = hillCipher.encrypt("", key1x1);
        assertEquals("", emptyActual);

        String singleActual = hillCipher.encrypt("B", key1x1);
        assertEquals("D", singleActual);
        assertArrayEquals(new int[] {3}, key1x1[0], "keyMatrix must not be mutated");

        System.out.println("CASE|smallest_empty|message='',key=[[3]]|expected=''|actual='" + emptyActual + "'");
        System.out.println("CASE|smallest_single|message='B',key=[[3]]|expected='D'|actual='" + singleActual + "'");
    }

    @Test
    @DisplayName("Worked example: documented 2x2 example ('cat!' -> 'GEWX') executes and matches expected output")
    void hillCipherEncryptDocumentedWorkedExampleTest() {
        // Preconditions from HillCipher.java (pinned lines):
        // line 7 (sanitization "cat!" -> "CAT"), lines 9 & 73 (det([[3,3],[2,5]]) = 9 % 26 != 0),
        // lines 17-23 (block 1 "CA" -> [2, 0]; block 2 "T" padded with 'X' -> [19, 23]),
        // lines 25-32 (matrix-vector mod 26 -> [6, 4]="GE", [22, 23]="WX" -> "GEWX").
        HillCipher cipher = new HillCipher();
        int[][] keyMatrix = {{3, 3}, {2, 5}};

        String ciphertext = cipher.encrypt("cat!", keyMatrix);

        assertEquals("GEWX", ciphertext);
        assertArrayEquals(new int[] {3, 3}, keyMatrix[0], "keyMatrix row 0 must not be mutated");
        assertArrayEquals(new int[] {2, 5}, keyMatrix[1], "keyMatrix row 1 must not be mutated");

        System.out.println("CASE|documented_worked_example|message='cat!',key=[[3,3],[2,5]]|expected='GEWX'|actual='" + ciphertext + "'");
    }
}
