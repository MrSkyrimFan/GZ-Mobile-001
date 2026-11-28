import importlib.metadata
import hashlib
import json
import os
import platform
import re
import shlex
import shutil
import subprocess
import sys
import threading
import time
from collections import defaultdict
from pathlib import Path

from perfetto.trace_processor import TraceProcessor

REPO = Path(os.environ["GITHUB_WORKSPACE"])
SOURCE = REPO / "source"
OUT = REPO / "r11-paired-receipt"
OUT.mkdir(exist_ok=True)
SERIAL = "emulator-5554"
CANDIDATE = "27406f4aed8efa42a6078e1108c53304d4a0043e"
METHOD = ("com.greenezone.gzphotoeditor.AndroidRegularProjectListAcceptanceTest"
          "#twelveLegacyRecipesMigrateAndTheReadyIndexDoesNoListDecodes")
HOST_ROOT = Path("/sys/kernel/tracing")
TRACE = HOST_ROOT / "instances" / ("r11paired" + os.environ["GITHUB_RUN_ID"])
EPS = 1000  # host ftrace text precision, in ns
STATUS = {"instrumentation_invocations": 0, "stage": "initialize"}

def invoke(args, *, data=None, timeout=20, check=True):
    return subprocess.run(list(map(str, args)), input=data, text=True, capture_output=True,
                          timeout=timeout, check=check)

def cmd(*args):
    return invoke(args).stdout.strip()

def adb(*args):
    return cmd("adb", "-s", SERIAL, *args)

def root_read(path):
    return invoke(["sudo", "-n", "cat", path]).stdout

def root_write(path, value):
    invoke(["sudo", "-n", "tee", path], data=str(value) + "\n")

def save_json(path, content):
    path.write_text(json.dumps(content, indent=2) + "\n")

def ns(value):
    a, b = value.split(".")
    return int(a) * 1_000_000_000 + int(b.ljust(9, "0"))

EVENT = re.compile(r"^\s*(.*?)\s+\[(\d+)\]\s+\S+\s+(\d+\.\d+):\s+(?:\w+:)?(\w+):\s*(.*)$")
EMITTER = re.compile(r"-(\d+)(?:\s+\([^)]*\))?$")
SWITCH = re.compile(r"\bprev_pid=(\d+).*?\bprev_state=(\S+).*?\bnext_pid=(\d+)")
WAKE = re.compile(r"\bpid=(\d+)")
KVM = re.compile(r"\bvcpu(?:_id)?(?:[=:\s]+)(\d+)\b")

def host_events(path):
    values = []
    for line in path.read_text().splitlines():
        if not line or line.lstrip().startswith("#"):
            continue
        r = EVENT.match(line)
        if r is None:
            raise RuntimeError("Cannot read a runner ftrace line: " + line[:180])
        comm, cpu, ts, kind, payload = r.groups()
        emitter = EMITTER.search(comm)
        if not emitter:
            raise RuntimeError("Missing host event emitter TID: " + comm)
        values.append((ns(ts), int(cpu), int(emitter[1]), kind, payload))
    return sorted(values, key=lambda e: e[0])

def cpu_vector(spec):
    result = set()
    for part in spec.strip().split(","):
        ends = list(map(int, part.split("-")))
        result.update(range(ends[0], ends[-1] + 1))
    return result

def snapshot_host(out, suffix, qemu, tids):
    data = {}
    for name, path in {"cpu": "/proc/stat", "pressure": "/proc/pressure/cpu",
                       "load": "/proc/loadavg", "online": "/sys/devices/system/cpu/online"}.items():
        try:
            data[name] = Path(path).read_text()
        except OSError as err:
            data[name] = str(err)
    for tid in tids:
        for name in ("stat", "status", "sched", "cgroup", "schedstat"):
            try:
                data[f"{tid}/{name}"] = Path(f"/proc/{qemu}/task/{tid}/{name}").read_text()
            except OSError as err:
                data[f"{tid}/{name}"] = str(err)
        for name, args in (("affinity", ["taskset", "-pc", tid]), ("policy", ["chrt", "-p", tid])):
            result = invoke(args, check=False)
            data[f"{tid}/{name}"] = (result.stdout + result.stderr).strip()
    save_json(out / f"host-context-{suffix}.json", data)

def qemu_identity():
    candidates = []
    for path in Path("/proc").iterdir():
        if not path.name.isdecimal():
            continue
        try:
            args = (path / "cmdline").read_bytes().decode(errors="replace").split("\0")[:-1]
            if not args or not re.fullmatch(r"qemu-system-x86_64(?:-headless)?", Path(args[0]).name):
                continue
            if not any(a == "@test" or (a == "-avd" and i + 1 < len(args) and args[i+1] == "test")
                       for i, a in enumerate(args)):
                continue
            candidates.append((int(path.name), args))
        except (OSError, UnicodeError):
            continue
    if len(candidates) != 1:
        raise RuntimeError(f"Expected one qemu-system-x86_64 for AVD test, observed {[r[0] for r in candidates]}")
    return candidates[0]

