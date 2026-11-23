package com.thealgorithms.datastructures.trees;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests covering failure behavior and edge cases of {@link RedBlackBST#printTree}.
 *
 * <p>Specifically validates that printTree gracefully handles:</p>
 * <ul>
 *   <li>Null node reference (preventing NullPointerException)</li>
 *   <li>Nil sentinel node reference (standard base case termination)</li>
 *   <li>Empty tree root (nil root)</li>
 *   <li>Corrupted/detached nodes with null parent pointer</li>
 *   <li>Corrupted child references (null left or right child)</li>
 *   <li>Correct output format when tree is populated</li>
 * </ul>
 *
 * Fixes: #28
 */
public class RedBlackBSTPrintTreeFailureTest {

    private RedBlackBST tree;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        tree = new RedBlackBST();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ── Failure Case 1: Null node input does not throw NullPointerException ──

    @Test
    void testPrintTreeNullNodeHandledGracefully() {
        assertDoesNotThrow(() -> tree.printTree(null),
                "printTree(null) must not throw NullPointerException");
        assertEquals("", outContent.toString(),
                "printTree(null) should produce no output");
    }

    @Test
    void testPrintTreepreNullNodeHandledGracefully() {
        assertDoesNotThrow(() -> tree.printTreepre(null),
                "printTreepre(null) must not throw NullPointerException");
        assertEquals("", outContent.toString(),
                "printTreepre(null) should produce no output");
    }

    // ── Failure Case 2: Nil sentinel node produces no output ─────────────────

    @Test
    void testPrintTreeNilSentinelProducesNoOutput() {
        assertDoesNotThrow(() -> tree.printTree(tree.nil));
        assertEquals("", outContent.toString(),
                "Printing nil sentinel must produce no output");
    }

    @Test
    void testPrintTreepreNilSentinelProducesNoOutput() {
        assertDoesNotThrow(() -> tree.printTreepre(tree.nil));
        assertEquals("", outContent.toString(),
                "Printing nil sentinel in pre-order must produce no output");
    }

    // ── Failure Case 3: Empty tree printing ──────────────────────────────────

    @Test
    void testPrintTreeEmptyTreeProducesNoOutput() {
        tree.printTree();
        assertEquals("", outContent.toString(),
                "Printing an empty tree must produce no output");

        tree.printTreepre();
        assertEquals("", outContent.toString(),
                "Pre-order printing of an empty tree must produce no output");
    }

    // ── Failure Case 4: Node with null parent pointer (detached/corrupted) ───

    @Test
    void testPrintTreeNodeWithNullParentDoesNotThrow() {
        RedBlackBST.Node isolatedNode = tree.new Node(42);
        isolatedNode.p = null; // simulate corrupted or uninitialized parent

        assertDoesNotThrow(() -> tree.printTree(isolatedNode),
                "printTree on a node with null parent must not throw NullPointerException");
        assertTrue(outContent.toString().contains("Key: 42"),
                "Output should contain the node's key");
    }

    // ── Failure Case 5: Node with null child pointer (unconnected) ───────────

    @Test
    void testPrintTreeNodeWithNullChildrenDoesNotThrow() {
        RedBlackBST.Node corruptedNode = tree.new Node(99);
        corruptedNode.left = null; // null instead of nil
        corruptedNode.right = null; // null instead of nil

        assertDoesNotThrow(() -> tree.printTree(corruptedNode),
                "printTree on a node with null children must handle nulls safely");
        assertTrue(outContent.toString().contains("Key: 99"));
    }

    // ── Normal Behavior Verification: Populated Tree ─────────────────────────

    @Test
    void testPrintTreePopulatedOutputsKeysInOrder() {
        tree.insert(20);
        tree.insert(10);
        tree.insert(30);

        outContent.reset();
        tree.printTree();

        String output = outContent.toString();
        assertTrue(output.contains("Key: 10"));
        assertTrue(output.contains("Key: 20"));
        assertTrue(output.contains("Key: 30"));

        // In-order traversal must print 10 before 20, and 20 before 30
        int idx10 = output.indexOf("Key: 10");
        int idx20 = output.indexOf("Key: 20");
        int idx30 = output.indexOf("Key: 30");

        assertTrue(idx10 < idx20, "10 must appear before 20 in in-order print");
        assertTrue(idx20 < idx30, "20 must appear before 30 in in-order print");
    }

    @Test
    void testPrintTreeprePopulatedOutputsRootFirst() {
        tree.insert(20);
        tree.insert(10);
        tree.insert(30);

        outContent.reset();
        tree.printTreepre();

        String output = outContent.toString();
        // In pre-order, the root (20) is visited first
        int idx20 = output.indexOf("Key: 20");
        int idx10 = output.indexOf("Key: 10");
        int idx30 = output.indexOf("Key: 30");

        assertTrue(idx20 < idx10, "Root 20 must appear before 10 in pre-order print");
        assertTrue(idx20 < idx30, "Root 20 must appear before 30 in pre-order print");
    }
}
