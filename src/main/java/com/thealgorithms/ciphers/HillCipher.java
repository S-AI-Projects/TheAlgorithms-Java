package com.thealgorithms.ciphers;

public class HillCipher {

    /**
     * Encrypts {@code message} using the Hill cipher with square key matrix {@code keyMatrix}.
     *
     * <p>Preconditions: {@code message != null}; {@code keyMatrix} is a square {@code n x n} matrix
     * ({@code n >= 1}) with non-negative entries whose determinant is non-zero modulo 26
     * (otherwise {@link IllegalArgumentException} is thrown). {@code keyMatrix} is not mutated.
     *
     * <p>Decisive state transitions:
     * <ol>
     *   <li>Sanitize {@code message} to uppercase ASCII letters {@code [A-Z]}.</li>
     *   <li>Partition letters into blocks of length {@code n}, padding an incomplete final block with {@code 'X'} (23).</li>
     *   <li>Map each block vector {@code m} via {@code (keyMatrix * m) mod 26} back to {@code 'A'..'Z'}.</li>
     * </ol>
     *
     * <p>Runnable worked example:
     * <pre>{@code
     * import com.thealgorithms.ciphers.HillCipher;
     *
     * HillCipher cipher = new HillCipher();
     * int[][] keyMatrix = {{3, 3}, {2, 5}}; // det = 9, 9 % 26 = 9 != 0
     * // "cat!" -> "CAT" -> blocks "CA" ([2, 0]) and padded "TX" ([19, 23])
     * // Block 1: [(3*2 + 3*0) % 26, (2*2 + 5*0) % 26] = [6, 4] -> "GE"
     * // Block 2: [(3*19 + 3*23) % 26, (2*19 + 5*23) % 26] = [22, 23] -> "WX"
     * String ciphertext = cipher.encrypt("cat!", keyMatrix); // "GEWX"
     * }</pre>
     *
     * @param message plaintext string (non-letters are removed and letters uppercased)
     * @param keyMatrix square {@code n x n} encryption key matrix with {@code det(keyMatrix) % 26 != 0}
     * @return encrypted uppercase ciphertext string
     * @throws IllegalArgumentException if {@code det(keyMatrix) % 26 == 0}
     */
    public String encrypt(String message, int[][] keyMatrix) {
        message = message.toUpperCase().replaceAll("[^A-Z]", "");
        int matrixSize = keyMatrix.length;
        validateDeterminant(keyMatrix, matrixSize);

        StringBuilder cipherText = new StringBuilder();
        int[] messageVector = new int[matrixSize];
        int[] cipherVector = new int[matrixSize];
        int index = 0;

        while (index < message.length()) {
            for (int i = 0; i < matrixSize; i++) {
                if (index < message.length()) {
                    messageVector[i] = message.charAt(index++) - 'A';
                } else {
                    messageVector[i] = 'X' - 'A'; // Padding with 'X' if needed
                }
            }

            for (int i = 0; i < matrixSize; i++) {
                cipherVector[i] = 0;
                for (int j = 0; j < matrixSize; j++) {
                    cipherVector[i] += keyMatrix[i][j] * messageVector[j];
                }
                cipherVector[i] = cipherVector[i] % 26;
                cipherText.append((char) (cipherVector[i] + 'A'));
            }
        }

        return cipherText.toString();
    }

    // Decrypts the message using the inverse key matrix
    public String decrypt(String message, int[][] inverseKeyMatrix) {
        message = message.toUpperCase().replaceAll("[^A-Z]", "");
        int matrixSize = inverseKeyMatrix.length;
        validateDeterminant(inverseKeyMatrix, matrixSize);

        StringBuilder plainText = new StringBuilder();
        int[] messageVector = new int[matrixSize];
        int[] plainVector = new int[matrixSize];
        int index = 0;

        while (index < message.length()) {
            for (int i = 0; i < matrixSize; i++) {
                if (index < message.length()) {
                    messageVector[i] = message.charAt(index++) - 'A';
                } else {
                    messageVector[i] = 'X' - 'A'; // Padding with 'X' if needed
                }
            }

            for (int i = 0; i < matrixSize; i++) {
                plainVector[i] = 0;
                for (int j = 0; j < matrixSize; j++) {
                    plainVector[i] += inverseKeyMatrix[i][j] * messageVector[j];
                }
                plainVector[i] = plainVector[i] % 26;
                plainText.append((char) (plainVector[i] + 'A'));
            }
        }

        return plainText.toString();
    }

    // Validates that the determinant of the key matrix is not zero modulo 26
    private void validateDeterminant(int[][] keyMatrix, int n) {
        int det = determinant(keyMatrix, n) % 26;
        if (det == 0) {
            throw new IllegalArgumentException("Invalid key matrix. Determinant is zero modulo 26.");
        }
    }

    // Computes the determinant of a matrix recursively
    private int determinant(int[][] matrix, int n) {
        int det = 0;
        if (n == 1) {
            return matrix[0][0];
        }
        int sign = 1;
        int[][] subMatrix = new int[n - 1][n - 1];
        for (int x = 0; x < n; x++) {
            int subI = 0;
            for (int i = 1; i < n; i++) {
                int subJ = 0;
                for (int j = 0; j < n; j++) {
                    if (j != x) {
                        subMatrix[subI][subJ++] = matrix[i][j];
                    }
                }
                subI++;
            }
            det += sign * matrix[0][x] * determinant(subMatrix, n - 1);
            sign = -sign;
        }
        return det;
    }
}
