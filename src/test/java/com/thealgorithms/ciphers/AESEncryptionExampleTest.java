package com.thealgorithms.ciphers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import javax.crypto.AEADBadTagException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

/**
 * Executable documentation and worked-example tests for {@link AESEncryption}.
 *
 * <p>Source preconditions ({@code AESEncryption.java}):
 * <ul>
 * <li>line 39: private constructor {@code AESEncryption()}</li>
 * <li>line 42: {@code HEX_ARRAY} encodes each byte into 2 uppercase hex chars (lines 113-121)</li>
 * <li>line 43: static {@code aesCipher} stores the GCM cipher instance initialized during
 * {@code encryptText} (lines 87-88); {@code decryptText} reads {@code aesCipher.getIV()} with a
 * 128-bit authentication tag (line 100)</li>
 * <li>lines 69-73: {@code getSecretEncryptionKey()} generates a 128-bit (16-byte) AES key</li>
 * <li>lines 85-90: {@code encryptText(String, SecretKey)} uses {@code AES/GCM/NoPadding},
 * producing {@code plainText.getBytes().length + 16} bytes (ciphertext + 16-byte GCM tag)</li>
 * <li>lines 50-60: {@code main(String[])} runs the worked example on {@code "Hello World"}</li>
 * </ul>
 */
public class AESEncryptionExampleTest {

    private static void logStep(String id, String input, Object expected, Object actual) {
        System.out.println("[R45-T017] " + id + " | input=" + input
            + " | expected=" + expected + " | actual=" + actual);
    }

    /**
     * Smallest valid inputs: empty string {@code ""} (0 bytes -> 16-byte GCM tag only, 32 hex
     * chars) and single-character {@code "A"} (1 byte -> 17 bytes, 34 hex chars).
     */
    @Test
    public void smallestValidInputsTraceIntermediateSizesAndRoundTrip() throws Exception {
        SecretKey key = AESEncryption.getSecretEncryptionKey();
        assertEquals(16, key.getEncoded().length);
        assertEquals(32, AESEncryption.bytesToHex(key.getEncoded()).length());

        byte[] emptyCipher = AESEncryption.encryptText("", key);
        logStep("smallest-empty-cipher-len", "plainText=\"\"", 16, emptyCipher.length);
        assertEquals(16, emptyCipher.length);
        assertEquals(32, AESEncryption.bytesToHex(emptyCipher).length());

        String emptyRecovered = AESEncryption.decryptText(emptyCipher, key);
        logStep("smallest-empty-decrypt", "cipherLen=16", "\"\"", "\"" + emptyRecovered + "\"");
        assertEquals("", emptyRecovered);

        byte[] singleCipher = AESEncryption.encryptText("A", key);
        logStep("smallest-single-char-len", "plainText=\"A\"", 17, singleCipher.length);
        assertEquals(17, singleCipher.length);
        assertEquals(34, AESEncryption.bytesToHex(singleCipher).length());

        String singleRecovered = AESEncryption.decryptText(singleCipher, key);
        logStep("smallest-single-char-decrypt", "cipherLen=17", "A", singleRecovered);
        assertEquals("A", singleRecovered);
    }

    /**
     * Ordinary input ({@code "Hello World"}, 11 bytes = {@code 48656C6C6F20576F726C64} in hex):
     * traces fixed 128-bit key -> 27-byte ciphertext+tag (54 hex chars) -> decisive
     * {@code aesCipher.getIV()} state transition -> recovered {@code "Hello World"}.
     */
    @Test
    public void ordinaryWorkedExampleTracesIntermediateStatesAndDecisiveIvState() throws Exception {
        byte[] rawFixedKey = new byte[16];
        SecretKey fixedKey = new SecretKeySpec(rawFixedKey, "AES");
        String keyHex = AESEncryption.bytesToHex(fixedKey.getEncoded());
        logStep("ordinary-key-hex", "16 zero bytes", "00000000000000000000000000000000", keyHex);
        assertEquals("00000000000000000000000000000000", keyHex);

        String plainText = "Hello World";
        String plainHex = AESEncryption.bytesToHex(plainText.getBytes(StandardCharsets.UTF_8));
        logStep("ordinary-plaintext-hex", "\"Hello World\"", "48656C6C6F20576F726C64", plainHex);
        assertEquals("48656C6C6F20576F726C64", plainHex);

        byte[] firstCipher = AESEncryption.encryptText(plainText, fixedKey);
        logStep("ordinary-cipher-len", "\"Hello World\" (11B + 16B tag)", 27, firstCipher.length);
        assertEquals(27, firstCipher.length);
        assertEquals(54, AESEncryption.bytesToHex(firstCipher).length());

        String recovered = AESEncryption.decryptText(firstCipher, fixedKey);
        logStep("ordinary-decrypt", "firstCipher (27B)", "Hello World", recovered);
        assertEquals("Hello World", recovered);

        // Decisive state transition: a second encryptText call overwrites static aesCipher with a new
        // random GCM IV (lines 87-88), so decrypting firstCipher against the overwritten IV fails.
        byte[] secondCipher = AESEncryption.encryptText(plainText, fixedKey);
        assertNotEquals(AESEncryption.bytesToHex(firstCipher), AESEncryption.bytesToHex(secondCipher));
        assertThrows(AEADBadTagException.class, () -> AESEncryption.decryptText(firstCipher, fixedKey));
        assertEquals("Hello World", AESEncryption.decryptText(secondCipher, fixedKey));
    }

    /**
     * Executes the documented {@link AESEncryption#main(String[])} runnable example and asserts
     * its displayed console output format and final verification line.
     */
    @Test
    public void runnableDocumentationExamplePrintsExpectedOutput() throws Exception {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            AESEncryption.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }

        String[] lines = captured.toString(StandardCharsets.UTF_8).trim().split("\\R");
        logStep("main-output-lines", "AESEncryption.main()", 4, lines.length);
        assertEquals(4, lines.length);
        assertEquals("Original Text:Hello World", lines[0]);
        assertTrue(lines[1].matches("^AES Key \\(Hex Form\\):[0-9A-F]{32}$"),
            "Unexpected key line: " + lines[1]);
        assertTrue(lines[2].matches("^Encrypted Text \\(Hex Form\\):[0-9A-F]{54}$"),
            "Unexpected cipher line: " + lines[2]);
        assertEquals("Decryption successful. Decrypted text matches original: true", lines[3]);
    }
}
