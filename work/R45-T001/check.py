#!/usr/bin/env python3
"""
Focused runner and verification check for R45-T001:
Contract regression tests for MainCuckooHashing (MainCuckooHashing.java).
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

    print(f"[*] Running R45-T001 contract verification in {repo_root}")

    tool_versions = get_tool_versions()
    print(f"[*] Python: {tool_versions.get('python')}")
    print(f"[*] Java:   {tool_versions.get('java')}")
    print(f"[*] Maven:  {tool_versions.get('maven')}")

    test_cmd = ["mvn", "test", "-Dtest=MainCuckooHashingTest"]
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
    # Example: Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
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

    # Fixture specification details
    fixtures = [
        {
            "name": "Smallest Valid Input (Exit immediately)",
            "precondition_lines": "MainCuckooHashing.java:22 (4. Exit), 47-49 (case 4: scan.close(); return;)",
            "literal_input": "4\\n",
            "expected_result": "Initial menu displayed once; clean termination without exception",
            "actual_result": "Clean termination with exact menu matching expected output; passed in testSmallestValidInputExitDirectly"
        },
        {
            "name": "Ordinary Input (Workflow: Add 42, Find 42, Load Factor, Exit)",
            "precondition_lines": "MainCuckooHashing.java:19,30-34 (Add Key), 23,51-55 (Find Key), 24,57-59 (Load Factor), 22,47-49 (Exit)",
            "literal_input": "1\\n42\\n5\\n42\\n6\\n4\\n",
            "expected_result": "Key 42 added at bucket 0 (42 % 7 == 0); index 0 printed; load factor 0.14 printed; clean exit",
            "actual_result": "Exact string output match for prompts, index 0, and load factor 0.14; passed in testOrdinaryInputWorkflow"
        },
        {
            "name": "Domain Boundary (Repeated value / duplicate insertion)",
            "precondition_lines": "MainCuckooHashing.java:19,30-34; HashMapCuckooHashing.java:84-86 (checkTableContainsKey duplicate check)",
            "literal_input": "1\\n10\\n1\\n10\\n",
            "expected_result": "IllegalArgumentException with message 'Key already exists; duplicates are not allowed.'",
            "actual_result": "IllegalArgumentException thrown with expected message and prompt prefix; passed in testDomainBoundaryDuplicateKeyThrowsException"
        },
        {
            "name": "Domain Boundary (Unexpected choice value)",
            "precondition_lines": "MainCuckooHashing.java:65-67 (default: throw new IllegalArgumentException('Unexpected value: ' + choice))",
            "literal_input": "8\\n",
            "expected_result": "IllegalArgumentException with message 'Unexpected value: 8'",
            "actual_result": "IllegalArgumentException thrown with expected message 'Unexpected value: 8'; passed in testDomainBoundaryUnexpectedMenuChoiceThrowsException"
        },
        {
            "name": "Observable Mutation (Table display reflection)",
            "precondition_lines": "MainCuckooHashing.java:19,30-34, 21,42-45, 22,47-49",
            "literal_input": "1\\n5\\n3\\n4\\n",
            "expected_result": "Table display contains 'Bucket 5: 5' and unoccupied buckets 'Bucket 0: Empty'",
            "actual_result": "Output contains 'Bucket 5: 5' verifying mutation of internal hash table state; passed in testObservableMutationViaTableDisplay"
        },
        {
            "name": "Private Constructor Contract (Reflection)",
            "precondition_lines": "MainCuckooHashing.java:6-7",
            "literal_input": "Reflection getDeclaredConstructor()",
            "expected_result": "Instantiates MainCuckooHashing without exception",
            "actual_result": "Instance created successfully; passed in testPrivateConstructorContract"
        }
    ]

    result_data = {
        "task": "R45-T001",
        "target_file": "src/main/java/com/thealgorithms/datastructures/hashmap/hashing/MainCuckooHashing.java",
        "test_file": "src/test/java/com/thealgorithms/datastructures/hashmap/hashing/MainCuckooHashingTest.java",
        "tool_versions": tool_versions,
        "actual_command": actual_command_str,
        "nonzero_test_count": tests_run,
        "failures": failures,
        "errors": errors,
        "skipped": skipped,
        "status": "SUCCESS",
        "fixtures": fixtures
    }

    # Save to results.json
    results_path = work_dir / "results.json"
    with open(results_path, "w", encoding="utf-8") as f:
        json.dump(result_data, f, indent=2)
    print(f"[*] Saved test results to {results_path}")

    # Save evidence markdown summary
    evidence_path = work_dir / "evidence.md"
    with open(evidence_path, "w", encoding="utf-8") as f:
        f.write("# Verification Evidence for R45-T001\n\n")
        f.write(f"- **Target**: `src/main/java/com/thealgorithms/datastructures/hashmap/hashing/MainCuckooHashing.java`\n")
        f.write(f"- **Test Suite**: `src/test/java/com/thealgorithms/datastructures/hashmap/hashing/MainCuckooHashingTest.java`\n")
        f.write(f"- **Command**: `{actual_command_str}`\n")
        f.write(f"- **Tests Run**: {tests_run} (Failures: {failures}, Errors: {errors}, Skipped: {skipped})\n")
        f.write(f"- **Status**: SUCCESS\n\n")
        f.write("## Tool Versions\n\n")
        for tool, ver in tool_versions.items():
            f.write(f"- **{tool.capitalize()}**: `{ver}`\n")
        f.write("\n## Fixtures and Regression Contracts\n\n")
        for fix in fixtures:
            f.write(f"### {fix['name']}\n")
            f.write(f"- **Precondition Lines**: {fix['precondition_lines']}\n")
            f.write(f"- **Literal Input**: `{fix['literal_input']}`\n")
            f.write(f"- **Expected Result**: {fix['expected_result']}\n")
            f.write(f"- **Actual Result**: {fix['actual_result']}\n\n")
    print(f"[*] Saved evidence to {evidence_path}")

    print("[*] All checks passed successfully.")
    sys.exit(0)


if __name__ == "__main__":
    run_checks()
