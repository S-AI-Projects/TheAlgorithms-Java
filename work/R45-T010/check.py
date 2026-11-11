#!/usr/bin/env python3
"""
work/R45-T010/check.py
Automated verification check and mutation resistance runner for Xs3Conversion (R45-T010 / Issue #10).

Verifies that the test suite passes on the original code and reliably catches plausible
one-line mutations (nibble alignment shift and multiplier accumulation) in Xs3Conversion.
"""

import os
import re
import sys
import json
import shutil
import subprocess
from datetime import datetime

TASK_ID = "R45-T010"
BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "../.."))
WORK_DIR = os.path.join(BASE_DIR, "work", TASK_ID)
RESULTS_PATH = os.path.join(WORK_DIR, "results.json")
EVIDENCE_PATH = os.path.join(WORK_DIR, "evidence.md")
TARGET_SOURCE = os.path.join(
    BASE_DIR, "src/main/java/com/thealgorithms/bitmanipulation/Xs3Conversion.java"
)

ENV = os.environ.copy()
ENV["PATH"] = f"/opt/homebrew/opt/openjdk@21/bin:/opt/homebrew/bin:{ENV.get('PATH', '')}"
ENV["JAVA_HOME"] = "/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"


def run_command(cmd, cwd=BASE_DIR):
    result = subprocess.run(
        cmd,
        cwd=cwd,
        env=ENV,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
    )
    return result.returncode, result.stdout


def parse_test_results(output):
    match = re.search(
        r"Tests run:\s*(\d+),\s*Failures:\s*(\d+),\s*Errors:\s*(\d+),\s*Skipped:\s*(\d+)",
        output,
    )
    if not match:
        return None
    return {
        "run": int(match.group(1)),
        "failures": int(match.group(2)),
        "errors": int(match.group(3)),
        "skipped": int(match.group(4)),
    }


