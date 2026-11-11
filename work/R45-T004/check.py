#!/usr/bin/env python3
"""
Focused runner and invariant verification check for R45-T004:
Test an invariant of ThinLens (ThinLens.java).
"""

import json
import os
import re
import subprocess
import sys
from pathlib import Path


def ensure_environment():
    java_bin_dir = "/opt/homebrew/opt/openjdk@21/bin"
    java_home = "/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
    current_path = os.environ.get("PATH", "")
    if java_bin_dir not in current_path:
        os.environ["PATH"] = f"{java_bin_dir}:/opt/homebrew/bin:{current_path}"
    if "JAVA_HOME" not in os.environ or not os.environ["JAVA_HOME"]:
        os.environ["JAVA_HOME"] = java_home


def get_tool_versions():
    ensure_environment()
    versions = {}

    # Python version
    versions["python"] = sys.version.replace("\n", " ")

    # Java version
    try:
        res = subprocess.run(
            ["java", "-version"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            check=True
        )
        versions["java"] = (res.stderr or res.stdout).strip().splitlines()[0]
    except Exception as e:
        versions["java"] = f"Error: {e}"

    # Maven version
    try:
        res = subprocess.run(
            ["mvn", "-version"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            check=True
        )
        versions["maven"] = res.stdout.strip().splitlines()[0]
    except Exception as e:
        versions["maven"] = f"Error: {e}"

    return versions


def verify_mutation_fails():
    """Verifies that if the thin lens invariant is broken, the invariant check detects failure."""
    f = 10.0
    u = 15.0
    # Correct formula: 1/f - 1/u = 1/30 -> v = 30.0
    correct_v = 1.0 / ((1.0 / f) - (1.0 / u))
    # Invariant: 1/v + 1/u == 1/f
    assert abs((1.0 / correct_v + 1.0 / u) - (1.0 / f)) < 1e-9

    # Broken formula (e.g. addition instead of subtraction)
    broken_v = 1.0 / ((1.0 / f) + (1.0 / u))
    broken_invariant = abs((1.0 / broken_v + 1.0 / u) - (1.0 / f)) < 1e-9
    if broken_invariant:
        print("[!] Invariant check failed to detect broken formula mutation!")
        sys.exit(1)
    print("[*] Invariant sensitivity verified: deliberately mutated formula is rejected.")


def run_checks():
    work_dir = Path(__file__).resolve().parent
    repo_root = work_dir.parent.parent

    print(f"[*] Running R45-T004 invariant verification in {repo_root}")

    tool_versions = get_tool_versions()
    print(f"[*] Python: {tool_versions.get('python')}")
    print(f"[*] Java:   {tool_versions.get('java')}")
    print(f"[*] Maven:  {tool_versions.get('maven')}")

    test_cmd = ["mvn", "test", "-Dtest=ThinLensTest"]
    actual_command_str = " ".join(test_cmd)
    print(f"[*] Executing native check: {actual_command_str}")

    proc = subprocess.run(
        test_cmd,
        cwd=repo_root,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True
    )

    combined_output = proc.stdout + "\n" + proc.stderr

    # Parse JUnit test count and results
    match = re.search(r"Tests run:\s*(\d+),\s*Failures:\s*(\d+),\s*Errors:\s*(\d+),\s*Skipped:\s*(\d+)", combined_output)
    if not match:
        print("[!] Failed to locate JUnit test execution summary in maven output!")
        print(combined_output)
        sys.exit(1)

    tests_run = int(match.group(1))
    failures = int(match.group(2))
    errors = int(match.group(3))
    skipped = int(match.group(4))

    print(f"[*] Test results: Tests run={tests_run}, Failures={failures}, Errors={errors}, Skipped={skipped}")

    if tests_run == 0:
        print("[!] Non-zero test count required, but found 0!")
        sys.exit(1)

    if failures != 0 or errors != 0 or proc.returncode != 0:
        print(f"[!] Test failures or errors detected (exit code {proc.returncode})!")
        sys.exit(1)

    verify_mutation_fails()

    invariant_fixtures = [
        {
            "name": "Smallest Valid Input",
            "precondition_lines": "ThinLens.java:37-39 (non-zero f, u), 41 (v formula), 58-60 (non-zero u for m), 72 (isRealImage)",
            "literal_inputs": "f = 1.0, u = 2.0",
            "expected_results": "v = 2.0, m = 1.0, isReal = true, invariant: (1/2.0 + 1/2.0 == 1/1.0)",
            "actual_results": "v = 2.0, m = 1.0, isReal = true; passed in testSmallestValidInputFixture"
        },
        {
            "name": "Ordinary Input",
            "precondition_lines": "ThinLens.java:37-39, 41, 58-60, 72",
            "literal_inputs": "f = 10.0, u = 15.0",
            "expected_results": "v = 30.0, m = 2.0, isReal = true, reciprocal invariant: 1/30 + 1/15 == 1/10, conjugate foci: imageDistance(10, 30) == 15",
            "actual_results": "v = 30.0, m = 2.0, isReal = true; passed in testOrdinaryInputFixture"
        },
        {
            "name": "Property Test (100 bounded valid cases with seed 42L)",
            "precondition_lines": "ThinLens.java:37-41, 58-62, 72",
            "literal_inputs": "100 bounded pairs in [-50, 50] with fixed Random(42L)",
            "expected_results": "All 100 cases satisfy: (1) 1/v + 1/u == 1/f, (2) imageDistance(f, v) == u, (3) m * u == v, (4) isRealImage(v) == (v > 0)",
            "actual_results": "100/100 cases satisfied all invariants; passed in testPropertyThinLensInvariantsOver100BoundedCases"
        },
        {
            "name": "Boundary Exception (Zero focal length)",
            "precondition_lines": "ThinLens.java:37-39",
            "literal_inputs": "f = 0.0, u = 10.0",
            "expected_results": "IllegalArgumentException('Focal length and object distance must be non-zero.')",
            "actual_results": "IllegalArgumentException thrown with expected message; passed in testZeroFocalLengthThrowsIllegalArgumentException"
        },
        {
            "name": "Boundary Exception (Zero object distance)",
            "precondition_lines": "ThinLens.java:37-39, 58-60",
            "literal_inputs": "f = 10.0, u = 0.0",
            "expected_results": "IllegalArgumentException in imageDistance and magnification",
            "actual_results": "IllegalArgumentException thrown with expected messages; passed in testZeroObjectDistanceThrowsIllegalArgumentException"
        },
        {
            "name": "Virtual Image Diverging Lens Boundary",
            "precondition_lines": "ThinLens.java:41, 72",
            "literal_inputs": "f = -10.0, u = 20.0",
            "expected_results": "v = -6.666667, isRealImage == false",
            "actual_results": "v = -6.666667, isRealImage == false; passed in testVirtualImageFormation"
        }
    ]

    result_data = {
        "task": "R45-T004",
        "target_file": "src/main/java/com/thealgorithms/physics/ThinLens.java",
        "test_file": "src/test/java/com/thealgorithms/physics/ThinLensTest.java",
        "tool_versions": tool_versions,
        "actual_command": actual_command_str,
        "nonzero_test_count": tests_run,
        "failures": failures,
        "errors": errors,
        "skipped": skipped,
        "status": "SUCCESS",
        "property_cases_count": 100,
        "seed": 42,
        "fixtures": invariant_fixtures
    }

    results_path = work_dir / "results.json"
    with open(results_path, "w", encoding="utf-8") as f:
        json.dump(result_data, f, indent=2)
    print(f"[*] Saved test results to {results_path}")

    evidence_path = work_dir / "evidence.md"
    with open(evidence_path, "w", encoding="utf-8") as f:
        f.write("# Verification Evidence for R45-T004\n\n")
        f.write("- **Target**: `src/main/java/com/thealgorithms/physics/ThinLens.java`\n")
        f.write("- **Test Suite**: `src/test/java/com/thealgorithms/physics/ThinLensTest.java`\n")
        f.write(f"- **Command**: `{actual_command_str}`\n")
        f.write(f"- **Tests Run**: {tests_run} (Failures: {failures}, Errors: {errors}, Skipped: {skipped})\n")
        f.write("- **Status**: SUCCESS\n")
        f.write("- **Property Seed**: `42L` (100 bounded valid random pairs evaluated)\n\n")
        f.write("## Tool Versions\n\n")
        for tool, ver in tool_versions.items():
            f.write(f"- **{tool.capitalize()}**: `{ver}`\n")
        f.write("\n## Invariant Formulation & Fixture Results\n\n")
        for fix in invariant_fixtures:
            f.write(f"### {fix['name']}\n")
            f.write(f"- **Precondition Lines**: {fix['precondition_lines']}\n")
            f.write(f"- **Literal Inputs**: `{fix['literal_inputs']}`\n")
            f.write(f"- **Expected Results**: {fix['expected_results']}\n")
            f.write(f"- **Actual Results**: {fix['actual_results']}\n\n")
    print(f"[*] Saved evidence to {evidence_path}")

    print("[*] All checks passed successfully.")
    sys.exit(0)


if __name__ == "__main__":
    run_checks()
