# R1.1 selected Open — diagnostic review and autonomous repair

Date: November 27, 2026 (America/Denver).

## Decision and next action

**Open a scoped selected-record Open performance repair now. Codex owns it through evidence, implementation, verification, independent review and qualified merge.** The source contains a specific repeated numeric-conversion path and large intermediate recipe copies worth measuring. This is a concrete engineering starting point; it is not proof that decoding caused either the original 717.745 ms miss or the latest 704.840 ms miss.

The final paired scheduling investigation is complete. Do not send the owner another choice between investigating the same scheduler question and doing nothing. The corrected handset APK remains held until the existing qualification gates pass. The next work improves real selected-Open cost and headroom while preserving correctness and every established limit.

Read current [AGENTS](../../AGENTS.md), [WORK_LEDGER](../WORK_LEDGER.md), [product specification](../PRODUCT_SPEC.md), [workspace contract](../WORKSPACE_UX_BLUEPRINT.md), [roadmap](../GZ_ENGINEERING_ROADMAP.md), [capability matrix](../CAPABILITY_MATRIX.md), [release gates](../RELEASE_GATE.md) and [owner Pro scope addendum](OWNER_PRO_SCOPE_ADDENDUM_2026-11-27.md). Preserve newer authorized work. This is a bounded execution handoff under the single roadmap, not another master plan.

## 1. Owner-directed engineering autonomy

During this review, Lance explicitly instructed ChatGPT to let Codex act as an engineer and keep working until problems are solved. Repeated owner-mediated handoffs between ordinary technical steps waste his usage and time.

Apply that instruction now and to subsequent authorized engineering:

- Own the complete agreed work package. Diagnose, obtain targeted evidence, select a justified repair, implement it, investigate failures, correct the implementation, run necessary verification, obtain independent technical review, merge under the existing authority and verify the exact merged result.
- Routine implementation choices, a rejected hypothesis, a recoverable setup problem, a failed test or a minor evidence gap do **not** automatically require another owner/ChatGPT relay. Give concise progress updates and keep working.
- Useful targeted profiling, comparative measurements and validation after a meaningful correction are authorized. There is no arbitrary one-attempt cap on engineering iterations. Each new experiment must resolve a concrete uncertainty or validate changed work; record that purpose and preserve its result.
- Do not retry an unchanged failure until green, weaken a gate, discard failure evidence, manufacture attribution or continue a hypothesis already disproved. When an approach fails, change the supported approach.
- Escalate only an actual product/scope decision reserved to Lance, missing essential access/input with no authorized alternative, a higher-priority restriction, or a material engineering limitation that cannot responsibly be resolved within the agreed scope. Identify the exact blocked action and continue independent authorized work.
- A minor missing tool-provenance field should be recovered from surviving evidence, independently checked where useful, or accurately recorded as unknown. It is not an automatic reason to restart an application experiment or ask Lance to relay another prompt.

This supersedes the previous requirement to stop for another relay merely because the one permitted scheduling capture did not establish a repair. That capture's one-execution allowance is consumed; it is not a continuing ban on the targeted measurements and tests needed for this newly authorized repair. Another unchanged general paired scheduling capture is not the default next step.

Keep project-local cached instructions aligned to these repository contracts. Do not override higher-priority rules, change global agent configuration or claim that unseen local files were edited.

## 2. Reviewed baselines and evidence

| Role | Exact identity |
| --- | --- |
| Documentation intake / PR #56 merge | `6f1dde78c33782900805ead609d001b37730ed85`, tree `bde41cf2a8f135bd02576c8c88a0631122bade0d` |
| Held application/test baseline / PR #50 | `27406f4aed8efa42a6078e1108c53304d4a0043e`, tree `2c0f8ffab9a8321f8f9f823c9354c26b5a2fb6c8` |
| U1 production correction merge / PR #49 | `b342f0bd656753b3692b741489438c6790d14d8b` |
| Last fully qualified application / PR #46 | `e6321322526521ace13a45c231b25bb08f91c565`, 0.12.14 Test/code26 |
| Paired diagnostic harness | `1d4a3fa78eed3a8c97f9963c8d5318aa97d428d4`, tree `6c84c808d7f5dde822b7ef7e8a16e079c5d8c5e2` |

The diagnostic harness differs from the held candidate only in its disposable workflow and `.github/scripts/r11-paired-scheduling.py`. The application/test checkout was pinned separately. Do not merge that diagnostic replacement workflow into normal qualification.

The initial [bootstrap run 46090706087](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46090706087) failed before emulator launch because its udev option was unsupported. Job 140810690927 confirms that the emulator/preflight step was skipped. The corrected KVM setup resumed the original batch; it did not run the app twice.

