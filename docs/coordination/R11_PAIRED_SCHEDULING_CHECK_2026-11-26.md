# R1.1 trace review and final paired scheduling check

November 26, 2026 (America/Denver). This records the independent review of PR #53 and the next bounded action. The [work ledger](../WORK_LEDGER.md#executed-api35-diagnostic-and-qualification-hold) remains the single active dispatch. This is not a new roadmap.

## Verdict and exact baseline

**The diagnostic and its conservative scheduling bounds are valid. No correction to the PR #53 numerical receipt is required. PR #50 remains draft and the corrected handset APK remains held.** The current evidence supports investigating execution conditions before changing application decoding/storage.

- Intake main: [PR #53](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/53), **d684a4b65ec3310a0ee33295034c77712eaea640**, tree **a8e4976f0bac8160c43420f10e823f21251a149e**.
- Held application/test candidate: **27406f4aed8efa42a6078e1108c53304d4a0043e**, tree **2c0f8ffab9a8321f8f9f823c9354c26b5a2fb6c8**.
- Production correction merge: **b342f0bd656753b3692b741489438c6790d14d8b**. The previously reviewed U1R-01/U1R-02/U1R-V1 corrections remain credited.
- Last fully qualified application: PR #46, **e6321322526521ace13a45c231b25bb08f91c565**.
- Diagnostic harness: **77af73ece1e21e22d671fb18eaad6b5bb80d0f90**, [run 45926598413](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45926598413).
- Saved-trace analysis: **7f4ac905ffdfd8d2f0d604ac27bba8eb06fcd7f9**, [run 45928121708](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45928121708).

The original **717.745435 ms** failure remains recorded with an unresolved cause. **Retroactively proving its precise cause is not a permanent exit requirement.** A prospectively supported correction or environment disposition, followed by the unchanged exact-candidate/exact-merge qualification gates, can resolve this delivery hold. A later isolated pass alone cannot.

This review inspected source, existing artifacts and official tracing documentation. It executed no application build, emulator or Android test. Three independent scopes checked raw trace math, diagnostic custody/execution, and the next bounded action.

## What was independently verified

The diagnostic workflow checks out the exact held candidate into a separate directory and verifies its SHA before building ([workflow 13–35](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/77af73ece1e21e22d671fb18eaad6b5bb80d0f90/.github/workflows/android-ci.yml#L13-L35)). Its delta from that candidate is exactly three diagnostic/workflow paths. The application, complete R1.1 method, fixture, selection order, stopwatch, GC setup and assertions were unchanged.

The actual guest/test reports **201,326,592 bytes = 192 MiB** maximum managed heap. The workflow's heap-size input does not override that measured contract. The [capture script 75–90](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/77af73ece1e21e22d671fb18eaad6b5bb80d0f90/.github/scripts/r11-api35-trace.sh#L75-L90) invokes instrumentation once and requires OK (1 test), so an adb exit of zero cannot hide the assertion failure. The saved-trace workflow runs analysis only, not another emulator or app test.

| Selected Open | Recorded wall time | Conservative guest runnable/off-CPU lower bound | Guest scheduled-running range |
| --- | ---: | ---: | ---: |
| First | **920.414908 ms** | **687.811796 ms** | 211.501891–215.065602 ms |
| Warm 1 | **857.696254 ms** | **540.598466 ms** | 231.988967–234.162553 ms |
| Warm 2 | 373.394719 ms | 159.512999 ms | 201.711942–201.973650 ms |
| Warm 3 | 532.514765 ms | 270.668666 ms | 194.027242–194.342328 ms |

The first latency assertion fails against **650 ms**. All four measurements were collected, but later selected-content/heap/latency assertions were not reached. The arithmetic warm median is **532.514765 ms**, above **300 ms**; the test never reached that median assertion. Recorded values are not promoted into executed assertion passes.

The supplied interval method is sound. For start-log time L, result-log time R and actual stopwatch duration W, [L,R] contains the stopwatch. The guaranteed inner interval is [R−W,L+W] when nonempty. Summing scheduling states inside that interval gives conservative lower bounds; the full bracket gives upper bounds. All nine phase state sums match their respective bracket/core durations. The short warm-list sample whose slack exceeds W correctly has an empty core; zero lower bounds do not mean zero execution.

Beyond checking the supplied JSON, an independent review scope decoded the saved raw protobuf using the official Perfetto v46 [TracePacket](https://github.com/google/perfetto/blob/v46.0/protos/perfetto/trace/trace_packet.proto), [bundle/compact scheduler](https://github.com/google/perfetto/blob/v46.0/protos/perfetto/trace/ftrace/ftrace_event_bundle.proto) and [scheduler event](https://github.com/google/perfetto/blob/v46.0/protos/perfetto/trace/ftrace/sched.proto) schemas. Reconstructed switch/waking intervals, including preempted R+, exactly reproduced all nine lower/upper state totals and all four selected-phase per-CPU occupancy totals. The read-only decoder ran inline as review analysis; this is not a new CI test or a separately published checker.

Raw reconstruction observed 2,005 ftrace bundles, zero raw lost-event flags, zero bundle parse errors and 573,295 scheduling switches across two guest CPUs. This supports the scheduler bounds independently of the supplied analysis's informational tokenizer count of 87,584. It does not establish complete ART attribution.

During the first Open's guaranteed runnable intervals, each guest CPU has 687.811796 ms of accounted occupancy. GMS highpool[0] occupies CPU 1 for 253.019096 ms; the app's HeapTaskDaemon overlap is 30.965493 ms. The cited keyboard worker's Warm 1 overlap is 94.868251 ms. These overlaps match the raw reconstruction. **Do not add occupancy across two CPUs as though it were unique wall time or assign all delay to one service.**

The test TID 2784 retains recorded scheduler priority 112 during this capture and runs on both guest CPUs across the method. No priority-demotion or permanent single-CPU defect is demonstrated. Actual scheduler policy, affinity and cgroup/cpuset restrictions remain unrecorded.

### Artifact custody

Both archives passed ZIP CRC and SHA-256 verification. Every one of the raw archive's 12 checksum-manifest entries matched.

- [Trace archive 15167106304](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45926598413/artifacts/15167106304): **d14e235612df3e72c95f16a80718197b267776429bc7b25a1585b6d6f5712a2c**.
- Raw trace: **33,512,355 bytes**, SHA-256 **c22fd144f6edfb0e545b387cfc7a0a2352fbd23bdf46296980e796ec107c2388**.
- [Occupancy archive 15167108427](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45928121708/artifacts/15167108427): **569c0128ee5180a70838415e3244ff46758b39c435d57388e108120fa18a65c0**.
- Occupancy JSON: **2ecf71513dfd627e61ea8bd984ae1697d0cdd5c25c3ee9170ea74b98240b08cf**.

## What this evidence does and does not establish

Most of the first Open's new traced wall interval was spent ready to run but not scheduled on a guest CPU. This justifies investigating scheduling conditions. It does not identify a source-code repair or justify changing the performance gate.

The diagnostic also starts shortly after a fresh emulator boot. The job log records boot completion at 02:12:30.822 UTC, capture-script start at 02:12:31.075, method start at 02:12:47.718 and first selected Open at 02:13:05.353—about 34.5 seconds after boot completion. No measured quiescence baseline was captured. Startup services and keyboard workers are plausible contributors to this diagnostic; this does not prove they caused the original full-suite miss.

Guest scheduled-running time is not verified runner-host execution time: a QEMU vCPU can be descheduled while the guest still considers its thread running. The runner hosting QEMU is itself a VM. A host-kernel trace observes that runner's scheduling, not the physical cloud hypervisor beneath it. Host steal/pressure counters can add context; an absent counter cannot prove absence of outside interference.

A paired capture can bound simultaneous guest and runner-host scheduling. It cannot retroactively recover the old trace or precisely measure counterfactual tracing overhead. Keep those limits explicit.

## Next action: one final paired diagnostic batch

The PR #52 first diagnostic is consumed. The unused original-suite-position conditional is **not** an extra retry credit: the miss reproduced in a fresh process, so its preceding-suite hypothesis was not established. This instruction authorizes **one additional paired guest/runner-host capture** for the newly demonstrated scheduling question. No automatic further trace cycle follows.

### Ownership and unchanged inputs

One Codex integrator owns a disposable diagnostic workflow, host/guest capture scripts, clock-correlation/analysis code and the ledger receipt. Delegate source/trace review without concurrent shared-file writes. Keep diagnostic selectors and temporary tracing machinery out of the production qualification workflow. Preserve the existing PR #50 test fixes.

Before editing, compare live main with d684a4b65ec3310a0ee33295034c77712eaea640 and preserve newer authorized work. Use the exact held candidate 27406f4aed8efa42a6078e1108c53304d4a0043e for app/test build inputs; record resulting APK hashes. Reuse existing binaries if available and verified. Do not modify application source or the R1.1 test merely to obtain this next observation.

Retain API35 Google APIs/x86_64/Pixel 3a, existing guest CPU configuration, 2,048 MiB RAM, measured 192 MiB managed heap, all twelve dense recipes, migration/list phases and selected order #12/#01/#06/#12. Preserve stopwatch, GC placement, one decode/exact-content checks and every memory/time limit. Per-Open **650 ms**, warm median **300 ms**, Open managed growth **64 MiB** and all original list/migration gates stay unchanged.

### Preflight before any new app-test execution

Use the existing diagnostic runner privileges and a small no-app capture to verify:

1. Host tracefs scheduler switch/wakeup events are actually available and readable. Record the runner image, kernel, CPU topology, emulator command and KVM configuration. Do not silently switch to a larger machine.
2. The emulator PID and its vCPU host TIDs are identified. Prefer observed KVM entry vcpu_id/TID mapping or an equivalent verified mapping supported by the actual emulator. A thread-name guess alone is insufficient for per-vCPU attribution. If only aggregate QEMU identity can be verified, state that limit and determine whether it can answer the named question before running the workload.
3. Both trace readers recover expected events and explicit synchronization markers with usable clock bounds. Record trace/parser versions and tool hashes; do not replace the Android Perfetto v46 service to obtain a new tracing feature.
4. Trace loss/overrun/coverage checks work on the preflight capture. Use low-overhead scheduler events, not full method/allocation tracing.
5. Existing process metadata can be recorded without changing it: host QEMU and guest test-thread policy/priority, CPU affinity/allowed sets and cgroup/cpuset membership when available. Capture boot age, premeasurement background activity and relevant host CPU/steal/pressure context. Mark unavailable supplemental counters explicitly.

The no-app preflight uses existing system/emulator processes. Collect the new test thread's optional metadata only when its TID exists during the one authorized method execution; never launch an extra app test solely to obtain it.

Host trace availability, usable identity, clock correlation and relevant interval coverage are essential. **If those essentials fail preflight, execute zero new app tests and publish the exact observability block.** Do not run another workload with the same known missing evidence. Missing optional counters alone need not block a capture that can answer the scoped scheduling question.

Do not introduce a guessed boot sleep, favorable-idle selection, service/keyboard disabling, background-work suppression, CPU pinning/priority change, tier upgrade or per-Open GC as part of the diagnostic. Record the time the preflight itself consumes; do not describe a different boot age as an identical startup condition.

### Explicit clock correlation

Keep separate host and guest traces. Select/verify a host trace clock synchronized across its CPUs and connect it explicitly to the host timestamps used for the marker protocol; for example, host trace_clock=mono with CLOCK_MONOTONIC timestamps. Do not assume a default per-CPU local trace clock is synchronized. Guest trace timestamps remain in their declared clock domain with the supplied clock snapshots.

Use a fixed set of unique marker handshakes, **five before and five after** the method, rather than searching indefinitely for a favorable timestamp. Bracket each synchronous guest trace-marker write with host timestamps h0 and h1, and recover guest marker time g. For a constant offset over that exchange, the host-minus-guest offset belongs to **[h0−g, h1−g]**. Preserve all brackets and their full uncertainty; the midpoint is not an exact mapping.

Check compatible pre/post offset intervals and any observed drift. Carry uncertainty into conservative host/guest overlap bounds. Refuse causal precision below the clock uncertainty; a zero conservative overlap lower bound means possible overlap, not established contribution. No arbitrary new millisecond cutoff is a replacement app acceptance gate. If clock drift cannot be bounded, retain separate measurements without pretending they are synchronized.

The no-app preflight must demonstrate that this protocol and the actual reader configuration work. The postflight checks must again validate clocks, mapping, loss and complete coverage of the relevant intervals. Invalid postflight evidence consumes the one diagnostic execution; retain its measurement but stop attribution and automatic repeats.

### Execute once and analyze saved evidence

Run the same complete method once while collecting guest scheduling/ART/phase logs and the runner-host scheduler trace:

com.greenezone.gzphotoeditor.AndroidRegularProjectListAcceptanceTest#twelveLegacyRecipesMigrateAndTheReadyIndexDoesNoListDecodes

Retain all nine phase measurements and exact selected-thread identities. Analyze the existing stopwatch brackets conservatively, as in the reviewed first trace. Distinguish:

- Guest ready but unscheduled intervals and the scheduled competing guest workers.
- Verified host vCPU scheduled-running, runnable-but-unscheduled and blocked intervals, with clock-mapping bounds.
- App waits/GC and tracer/runner activity, without treating overlapping categories as disjoint additive causes.
- Observability limits below the runner VM and uncertainty introduced by tracing/preflight.

Record all scheduled guest CPUs and the verified host mapping rather than attributing CPU 0 and CPU 1 by name alone. Do not subtract any scheduling/GC interval from acceptance wall time. Saved-trace reanalysis is permitted; another app execution is not an automatic response to an inconclusive result.

## Required disposition and forward path

| Outcome | Next permitted action |
| --- | --- |
| A specific environment/runner configuration condition is demonstrated | Document the condition, supporting intervals and smallest correction. Fix only that condition, preserving the named device/workload tier and ordinary Android services. Carry the correction into qualification reproducibly; a one-off manual favorable setup is not a fix. |
| A specific application allocation/wait/lock/I/O cost is established | Name the responsible path and owned files, then make the smallest supported repair with the inherited custody, compatibility, source-byte and output assertions intact. Do not infer a new processing-engine rewrite from this result. |
| A passing trace but no supported correction/disposition | Credit the observation only. It is not full qualification and does not explain the earlier failure. Do not invent a source patch. |
| Preflight blocked, clocks/loss invalid, or attribution still inconclusive | Stop this diagnostic batch, retain the hold and publish the precise limitation and finite engineering decision needed. Do not automatically prescribe another trace, another question cycle or retrospective proof of the original sample. |

When the evidence supports a correction or a justified prospective no-code environment disposition, Codex may proceed directly through the already authorized review and **unprofiled complete qualification**. No extra planning relay is needed for that supported next step. A no-code disposition must state what the new evidence establishes and why normal qualification is justified; a lone later green number or a guessed cause is insufficient.

Final exact candidate and exact merged application still require the established JVM/build/signing, complete API29/API35 suites, all **303 inherited + ten U1 correction identities** (plus any justified additions), unchanged meaningful assertions/budgets and separate actual force-stop/new-PID recovery. Retain still-applicable successful exact-head receipts. Changed application/test/build/CI inputs require the corresponding final qualification. Preserve every original failure and separate diagnostic timing from acceptance timing.

Only after the exact application merge qualifies and the focused review requirements are met should a corrected signed handset APK be checksum-verified and recommended. PR #46 remains prior qualification, not a substitute corrected APK.

Return one concise disposition with exact commits/trees, input/binary identity, commands/configuration, complete samples, clock bounds, trace loss/coverage, checked hashes, causal support versus uncertainty, the minimal change or no-code basis, and candidate/merge status. Publish raw artifacts through the established repository/Actions workflow; no local handoff copies.

Remaining R1.2 continues after this qualification disposition under the existing ledger. All 77 capability families and R1–R11 child scope remain. Earlier phone passes stay protected; B2/F03, RV5, broader device/accessibility evidence and formal workspace/release gates stay at their current boundaries. No change to offline/$2.99/no-runtime-AI/no-ads/no-subscription/no-IAP/no-watermark requirements.

## Primary technical references

These explain the measurement tools and boundaries; the execution choices above are this review's engineering decisions.

- [Perfetto scheduler events](https://perfetto.dev/docs/data-sources/cpu-scheduling): distinguishes scheduling, wakeup and thread-state transitions within the observed kernel.
- [Perfetto Linux capture](https://perfetto.dev/docs/quickstart/linux-tracing) and [clock domains](https://perfetto.dev/docs/concepts/clock-sync).
- [Linux ftrace clock and marker documentation](https://docs.kernel.org/trace/ftrace.html): verify the chosen trace clock and marker capture instead of assuming clocks match.
- [Linux KVM tracepoints](https://github.com/torvalds/linux/blob/master/arch/x86/kvm/trace.h): inspect the actual runner's available event format before relying on vCPU fields.
- [GitHub hosted runners](https://docs.github.com/en/actions/concepts/runners/github-hosted-runners) and [runner privileges/reference](https://docs.github.com/en/actions/reference/runners/github-hosted-runners): runner VM/host boundaries and actual capabilities.
- [Linux proc/stat documentation](https://kernel.googlesource.com/pub/scm/docs/man-pages/man-pages/+/refs/tags/man-pages-6.11/man/man5/proc_stat.5): supplementary accounting, not per-event proof of all host interference.

This is a documentation-only review/dispatch. It does not change application or CI inputs, rerun qualification, approve publication or claim handset acceptance.
