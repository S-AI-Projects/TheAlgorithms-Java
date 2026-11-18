#!/usr/bin/env python3
"""Focused verification runner for R45-T031 (HopcroftKarp contract regression tests)."""

from __future__ import annotations

import os
import pathlib
import re
import subprocess
import sys

REPO_ROOT = pathlib.Path(__file__).resolve().parents[2]
WORK_DIR = pathlib.Path(__file__).resolve().parent
TEST_CLASS = "com.thealgorithms.graph.HopcroftKarpTest"


def build_env() -> dict[str, str]:
    env = os.environ.copy()
    jdk21 = pathlib.Path("/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home")
    if "JAVA_HOME" not in env and jdk21.exists():
        env["JAVA_HOME"] = str(jdk21)
        env["PATH"] = f"{jdk21 / 'bin'}:{env.get('PATH', '')}"
    return env


def capture_version(cmd: list[str], env: dict[str, str]) -> str:
    proc = subprocess.run(cmd, capture_output=True, text=True, env=env)
    combined = (proc.stdout + "\n" + proc.stderr).strip()
    return combined.splitlines()[0] if combined else f"exit={proc.returncode}"


def main() -> int:
    env = build_env()
    WORK_DIR.mkdir(parents=True, exist_ok=True)

    tool_versions = [
        f"python3: {sys.version.splitlines()[0]}",
        f"java: {capture_version(['java', '-version'], env)}",
        f"mvn: {capture_version(['mvn', '-version'], env)}",
    ]
    (WORK_DIR / "tool_versions.txt").write_text("\n".join(tool_versions) + "\n", encoding="utf-8")

    cmd = [
        "mvn",
        "-B",
        "test",
        f"-Dtest={TEST_CLASS}",
        "-Dsurefire.failIfNoSpecifiedTests=false",
    ]
    proc = subprocess.run(cmd, cwd=REPO_ROOT, env=env, capture_output=True, text=True)
    full_output = proc.stdout + ("\n" + proc.stderr if proc.stderr else "")
    (WORK_DIR / "mvn_output.txt").write_text(full_output, encoding="utf-8")

    summary_match = re.findall(
        r"Tests run:\s*(\d+),\s*Failures:\s*(\d+),\s*Errors:\s*(\d+),\s*Skipped:\s*(\d+)",
        full_output,
    )
    if summary_match:
        tests_run, failures, errors, skipped = map(int, summary_match[-1])
    else:
        tests_run, failures, errors, skipped = 0, 0, 1, 0

    case_lines = [line.strip() for line in full_output.splitlines() if line.strip().startswith("CASE|")]
    (WORK_DIR / "comparisons.txt").write_text(
        "\n".join(
            [
                "# R45-T031 HopcroftKarp Contract Regression Comparisons",
                "# Source: src/main/java/com/thealgorithms/graph/HopcroftKarp.java",
                "# Preconditions: lines 33-34 (nLeft>=0, nRight>=0), lines 37-38 (initial -1), lines 69-70 (adj bounds), lines 96-102 (defensive clones)",
                *case_lines,
            ]
        )
        + "\n",
        encoding="utf-8",
    )

    passed = proc.returncode == 0 and tests_run > 0 and failures == 0 and errors == 0 and len(case_lines) >= 4
    status = "PASS" if passed else "FAIL"
    summary = (
        f"task=R45-T031\n"
        f"status={status}\n"
        f"command={' '.join(cmd)}\n"
        f"mvn_exit={proc.returncode}\n"
        f"tests_run={tests_run}\n"
        f"failures={failures}\n"
        f"errors={errors}\n"
        f"skipped={skipped}\n"
        f"comparison_records={len(case_lines)}\n"
    )
    (WORK_DIR / "result.txt").write_text(summary, encoding="utf-8")
    print(summary, end="")
    return 0 if passed else 1


if __name__ == "__main__":
    raise SystemExit(main())
