package com.thealgorithms.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Reference-comparison regression tests for {@link FairShareScheduling.User#addWeight(int)}
 * in {@link FairShareScheduling}.
 *
 * <p>Source facts ({@code FairShareScheduling.java}):
 * <ul>
 * <li>line 18: {@code User} is package-private static inner class holding per-user state</li>
 * <li>lines 23-27: {@code User(String)} initializes {@code allocatedResources = 0} (line 25)
 * and {@code totalWeight = 0} (line 26)</li>
 * <li>lines 39-41: {@code addWeight(int weight)} is the only weight-accumulation method; it
 * mutates {@code this.totalWeight += weight} (line 40) with no I/O, time, or random dependency</li>
 * <li>lines 50-52: {@code addUser(String)} registers a fresh {@code User} via
 * {@code putIfAbsent}</li>
 * <li>lines 54-59: {@code addTask(String, int)} looks up the user and delegates to
 * {@code user.addWeight(weight)} only when {@code user != null} (line 56)</li>
 * <li>lines 61-66: {@code allocateResources(int)} computes
 * {@code (int) ((double) user.totalWeight / totalWeights * totalResources)}</li>
 * <li>lines 68-74: {@code getAllocatedResources()} returns a fresh {@code HashMap} snapshot (line 69)</li>
 * </ul>
 *
 * <p>Expected values are hand-calculated literals and cross-checked against an independent
 * reference calculation that does not call {@code addWeight} or {@code FairShareScheduling}.
 */
public class FairShareSchedulingReferenceTest {

    private record WeightCase(String id, int[] weights, int expectedTotalWeight) {
    }

    private record AllocationCase(
        String id,
        Map<String, List<Integer>> userTaskWeights,
        int totalResources,
        Map<String, Integer> expectedAllocation
    ) {
    }

    private static int independentWeightSum(int[] weights) {
        return Arrays.stream(weights).sum();
    }

    private static Map<String, Integer> independentFairShareOracle(
        Map<String, List<Integer>> userTaskWeights,
        int totalResources
    ) {
        Map<String, Integer> totals = new HashMap<>();
        int grandTotal = 0;
        for (Map.Entry<String, List<Integer>> entry : userTaskWeights.entrySet()) {
            int userTotal = 0;
            for (int w : entry.getValue()) {
                userTotal += w;
            }
            totals.put(entry.getKey(), userTotal);
            grandTotal += userTotal;
        }
        Map<String, Integer> expected = new HashMap<>();
        BigDecimal totalWeightDec = BigDecimal.valueOf(grandTotal);
        BigDecimal totalResDec = BigDecimal.valueOf(totalResources);
        for (Map.Entry<String, Integer> entry : totals.entrySet()) {
            BigDecimal share = BigDecimal.valueOf(entry.getValue())
                                   .divide(totalWeightDec, 16, RoundingMode.DOWN)
                                   .multiply(totalResDec);
            expected.put(entry.getKey(), share.intValue());
        }
        return expected;
    }

    private static void logComparison(String id, String input, Object expected, Object actual) {
        System.out.println("[R45-T013] " + id + " | input=" + input
            + " | expected=" + expected + " | actual=" + actual);
    }

    /**
     * Smallest valid inputs for {@code User.addWeight(int)}:
     * zero weight (0) and minimal positive weight (1), starting from {@code totalWeight = 0}
     * (line 26).
     */
    @Test
    public void smallestValidInputMatchesLiteralExpectedOutput() {
        FairShareScheduling.User zeroUser = new FairShareScheduling.User("zero");
        zeroUser.addWeight(0);
        logComparison("smallest-zero", "weights=[0]", 0, zeroUser.totalWeight);
        assertEquals(0, zeroUser.totalWeight);

        FairShareScheduling.User minPositiveUser = new FairShareScheduling.User("u1");
        minPositiveUser.addWeight(1);
        logComparison("smallest-one", "weights=[1]", 1, minPositiveUser.totalWeight);
        assertEquals(1, minPositiveUser.totalWeight);

        FairShareScheduling scheduler = new FairShareScheduling();
        scheduler.addUser("u1");
        scheduler.addTask("u1", 1);
        scheduler.allocateResources(1);
        Map<String, Integer> expected = Map.of("u1", 1);
        Map<String, Integer> actual = scheduler.getAllocatedResources();
        logComparison("smallest-scheduler", "u1:[1],totalResources=1", expected, actual);
        assertEquals(expected, actual);
    }

    /**
     * Ordinary input for {@code User.addWeight(int)}:
     * starting from {@code totalWeight = 0} (line 26), adding weights {@code 2} and {@code 3}
     * yields {@code 5} (line 40); paired with {@code bob} having weight {@code 5} across
     * {@code totalResources = 100}, each receives {@code 50}.
     */
    @Test
    public void ordinaryInputMatchesLiteralExpectedOutput() {
        FairShareScheduling.User alice = new FairShareScheduling.User("alice");
        alice.addWeight(2);
        assertEquals(2, alice.totalWeight);
        alice.addWeight(3);
        logComparison("ordinary-alice", "weights=[2, 3]", 5, alice.totalWeight);
        assertEquals(5, alice.totalWeight);

        FairShareScheduling scheduler = new FairShareScheduling();
        scheduler.addUser("alice");
        scheduler.addUser("bob");
        scheduler.addTask("alice", 2);
        scheduler.addTask("alice", 3);
        scheduler.addTask("bob", 5);
        scheduler.allocateResources(100);
        Map<String, Integer> expected = Map.of("alice", 50, "bob", 50);
        Map<String, Integer> actual = scheduler.getAllocatedResources();
        logComparison("ordinary-scheduler", "alice:[2,3],bob:[5],totalResources=100", expected, actual);
        assertEquals(expected, actual);
    }

    /**
     * Enumerates 24 small valid weight-accumulation sequences and 6 multi-user allocation cases,
     * comparing {@code addWeight} and {@code FairShareScheduling} against both a hand-calculated
     * literal table and an independent {@code IntStream} / {@code BigDecimal} reference oracle.
     */
    @Test
    public void enumeratedSmallInputsMatchReferenceTableAndOracle() {
        List<WeightCase> weightCases = List.of(
            new WeightCase("w-01-empty", new int[] {}, 0),
            new WeightCase("w-02-zero", new int[] {0}, 0),
            new WeightCase("w-03-one", new int[] {1}, 1),
            new WeightCase("w-04-two", new int[] {2}, 2),
            new WeightCase("w-05-three", new int[] {3}, 3),
            new WeightCase("w-06-five", new int[] {5}, 5),
            new WeightCase("w-07-seven", new int[] {7}, 7),
            new WeightCase("w-08-ten", new int[] {10}, 10),
            new WeightCase("w-09-zero-zero", new int[] {0, 0}, 0),
            new WeightCase("w-10-zero-one", new int[] {0, 1}, 1),
            new WeightCase("w-11-one-zero", new int[] {1, 0}, 1),
            new WeightCase("w-12-one-one", new int[] {1, 1}, 2),
            new WeightCase("w-13-one-two", new int[] {1, 2}, 3),
            new WeightCase("w-14-two-three", new int[] {2, 3}, 5),
            new WeightCase("w-15-three-four", new int[] {3, 4}, 7),
            new WeightCase("w-16-four-six", new int[] {4, 6}, 10),
            new WeightCase("w-17-ones", new int[] {1, 1, 1}, 3),
            new WeightCase("w-18-zero-two-three", new int[] {0, 2, 3}, 5),
            new WeightCase("w-19-one-two-three", new int[] {1, 2, 3}, 6),
            new WeightCase("w-20-two-two-five", new int[] {2, 2, 5}, 9),
            new WeightCase("w-21-two-three-five", new int[] {2, 3, 5}, 10),
            new WeightCase("w-22-asc-four", new int[] {1, 2, 3, 4}, 10),
            new WeightCase("w-23-desc-four", new int[] {4, 3, 2, 1}, 10),
            new WeightCase("w-24-zeros-fives", new int[] {0, 5, 0, 5}, 10)
        );

        for (WeightCase tc : weightCases) {
            int oracleTotal = independentWeightSum(tc.weights());
            assertEquals(tc.expectedTotalWeight(), oracleTotal, "Table vs oracle mismatch for " + tc.id());

            FairShareScheduling.User user = new FairShareScheduling.User("u");
            for (int w : tc.weights()) {
                user.addWeight(w);
            }
            logComparison(tc.id(), Arrays.toString(tc.weights()), tc.expectedTotalWeight(), user.totalWeight);
            assertEquals(tc.expectedTotalWeight(), user.totalWeight, "addWeight mismatch for " + tc.id());
        }

        List<AllocationCase> allocationCases = List.of(
            new AllocationCase("alloc-01-single", Map.of("a", List.of(1)), 10, Map.of("a", 10)),
            new AllocationCase("alloc-02-equal", Map.of("a", List.of(1), "b", List.of(1)), 100,
                Map.of("a", 50, "b", 50)),
            new AllocationCase("alloc-03-split", Map.of("a", List.of(2, 3), "b", List.of(5)), 100,
                Map.of("a", 50, "b", 50)),
            new AllocationCase("alloc-04-zero-user", Map.of("a", List.of(0), "b", List.of(4)), 20,
                Map.of("a", 0, "b", 20)),
            new AllocationCase("alloc-05-three-users",
                Map.of("a", List.of(1, 1), "b", List.of(3), "c", List.of(5)), 100,
                Map.of("a", 20, "b", 30, "c", 50)),
            new AllocationCase("alloc-06-floor",
                Map.of("a", List.of(1), "b", List.of(2), "c", List.of(3)), 10,
                Map.of("a", 1, "b", 3, "c", 5))
        );

        for (AllocationCase ac : allocationCases) {
            Map<String, Integer> oracle = independentFairShareOracle(ac.userTaskWeights(), ac.totalResources());
            assertEquals(ac.expectedAllocation(), oracle, "Table vs oracle mismatch for " + ac.id());

            FairShareScheduling scheduler = new FairShareScheduling();
            for (Map.Entry<String, List<Integer>> entry : ac.userTaskWeights().entrySet()) {
                scheduler.addUser(entry.getKey());
                for (int w : entry.getValue()) {
                    scheduler.addTask(entry.getKey(), w);
                }
            }
            scheduler.allocateResources(ac.totalResources());
            Map<String, Integer> actual = scheduler.getAllocatedResources();
            logComparison(ac.id(), ac.userTaskWeights() + ",totalResources=" + ac.totalResources(),
                ac.expectedAllocation(), actual);
            assertEquals(ac.expectedAllocation(), actual, "Allocation mismatch for " + ac.id());
        }
    }

    /**
     * Verifies equivalent fresh inputs under controlled state, unknown-user no-op guard (line 56),
     * and defensive copy behavior of {@link FairShareScheduling#getAllocatedResources()} (line 69).
     */
    @Test
    public void freshEquivalentInputsAndControlledStatePreserveContract() {
        FairShareScheduling grouped = new FairShareScheduling();
        grouped.addUser("alice");
        grouped.addUser("bob");
        grouped.addTask("alice", 2);
        grouped.addTask("alice", 3);
        grouped.addTask("bob", 5);
        // Unregistered user must be ignored without affecting existing weights (line 56).
        grouped.addTask("unknown", 100);
        grouped.allocateResources(100);

        FairShareScheduling combined = new FairShareScheduling();
        combined.addUser("alice");
        combined.addUser("bob");
        combined.addTask("alice", 5);
        combined.addTask("bob", 5);
        combined.allocateResources(100);

        Map<String, Integer> groupedResult = grouped.getAllocatedResources();
        Map<String, Integer> combinedResult = combined.getAllocatedResources();
        logComparison("equivalent-state", "grouped([2,3],[5]) vs combined([5],[5])",
            combinedResult, groupedResult);
        assertEquals(Map.of("alice", 50, "bob", 50), groupedResult);
        assertEquals(combinedResult, groupedResult);
        assertFalse(groupedResult.containsKey("unknown"));

        // Mutating the returned map snapshot must not alter internal scheduler state (line 69).
        groupedResult.put("alice", 999);
        assertEquals(Map.of("alice", 50, "bob", 50), grouped.getAllocatedResources());
    }
}
