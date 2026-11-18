package com.thealgorithms.matrix.matrixexponentiation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FibonacciTest {

    private static void assertMatrixEquals(long fNext, long fCurr, long fPrev, BigDecimal[][] actual, String label, int n) {
        assertEquals(2, actual.length, "Expected 2 rows for n=" + n);
        assertEquals(2, actual[0].length, "Expected 2 columns in row 0 for n=" + n);
        assertEquals(2, actual[1].length, "Expected 2 columns in row 1 for n=" + n);

        BigDecimal exp00 = BigDecimal.valueOf(fNext);
        BigDecimal exp01 = BigDecimal.valueOf(fCurr);
        BigDecimal exp10 = BigDecimal.valueOf(fCurr);
        BigDecimal exp11 = BigDecimal.valueOf(fPrev);

        assertEquals(0, exp00.compareTo(actual[0][0]), "F_{n+1} mismatch for n=" + n);
        assertEquals(0, exp01.compareTo(actual[0][1]), "F_n (0,1) mismatch for n=" + n);
        assertEquals(0, exp10.compareTo(actual[1][0]), "F_n (1,0) mismatch for n=" + n);
        assertEquals(0, exp11.compareTo(actual[1][1]), "F_{n-1} mismatch for n=" + n);

        System.out.println("CASE|" + label + "|n=" + n + "|expected=[[" + fNext + "," + fCurr + "],[" + fCurr + "," + fPrev + "]],F_n=" + fCurr + "|actual=[[" + actual[0][0] + "," + actual[0][1] + "],[" + actual[1][0] + "," + actual[1][1] + "]],F_n=" + actual[0][1]);
    }

    @Test
    @DisplayName("Boundary: n = 0 hits base case IDENTITY_MATRIX with F_0 = 0")
    void testBaseBoundaryZero() {
        // Source contract (Fibonacci.java pinned lines 20, 30-31): n == 0 returns IDENTITY_MATRIX {{1, 0}, {0, 1}}.
        BigDecimal[][] actual = Fibonacci.fib(0);
        assertMatrixEquals(1, 0, 1, actual, "boundary_zero", 0);
    }

    @Test
    @DisplayName("Boundary: n = 1 enters first recursive odd step returning FIB_MATRIX with F_1 = 1")
    void testFirstRecursiveOddBoundaryOne() {
        // Source contract (Fibonacci.java pinned lines 19, 32-39): n = 1 recurses to n/2 = 0 then multiplies by FIB_MATRIX.
        BigDecimal[][] actual = Fibonacci.fib(1);
        assertMatrixEquals(1, 1, 0, actual, "boundary_one_odd", 1);
    }

    @Test
    @DisplayName("Boundary: n = 2 enters smallest positive even branch with F_2 = 1")
    void testSmallestEvenBoundaryTwo() {
        // Source contract (Fibonacci.java pinned lines 33-36): n = 2 squares fib(1) without extra FIB_MATRIX multiply.
        BigDecimal[][] actual = Fibonacci.fib(2);
        assertMatrixEquals(2, 1, 1, actual, "boundary_two_even", 2);
    }

    @Test
    @DisplayName("Interior odd: n = 5 yields F_5 = 5 and full matrix {{8, 5}, {5, 3}}")
    void testInteriorOddFive() {
        // Zero-based sequence: F_0=0, F_1=1, F_2=1, F_3=2, F_4=3, F_5=5, F_6=8.
        BigDecimal[][] actual = Fibonacci.fib(5);
        assertMatrixEquals(8, 5, 3, actual, "interior_five_odd", 5);
    }

    @Test
    @DisplayName("Interior even: n = 10 yields F_10 = 55 and full matrix {{89, 55}, {55, 34}}")
    void testInteriorEvenTen() {
        // Zero-based sequence: F_9=34, F_10=55, F_11=89.
        BigDecimal[][] actual = Fibonacci.fib(10);
        assertMatrixEquals(89, 55, 34, actual, "interior_ten_even", 10);
    }

    @Test
    @DisplayName("Interior larger index: n = 20 yields F_20 = 6765")
    void testInteriorTwenty() {
        // Zero-based sequence: F_19=4181, F_20=6765, F_21=10946.
        BigDecimal[][] actual = Fibonacci.fib(20);
        assertMatrixEquals(10946, 6765, 4181, actual, "interior_twenty_even", 20);
    }
}
