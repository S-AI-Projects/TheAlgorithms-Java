package com.thealgorithms.sorts;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StrandSortTest extends SortingAlgorithmTest {
    @Override
    SortAlgorithm getSortAlgorithm() {
        return new StrandSort();
    }

    @Test
    @DisplayName("Refactor fixture: locks down ascending ordering, duplicates, smallest collections, and in-place array return")
    void testFixtureBehaviorPreserved() {
        StrandSort sorter = new StrandSort();

        // Smallest supported collections: empty and single-element arrays
        Integer[] empty = new Integer[] {};
        Integer[] emptyResult = sorter.sort(empty);
        assertSame(empty, emptyResult, "toArray(array) preserves the array instance");
        assertArrayEquals(new Integer[] {}, emptyResult);

        Integer[] single = new Integer[] {1};
        Integer[] singleResult = sorter.sort(single);
        assertSame(single, singleResult);
        assertArrayEquals(new Integer[] {1}, singleResult);

        // Fixture: [3, 1, 2] -> [1, 2, 3]
        Integer[] mixed = new Integer[] {3, 1, 2};
        Integer[] mixedResult = sorter.sort(mixed);
        assertSame(mixed, mixedResult);
        assertArrayEquals(new Integer[] {1, 2, 3}, mixedResult);

        // Fixture: [2, 2, 1] -> [1, 2, 2]
        Integer[] duplicates = new Integer[] {2, 2, 1};
        Integer[] duplicatesResult = sorter.sort(duplicates);
        assertSame(duplicates, duplicatesResult);
        assertArrayEquals(new Integer[] {1, 2, 2}, duplicatesResult);

        // Fixture: already ordered [1, 2, 3] -> [1, 2, 3]
        Integer[] ordered = new Integer[] {1, 2, 3};
        Integer[] orderedResult = sorter.sort(ordered);
        assertSame(ordered, orderedResult);
        assertArrayEquals(new Integer[] {1, 2, 3}, orderedResult);

        // Fixture: reversed [3, 2, 1] -> [1, 2, 3]
        Integer[] reversed = new Integer[] {3, 2, 1};
        Integer[] reversedResult = sorter.sort(reversed);
        assertSame(reversed, reversedResult);
        assertArrayEquals(new Integer[] {1, 2, 3}, reversedResult);

        // List overload from SortAlgorithm default interface
        List<Integer> listResult = sorter.sort(Arrays.asList(3, 1, 2));
        assertEquals(List.of(1, 2, 3), listResult);

        System.out.println("CASE|smallest_empty|input=[]|expected=[]|actual=" + Arrays.toString(emptyResult));
        System.out.println("CASE|smallest_single|input=[1]|expected=[1]|actual=" + Arrays.toString(singleResult));
        System.out.println("CASE|mixed|input=[3, 1, 2]|expected=[1, 2, 3]|actual=" + Arrays.toString(mixedResult));
        System.out.println("CASE|duplicates|input=[2, 2, 1]|expected=[1, 2, 2]|actual=" + Arrays.toString(duplicatesResult));
        System.out.println("CASE|already_ordered|input=[1, 2, 3]|expected=[1, 2, 3]|actual=" + Arrays.toString(orderedResult));
        System.out.println("CASE|reversed|input=[3, 2, 1]|expected=[1, 2, 3]|actual=" + Arrays.toString(reversedResult));
        System.out.println("CASE|list_overload|input=[3, 1, 2]|expected=[1, 2, 3]|actual=" + listResult);
    }
}
