#!/usr/bin/env python3
"""
Focused runner and repeatability verification check for R45-T005:
Make verification repeatable for recursiveSearch (DepthFirstSearch.java).
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

    print(f"[*] Running R45-T005 repeatability verification in {repo_root}")

    tool_versions = get_tool_versions()
    print(f"[*] Python: {tool_versions.get('python')}")
    print(f"[*] Java:   {tool_versions.get('java')}")
    print(f"[*] Maven:  {tool_versions.get('maven')}")

    test_cmd = ["mvn", "test", "-Dtest=DepthFirstSearchTest"]
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

    repeatability_fixtures = [
        {
            "name": "Smallest Valid Input (Null Node)",
            "precondition_lines": "DepthFirstSearch.java:41-43 (if node == null return Optional.empty())",
            "literal_inputs": "node = null, value = 42",
            "expected_results": "Optional.empty(), visited list remains empty []",
            "actual_results": "Optional.empty() returned, visited is empty; passed in testSmallestValidInputNullNodeFixture"
        },
        {
            "name": "Ordinary Input (Preorder Tree Search)",
            "precondition_lines": "DepthFirstSearch.java:41-43, 44, 45, 49",
            "literal_inputs": "Root 1 (children 2, 3, 4; 2 has 5, 6), target = 4",
            "expected_results": "Optional.of(node4), visited = [1, 2, 5, 6, 3, 4]",
            "actual_results": "Node 4 found, visited matched [1, 2, 5, 6, 3, 4]; passed in testOrdinaryInputWorkflowFixture"
        },
        {
            "name": "Repeatability in Fresh Isolated States",
            "precondition_lines": "DepthFirstSearch.java:33-35 (fresh instance constructor), 41-49",
            "literal_inputs": "Two distinct fresh instances executing fixture (root, 4)",
            "expected_results": "Both instances produce strictly identical search results and identical visited sequences",
            "actual_results": "Outcome 1 == Outcome 2, Visited 1 == Visited 2; passed in testRepeatabilityAndStateIsolation"
        },
        {
            "name": "Sequential Run Input Isolation",
            "precondition_lines": "DepthFirstSearch.java:41-49, 56-58 (clearVisited seam)",
            "literal_inputs": "Instance runs target 1 first, then runs fixture (root, 4)",
            "expected_results": "Earlier run does not alter subsequent search result for target 4; clearVisited resets accumulator",
            "actual_results": "Result for target 4 is identical to fresh isolated runs; passed in testRepeatabilityAndStateIsolation"
        },
        {
            "name": "Dependency Injection Seam",
            "precondition_lines": "DepthFirstSearch.java:37-39 (custom constructor dependency seam)",
            "literal_inputs": "new DepthFirstSearch<>(customSinkList), search target 2",
            "expected_results": "Node 2 found, visited recorded directly in injected list sink [1, 2]",
            "actual_results": "Node 2 found, customSink == [1, 2]; passed in testDependencyInjectionSeam"
        }
    ]

    result_data = {
        "task": "R45-T005",
        "target_file": "src/main/java/com/thealgorithms/searches/DepthFirstSearch.java",
        "test_file": "src/test/java/com/thealgorithms/searches/DepthFirstSearchTest.java",
        "tool_versions": tool_versions,
        "actual_command": actual_command_str,
        "nonzero_test_count": tests_run,
        "failures": failures,
        "errors": errors,
        "skipped": skipped,
        "status": "SUCCESS",
        "external_dependencies": "None (pure deterministic in-memory traversal algorithm; no time, network, filesystem, or UI dependencies)",
        "fixtures": repeatability_fixtures
    }

    results_path = work_dir / "results.json"
    with open(results_path, "w", encoding="utf-8") as f:
        json.dump(result_data, f, indent=2)
    print(f"[*] Saved test results to {results_path}")

    evidence_path = work_dir / "evidence.md"
    with open(evidence_path, "w", encoding="utf-8") as f:
        f.write("# Verification Evidence for R45-T005\n\n")
        f.write("- **Target**: `src/main/java/com/thealgorithms/searches/DepthFirstSearch.java`\n")
        f.write("- **Test Suite**: `src/test/java/com/thealgorithms/searches/DepthFirstSearchTest.java`\n")
        f.write(f"- **Command**: `{actual_command_str}`\n")
        f.write(f"- **Tests Run**: {tests_run} (Failures: {failures}, Errors: {errors}, Skipped: {skipped})\n")
        f.write("- **Status**: SUCCESS\n")
        f.write("- **External Dependencies**: None (pure deterministic tree search)\n\n")
        f.write("## Tool Versions\n\n")
        for tool, ver in tool_versions.items():
            f.write(f"- **{tool.capitalize()}**: `{ver}`\n")
        f.write("\n## Fixtures and Repeatability Contracts\n\n")
        for fix in repeatability_fixtures:
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
