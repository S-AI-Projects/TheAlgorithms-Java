package com.thealgorithms.bitmanipulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Xs3Conversion class.
 */
public class Xs3ConversionTest {

    /**
     * Test the xs3ToBinary method with an XS-3 number.
     */
    @Test
    public void testXs3ToBinary() {
        int binary = Xs3Conversion.xs3ToBinary(0x4567);
        assertEquals(1234, binary); // XS-3 0x4567 should convert to binary 1234
    }

    /**
     * Test the binaryToXs3 method with a binary number.
     */
    @Test
    public void testBinaryToXs3() {
        int xs3 = Xs3Conversion.binaryToXs3(1234);
        assertEquals(0x4567, xs3); // Binary 1234 should convert to XS-3 0x4567
    }

    /**
     * Test the xs3ToBinary method with zero.
     */
    @Test
    public void testXs3ToBinaryZero() {
        int binary = Xs3Conversion.xs3ToBinary(0x0);
        assertEquals(0, binary); // XS-3 0x0 should convert to binary 0
    }

    /**
     * Test the binaryToXs3 method with zero.
     */
    @Test
    public void testBinaryToXs3Zero() {
        int xs3 = Xs3Conversion.binaryToXs3(0);
        assertEquals(0x0, xs3); // Binary 0 should convert to XS-3 0x0
    }

    /**
     * Test the xs3ToBinary method with a single digit XS-3 number.
     */
    @Test
    public void testXs3ToBinarySingleDigit() {
        int binary = Xs3Conversion.xs3ToBinary(0x5);
        assertEquals(2, binary); // XS-3 0x5 should convert to binary 2
    }

    /**
     * Test the binaryToXs3 method with a single digit binary number.
     */
    @Test
    public void testBinaryToXs3SingleDigit() {
        int xs3 = Xs3Conversion.binaryToXs3(2);
        assertEquals(0x5, xs3); // Binary 2 should convert to XS-3 0x5
    }

    @Test
    @DisplayName("Smallest valid input zero fixture (Xs3Conversion.java:39,64)")
    public void testSmallestValidInputZeroFixture() {
        // Precondition: xs3 == 0 (Xs3Conversion.java:39), binary == 0 (Xs3Conversion.java:64)
        assertEquals(0, Xs3Conversion.xs3ToBinary(0));
        assertEquals(0x0, Xs3Conversion.binaryToXs3(0));
    }

    @Test
    @DisplayName("Smallest valid positive single-digit fixture (Xs3Conversion.java:40,65)")
    public void testSmallestValidPositiveSingleDigitFixture() {
        // Precondition: binary == 1 > 0 (Xs3Conversion.java:64), digit 1 + 3 = 4 (line 65)
        assertEquals(0x4, Xs3Conversion.binaryToXs3(1));
        assertEquals(1, Xs3Conversion.xs3ToBinary(0x4));
    }

    @Test
    @DisplayName("Ordinary input fixture from documentation (Xs3Conversion.java:39-44,64-69)")
    public void testOrdinaryInputDocumentationFixture() {
        // Precondition: positive multi-digit inputs (1234 <-> 0x4567)
        int binary = 1234;
        int xs3 = 0x4567;
        assertEquals(xs3, Xs3Conversion.binaryToXs3(binary));
        assertEquals(binary, Xs3Conversion.xs3ToBinary(xs3));
    }

    @Test
    @DisplayName("Ordinary input two-digit fixture (Xs3Conversion.java:39-44,64-69)")
    public void testOrdinaryInputTwoDigitFixture() {
        // 25: 5+3=8, 2+3=5 => 0x58 (88 decimal)
        int binary = 25;
        int xs3 = 0x58;
        assertEquals(xs3, Xs3Conversion.binaryToXs3(binary));
        assertEquals(binary, Xs3Conversion.xs3ToBinary(xs3));
    }

    @Test
    @DisplayName("Mutation resistance: protects nibble shift alignment (shift * 4, Xs3Conversion.java:66)")
    public void testMutationResistanceNibbleShiftAlignment() {
        // If line 66 is mutated from (shift * 4) to (shift), 25 produces 10 (0xA) instead of 88 (0x58)
        assertEquals(0x58, Xs3Conversion.binaryToXs3(25));
        // If line 66 is mutated to (shift), 89 produces 25 (0x19) instead of 188 (0xBC)
        assertEquals(0xBC, Xs3Conversion.binaryToXs3(89));
        // Multi-digit 3-digit number 100: 0+3=3, 0+3=3, 1+3=4 => 0x433
        assertEquals(0x433, Xs3Conversion.binaryToXs3(100));
    }

    @Test
    @DisplayName("Mutation resistance: protects decimal place multiplier accumulation (multiplier *= 10, Xs3Conversion.java:42)")
    public void testMutationResistanceMultiplierAccumulation() {
        // If line 42 is mutated from (*= 10) to (*= 16), 0x58 produces 37 instead of 25
        assertEquals(25, Xs3Conversion.xs3ToBinary(0x58));
        // If line 42 is mutated to (*= 16), 0xBC produces 137 instead of 89
        assertEquals(89, Xs3Conversion.xs3ToBinary(0xBC));
        // 0x433 should decode to 100
        assertEquals(100, Xs3Conversion.xs3ToBinary(0x433));
    }

    @Test
    @DisplayName("Bijective property across all decimal digits and multi-digit range [0, 999]")
    public void testBijectiveRoundTripAllDigitsAndMultiDigits() {
        for (int i = 0; i < 1000; i++) {
            int xs3 = Xs3Conversion.binaryToXs3(i);
            int decoded = Xs3Conversion.xs3ToBinary(xs3);
            assertEquals(i, decoded, "Round-trip conversion failed for: " + i);
        }
    }

    @Test
    @DisplayName("Verify Xs3Conversion private constructor via reflection")
    public void testPrivateConstructorContract() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<Xs3Conversion> constructor = Xs3Conversion.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        Xs3Conversion instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
