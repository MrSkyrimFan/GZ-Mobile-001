import json
import sys
from pathlib import Path

from perfetto.trace_processor import TraceProcessor

path = Path(sys.argv[1])
records = json.loads((path / "analysis.json").read_text())["phases"]
output = {}
with TraceProcessor(trace=str(path / "r11.pftrace")) as tp:
    for name in ("selected_first", "selected_warm_1", "selected_warm_2", "selected_warm_3"):
        phase = records[name]
        left = phase["bracket_end_ns"] - phase["elapsed_ns"]
        right = phase["bracket_start_ns"] + phase["elapsed_ns"]
        utid = phase["utid"]
        sql = f"""
          WITH ready AS (
            SELECT MAX(ts, {left}) AS b, MIN(ts+dur, {right}) AS e
            FROM thread_state WHERE utid = {utid} AND state IN ('R', 'R+')
              AND dur > 0 AND ts < {right} AND ts + dur > {left}
          ),
          occupancy AS (
            SELECT s.cpu, coalesce(p.name, '(no process)') AS proc,
              coalesce(t.name, '(unnamed)') AS thread_name,
              SUM(MIN(s.ts+s.dur, ready.e)-MAX(s.ts, ready.b)) AS ns
            FROM ready JOIN sched s ON s.ts < ready.e AND s.ts+s.dur > ready.b
            LEFT JOIN thread t USING(utid) LEFT JOIN process p USING(upid)
            WHERE s.dur > 0 GROUP BY 1, 2, 3
          )
          SELECT * FROM occupancy ORDER BY ns DESC
        """
        rows = [
            {"cpu": r.cpu, "process": r.proc, "thread": r.thread_name, "ns": r.ns}
            for r in tp.query(sql)
        ]
        if not rows:
            raise RuntimeError("No guest CPU occupancy for " + name)
        output[name] = {
            "guest_ready_core_ns": sum(x["ns"] for x in phase["core_states"] if x["state"] in ("R", "R+")),
            "per_cpu_ns": {cpu: sum(r["ns"] for r in rows if r["cpu"] == cpu) for cpu in sorted({r["cpu"] for r in rows})},
            "occupancy": rows[:30],
        }
print(json.dumps(output, indent=2))
