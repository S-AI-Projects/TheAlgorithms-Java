package com.thealgorithms.datastructures.lists;

/**
 * Singly-linked list supporting middle-element lookup via Floyd's tortoise-and-hare pointers.
 *
 * <p>Supported boundaries:
 * <ul>
 * <li>Empty list (0 nodes, {@code head == null}): {@link #getMiddle()} returns {@code null} and
 * {@link #toString()} returns {@code "[]"}.</li>
 * <li>Smallest non-empty list (1 {@link Node}): {@link #getMiddle()} skips the fast/slow loop
 * and returns the single node's value.</li>
 * <li>Even-length lists ({@code 2k} nodes, {@code k >= 1}): {@link #getMiddle()} returns the
 * second middle element (0-based index {@code k}).</li>
 * <li>Element values may be {@code null}; {@link #append(Object)} always terminates new nodes
 * with {@code next = null} and {@link Node}'s constructor is {@code private}, so lists built
 * through the public API remain acyclic.</li>
 * </ul>
 *
 * @param <E> element type stored in list nodes
 */
public class TortoiseHareAlgo<E> {
    /**
     * Singly-linked node storing an element {@code value} (nullable) and {@code next} link.
     * The constructor is {@code private} so callers append values only through
     * {@link TortoiseHareAlgo#append(Object)}, keeping {@code next} acyclic.
     */
    static final class Node<E> {
        Node<E> next;
        E value;

        private Node(E value, Node<E> next) {
            this.value = value;
            this.next = next;
        }
    }

    private Node<E> head = null;

    public TortoiseHareAlgo() {
        head = null;
    }

    public void append(E value) {
        Node<E> newNode = new Node<>(value, null);
        if (head == null) {
            head = newNode;
            return;
        }
        Node<E> current = head;
        while (current.next != null) {
            current = current.next;
        }
        current.next = newNode;
    }

    public E getMiddle() {
        if (head == null) {
            return null;
        }

        Node<E> slow = head;
        Node<E> fast = head;

        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        return slow.value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<E> current = head;
        while (current != null) {
            sb.append(current.value);
            if (current.next != null) {
                sb.append(", ");
            }
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }
}
