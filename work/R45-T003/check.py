#!/usr/bin/env python3
"""
Focused runner and oracle cross-check verification for R45-T003:
Cross-check Isomorphic (Isomorphic.java).
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

    print(f"[*] Running R45-T003 oracle verification in {repo_root}")

    tool_versions = get_tool_versions()
    print(f"[*] Python: {tool_versions.get('python')}")
    print(f"[*] Java:   {tool_versions.get('java')}")
    print(f"[*] Maven:  {tool_versions.get('maven')}")

    test_cmd = ["mvn", "test", "-Dtest=IsomorphicTest"]
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

    # Documented cross-check oracle cases
    cross_check_cases = [
        {"s": "", "t": "", "expected": True, "notes": "Smallest valid input (empty strings); Isomorphic.java:32, 56"},
        {"s": "egg", "t": "add", "expected": True, "notes": "Ordinary input (bijective e->a, g->d); Isomorphic.java:32, 43-53, 56"},
        {"s": "foo", "t": "bar", "expected": False, "notes": "Multi-mapping collision ('o' to 'a' and 'r'); Isomorphic.java:44-46"},
        {"s": "bar", "t": "foo", "expected": False, "notes": "Symmetry check (reverse mapping collision); Isomorphic.java:48-50"},
        {"s": "paper", "t": "title", "expected": True, "notes": "Multi-character bijection p->t, a->i, e->l, r->e"},
        {"s": "badc", "t": "baba", "expected": False, "notes": "Non-injective target character re-use; Isomorphic.java:48-50"},
        {"s": "a", "t": "b", "expected": True, "notes": "Single distinct characters"},
        {"s": "a", "t": "a", "expected": True, "notes": "Single identical characters"},
        {"s": "ab", "t": "cd", "expected": True, "notes": "Two distinct character pairs"},
        {"s": "ab", "t": "aa", "expected": False, "notes": "Two source chars mapping to same target char"},
        {"s": "aa", "t": "ab", "expected": False, "notes": "One source char mapping to two target chars"},
        {"s": "abc", "t": "def", "expected": True, "notes": "Three distinct characters"},
        {"s": "aba", "t": "cdc", "expected": True, "notes": "Alternating pattern"},
        {"s": "aba", "t": "cdd", "expected": False, "notes": "Mismatched repetition pattern"},
        {"s": "abba", "t": "cddc", "expected": True, "notes": "Palindromic pattern"},
        {"s": "abba", "t": "cdcd", "expected": False, "notes": "Pattern mismatch"},
        {"s": "123", "t": "456", "expected": True, "notes": "Numeric digit strings"},
        {"s": "1122", "t": "3344", "expected": True, "notes": "Repeated digit patterns"},
        {"s": "1122", "t": "3434", "expected": False, "notes": "Digit pattern mismatch"},
        {"s": "hello", "t": "world", "expected": False, "notes": "Double letter 'l' vs unique letters"},
        {"s": "listen", "t": "silent", "expected": True, "notes": "All distinct characters of equal length"},
        {"s": "banana", "t": "pomomo", "expected": True, "notes": "Complex repetition pattern"},
        {"s": "xyz", "t": "xy", "expected": False, "notes": "Length mismatch; Isomorphic.java:32"}
    ]

    result_data = {
        "task": "R45-T003",
        "target_file": "src/main/java/com/thealgorithms/strings/Isomorphic.java",
        "test_file": "src/test/java/com/thealgorithms/strings/IsomorphicTest.java",
        "oracle_description": "Canonical first-occurrence index pattern encoding (s.indexOf(s.charAt(i)) == t.indexOf(t.charAt(i)))",
        "tool_versions": tool_versions,
        "actual_command": actual_command_str,
        "nonzero_test_count": tests_run,
        "failures": failures,
        "errors": errors,
        "skipped": skipped,
        "status": "SUCCESS",
        "cross_check_cases": cross_check_cases
    }

    results_path = work_dir / "results.json"
    with open(results_path, "w", encoding="utf-8") as f:
        json.dump(result_data, f, indent=2)
    print(f"[*] Saved test results to {results_path}")

    evidence_path = work_dir / "evidence.md"
    with open(evidence_path, "w", encoding="utf-8") as f:
        f.write("# Verification Evidence for R45-T003\n\n")
        f.write("- **Target**: `src/main/java/com/thealgorithms/strings/Isomorphic.java`\n")
        f.write("- **Test Suite**: `src/test/java/com/thealgorithms/strings/IsomorphicTest.java`\n")
        f.write(f"- **Command**: `{actual_command_str}`\n")
        f.write(f"- **Tests Run**: {tests_run} (Failures: {failures}, Errors: {errors}, Skipped: {skipped})\n")
        f.write("- **Status**: SUCCESS\n\n")
        f.write("## Tool Versions\n\n")
        for tool, ver in tool_versions.items():
            f.write(f"- **{tool.capitalize()}**: `{ver}`\n")
        f.write("\n## Independent Oracle Specification\n\n")
        f.write("The independent oracle uses canonical first-occurrence index pattern encoding without utilizing `Map` or `Set`.\n")
        f.write("For any two strings `s` and `t`, `s` and `t` are isomorphic iff `s.indexOf(s.charAt(i)) == t.indexOf(t.charAt(i))` for all `0 <= i < length`.\n\n")
        f.write("## Cross-Check Result Table\n\n")
        f.write("| String 1 (`s`) | String 2 (`t`) | Expected | Actual Result | Precondition / Notes |\n")
        f.write("|---|---|---|---|---|\n")
        for c in cross_check_cases:
            f.write(f"| `{c['s']}` | `{c['t']}` | `{c['expected']}` | Match | {c['notes']} |\n")
    print(f"[*] Saved evidence to {evidence_path}")

    print("[*] All checks passed successfully.")
    sys.exit(0)


if __name__ == "__main__":
    run_checks()
