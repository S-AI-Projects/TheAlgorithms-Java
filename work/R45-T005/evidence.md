# Verification Evidence for R45-T005

- **Target**: `src/main/java/com/thealgorithms/searches/DepthFirstSearch.java`
- **Test Suite**: `src/test/java/com/thealgorithms/searches/DepthFirstSearchTest.java`
- **Command**: `mvn test -Dtest=DepthFirstSearchTest`
- **Tests Run**: 8 (Failures: 0, Errors: 0, Skipped: 0)
- **Status**: SUCCESS
- **External Dependencies**: None (pure deterministic tree search)

## Tool Versions

- **Python**: `3.15.2 (v3.15.2:5e58eaad7c5, Oct 25 2026, 14:19:34) [Clang 21.0.0 (clang-2100.1.1.101)]`
- **Java**: `openjdk version "21.0.13.0.3" 2026-10-24`
- **Maven**: `Apache Maven 3.10.0 (c43a36b8d67be7e0805a411bc0898af1a51f5472)`

## Fixtures and Repeatability Contracts

### Smallest Valid Input (Null Node)
- **Precondition Lines**: DepthFirstSearch.java:41-43 (if node == null return Optional.empty())
- **Literal Inputs**: `node = null, value = 42`
- **Expected Results**: Optional.empty(), visited list remains empty []
- **Actual Results**: Optional.empty() returned, visited is empty; passed in testSmallestValidInputNullNodeFixture

### Ordinary Input (Preorder Tree Search)
- **Precondition Lines**: DepthFirstSearch.java:41-43, 44, 45, 49
- **Literal Inputs**: `Root 1 (children 2, 3, 4; 2 has 5, 6), target = 4`
- **Expected Results**: Optional.of(node4), visited = [1, 2, 5, 6, 3, 4]
- **Actual Results**: Node 4 found, visited matched [1, 2, 5, 6, 3, 4]; passed in testOrdinaryInputWorkflowFixture

### Repeatability in Fresh Isolated States
- **Precondition Lines**: DepthFirstSearch.java:33-35 (fresh instance constructor), 41-49
- **Literal Inputs**: `Two distinct fresh instances executing fixture (root, 4)`
- **Expected Results**: Both instances produce strictly identical search results and identical visited sequences
- **Actual Results**: Outcome 1 == Outcome 2, Visited 1 == Visited 2; passed in testRepeatabilityAndStateIsolation

### Sequential Run Input Isolation
- **Precondition Lines**: DepthFirstSearch.java:41-49, 56-58 (clearVisited seam)
- **Literal Inputs**: `Instance runs target 1 first, then runs fixture (root, 4)`
- **Expected Results**: Earlier run does not alter subsequent search result for target 4; clearVisited resets accumulator
- **Actual Results**: Result for target 4 is identical to fresh isolated runs; passed in testRepeatabilityAndStateIsolation

### Dependency Injection Seam
- **Precondition Lines**: DepthFirstSearch.java:37-39 (custom constructor dependency seam)
- **Literal Inputs**: `new DepthFirstSearch<>(customSinkList), search target 2`
- **Expected Results**: Node 2 found, visited recorded directly in injected list sink [1, 2]
- **Actual Results**: Node 2 found, customSink == [1, 2]; passed in testDependencyInjectionSeam

