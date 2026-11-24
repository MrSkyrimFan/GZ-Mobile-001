# GZ Editor Pro — single active work ledger

Updated 2026-11-24. Read with [roadmap](GZ_ENGINEERING_ROADMAP.md), [capability inventory](CAPABILITY_MATRIX.md) and [release gates](RELEASE_GATE.md). This ledger controls execution order; the linked engineering records control run results. Historical green counts do not close newly reported defects.

## Current state — focused Eraser correction active; R1 follows

[PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29), app merge `c9efc5aa0673f21b98f150733582a24996b34d10`, candidate `6ad22e3fabe3964a662c7e0e39bd08c4a8e8f9ca`, [candidate workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45352950946) and [merged-main workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45355074845). Both source trees match, and each full candidate/merge Android lane passed 186/186 with zero failures/errors/skips. Verify/JVM/assembly/signature checks passed. The exact exercised phone APK and its hash/package/certificate are in the PR execution receipt.

The four review defects are corrected. Additional qualification repairs cover broad late-corner sampling, bounded legacy Pro reading, and the observed Compose test scheduler issue. These results do not close handset acceptance, full feature families, smallest-heap qualification or commercial release.

**Active item: R0 Eraser follow-up — R29-01/R29-02.** Lance supplied the independent 0.12.9 review and explicitly asked this assistant to fix the repository. Reopening reason: a supported 160-stroke erase can omit unrelated artwork; the new sampler exposes visible segments the point-only eraser cannot hit. The four original regression repairs remain credited. Do not issue another combined phone candidate until this focused correction is qualified.

**Observable result:** Regular and selected Collage-tile Eraser cuts the displayed line, including between sparse retained vertices, without losing unrelated strokes. First/later capacity overflow restores the entire gesture, dirty work and history, with visible feedback through existing controls. Named saves reject over-capacity ink rather than encoding an incomplete prefix.

**Baseline:** main `e04bc08a4800233b0f407b419a91c97ca2c068f6`, same application as `c9efc5aa0673f21b98f150733582a24996b34d10`. **Owners:** root UI/codec/integration; distinct geometry/JVM and Android-regression implementers; independent review. **Production scope:** InkMath.kt, PhotoEditorScreen.kt, EditDocumentCodec.kt, test version. No layout, mode, GIF, Pro, dependency or workflow changes.

**Reproduction and decisive check:** preserve test-only red evidence on unchanged production; seed 160/159-stroke boundaries and run the actual Eraser UI, long Draw→Erase, history, dirty-state Back, named reopen, external PNG decode and full parent Collage isolation. Retain all 186 existing Android cases and GIF budgets. Final candidate and merge require JVM/API29/API35, exact source tree and exercised APK identity. [Execution evidence](ERASER_REPAIR_VERIFICATION.md).

**Next planned item after this correction: R1.1 — bounded saved-project listing and loading.** Inventory current state/source ownership once, then load lightweight project metadata and decode only the selected recipe. Qualify a full twelve-save set containing near-budget recipes, failed/corrupt entries, correct selected identity and unchanged saved bytes, with measured memory. Preserve the existing saved projects through any required storage migration. Pin the baseline, affected files and decisive fault experiment before editing; do not restart blueprint intake.

**Following item R1.2 — recoverable Regular working drafts.** Persist completed edits atomically, keep transient gestures out of checkpoints, retain source identity/grants and restore the selected work after genuine process loss. Carry the same contracts to Pro/Collage/GIF in subsequent R1 slices.

## Completed wave R0 — independent PR #26 corrections

**Owner:** root integration, with distinct Regular gesture, storage and Pro selection owners. **App baseline:** `372e0e947caacb2ceaf603bc4ff5a60e44cc33ec`. **Intake main:** `d2a8f4de41839e25ad54060be803746d7307bee8` (documentation-only difference). The owner supplied the review and authorized engineering. The former successor read-only hold is lifted.

| Item | Required behavior | Production scope | Decisive evidence |
| --- | --- | --- | --- |
| D01 | A newly drawn privacy cover remains over the same source pixels during the first manual Crop; held gesture survives recomposition | PhotoEditorScreen preview identity and crop anchoring | Actual held/quick pointer flow, Undo/Redo, reopen, PNG and selected-tile isolation |
| D02 | Oversized Regular save/export rejects before storage mutation or destination picker; healthy save and dirty work survive | ProjectStore; PhotoExportController; actionable save error | Allowed dense draw fixture, last healthy bytes unchanged, readable smaller recipe; actual Save-before-close/replace remains open |
| D03 | Long Regular Draw retains late bends and endpoint within budget; cancellation makes no edit | CreativeCanvasOverlays; PhotoEditorScreen; bounded sampler | Actual >500-sample Regular/tile gestures, saved/exported pixels, cancellation and history |
| D04 | Long Pro lasso represents the full contour rather than a truncated polygon | ProStrokeSampling; ProWorkspaceScreen | Actual >450-sample lasso, late-region applied mask pixels, Undo/Redo/save/reopen; cancellation restores prior selection |

**Test-only baseline:** [commit 018b45c](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/018b45c4b1af0beb867abfe0261f5bce9e4f54e1), [workflow 45348806687](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45348806687). Three new Android test classes add ten cases against unchanged production. Keep actual failures, including any harness defect, distinct from intended product reproductions.

