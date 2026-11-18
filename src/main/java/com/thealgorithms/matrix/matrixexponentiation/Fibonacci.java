package com.thealgorithms.matrix.matrixexponentiation;

import com.thealgorithms.matrix.utils.MatrixUtil;
import java.math.BigDecimal;

/**
 * @author Anirudh Buvanesh (https://github.com/anirudhb11) For more information
 * see https://www.geeksforgeeks.org/matrix-exponentiation/
 *
 */
public final class Fibonacci {
    private Fibonacci() {
    }

    // Exponentiation matrix for Fibonacci sequence
    private static final BigDecimal ONE = BigDecimal.valueOf(1);
    private static final BigDecimal ZERO = BigDecimal.valueOf(0);

    private static final BigDecimal[][] FIB_MATRIX = {{ONE, ONE}, {ONE, ZERO}};
    private static final BigDecimal[][] IDENTITY_MATRIX = {{ONE, ZERO}, {ZERO, ONE}};

    /**
     * Calculates the Fibonacci transformation matrix {@code {{1, 1}, {1, 0}}^n}
     * using binary matrix exponentiation with zero-based indexing.
     *
     * <p>Supported domain and boundaries:
     * <ul>
     *   <li>Supported domain: non-negative integers {@code n >= 0}.</li>
     *   <li>{@code n == 0}: base boundary returning the 2x2 identity matrix
     *       {@code {{1, 0}, {0, 1}}} where {@code result[0][1] = result[1][0] = F_0 = 0}
     *       and {@code result[0][0] = F_1 = 1}.</li>
     *   <li>{@code n >= 1}: returns the 2x2 matrix
     *       {@code {{F_{n+1}, F_n}, {F_n, F_{n-1}}}} where {@code F_0 = 0, F_1 = 1, F_2 = 1, F_5 = 5, F_10 = 55}.</li>
     *   <li>{@code n < 0}: outside the supported domain; the implementation defines no guard or exception.</li>
     * </ul>
     *
     * @param n non-negative zero-based Fibonacci index ({@code n >= 0})
     * @return a 2x2 {@code BigDecimal} matrix {@code {{F_{n+1}, F_n}, {F_n, F_{n-1}}}}
     *         (or the 2x2 identity matrix when {@code n == 0})
     */
    public static BigDecimal[][] fib(int n) {
        if (n == 0) {
            return IDENTITY_MATRIX;
        } else {
            BigDecimal[][] cachedResult = fib(n / 2);
            BigDecimal[][] matrixExpResult = MatrixUtil.multiply(cachedResult, cachedResult).get();
            if (n % 2 == 0) {
                return matrixExpResult;
            } else {
                return MatrixUtil.multiply(FIB_MATRIX, matrixExpResult).get();
            }
        }
    }
}
