# GZ Editor Pro — single active work ledger

## Selected task A1 — owner phone report and ChatGPT / Codex alignment

**Phase: Stage 2 complete — awaiting ChatGPT answers. Application engineering is paused by the owner's explicit three-stage instruction.** Source inspection and coordination documentation are allowed; app/test/build/CI changes, APK rebuilds, new suite runs and R1.1/R1.2 implementation wait for the completed exchange. Read the [owner phone report and Codex alignment handoff](CODEX_ALIGNMENT_HANDOFF_2026-11-24.md) in full.

| Stage | Owner | Required result | State |
| --- | --- | --- | --- |
| 1 | ChatGPT | Record phone findings, UI rules, full-scope continuity and Q01–Q25; publish the Codex activation prompt | Complete — PR #34, intake main `b9b5ff14ec5a833416b972c4b670a7e9157a2e0a` |
| 2 | Codex | Answer Q01–Q25; provide full existing roadmap snapshot and 77-family reconciliation; author CQ01–CQ50; commit response and return prompt | Complete — committed responses linked below; no application changes |
| 3 | ChatGPT | Read actual Codex response; answer CQ01–CQ50; reconcile canonical documents; publish agreed batch and Codex resume prompt | Awaiting owner-relayed ChatGPT activation; engineering remains paused |

Stage 2 is complete. Read the committed [Q01–Q25 response, full frozen roadmap snapshots and 77-family reconciliation](coordination/CODEX_RESPONSE_TO_25_2026-11-24.md) and [CQ01–CQ50](coordination/CODEX_QUESTIONS_50_2026-11-24.md). Exact reviewed response content ref: `37023ada665a6a13e81791efae77080a8ad93291`; documentation branch `codex/stage2-alignment-20261124`. These are comparison snapshots and proposed decisions, not another active master plan or an engineering-resume instruction.

Validation: 25 ordered answers; 77 unique family rows with retained full exits; exactly 50 ordered questions; complete frozen public plan/child texts at both `e04bc08a4800233b0f407b419a91c97ca2c068f6` and `eb8796c92fa654cb0dac371e35d7645ecd07db8a` (all 18 source identities verified by recomputed UTF-8 Git blob SHA-1; identical files shared). Read-only source and documentation review identified specific route/menu/timing gaps. No app/test/build/CI modifications, APK rebuild/download or suite rerun; PR #31's application/binary and recorded 193-per-API candidate/merge evidence remain unchanged.

**Next action belongs to ChatGPT after Lance relays the return prompt:** read the actual committed documents in full, answer every CQ in `docs/coordination/CHATGPT_RESPONSE_TO_50_2026-11-24.md`, reconcile the single canonical documents, then create `docs/coordination/ENGINEERING_RESUME_2026-11-24.md` with exact baseline, bounded batch, ownership, exits and genuine blockers. Only after Codex reads those answers and the owner's final relayed resume instruction may application engineering continue. Do not prefill ChatGPT's answers, poll its chat, substitute internal reviewers for Stage 3 or require another arbitrary 25/50 cycle.

### Owner phone result and bounded follow-up

Lance reports **unmentioned supplied phone checks worked**. Record those as owner-reported pass and retain all PR #29/#31 evidence. The new exceptions/requests are:

| Finding | Owner-observed result or request | Relevant existing families |
| --- | --- | --- |
| F01 | Collage requires quick double tap; restore one-tap hide/show while preserving active tools and guides | S04, C03 |
| F02 | Regular/Pro/Collage exit lacks expected unsaved-work protection; reconcile every departure and child transaction | S01, S03, S04, S05, C03 |
| F03 | GIF Save approximately 2–5 minutes on phone; menus/options work | G02, G08, S05 |
| F04 | One clearly titled top-right Options dropdown for New/Open media, Open projects, Save project, Export, Help; eliminate duplicate major command locations | S08, Q01 |
| F05 | Remove Regular More entirely and its Create collage and Histogram entries; preserve other commands and underlying compatible processing | S08, R03 |
| F06 | Common header/style/control layout and consistent Undo/Redo requested; shared arrow icons selected in this handoff for current workspaces and future gated Video | S08, Q01 |

Device/Android version and GIF workload/settings/provider/timing stages were not supplied. The physical performance report remains open even though the small CI fixture passed. Scoped phone success does not lock whole workspaces, establish true process-loss recovery or authorize release.

