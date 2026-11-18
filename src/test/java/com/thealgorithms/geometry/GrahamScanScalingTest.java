package com.thealgorithms.geometry;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GrahamScanScalingTest {

    private static final int[] SIZES = {2_000, 8_000, 32_000};
    private static final int REPETITIONS = 5;
    private static final int COORD_BOUND = 10_000;

    private static Point[] generatePoints(int n, long seed) {
        Random rng = new Random(seed);
        Point[] pts = new Point[n];
        // Plant one unique lowest-y anchor at (0, -COORD_BOUND) and place remaining points
        // strictly above y = -COORD_BOUND so inputs stay within GrahamScan's non-degenerate
        // lowest-edge domain and well below 32-bit int cross-product overflow.
        pts[0] = new Point(0, -COORD_BOUND);
        for (int i = 1; i < n; i++) {
            int x = rng.nextInt(2 * COORD_BOUND + 1) - COORD_BOUND;
            int y = rng.nextInt(2 * COORD_BOUND) - (COORD_BOUND - 1);
            pts[i] = new Point(x, y);
        }
        return pts;
    }

    private static void verifyConvexHull(Point[] originalInput, List<Point> hull) {
        assertTrue(hull.size() >= 3, "Non-degenerate random cloud must have at least 3 hull vertices");
        Set<Point> inputSet = new HashSet<>(Arrays.asList(originalInput));
        for (Point h : hull) {
            assertTrue(inputSet.contains(h), "Hull vertex must come from input set: " + h);
        }
        int k = hull.size();
        for (int i = 0; i < k; i++) {
            Point a = hull.get(i);
            Point b = hull.get((i + 1) % k);
            Point c = hull.get((i + 2) % k);
            assertTrue(Point.orientation(a, b, c) > 0, "Hull vertices must form strict CCW turns");
        }
        for (Point p : originalInput) {
            for (int i = 0; i < k; i++) {
                Point a = hull.get(i);
                Point b = hull.get((i + 1) % k);
                assertTrue(Point.orientation(a, b, p) >= 0, "Every input point must lie on or inside hull edge");
            }
        }
    }

    @Test
    @DisplayName("Complexity experiment: measure GrahamScan scaling across 3 sizes x 5 repetitions with correctness checks")
    void measureScalingAndVerifyCorrectness() {
        // Warm-up JVM outside timed measurements to separate JIT compilation from algorithm runtime.
        for (int w = 0; w < 3; w++) {
            new GrahamScan(generatePoints(2_000, 100L + w));
        }

        for (int n : SIZES) {
            for (int rep = 1; rep <= REPETITIONS; rep++) {
                long seed = 45039L + rep * 100_000L + n;
                Point[] freshInput = generatePoints(n, seed);
                Point[] snapshot = freshInput.clone();

                // Timed interval covers only GrahamScan construction (in-place sorts + hull scan).
                long startNs = System.nanoTime();
                GrahamScan scan = new GrahamScan(freshInput);
                long elapsedNs = System.nanoTime() - startNs;

                // Correctness verification runs outside the timed interval; no wall-clock pass/fail threshold.
                List<Point> hull = new ArrayList<>();
                for (Point p : scan.hull()) {
                    hull.add(p);
                }
                verifyConvexHull(snapshot, hull);

                System.out.println("TIMING|n=" + n + "|rep=" + rep + "|seed=" + seed + "|ns=" + elapsedNs + "|hull_size=" + hull.size() + "|valid=true");
            }
        }
    }
}