**Corrected candidate / final result:** [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29), [repair branch](https://github.com/MrSkyrimFan/GZ-Mobile-001/tree/feature/pr26-review-corrections) and its pull request, [branch workflows](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions?query=branch%3Afeature%2Fpr26-review-corrections). The PR description is the execution receipt for exact corrected commit, red/green case disposition, full candidate/merge jobs, independent review, APK identity and remaining owner checks. The PR now carries the completed execution receipt; this ledger records the resulting state. Use its exact qualified artifact, not an earlier candidate download.

### Qualification findings retained with their causes

- Initial unchanged-app run 45348806687: the ten new cases reproduced the reported defects. Gesture/lasso cases reached their intended assertions on both APIs; storage cases reached false-success assertions on API 35 and exhausted memory inside the unbounded production encoder on API 29. Android 35 later aborted in a separate existing Collage v1 test-rule scheduler failure; Android 29 also exposed a legacy Pro read allocation failure. Do not describe that run as a clean full-suite reproduction.
- Initial correction run 45349823052 at `a3cf1b66e38034d41d51e35dce31ad2cdf6d9108`: both APIs completed the identical 184-case set, 182 passed/2 failed/0 skipped. All 174 prior cases passed. The two failures retained the endpoint but lost a broad late Draw corner; the assertions were not relaxed.
- Follow-up uses area-aware bounded sampling with explicit reversal protection, keeping the existing 450/500 caps. It also migrates only the observed Collage test and new Regular gesture fixture to the standard v2 scheduler.
- The baseline legacy Pro failure occurred in JSON unescaping: its 7,591,944-byte transient allocation exceeded the 16 MiB heap observed in that run. The merged read optimization reduces copies while preserving format, file/recipe limits, validation, backup behavior and full recovery equality. Later larger-heap success is not evidence that this memory failure disappeared.
- Final changed-candidate and merged-application results, exact artifact identity and any further diagnostic disposition belong in [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29). Preserve all earlier failures. These bounded qualification repairs do not close the broader R1 durability/memory package.

**Prior state transition:** PR #29 closed its original R0 reproductions. The newly demonstrated Eraser failures above reopen only that behavior; R1 follows their correction. Reopen an R0 behavior only for a named new failure, changed dependency, weaker evidence or owner report. Handset acceptance remains separately open.

**Preserve:** all original 174 Android cases, established GIF performance/fidelity budgets, mode boundaries, named project identity, immutable picker recipes, all source files and independent Collage tiles. New version 0.12.9 / code 21 isolates the next phone test from prior builds; separate packages do not prove same-ID upgrade behavior.

## Ordered remaining work

| Order | Package / completion target | Prerequisite and current disposition |
| --- | --- | --- |
| R1 | Durable documents, lazy project metadata loading, process-loss recovery, source/provider custody, Back/history, saved repeated exchange and portable bundles | NEXT after the active Eraser correction — begin R1.1 above; required families S01–S08 remain open/partial |
| R2 | Pro layer tree/groups/clipping and memory-bounded reference compositor | R1 contracts; preserve legacy pixels |
| R3 | Full Pro selection, paint, retouch, shapes, transforms, effects and declared interchange | R2 foundation |
| R4 | Offline Subject & Background, refine/removal and reshaping | R2/R3 mask/geometry foundation; no runtime AI |
| R5 | Regular photographic development, RAW/color/optics and multi-image processing | R1/R2 shared custody/color/geometry; preserve Regular without Masks authoring |
| R6 | Catalog, metadata/virtual copies, durable batch, text/creativity and at least 500 finished stickers | R1 plus qualified processing; stickers no longer omitted after repair stabilization |
| R7 | Full GIF frame/timeline/palette workflow, reversible legacy v4 aspect upgrade and complete Collage qualification | Shared foundations and selected-tile features |
| R8 | Pre-Video device/accessibility/performance qualification and four explicit owner locks | Every required pre-Video family accepted or specifically dispositioned by Lance |
| R9 | Separate complete professional Video workspace | Blocked on all four owner locks, never on CI count alone |
| R10 | Full five-workspace editing acceptance, then English/Spanish welcome/help/legal/acceptance | All editing completed/accepted; consumer onboarding stays last among app features |
| R11 | Finished website, Play Console/account, signed AAB/store testing and authorized release | Fully functioning qualified app AND Greenezone website complete; account not set up |

Subdivide a selected package into bounded deliverables with affected files and observable exits before implementation. Do not treat these family rows as single giant commits, invent completion percentages, silently remove difficult capabilities, or create another master backlog.

## Next action — complete the named Eraser correction

Reproduce and repair R29-01/R29-02 using the scoped contract above. Preserve the prior regression evidence, qualify the final candidate/merge, and deliver the exact replacement APK. Then execute R1.1 already selected above, followed by R1.2. This owner-directed new failure is the recorded reason to reopen the affected behavior; it is not a new blueprint intake.

## Pending owner/device decisions

- Prior v0.12.9 Test handset wave is held by the new Eraser findings. Replacement qualification will cover: fresh-cover Crop, long Draw, Pro lasso, dense Save rejection, prior portrait GIF workflow and physical Back/picker/provider checks. Use the exact APK and focused script in the PR execution receipt.
- Four formal pre-Video workspace locks remain open; a limited successful phone session does not close the full product inventory.
- Shipping device/performance, RAW-camera and interchange support tiers are explicit later decisions, informed by implementation evidence.
- Greenezone website completion is an external dependency, not app-engineering scope. Play Console setup waits for that plus the functioning app; no account work now.
- Exact production signing/account/listing and public release need Lance at R11. Routine authorized engineering continues without repeated permission.

## Failure and drift rule

For a failed check, record the exact symptom and phase, causal hypothesis and bounded next correction. Do not retry unchanged application failures, relax an established budget for green, replay passed intake, or call a helper result full workflow acceptance. Reopen settled work only for a new failure, relevant dependency change, weaker evidence or an owner report, with the reason recorded.
