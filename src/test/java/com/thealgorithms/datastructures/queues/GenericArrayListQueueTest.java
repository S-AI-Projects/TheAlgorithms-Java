package com.thealgorithms.datastructures.queues;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GenericArrayListQueueTest {

    @Test
    void testAdd() {
        GenericArrayListQueue<Integer> queue = new GenericArrayListQueue<>();
        assertTrue(queue.add(10));
        assertTrue(queue.add(20));
        assertEquals(10, queue.peek()); // Ensure the first added element is at the front
    }

    @Test
    void testPeek() {
        GenericArrayListQueue<Integer> queue = new GenericArrayListQueue<>();
        assertNull(queue.peek(), "Peek should return null for an empty queue");

        queue.add(10);
        queue.add(20);

        assertEquals(10, queue.peek(), "Peek should return the first element (10)");
        queue.poll();
        assertEquals(20, queue.peek(), "Peek should return the next element (20) after poll");
    }

    @Test
    void testPoll() {
        GenericArrayListQueue<Integer> queue = new GenericArrayListQueue<>();
        assertNull(queue.poll(), "Poll should return null for an empty queue");

        queue.add(10);
        queue.add(20);

        assertEquals(10, queue.poll(), "Poll should return and remove the first element (10)");
        assertEquals(20, queue.poll(), "Poll should return and remove the next element (20)");
        assertNull(queue.poll(), "Poll should return null when queue is empty after removals");
    }

    @Test
    void testIsEmpty() {
        GenericArrayListQueue<Integer> queue = new GenericArrayListQueue<>();
        assertTrue(queue.isEmpty(), "Queue should initially be empty");

        queue.add(30);
        assertFalse(queue.isEmpty(), "Queue should not be empty after adding an element");
        queue.poll();
        assertTrue(queue.isEmpty(), "Queue should be empty after removing the only element");
    }

    // ── Strengthened isEmpty Tests (Issue #30) ───────────────────────────

    @Test
    void testIsEmptyInitialState() {
        GenericArrayListQueue<String> queue = new GenericArrayListQueue<>();
        assertTrue(queue.isEmpty(), "A brand new queue must report isEmpty() == true");
        assertNull(queue.peek(), "peek() on an empty queue must be null");
    }

    @Test
    void testIsEmptyTransitionsDuringAddAndPoll() {
        GenericArrayListQueue<Integer> queue = new GenericArrayListQueue<>();
        assertTrue(queue.isEmpty());

        // First addition flips isEmpty from true to false
        queue.add(1);
        assertFalse(queue.isEmpty(), "Queue must not be empty after 1st add");

        // Second addition keeps isEmpty false
        queue.add(2);
        assertFalse(queue.isEmpty(), "Queue must remain non-empty after 2nd add");

        // Third addition keeps isEmpty false
        queue.add(3);
        assertFalse(queue.isEmpty(), "Queue must remain non-empty after 3rd add");

        // Polling 1st element: 2 remaining -> still not empty
        queue.poll();
        assertFalse(queue.isEmpty(), "Queue must remain non-empty with 2 elements remaining");

        // Polling 2nd element: 1 remaining -> still not empty
        queue.poll();
        assertFalse(queue.isEmpty(), "Queue must remain non-empty with 1 element remaining");

        // Polling last element: 0 remaining -> flips to true
        queue.poll();
        assertTrue(queue.isEmpty(), "Queue must become empty after polling the final element");
    }

    @Test
    void testIsEmptyWhenNullElementIsAdded() {
        GenericArrayListQueue<String> queue = new GenericArrayListQueue<>();
        assertTrue(queue.isEmpty());

        // Adding null still means the queue has 1 element
        queue.add(null);
        assertFalse(queue.isEmpty(), "Adding null must make the queue non-empty (size 1)");
        assertNull(queue.peek(), "peek() returns null because the element itself is null");
        assertFalse(queue.isEmpty(), "isEmpty() must still be false after peek()");

        // Polling the null element empties the queue
        assertNull(queue.poll());
        assertTrue(queue.isEmpty(), "Queue must be empty after polling the null element");
    }

    @Test
    void testIsEmptyInvarianceUnderPeek() {
        GenericArrayListQueue<Integer> queue = new GenericArrayListQueue<>();
        // Calling peek on empty queue does not affect isEmpty
        assertTrue(queue.isEmpty());
        assertNull(queue.peek());
        assertTrue(queue.isEmpty());

        // Calling peek on non-empty queue does not affect isEmpty
        queue.add(42);
        assertFalse(queue.isEmpty());
        for (int i = 0; i < 5; i++) {
            assertEquals(42, queue.peek());
            assertFalse(queue.isEmpty(), "peek() must never alter isEmpty() state");
        }
    }

    @Test
    void testIsEmptyInvarianceUnderPollOnEmptyQueue() {
        GenericArrayListQueue<Integer> queue = new GenericArrayListQueue<>();
        assertTrue(queue.isEmpty());

        // Multiple polls on empty queue must remain safe and keep isEmpty true
        for (int i = 0; i < 10; i++) {
            assertNull(queue.poll(), "poll() on empty queue must return null");
            assertTrue(queue.isEmpty(), "isEmpty() must remain true after polling empty queue");
        }
    }

    @Test
    void testIsEmptyAcrossMultipleRefillCycles() {
        GenericArrayListQueue<Integer> queue = new GenericArrayListQueue<>();

        for (int cycle = 0; cycle < 5; cycle++) {
            assertTrue(queue.isEmpty(), "Must be empty at start of cycle " + cycle);

            // Add 10 items
            for (int i = 0; i < 10; i++) {
                queue.add(i);
                assertFalse(queue.isEmpty());
            }

            // Drain 9 items (still not empty)
            for (int i = 0; i < 9; i++) {
                queue.poll();
                assertFalse(queue.isEmpty());
            }

            // Drain last item (now empty)
            queue.poll();
            assertTrue(queue.isEmpty(), "Must be empty at end of cycle " + cycle);
        }
    }

    @Test
    void testIsEmptyStressLargeNumberOfElements() {
        GenericArrayListQueue<Integer> queue = new GenericArrayListQueue<>();
        int count = 5000;

        for (int i = 0; i < count; i++) {
            queue.add(i);
            assertFalse(queue.isEmpty());
        }

        // Drain count - 1 items
        for (int i = 0; i < count - 1; i++) {
            queue.poll();
            assertFalse(queue.isEmpty(), "Must not be empty until the very last item is removed");
        }

        // Remove final element
        queue.poll();
        assertTrue(queue.isEmpty(), "Must be empty after removing all 5000 items");
    }

    @Test
    void testClearQueueAndReuse() {
        GenericArrayListQueue<Integer> queue = new GenericArrayListQueue<>();
        queue.add(5);
        queue.add(10);
        queue.poll();
        queue.poll(); // Remove all elements

        assertTrue(queue.isEmpty(), "Queue should be empty after all elements are removed");
        assertNull(queue.peek(), "Peek should return null on an empty queue after clear");
        assertTrue(queue.add(15), "Queue should be reusable after being emptied");
        assertEquals(15, queue.peek(), "Newly added element should be accessible in the empty queue");
    }

    @Test
    void testOrderMaintained() {
        GenericArrayListQueue<String> queue = new GenericArrayListQueue<>();
        queue.add("First");
        queue.add("Second");
        queue.add("Third");

        assertEquals("First", queue.poll(), "Order should be maintained; expected 'First'");
        assertEquals("Second", queue.poll(), "Order should be maintained; expected 'Second'");
        assertEquals("Third", queue.poll(), "Order should be maintained; expected 'Third'");
    }

    @Test
    void testVariousDataTypes() {
        GenericArrayListQueue<Double> queue = new GenericArrayListQueue<>();
        queue.add(1.1);
        queue.add(2.2);

        assertEquals(1.1, queue.peek(), "Queue should handle Double data type correctly");
        assertEquals(1.1, queue.poll(), "Poll should return correct Double value");
        assertEquals(2.2, queue.peek(), "Peek should show next Double value in the queue");
    }

    @Test
    void testEmptyPollAndPeekBehavior() {
        GenericArrayListQueue<Integer> queue = new GenericArrayListQueue<>();
        assertNull(queue.peek(), "Peek on an empty queue should return null");
        assertNull(queue.poll(), "Poll on an empty queue should return null");
    }
}