def identities(qemu, tids):
    return {tid: Path(f"/proc/{qemu}/task/{tid}/stat").read_text().rsplit(")", 1)[1].split()[19]
            for tid in tids}

def guest_apics():
    raw = adb("shell", "cat", "/proc/cpuinfo")
    (OUT / "guest-cpuinfo.txt").write_text(raw + "\n")
    online = cpu_vector(adb("shell", "cat", "/sys/devices/system/cpu/online"))
    pairs = {}
    for block in re.split(r"\n\s*\n", raw):
        fields = dict((m[1].strip(), m[2].strip()) for m in re.finditer(r"^([^:\n]+):\s*([^\n]+)", block, re.M))
        if "processor" not in fields or int(fields["processor"]) not in online:
            continue
        apic = fields.get("initial apicid") or fields.get("apicid")
        if apic is None or ("apicid" in fields and fields["apicid"] != apic):
            raise RuntimeError("Missing or inconsistent guest APIC identity")
        pairs[int(fields["processor"])] = int(apic)
    if set(pairs) != online or len(set(pairs.values())) != len(online):
        raise RuntimeError("Guest CPU/APIC identity incomplete")
    return pairs

EVENT_FIELDS = {
    "sched/sched_switch": ("prev_pid", "next_pid", "prev_state"),
    "sched/sched_wakeup": ("pid",),
    "sched/sched_wakeup_new": ("pid",),
    "kvm/kvm_entry": ("common_pid", "vcpu_id"),
}

def host_start(out, tids, map_kvm):
    invoke(["sudo", "-n", "mkdir", TRACE])
    root_write(TRACE / "tracing_on", 0)
    root_write(TRACE / "trace_clock", "mono")
    root_write(TRACE / "buffer_size_kb", 16384)
    clock = root_read(TRACE / "trace_clock")
    mask = root_read(TRACE / "tracing_cpumask")
    host_online = cpu_vector(Path("/sys/devices/system/cpu/online").read_text())
    if "[mono]" not in clock or not all(int(mask.strip().replace(",", ""), 16) & (1 << cpu) for cpu in host_online):
        raise RuntimeError("Trace clock or runner CPU mask is incomplete")
    settings = {"clock": clock.strip(), "cpumask": mask.strip(), "online": sorted(host_online)}
    for name, fields in EVENT_FIELDS.items():
        if name.startswith("kvm/") and not map_kvm:
            continue
        ev = TRACE / "events" / name
        fmt = root_read(ev / "format")
        if any(not re.search(r"\b" + field + r"\s*;", fmt) for field in fields):
            raise RuntimeError(f"Required tracepoint fields missing: {name} {fields}")
        (out / (name.replace("/", "-") + "-format.txt")).write_text(fmt)
        pids = fields[:2] if name == "sched/sched_switch" else fields[:1]
        filt = " || ".join(f"{field} == {tid}" for field in pids for tid in tids)
        root_write(ev / "filter", filt)
        root_write(ev / "enable", 1)
        if root_read(ev / "enable").strip() != "1":
            raise RuntimeError(f"Tracepoint failed enable readback: {name}")
        settings[name] = root_read(ev / "filter").strip()
    save_json(out / "host-trace-settings.json", settings)
    root_write(TRACE / "trace", "")
    root_write(TRACE / "tracing_on", 1)
    return host_online

def host_stop(out, host_online):
    root_write(TRACE / "tracing_on", 0)
    with (out / "host.ftrace.txt").open("w") as file:
        subprocess.run(["sudo", "-n", "cat", TRACE / "trace"], stdout=file, check=True, timeout=30)
    counters = {cpu: root_read(TRACE / "per_cpu" / f"cpu{cpu}" / "stats") for cpu in sorted(host_online)}
    save_json(out / "host-ftrace-stats.json", counters)
    for cpu, stats in counters.items():
        found = dict(re.findall(r"^\s*(overrun|commit overrun|dropped events)\s*:\s*(\d+)", stats, re.M))
        if len(found) != 3 or any(int(value) for value in found.values()):
            raise RuntimeError(f"Missing/nonzero runner loss counters on CPU {cpu}: {found}")

def host_mark(name):
    left = time.monotonic_ns()
    root_write(TRACE / "trace_marker", name)
    return [name, left, time.monotonic_ns()]

def resolve_vcpus(events, apics, initial_tids):
    by_apic = defaultdict(set)
    for _, _, tid, kind, body in events:
        if kind == "kvm_entry":
            hit = KVM.search(body)
            if not hit or tid not in initial_tids:
                raise RuntimeError("Unmapped/invalid KVM entry: " + body)
            by_apic[int(hit[1])].add(tid)
    if set(by_apic) != set(apics.values()) or any(len(t) != 1 for t in by_apic.values()):
        raise RuntimeError(f"KVM/APIC-to-TID mapping incomplete: {dict(by_apic)} vs {apics}")
    mapping = {cpu: next(iter(by_apic[apic])) for cpu, apic in apics.items()}
    if len(set(mapping.values())) != len(mapping):
        raise RuntimeError("A runner TID maps to multiple guest CPUs")
    return mapping