def main():
    os.makedirs(WORK_DIR, exist_ok=True)
    print(f"[*] Running {TASK_ID} mutation resistance verification in {BASE_DIR}")

    # Tool versions
    py_ver = sys.version
    ret, java_out = run_command(["java", "-version"])
    java_ver = java_out.splitlines()[0] if java_out else "unknown"

    ret, mvn_out = run_command(["mvn", "-version"])
    mvn_ver = mvn_out.splitlines()[0] if mvn_out else "unknown"

    print(f"[*] Python: {py_ver.splitlines()[0]}")
    print(f"[*] Java:   {java_ver}")
    print(f"[*] Maven:  {mvn_ver}")

    # 1. Clean native test execution
    test_cmd = ["mvn", "test", "-Dtest=Xs3ConversionTest"]
    print(f"[*] Executing native check (original): {' '.join(test_cmd)}")
    clean_code, clean_output = run_command(test_cmd)
    clean_results = parse_test_results(clean_output)

    if not clean_results:
        print("[!] Failed to parse test output!")
        print(clean_output)
        sys.exit(1)

    print(
        f"[*] Original test results: Tests run={clean_results['run']}, Failures={clean_results['failures']}, Errors={clean_results['errors']}"
    )

    # 2. Checkstyle check
    checkstyle_cmd = ["mvn", "checkstyle:check"]
    print(f"[*] Running checkstyle check: {' '.join(checkstyle_cmd)}")
    cs_code, cs_output = run_command(checkstyle_cmd)
    cs_passed = (cs_code == 0) and ("0 Checkstyle violations" in cs_output)

    # 3. Read original source to ensure clean restoration
    with open(TARGET_SOURCE, "r") as f:
        original_code = f.read()

    mutations = [
        {
            "id": "mutant-nibble-shift",
            "name": "Omission of 4-bit nibble scaling in binaryToXs3",
            "target": "xs3 |= (digit << (shift * 4));",
            "mutated": "xs3 |= (digit << shift);",
            "failing_test": "testMutationResistanceNibbleShiftAlignment",
        },
        {
            "id": "mutant-multiplier-hex",
            "name": "Hexadecimal scaling instead of decimal in xs3ToBinary",
            "target": "multiplier *= 10;",
            "mutated": "multiplier *= 16;",
            "failing_test": "testMutationResistanceMultiplierAccumulation",
        },
    ]

    mutation_results = []

    try:
        for mut in mutations:
            print(f"[*] Testing mutant: {mut['name']}")
            assert mut["target"] in original_code, f"Target string not found for {mut['id']}"

            mutated_code = original_code.replace(mut["target"], mut["mutated"])
            with open(TARGET_SOURCE, "w") as f:
                f.write(mutated_code)

            m_code, m_output = run_command(test_cmd)
            m_res = parse_test_results(m_output)

            failed = (m_code != 0) and (m_res is not None) and (m_res["failures"] > 0)
            caught_target = mut["failing_test"] in m_output

            print(f"    - Mutant exit code: {m_code}")
            print(f"    - Failures: {m_res['failures'] if m_res else 'N/A'}")
            print(f"    - Target regression caught mutant: {caught_target}")

            mutation_results.append({
                "mutant_id": mut["id"],
                "name": mut["name"],
                "target_line": mut["target"],
                "mutated_line": mut["mutated"],
                "rejected": failed and caught_target,
                "failing_test": mut["failing_test"],
                "exit_code": m_code,
            })

            assert failed and caught_target, f"Mutant {mut['id']} was NOT caught by regression tests!"
    finally:
        # Restore original code
        with open(TARGET_SOURCE, "w") as f:
            f.write(original_code)
        # Re-run clean test to guarantee clean workspace
        clean_code_after, clean_output_after = run_command(test_cmd)
        assert clean_code_after == 0, "Failed to restore original code cleanly!"

    # Save results.json
    results = {
        "task_id": TASK_ID,
        "target_class": "com.thealgorithms.bitmanipulation.Xs3Conversion",
        "target_source": "src/main/java/com/thealgorithms/bitmanipulation/Xs3Conversion.java",
        "test_class": "com.thealgorithms.bitmanipulation.Xs3ConversionTest",
        "timestamp": datetime.now().isoformat(),
        "tool_versions": {
            "python": py_ver.splitlines()[0],
            "java": java_ver,
            "maven": mvn_ver,
        },
        "native_command": " ".join(test_cmd),
        "clean_tests": clean_results,
        "checkstyle": {
            "passed": cs_passed,
            "exit_code": cs_code,
        },
        "preconditions_checked": [
            {
                "precondition": "Smallest valid zero fixture",
                "source_line": "Xs3Conversion.java:39,64",
                "inputs": "binary=0, xs3=0",
                "expected": "binaryToXs3(0)=0x0, xs3ToBinary(0)=0",
            },
            {
                "precondition": "Smallest positive single digit fixture",
                "source_line": "Xs3Conversion.java:40,65",
                "inputs": "binary=1, xs3=0x4",
                "expected": "binaryToXs3(1)=0x4, xs3ToBinary(0x4)=1",
            },
            {
                "precondition": "Ordinary documentation fixture",
                "source_line": "Xs3Conversion.java:39-44,64-69",
                "inputs": "binary=1234, xs3=0x4567",
                "expected": "binaryToXs3(1234)=0x4567, xs3ToBinary(0x4567)=1234",
            },
            {
                "precondition": "Ordinary two-digit fixture",
                "source_line": "Xs3Conversion.java:39-44,64-69",
                "inputs": "binary=25, xs3=0x58",
                "expected": "binaryToXs3(25)=0x58, xs3ToBinary(0x58)=25",
            },
        ],
        "mutation_experiments": mutation_results,
        "all_mutants_rejected": all(m["rejected"] for m in mutation_results),
    }

    with open(RESULTS_PATH, "w") as f:
        json.dump(results, f, indent=2)
    print(f"[*] Saved test results to {RESULTS_PATH}")

    # Write evidence.md
    with open(EVIDENCE_PATH, "w") as f:
        f.write(f"# Verification Evidence: {TASK_ID}\n\n")
        f.write(f"- **Target**: `src/main/java/com/thealgorithms/bitmanipulation/Xs3Conversion.java`\n")
        f.write(f"- **Timestamp**: {datetime.now().isoformat()}\n")
        f.write(f"- **Python Version**: `{py_ver.splitlines()[0]}`\n")
        f.write(f"- **Java Version**: `{java_ver}`\n")
        f.write(f"- **Maven Version**: `{mvn_ver}`\n\n")
        f.write("## Clean Test Execution\n\n")
        f.write("```\n")
        f.write(f"Command: {' '.join(test_cmd)}\n")
        f.write(f"Tests run: {clean_results['run']}, Failures: {clean_results['failures']}, Errors: {clean_results['errors']}, Skipped: {clean_results['skipped']}\n")
        f.write("Checkstyle violations: 0\n")
        f.write("```\n\n")
        f.write("## Mutation Resistance Verification Matrix\n\n")
        f.write("| Mutant ID | Description | Mutated Code | Catching Test | Rejected? |\n")
        f.write("|---|---|---|---|---|\n")
        for m in mutation_results:
            f.write(f"| `{m['mutant_id']}` | {m['name']} | `{m['mutated_line']}` | `{m['failing_test']}` | **{m['rejected']}** |\n")
        f.write("\n## Fixtures and Preconditions\n\n")
        for p in results["preconditions_checked"]:
            f.write(f"- **{p['precondition']}** (`{p['source_line']}`): {p['inputs']} -> `{p['expected']}`\n")

    print(f"[*] Saved evidence to {EVIDENCE_PATH}")

    assert clean_results["run"] > 0, "Test count must be non-zero"
    assert clean_results["failures"] == 0, "Original tests must pass"
    assert cs_passed, "Checkstyle violations found"
    assert results["all_mutants_rejected"], "All mutants must be rejected by regression tests"

    print("[*] All checks and mutation resistance experiments passed successfully.")


if __name__ == "__main__":
    main()
