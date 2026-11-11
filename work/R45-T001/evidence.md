# Verification Evidence for R45-T001

- **Target**: `src/main/java/com/thealgorithms/datastructures/hashmap/hashing/MainCuckooHashing.java`
- **Test Suite**: `src/test/java/com/thealgorithms/datastructures/hashmap/hashing/MainCuckooHashingTest.java`
- **Command**: `mvn test -Dtest=MainCuckooHashingTest`
- **Tests Run**: 6 (Failures: 0, Errors: 0, Skipped: 0)
- **Status**: SUCCESS

## Tool Versions

- **Python**: `3.15.2 (v3.15.2:5e58eaad7c5, Oct 25 2026, 14:19:34) [Clang 21.0.0 (clang-2100.1.1.101)]`
- **Java**: `openjdk version "21.0.13.0.3" 2026-10-24`
- **Maven**: `Apache Maven 3.10.0 (c43a36b8d67be7e0805a411bc0898af1a51f5472)`

## Fixtures and Regression Contracts

### Smallest Valid Input (Exit immediately)
- **Precondition Lines**: MainCuckooHashing.java:22 (4. Exit), 47-49 (case 4: scan.close(); return;)
- **Literal Input**: `4\n`
- **Expected Result**: Initial menu displayed once; clean termination without exception
- **Actual Result**: Clean termination with exact menu matching expected output; passed in testSmallestValidInputExitDirectly

### Ordinary Input (Workflow: Add 42, Find 42, Load Factor, Exit)
- **Precondition Lines**: MainCuckooHashing.java:19,30-34 (Add Key), 23,51-55 (Find Key), 24,57-59 (Load Factor), 22,47-49 (Exit)
- **Literal Input**: `1\n42\n5\n42\n6\n4\n`
- **Expected Result**: Key 42 added at bucket 0 (42 % 7 == 0); index 0 printed; load factor 0.14 printed; clean exit
- **Actual Result**: Exact string output match for prompts, index 0, and load factor 0.14; passed in testOrdinaryInputWorkflow

### Domain Boundary (Repeated value / duplicate insertion)
- **Precondition Lines**: MainCuckooHashing.java:19,30-34; HashMapCuckooHashing.java:84-86 (checkTableContainsKey duplicate check)
- **Literal Input**: `1\n10\n1\n10\n`
- **Expected Result**: IllegalArgumentException with message 'Key already exists; duplicates are not allowed.'
- **Actual Result**: IllegalArgumentException thrown with expected message and prompt prefix; passed in testDomainBoundaryDuplicateKeyThrowsException

### Domain Boundary (Unexpected choice value)
- **Precondition Lines**: MainCuckooHashing.java:65-67 (default: throw new IllegalArgumentException('Unexpected value: ' + choice))
- **Literal Input**: `8\n`
- **Expected Result**: IllegalArgumentException with message 'Unexpected value: 8'
- **Actual Result**: IllegalArgumentException thrown with expected message 'Unexpected value: 8'; passed in testDomainBoundaryUnexpectedMenuChoiceThrowsException

### Observable Mutation (Table display reflection)
- **Precondition Lines**: MainCuckooHashing.java:19,30-34, 21,42-45, 22,47-49
- **Literal Input**: `1\n5\n3\n4\n`
- **Expected Result**: Table display contains 'Bucket 5: 5' and unoccupied buckets 'Bucket 0: Empty'
- **Actual Result**: Output contains 'Bucket 5: 5' verifying mutation of internal hash table state; passed in testObservableMutationViaTableDisplay

### Private Constructor Contract (Reflection)
- **Precondition Lines**: MainCuckooHashing.java:6-7
- **Literal Input**: `Reflection getDeclaredConstructor()`
- **Expected Result**: Instantiates MainCuckooHashing without exception
- **Actual Result**: Instance created successfully; passed in testPrivateConstructorContract