def host_states(events, cpus, end):
    tids = set(cpus.values())
    start = {}
    rows = defaultdict(list)
    def transition(tid, ts, mark):
        if tid not in tids or ts > end:
            return
        previous = start.get(tid)
        if previous is not None and previous[1] == mark:
            return
        if previous is not None and previous[0] < ts:
            rows[tid].append([previous[0], ts, previous[1]])
        start[tid] = (ts, mark)
    for ts, _, tid, kind, body in events:
        if ts > end:
            break
        if kind == "sched_switch":
            result = SWITCH.search(body)
            if not result:
                raise RuntimeError("Missing runner switch fields: " + body)
            before, state, after = result.groups()
            before, after = int(before), int(after)
            if before in tids and before not in start:
                start[before] = (events[0][0], "running")
            transition(before, ts, "runnable" if state in ("R", "R+") else "blocked:" + state)
            transition(after, ts, "running")
        elif kind in ("sched_wakeup", "sched_wakeup_new"):
            match = WAKE.search(body)
            if not match:
                raise RuntimeError("Missing runner wakeup target: " + body)
            awakened = int(match[1])
            if start.get(awakened, (0, ""))[1] != "running":
                transition(awakened, ts, "runnable")
        elif kind == "kvm_entry" and tid in tids and tid not in start:
            transition(tid, ts, "running")
    for tid, (b, state) in start.items():
        if b < end:
            rows[tid].append([b, end, state])
    return rows


GUEST_CONFIG = '''
duration_ms: 90000
buffers { size_kb: 65536 fill_policy: RING_BUFFER }
data_sources {
  config {
    name: "linux.ftrace"
    ftrace_config {
      ftrace_events: "sched/sched_switch"
      ftrace_events: "sched/sched_waking"
      ftrace_events: "sched/sched_wakeup_new"
      ftrace_events: "sched/sched_blocked_reason"
      ftrace_events: "sched/sched_process_exit"
      ftrace_events: "sched/sched_process_free"
      ftrace_events: "task/task_newtask"
      ftrace_events: "task/task_rename"
      ftrace_events: "ftrace/print"
      atrace_categories: "dalvik"
      disable_generic_events: true
    }
  }
}
data_sources {
  config {
    name: "linux.process_stats"
    process_stats_config { scan_all_processes_on_start: true }
  }
}
data_sources {
  config {
    name: "android.log"
    android_log_config { log_ids: LID_DEFAULT filter_tags: "GzR11Acceptance" }
  }
}
'''

def guest_start(out):
    name = "/data/misc/perfetto-traces/r11-" + out.name + ".pftrace"
    (out / "guest.pbtxt").write_text(GUEST_CONFIG)
    open_trace = invoke(["adb", "-s", SERIAL, "shell", "-T", "perfetto", "--txt", "-c", "-",
                         "-o", name, "--background-wait"], data=GUEST_CONFIG, timeout=20, check=False)
    (out / "guest-trace-start.txt").write_text(open_trace.stdout + open_trace.stderr)
    pid = open_trace.stdout.strip()
    if open_trace.returncode or not pid.isdecimal():
        raise RuntimeError("Guest Perfetto failed to start with a confirmed PID")
    return name, pid

def guest_stop(out, trace):
    name, pid = trace
    invoke(["adb", "-s", SERIAL, "shell", "kill", "-TERM", pid], check=False)
    for _ in range(30):
        r = invoke(["adb", "-s", SERIAL, "shell", "test", "-d", "/proc/" + pid], check=False)
        if r.returncode != 0:
            break
        time.sleep(.25)
    else:
        raise RuntimeError("Guest Perfetto did not exit before pull")
    invoke(["adb", "-s", SERIAL, "pull", name, out / "guest.pftrace"], timeout=30)
    if not (out / "guest.pftrace").stat().st_size:
        raise RuntimeError("Guest trace is empty")

def paired_markers(tag):
    marks = []
    for index in range(5):
        name = f"GZPAIR_{tag}_{index}"
        body = '''set -eu
path=/sys/kernel/tracing/trace_marker
if [ ! -e "$path" ]; then path=/sys/kernel/debug/tracing/trace_marker; fi
printf 'I|%d|%s\\n' "$$" "$1" > "$path"
'''
        left = time.monotonic_ns()
        invoke(["adb", "-s", SERIAL, "shell", "-T", f"su 0 sh -s -- {name}"], data=body, timeout=8)
        marks.append([name, left, time.monotonic_ns()])
    return marks

def query(tp, sql, cols):
    return [{key: getattr(row, key) for key in cols} for row in tp.query(sql)]

