package work;

import com.thealgorithms.strings.KasaiAlgorithm;
import com.thealgorithms.strings.SuffixArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Benchmark {
    public static void main(String[] args) {
        int[] sizes = {1000, 5000, 25000};
        int repetitions = 5;
        Random rng = new Random(42L);

        // Warmup
        for (int i = 0; i < 30; i++) {
            String warmText = generateRandomString(500, rng);
            int[] warmSa = SuffixArray.buildSuffixArray(warmText);
            KasaiAlgorithm.kasai(warmText, warmSa);
        }

        System.out.println("JSON_START");
        System.out.print("[");
        boolean firstSize = true;
        for (int size : sizes) {
            if (!firstSize) {
                System.out.print(",");
            }
            firstSize = false;
            List<Long> durationsNs = new ArrayList<>();
            List<Boolean> correctness = new ArrayList<>();

            for (int r = 0; r < repetitions; r++) {
                String text = generateRandomString(size, rng);
                int[] sa = SuffixArray.buildSuffixArray(text);

                // Timed interval strictly measuring KasaiAlgorithm.kasai
                long start = System.nanoTime();
                int[] lcp = KasaiAlgorithm.kasai(text, sa);
                long elapsed = System.nanoTime() - start;

                durationsNs.add(elapsed);

                // Correctness check outside timed interval
                boolean valid = verifyLcp(text, sa, lcp);
                correctness.add(valid);
            }

            Collections.sort(durationsNs);
            long medianNs = durationsNs.get(durationsNs.size() / 2);
            long minNs = durationsNs.get(0);
            long maxNs = durationsNs.get(durationsNs.size() - 1);
            long rangeNs = maxNs - minNs;

            boolean allCorrect = correctness.stream().allMatch(b -> b);

            System.out.printf("{\"size\": %d, \"median_ns\": %d, \"min_ns\": %d, \"max_ns\": %d, \"range_ns\": %d, \"all_correct\": %s}",
                size, medianNs, minNs, maxNs, rangeNs, allCorrect);
        }
        System.out.println("]");
        System.out.println("JSON_END");
    }

    private static String generateRandomString(int n, Random rng) {
        char[] chars = new char[n];
        for (int i = 0; i < n; i++) {
            chars[i] = (char) ('a' + rng.nextInt(4));
        }
        return new String(chars);
    }

    private static boolean verifyLcp(String text, int[] sa, int[] lcp) {
        int n = text.length();
        if (lcp.length != n || lcp[n - 1] != 0) {
            return false;
        }
        int step = Math.max(1, (n - 1) / 50);
        for (int r = 0; r < n - 1; r += step) {
            int i = sa[r];
            int j = sa[r + 1];
            int match = 0;
            while (i + match < n && j + match < n && text.charAt(i + match) == text.charAt(j + match)) {
                match++;
            }
            if (match != lcp[r]) {
                return false;
            }
        }
        return true;
    }
}
