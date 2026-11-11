package com.thealgorithms.physics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ThinLensTest {

    @Test
    void testConvexLensRealImage() {
        double v = ThinLens.imageDistance(10, 20);
        assertEquals(20, v, 1e-6);
    }

    @Test
    void testMagnification() {
        assertEquals(2.0, ThinLens.magnification(20, 10), 1e-6);
    }

    /**
     * Smallest valid input fixture: f = 1.0, u = 2.0.
     * Preconditions established at:
     * - ThinLens.java lines 37-39 (focalLength and objectDistance != 0)
     * - ThinLens.java line 41 (1.0 / ((1.0 / f) - (1.0 / u)))
     * - ThinLens.java lines 58-60 (objectDistance != 0)
     * - ThinLens.java line 72 (isRealImage: imageDistance > 0)
     */
    @Test
    void testSmallestValidInputFixture() {
        double f = 1.0;
        double u = 2.0;

        // Independently calculated expected values:
        // 1/f = 1.0; 1/u = 0.5 -> 1/v = 1.0 - 0.5 = 0.5 -> v = 2.0
        double expectedV = 2.0;
        double expectedM = 1.0;
        boolean expectedIsReal = true;

        double v = ThinLens.imageDistance(f, u);
        assertEquals(expectedV, v, 1e-9, "Image distance must equal 2.0 for smallest valid input.");

        double m = ThinLens.magnification(v, u);
        assertEquals(expectedM, m, 1e-9, "Magnification must equal 1.0 for smallest valid input.");

        assertEquals(expectedIsReal, ThinLens.isRealImage(v), "Image must be real when v > 0.");

        // Assert invariant: 1/v + 1/u == 1/f
        assertEquals(1.0 / f, (1.0 / v) + (1.0 / u), 1e-9, "Lens equation invariant 1/v + 1/u == 1/f must hold.");
    }

    /**
     * Ordinary input fixture: f = 10.0, u = 15.0.
     * Preconditions established at:
     * - ThinLens.java lines 37-39 (non-zero f, u)
     * - ThinLens.java line 41 (v calculation)
     * - ThinLens.java lines 58-60 (m calculation)
     * - ThinLens.java line 72 (isRealImage)
     */
    @Test
    void testOrdinaryInputFixture() {
        double f = 10.0;
        double u = 15.0;

        // Independently calculated expected values:
        // 1/10 - 1/15 = 3/30 - 2/30 = 1/30 -> v = 30.0
        double expectedV = 30.0;
        double expectedM = 2.0;
        boolean expectedIsReal = true;

        double v = ThinLens.imageDistance(f, u);
        assertEquals(expectedV, v, 1e-9, "Image distance must equal 30.0.");

        double m = ThinLens.magnification(v, u);
        assertEquals(expectedM, m, 1e-9, "Magnification must equal 2.0.");

        assertEquals(expectedIsReal, ThinLens.isRealImage(v));

        // Invariant checks:
        assertEquals(1.0 / f, (1.0 / v) + (1.0 / u), 1e-9, "Reciprocal invariant must hold.");
        assertEquals(u, ThinLens.imageDistance(f, v), 1e-9, "Conjugate foci reciprocity must hold.");
        assertEquals(v, m * u, 1e-9, "Magnification proportionality must hold.");
    }

    /**
     * Property test generating 100 bounded valid cases with a deterministic seed (42L).
     * Asserts fundamental physical invariants across the valid domain:
     * 1. Reciprocal relation: (1.0 / v) + (1.0 / u) == (1.0 / f)
     * 2. Conjugate foci involution: imageDistance(f, imageDistance(f, u)) == u
     * 3. Magnification consistency: magnification(v, u) * u == v
     * 4. Image type consistency: isRealImage(v) == (v > 0)
     */
    @Test
    void testPropertyThinLensInvariantsOver100BoundedCases() {
        Random random = new Random(42L);
        int validCasesTested = 0;

        while (validCasesTested < 100) {
            // Generate non-zero bounded focal length and object distance in [-50, -1] U [1, 50]
            double f = (random.nextBoolean() ? 1.0 : -1.0) * (1.0 + random.nextDouble() * 49.0);
            double u = (random.nextBoolean() ? 1.0 : -1.0) * (1.0 + random.nextDouble() * 49.0);

            // Avoid pole where 1/f == 1/u (f == u) to keep image distance well-conditioned
            if (Math.abs((1.0 / f) - (1.0 / u)) < 0.02) {
                continue;
            }

            double v = ThinLens.imageDistance(f, u);

            // Invariant 1: 1/f == 1/v + 1/u
            double reciprocalSum = (1.0 / v) + (1.0 / u);
            assertEquals(1.0 / f, reciprocalSum, 1e-9, "Invariant failure: 1/f must equal 1/v + 1/u.");

            // Invariant 2: Conjugate foci round-trip
            double roundTripU = ThinLens.imageDistance(f, v);
            assertEquals(u, roundTripU, 1e-6, "Invariant failure: conjugate foci reciprocity.");

            // Invariant 3: Magnification consistency
            double m = ThinLens.magnification(v, u);
            assertEquals(v, m * u, 1e-9, "Invariant failure: magnification * u must equal v.");

            // Invariant 4: Real image condition
            assertEquals(v > 0, ThinLens.isRealImage(v), "Invariant failure: isRealImage must match v > 0.");

            validCasesTested++;
        }

        assertEquals(100, validCasesTested, "Must test exactly 100 valid random cases.");
    }

    /**
     * Boundary fixture: zero focal length precondition violation.
     * Preconditions established at:
     * - ThinLens.java lines 37-39 (focalLength == 0 check)
     */
    @Test
    void testZeroFocalLengthThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> ThinLens.imageDistance(0, 10),
            "Zero focal length must throw IllegalArgumentException."
        );
        assertEquals("Focal length and object distance must be non-zero.", ex.getMessage());
    }

    /**
     * Boundary fixture: zero object distance precondition violation.
     * Preconditions established at:
     * - ThinLens.java lines 37-39 & lines 58-60 (objectDistance == 0 check)
     */
    @Test
    void testZeroObjectDistanceThrowsIllegalArgumentException() {
        IllegalArgumentException ex1 = assertThrows(
            IllegalArgumentException.class,
            () -> ThinLens.imageDistance(10, 0),
            "Zero object distance in imageDistance must throw IllegalArgumentException."
        );
        assertEquals("Focal length and object distance must be non-zero.", ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(
            IllegalArgumentException.class,
            () -> ThinLens.magnification(10, 0),
            "Zero object distance in magnification must throw IllegalArgumentException."
        );
        assertEquals("Object distance must be non-zero.", ex2.getMessage());
    }

    /**
     * Virtual image test fixture: concave lens producing negative image distance.
     * Preconditions established at:
     * - ThinLens.java line 72 (imageDistance > 0 is false when v < 0)
     */
    @Test
    void testVirtualImageFormation() {
        // Concave lens: f = -10, u = 20 -> 1/v = -1/10 - 1/20 = -3/20 -> v = -6.666667
        double f = -10.0;
        double u = 20.0;
        double v = ThinLens.imageDistance(f, u);
        assertEquals(-20.0 / 3.0, v, 1e-9);
        assertFalse(ThinLens.isRealImage(v), "Virtual image formed by diverging lens must have isRealImage == false.");
    }

    /**
     * Reflection contract test: private constructor throws AssertionError("No instances.").
     * Preconditions established at:
     * - ThinLens.java lines 23-25
     */
    @Test
    void testPrivateConstructorContract() throws Exception {
        Constructor<ThinLens> constructor = ThinLens.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(thrown.getCause() instanceof AssertionError);
        assertEquals("No instances.", thrown.getCause().getMessage());
    }
}
