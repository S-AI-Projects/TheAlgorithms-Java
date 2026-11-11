package com.thealgorithms.bitmanipulation;

/**
 * A utility class to find the non-repeating number in an array where every other number repeats.
 * This class contains a method to identify the single unique number using bit manipulation.
 *
 * <p>The solution leverages the properties of the XOR operation:
 * <ul>
 *   <li>{@code x ^ x = 0} for any integer x (self-inverse property)</li>
 *   <li>{@code x ^ 0 = x} for any integer x (identity property)</li>
 *   <li>Associative and commutative: ordering of inputs does not affect the final result</li>
 * </ul>
 *
 * <p>Example:
 * Given the input array {@code [2, 3, 5, 2, 3]}, the output will be {@code 5} since it does not repeat.
 *
 * @author Bama Charan Chhandogi (https://github.com/BamaCharanChhandogi)
 */
public final class NonRepeatingNumberFinder {
    private NonRepeatingNumberFinder() {
    }

    /**
     * Finds the non-repeating number in the given array.
     *
     * @param arr an array of integers where every number except one appears twice
     * @return the integer that appears only once in the array or 0 if the array is empty
     */
    public static int findNonRepeatingNumber(int[] arr) {
        return reduceXor(arr);
    }

    /**
     * Reduces the array by sequentially accumulating bitwise XOR on each element.
     * Paired duplicate values cancel each other out to zero, leaving the lone non-repeating value
     * or 0 if the array is empty.
     *
     * @param arr the input array of integers
     * @return the XOR-accumulated sum of all elements
     */
    private static int reduceXor(int[] arr) {
        int xorSum = 0;
        for (int num : arr) {
            xorSum ^= num;
        }
        return xorSum;
    }
}
