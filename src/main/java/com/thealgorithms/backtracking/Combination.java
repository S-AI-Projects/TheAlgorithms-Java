package com.thealgorithms.backtracking;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * Finds all combinations of a given array using backtracking algorithm * @author Alan Piao (<a href="https://github.com/cpiao3">git-Alan Piao</a>)
 */
public final class Combination {
    private Combination() {
    }

    /**
     * Finds all combinations of length {@code n} from the given array using backtracking.
     *
     * <p>Contract and invariants:
     * <ul>
     *   <li>Preconditions: {@code n >= 0} (throws {@link IllegalArgumentException} if {@code n < 0});
     *       for {@code n > 0}, {@code arr} must be non-null with mutually {@link Comparable} non-null elements.</li>
     *   <li>When {@code n == 0} or {@code n > arr.length}, returns an empty list.</li>
     *   <li>For {@code arr} with {@code m} distinct elements and {@code 1 <= n <= m}, returns exactly
     *       {@code C(m, n)} unique {@link TreeSet} combinations of size {@code n} in lexicographical order,
     *       each containing elements strictly from {@code arr}.</li>
     *   <li>Input immutability: {@code arr} is cloned before sorting and is never mutated; fresh equivalent
     *       (even reordered) arrays of the same distinct elements yield identical results.</li>
     * </ul>
     *
     * @param arr the input array
     * @param n length of each combination ({@code n >= 0})
     * @param <T> the element type
     * @return a list of all combinations of length {@code n}, or an empty list if {@code n == 0} or {@code n > arr.length}
     */
    public static <T> List<TreeSet<T>> combination(T[] arr, int n) {
        if (n < 0) {
            throw new IllegalArgumentException("The combination length cannot be negative.");
        }

        if (n == 0) {
            return Collections.emptyList();
        }
        T[] array = arr.clone();
        Arrays.sort(array);

        List<TreeSet<T>> result = new LinkedList<>();
        backtracking(array, n, 0, new TreeSet<T>(), result);
        return result;
    }

    /**
     * Backtrack all possible combinations of a given array
     * @param arr the array.
     * @param n length of the combination
     * @param index the starting index.
     * @param currSet set that tracks current combination
     * @param result the list contains all combination.
     * @param <T> the type of elements in the array.
     */
    private static <T> void backtracking(T[] arr, int n, int index, TreeSet<T> currSet, List<TreeSet<T>> result) {
        if (index + n - currSet.size() > arr.length) {
            return;
        }
        if (currSet.size() == n - 1) {
            for (int i = index; i < arr.length; i++) {
                currSet.add(arr[i]);
                result.add(new TreeSet<>(currSet));
                currSet.remove(arr[i]);
            }
            return;
        }
        for (int i = index; i < arr.length; i++) {
            currSet.add(arr[i]);
            backtracking(arr, n, i + 1, currSet, result);
            currSet.remove(arr[i]);
        }
    }
}
