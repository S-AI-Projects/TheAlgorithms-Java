# Verification Evidence for R45-T003

- **Target**: `src/main/java/com/thealgorithms/strings/Isomorphic.java`
- **Test Suite**: `src/test/java/com/thealgorithms/strings/IsomorphicTest.java`
- **Command**: `mvn test -Dtest=IsomorphicTest`
- **Tests Run**: 34 (Failures: 0, Errors: 0, Skipped: 0)
- **Status**: SUCCESS

## Tool Versions

- **Python**: `3.15.2 (v3.15.2:5e58eaad7c5, Oct 25 2026, 14:19:34) [Clang 21.0.0 (clang-2100.1.1.101)]`
- **Java**: `openjdk version "21.0.13.0.3" 2026-10-24`
- **Maven**: `Apache Maven 3.10.0 (c43a36b8d67be7e0805a411bc0898af1a51f5472)`

## Independent Oracle Specification

The independent oracle uses canonical first-occurrence index pattern encoding without utilizing `Map` or `Set`.
For any two strings `s` and `t`, `s` and `t` are isomorphic iff `s.indexOf(s.charAt(i)) == t.indexOf(t.charAt(i))` for all `0 <= i < length`.

## Cross-Check Result Table

| String 1 (`s`) | String 2 (`t`) | Expected | Actual Result | Precondition / Notes |
|---|---|---|---|---|
| `` | `` | `True` | Match | Smallest valid input (empty strings); Isomorphic.java:32, 56 |
| `egg` | `add` | `True` | Match | Ordinary input (bijective e->a, g->d); Isomorphic.java:32, 43-53, 56 |
| `foo` | `bar` | `False` | Match | Multi-mapping collision ('o' to 'a' and 'r'); Isomorphic.java:44-46 |
| `bar` | `foo` | `False` | Match | Symmetry check (reverse mapping collision); Isomorphic.java:48-50 |
| `paper` | `title` | `True` | Match | Multi-character bijection p->t, a->i, e->l, r->e |
| `badc` | `baba` | `False` | Match | Non-injective target character re-use; Isomorphic.java:48-50 |
| `a` | `b` | `True` | Match | Single distinct characters |
| `a` | `a` | `True` | Match | Single identical characters |
| `ab` | `cd` | `True` | Match | Two distinct character pairs |
| `ab` | `aa` | `False` | Match | Two source chars mapping to same target char |
| `aa` | `ab` | `False` | Match | One source char mapping to two target chars |
| `abc` | `def` | `True` | Match | Three distinct characters |
| `aba` | `cdc` | `True` | Match | Alternating pattern |
| `aba` | `cdd` | `False` | Match | Mismatched repetition pattern |
| `abba` | `cddc` | `True` | Match | Palindromic pattern |
| `abba` | `cdcd` | `False` | Match | Pattern mismatch |
| `123` | `456` | `True` | Match | Numeric digit strings |
| `1122` | `3344` | `True` | Match | Repeated digit patterns |
| `1122` | `3434` | `False` | Match | Digit pattern mismatch |
| `hello` | `world` | `False` | Match | Double letter 'l' vs unique letters |
| `listen` | `silent` | `True` | Match | All distinct characters of equal length |
| `banana` | `pomomo` | `True` | Match | Complex repetition pattern |
| `xyz` | `xy` | `False` | Match | Length mismatch; Isomorphic.java:32 |
