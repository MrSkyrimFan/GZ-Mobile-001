import json
import re
import sys
from collections import defaultdict
from pathlib import Path

from perfetto.trace_processor import TraceProcessor

directory = Path(sys.argv[1])
logcat = (directory / "r11-logcat.txt").read_text()
table = re.compile(
    r"^(\d+)\.(\d+)\s+(\d+)\s+(\d+)\s+\w\s+GzR11Acceptance:\s+(.*)$",
    re.MULTILINE,
)
epoch_logs = [
    (int(sec) * 1_000_000_000 + int(frac.ljust(9, "0")), int(pid), int(tid), msg)
    for sec, frac, pid, tid, msg in table.findall(logcat)
]
with TraceProcessor(trace=str(directory / "r11.pftrace")) as tp:
    def query(sql, columns):
        return [{key: getattr(row, key) for key in columns} for row in tp.query(sql)]

    health = query(
        "SELECT name, idx, value, severity FROM stats WHERE value != 0",
        ("name", "idx", "value", "severity"),
    )
    clock = query(
        "SELECT ts, clock_value FROM clock_snapshot WHERE clock_id = 1 ORDER BY ts",
        ("ts", "clock_value"),
    )
    traced_logs = query("""
        SELECT l.ts, p.pid, t.tid, l.msg
        FROM android_logs l LEFT JOIN thread t USING (utid)
        LEFT JOIN process p USING (upid) WHERE l.tag = 'GzR11Acceptance' ORDER BY l.ts
    """, ("ts", "pid", "tid", "msg"))
    logs = [(r["ts"], r["pid"], r["tid"], r["msg"]) for r in traced_logs if r["pid"] and r["tid"]]
    method = "android_logs"
    if len(logs) < 20:
        method = "epoch logcat with Perfetto CLOCK_REALTIME snapshots"
        if not clock:
            raise RuntimeError("Neither trace log markers nor realtime clock snapshots were usable")
        logs = []
        for time, pid, tid, msg in epoch_logs:
            snap = min(clock, key=lambda r: abs(time - r["clock_value"]))
            logs.append((time + snap["ts"] - snap["clock_value"], pid, tid, msg))

    starts = {}
    results = {}
    pattern = re.compile(r"^phase=(\S+) (measurement=start|elapsedNs=(\d+))")
    for ts, pid, tid, msg in logs:
        match = pattern.match(msg)
        if not match:
            continue
        phase, _, elapsed = match.groups()
        if not elapsed:
            starts[phase] = (ts, pid, tid)
            continue
        if phase.endswith("_median"):
            continue
        left, left_pid, left_tid = starts.pop(phase)
        elapsed = int(elapsed)
        if (left_pid, left_tid) != (pid, tid) or ts - left < elapsed:
            raise RuntimeError(f"Incompatible trace clock or thread for {phase}")
        ids = query(f"SELECT t.utid FROM thread t JOIN process p USING(upid) WHERE t.tid = {tid} AND p.pid = {pid}", ("utid",))
        if len(ids) != 1:
            raise RuntimeError(f"Unresolved scheduler TID/PID {tid}/{pid} for {phase}")
        utid = ids[0]["utid"]

        def states(begin, end):
            if end <= begin:
                return []
            return query(f"""
                SELECT state, io_wait, blocked_function,
                  SUM(MIN(ts + dur, {end}) - MAX(ts, {begin})) AS ns
                FROM thread_state WHERE utid = {utid} AND dur > 0
                    AND ts < {end} AND ts + dur > {begin}
                GROUP BY state, io_wait, blocked_function ORDER BY ns DESC
            """, ("state", "io_wait", "blocked_function", "ns"))

        outer = states(left, ts)
        inner = states(ts - elapsed, left + elapsed)
        coverage = sum(row["ns"] for row in outer)
        art = query(f"""
            SELECT MAX(s.ts, {left}) - {left} AS within_ns,
              MIN(s.ts + s.dur, {ts}) - MAX(s.ts, {left}) AS overlap_ns,
              s.name, t.tid, t.name AS thread
            FROM slice s JOIN thread_track tt ON s.track_id = tt.id
              JOIN thread t USING(utid) JOIN process p USING(upid)
            WHERE p.pid = {pid} AND s.dur > 0 AND s.ts < {ts} AND s.ts+s.dur > {left}
              AND (lower(s.name) LIKE '%gc%' OR lower(s.name) LIKE '%suspend%'
                   OR lower(s.name) LIKE '%alloc%' OR lower(s.name) LIKE '%monitor%')
            ORDER BY overlap_ns DESC LIMIT 25
        """, ("within_ns", "overlap_ns", "name", "tid", "thread"))
        results[phase] = {
            "pid": pid, "tid": tid, "utid": utid, "elapsed_ns": elapsed,
            "bracket_start_ns": left, "bracket_end_ns": ts, "bracket_slack_ns": ts - left - elapsed,
            "outer_coverage_ns": coverage, "outer_states": outer, "core_states": inner, "art": art,
        }

    expected = [
        "initial_migration", "existing_index_first",
        *[f"existing_index_warm_{n}" for n in range(1, 4)],
        "selected_first", *[f"selected_warm_{n}" for n in range(1, 4)],
    ]
    out = {
        "clock_source": method,
        "realtime_offset_span_ns": (
            max(r["ts"] - r["clock_value"] for r in clock) -
            min(r["ts"] - r["clock_value"] for r in clock)
        ) if clock else None,
        "logs": [list(row) for row in logs],
        "nonzero_stats": health,
        "phases": results,
        "missing_phases": [name for name in expected if name not in results],
    }
    (directory / "analysis.json").write_text(json.dumps(out, indent=2) + "\n")
    if out["missing_phases"] or any(not r["outer_states"] for r in results.values()):
        raise RuntimeError(f"Incomplete scheduler phase capture: {out['missing_phases']}")
    print("Captured all nine R1.1 phase brackets and scheduler intervals.")
