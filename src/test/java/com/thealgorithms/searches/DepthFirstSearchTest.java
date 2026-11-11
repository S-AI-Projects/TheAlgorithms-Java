package com.thealgorithms.searches;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thealgorithms.datastructures.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DepthFirstSearchTest {

    private Node<Integer> root;
    private DepthFirstSearch<Integer> dfs;

    //
    // Tree structure:
    //          1
    //        / | \
    //       2  3  4
    //      / \
    //     5   6

    @BeforeEach
    public void setUp() {
        // nodes declaration
        root = new Node<>(1);

        var nodeB = new Node<>(2);
        var nodeC = new Node<>(3);
        var nodeD = new Node<>(4);

        var nodeE = new Node<>(5);
        var nodeF = new Node<>(6);

        // tree initialization
        root.addChild(nodeB);
        root.addChild(nodeC);
        root.addChild(nodeD);

        nodeB.addChild(nodeE);
        nodeB.addChild(nodeF);

        // create an instance to monitor the visited nodes
        dfs = new DepthFirstSearch<>();
    }

    @Test
    public void testSearchRoot() {
        Integer expectedValue = 1;
        List<Integer> expectedPath = List.of(1);

        // check value
        Optional<Node<Integer>> value = dfs.recursiveSearch(root, expectedValue);
        assertEquals(expectedValue, value.orElseGet(() -> new Node<>(null)).getValue());

        // check path
        assertArrayEquals(expectedPath.toArray(), dfs.getVisited().toArray());
    }

    @Test
    public void testSearch4() {
        Integer expectedValue = 4;
        List<Integer> expectedPath = List.of(1, 2, 5, 6, 3, 4);

        // check value
        Optional<Node<Integer>> value = dfs.recursiveSearch(root, expectedValue);
        assertEquals(expectedValue, value.orElseGet(() -> new Node<>(null)).getValue());

        // check path
        assertArrayEquals(expectedPath.toArray(), dfs.getVisited().toArray());
    }

    @Test
    void testNullRoot() {
        var value = dfs.recursiveSearch(null, 4);
        assertTrue(value.isEmpty());
    }

    @Test
    void testSearchValueThatNotExists() {
        List<Integer> expectedPath = List.of(1, 2, 5, 6, 3, 4);
        var value = dfs.recursiveSearch(root, 10);

        // check that the value is empty because it's not exists in the tree
        assertTrue(value.isEmpty());

        // check path is the whole list
        assertArrayEquals(expectedPath.toArray(), dfs.getVisited().toArray());
    }

    /**
     * Smallest valid input fixture: null node.
     * Preconditions established at:
     * - DepthFirstSearch.java lines 41-43 (if node == null return Optional.empty())
     */
    @Test
    void testSmallestValidInputNullNodeFixture() {
        Node<Integer> node = null;
        Integer target = 42;

        Optional<Node<Integer>> result = dfs.recursiveSearch(node, target);
        assertTrue(result.isEmpty(), "Searching null node must return Optional.empty().");
        assertTrue(dfs.getVisited().isEmpty(), "Visiting null node must not add elements to visited.");
    }

    /**
     * Ordinary input fixture: tree root with target 4.
     * Preconditions established at:
     * - DepthFirstSearch.java lines 41-43 (non-null node check)
     * - DepthFirstSearch.java line 44 (visited.add)
     * - DepthFirstSearch.java line 45 (matching value check)
     * - DepthFirstSearch.java line 49 (children recursive stream search)
     */
    @Test
    void testOrdinaryInputWorkflowFixture() {
        Integer target = 4;
        Optional<Node<Integer>> result = dfs.recursiveSearch(root, target);

        assertTrue(result.isPresent(), "Target node 4 must be found in the tree.");
        assertEquals(target, result.get().getValue(), "Found node value must equal 4.");
        List<Integer> expectedVisited = List.of(1, 2, 5, 6, 3, 4);
        assertEquals(expectedVisited, dfs.getVisited(), "Visited path must follow preorder DFS traversal.");
    }

    /**
     * Repeatability test: run the same ordinary fixture twice in fresh isolated state
     * and once after another valid search.
     * Asserts that earlier runs cannot alter later search outcomes.
     */
    @Test
    void testRepeatabilityAndStateIsolation() {
        Integer target = 4;
        List<Integer> expectedVisited = List.of(1, 2, 5, 6, 3, 4);

        // Run 1 in fresh isolated instance
        DepthFirstSearch<Integer> freshInstance1 = new DepthFirstSearch<>();
        Optional<Node<Integer>> result1 = freshInstance1.recursiveSearch(root, target);
        assertTrue(result1.isPresent());
        assertEquals(target, result1.get().getValue());
        assertEquals(expectedVisited, freshInstance1.getVisited());

        // Run 2 in fresh isolated instance
        DepthFirstSearch<Integer> freshInstance2 = new DepthFirstSearch<>();
        Optional<Node<Integer>> result2 = freshInstance2.recursiveSearch(root, target);
        assertTrue(result2.isPresent());
        assertEquals(target, result2.get().getValue());
        assertEquals(expectedVisited, freshInstance2.getVisited());

        // Asserts identical outcomes between fresh instances
        assertEquals(result1.get().getValue(), result2.get().getValue(), "Search results across isolated instances must be identical.");
        assertEquals(freshInstance1.getVisited(), freshInstance2.getVisited(), "Visited history across isolated instances must be identical.");

        // Run 3: execute another valid search (target = 1) on an existing instance, then run the fixture
        DepthFirstSearch<Integer> reusedInstance = new DepthFirstSearch<>();
        Optional<Node<Integer>> priorSearch = reusedInstance.recursiveSearch(root, 1);
        assertTrue(priorSearch.isPresent());
        assertEquals(List.of(1), reusedInstance.getVisited());

        // Clean accumulator seam
        reusedInstance.clearVisited();

        Optional<Node<Integer>> result3 = reusedInstance.recursiveSearch(root, target);
        assertTrue(result3.isPresent());
        assertEquals(target, result3.get().getValue(), "Prior run must not alter subsequent search result.");
        assertEquals(expectedVisited, reusedInstance.getVisited(), "Cleaned instance must match isolated visited path.");
    }

    /**
     * Dependency injection seam test: custom visited sink list injection.
     * Preconditions established at:
     * - DepthFirstSearch.java lines 37-39 (custom constructor dependency seam)
     */
    @Test
    void testDependencyInjectionSeam() {
        List<Integer> customSink = new ArrayList<>();
        DepthFirstSearch<Integer> injectedDfs = new DepthFirstSearch<>(customSink);

        Optional<Node<Integer>> found = injectedDfs.recursiveSearch(root, 2);
        assertTrue(found.isPresent());
        assertEquals(2, found.get().getValue());

        // Visited values are directly recorded in the injected sink
        assertNotNull(customSink);
        assertEquals(List.of(1, 2), customSink, "Injected sink must record visited nodes directly.");
    }
}
