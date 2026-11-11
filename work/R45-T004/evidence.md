# Verification Evidence for R45-T004

- **Target**: `src/main/java/com/thealgorithms/physics/ThinLens.java`
- **Test Suite**: `src/test/java/com/thealgorithms/physics/ThinLensTest.java`
- **Command**: `mvn test -Dtest=ThinLensTest`
- **Tests Run**: 9 (Failures: 0, Errors: 0, Skipped: 0)
- **Status**: SUCCESS
- **Property Seed**: `42L` (100 bounded valid random pairs evaluated)

## Tool Versions

- **Python**: `3.15.2 (v3.15.2:5e58eaad7c5, Oct 25 2026, 14:19:34) [Clang 21.0.0 (clang-2100.1.1.101)]`
- **Java**: `openjdk version "21.0.13.0.3" 2026-10-24`
- **Maven**: `Apache Maven 3.10.0 (c43a36b8d67be7e0805a411bc0898af1a51f5472)`

## Invariant Formulation & Fixture Results

### Smallest Valid Input
- **Precondition Lines**: ThinLens.java:37-39 (non-zero f, u), 41 (v formula), 58-60 (non-zero u for m), 72 (isRealImage)
- **Literal Inputs**: `f = 1.0, u = 2.0`
- **Expected Results**: v = 2.0, m = 1.0, isReal = true, invariant: (1/2.0 + 1/2.0 == 1/1.0)
- **Actual Results**: v = 2.0, m = 1.0, isReal = true; passed in testSmallestValidInputFixture

### Ordinary Input
- **Precondition Lines**: ThinLens.java:37-39, 41, 58-60, 72
- **Literal Inputs**: `f = 10.0, u = 15.0`
- **Expected Results**: v = 30.0, m = 2.0, isReal = true, reciprocal invariant: 1/30 + 1/15 == 1/10, conjugate foci: imageDistance(10, 30) == 15
- **Actual Results**: v = 30.0, m = 2.0, isReal = true; passed in testOrdinaryInputFixture

### Property Test (100 bounded valid cases with seed 42L)
- **Precondition Lines**: ThinLens.java:37-41, 58-62, 72
- **Literal Inputs**: `100 bounded pairs in [-50, 50] with fixed Random(42L)`
- **Expected Results**: All 100 cases satisfy: (1) 1/v + 1/u == 1/f, (2) imageDistance(f, v) == u, (3) m * u == v, (4) isRealImage(v) == (v > 0)
- **Actual Results**: 100/100 cases satisfied all invariants; passed in testPropertyThinLensInvariantsOver100BoundedCases

### Boundary Exception (Zero focal length)
- **Precondition Lines**: ThinLens.java:37-39
- **Literal Inputs**: `f = 0.0, u = 10.0`
- **Expected Results**: IllegalArgumentException('Focal length and object distance must be non-zero.')
- **Actual Results**: IllegalArgumentException thrown with expected message; passed in testZeroFocalLengthThrowsIllegalArgumentException

### Boundary Exception (Zero object distance)
- **Precondition Lines**: ThinLens.java:37-39, 58-60
- **Literal Inputs**: `f = 10.0, u = 0.0`
- **Expected Results**: IllegalArgumentException in imageDistance and magnification
- **Actual Results**: IllegalArgumentException thrown with expected messages; passed in testZeroObjectDistanceThrowsIllegalArgumentException

### Virtual Image Diverging Lens Boundary
- **Precondition Lines**: ThinLens.java:41, 72
- **Literal Inputs**: `f = -10.0, u = 20.0`
- **Expected Results**: v = -6.666667, isRealImage == false
- **Actual Results**: v = -6.666667, isRealImage == false; passed in testVirtualImageFormation

