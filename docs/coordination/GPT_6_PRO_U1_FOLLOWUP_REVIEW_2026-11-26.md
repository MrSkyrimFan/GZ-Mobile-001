# Independent U1 correction review and R1.1 qualification disposition

November 26, 2026 (America/Denver). This is a bounded review of PR #49 and draft PR #50, followed by the next diagnostic instruction. It supplements the single [work ledger](../WORK_LEDGER.md#u1-review-correction-and-qualification-hold); it does not introduce another roadmap or reopen the completed U1 design.

## Verdict

**The three U1 corrections are supported by source and actual red-to-green evidence. No additional blocking source defect was substantiated in those corrections. Keep PR #50 in draft and the corrected handset APK on hold.** The exact candidate failed the existing API35 R1.1 per-Open wall-time limit. Concurrent garbage collection is a diagnostic lead, not a proved explanation or a waiver.

The next action is one traced execution of the unchanged complete API35 R1.1 test method. Diagnose the delay before choosing a production correction or another qualification attempt. The finite instruction below preserves the fixture, every assertion, the 650 ms individual limit and the 300 ms warm-median limit.

This review performed no application-source changes, builds, emulator executions or handset testing. It inspected source and existing raw artifacts. Three independent scopes reviewed Pro tool/save behavior, Open resource ownership, and test/qualification behavior. Root independently checked the receipt hashes, XML identities, failure and process logs, source changes and active dispatch.

## Exact baseline and scope