The [paired run 46091040023](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46091040023), job 140811730127, passed its no-app preflight and capture postflight and ran the exact method once. **Capture checks passed; the application test failed.**

| Selected phase | Recorded wall time | 650 ms individual gate |
| --- | ---: | --- |
| First / project #12 | 597.460462 ms | Within limit |
| Warm 1 / project #01 | 438.302485 ms | Within limit |
| Warm 2 / project #06 | 468.741368 ms | Within limit |
| Warm 3 / project #12 | 704.840334 ms | Failed |

The recorded warm median is **468.741368 ms**, above **300 ms**. The actual test stopped at Warm 3's individual latency assertion before executing the median assertion. It also stopped before that sample's subsequent heap assertion. Do not describe either later assertion as executed.

All four Opens logged one decode and success, with the expected selected identities/content markers checked before their latency assertions. Twelve dense recipes, 23,881,171 legacy XML bytes, the original GC placement and stopwatch, four metadata lists and the 192 MiB API35 managed heap were retained. The complete nine-phase receipt remains in the [PR #56 ledger](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/6f1dde78c33782900805ead609d001b37730ed85/docs/WORK_LEDGER.md#final-paired-api35-diagnostic-disposition).

### Independent raw-evidence verification

This review checked both artifact ZIP hashes, CRCs and all 55 paired-manifest entries. Independent reconstruction from the saved guest protobuf and runner ftrace, without relying on the original native TraceProcessor executable, reproduced all nine phase state bounds, the vCPU mapping, clock markers and all four selected-phase overlap results.

- Guest reconstruction: 2,507 bundles; 414,453 CPU0 and 366,828 CPU1 switches; no decoded bundle loss/error flags.
- Runner reconstruction: 38,087 saved events; actual KVM entries map guest CPUs 0/1 to host TIDs 3910/3911 in QEMU PID3847, with stable identities and complete relevant coverage.
- Slow Open: at least **360.994399 ms** guest runnable but unscheduled.
- The simultaneous mapped-vCPU off-CPU overlap is **0.121 ms at the saved host timestamp precision** under the observed clock envelope. Conservatively allowing each saved host-state endpoint ±1 microsecond gives an upper bound of **0.129 ms** for the relevant bracket. This precision qualification leaves the conclusion unchanged.
- The mapped-running guest intervals' possible host-runnable overlaps are 0.351 ms / 0.219 ms at saved precision. These are not app CPU-time measurements or a claim that every form of host influence was excluded.

The simultaneous-off intervals inside the expanded possible whole-phase host bracket `[331879371967, 332627072435]` ns are:

| Runner CLOCK_MONOTONIC start, ns | End, ns | Saved duration | With ±1 microsecond endpoints |
| ---: | ---: | ---: | ---: |
| 332131859000 | 332131863000 | 4 microseconds | 6 microseconds |
| 332356026000 | 332356029000 | 3 microseconds | 5 microseconds |
| 332463882000 | 332463908000 | 26 microseconds | 28 microseconds |
| 332618024000 | 332618112000 | 88 microseconds | 90 microseconds |
| Total | | 121 microseconds | 129 microseconds |

This whole-bracket upper bound does not require choosing a particular constant offset or finer guest-ready segmentation. It remains a runner-clock duration conditional on the actual phase mapping staying within the observed envelope.

Thus the capture weighs against a large simultaneous runner-host vCPU unavailability gap as the explanation of this particular guest-ready interval. It does not establish a specific app defect, a reproducible environment fix, or the original untraced miss's cause. Guest service occupancy and ART overlap remain context, not additive causal delay.

The **40.602 ms observed clock-envelope width** and inability to rule out unobserved interior drift remain explicit. The independent reconstruction verifies the saved data and conditional calculations; it does not remove those experimental limits. Preflight/boot age, four ADB metadata reads and underlying hypervisor visibility remain qualified as in the receipt.

### Provenance gap: original native TraceProcessor

Codex correctly disclosed that the original native TraceProcessor executable's separate hash was not recorded. The Python package version, capture script, ADB and guest Perfetto identities were recorded. Therefore do not claim every tool-provenance requirement was satisfied.

Recover the original native reader identity only if it survives in existing runner/cache/log evidence. A later download is not proof of the earlier binary. Otherwise retain the gap. Independent raw reconstruction supports the scheduling numbers without pretending to repair the missing historical identity. **No application rerun is needed for this provenance field.**

### Exact retained artifacts

- [Bootstrap archive 15240774262](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46090706087/artifacts/15240774262): SHA-256 `f837492697790243a307933a04067c403445b5d3933e4810a075f2676a892397`.
- [Paired/preflight archive 15241238298](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46091040023/artifacts/15241238298): SHA-256 `f6254becb8f571b88954c8c6939371639f87d13513183b5fe3c906f63905cadb`.
- Paired guest trace: `3e674d0b31b713f5ded3d94e8bb7d694377dcfa79e9ac667056159c5398fe7df`.
- Paired host trace: `6f3593ec1b872fa08dc1ea22bdd2484980718865da8266997a7af12981e54c5f`.
- Paired phase JSON: `c369c16590f588749e7b7a044b7af404bf27cbadd54f9082eb70d2251468cce6`.

These are diagnostic evidence, not a qualified handset artifact.

## 3. Specific source evidence that opens the repair

All references in this section are pinned to held candidate `27406f4aed8efa42a6078e1108c53304d4a0043e`.

### Primary candidate: repeated ink-coordinate fallback conversion

[EditDocumentCodec.kt lines 186–227](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/main/java/com/greenezone/gzphotoeditor/editor/EditDocumentCodec.kt#L186-L227) parses every retained ink point. Its numeric fast path accepts only `0.` followed by one through nine decimal digits. Other forms call `substring(start, end).toFloatOrNull()` per coordinate. Scientific notation and longer decimals cannot take the restricted path.

The [unchanged dense fixture, lines 220–245](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidRegularProjectListAcceptanceTest.kt#L220-L245) uses `.00012345678f` and `.0034567893f`, 500-point strokes and 100–160 strokes near the 1,990,000-character recipe target. The writer serializes coordinates through `Float.toString()` at [codec lines 166–175](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/main/java/com/greenezone/gzphotoeditor/editor/EditDocumentCodec.kt#L166-L175).

This identifies repeated source-level conversion/copy work inside selected Open. **Record the actual emitted tokens and fallback counts on the resolved runtime before claiming an exact count or allocation attribution.** Do not infer regex overhead from historical Kotlin implementations or infer the runtime stdlib version from a Compose plugin version.

### Secondary candidate: large intermediate recipe strings

The selected path reads the entire bounded UTF-8 record in [PhotoProjectFiles.kt lines 84–96](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/main/java/com/greenezone/gzphotoeditor/editor/PhotoProjectFiles.kt#L84-L96), extracts the recipe substring in [ProjectStore.kt lines 68–75](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/main/java/com/greenezone/gzphotoeditor/editor/ProjectStore.kt#L68-L75), then creates line/key/value strings in [EditDocumentCodec.kt lines 230–235](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/main/java/com/greenezone/gzphotoeditor/editor/EditDocumentCodec.kt#L230-L235). Measure whether eliminating a subset of these copies is useful before broadening the patch.

These source observations justify a focused performance task. Their runtime size and benefit remain to be established. Choose the smallest measured improvement; the instruction is not to force a particular parser implementation.

### Integrity work is not redundant payload reading

[NamedProjectIdentity.kt lines 139–191](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/main/java/com/greenezone/gzphotoeditor/editor/NamedProjectIdentity.kt#L139-L191) already calculates SHA-256 while reading through a bounded digest stream. It preserves publication/backup recovery, detects truncation/growth, completes the digest on early parser termination and checks final length. The selected row's stored version must still match before exposing content.

There is no basis here to remove hashing, skip recovery or trust cached decoded content. Keep exact selected identity/version, selected-only decoding and source bytes intact.

## 4. Complete engineering package

### Baseline and ownership

Use the held application's/test tree above as the behavioral baseline. Integrate current documentation and preserve the three PR #50 instrumentation fixes plus all newer authorized work. Codex may choose the safe branch/merge strategy and resolve ordinary documentation conflicts without another owner relay. Retain a precise source/tree mapping; never accidentally adopt the disposable diagnostic workflow.

The primary production scope is `EditDocumentCodec.kt`. Expand to `PhotoProjectFiles.kt` / `ProjectStore.kt` only if evidence supports the selected read/copy cost. Relevant codec/recipe tests and necessary internal measurement support are in scope. One writer owns each shared file. This task does not authorize unrelated UI changes, a storage-format migration, source overwrite, a renderer rewrite, a new C++ engine or unrelated feature removal.

### A. Establish attributable cost, then choose the repair

Use the existing saved traces for scheduling context. Inspect the resolved runtime and actual serialized fixture. Obtain the minimum useful stage/CPU/allocation/call-count evidence for selected record read, recipe extraction and decoding. Separate process-wide allocation counters from method attribution. Temporary instrumentation is diagnostic evidence and must not change the final acceptance stopwatch or pollute shipped user flows.

State the candidate cost and mechanism before modifying production. A source-proven repeated conversion path is a starting hypothesis, not permission to claim it caused all off-CPU time. If measurement rejects it, investigate the next concrete read/decode cost within this package. Continue that engineering decision without returning to Lance for permission.

### B. Implement a general, compatible improvement

Preserve saved recipes and decoded state exactly. A bounded range parser or reduced intermediate copying may be appropriate if the evidence supports it. Retain a correct general fallback for forms outside any proven fast subset.

Do not merely enlarge the decimal divisor array, overflow an integer accumulator, or assume conversion through Double always rounds like direct Float parsing. Preserve signed zero, boundary/tie behavior, scientific/hex/suffixed/whitespace forms, underflow, finite/range checks and malformed pairs.

Do not special-case the fixture's two values, reduce point density/precision, share mutable document graphs, predecode all twelve records or cache selected Opens to satisfy this benchmark.

If changing field scanning, preserve the physical 6,000-line boundary, first-equals splitting, duplicate-key last-wins behavior, newline/version/unknown-field compatibility and the separately bounded legacy Pro recovery path. If these paths need not change, leave them alone.

### C. Verify meaningful correctness and improvement

Retain [RegularInkRecipeIntegrityTest](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/test/java/com/greenezone/gzphotoeditor/editor/RegularInkRecipeIntegrityTest.kt) and the inherited recipe, drawing, migration, rollback, identity, native-lineage and output protections.

Add only tests needed for the chosen implementation's actual risks. Use independent expected float bits and the established general conversion where appropriate; cover varied real/canonical and legacy tokens, boundary cases, complete decoded documents, retained geometry/history/output and old-project compatibility. Preserve the first **500 raw tokens**, including invalid/empty tokens; do not reinterpret this as the first 500 valid points.

Compare baseline and repaired attributable work on declared inputs/tier and keep all observations. Use varied supported recipes as well as the unchanged dense fixture so a fixture-specific shortcut cannot masquerade as an improvement. A helper speedup alone does not qualify the user-visible Open.

Run targeted tests during implementation, then complete qualification at the ready candidate and exact merge. Do not run the entire app suite for every small experiment or repeat settled audits with no concrete remaining risk.

### D. Preserve the acceptance gates

The [complete R1.1 method](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/27406f4aed8efa42a6078e1108c53304d4a0043e/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidRegularProjectListAcceptanceTest.kt) stays unchanged for final acceptance:

- Twelve near-budget recipes; migration; four zero-decode metadata lists; selected order #12/#01/#06/#12.
- Original stopwatch, GC placement, identity/source/exposure/point checks, one selected decode and zero legacy-access assertions.
- **650 ms each selected Open; 300 ms warm median; 64 MiB selected managed growth.**
- Existing migration **12 s / 112 MiB** and list **150 ms / 8 MiB** limits; API35 **192 MiB** and API29 **256 MiB** managed heaps; same emulator/workload tier.
- No scheduling/GC subtraction, service/keyboard disabling, favorable-idle selection, forced per-Open GC, bigger heap/machine or weaker assertion to obtain a pass.

If a changed implementation fails, use its evidence to make the next supported correction and reverify. An unchanged blind rerun is not a repair. Original untraced causality is not a permanent requirement; prove the prospective improvement and meet the actual gates.

Final unprofiled exact-candidate and exact-merge qualification retains JVM/build/signing, full API29/API35 suites, all **303 inherited plus ten U1 correction identities** and meaningful assertions, plus justified additions. Preserve the separate actual force-stop/new-PID recovery, both named Open routes and durable native layer edits. Prior exact-head passes retain only their applicable scope; changed inputs require corresponding new final evidence.

Independent review is part of Codex's engineering process and need not become an owner relay after every patch. Repair substantiated review findings before merging and verify the actual merged tree. Keep PR #50/its successor and the corrected handset delivery held until the required result exists.

## 5. Completion and continuation

Complete this package when:

1. The selected read/decode cost and the chosen improvement are supported by specific evidence; compatibility and source/identity safety remain intact.
2. The unchanged full qualification passes at the exact final candidate and merged application, with failures, test inventory and actual process recovery preserved.
3. Independent review is completed and its required corrections resolved.
4. The ledger identifies exact source/tree, changes, before/after evidence, qualification runs, remaining limitations and the checksum/signature-verified merged APK.
5. The owner receives one consolidated engineering result and a short focused handset script for affected behavior, without repeating earlier successful phone instructions.

A qualified intermediate APK remains a focused test build; Pro remains unfinished and unaccepted as the final professional editor. Do not convert this repair into an overall completion claim.

Then continue the ledger's remaining R1.2 and subsequent authorized roadmap work using the same autonomy rule. Missing handset observations hold only their affected acceptance, not all independent development. Preserve all 77 families and required children, B2/F03, RV5, full R2–R4 professional Pro scope, the four explicit pre-Video locks, all-five editing acceptance before final EN/ES onboarding, and finished app plus Greenezone website before Console. Public publication remains an owner decision.

This independent review and dispatch changes documentation only. No application source/test/build/CI input was modified, no application was executed and no APK was rebuilt by this review.
