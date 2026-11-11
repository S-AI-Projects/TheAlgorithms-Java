# Verification Evidence: R45-T008

- **Target**: `src/main/java/com/thealgorithms/graph/GomoryHuTree.java`
- **Timestamp**: 2026-11-11T11:19:58.079591
- **Python Version**: `3.15.2 (v3.15.2:5e58eaad7c5, Oct 25 2026, 14:19:34) [Clang 21.0.0 (clang-2100.1.1.101)]`
- **Java Version**: `openjdk version "21.0.13.0.3" 2026-10-24`
- **Maven Version**: `Apache Maven 3.10.0 (c43a36b8d67be7e0805a411bc0898af1a51f5472)`

## Test Execution

```
Command: mvn test -Dtest=GomoryHuTreeTest
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
Checkstyle violations: 0
```

## Precondition & Failure Path Verification Matrix

| Precondition | Source Check | Source Throw / Return | Expected Result |
|---|---|---|---|
| `cap == null` | `GomoryHuTree.java:73` | `GomoryHuTree.java:74` | `Capacity matrix must not be null or empty` |
| `cap.length == 0` | `GomoryHuTree.java:73` | `GomoryHuTree.java:74` | `Capacity matrix must not be null or empty` |
| `cap[i] == null` | `GomoryHuTree.java:78` | `GomoryHuTree.java:79` | `Capacity matrix must be square` |
| `cap[i].length != n` | `GomoryHuTree.java:78` | `GomoryHuTree.java:79` | `Capacity matrix must be square` |
| `cap[i][j] < 0` | `GomoryHuTree.java:82` | `GomoryHuTree.java:83` | `Capacities must be non-negative` |
| `n == 1` | `GomoryHuTree.java:40` | `GomoryHuTree.java:41` | `{'parent': [-1], 'weight': [0]}` |
| `Ordinary 2x2 symmetric matrix` | `GomoryHuTree.java:38-51` | `None` | `{'parent': [-1, 0], 'weight': [0, 7]}` |
| `Ordinary 3x3 triangle matrix` | `GomoryHuTree.java:38-51` | `None` | `{'parent': [-1, 0, 0], 'weight': [0, 5, 6]}` |

## State Invariant Checks

- **Caller State Preserved**: Caller-owned matrix references and array contents are never mutated during failure or success.
- **Post-Failure Isolation**: Successful calls execute normally and produce accurate cut trees immediately following handled exceptions.
