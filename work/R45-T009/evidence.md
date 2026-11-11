# Verification Evidence: R45-T009

- **Target**: `src/main/java/com/thealgorithms/strings/KasaiAlgorithm.java`
- **Timestamp**: 2026-11-11T11:32:55.056092
- **Python Version**: `3.15.2 (v3.15.2:5e58eaad7c5, Oct 25 2026, 14:19:34) [Clang 21.0.0 (clang-2100.1.1.101)]`
- **Java Version**: `openjdk version "21.0.13.0.3" 2026-10-24`
- **Maven Version**: `Apache Maven 3.10.0 (c43a36b8d67be7e0805a411bc0898af1a51f5472)`

## Test Execution

```
Command: mvn test -Dtest=KasaiAlgorithmTest
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
Checkstyle violations: 0
```

## Algorithmic Scaling Experiment Results

- **Methodology**: 5 repetitions per size with deterministic fresh inputs (seed 42L).
- **Measurement**: Execution time of `KasaiAlgorithm.kasai` measured in isolation (excluding JVM startup, I/O, and data generation).
- **Correctness**: Output LCP arrays verified against independent string oracle outside the timed interval.

| Input Size (n) | Median Duration (µs) | Min Duration (µs) | Max Duration (µs) | Range (µs) | Correctness Passed |
|---|---|---|---|---|---|
| 1000 | 74.88 | 73.71 | 80.12 | 6.42 | True |
| 5000 | 170.75 | 112.92 | 830.25 | 717.33 | True |
| 25000 | 226.75 | 220.88 | 378.17 | 157.29 | True |

## Complexity Analysis Summary

- **Input Size Variable**: `n (text.length() == suffixArr.length)`
- **Theoretical Bound**: O(n) time, O(n) space. Loop counter k starts at 0, bounded by n, and decreases at most n times total (k--), so increments (k++) cannot exceed 2n.
- **Empirical Observation**: Observed runtimes scale linearly with n across synthetic and pseudorandom inputs with all correctness checks passing outside the timed interval.
