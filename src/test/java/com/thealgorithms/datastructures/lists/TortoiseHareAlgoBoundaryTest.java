package com.thealgorithms.datastructures.lists;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Boundary tests for {@link TortoiseHareAlgo.Node} and {@link TortoiseHareAlgo}.
 *
 * <p>Source contract ({@code TortoiseHareAlgo.java}):
 * <ul>
 * <li>lines 27-35: package-private {@code static final class Node<E>} with fields {@code next}
 * (line 28) and {@code value} (line 29), and {@code private Node(E value, Node<E> next)}
 * (lines 31-34) storing {@code value} (including {@code null}) and {@code next}</li>
 * <li>line 37 &amp; lines 39-41: {@code private Node<E> head = null} starts empty</li>
 * <li>lines 43-54: {@code append(E value)} allocates {@code new Node<>(value, null)} (line 44),
 * sets {@code head = newNode} when empty (lines 45-48), or appends at tail (lines 49-53)</li>
 * <li>lines 56-70: {@code getMiddle()} returns {@code null} sentinel on empty list (lines 57-59);
 * advances {@code slow} by 1 and {@code fast} by 2 while {@code fast != null && fast.next != null}
 * (lines 64-67), returning {@code slow.value} (line 69, second middle for even lengths)</li>
 * <li>lines 72-85: {@code toString()} formats {@code "[e0, e1, ...]"} or {@code "[]"}</li>
 * </ul>
 *
 * <p>Boundary partition:
 * <ul>
 * <li>Valid boundaries: 0 nodes (empty, {@code head == null}), 1 node (smallest non-empty,
 * {@code fast.next == null} skips loop), 1 node with {@code null} value, 2 nodes (smallest even,
 * second middle), 3 nodes (smallest odd entering loop).</li>
 * <li>Valid interior: ordinary multi-node lists (4 nodes, 5 nodes) and step-by-step growth.</li>
 * <li>Encapsulated / unsupported externally: {@code Node(E, Node<E>)} is {@code private} (line 31)
 * and {@code head} is {@code private} (line 37), preventing direct cyclic link construction.</li>
 * </ul>
 */
public class TortoiseHareAlgoBoundaryTest {

    private record BoundaryCase(
        String id,
        String partition,
        List<Integer> values,
        Integer expectedMiddle,
        String expectedString
    ) {
    }

    private static void logBoundary(
        String id,
        String partition,
        String input,
        Object expected,
        Object actual
    ) {
        System.out.println("[R45-T012] " + id + " [" + partition + "] | input=" + input
            + " | expected=" + expected + " | actual=" + actual);
    }

    /**
     * Smallest valid input for {@code Node} (single node created by {@code append(7)} at line 44)
     * alongside the empty-list sentinel boundary ({@code head == null} at line 57).
     */
    @Test
    public void smallestValidNodeAndEmptyBoundaryMatchExpectedContract() {
        TortoiseHareAlgo<Integer> empty = new TortoiseHareAlgo<>();
        Integer emptyMiddle = empty.getMiddle();
        String emptyStr = empty.toString();
        logBoundary("boundary-empty-middle", "valid-boundary", "[]", null, emptyMiddle);
        logBoundary("boundary-empty-str", "valid-boundary", "[]", "[]", emptyStr);
        assertNull(emptyMiddle);
        assertEquals("[]", emptyStr);

        TortoiseHareAlgo<Integer> single = new TortoiseHareAlgo<>();
        single.append(7);
        Integer singleMiddle = single.getMiddle();
        String singleStr = single.toString();
        logBoundary("smallest-single-middle", "valid-boundary", "[7]", 7, singleMiddle);
        logBoundary("smallest-single-str", "valid-boundary", "[7]", "[7]", singleStr);
        assertEquals(7, singleMiddle);
        assertEquals("[7]", singleStr);
    }

    /**
     * Ordinary interior input (5 nodes: {@code [10, 20, 30, 40, 50]}), plus step-by-step growth
     * across boundaries 0 -> 1 -> 2 -> 3 -> 4 -> 5.
     */
    @Test
    public void ordinaryInteriorAndGrowthAcrossBoundariesMatchExpectedContract() {
        TortoiseHareAlgo<Integer> list = new TortoiseHareAlgo<>();
        int[] inputs = {10, 20, 30, 40, 50};
        Integer[] expectedMiddlesAfterAppend = {10, 20, 20, 30, 30};
        String[] expectedStringsAfterAppend = {
            "[10]",
            "[10, 20]",
            "[10, 20, 30]",
            "[10, 20, 30, 40]",
            "[10, 20, 30, 40, 50]"
        };

        for (int i = 0; i < inputs.length; i++) {
            list.append(inputs[i]);
            Integer actualMiddle = list.getMiddle();
            String actualStr = list.toString();
            logBoundary("growth-step-" + (i + 1), "valid-interior",
                expectedStringsAfterAppend[i],
                expectedMiddlesAfterAppend[i] + " / " + expectedStringsAfterAppend[i],
                actualMiddle + " / " + actualStr);
            assertEquals(expectedMiddlesAfterAppend[i], actualMiddle);
            assertEquals(expectedStringsAfterAppend[i], actualStr);
        }
    }

