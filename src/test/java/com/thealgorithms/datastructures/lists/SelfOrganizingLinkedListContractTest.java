package com.thealgorithms.datastructures.lists;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for {@link SelfOrganizingLinkedList}.
 *
 * These tests lock down the guaranteed behavioral contracts of the
 * Move-To-Front self-organizing linked list:
 *
 * <ul>
 *   <li>C1: A newly constructed list is empty (size == 0, isEmpty == true, head == null).</li>
 *   <li>C2: insert() always appends to the tail and increments size by exactly 1.</li>
 *   <li>C3: search() on a present, non-head element moves it to the head (Move-To-Front).</li>
 *   <li>C4: search() on the current head returns true without changing the head.</li>
 *   <li>C5: search() on an absent element returns false and does not modify the list.</li>
 *   <li>C6: search() on an empty list returns false.</li>
 *   <li>C7: After a sequence of search operations, size remains unchanged.</li>
 *   <li>C8: insert() and search() work correctly with null elements.</li>
 *   <li>C9: The list works with different generic types (String, Integer).</li>
 * </ul>
 *
 * Fixes: #21
 */
public class SelfOrganizingLinkedListContractTest {

    private SelfOrganizingLinkedList<Integer> list;

    @BeforeEach
    void setUp() {
        list = new SelfOrganizingLinkedList<>();
    }

    // ── C1: New list contract ────────────────────────────────────────────

    @Test
    void contractNewListIsEmpty() {
        assertTrue(list.isEmpty(), "A new list must be empty");
        assertEquals(0, list.getSize(), "A new list must have size 0");
        assertNull(list.getHeadValue(), "Head of an empty list must be null");
    }

    // ── C2: insert() appends to tail and increments size ────────────────

    @Test
    void contractInsertAppendsToTailAndIncrementsSize() {
        list.insert(10);
        assertEquals(1, list.getSize());
        assertEquals(10, list.getHeadValue(), "Single element must be the head");

        list.insert(20);
        assertEquals(2, list.getSize());
        assertEquals(10, list.getHeadValue(), "Head must remain the first inserted element");

        list.insert(30);
        assertEquals(3, list.getSize());
        assertEquals(10, list.getHeadValue(), "Head must remain the first inserted element");
    }

    @Test
    void contractInsertIncrementsExactlyByOne() {
        for (int i = 0; i < 100; i++) {
            assertEquals(i, list.getSize());
            list.insert(i);
            assertEquals(i + 1, list.getSize());
        }
    }

    // ── C3: search() moves found (non-head) element to head ────────────

    @Test
    void contractSearchMovesFoundElementToHead() {
        list.insert(1);
        list.insert(2);
        list.insert(3);

        assertTrue(list.search(3), "Element 3 must be found");
        assertEquals(3, list.getHeadValue(), "Searched element must become the new head");
    }

    @Test
    void contractSearchMovesMiddleElementToHead() {
        list.insert(1);
        list.insert(2);
        list.insert(3);
        list.insert(4);
        list.insert(5);

        assertTrue(list.search(3));
        assertEquals(3, list.getHeadValue(), "Middle element must move to head after search");
    }

    @Test
    void contractSearchMovesTailElementToHead() {
        list.insert(1);
        list.insert(2);
        list.insert(3);

        assertTrue(list.search(3));
        assertEquals(3, list.getHeadValue(), "Tail element must move to head after search");
    }

    // ── C4: search() on head returns true without changing head ─────────

    @Test
    void contractSearchOnHeadDoesNotChangeHead() {
        list.insert(10);
        list.insert(20);
        list.insert(30);

        assertEquals(10, list.getHeadValue());
        assertTrue(list.search(10), "Searching head element must return true");
        assertEquals(10, list.getHeadValue(), "Head must remain unchanged when searched element is already head");
    }

    // ── C5: search() on absent element returns false, no side effects ──

    @Test
    void contractSearchAbsentReturnsFalseNoSideEffects() {
        list.insert(10);
        list.insert(20);
        list.insert(30);

        int sizeBefore = list.getSize();
        Object headBefore = list.getHeadValue();

        assertFalse(list.search(999), "Searching absent element must return false");
        assertEquals(sizeBefore, list.getSize(), "Size must not change on failed search");
        assertEquals(headBefore, list.getHeadValue(), "Head must not change on failed search");
    }

    // ── C6: search() on empty list returns false ────────────────────────

    @Test
    void contractSearchOnEmptyListReturnsFalse() {
        assertFalse(list.search(42), "Search on empty list must return false");
        assertEquals(0, list.getSize(), "Size must remain 0");
        assertNull(list.getHeadValue(), "Head must remain null");
    }

    // ── C7: search() does not alter size ────────────────────────────────

    @Test
    void contractSearchSequencePreservesSize() {
        list.insert(1);
        list.insert(2);
        list.insert(3);
        list.insert(4);
        list.insert(5);

        int originalSize = list.getSize();

        // Search every element, including absent ones
        list.search(5);
        list.search(3);
        list.search(1);
        list.search(999);
        list.search(2);
        list.search(4);

        assertEquals(originalSize, list.getSize(), "Size must be invariant under search operations");
    }

    // ── C8: null element handling ───────────────────────────────────────

    @Test
    void contractNullElementCanBeInsertedAndFound() {
        list.insert(null);
        assertEquals(1, list.getSize());
        assertNull(list.getHeadValue());
        assertTrue(list.search(null), "Null element must be searchable");
    }

    @Test
    void contractNullSearchOnNonNullListReturnsFalse() {
        list.insert(10);
        list.insert(20);

        assertFalse(list.search(null), "Searching null in a list without nulls must return false");
    }

    // ── C9: Generic type contract ───────────────────────────────────────

    @Test
    void contractWorksWithStringType() {
        SelfOrganizingLinkedList<String> strList = new SelfOrganizingLinkedList<>();
        strList.insert("alpha");
        strList.insert("beta");
        strList.insert("gamma");

        assertTrue(strList.search("gamma"));
        assertEquals("gamma", strList.getHeadValue());
        assertEquals(3, strList.getSize());
    }

    // ── Additional contract: consecutive searches cycle through correctly ──

    @Test
    void contractConsecutiveSearchesMaintainCorrectOrder() {
        list.insert(1);
        list.insert(2);
        list.insert(3);

        // Initial: [1, 2, 3]
        assertTrue(list.search(3)); // -> [3, 1, 2]
        assertEquals(3, list.getHeadValue());

        assertTrue(list.search(2)); // -> [2, 3, 1]
        assertEquals(2, list.getHeadValue());

        assertTrue(list.search(1)); // -> [1, 2, 3]
        assertEquals(1, list.getHeadValue());

        // Verify all elements still findable
        assertTrue(list.search(1));
        assertTrue(list.search(2));
        assertTrue(list.search(3));
        assertEquals(3, list.getSize());
    }

    @Test
    void contractSingleElementListSearch() {
        list.insert(42);
        assertTrue(list.search(42));
        assertEquals(42, list.getHeadValue());
        assertEquals(1, list.getSize());
        assertFalse(list.search(99));
        assertEquals(1, list.getSize());
    }
}
