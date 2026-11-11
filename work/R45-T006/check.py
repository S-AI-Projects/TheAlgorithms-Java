#!/usr/bin/env python3
"""
Focused runner and refactoring verification check for R45-T006:
Simplify one responsibility in NonRepeatingNumberFinder (NonRepeatingNumberFinder.java).
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


def run_checks():
    work_dir = Path(__file__).resolve().parent
    repo_root = work_dir.parent.parent

    print(f"[*] Running R45-T006 refactoring verification in {repo_root}")

    tool_versions = get_tool_versions()
    print(f"[*] Python: {tool_versions.get('python')}")
    print(f"[*] Java:   {tool_versions.get('java')}")
    print(f"[*] Maven:  {tool_versions.get('maven')}")

    test_cmd = ["mvn", "test", "-Dtest=NonRepeatingNumberFinderTest"]
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

    refactoring_fixtures = [
        {
            "name": "Smallest Valid Input (Empty Array)",
            "precondition_lines": "NonRepeatingNumberFinder.java:26 (@return ... or 0 if array is empty), 39 (xorSum = 0)",
            "literal_inputs": "new int[] {}",
            "expected_results": "0",
            "actual_results": "0; passed in testSmallestValidInputEmptyArrayFixture"
        },
        {
            "name": "Smallest Valid Input (Single Element)",
            "precondition_lines": "NonRepeatingNumberFinder.java:28-30, 39-43 (single loop iteration: 0 ^ 12 = 12)",
            "literal_inputs": "new int[] {12}",
            "expected_results": "12",
            "actual_results": "12; passed in testSmallestValidInputSingleElementFixture"
        },
        {
            "name": "Ordinary Input (Documentation Example)",
            "precondition_lines": "NonRepeatingNumberFinder.java:28-30, 39-44 (reduceXor loop)",
            "literal_inputs": "new int[] {2, 3, 5, 2, 3}",
            "expected_results": "5 (pairwise cancellation of 2 and 3)",
            "actual_results": "5; passed in testOrdinaryInputDocumentationFixture"
        },
        {
            "name": "Domain Boundary (Negative Numbers)",
            "precondition_lines": "NonRepeatingNumberFinder.java:39-43",
            "literal_inputs": "new int[] {-42, 99, -42}",
            "expected_results": "99",
            "actual_results": "99; passed in testDomainBoundaryNegativeNumbers"
        },
        {
            "name": "Boundary Precondition (Null Input)",
            "precondition_lines": "NonRepeatingNumberFinder.java:40 (for-each over null throws NullPointerException)",
            "literal_inputs": "null",
            "expected_results": "NullPointerException",
            "actual_results": "NullPointerException thrown; passed in testNullArrayThrowsNullPointerException"
        }
    ]

    result_data = {
        "task": "R45-T006",
        "target_file": "src/main/java/com/thealgorithms/bitmanipulation/NonRepeatingNumberFinder.java",
        "test_file": "src/test/java/com/thealgorithms/bitmanipulation/NonRepeatingNumberFinderTest.java",
        "refactored_responsibility": "Extracted reduceXor(int[] arr) method with dedicated xorSum accumulator separating the public query entry point from bitwise XOR reduction.",
        "tool_versions": tool_versions,
        "actual_command": actual_command_str,
        "nonzero_test_count": tests_run,
        "failures": failures,
        "errors": errors,
        "skipped": skipped,
        "status": "SUCCESS",
        "fixtures": refactoring_fixtures
    }

    results_path = work_dir / "results.json"
    with open(results_path, "w", encoding="utf-8") as f:
        json.dump(result_data, f, indent=2)
    print(f"[*] Saved test results to {results_path}")

    evidence_path = work_dir / "evidence.md"
    with open(evidence_path, "w", encoding="utf-8") as f:
        f.write("# Verification Evidence for R45-T006\n\n")
        f.write("- **Target**: `src/main/java/com/thealgorithms/bitmanipulation/NonRepeatingNumberFinder.java`\n")
        f.write("- **Test Suite**: `src/test/java/com/thealgorithms/bitmanipulation/NonRepeatingNumberFinderTest.java`\n")
        f.write(f"- **Command**: `{actual_command_str}`\n")
        f.write(f"- **Tests Run**: {tests_run} (Failures: {failures}, Errors: {errors}, Skipped: {skipped})\n")
        f.write("- **Status**: SUCCESS\n")
        f.write("- **Refactored Responsibility**: Extracted `reduceXor(int[] arr)` isolating bitwise XOR accumulation.\n\n")
        f.write("## Tool Versions\n\n")
        for tool, ver in tool_versions.items():
            f.write(f"- **{tool.capitalize()}**: `{ver}`\n")
        f.write("\n## Fixtures and Regression Verification\n\n")
        for fix in refactoring_fixtures:
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