    /**
     * Explicit boundary table covering each side of the documented boundaries:
     * empty (0 nodes), single non-null (1 node), single null (1 node), smallest even (2 nodes),
     * smallest odd entering loop (3 nodes), and interior even/odd (4 and 5 nodes).
     */
    @Test
    public void boundaryTableAssertionsCoverEachDocumentedSide() {
        List<BoundaryCase> table = List.of(
            new BoundaryCase("b-00-empty", "valid-boundary", List.of(), null, "[]"),
            new BoundaryCase("b-01-single", "valid-boundary", List.of(42), 42, "[42]"),
            new BoundaryCase("b-02-two-even", "valid-boundary", List.of(1, 2), 2, "[1, 2]"),
            new BoundaryCase("b-03-three-odd", "valid-boundary", List.of(1, 2, 3), 2, "[1, 2, 3]"),
            new BoundaryCase("b-04-four-even", "valid-interior", List.of(1, 2, 3, 4), 3, "[1, 2, 3, 4]"),
            new BoundaryCase("b-05-five-odd", "valid-interior", List.of(1, 2, 3, 4, 5), 3, "[1, 2, 3, 4, 5]")
        );

        for (BoundaryCase bc : table) {
            TortoiseHareAlgo<Integer> list = new TortoiseHareAlgo<>();
            for (Integer v : bc.values()) {
                list.append(v);
            }
            Integer actualMiddle = list.getMiddle();
            String actualStr = list.toString();
            logBoundary(bc.id(), bc.partition(), bc.values().toString(),
                bc.expectedMiddle() + " / " + bc.expectedString(),
                actualMiddle + " / " + actualStr);
            assertEquals(bc.expectedMiddle(), actualMiddle, "Middle mismatch for " + bc.id());
            assertEquals(bc.expectedString(), actualStr, "String mismatch for " + bc.id());
        }
    }

    /**
     * Null-value boundary: {@code Node} permits {@code null} element values (line 32),
     * distinguishing a single {@code null}-valued node ({@code "[null]"}) from an empty list
     * ({@code "[]"}).
     */
    @Test
    public void nullElementValueIsPermittedByNodeContract() {
        TortoiseHareAlgo<String> singleNull = new TortoiseHareAlgo<>();
        singleNull.append(null);
        String middle = singleNull.getMiddle();
        String str = singleNull.toString();
        logBoundary("boundary-single-null", "valid-boundary", "[null]", "null / [null]", middle + " / " + str);
        assertNull(middle);
        assertEquals("[null]", str);

        TortoiseHareAlgo<String> mixedNull = new TortoiseHareAlgo<>();
        mixedNull.append("a");
        mixedNull.append(null);
        mixedNull.append("c");
        String mixedMiddle = mixedNull.getMiddle();
        String mixedStr = mixedNull.toString();
        logBoundary("boundary-mixed-null", "valid-interior", "[a, null, c]",
            "null / [a, null, c]", mixedMiddle + " / " + mixedStr);
        assertNull(mixedMiddle);
        assertEquals("[a, null, c]", mixedStr);
    }

    /**
     * Encapsulation boundary: {@code Node} is package-private {@code static final} (line 27) and its
     * constructor {@code Node(E, Node<E>)} is {@code private} (line 31), so external code cannot
     * instantiate {@code Node} with an arbitrary {@code next} reference to form cycles.
     */
    @Test
    public void nodeConstructorEncapsulationBoundaryIsPreserved() throws NoSuchMethodException {
        Constructor<?> ctor =
            TortoiseHareAlgo.Node.class.getDeclaredConstructor(Object.class, TortoiseHareAlgo.Node.class);
        boolean isPrivate = Modifier.isPrivate(ctor.getModifiers());
        boolean isClassFinal = Modifier.isFinal(TortoiseHareAlgo.Node.class.getModifiers());
        logBoundary("boundary-node-encapsulation", "encapsulated-boundary",
            "Node(Object, Node)", "private=true,final=true",
            "private=" + isPrivate + ",final=" + isClassFinal);
        assertTrue(isPrivate);
        assertTrue(isClassFinal);
    }
}
