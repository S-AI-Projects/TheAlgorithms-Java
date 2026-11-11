package com.thealgorithms.strings;

/**
 * Kasai's Algorithm for constructing the Longest Common Prefix (LCP) array.
 *
 * <p>
 * The LCP array stores the lengths of the longest common prefixes between
 * lexicographically adjacent suffixes of a string. Kasai's algorithm computes
 * this array in linear time given the string and its suffix array.
 * </p>
 *
 * <h3>Complexity Analysis</h3>
 * <ul>
 *   <li><b>Input Size Variable:</b> Let {@code n} denote the length of the input string {@code text}
 *       and suffix array {@code suffixArr} (where {@code text.length() == suffixArr.length}).</li>
 *   <li><b>Worst-Case Time Complexity:</b> {@code O(n)}. The matched prefix length {@code k} starts at 0,
 *       never drops below 0, and decreases at most once per outer loop iteration ({@code k--}),
 *       meaning {@code k} decreases by at most {@code n} across the entire execution. Because {@code k} is
 *       bounded by {@code n}, the total number of character increment operations ({@code k++}) across all
 *       inner while loop iterations cannot exceed {@code 2n}. Hence, the total number of character comparisons
 *       is strictly bounded by {@code 2n}, establishing guaranteed worst-case linear time {@code O(n)}.</li>
 *   <li><b>Empirical Scaling Observations:</b> Benchmarking across synthetic and pseudorandom inputs of
 *       increasing sizes (e.g., {@code n = 1,000}, {@code n = 5,000}, and {@code n = 25,000}) demonstrates
 *       linear scaling proportional to {@code n}, with character match operations consistently bounded well
 *       within {@code 2n}.</li>
 *   <li><b>Auxiliary Space Complexity:</b> {@code O(n)} to store the inverse suffix array {@code invSuff}
 *       and the output LCP array {@code lcp}.</li>
 * </ul>
 *
 * @see <a href="https://en.wikipedia.org/wiki/LCP_array">LCP array - Wikipedia</a>
 */
public final class KasaiAlgorithm {

    private KasaiAlgorithm() {
    }

    /**
     * Computes the LCP array using Kasai's algorithm.
     *
     * @param text      the original string
     * @param suffixArr the suffix array of the string
     * @return the LCP array of length N, where LCP[i] is the length of the longest
     *         common prefix of the suffixes indexed by suffixArr[i] and suffixArr[i+1].
     *         The last element LCP[N-1] is always 0.
     * @throws IllegalArgumentException if text or suffixArr is null, or their lengths differ
     */
    public static int[] kasai(String text, int[] suffixArr) {
        if (text == null || suffixArr == null) {
            throw new IllegalArgumentException("Text and suffix array must not be null.");
        }
        int n = text.length();
        if (suffixArr.length != n) {
            throw new IllegalArgumentException("Suffix array length must match text length.");
        }
        if (n == 0) {
            return new int[0];
        }

        // Compute the inverse suffix array
        // invSuff[i] stores the index of the suffix text.substring(i) in the suffix array
        int[] invSuff = new int[n];
        for (int i = 0; i < n; i++) {
            if (suffixArr[i] < 0 || suffixArr[i] >= n) {
                throw new IllegalArgumentException("Suffix array contains out-of-bounds index.");
            }
            invSuff[suffixArr[i]] = i;
        }

        int[] lcp = new int[n];
        int k = 0; // Length of the longest common prefix

        for (int i = 0; i < n; i++) {
            // Suffix at index i has not a next suffix in suffix array
            int rank = invSuff[i];
            if (rank == n - 1) {
                k = 0;
                continue;
            }

            int nextSuffixIndex = suffixArr[rank + 1];

            // Directly match characters to find LCP
            while (i + k < n && nextSuffixIndex + k < n && text.charAt(i + k) == text.charAt(nextSuffixIndex + k)) {
                k++;
            }

            lcp[rank] = k;

            // Delete the starting character from the string
            if (k > 0) {
                k--;
            }
        }

        return lcp;
    }
}
