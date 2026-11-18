package com.thealgorithms.sorts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * StrandSort class implementing the SortAlgorithm interface using arrays.
 */
public final class StrandSort implements SortAlgorithm {

    /**
     * Sorts the given array using the Strand Sort algorithm.
     *
     * @param <T> The type of elements to be sorted, must be Comparable.
     * @param array The array to be sorted.
     * @return The sorted array.
     */
    @Override
    public <T extends Comparable<T>> T[] sort(T[] array) {
        List<T> unsortedList = new ArrayList<>(Arrays.asList(array));
        List<T> sortedList = strandSort(unsortedList);
        return sortedList.toArray(array);
    }

    /**
     * Strand Sort algorithm that sorts a list.
     *
     * @param <T> The type of elements to be sorted, must be Comparable.
     * @param list The list to be sorted.
     * @return The sorted list.
     */
    private static <T extends Comparable<? super T>> List<T> strandSort(List<T> list) {
        if (list.size() <= 1) {
            return list;
        }

        List<T> result = new ArrayList<>();
        while (!list.isEmpty()) {
            List<T> strand = extractStrand(list);
            result = merge(result, strand);
        }
        return result;
    }

    /**
     * Extracts the next non-decreasing subsequence (strand) from the remaining unsorted list,
     * removing each selected element from {@code unsorted}.
     *
     * @param <T> The type of elements to be sorted, must be Comparable.
     * @param unsorted The remaining unsorted elements (must be non-empty).
     * @return A non-decreasing strand extracted in a single pass.
     */
    private static <T extends Comparable<? super T>> List<T> extractStrand(List<T> unsorted) {
        final List<T> strand = new ArrayList<>();
        strand.add(unsorted.removeFirst());
        for (int i = 0; i < unsorted.size();) {
            if (strand.getLast().compareTo(unsorted.get(i)) <= 0) {
                strand.add(unsorted.remove(i));
            } else {
                i++;
            }
        }
        return strand;
    }

    /**
     * Merges two sorted lists into one sorted list.
     *
     * @param <T> The type of elements to be sorted, must be Comparable.
     * @param left The first sorted list.
     * @param right The second sorted list.
     * @return The merged sorted list.
     */
    private static <T extends Comparable<? super T>> List<T> merge(List<T> left, List<T> right) {
        List<T> result = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < left.size() && j < right.size()) {
            if (left.get(i).compareTo(right.get(j)) <= 0) {
                result.add(left.get(i));
                i++;
            } else {
                result.add(right.get(j));
                j++;
            }
        }
        result.addAll(left.subList(i, left.size()));
        result.addAll(right.subList(j, right.size()));
        return result;
    }
}
