# GZ Editor Pro — single active work ledger

Updated 2026-11-24. Read with [roadmap](GZ_ENGINEERING_ROADMAP.md), [capability inventory](CAPABILITY_MATRIX.md) and [release gates](RELEASE_GATE.md). This ledger controls execution order; the linked engineering records control run results. Historical green counts do not close newly reported defects.

## Active wave R0 — independent PR #26 corrections

**Owner:** root integration, with distinct Regular gesture, storage and Pro selection owners. **App baseline:** `372e0e947caacb2ceaf603bc4ff5a60e44cc33ec`. **Intake main:** `d2a8f4de41839e25ad54060be803746d7307bee8` (documentation-only difference). The owner supplied the review and authorized engineering. The former successor read-only hold is lifted.

| Item | Required behavior | Production scope | Decisive evidence |
| --- | --- | --- | --- |
| D01 | A newly drawn privacy cover remains over the same source pixels during the first manual Crop; held gesture survives recomposition | PhotoEditorScreen preview identity and crop anchoring | Actual held/quick pointer flow, Undo/Redo, reopen, PNG and selected-tile isolation |
| D02 | Oversized Regular save/export rejects before storage mutation or destination picker; healthy save and dirty work survive | ProjectStore; PhotoExportController; actionable save error | Allowed dense draw fixture, last healthy bytes unchanged, readable smaller recipe; actual Save-before-close/replace remains open |
| D03 | Long Regular Draw retains late bends and endpoint within budget; cancellation makes no edit | CreativeCanvasOverlays; PhotoEditorScreen; bounded sampler | Actual >500-sample Regular/tile gestures, saved/exported pixels, cancellation and history |
| D04 | Long Pro lasso represents the full contour rather than a truncated polygon | ProStrokeSampling; ProWorkspaceScreen | Actual >450-sample lasso, late-region applied mask pixels, Undo/Redo/save/reopen; cancellation restores prior selection |

**Test-only baseline:** [commit 018b45c](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/018b45c4b1af0beb867abfe0261f5bce9e4f54e1), [workflow 45348806687](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45348806687). Three new Android test classes add ten cases against unchanged production. Keep actual failures, including any harness defect, distinct from intended product reproductions.

**Corrected candidate / final result:** [repair branch](https://github.com/MrSkyrimFan/GZ-Mobile-001/tree/feature/pr26-review-corrections) and its pull request, [branch workflows](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions?query=branch%3Afeature%2Fpr26-review-corrections). The PR description is the execution receipt for exact corrected commit, red/green case disposition, full candidate/merge jobs, independent review, APK identity and remaining owner checks. This document is a work contract, not a predeclared pass. Do not recommend an artifact until those receipts pass.

**State transition:** R0 is engineering-complete only when all four corrected behaviors have appropriate regression evidence, full JVM/API29/API35 candidate and merged application results, and a verified exercised phone APK. If the linked PR is not yet merged with those receipts, finish R0. If it is, proceed to R1; do not repeat R0 intake or rerun unchanged tests. Handset acceptance remains separately open.

**Preserve:** all original 174 Android cases, established GIF performance/fidelity budgets, mode boundaries, named project identity, immutable picker recipes, all source files and independent Collage tiles. New version 0.12.9 / code 21 isolates the next phone test from prior builds; separate packages do not prove same-ID upgrade behavior.

## Ordered remaining work

| Order | Package / completion target | Prerequisite and current disposition |
| --- | --- | --- |
| R1 | Durable documents, lazy project metadata loading, process-loss recovery, source/provider custody, Back/history, saved repeated exchange and portable bundles | First unblocked engineering after R0; required families S01–S08 remain open/partial |
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

## Next action after R0

Inventory current document/save/source ownership and choose one bounded R1 deliverable from actual missing behavior. Start with recoverable drafts and project-list memory requirements, schema/write transactions and explicit process-loss experiments. Establish source/version identity before adding larger professional features. Record the chosen item, exact baseline, owner, expected visible result and next decisive check here. Do not restart already completed blueprint intake.

## Pending owner/device decisions

- One combined repaired handset candidate: fresh-cover Crop, long Draw, Pro lasso, dense Save rejection, prior portrait GIF workflow and physical Back/picker/provider checks. Engineering supplies the exact APK and short script after qualification.
- Four formal pre-Video workspace locks remain open; a limited successful phone session does not close the full product inventory.
- Shipping device/performance, RAW-camera and interchange support tiers are explicit later decisions, informed by implementation evidence.
- Greenezone website completion is an external dependency, not app-engineering scope. Play Console setup waits for that plus the functioning app; no account work now.
- Exact production signing/account/listing and public release need Lance at R11. Routine authorized engineering continues without repeated permission.

## Failure and drift rule

For a failed check, record the exact symptom and phase, causal hypothesis and bounded next correction. Do not retry unchanged application failures, relax an established budget for green, replay passed intake, or call a helper result full workflow acceptance. Reopen settled work only for a new failure, relevant dependency change, weaker evidence or an owner report, with the reason recorded.