def guest_quality(tp, out):
    stats = query(tp, "SELECT name,idx,value,severity FROM stats WHERE value!=0", ("name","idx","value","severity"))
    save_json(out / "guest-trace-stats.json", stats)
    loss = re.compile(r"ftrace.*(?:overrun|dropped|lost)|traced_buf.*(?:overwrit|discard)|packet_loss")
    bad = [r for r in stats if r["value"] > 0 and (r["severity"] in ("data_loss", "error") or loss.search(r["name"]))]
    if bad:
        raise RuntimeError("Guest Perfetto loss/error counters are nonzero: " + str(bad[:8]))
    clocks = query(tp, "SELECT ts,clock_id,clock_value FROM clock_snapshot ORDER BY ts,clock_id",
                   ("ts","clock_id","clock_value"))
    save_json(out / "guest-clock-snapshots.json", clocks)
    if not clocks or not any(r["clock_id"] == 6 for r in clocks):
        raise RuntimeError("Guest clock snapshot lacks BOOTTIME")
    return {"guest_clock_snapshots": len(clocks), "guest_nonzero_stats": len(stats)}

def clock_pair(tp, out, host, hostmarks, before, after):
    hnames = {x[0] for x in hostmarks}
    hfound = defaultdict(list)
    for ts, _, _, kind, msg in host:
        if kind == "tracing_mark_write" and msg in hnames:
            hfound[msg].append(ts)
    for name, low, high in hostmarks:
        got = hfound[name]
        if len(got) != 1 or not low - EPS <= got[0] <= high + EPS:
            raise RuntimeError(f"Host mono clock not bracketed for {name}: {got}")
    guests = query(tp, "SELECT ts,name FROM slice WHERE dur=0 AND name GLOB 'GZPAIR_*' ORDER BY ts", ("ts","name"))
    gfound = defaultdict(list)
    for row in guests:
        gfound[row["name"]].append(row["ts"])
    expected = {x[0] for x in before + after}
    if set(gfound) != expected or any(len(values) != 1 for values in gfound.values()):
        raise RuntimeError(f"Guest marker set differs; expected {len(expected)}, saw {len(gfound)} distinct")
    def group(markers):
        rows = [
            {"marker": name, "guest_ns": gfound[name][0], "host_before_ns": start, "host_after_ns": stop,
             "offset_low_ns": start-gfound[name][0], "offset_high_ns": stop-gfound[name][0]}
            for name, start, stop in markers
        ]
        return rows, [max(r["offset_low_ns"] for r in rows), min(r["offset_high_ns"] for r in rows)]
    pre, prebounds = group(before)
    post, postbounds = group(after)
    compatible = [max(prebounds[0], postbounds[0]), min(prebounds[1], postbounds[1])]
    scope = [min(prebounds[0], postbounds[0]) - EPS, max(prebounds[1], postbounds[1]) + EPS]
    result = {
        "before": pre, "after": post, "before_intersection": prebounds, "after_intersection": postbounds,
        "constant_offset_compatible": compatible, "observed_envelope": scope,
        "host_markers": hfound, "host_marker_brackets": hostmarks,
        "interior_guest_ns": [pre[-1]["guest_ns"], post[0]["guest_ns"]],
        "offset_drift_allowed_between_groups_ns": [postbounds[0]-prebounds[1], postbounds[1]-prebounds[0]],
    }
    save_json(out / "clock-pair.json", result)
    if compatible[0] > compatible[1] or result["interior_guest_ns"][0] >= result["interior_guest_ns"][1]:
        raise RuntimeError("Before/after clock offset intersections or ordered interiors are incompatible")
    return result, hfound[hostmarks[0][0]][0], hfound[hostmarks[-1][0]][0]

def intersection(left, right):
    out = []
    i = j = 0
    while i < len(left) and j < len(right):
        start, stop = max(left[i][0], right[j][0]), min(left[i][1], right[j][1])
        if stop > start:
            out.append([start, stop])
        if left[i][1] <= right[j][1]:
            i += 1
        else:
            j += 1
    return out

def union(values):
    out = []
    for a, b in sorted(values):
        if b <= a:
            continue
        if out and a <= out[-1][1]:
            out[-1][1] = max(out[-1][1], b)
        else:
            out.append([a, b])
    return out

def duration(values):
    return sum(r[1] - r[0] for r in values)

def map_intervals(intervals, clock, possible):
    low, high = clock["observed_envelope"]
    da, db = (low, high) if possible else (high, low)
    return union([[a + da, b + db] for a, b in intervals])

