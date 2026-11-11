#!/usr/bin/env python3
"""Runner for task R45-T015: determinism checks for HowManyTimesRotated.

Runs the focused JUnit test through Maven, requires a nonzero test count,
saves evidence into work/R45-T015/, and exits non-zero on any failure.
"""
import glob
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

HERE = Path(__file__).resolve().parent
REPO = HERE.parent.parent
TEST_CLASS = "HowManyTimesRotatedDeterminismTest"
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
    (HERE / "mvn_output.txt").write_text(proc.stdout + proc.stderr)

    reports = glob.glob(str(REPO / "target/surefire-reports" / f"TEST-*{TEST_CLASS}.xml"))
    if not reports:
        print("FAIL: no surefire report found")
        return 1
    suite = ET.parse(reports[0]).getroot()
    tests = int(suite.get("tests", 0))
    failures = int(suite.get("failures", 0)) + int(suite.get("errors", 0))
    skipped = int(suite.get("skipped", 0))
    summary = (f"command: {' '.join(CMD)}\ntests={tests} failures/errors={failures} "
               f"skipped={skipped} mvn_exit={proc.returncode}\n")
    (HERE / "result.txt").write_text(summary)
    print(summary)

    if proc.returncode != 0 or failures or tests == 0:
        print("FAIL")
        return 1
    print("PASS")
    return 0


if __name__ == "__main__":
    sys.exit(main())

