# B1 workspace correction verification

Status: integrated draft, not yet qualified for handset delivery. Updated November 25, 2026.

## Authority and exact baseline

Lance relayed the completed Stage 3 answers/resume prompt. The full CQ01–CQ50, active instructions, specification, roadmap, 77-family inventory and release gate were read before source edits. Baseline main was `6f7c574f223e5349a74e622e97433736a07f4a44`, tree `99239d1fd9efc25a4324aa39bb6ebe44a02395ff`. Its qualified app remains PR #31 merge `3ad65c533f7667a136d4fa1838afcc06d3f484da`, 0.12.10 Test/code22, until this changed app passes complete qualification.

[Draft PR #37](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/37) implements B1 plus B2 measurements. Engineering branch is `codex/b1-workspace-contract-20261125`; `feature/b1-contract-verification-20261125` invokes the unchanged push-triggered full matrix. This document is an evidence receipt; [WORK_LEDGER](WORK_LEDGER.md) and [GZ_ENGINEERING_ROADMAP](GZ_ENGINEERING_ROADMAP.md) remain the single execution record and roadmap.

## Reproductions and diagnosed failures

| Input / run | Observed result | Disposition |
| --- | --- | --- |
| Test-only `84ecf372d3c76b12f95784916d23fef7b40f18c7`; [45568857304](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45568857304) | API35: 193 inherited passes, 6 new failures. Three intended dirty-route failures; photo-GIF scroll and two Collage URI setup defects. API29 stopped after seven cases from system_server 16 MiB heap exhaustion. | Retained failure evidence. Fix test setup only and explicitly configure AVD RAM 2048M / heap 256M; no assertion/timing relaxation. |
| Corrected test-only `cf94cf3c4be22f6fbd473d5cb97a76aefbb70bc9`; [45571000015](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45571000015) | JVM/build/signing pass; both APIs: 199 cases, 6 intended failures, zero errors/skips; all 193 inherited cases pass. | Executed red evidence for Pro Back/mode, photo/video GIF Back, Collage Back and single-tap chrome. API35 XML artifact 15002133591 ZIP SHA-256 `72f8f8dadd66cdc25d4e7ddf7bf583c36be8341b00fbce157b8267618d41c742`; API29 artifact 15001949288, ZIP SHA-256 `6681da6fab5164acb508f8db22696890cf728268bb1d14fba00d8dd25594a92a`. Both full XML receipts confirm exactly the six named failures. |
| Integrated `a990aab0a03de36127d56cd6100f02b872f344c3`; [45575188527](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45575188527) | App compilation, JVM tests and debug assembly completed. Android test compilation rejected an invalid onNode import. | Remove the import, preserve test body. |
| `f61c786e0da534fdc06d5683b7cf82047a6182a6`; [45576278160](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45576278160) | App compilation/JVM/debug assembly completed. Android test cleanup used nonexistent GifStore.delete. | Use existing deleteProject API; no test assertion changes. |
| `0cc910ce1ca486f5a3da296855fe3f516a9bc193`; [45576913155](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45576913155) | Production compilation rejected recoveryResolved in the wrong function. | Correct exact closeCurrentPhoto/commitDiscard blocks; the independent reviewer found the same mistake. |

Integrated `805f97a3dc05702ec9c424de3d8e2a68573d0657`, [run 45577375539](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45577375539), passed production/test compilation, JVM tests, debug assembly and signature verification. Final source review confirmed the recovery/header correction. The first full run is recorded below; this compile-only checkpoint is historical.

### First integrated full execution — retained failures

Candidate `c819db57bc7fb33d0c46d83019cb5d9e7c428caa`, [run 45577786262](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45577786262), passed build/JVM/signature checks and executed all 250 Android cases without skips: API29 had 14 failures, API35 had 13. Neither is qualified. Full XML ZIPs were independently read and SHA-256 matched: API29 artifact `15006357100` `79e6f611001920c7f306bc01c4f8268f4cf9fd065877fd20ab4c66bb2a76e4d2`; API35 artifact `15006226855` `1334b8e73c77e985ed1e6c1b3eb3cc09986740adbea9e584810cc2592fdf4956`.

Diagnosed repairs are bounded to those failures: exact saved GIF identity recovery; AtomicFile obstruction/rollback and saved GIF read preflight; actual asynchronous Pro completion; dialog-scoped selectors and held-pointer semantic actions; loaded crop readiness; Collage relink readiness; and the API29 portrait capture viewport. Original method identities, content/source/history/fidelity assertions and existing budgets remain protected. Subsequent execution must verify the corrections; a diagnosis is not a pass.

### Second integrated execution and bounded follow-up

Candidate `713ed1917804b74f1cf4d93b46889c17dbd1f875`, [run 45583464308](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45583464308), passed build/JVM/signature checks. API29 passed **258/258**, zero failures/errors/skips. API35 passed **257/258**, with one test observation race and zero errors/skips. Full XML ZIP identities: API29 artifact `15008911956`, SHA-256 `6e743c79875da1920069fe20035e1bc46f83c4879c424b6f200c6f3637aedf45`; API35 artifact `15007774538`, SHA-256 `60f3a48640298fc7cdc1be87dd054841e511d5b1100007ffee349ef8549f4fa2`. Both archives were read completely and matched their published digests.

The sole API35 failure is ordinaryProDiscardReadsFreshRecoveryBeforeItNavigates. Source diagnosis confirms durable revoke → original baseline publication → session publication → departure callback. The fixture's departure counter updates synchronously but its external document mirror updates on the next composition. The corrected test retains the departure wait, then asserts exact count/document on Compose idle; all journal assertions remain. This is not a production ordering change.

The first bulk-row experiment passed all exact selected-frame ARGB, reverse-cache, output fidelity/schedule, source-protection and inherited timing tests on both APIs. Its measured row arrays are 4,320 bytes for the portrait fixture and 5,120 for the rotated fixture. However, it **did not meet the prospective preparation/source-reader speed targets**:

| Post-bulk-row metric | API29 | API35 |
| --- | ---: | ---: |
| Preparation to picker | 17,929 ms | 16,110 ms |
| Source reader wall / calling-thread CPU | 12,008 /11,407 ms | 13,640 /13,204 ms |
| Pipeline | 16,037 ms | 15,182 ms |
| Start UI acknowledgement | 122 ms | 88 ms |
| Cancel UI acknowledgement / worker stop | 59 /38 ms | 53 /16 ms |
| Observed cancel-command to completed notice | 103 ms | 104 ms |
| Output bytes / frames / duration | 995,576 /80 /2670 ms | 995,576 /80 /2670 ms |

Sampled independent decoded-color errors are identical to the corresponding baseline API. All three cache-equivalence cases and both preparation/cancellation cases pass. These observations do not close handset F03.

**Second bounded B2 experiment, recorded before its production patch:** source-reader CPU remains dominant. Test exact memoization of repeated packed YUV samples inside that reader, without changing PreviewColor.pixel arithmetic. Use one reader-owned direct-mapped cache of at most 4,096 keys plus 4,096 ARGB values (32 KiB array payload), with the color-matrix identity in each key; collisions recompute the original result. Do not attach this cache to retained reverse pictures or share mutable global caches. Retain the uncached scalar oracle, bounded reverse storage, existing row-memory bounds and all prior absolute/relative/fidelity/response targets unchanged. Add replacement/matrix-identity tests and aggregate conversion/cache counters. This experiment has one explicit causal hypothesis: repeated exact sample colors can avoid repeated floating-point conversion while preserving every pixel.

The GIF implementation owner returned the bounded reader memo and four JVM regressions; integration source review confirms matrix-qualified exact keys, collision recomputation, reader-only ownership, unchanged pixel arithmetic and preserved scalar oracle. GzGifReaderStage records bitmap materialization (sampling/reverse inflate + lookup/conversion + bitmap allocation), reverse sample packing and cache hit/miss/storage totals once on close. These counters are nested within sourceRead and are not additive to it. Other production files are unchanged by this experiment. Evaluate the resulting run against the original 10,000 ms /50% targets and record any unmet condition explicitly.

### Third integrated execution: representative improvement, invalid API29 heap

Candidate `32341cac97c0fe3b4595ff4dd909aeac2ffd9490`, [run 45589117594](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45589117594), passed build/JVM/signature checks including all four exact-YUV-cache JVM cases. API35 passed **258/258**, zero failures/errors/skips. API29 passed **253/258**, with five failures, zero errors/skips. The complete XML archives match GitHub's ZIP digests: API35 artifact `15012080963`, SHA-256 `ab5ccbb7a4a81a7de2fee7ad4071d14c634cda19cdda2539058d1769e01f0707`; API29 artifact `15011324585`, SHA-256 `3cfac8033731e25796e8aa87d2df5a25935818d6d7e8a2661a647efe72ee63f2`.

The API29 process reports `growth limit 16777216` (16 MiB): native portrait exact-pixel comparison cannot allocate its 8,294,416-byte array; rotated reverse storage cannot allocate its 2,764,816-byte array; both original-size GIF profiles fail frame allocation before a picker exists and then time out; the inherited Regular below-budget Save/read fixture logs a 1,999,296-byte allocation OOM at the same 16 MiB limit before its readable-project assertion fails. These receipts are failures, not a qualified API29 result. Workflow inputs requested `ram-size: 2048M` and `heap-size: 256M`, while launch logs report `qemu.dalvik.vm.heapsize=512m`; configured inputs therefore do not establish the effective ART application growth limit. A bounded infrastructure diagnosis and preflight correction precede any retry. No application memory/fidelity gate is waived.

API35's second bounded optimization observation: preparation-to-picker **13,698 ms** versus original **26,262 ms**; source reader **9,339 ms wall /8,818 ms CPU**; pipeline **12,212 ms**; bitmap materialization **9,069 ms wall /8,765 ms CPU**; 165,885,502 exact-cache hits /2,498 misses, 32,768-byte cache payload and 4,320-byte row arrays. Output remains **995,576 bytes /80 frames /2670 ms**, with identical sampled decoded-color errors to that API's baseline and passing exact selected-frame comparisons. Start acknowledgement was **257 ms**; cancel acknowledgement **36 ms**, worker stop **19 ms**, observed cancel completion **84 ms**.

The original **10,000 ms** absolute and **50% picker improvement** targets remain unmet on this API (47.8% observed picker reduction); source-reader reduction exceeds 50%. The 257 ms start sample misses the 250 ms engineering response target. These are single observations on separate CI hosts, not a completed matched cold/warm sample study or handset proof. Representative gains may be credited under CQ18; B2 target completion and F03 handset closure remain open. The two bounded experiments are evaluated with unmet exits, not successful B2 completion. CQ18/CQ50 permit a coherent regression-qualified B1 candidate and subsequent R1.1 work while these numeric misses and F03 remain open; they do not require a third speculative optimization. Candidate and merged-main qualification are still required before delivery.

### Bounded CI and diagnostic correction after candidate 3

The [runner implementation](https://github.com/reactivecircus/android-emulator-runner/blob/v2/src/emulator-manager.ts#L40) writes its heap input as `hw.heapSize`; the [AOSP AVD schema](https://github.com/aosp-mirror/platform_external_qemu/blob/master/android/avd/hardware-properties.ini#L263) defines integer-megabyte `vm.heapSize`. [Android 10 VM startup](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-10.0.0_r47/core/jni/AndroidRuntime.cpp#L770) reads maximum heap and growth limit separately, with a 16m default if the maximum property is absent. The failed run did not record guest properties, so the exact missing/restricted property cannot be reconstructed. The ineffective input and observed 16 MiB budget are verified; no stronger causal claim is made.

The correction explicitly sets API29 AVD `vm.heapSize=256` and `-memory 2048`, then uses a CI-only emulator/API29 guard to record the old guest values, set both `dalvik.vm.heapsize` and `dalvik.vm.heapgrowthlimit` to `256m`, restart the framework and require a fresh zygote plus the exact properties before the unchanged full suite. ADB calls and stop/boot waits are bounded; failure stops the job. No app `largeHeap`, test exclusion or acceptance relaxation is introduced. API35 execution settings are retained.

The GIF profile now fails immediately when the actual UI reports `GIF export failed.`, preserving its explanation instead of waiting six minutes for impossible success. Expected cancellation remains success; every existing timeout/assertion/workload/fidelity check is unchanged. Both profile paths record `Runtime.maxMemory()` to expose the actual app budget. Root readback review retained all 25 assertion lines byte-for-byte. The script received source review; a local bash syntax check was unavailable because bash is not installed, so execution of this changed CI setup is still required.

### CI preflight refinement, before Android instrumentation

Candidate `f4802bb807e3f0a96fc20b4335b3e17c06453377`, [run 45595436544](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45595436544), passes build/JVM/signing. API29 job `139140147036` stops in preflight with **zero Android tests executed**. The old guest values are maximum `512m` and empty growth limit; after framework restart both are exactly `256m`. The guard rejects `pidof zygote64` returning `3180 4097` instead of a single PID (old PID `1664`). This is a process-identification assumption in the new CI guard, not an application regression or proof of an Android suite pass.

The bounded correction selects the single `zygote64` with parent PID1 from Android's process table and requires a changed owner PID, `init.svc.zygote=running` and both exact heap properties. This checks the init-owned VM rather than counting same-named children. All budgets, full-suite selection and application code are unchanged. The next push supersedes the unqualified run; preserve its preflight failure instead of retrying it unchanged.

## Implemented contract and finite review

The common header places the wrapping brand and Options at opposite edges on its first row, with workspace/Back and history on the second. Major actions have one Options location. Regular More/Create collage/Histogram entries are removed; processing/legacy compatibility remain. Collage free-canvas single taps select a hit tile and toggle chrome once; canceled/multi-pointer/moved gestures do neither.

Save/Discard/Cancel operates on captured native owners, revisions and expected saved-project identities. Child photo saves remain separate from Apply to Collage. A child-to-workspace departure stages child resolution until the parent decision and target preparation succeed. Regular-to-Pro resolves the intended photo appearance and existing Pro work before publication. Save acknowledgement follows verified publication; failed/canceled work stays open.

Stable save UUIDs are sidecars to compatible existing payloads. Update/Replace authorize the exact identity and stored-byte version; delete/recreate gets a new identity. File publication preserves verified old bytes until commit. Regular preferences retain a bounded previous-value record through verification and rollback. This is B1's minimum safe adapter, not the R1.1 per-project migration or R1.2 autosave completion.

Finite independent reviews found and corrected stale native-export restoration, post-publication rollback, late picker/dialog ownership, typed Retry/Cancel, child Photo routing, active pointer rollback, paired content/selection mutations, anonymous recovered-draft baselines, GIF name-only recovery aliases, Pro Delete recovery and narrow-header placement. Reviewers identify source findings separately from runtime results. Export retry keeps its captured recipe independently of native draft disposition; unreadable recovery permissions cannot count as a successful live Discard.

## Existing assertion migration

All **193 original Android method identities** are retained. The first integrated run executed **250 cases**; its failures are preserved above. The follow-up adds exact saved-GIF recovery, bulk/scalar ARGB equivalence and actual preparation cancellation regressions; the next combined source contains **258 cases**, all 193 inherited identities retained with zero duplicates, pending runtime qualification. Required candidate and merged-main JVM/API29/API35/signing receipts remain pending.

| Existing files / scope | Authorized route changes | Retained checks |
| --- | --- | --- |
| Nine Pro files; two assigned files unchanged, 31 cases | Options replaces File; Undo/Redo use accessible descriptions; named Save updates directly; Save as handles copy/collision. Dirty replacement explicitly saves; hidden work is saved separately before testing reopen history. | All 210 assertion-bearing lines retained modulo selector/default-copy-name text; layer geometry, masks, blend equations, source pixels, render samples, reset and named history ownership. |
| Four Regular files, 24 cases | Options replaces More/direct commands; child commands are photo-labeled; failed Save resolves before replacement picker. | 151 of 152 non-UI assertion lines retained; failed-save picker count strengthens 2→1. Dense-recipe budget, stroke/capacity/endpoints, sibling tiles, decoded PNG, source/history remain. |
| Regular histogram method | Production Histogram entry removed. Test-only dialog consumes samples from the actual hidden-chrome canvas, excluding letterboxing/seam interpolation. | Same method, full-bitmap RGB/luminance counts, chart assertions and 50%→0%→50% clipping expectations; additionally checks More/Histogram production absence. |
| Four GIF files, 16 cases | Options/current command labels; explicit save decisions; history semantics; named identity checks. | All 16 methods; scheduling, metadata, independent decoded frames, source bytes, export recovery and existing 6s/15s gates remain. |
| AndroidBackNavigationTest,19 cases | Common menus/appearance labels and child Apply/Discard. Retained-history departures explicitly save a fixture and clean it after the test. Async Pro reopen waits for the actual restored preview before export. | Every source-byte, sampled-pixel/crop, timing, viewport, selection, history and one-commit assertion retained. |
| AndroidExportRecoveryTest,3 cases | Options routes; restored draft's dirty Back now explicitly guards; restored native Collage saved through Save as. | Exact output MIME/dimensions/pixels, duplicate and canceled destination bytes, pending journal and restored native recipe checks. |
| CollageExportUiTest and CollageReopenUiTest,8 cases | Options owns opening/export; original slot assertion now compares the persistent Options control before/after load, then opens the actual Export action. | All original output pixels/format/dimensions/source checks, tile identity, relink, failed atomic-write bytes, exact saved document; same bounds comparison for the surviving common command location. |
| PhotoFirstUiTest,13 cases | Empty guidance points to Options; Pro uses its legitimate Add layer path; Collage opens via workspace menu; common Back semantics. | All numeric/document/tool ownership/grade/export-format checks; obsolete direct-action expectations map to their one Options entry and no Create collage duplicate. |

New tests cover actual dirty routes, delayed/duplicate picker delivery, save failure/rollback, owner-qualified recovery, interrupted text/shape/privacy/crop gestures, lifecycle interruption, paired Duplicate cancellation, narrow 320dp/200% text header interactions, and the default-quality GIF profile. Some focused custody tests exercise the actual controller/store directly; they are not claimed as end-to-end phone execution.

## B2 measurement and owner boundary

Lance reports the 2–5 minute delay occurred before choosing a save location, on an approximately 2–3 second clip with all options at defaults except trim. Phone/Android/source remain unknown.

The representative profile opens the existing 1080×1920 / 150-source-frame motion fixture, trims through the real UI to 2–3 seconds, retains Original dimensions/source timing/100% forward/neutral edits, and measures export activation to destination-picker launch. It verifies output dimensions, complete frame schedule/delays/duration/loop, independent decoded-frame samples and unchanged source hash. Its 360-second diagnostic bound is not a product acceptance target and does not replace the inherited 6s sampled-motion or 15s Save fixture gate.

Measurements distinguish preparation queue, processing, provider-copy and response/cancellation stages. Overlapping counters are not summed as disjoint time. No palette, dimensions, quality, source schedule or frame selection has been reduced. The first execution measures the following default-quality workload. Both lanes use 80 scheduled frames, trim 0–2665 ms, 1080×1920, and produce 995,576 bytes /2670 ms of GIF timing.

| Metric | API29 x86_64 | API35 x86_64 |
| --- | ---: | ---: |
| Export activation to picker | 20,932 ms | 26,262 ms |
| Pipeline | 19,119 ms | 24,853 ms |
| Source reader wall / calling-thread CPU | 15,199 /14,673 ms | 22,354 /21,718 ms |
| Palette / LZW including output | 2,523 /1,063 ms | 1,517 /895 ms |
| Provider copy after picker | 58 ms | 33 ms |
| Start UI acknowledgement | 178 ms | 121 ms |
| Decoded sampled mean channel error, first/middle/last | 2.757 /2.642 /2.642 | 2.155 /2.064 /2.079 |

**B2 prospective bounded experiment, recorded before its production patch:** the source reader dominates and its CPU time nearly matches wall time. Inspection shows Original-size sampling performs three absolute direct ByteBuffer reads per output pixel; the existing filtered-downscale branch already bulk-reads rows. Hypothesis: applying the same bounded row access to unfiltered sampling will remove per-pixel buffer overhead without changing any sample coordinate or color calculation. This is an attribution to the reader, not a claim that platform decode alone is slow.

The integration owner exclusively owns EditedVideoPreviewReader.kt and its focused equivalence tests for this experiment. Exit targets for the same fixture and settings on each named CI API: preparation-to-picker at most 10,000 ms AND at least 50% lower than that API's recorded baseline; source-reader wall at least 50% lower; exact scalar-versus-bulk decoded ARGB equality on representative native-size/rotation/reverse samples; unchanged complete GIF dimensions/frame schedule/delays/duration/loop and existing independent decoding/error thresholds; unchanged original source bytes. Preserve all inherited 6s motion /15s Save checks.

The added row cache must scale with source row width, not retained frame count; at the 1080-wide fixture it must stay below 16 KiB additional row storage. Existing 16 MiB reverse-picture cap and full-frame working buffers remain unchanged. Start/cancel visible acknowledgement target is 250 ms; cancellation of this local fixture should release the worker within 1,000 ms. Record actual measurements and explicitly retain any unmet target instead of changing it after the run. These are bounded CI-workload targets, not handset or universal supported-media guarantees.

F03 remains open until the applicable measured and owner handset evidence exists.

### Post-review changes awaiting execution

The bulk-row source reviewer found no production correctness issue and requested two stronger test details: include a neighboring reverse timestamp so the test consumes an actually retained packed picture, and use the strict less-than row-cache bound. Both were applied. The exact-pixel comparison covers selected native-size, rotated, reverse-cache, anamorphic and filtered frames; it does not claim every frame of every supported source.

The empty staging-directory regression additionally verifies both legacy/named Save and load paths, old bytes/identity, retained obstruction and cleared pending metadata. The shared reader preserves the healthy base without invoking destructive AtomicFile cleanup in that specific state; publication rejects non-file transaction occupants before changing bytes.

The first-run fixes retain the original method identities and non-UI assertions. Collision cancellation now asserts the entire canceled dialog is gone; held-pointer toolbar commands use the actual semantic click without introducing an invalid second DOWN; dialog actions are scoped to their modal; crop/relink and Pro completion wait for their actual state. The Pro hidden-save fixture now uses a distinct name inside the existing 28-character input limit. These changes do not alter content, quality or timing acceptance checks.

A separate source review checked the new actual-UI cancellation profile and its timing report. The canceled notice follows worker join and journal clearance; the test observes completion including dispatch/polling overhead, while worker-stop and visible-frame acknowledgement are logged separately. It cancels during frame preparation, not necessarily at an exact frame number. Execution is still required.

The final targeted Help read confirms the changed Pro, GIF and Photo instructions use Options/Save project/Export and the Collage Help describes the common command location and independent tile workflow. This is source confirmation of the renamed-command documentation, not a separate UI execution claim.

## Delivery gate

Do not deliver this draft as qualified. Complete the integrated candidate and merge JVM/API29/API35/signing checks, resolve reproduced high-impact failures, preserve run/artifact hashes and verify the exact phone APK. Then provide one coherent candidate and a short owner confirmation script. Workspace locks, R1–R11 completion, Video, bilingual final onboarding, website dependency, Play Console setup and public release retain their separate roadmap/owner gates.

## Focused handset confirmation after a qualified artifact is delivered

1. Open the same photo in Regular, visit Pro/Collage/GIF, and confirm Options remains at the top right with one New/Open/project/export/help location. Check larger text if used on the phone.
2. In a two-photo Collage, tap a tile and a margin once each to hide/show tools. Edit only one tile, try leaving with Apply and then cancel the parent departure; confirm both the child work and parent remain available.
3. Make a visible change in Regular, Pro and Collage. Try Back and a workspace switch. Cancel must keep the work; Save must finish before leaving; Discard must remove only the specified unsaved branch. A standalone tile-photo Save must still leave its parent Apply decision.
4. Save a named project, change it and Save again, then use Save as for a second name. Reopen both and confirm the intended edits and independent targets. Cancel a name collision and remain in the current work.
5. Repeat the reported approximately 2–3 second video trim with all other options at defaults. Note the approximate wait until the save-location picker, separately from the wait after choosing the destination. Open the completed GIF and confirm movement, crop/orientation and duration.
6. During a separate GIF preparation, use the visible cancellation action. Confirm the interface acknowledges it and reports completion only when the worker has stopped. Retained native edits should still be available.

These focused owner observations are distinct from emulator qualification. They do not grant workspace locks or release acceptance.