def verify_coverage(tp, mapping, host, clock, host_begin, host_end, guest_first, guest_last):
    lo, hi = clock["observed_envelope"]
    host_window = [[guest_first + lo, guest_last + hi]]
    if host_window[0][0] < host_begin or host_window[0][1] > host_end:
        raise RuntimeError("Relevant mapped guest interval is outside host markers")
    if not map_intervals([[guest_first, guest_last]], clock, False):
        raise RuntimeError("Clock uncertainty leaves an empty informative interval")
    for cpu, tid in mapping.items():
        covered = intersection([[a,b] for a,b,_ in host.get(tid, [])], host_window)
        if duration(covered) != duration(host_window):
            raise RuntimeError(f"Host scheduler coverage incomplete for mapped vCPU {cpu}/TID {tid}")
    guest = query(tp, f"""
      SELECT cpu, SUM(MIN(ts+dur,{guest_last})-MAX(ts,{guest_first})) AS covered
      FROM sched WHERE dur>0 AND ts<{guest_last} AND ts+dur>{guest_first} GROUP BY cpu
    """, ("cpu","covered"))
    if {r["cpu"] for r in guest} != set(mapping) or any(r["covered"] != guest_last-guest_first for r in guest):
        raise RuntimeError(f"Guest scheduled CPU intervals incomplete: {guest}")
    return {"guest_ns": guest_last-guest_first, "host_covered_ns_each": duration(host_window), "guest_cpu": guest}


def phase_evidence(tp, folder, clock, states, mapping):
    logs = query(tp, """
      SELECT l.ts,p.pid,t.tid,t.utid,l.msg FROM android_logs l
       LEFT JOIN thread t USING(utid) LEFT JOIN process p USING(upid)
       WHERE l.tag='GzR11Acceptance' ORDER BY l.ts
    """, ("ts","pid","tid","utid","msg"))
    save_json(folder / "phase-logs.json", logs)
    fixture = [r for r in logs if r["msg"].startswith("fixture=matched_twelve")]
    if len(fixture) != 1 or not re.search(r"\brecipes=12 javaMaxBytes=201326592\b", fixture[0]["msg"]):
        raise RuntimeError("Test source did not verify the exact dense fixture and 192 MiB heap")
    phases = ("initial_migration","existing_index_first","existing_index_warm_1","existing_index_warm_2",
              "existing_index_warm_3","selected_first","selected_warm_1","selected_warm_2","selected_warm_3")
    grouped = {}
    for phase in phases:
        lead = "phase=" + phase + " "
        start = [r for r in logs if r["msg"] == lead + "measurement=start"]
        done = [r for r in logs if r["msg"].startswith(lead + "elapsedNs=")]
        if len(start) != 1 or len(done) != 1 or start[0]["utid"] != done[0]["utid"] or not start[0]["tid"]:
            raise RuntimeError("Could not bind exact phase markers to one guest test TID: " + phase)
        left, right = start[0]["ts"], done[0]["ts"]
        match = re.search(r"elapsedNs=(\d+)", done[0]["msg"])
        wall = int(match[1])
        if right-left < wall or left < clock["interior_guest_ns"][0] or right > clock["interior_guest_ns"][1]:
            raise RuntimeError("Phase outside its valid stopwatch/clock bracket: " + phase)
        core_a, core_b = right-wall, left+wall
        utid = done[0]["utid"]
        windows = query(tp, f"""
          SELECT MAX(ts,{left}) AS b,MIN(ts+dur,{right}) AS e,state,io_wait,blocked_function
           FROM thread_state WHERE utid={utid} AND dur>0 AND ts<{right} AND ts+dur>{left} ORDER BY ts
        """, ("b","e","state","io_wait","blocked_function"))
        if duration([[r["b"],r["e"]] for r in windows]) != right-left:
            raise RuntimeError("Guest selected test thread has uncovered time: " + phase)
        bounds = defaultdict(lambda: [0,0])
        for r in windows:
            bounds[r["state"]][0] += max(0, min(r["e"],core_b)-max(r["b"],core_a))
            bounds[r["state"]][1] += r["e"]-r["b"]
        ready = [[r["b"],r["e"]] for r in windows if r["state"] in ("R","R+")]
        ready_core = intersection(ready, [[core_a,core_b]]) if core_b>core_a else []
        ready_pair = [map_intervals(ready_core,clock,False), map_intervals(ready,clock,True)]
        phase_item = {"wall_ns":wall,"pid":done[0]["pid"],"tid":done[0]["tid"],"utid":utid,
                      "slack_ns":right-left-wall,"states_core_bracket_ns":bounds,
                      "measured":done[0]["msg"],"guest_bracket_ns":[left,right],"guest_core_ns":[core_a,core_b]}
        if phase.startswith("selected"):
            run_guest = query(tp, f"""
              SELECT cpu,MAX(ts,{left}) AS b,MIN(ts+dur,{right}) AS e FROM sched
               WHERE utid={utid} AND dur>0 AND ts<{right} AND ts+dur>{left} ORDER BY ts
            """, ("cpu","b","e"))
            if not set(r["cpu"] for r in run_guest).issubset(mapping):
                raise RuntimeError("Unmapped scheduled guest CPU")
            paired = {}
            all_unavailable = None
            for cpu, tid in sorted(mapping.items()):
                h = states[tid]
                host_running = [[a,b] for a,b,state in h if state == "running"]
                host_ready = [[a,b] for a,b,state in h if state == "runnable"]
                host_blocked = [[a,b] for a,b,state in h if state.startswith("blocked")]
                off = union(host_ready+host_blocked)
                all_unavailable = off if all_unavailable is None else intersection(all_unavailable,off)
                guest_here = [[r["b"],r["e"]] for r in run_guest if r["cpu"] == cpu]
                guest_core = intersection(guest_here, [[core_a,core_b]]) if core_b>core_a else []
                mapped_core = map_intervals(guest_core,clock,False)
                mapped_bracket = map_intervals(guest_here,clock,True)
                paired[cpu] = {name:[duration(intersection(mapped_core,d)), duration(intersection(mapped_bracket,d))]
                               for name,d in (("host_running",host_running),("host_runnable",host_ready),
                                              ("host_blocked",host_blocked))}
            phase_item["scheduled_guest_per_vcpu_overlap_ns"] = paired
            phase_item["guest_ready_overlap_all_vcpus_off_ns"] = [
                duration(intersection(v,all_unavailable)) for v in ready_pair
            ]
            phase_item["overlap_model"] = "Conditional on the observed clock envelope; markers cannot prove arbitrary interior excursions"
            phase_item["art_slices"] = query(tp, f"""
              SELECT s.name,t.tid,MAX(s.ts,{left})-{left} AS relative_ns,
               MIN(s.ts+s.dur,{right})-MAX(s.ts,{left}) AS overlap_ns
              FROM slice s JOIN thread_track tt ON s.track_id=tt.id JOIN thread t USING(utid)
              JOIN process p USING(upid)
              WHERE p.pid={int(done[0]['pid'])} AND s.dur>0 AND s.ts<{right} AND s.ts+s.dur>{left}
                AND (lower(s.name) LIKE '%gc%' OR lower(s.name) LIKE '%suspend%'
                     OR lower(s.name) LIKE '%alloc%' OR lower(s.name) LIKE '%monitor%')
              ORDER BY overlap_ns DESC LIMIT 30
            """, ("name","tid","relative_ns","overlap_ns"))
            phase_item["guest_runnable_competitors"] = query(tp, f"""
              WITH ready AS (
                SELECT MAX(ts,{core_a}) AS b,MIN(ts+dur,{core_b}) AS e
                 FROM thread_state WHERE utid={utid} AND state IN('R','R+')
                  AND dur>0 AND ts<{core_b} AND ts+dur>{core_a} AND {core_b}>{core_a}
              )
              SELECT s.cpu,coalesce(p.name,'(no process)') AS proc,t.name AS thread,
                SUM(MIN(s.ts+s.dur,ready.e)-MAX(s.ts,ready.b)) AS ns
                 FROM ready JOIN sched s ON s.ts<ready.e AND s.ts+s.dur>ready.b
                 LEFT JOIN thread t USING(utid) LEFT JOIN process p USING(upid)
                 WHERE s.dur>0 GROUP BY 1,2,3 ORDER BY ns DESC LIMIT 24
            """, ("cpu","proc","thread","ns"))
        grouped[phase] = phase_item
    save_json(folder / "paired-phases.json", grouped)
    return grouped

