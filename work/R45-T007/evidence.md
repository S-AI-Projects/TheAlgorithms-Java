# Verification Evidence for R45-T007

- **Target**: `src/main/java/com/thealgorithms/strings/SuffixArray.java`
- **Test Suite**: `src/test/java/com/thealgorithms/strings/SuffixArrayTest.java`
- **Command**: `mvn test -Dtest=SuffixArrayTest`
- **Tests Run**: 10 (Failures: 0, Errors: 0, Skipped: 0)
- **Status**: SUCCESS

## Tool Versions

- **Python**: `3.15.2 (v3.15.2:5e58eaad7c5, Oct 25 2026, 14:19:34) [Clang 21.0.0 (clang-2100.1.1.101)]`
- **Java**: `openjdk version "21.0.13.0.3" 2026-10-24`
- **Maven**: `Apache Maven 3.10.0 (c43a36b8d67be7e0805a411bc0898af1a51f5472)`

## Executable Worked Example Trace (text = "banana")

- **Initial ASCII Ranks**: `[98, 97, 110, 97, 110, 97]`
- **k = 1 Permutation (2-grams)**: `[5, 1, 3, 0, 2, 4]`
- **k = 2 Decisive Transition**: All 4-gram pairs acquire unique ranks [0..5]; rank[suffixArray[n - 1]] == 5 triggers early termination break
- **Final Suffix Array**: `[5, 3, 1, 0, 4, 2]`
- **Sorted Suffix Substrings**: `['a', 'ana', 'anana', 'banana', 'na', 'nana']`

## Fixtures and Verified Cases

### Smallest Valid Input (Empty String)
- **Precondition Lines**: SuffixArray.java:54 (n = text.length()), 94 (returns empty array)
- **Literal Inputs**: `text = ""`
- **Expected Results**: new int[] {}
- **Actual Results**: [] (empty array); passed in testSmallestValidInputFixture

### Documented Worked Example (banana)
- **Precondition Lines**: SuffixArray.java:54 (n = 6), 60-64 (initial ranks), 66-93 (doubling steps k=1, k=2), 90-92 (decisive transition break)
- **Literal Inputs**: `text = "banana"`
- **Expected Results**: [5, 3, 1, 0, 4, 2] corresponding to suffixes ['a', 'ana', 'anana', 'banana', 'na', 'nana'] in increasing order
- **Actual Results**: [5, 3, 1, 0, 4, 2] and lexicographical substring ordering verified; passed in testDocumentedWorkedExampleBananaExecutionAndDisplayedOutput

### Smallest Non-Empty Input (Single Character)
- **Precondition Lines**: SuffixArray.java:54, 60-64
- **Literal Inputs**: `text = "a"`
- **Expected Results**: [0]
- **Actual Results**: [0]; passed in testSingleCharacter

### Boundary Precondition (Null Input)
- **Precondition Lines**: SuffixArray.java:54 (text.length() dereference throws NullPointerException)
- **Literal Inputs**: `null`
- **Expected Results**: NullPointerException
- **Actual Results**: NullPointerException thrown; passed in testNullInputThrowsNullPointerException

