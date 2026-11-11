#!/usr/bin/env python3
"""Runner for task R45-T011: contract regression checks for ComplexNumberMultiply.

Runs the focused JUnit test through Maven, requires a nonzero test count,
saves inputs, expected versus actual results, and tool versions into work/R45-T011/,
and exits non-zero on any failure.
"""
import glob
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

HERE = Path(__file__).resolve().parent
REPO = HERE.parent.parent
TEST_CLASS = "ComplexNumberMultiplyContractTest"
CMD = ["mvn", "-B", "test", f"-Dtest={TEST_CLASS}", "-Dsurefire.failIfNoSpecifiedTests=false"]


def tool_version(cmd):
    try:
        out = subprocess.run(cmd, capture_output=True, text=True, cwd=REPO)
        return "\n".join(l for l in (out.stdout + out.stderr).splitlines()
                         if not l.startswith("Picked up")).strip()
    except FileNotFoundError:
        return f"{cmd[0]}: not found"


def main():
    (HERE / "tool_versions.txt").write_text(
        "java:\n" + tool_version(["java", "-version"]) + "\n\nmvn:\n"
        + tool_version(["mvn", "-v"]) + "\n\npython:\n" + sys.version + "\n")

    print("Running:", " ".join(CMD))
    proc = subprocess.run(CMD, capture_output=True, text=True, cwd=REPO)
    combined_output = proc.stdout + proc.stderr
    (HERE / "mvn_output.txt").write_text(combined_output)

    comparisons = [line.strip() for line in combined_output.splitlines()
                   if line.strip().startswith("[R45-T011]")]
    (HERE / "comparisons.txt").write_text(
        ("\n".join(comparisons) + "\n") if comparisons else "No comparison records captured.\n")

    reports = glob.glob(str(REPO / "target/surefire-reports" / f"TEST-*{TEST_CLASS}.xml"))
    if not reports:
        (HERE / "result.txt").write_text(
            f"command: {' '.join(CMD)}\nFAIL: no surefire report found\n")
        print("FAIL: no surefire report found")
        return 1

    suite = ET.parse(reports[0]).getroot()
    tests = int(suite.get("tests", 0))
    failures = int(suite.get("failures", 0)) + int(suite.get("errors", 0))
    skipped = int(suite.get("skipped", 0))
    passed = proc.returncode == 0 and failures == 0 and tests > 0 and len(comparisons) > 0
    status = "PASS" if passed else "FAIL"
    summary = (
        f"command: {' '.join(CMD)}\n"
        f"tests={tests} failures/errors={failures} skipped={skipped} "
        f"comparisons={len(comparisons)} mvn_exit={proc.returncode}\n"
        f"status={status}\n"
    )
    (HERE / "result.txt").write_text(summary)
    print(summary)

    return 0 if passed else 1


if __name__ == "__main__":
    sys.exit(main())