def collect_test_thread(folder):
    pat = re.compile(r"\s(\d+)\s+(\d+)\s+[A-Z]\s+GzR11Acceptance\s*:\s*fixture=matched_twelve")
    monitor = subprocess.Popen(["adb","-s",SERIAL,"logcat","-b","main","-T","1","-v","threadtime",
                                "-s","GzR11Acceptance:I","*:S"], stdout=subprocess.PIPE,
                               stderr=subprocess.DEVNULL, text=True)
    def read_one():
        for line in monitor.stdout:
            match = pat.search(line)
            if not match:
                continue
            data = {"observed": line.strip(), "host_monotonic_ns": time.monotonic_ns()}
            p, t = match.groups()
            for name in ("stat","status","sched","cgroup"):
                r = invoke(["adb","-s",SERIAL,"shell","cat",f"/proc/{p}/task/{t}/{name}"],
                           check=False,timeout=5)
                data[name] = r.stdout+r.stderr
            save_json(folder / "guest-test-thread.json", data)
            break
    thread = threading.Thread(target=read_one,daemon=True)
    thread.start()
    return monitor, thread

def part(name, qemu, proc, apics, known=None):
    folder = OUT / name
    folder.mkdir()
    initial = sorted(known.values()) if known else sorted(int(p.name) for p in Path(f"/proc/{qemu}/task").iterdir())
    identity_before = {}
    for tid in initial:
        try:
            identity_before.update(identities(qemu, [tid]))
        except FileNotFoundError:
            if known:
                raise
    initial = sorted(identity_before)
    snapshot_host(folder, "before", qemu, initial if known else [qemu])
    (folder / "guest-uptime-before.txt").write_text(adb("shell","cat","/proc/uptime")+"\n")
    (folder / "guest-cpu-before.txt").write_text(adb("shell","cat","/proc/stat")+"\n")
    cpus = host_start(folder, initial, True)
    hm = [host_mark("GZHOST_" + name + "_begin")]
    guest = guest_start(folder)
    before = after = None
    instrument = None
    try:
        before = paired_markers(name + "_before")
        if known is None:
            adb("shell","log","-t","GzR11Acceptance", "phase=paired_preflight_probe")
            adb("shell","cat","/proc/stat","/proc/uptime")
        else:
            root_write(TRACE / "events" / "kvm/kvm_entry" / "enable", 0)
            if root_read(TRACE / "events" / "kvm/kvm_entry" / "enable").strip() != "0":
                raise RuntimeError("KVM preflight probe did not stop before instrumentation")
            monitor, observer = collect_test_thread(folder)
            try:
                STATUS["instrumentation_invocations"] = 1
                STATUS["stage"] = "instrumentation"
                save_json(OUT / "disposition.json", STATUS)
                command = ["adb","-s",SERIAL,"shell","am","instrument","-w","-r","-e","class",
                           METHOD,"com.greenezone.gzphotoeditor.phonetest.v27.test/androidx.test.runner.AndroidJUnitRunner"]
                (folder / "instrumentation-command.txt").write_text(shlex.join(command)+"\n")
                with (folder / "instrumentation.txt").open("w") as log:
                    result = subprocess.run(command, stdout=log, stderr=subprocess.STDOUT, text=True, timeout=70)
                instrument = result.returncode
            finally:
                monitor.terminate()
                monitor.wait(timeout=5)
                observer.join(timeout=1)
                root_write(TRACE / "events" / "kvm/kvm_entry" / "enable", 1)
        after = paired_markers(name + "_after")
    finally:
        hm.append(host_mark("GZHOST_" + name + "_end"))
        save_json(folder / "marker-input.json", {"host":hm,"before":before,"after":after})
        try:
            host_stop(folder,cpus)
        finally:
            try:
                guest_stop(folder,guest)
            finally:
                invoke(["sudo","-n","rmdir",TRACE],check=False)
    adb_logs = invoke(["adb","-s",SERIAL,"logcat","-b","main","-d","-v","threadtime","-v","epoch","-v","usec"])
    useful = [l for l in adb_logs.stdout.splitlines() if re.search(r"GzR11Acceptance|TestRunner|r\.phonetest\.v27:",l)]
    (folder / "guest-logcat.txt").write_text("\n".join(useful)+"\n")
    (folder / "guest-uptime-after.txt").write_text(adb("shell","cat","/proc/uptime")+"\n")
    host = host_events(folder / "host.ftrace.txt")
    mapping = resolve_vcpus(host,apics,set(initial))
    if (known is not None and mapping != known) or any(
        identity_before[tid]!=v for tid,v in identities(qemu,mapping.values()).items()
    ):
        raise RuntimeError("Host vCPU mapping or TID lifetime changed")
    save_json(folder / "vcpu-mapping.json", {"qemu_pid":qemu,"guest_cpu_apic":apics,
                                            "guest_cpu_host_tid":mapping,"host_tid_start":identities(qemu,mapping.values())})
    snapshot_host(folder, "after", qemu, mapping.values())
    with TraceProcessor(trace=str(folder/"guest.pftrace")) as tp:
        quality=guest_quality(tp,folder)
        clock,start,end=clock_pair(tp,folder,host,hm,before,after)
        state=host_states(host,mapping,end)
        save_json(folder/"host-vcpu-states.json",state)
        coverage=verify_coverage(tp,mapping,state,clock,start,end,*clock["interior_guest_ns"])
        result={"mapping":mapping,"clock":clock,"quality":quality,"coverage":coverage}
        if known is None:
            seen = query(tp,"SELECT COUNT(*) AS n FROM android_logs WHERE tag='GzR11Acceptance' AND msg='phase=paired_preflight_probe'",("n",))
            if seen != [{"n":1}]:
                raise RuntimeError("Guest preflight did not recover the phase-log probe")
        else:
            result["instrumentation_adb_status"]=instrument
            raw=(folder/"instrumentation.txt").read_text()
            if raw.count("INSTRUMENTATION_STATUS: numtests=1") < 1:
                raise RuntimeError("Instrumentation did not report the one expected test")
            result["test_pass"]="OK (1 test)" in raw and instrument==0
            result["phases"]=phase_evidence(tp,folder,clock,state,mapping)
    save_json(folder/"summary.json",result)
    print(json.dumps({"part":name,"mapping":mapping,"clock":clock["observed_envelope"],"coverage":coverage}))
    return result