**Proposed post-alignment priority:** resolve the named phone defects and common UI contract in a dependency-aware wave, then continue R1.1/R1.2 and the existing full roadmap. Stage 3 fixes the exact batch and any justified durability prerequisite; no implementation is authorized by this proposal alone. The new owner report is the specific reason for reopening affected touch/navigation/performance behavior. It is not a reason to repeat completed audits.

Updated 2026-11-24. Read with [roadmap](GZ_ENGINEERING_ROADMAP.md), [capability inventory](CAPABILITY_MATRIX.md) and [release gates](RELEASE_GATE.md). This ledger controls execution order; the linked engineering records control run results. Historical green counts do not close newly reported defects.

## Qualified application baseline — PR #31 engineering complete

[PR #31](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/31) is merged at application commit `3ad65c533f7667a136d4fa1838afcc06d3f484da`. It shares tree `1c7e3007204192ea240a7fd8d65cbd00cf7f9511` with candidate `aa86c37c64c5e395d5378df281a0c7583a39d0e0`. [Candidate run 45480001019](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45480001019) and [merged-main run 45483297512](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45483297512) each pass JVM/assembly/signing and **193/193 on API29 and API35**, zero failures/errors/skips. The current combined phone candidate is **0.12.10 Test / code 22 / phonetest.v22**. [Raw receipts, exact APK, retained failures and phone checklist](ERASER_REPAIR_VERIFICATION.md).

Later documentation-only commits reconcile this state without replacing that tested application commit or APK. The phone report above supplies owner-reported passes with named exceptions. The complete Draw/GIF families, all workspace locks and commercial release remain open. R14 is PARTIAL, not complete. The four original PR #29 corrections remain credited; its 0.12.9 artifact is historical and superseded for current delivery.

### Completed scoped follow-up — R29-01/R29-02 and observed GIF Save failure

**Reopening reason and authority:** Lance supplied the independent 0.12.9 review and explicitly requested repository fixes. A supported 160-stroke erase could omit unrelated artwork, and the bounded Draw sampler exposed visible segments the point-only Eraser could not cut. These named failures justified the follow-up without reopening the full blueprint.

**Delivered result:** Regular and the selected Collage-tile Eraser cut visible segments and the swept brush path. First/later capacity overflow restores the entire pre-gesture document, dirty work and history with feedback through the existing notice. Named saves reject invalid ink cardinality rather than encoding a prefix. Five actual UI regressions verify seeded boundaries, long Draw→Erase, cancellation, history, dirty-state Back, named reopen, decoded PNG and full-parent duplicate-source Collage isolation. All original 186 Android case identities remain.

**Qualification extension, resolved:** candidate 1 passed the Eraser cases on both APIs but failed the unchanged API29 portrait GIF Save gate at 28,331 ms. That failure was preserved and diagnosed; the application was not retried unchanged. A bounded exact `(sourceARGB, computedDetail.rawBits)` export cache and stage measurements were added. Two actual Android bitmap-equivalence cases retain full pixels across changing neighborhoods/grades. The 6-second sampled-motion and 15-second Save gates, source fixture, 180×320 canvas, 88 frames and complete delays remain unchanged. Candidate and merged-main now pass those gates. The old timing counters cannot retrospectively assign the entire failed interval to one stage; physical performance remains a separate gate.

**Scoped production files:** `InkMath.kt`, `PhotoEditorScreen.kt`, `EditDocumentCodec.kt`, `ImagePipeline.kt`, `GifEncoder.kt`, `VideoGifSource.kt`, and test version. The one observed rotation-harness fault was corrected through its rule import only. No layout/style, mode ownership, Pro production, preview routing, schema, dependency or workflow change. Baseline main was `e04bc08a4800233b0f407b419a91c97ca2c068f6`, with the same application as PR #29 merge `c9efc5aa0673f21b98f150733582a24996b34d10`.

### Queued durability item — R1.1 bounded saved-project listing and loading

**Observable result:** opening the saved-project list reads lightweight metadata and decodes only the selected recipe. A full twelve-save set containing near-budget recipes and failed/corrupt entries remains bounded in memory; selection opens the intended project and existing saved bytes remain intact.

Inventory the current state/source ownership once, pin the current main and compare app/test/Gradle/CI inputs against the qualified application, identify affected files, and define the decisive fault experiment before editing. Preserve saved projects through any required storage migration. Preserve list behavior and unrelated working UI while applying the owner's specified common chrome after alignment; do not restart blueprint intake or reopen the completed repair wave without a new reason. R1.1 is queued and has not been implemented by PR #31.

**Following item R1.2 — recoverable Regular working drafts.** Persist completed edits atomically, keep transient gestures out of checkpoints, retain source identity/grants and restore selected work after genuine process loss. Carry the same contracts to Pro/Collage/GIF in subsequent R1 slices. The reported 0.12.10 phone checks supply scoped owner evidence with named exceptions; they do not imply all workspaces are accepted.

## Completed wave R0 — independent PR #26 corrections

**Owner:** root integration, with distinct Regular gesture, storage and Pro selection owners. **App baseline:** `372e0e947caacb2ceaf603bc4ff5a60e44cc33ec`. **Intake main:** `d2a8f4de41839e25ad54060be803746d7307bee8` (documentation-only difference). The owner supplied the review and authorized engineering. The former successor read-only hold is lifted.

| Item | Required behavior | Production scope | Decisive evidence |
| --- | --- | --- | --- |
| D01 | A newly drawn privacy cover remains over the same source pixels during the first manual Crop; held gesture survives recomposition | PhotoEditorScreen preview identity and crop anchoring | Actual held/quick pointer flow, Undo/Redo, reopen, PNG and selected-tile isolation |
| D02 | Oversized Regular save/export rejects before storage mutation or destination picker; healthy save and dirty work survive | ProjectStore; PhotoExportController; actionable save error | Allowed dense draw fixture, last healthy bytes unchanged, readable smaller recipe; [failed Save-before-close/replace keeps the dirty editor open](../app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidRegularSaveBudgetTest.kt) |
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

**Retained state transition:** PR #29 closed its original R0 reproductions. The newly demonstrated Eraser failures reopened only that behavior and are now engineering-verified in PR #31, together with the observed GIF Save correction. R1.1 remains the next durability item after the current alignment and agreed corrections. Reopen a settled behavior only for a named new failure, changed dependency, weaker evidence or owner report; record that reason. The latest owner report is recorded above.

**Preserve:** all original 174 Android cases, established GIF performance/fidelity budgets, mode boundaries, named project identity, immutable picker recipes, all source files and independent Collage tiles. Historical version 0.12.9 / code 21 isolated that phone test from prior builds. Current 0.12.10 / code 22 is also a separate test package; neither proves same-ID upgrade behavior.

## Ordered remaining work

| Order | Package / completion target | Prerequisite and current disposition |
| --- | --- | --- |
| R1 | Durable documents, lazy project metadata loading, process-loss recovery, source/provider custody, Back/history, saved repeated exchange and portable bundles | QUEUED — A1 alignment and agreed owner-correction sequence precede execution; preserve R1.1 then R1.2; S01–S08 remain open/partial |
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

## Next action — Codex Stage 2 response

Read the current alignment handoff, answer its 25 questions with evidence, return your complete existing roadmap, and author exactly 50 questions for ChatGPT. Commit the response documents, update A1 above and give Lance the return prompt specified in the handoff. Stop before application engineering. R1.1/R1.2 remain queued; their implementation and the new owner corrections begin only after Stage 3 and the final resume instruction. Preserve the qualified 0.12.10 artifact and all existing receipts. Do not rerun settled qualification or create another master backlog.

## Pending owner/device decisions

- The 0.12.10 Test phone report has been received: unmentioned supplied checks worked; F01–F03 and the F04–F06 UI requests remain open. Follow the alignment handoff. Do not ask Lance to repeat the whole successful script or draw 160 strokes by hand. Any later handset request must identify the remaining device-specific observation or a concrete regression risk.
- Four formal pre-Video workspace locks remain open; a limited successful phone session does not close the full product inventory.
- Shipping device/performance, RAW-camera and interchange support tiers are explicit later decisions, informed by implementation evidence.
- Greenezone website completion is an external dependency, not app-engineering scope. Play Console setup waits for that plus the functioning app; no account work now.
- Exact production signing/account/listing and public release need Lance at R11. After the explicit A1 alignment hold is released, routine agreed engineering continues without repeated permission.

## Failure and drift rule

For a failed check, record the exact symptom and phase, causal hypothesis and bounded next correction. Do not retry unchanged application failures, relax an established budget for green, replay passed intake, or call a helper result full workflow acceptance. Reopen settled work only for a new failure, relevant dependency change, weaker evidence or an owner report, with the reason recorded.
