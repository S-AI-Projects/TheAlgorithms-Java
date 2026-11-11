package com.thealgorithms.maths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Contract regression tests for {@link ComplexNumberMultiply#multiply(String, String)}.
 *
 * <p>Source facts ({@code ComplexNumberMultiply.java}):
 * <ul>
 * <li>line 9: private constructor {@code ComplexNumberMultiply()} (utility class)</li>
 * <li>lines 12-21: {@code parse(String)} requires non-null input matching
 * {@code -?\d+\+-?\d+i} (line 13), throwing {@code IllegalArgumentException} on mismatch
 * (line 14), splitting on {@code "\\+"} (line 17), and parsing {@code int} parts (lines 18-19)</li>
 * <li>lines 39-47: {@code multiply(String, String)} computes
 * {@code real = a[0] * b[0] - a[1] * b[1]} (line 43) and
 * {@code imaginary = a[0] * b[1] + a[1] * b[0]} (line 44), returning
 * {@code real + "+" + imaginary + "i"} (line 46)</li>
 * </ul>
 *
 * <p>Expected values are independently calculated literals.
 */
public class ComplexNumberMultiplyContractTest {

    private static void logCase(String id, String input, Object expected, Object actual) {
        System.out.println("[R45-T011] " + id + " | input=" + input
            + " | expected=" + expected + " | actual=" + actual);
    }

    /**
     * Smallest valid inputs: {@code "0+0i"} (zero complex number, minimal digits matching line 13)
     * and unit elements {@code "1+0i"} and {@code "0+1i"}.
     */
    @Test
    public void smallestValidInputsMatchExpectedContract() {
        String actualZero = ComplexNumberMultiply.multiply("0+0i", "0+0i");
        logCase("smallest-zero", "(\"0+0i\", \"0+0i\")", "0+0i", actualZero);
        assertEquals("0+0i", actualZero);

        String actualRealUnit = ComplexNumberMultiply.multiply("1+0i", "1+0i");
        logCase("smallest-real-unit", "(\"1+0i\", \"1+0i\")", "1+0i", actualRealUnit);
        assertEquals("1+0i", actualRealUnit);

        String actualImagUnit = ComplexNumberMultiply.multiply("0+1i", "0+1i");
        logCase("smallest-imag-unit", "(\"0+1i\", \"0+1i\")", "-1+0i", actualImagUnit);
        assertEquals("-1+0i", actualImagUnit);
    }

    /**
     * Ordinary inputs with positive and negative components:
     * (2 + 3i)(4 + 5i) = (8 - 15) + (10 + 12)i = -7 + 22i;
     * (1 - 2i)(-3 + 4i) = (-3 + 8) + (4 + 6)i = 5 + 10i.
     */
    @Test
    public void ordinaryInputsMatchExpectedContract() {
        String actualPos = ComplexNumberMultiply.multiply("2+3i", "4+5i");
        logCase("ordinary-positive", "(\"2+3i\", \"4+5i\")", "-7+22i", actualPos);
        assertEquals("-7+22i", actualPos);

        String actualMixed = ComplexNumberMultiply.multiply("1+-2i", "-3+4i");
        logCase("ordinary-mixed-signs", "(\"1+-2i\", \"-3+4i\")", "5+10i", actualMixed);
        assertEquals("5+10i", actualMixed);
    }

    /**
     * Repeated-value and domain boundary cases: repeated identical operands, multiplicative identity,
     * zero absorption, pure imaginary operands, conjugate pairs, and negative-imaginary formatting
     * ({@code "a+-bi"} per line 46).
     */
    @Test
    public void repeatedValuesAndDomainBoundariesMatchExpectedContract() {
        String actualRepeatedPos = ComplexNumberMultiply.multiply("1+1i", "1+1i");
        logCase("repeated-1+1i", "(\"1+1i\", \"1+1i\")", "0+2i", actualRepeatedPos);
        assertEquals("0+2i", actualRepeatedPos);

        String actualRepeatedNeg = ComplexNumberMultiply.multiply("-1+-1i", "-1+-1i");
        logCase("repeated--1+-1i", "(\"-1+-1i\", \"-1+-1i\")", "0+2i", actualRepeatedNeg);
        assertEquals("0+2i", actualRepeatedNeg);

        String actualRepeatedNegI = ComplexNumberMultiply.multiply("0+-1i", "0+-1i");
        logCase("repeated-0+-1i", "(\"0+-1i\", \"0+-1i\")", "-1+0i", actualRepeatedNegI);
        assertEquals("-1+0i", actualRepeatedNegI);

        String actualIdentity = ComplexNumberMultiply.multiply("1+0i", "-7+-9i");
        logCase("boundary-identity", "(\"1+0i\", \"-7+-9i\")", "-7+-9i", actualIdentity);
        assertEquals("-7+-9i", actualIdentity);

        String actualZeroAbsorb = ComplexNumberMultiply.multiply("0+0i", "-7+-9i");
        logCase("boundary-zero-absorb", "(\"0+0i\", \"-7+-9i\")", "0+0i", actualZeroAbsorb);
        assertEquals("0+0i", actualZeroAbsorb);

        String actualPureImag = ComplexNumberMultiply.multiply("0+3i", "0+4i");
        logCase("boundary-pure-imag", "(\"0+3i\", \"0+4i\")", "-12+0i", actualPureImag);
        assertEquals("-12+0i", actualPureImag);

        String actualConjugate = ComplexNumberMultiply.multiply("3+4i", "3+-4i");
        logCase("boundary-conjugate", "(\"3+4i\", \"3+-4i\")", "25+0i", actualConjugate);
        assertEquals("25+0i", actualConjugate);

        String actualMultiDigit = ComplexNumberMultiply.multiply("-12+15i", "10+-20i");
        logCase("boundary-multi-digit", "(\"-12+15i\", \"10+-20i\")", "180+390i", actualMultiDigit);
        assertEquals("180+390i", actualMultiDigit);
    }

    /**
     * Invalid format and null-input contract (lines 13-15):
     * {@code parse(String)} rejects {@code null} and any string not matching {@code -?\d+\+-?\d+i}
     * with {@code IllegalArgumentException("Invalid complex number format: " + num)}.
     */
    @Test
    public void invalidInputsThrowIllegalArgumentExceptionWithExpectedMessage() {
        IllegalArgumentException nullFirst = assertThrows(
            IllegalArgumentException.class, () -> ComplexNumberMultiply.multiply(null, "1+1i"));
        logCase("invalid-null-first", "(null, \"1+1i\")",
            "IllegalArgumentException: Invalid complex number format: null", nullFirst.getMessage());
        assertEquals("Invalid complex number format: null", nullFirst.getMessage());

        IllegalArgumentException nullSecond = assertThrows(
            IllegalArgumentException.class, () -> ComplexNumberMultiply.multiply("1+1i", null));
        logCase("invalid-null-second", "(\"1+1i\", null)",
            "IllegalArgumentException: Invalid complex number format: null", nullSecond.getMessage());
        assertEquals("Invalid complex number format: null", nullSecond.getMessage());

        String[] badInputs = {"1+1", "1-1i", " 1+1i", "", "a+bi", "+1i"};
        for (String bad : badInputs) {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> ComplexNumberMultiply.multiply(bad, "1+0i"));
            String expectedMsg = "Invalid complex number format: " + bad;
            logCase("invalid-format-" + bad, "(\"" + bad + "\", \"1+0i\")", expectedMsg, ex.getMessage());
            assertEquals(expectedMsg, ex.getMessage());
        }
    }

    /**
     * Separately verifies absence of observable state mutation across repeated calls.
     */
    @Test
    public void repeatedCallsHaveNoObservableStateMutation() {
        String a = new String("2+3i");
        String b = new String("4+5i");
        String first = ComplexNumberMultiply.multiply(a, b);
        String second = ComplexNumberMultiply.multiply(a, b);
        logCase("purity-repeated-call", "(\"2+3i\", \"4+5i\")", "-7+22i", first + " / " + second);
        assertEquals("2+3i", a);
        assertEquals("4+5i", b);
        assertEquals("-7+22i", first);
        assertEquals("-7+22i", second);
    }
}