def main():
    STATUS["stage"]="identity"
    actual = cmd("git","-C",SOURCE,"rev-parse","HEAD")
    if actual!=CANDIDATE or adb("shell","getprop","ro.build.version.sdk")!="35" or adb("shell","getprop","ro.kernel.qemu")!="1":
        raise RuntimeError("Candidate or emulator does not match held API35 inputs")
    if adb("shell","getprop","dalvik.vm.heapgrowthlimit")!="192m" or adb("shell","getprop","ro.product.cpu.abi")!="x86_64":
        raise RuntimeError("The actual emulator does not expose the named 192 MiB x86_64 tier")
    if adb("shell","getprop","ro.build.type")!="userdebug" or "test" not in adb("emu","avd","name").splitlines():
        raise RuntimeError("Guest trace producer tier/AVD identity mismatch")
    qemu, proc = qemu_identity()
    apics = guest_apics()
    (OUT/"qemu-kvm-fds.txt").write_text(cmd("sudo","-n","ls","-l",f"/proc/{qemu}/fd")+"\n")
    if not re.search(r"(?:/dev/kvm|kvm-vm|kvm-vcpu)",(OUT/"qemu-kvm-fds.txt").read_text()):
        raise RuntimeError("Runner emulator has no identified KVM device or VM descriptor")
    apk = SOURCE/"app/build/outputs/apk/debug/app-debug.apk"
    test_apk = SOURCE/"app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
    aapt = Path(os.environ["ANDROID_HOME"])/"build-tools/36.0.0/aapt"
    expected = ("com.greenezone.gzphotoeditor.phonetest.v27","com.greenezone.gzphotoeditor.phonetest.v27.test")
    binary_ids=[]
    for path, id in zip((apk,test_apk), expected):
        package=re.search(r"^package: name='([^']+)'",cmd(aapt,"dump","badging",path),re.M)
        if not package or package[1]!=id:
            raise RuntimeError("Built trace package does not match held v27 test install")
        binary_ids.append({"name":path.name,"package":id,"bytes":path.stat().st_size,
                           "sha256":hashlib.sha256(path.read_bytes()).hexdigest()})
    if invoke(["sudo","-n","test","-d",HOST_ROOT/"instances"],check=False).returncode != 0:
        invoke(["sudo","-n","mount","-t","tracefs","tracefs",HOST_ROOT])
    save_json(OUT/"configuration.json",{"candidate":actual,"candidate_tree":cmd("git","-C",SOURCE,"rev-parse","HEAD^{tree}"),
      "diagnostic_head":os.environ["GITHUB_SHA"],"host_image":os.getenv("ImageOS"),"host_image_version":os.getenv("ImageVersion"),
      "host_kernel":platform.release(),"host_cpu":cmd("lscpu"),"host_kvm":cmd("ls","-l","/dev/kvm"),
      "qemu_pid":qemu,"emulator_command":proc,"guest_fingerprint":adb("shell","getprop","ro.build.fingerprint"),
      "guest_heapgrowthlimit":"192m","guest_perfetto":adb("shell","perfetto","--version"),
      "guest_perfetto_sha256":adb("shell","sha256sum","/system/bin/perfetto"),"trace_processor_python":importlib.metadata.version("perfetto"),
      "python":sys.version,"binaries":binary_ids,
      "tool_sha256":{p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in (Path(__file__),Path(shutil.which("adb")))}})
    STATUS["stage"]="no-app preflight"
    begun=time.monotonic_ns()
    before=part("preflight",qemu,proc,apics)
    STATUS.update({"preflight":"passed","preflight_ns":time.monotonic_ns()-begun,"stage":"install exact fixture APKs"})
    save_json(OUT/"disposition.json",STATUS)
    print("No-app paired preflight passed. One unchanged instrumentation invocation is authorized.",flush=True)
    adb("install","-r",apk)
    adb("install","-r",test_apk)
    if adb("shell","pm","clear",expected[0])!="Success":
        raise RuntimeError("Dedicated diagnostic package could not be cleared")
    if qemu_identity()[0]!=qemu or guest_apics()!=apics:
        raise RuntimeError("Emulator identity changed after preflight")
    STATUS["stage"]="paired capture setup"
    after=part("paired",qemu,proc,apics,before["mapping"])
    STATUS.update({"postflight":"passed","stage":"complete","test_pass":after["test_pass"]})
    if not after["test_pass"]:
        raise RuntimeError("The unchanged R1.1 method reported a failure; paired raw evidence was retained")

if __name__=="__main__":
    try:
        main()
    except BaseException as error:
        STATUS["error"]=str(error)
        if isinstance(error,subprocess.CalledProcessError):
            STATUS["stderr"]=(error.stderr or "")[-4000:]
            STATUS["stdout"]=(error.stdout or "")[-2000:]
        print("R1.1 paired diagnostic disposition:",json.dumps(STATUS),flush=True)
        raise
    finally:
        save_json(OUT/"disposition.json",STATUS)
        if invoke(["sudo","-n","test","-d",TRACE],check=False).returncode==0:
            invoke(["sudo","-n","tee",TRACE/"tracing_on"],data="0\n",check=False)
            invoke(["sudo","-n","rmdir",TRACE],check=False)
