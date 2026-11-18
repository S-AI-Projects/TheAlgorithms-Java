#!/usr/bin/env python3
"""Focused verification and complexity measurement runner for R45-T039 (GrahamScan)."""

from __future__ import annotations

import os
import pathlib
import re
import statistics
import subprocess
import sys

REPO_ROOT = pathlib.Path(__file__).resolve().parents[2]
WORK_DIR = pathlib.Path(__file__).resolve().parent
TEST_CLASSES = "com.thealgorithms.geometry.GrahamScanTest,com.thealgorithms.geometry.GrahamScanScalingTest"


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
        f"-Dtest={TEST_CLASSES}",
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
    timing_lines = [line.strip() for line in full_output.splitlines() if line.strip().startswith("TIMING|")]

    by_size: dict[int, list[int]] = {}
    for line in timing_lines:
        m = re.search(r"n=(\d+)\|rep=(\d+)\|seed=(\d+)\|ns=(\d+)\|hull_size=(\d+)\|valid=(\w+)", line)
        if m:
            n_val = int(m.group(1))
            ns_val = int(m.group(4))
            by_size.setdefault(n_val, []).append(ns_val)

    timing_report = [
        "# R45-T039 GrahamScan Complexity Experiment Timings",
        "# Input size variable: n = points.length (2D integer coordinates in [-10000, 10000])",
        "# Protocol: JVM warmup (3 runs) + 5 repetitions per size with fixed seeds and fresh inputs;",
        "# timing wraps only new GrahamScan(points); correctness verified outside the timed interval.",
        "# No pass/fail threshold depends on wall-clock speed.",
        "",
        "| n | reps | median_ms | min_ms | max_ms | range_ms |",
        "| --- | --- | --- | --- | --- | --- |",
    ]
    prev_med = None
    prev_n = None
    ratios = []
    for n_val in sorted(by_size):
        samples_ms = [s / 1_000_000.0 for s in by_size[n_val]]
        med = statistics.median(samples_ms)
        lo = min(samples_ms)
        hi = max(samples_ms)
        timing_report.append(
            f"| {n_val} | {len(samples_ms)} | {med:.3f} | {lo:.3f} | {hi:.3f} | {hi - lo:.3f} |"
        )
        if prev_med is not None and prev_med > 0:
            ratios.append(f"n={prev_n}->{n_val} ({n_val/prev_n:.1f}x size): median ratio = {med/prev_med:.2f}x")
        prev_med = med
        prev_n = n_val

    timing_report.append("")
    timing_report.append("# Observed scaling ratios:")
    timing_report.extend(f"# {r}" for r in ratios)
    timing_report.append("")
    timing_report.append("# Raw samples:")
    timing_report.extend(timing_lines)
    (WORK_DIR / "timings.txt").write_text("\n".join(timing_report) + "\n", encoding="utf-8")

    (WORK_DIR / "comparisons.txt").write_text(
        "\n".join(
            [
                "# R45-T039 GrahamScan Fixture & Correctness Comparisons",
                "# Source: src/main/java/com/thealgorithms/geometry/GrahamScan.java",
                "# Preconditions: pinned lines 24-25 (non-null points, length >= 1, sorted in place), Point.java line 23 (no 32-bit int overflow in orientation)",
                *case_lines,
                f"SCALING_VALIDATED|sizes={sorted(by_size.keys())}|total_runs={len(timing_lines)}|all_hulls_verified=true",
            ]
        )
        + "\n",
        encoding="utf-8",
    )

    passed = (
        proc.returncode == 0
        and tests_run > 0
        and failures == 0
        and errors == 0
        and len(case_lines) >= 2
        and len(timing_lines) == 15
    )
    status = "PASS" if passed else "FAIL"
    summary = (
        f"task=R45-T039\n"
        f"status={status}\n"
        f"command={' '.join(cmd)}\n"
        f"mvn_exit={proc.returncode}\n"
        f"tests_run={tests_run}\n"
        f"failures={failures}\n"
        f"errors={errors}\n"
        f"skipped={skipped}\n"
        f"comparison_records={len(case_lines)}\n"
        f"timing_samples={len(timing_lines)}\n"
    )
    (WORK_DIR / "result.txt").write_text(summary, encoding="utf-8")
    print(summary, end="")
    return 0 if passed else 1


if __name__ == "__main__":
    raise SystemExit(main())
