package com.thealgorithms.searches;

import com.thealgorithms.datastructures.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Depth-First Search (DFS) on generic tree structures.
 *
 * <p>State & Dependency Contract:
 * <ul>
 *   <li><b>State / Mutation:</b> Maintains an internal list of visited node values traversed during recursive search calls.
 *       Tree nodes themselves are never mutated.</li>
 *   <li><b>External Dependencies:</b> None (no live network, time, filesystem, randomness, or GUI dependencies).
 *       Operates as a deterministic in-memory traversal algorithm.</li>
 *   <li><b>Dependency Injection:</b> Accepts an optional custom {@link List} sink for tracking visited nodes,
 *       defaulting to a new {@link ArrayList} to preserve all existing callers.</li>
 *   <li><b>Repeatability:</b> Given the same tree and target value, {@link #recursiveSearch(Node, Integer)} produces
 *       identical search outcomes regardless of prior searches.</li>
 * </ul>
 *
 * @author: caos321
 * @date: 31 October 2021 (Sunday)
 * @wiki: https://en.wikipedia.org/wiki/Depth-first_search
 */
public class DepthFirstSearch<T> {

    private final List<T> visited;

    public DepthFirstSearch() {
        this(new ArrayList<>());
    }

    public DepthFirstSearch(final List<T> visited) {
        this.visited = visited != null ? visited : new ArrayList<>();
    }

    public Optional<Node<T>> recursiveSearch(final Node<T> node, final Integer value) {
        if (node == null) {
            return Optional.empty();
        }
        visited.add(node.getValue());
        if (node.getValue().equals(value)) {
            return Optional.of(node);
        }

        return node.getChildren().stream().map(v -> recursiveSearch(v, value)).flatMap(Optional::stream).findAny();
    }

    public List<T> getVisited() {
        return visited;
    }

    public void clearVisited() {
        visited.clear();
    }
}