| Role | Exact checkpoint |
| --- | --- |
| Intake/current main and complete supplied handoff | [PR #51](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/51), 792e758fe01c0fcbcf96e30c59e7ed441793fb54; tree e849afacf864b6e3555581165459095b4ad0b9d1 |
| Production corrections | [PR #49](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/49), b342f0bd656753b3692b741489438c6790d14d8b; tree 046ac9806b7181c78cb1b30cf88505d02354e9cf |
| Held draft candidate | [PR #50](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/50), 27406f4aed8efa42a6078e1108c53304d4a0043e; tree 2c0f8ffab9a8321f8f9f823c9354c26b5a2fb6c8; 0.12.15 Test/code 27 |
| Last fully qualified application | PR #46, e6321322526521ace13a45c231b25bb08f91c565; 0.12.14 Test/code 26 |

The [complete intake handoff](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/792e758fe01c0fcbcf96e30c59e7ed441793fb54/docs/coordination/GPT_6_PRO_U1_FOLLOWUP_REVIEW_HANDOFF_2026-11-26.md) was read. Current main matched its documentation pin. PR #50 changes exactly three Android test files relative to the production correction merge; production/build/CI inputs are unchanged by that PR. Blob identities for the R1.1 test, ProjectStore, PhotoProjectFiles, EditDocumentCodec, the workflow and the ordinary/process runner also match the prior PR #46 versions. That rules out a direct change to those files; it does not prove the current timing failure is infrastructure-only or unrelated to accumulated application state.

The current 77 complete capability rows/77 unique IDs match the prior reviewed PR #48 rows. The roadmap from R1 through its end is byte-identical to that checkpoint. No family, child requirement, budget or owner gate is closed by this review.

## U1 findings: disposition and exact support

### U1R-01 — linked Pro Layers shortcuts: correction supported

Both real Layers shortcuts now enter the existing guarded native-tool transition: [ProWorkspaceScreen.kt 324–338](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt#L324-L338) and [1775–1780](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt#L1775-L1780). They leave finished-output viewing while preserving the target, native parent, linked Regular finishes and existing busy/departure guards. Tool selection itself does not add a content-history entry.

The preview/render state is keyed to the finished/native mode, so a late finished render cannot supply the raster used by newly enabled native input: [281–284](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt#L281-L284), [1407–1439](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt#L1407-L1439) and [1593–1601](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt#L1593-L1601).

[AndroidU1ReviewProShortcutsTest 47–156](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidU1ReviewProShortcutsTest.kt#L47-L156) taps the actual shortcuts, performs a selection drag or held retouch source/destination gesture, checks the selected second layer and unchanged sibling, checks exact history/Undo/Redo, saves/reopens and independently decodes PNG output. Asymmetric colors and a linked mirror make the pixel checks meaningful.

The initial unchanged-behavior receipt fails at disabled Apply layer mask and Reset source. The focused and current complete receipts pass both identities on both APIs. The fixture seeds linked state using real native storage and directly mounts the Pro screen with persistence adapters; it is not itself a full main-host creation journey through Regular and Pro. Inherited host-switch and process tests provide complementary evidence. This scope limit does not reopen the correction.

### U1R-02 — failed/stale/canceled Open custody: correction supported

[NativeWorkStore.kt 100–125](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/main/java/com/greenezone/gzphotoeditor/editor/NativeWorkStore.kt#L100-L125) retains an undelivered prepared handle across the IO dispatcher return. Cancellation before the caller receives it reaches exact cleanup. [Release at 299–306](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/main/java/com/greenezone/gzphotoeditor/editor/NativeWorkStore.kt#L299-L306) uses noncancellable IO and removes that identity-keyed lease, including when another handle references identical manifest bytes. [Acknowledgement at 132–158](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/main/java/com/greenezone/gzphotoeditor/editor/NativeWorkStore.kt#L132-L158) publishes the durable catalog before removing transferred stages.

Named preparation, working-draft acquisition and both named Open routes now account for their acquired handles and unadopted photos: PhotoEditorScreen [1304–1405](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/main/java/com/greenezone/gzphotoeditor/ui/PhotoEditorScreen.kt#L1304-L1405), [1854–1905](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/main/java/com/greenezone/gzphotoeditor/ui/PhotoEditorScreen.kt#L1854-L1905) and [2433–2451](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/main/java/com/greenezone/gzphotoeditor/ui/PhotoEditorScreen.kt#L2433-L2451). [RegularProSwitch.kt 25–58](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/main/java/com/greenezone/gzphotoeditor/ui/RegularProSwitch.kt#L25-L58) also owns decoded/rendered bitmaps until dispatcher-result delivery succeeds.

[AndroidU1ReviewOpenTest 36–135](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidU1ReviewOpenTest.kt#L36-L135) exercises three failed actual named Opens in each mode, restored-source success, stale Regular named Open, and stale/canceled working-draft Open with active and displaced leases. Current work, named identities, unrelated draft/source custody and an independent GIF retry remain checked. [AndroidU1ReviewLeaseTest 33–127](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidU1ReviewLeaseTest.kt#L33-L127) deterministically cancels at IO-result delivery and separately verifies exact-token release, retained roots and normal collection of an unrooted manifest.

The corrected Open baseline fails with lease counts 1/2 or [1,2,3] on both APIs; the new behavior passes. Initial fixture failures are not counted as defect reproductions. No exhaustive OOM, every Pro interleaving or direct bitmap-recycled assertion is claimed. These source-supported limits do not justify another broad test cycle.

### U1R-V1 — Save completion after Cancel: reproduced and corrected

The parent captures the exact native owner, previous save target and saved document: [PhotoEditorScreen.kt 1133–1155](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/main/java/com/greenezone/gzphotoeditor/ui/PhotoEditorScreen.kt#L1133-L1155). The still-mounted Pro child consumes the acknowledgement, updates only target/history/baseline metadata and preserves newer document content; active drag/slider state defers consumption: [ProWorkspaceScreen.kt 953–973](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt#L953-L973). The host clears only the consumed acknowledgement [1839–1841](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/main/java/com/greenezone/gzphotoeditor/ui/PhotoEditorScreen.kt#L1839-L1841); canceled switch ownership prevents late navigation [1167–1187](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/main/java/com/greenezone/gzphotoeditor/ui/PhotoEditorScreen.kt#L1167-L1187).

[AndroidU1ReviewProSaveTest 34–136](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b342f0bd656753b3692b741489438c6790d14d8b/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidU1ReviewProSaveTest.kt#L34-L136) mounts the actual host, holds publication, cancels the switch, edits visibility, then releases Save. The captured revision is saved while newer work remains dirty; Undo reaches the clean saved revision, Redo restores dirty work, and ordinary Save reuses the same named project without a naming dialog. The unchanged baseline fails the clean-after-Undo assertion, and both corrected APIs pass. Gesture-active deferral is source-inspected rather than separately injected by this test.

U1R-N1's optional Collage wording remains deferred; it is not a delivery blocker.

## PR #50 test-only changes

| Change | Assessment and retained checks |
| --- | --- |
| GIF fixture publication/readback | [AndroidGifProjectIdentityTest.kt 319–359](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidGifProjectIdentityTest.kt#L319-L359) verifies one published MediaStore row, pending=0 and decoded 32×32 marker pixels before mounting. [414–458](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidGifProjectIdentityTest.kt#L414-L458) retains the same 15-second deadline, preview/header/Play prerequisites and rendered 5×5 pixel check while reporting which prerequisite is missing. Save/Open, order, timing, identity and independent-project checks remain. These additions do not prove why the original readiness timeout occurred or resolve B2/F03. |
| Collage child Save acknowledgement | [AndroidBackNavigationTest.kt 1125–1147](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidBackNavigationTest.kt#L1125-L1147) waits for both the saved record and visible acknowledgement under the existing 15 seconds; subsequent Back, Apply tile changes?, Discard and unchanged-parent/zero-commit assertions remain. |
| Real process-recovery scheduler | [AndroidU1ProcessRecoveryTest.kt 17](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidU1ProcessRecoveryTest.kt#L17) changes only the empty-rule import to Compose v2. Actual ActivityScenario launch, distinct PID, cold-host/heap limits, both named UI Opens, native-layer edits and durable state/source/retry assertions remain at [165–407](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidU1ProcessRecoveryTest.kt#L165-L407). |

[Official Compose v2 guidance](https://developer.android.com/develop/ui/compose/testing/migrate-v2) explains its queued StandardTestDispatcher versus the legacy Unconfined dispatcher. That supports the narrow scheduler correction, without establishing that all prior failures were harness-only.

The four preceding API29 receipts were also inspected: artifacts 15160912621 and 15161928024 each fail the initial 15-second GIF readiness wait before Save/Open/pixel sampling; the old message cannot distinguish preview, header or Play. Artifact 15162803788 passes all 313 ordinary cases but fails separate reopen in DialogWrapper through the legacy Compose scheduler on a worker thread; it is not a recovery pass. Artifact 15164431420 reaches the disk-record condition then fails the immediate displayed-notice assertion; the raw receipt does not establish when that notice would later appear. Its separate recovery passes, but the overall run was canceled, so only its completed receipt is credited. All four ZIP hashes match the pinned ledger and CRC checks pass. These observations support the narrow test changes while preserving uncertainty about the original GIF readiness cause.

The [unchanged shell runner 104–141](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/.github/scripts/android-test-with-receipts.sh#L104-L141) installs and clears the disposable package, runs seed, performs actual force-stop, and runs reopen. Both phases must return OK (1 test). A passing recovery lane does not mask an ordinary-suite failure. The ordinary 313-case identity for recovery does not itself represent force-stop; that proof is separate.

## Raw evidence independently inspected

All downloaded ZIP CRC checks passed and SHA-256 matched the ledger. Current run metadata pins [45920992792](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45920992792) to 27406f4aed8efa42a6078e1108c53304d4a0043e; Verify succeeded and the API35 ordinary job failed. Its handset-artifact upload was skipped. The Verify job log shows JVM/build completion and APK v2 signing verification; this review did not independently install or revalidate a corrected APK's bytes.

| Existing run / artifacts | Independently observed result |
| --- | --- |
| [Initial baseline 45906324931](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45906324931), API29 15157614042 / API35 15157858464 | Ten cases each: expected shortcut, lost-lease and Save acknowledgement failures; exact-handle control passes. Four initial Open fixture failures retained without false reproduction credit. |
| [Corrected Open baseline 45907070523](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45907070523), API29 15159145281 / API35 15158442140 | Five intended Open lease failures per API, after fixture correction. |
| [Focused 45907591692](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45907591692), API29 15159365857 / API35 15158349089 | Ten unique cases per API, zero failures/errors/skips. |
| [Current full 45920992792](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45920992792), [API29 15165596289](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45920992792/artifacts/15165596289) / [API35 15165426352](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45920992792/artifacts/15165426352) | 313 unique identities on each API; all 303 inherited identities retained plus exactly ten new cases. API29: zero failures/errors/skips. API35: one latency failure, zero errors/skips. All ten new cases pass in both complete suites. |

The sorted classname#method inventory joined by LF without a trailing LF has SHA-256 **b3e75ca493b1e5b8bcc2b14f5e9ea70c7ad21edb0a868db220407666c536f377** on both complete suites. The prior 303-case inventory was compared directly, not inferred from a count.

Current API29 ZIP: **4d6cb9b5d8a74863555f53ad281afee345f062814f8aa70cf1198943dd9b7af2**. Current API35 ZIP: **393898458ae08eb19023f4025aa933c47f1d9f76e1671d328a5a2c08be3ee874**. The six baseline/focused hashes remain in the [pinned ledger](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/792e758fe01c0fcbcf96e30c59e7ed441793fb54/docs/WORK_LEDGER.md).

Separate seed/reopen transcripts return OK (1 test) on both APIs:

| API | Actual PID change | Cold host | Managed increase | Logged proof, checked against test source |
| --- | --- | --- | --- | --- |
| 29 | 19002 → 19049 | 1,618 ms | 10,877,344 bytes | Active native edit; both named UI Opens; two subsequent native edits; named versions retained |
| 35 | 9293 → 9330 | 1,616 ms | 10,845,136 bytes | Same exact proof in a new process |

The seed is programmatic; separate ordinary host tests cover creation and switching. No single uninterrupted all-UI creation-through-force-stop journey is claimed.

## Remaining blocking finding — R1.1 per-Open qualification failure

**Severity: P2 / qualification blocker.** Trigger: the fourth selected Open in the retained twelve-project dense-recipe fixture on API35. Observed impact: this candidate violates its established store-Open responsiveness bound, so it cannot be called fully qualified or recommended as the corrected handset candidate. This is not evidence of corruption or a newly observed phone delay.

The authoritative assertions are [AndroidRegularProjectListAcceptanceTest.kt 107–132](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidRegularProjectListAcceptanceTest.kt#L107-L132) and [161–165](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidRegularProjectListAcceptanceTest.kt#L161-L165); wall time is measured around actual loadNamed at [255–286](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidRegularProjectListAcceptanceTest.kt#L255-L286).

| API35 selected phase | Time | Result |
| --- | --- | --- |
| selected_first | 206.768912 ms | Within 650 ms |
| selected_warm_1 | 191.220062 ms | Within 650 ms |
| selected_warm_2 | 182.319529 ms | Within 650 ms |
| selected_warm_3 | **717.745435 ms** | **Fails 650 ms by 67.745435 ms** |

All four Opens decoded exactly one recipe and returned the requested identity/source/exposure/point count. The slow sample observed 33,726,512 bytes (32.16 MiB) heap growth, below 64 MiB. The latency assertion fails before the test asserts the slow sample's heap limit or logs/asserts the warm median. A median calculated from the raw numbers is not a completed test gate and cannot excuse a per-sample failure.

The per-test log in API35 artifact 15165426352 records the sample at **01:25:31.547–01:25:32.265 UTC**, with a concurrent mark-compact GC ending **01:25:32.152**, total **551.746 ms** and recorded pauses **10.145 + 11.190 = 21.335 ms**. It also logs a 9.713 ms suspension message; do not add potentially overlapping runtime timings together as independent stalls. A concurrent collection's total is not time wholly lost by the application. Do not subtract either collection duration or pauses from acceptance wall time.

Process-wide allocated bytes were 56,238,128 for the slow sample versus 56,295,472 for the preceding 182.320 ms Open. These counters do not attribute all allocation to this method and do not establish a unique allocation-volume spike. The sampler observed 120 polls over the slow interval versus 83 over the preceding shorter interval; this is compatible with scheduling/runtime interference but does not identify a cause. There is no scheduler/thread-CPU trace in the supplied receipt.

[Official ART performance guidance](https://source.android.com/docs/core/runtime/gc-debug) recommends Perfetto/GC timing to distinguish pauses and preemption, and notes that excessive allocation can cause performance problems. It supports gathering attribution rather than assuming an emulator flake or prescribing a codec rewrite from one overlapping log entry.

## Finite Codex continuation — next diagnostic and completion criteria

### Baseline and ownership

Read this review and the live ledger, compare main to 792e758fe01c0fcbcf96e30c59e7ed441793fb54, and preserve subsequent authorized documentation. Start from draft 27406f4aed8efa42a6078e1108c53304d4a0043e; reconcile main's documentation without losing PR #50's three reviewed test changes. Inspect applicable actual project instructions. Do not claim to inspect or alter an unavailable local routing file.

One Codex integrator owns the diagnostic command/receipt and any necessary R1.1 test seam. Independent reviewers may analyze traces and proposed diffs. No concurrent edits to shared files. Prefer an external Perfetto capture of the existing APK/test APK with the complete test method selected; no production edit is required to start.

### First decisive check

Run **one diagnostic API35 execution of the complete unchanged R1.1 method**:

com.greenezone.gzphotoeditor.AndroidRegularProjectListAcceptanceTest#twelveLegacyRecipesMigrateAndTheReadyIndexDoesNoListDecodes

Use the same google_apis/x86_64/pixel_3a configuration, actual **192 MiB managed heap**, and existing dense twelve near-budget recipes. Preserve fixture setup, initial migration, four metadata lists and selected order **last / first / sixth / last**. Preserve existing GC requests outside the migration/list timing; add no GC, extra warm-up, predecode or cache reset between selected Opens.

Capture a low-overhead Perfetto scheduling/ART trace covering fixture setup through all four Opens, correlated with existing phase logs and PID/TID. Retain wall time, per-phase heap/allocation data, and selected-thread running/CPU time versus runnable-wait/blocked time. Prefer scheduling-derived CPU time so the measured method need not change. If an extra diagnostic marker is necessary, isolate it on a diagnostic branch and disclose its overhead; it must not replace or narrow the acceptance stopwatch. Do not start a high-overhead allocation/method tracer and treat its timings as qualification.

Use the trace to distinguish app execution from monitor wait, I/O, GC/allocation waits and scheduling competition. The relevant unchanged path is [ProjectStore.loadNamed 94–102](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/main/java/com/greenezone/gzphotoeditor/editor/ProjectStore.kt#L94-L102) through [PhotoProjectFiles.readRecord 84–96](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/main/java/com/greenezone/gzphotoeditor/editor/PhotoProjectFiles.kt#L84-L96) and [decodeRecord 68–75](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/main/java/com/greenezone/gzphotoeditor/editor/ProjectStore.kt#L68-L75). Preserve hash/version validation and synchronization semantics; their cost has not yet been established as the cause.

The authoritative acceptance remains **650 ms for every Open**, **300 ms warm median**, **64 MiB growth per Open**, one decode and exact content markers. List, migration and heap-budget assertions remain unchanged. No sample deletion, average substitution, CPU-time substitution, threshold change, fixture reduction, forced per-Open GC, skipped assertion or C++/storage rewrite is authorized by this diagnosis.

### Branch on evidence, then stop the diagnostic loop

- If the trace identifies application allocation/parsing/I/O/lock cost that explains the delay, record the specific path and make the smallest evidence-backed repair under that path's existing custody/compatibility contract. Name owned files before editing. Use the unchanged fixture to verify the repair; retain all original failures.
- If the trace demonstrates a concrete environment/runtime scheduling condition, record it and correct only that condition without upgrading the named heap/device tier, bypassing limits or changing the workload. A contemporaneous GC line alone is insufficient.
- A passing targeted run is diagnostic only: fresh-process state differs from the preceding complete suite. If the original suite context remains the concrete unresolved variable, allow **one** traced execution at the original full-suite position with its normal preceding work. This is a conditional, hypothesis-driven diagnostic, not a retry-until-green loop.
- If those bounded diagnostics still provide no attribution/disposition, keep the hold and record the precise unresolved evidence and next technical hypothesis. Do not manufacture a production patch or promote the APK because one later run passed.

### Qualification and delivery exits

After a supported correction or diagnostic disposition, qualify the final exact candidate and exact application merge with the existing JVM/build/signing, complete API29/API35 suites, all **303 inherited + ten correction identities** (plus any justified additions), unchanged assertions/budgets and both separate real force-stop/new-PID lanes. Retain still-applicable successful receipts for the identical head/inputs; do not rerun a passed lane merely because this documentation review occurred. Changed application/test/build/CI inputs need their required final qualification. A targeted or profiled pass alone never substitutes for the full gates, and an original failed result stays in the ledger.

Keep PR #50 draft until the candidate's gates and independent review are complete. Only after the exact merge also qualifies should the final signed handset APK be identified, checksum-verified and recommended. Do not substitute the unqualified v27 debug artifact or the older v26 artifact as the corrected build.

Return one finite receipt: exact heads/trees, diagnostic command/configuration, trace/artifact links and hashes, all raw samples, causal conclusion versus remaining uncertainty, minimal diff (or justified no-code disposition), candidate/merge qualification and the next action. No new 25/50-question cycle or master roadmap.

## Following scope and phone work

Once this qualification disposition and focused review are complete, remaining **R1.2** continues under the existing roadmap: acknowledged atomic drafts, bounded retained history, actual process recovery of document/source/baselines/route/child state across remaining workspace owners. Preserve the existing Regular/Pro native lineage implementation and record its known Bring-only derivative/finish result-delivery concern within that bounded scope. Do not mark all of R1.2 complete because the U1 process lane passes.

After a qualified correction APK exists, use only the ledger's focused phone script: linked Regular→Pro return, Layers Select / mask and Clone / repair with actual gestures, selected layer, Undo/Redo, Save and editable reopen, plus one landscape/large-text pass over shared controls. Engineers own injected cancellation, failed Open and force-stop checks. Preserve the owner's unmentioned prior phone passes.

B2/F03's real GIF workload/numerical misses, RV5, all 77 capability families, full R1–R11 child scope, broader device/accessibility checks and formal owner gates remain open at their existing boundaries. Four owner workspace locks precede Video; all-five editing acceptance precedes final English/Spanish onboarding; finished app and Greenezone website precede Play Console; public publication needs explicit owner approval. Product remains fully offline, no runtime AI/ML/ads/subscription/IAP/watermark, $2.99 once.

This review changes documentation only and provides the next bounded diagnostic; it does not approve a release or claim handset acceptance.
