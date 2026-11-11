package com.thealgorithms.datastructures.heaps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Correctness and scaling checks for {@link HeapElement}.
 *
 * <p>Source preconditions ({@code HeapElement.java}):
 * <ul>
 * <li>lines 37-38: immutable fields {@code final double key} and
 * {@code final Object additionalInfo}</li>
 * <li>lines 47-50: {@code HeapElement(double, Object)} performs 2 constant-time field
 * assignments</li>
 * <li>lines 100-103: {@code HeapElement(int)} initializes {@code additionalInfo = null}</li>
 * <li>lines 131-142: {@code getInfo()} and {@code getKey()} are constant-time field reads</li>
 * <li>lines 151-178: {@code toString()}, {@code equals(Object)}, and {@code hashCode()} compare
 * {@code key} and delegate to {@code additionalInfo} when non-null</li>
 * </ul>
 */
public class HeapElementScalingTest {

    private static final long FIXED_SEED = 20261111L;
    private static final int REPETITIONS = 5;
    private static final int[] SIZES = {1_000, 10_000, 100_000};

    private static void logRecord(String tag, String detail) {
        System.out.println("[" + tag + "] " + detail);
    }

    /**
     * Smallest valid input ({@code new HeapElement(0.0, null)} and {@code new HeapElement(0)})
     * and ordinary input ({@code new HeapElement(5.0, "Info")}) with independently calculated
     * literal expected outputs:
     * {@code "Info".hashCode() = ((73*31 + 110)*31 + 102)*31 + 111 = 2283726};
     * {@code 31 * 5 + 2283726 = 2283881}.
     */
    @Test
    public void smallestAndOrdinaryInputsMatchLiteralExpectedOutputs() {
        HeapElement smallest = new HeapElement(0.0, null);
        logRecord("R45-T019", "smallest-double-null | input=(0.0, null) | expected=0.0/null/0"
            + " | actual=" + smallest.getKey() + "/" + smallest.getInfo() + "/" + smallest.hashCode());
        assertEquals(0.0, smallest.getKey());
        assertNull(smallest.getInfo());
        assertEquals("Key: 0.0 - No additional info", smallest.toString());
        assertEquals(0, smallest.hashCode());
        assertEquals(new HeapElement(0), smallest);

        HeapElement ordinary = new HeapElement(5.0, "Info");
        logRecord("R45-T019", "ordinary-double-info | input=(5.0, \"Info\") | expected=5.0/Info/2283881"
            + " | actual=" + ordinary.getKey() + "/" + ordinary.getInfo() + "/" + ordinary.hashCode());
        assertEquals(5.0, ordinary.getKey());
        assertEquals("Info", ordinary.getInfo());
        assertEquals("Key: 5.0 - Info", ordinary.toString());
        assertEquals(2283881, ordinary.hashCode());
        assertEquals(new HeapElement(5, "Info"), ordinary);
        assertNotEquals(new HeapElement(5.0, "Other"), ordinary);
        assertNotEquals(new HeapElement(6.0, "Info"), ordinary);
    }

    /**
     * Scaling experiment over {@code N in {1000, 10000, 100000}} with fixed seed and 5 repetitions
     * per size. Fresh inputs are prepared before the timed interval, only {@code HeapElement}
     * construction and {@code getKey()}/{@code getInfo()} lookups are timed, and all {@code N}
     * outputs plus exact operation counts ({@code 3 * N}) are verified outside the timed interval.
     */
    @Test
    public void scalingAcrossThreeSizesPreservesCorrectnessAndLinearOperationCount() {
        Random rng = new Random(FIXED_SEED);

        // Warmup outside reported repetitions to reduce JIT compilation skew.
        for (int i = 0; i < 200; i++) {
            HeapElement w = new HeapElement((double) i, "w");
            assertEquals((double) i, w.getKey());
        }

        for (int n : SIZES) {
            long[] elapsedNanos = new long[REPETITIONS];
            long totalOpsPerRep = 0;

            for (int rep = 0; rep < REPETITIONS; rep++) {
                double[] keys = new double[n];
                String[] infos = new String[n];
                for (int i = 0; i < n; i++) {
                    keys[i] = rng.nextInt(10_000);
                    infos[i] = "item-" + (i & 63);
                }

                HeapElement[] elements = new HeapElement[n];
                double[] observedKeys = new double[n];
                Object[] observedInfos = new Object[n];

                long ops = 0;
                long start = System.nanoTime();
                for (int i = 0; i < n; i++) {
                    HeapElement el = new HeapElement(keys[i], infos[i]);
                    elements[i] = el;
                    observedKeys[i] = el.getKey();
                    observedInfos[i] = el.getInfo();
                    ops += 3; // 1 construction + 1 getKey + 1 getInfo
                }
                long duration = System.nanoTime() - start;
                elapsedNanos[rep] = duration;
                totalOpsPerRep = ops;

                // Verify every constructed element outside the timed interval.
                assertEquals(3L * n, ops);
                for (int i = 0; i < n; i++) {
                    assertEquals(keys[i], observedKeys[i]);
                    assertEquals(infos[i], observedInfos[i]);
                }
            }

            long[] sorted = Arrays.copyOf(elapsedNanos, elapsedNanos.length);
            Arrays.sort(sorted);
            long medianNs = sorted[REPETITIONS / 2];
            long minNs = sorted[0];
            long maxNs = sorted[REPETITIONS - 1];
            double medianNsPerElement = (double) medianNs / n;

            logRecord("R45-T019", "scaling-n-" + n + " | input=N=" + n + ",reps=" + REPETITIONS
                + ",seed=" + FIXED_SEED + " | expected=ops=" + (3L * n)
                + ",all_correct=true | actual=ops=" + totalOpsPerRep + ",all_correct=true");
            logRecord("R45-T019-TIMING", String.format(
                "N=%d | ops=%d | median_ns=%d | min_ns=%d | max_ns=%d | range_ns=%d | median_ns_per_elem=%.2f",
                n, totalOpsPerRep, medianNs, minNs, maxNs, maxNs - minNs, medianNsPerElement));
        }
    }
}
