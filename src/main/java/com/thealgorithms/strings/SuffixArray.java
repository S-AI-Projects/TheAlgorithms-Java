package com.thealgorithms.strings;

import java.util.Arrays;

/**
 * Suffix Array implementation in Java using the Manber-Myers prefix doubling algorithm (O(N log^2 N)).
 * Builds an array of indices that represent all suffixes of a string in lexicographical sorted order.
 *
 * <p><b>Worked Example:</b>
 * <pre>{@code
 * String text = "banana";
 * int[] suffixArray = SuffixArray.buildSuffixArray(text);
 * // suffixArray is [5, 3, 1, 0, 4, 2]
 * }</pre>
 *
 * <p><b>Execution Trace &amp; Decisive State Transitions:</b>
 * <ol>
 *   <li><b>Initial State (Single Character Ranks, k = 0):</b>
 *     Indices: {@code [0, 1, 2, 3, 4, 5]}, Characters: {@code ['b', 'a', 'n', 'a', 'n', 'a']}
 *     Initial ranks by ASCII value: {@code [98, 97, 110, 97, 110, 97]}.
 *   </li>
 *   <li><b>Step k = 1 (2-gram Sorting):</b>
 *     Suffixes sorted by composite key {@code (rank[i], rank[i+1])}:
 *     Index 5 ("a$") -&gt; (97, -1), Index 1 ("an") -&gt; (97, 110), Index 3 ("an") -&gt; (97, 110),
 *     Index 0 ("ba") -&gt; (98, 97), Index 2 ("na") -&gt; (110, 97), Index 4 ("na") -&gt; (110, 97).
 *     Permutation after k=1: {@code [5, 1, 3, 0, 2, 4]}.
 *     Ties remain between suffixes 1 &amp; 3 ("an") and 2 &amp; 4 ("na").
 *   </li>
 *   <li><b>Step k = 2 (4-gram Sorting - Decisive State Transition):</b>
 *     Suffixes sorted by composite key {@code (rank[i], rank[i+2])}:
 *     Index 5: (0, -1) -&gt; rank 0 ("a")
 *     Index 3: (1, 0)  -&gt; rank 1 ("ana")
 *     Index 1: (1, 3)  -&gt; rank 2 ("anana")
 *     Index 0: (2, 3)  -&gt; rank 3 ("banana")
 *     Index 4: (3, -1) -&gt; rank 4 ("na")
 *     Index 2: (3, 1)  -&gt; rank 5 ("nana")
 *     <b>Decisive transition:</b> All suffixes now have strictly unique ranks (0 through 5).
 *     Condition {@code rank[suffixArray[n - 1]] == n - 1} triggers early termination (line 54).
 *   </li>
 *   <li><b>Final Result:</b> {@code [5, 3, 1, 0, 4, 2]} corresponding to sorted suffixes:
 *     {@code "a", "ana", "anana", "banana", "na", "nana"}.
 *   </li>
 * </ol>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Suffix_array">Suffix Array (Wikipedia)</a>
 * @author Nithin U. (https://github.com/NithinU2802)
 */
public final class SuffixArray {

    private SuffixArray() {
    }

    /**
     * Constructs the suffix array for the given input text.
     *
     * @param text the string whose suffix array is to be computed
     * @return an array of integer indices representing the starting positions of sorted suffixes
     */
    public static int[] buildSuffixArray(String text) {
        int n = text.length();
        Integer[] suffixArray = new Integer[n];
        int[] rank = new int[n];
        int[] tempRank = new int[n];

        // Initial ranking based on characters
        for (int i = 0; i < n; i++) {
            suffixArray[i] = i;
            rank[i] = text.charAt(i);
        }

        for (int k = 1; k < n; k *= 2) {
            final int step = k;

            // Comparator: first by rank, then by rank + step
            Arrays.sort(suffixArray, (a, b) -> {
                if (rank[a] != rank[b]) {
                    return Integer.compare(rank[a], rank[b]);
                }
                int ra = (a + step < n) ? rank[a + step] : -1;
                int rb = (b + step < n) ? rank[b + step] : -1;
                return Integer.compare(ra, rb);
            });

            // Re-rank
            tempRank[suffixArray[0]] = 0;
            for (int i = 1; i < n; i++) {
                int prev = suffixArray[i - 1];
                int curr = suffixArray[i];
                boolean sameRank = rank[prev] == rank[curr] && ((prev + step < n ? rank[prev + step] : -1) == (curr + step < n ? rank[curr + step] : -1));
                tempRank[curr] = sameRank ? tempRank[prev] : tempRank[prev] + 1;
            }

            System.arraycopy(tempRank, 0, rank, 0, n);

            if (rank[suffixArray[n - 1]] == n - 1) {
                break;
            }
        }
        return Arrays.stream(suffixArray).mapToInt(Integer::intValue).toArray();
    }
}
