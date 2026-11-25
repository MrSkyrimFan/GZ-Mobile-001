# B1 workspace correction verification

Status: integrated draft, not yet qualified for handset delivery. Updated November 25, 2026.

## Authority and exact baseline

Lance relayed the completed Stage 3 answers/resume prompt. The full CQ01–CQ50, active instructions, specification, roadmap, 77-family inventory and release gate were read before source edits. Baseline main was `6f7c574f223e5349a74e622e97433736a07f4a44`, tree `99239d1fd9efc25a4324aa39bb6ebe44a02395ff`. Its qualified app remains PR #31 merge `3ad65c533f7667a136d4fa1838afcc06d3f484da`, 0.12.10 Test/code22, until this changed app passes complete qualification.

[Draft PR #37](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/37) implements B1 plus B2 measurements. Engineering branch is `codex/b1-workspace-contract-20261125`; `feature/b1-contract-verification-20261125` invokes the unchanged push-triggered full matrix. This document is an evidence receipt; [WORK_LEDGER](WORK_LEDGER.md) and [GZ_ENGINEERING_ROADMAP](GZ_ENGINEERING_ROADMAP.md) remain the single execution record and roadmap.

## Reproductions and diagnosed failures

| Input / run | Observed result | Disposition |
| --- | --- | --- |
| Test-only `84ecf372d3c76b12f95784916d23fef7b40f18c7`; [45568857304](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45568857304) | API35: 193 inherited passes, 6 new failures. Three intended dirty-route failures; photo-GIF scroll and two Collage URI setup defects. API29 stopped after seven cases from system_server 16 MiB heap exhaustion. | Retained failure evidence. Fix test setup only and explicitly configure AVD RAM2048M/heap256M; no assertion/timing relaxation. |
| Corrected test-only `cf94cf3c4be22f6fbd473d5cb97a76aefbb70bc9`; [45571000015](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45571000015) | JVM/build/signing pass; both APIs199 cases, 6 intended failures, zero errors/skips; all193 inherited cases pass. | Executed red evidence for Pro Back/mode, photo/video GIF Back, Collage Back and single-tap chrome. API35 XML artifact15002133591 ZIP SHA256 `72f8f8dadd66cdc25d4e7ddf7bf583c36be8341b00fbce157b8267618d41c742`. |
| Integrated `a990aab0a03de36127d56cd6100f02b872f344c3`; [45575188527](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45575188527) | App compilation, JVM tests and debug assembly completed. Android test compilation rejected an invalid onNode import. | Remove the import, preserve test body. |
| `f61c786e0da534fdc06d5683b7cf82047a6182a6`; [45576278160](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45576278160) | App compilation/JVM/debug assembly completed. Android test cleanup used nonexistent GifStore.delete. | Use existing deleteProject API; no test assertion changes. |
| `0cc910ce1ca486f5a3da296855fe3f516a9bc193`; [45576913155](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45576913155) | Production compilation rejected recoveryResolved in the wrong function. | Correct exact closeCurrentPhoto/commitDiscard blocks; the independent reviewer found the same mistake. |

Integrated `805f97a3dc05702ec9c424de3d8e2a68573d0657`, [run 45577375539](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45577375539), passed production/test compilation, JVM tests, debug assembly and signature verification. Final source review confirmed the recovery/header correction. Full Android execution remains pending.

## Implemented contract and finite review

The common header places the wrapping brand and Options at opposite edges on its first row, with workspace/Back and history on the second. Major actions have one Options location. Regular More/Create collage/Histogram entries are removed; processing/legacy compatibility remain. Collage free-canvas single taps select a hit tile and toggle chrome once; canceled/multi-pointer/moved gestures do neither.

Save/Discard/Cancel operates on captured native owners, revisions and expected saved-project identities. Child photo saves remain separate from Apply to Collage. A child-to-workspace departure stages child resolution until the parent decision and target preparation succeed. Regular-to-Pro resolves the intended photo appearance and existing Pro work before publication. Save acknowledgement follows verified publication; failed/canceled work stays open.

Stable save UUIDs are sidecars to compatible existing payloads. Update/Replace authorize the exact identity and stored-byte version; delete/recreate gets a new identity. File publication preserves verified old bytes until commit. Regular preferences retain a bounded previous-value record through verification and rollback. This is B1's minimum safe adapter, not the R1.1 per-project migration or R1.2 autosave completion.

Finite independent reviews found and corrected stale native-export restoration, post-publication rollback, late picker/dialog ownership, typed Retry/Cancel, child Photo routing, active pointer rollback, paired content/selection mutations, anonymous recovered-draft baselines, GIF name-only recovery aliases, Pro Delete recovery and narrow-header placement. Reviewers identify source findings separately from runtime results. Export retry keeps its captured recipe independently of native draft disposition; unreadable recovery permissions cannot count as a successful live Discard.

## Existing assertion migration

All **193 original Android method identities** are retained. The integrated source currently has **250 cases**; this is a source count, not executed passes. Required candidate and merged-main JVM/API29/API35/signing receipts remain pending.

| Existing files / scope | Authorized route changes | Retained checks |
| --- | --- | --- |
| Nine Pro files; two assigned files unchanged, 31 cases | Options replaces File; Undo/Redo use accessible descriptions; named Save updates directly; Save as handles copy/collision. Dirty replacement explicitly saves; hidden work is saved separately before testing reopen history. | All210 assertion-bearing lines retained modulo selector/default-copy-name text; layer geometry, masks, blend equations, source pixels, render samples, reset and named history ownership. |
| Four Regular files, 24 cases | Options replaces More/direct commands; child commands are photo-labeled; failed Save resolves before replacement picker. | 151 of152 non-UI assertion lines retained; failed-save picker count strengthens2→1. Dense-recipe budget, stroke/capacity/endpoints, sibling tiles, decoded PNG, source/history remain. |
| Regular histogram method | Production Histogram entry removed. Test-only dialog consumes samples from the actual hidden-chrome canvas, excluding letterboxing/seam interpolation. | Same method, full-bitmap RGB/luminance counts, chart assertions and50%→0%→50% clipping expectations; additionally checks More/Histogram production absence. |
| Four GIF files,16 cases | Options/current command labels; explicit save decisions; history semantics; named identity checks. | All16 methods; scheduling, metadata, independent decoded frames, source bytes, export recovery and existing6s/15s gates remain. |
| AndroidBackNavigationTest,19 cases | Common menus/appearance labels and child Apply/Discard. Retained-history departures explicitly save a fixture and clean it after the test. Async Pro reopen waits for the actual restored preview before export. | Every source-byte, sampled-pixel/crop, timing, viewport, selection, history and one-commit assertion retained. |
| AndroidExportRecoveryTest,3 cases | Options routes; restored draft's dirty Back now explicitly guards; restored native Collage saved through Save as. | Exact output MIME/dimensions/pixels, duplicate and canceled destination bytes, pending journal and restored native recipe checks. |
| CollageExportUiTest and CollageReopenUiTest,8 cases | Options owns opening/export; original slot assertion now compares the persistent Options control before/after load, then opens the actual Export action. | All original output pixels/format/dimensions/source checks, tile identity, relink, failed atomic-write bytes, exact saved document; same bounds comparison for the surviving common command location. |
| PhotoFirstUiTest,13 cases | Empty guidance points to Options; Pro uses its legitimate Add layer path; Collage opens via workspace menu; common Back semantics. | All numeric/document/tool ownership/grade/export-format checks; obsolete direct-action expectations map to their one Options entry and no Create collage duplicate. |

New tests cover actual dirty routes, delayed/duplicate picker delivery, save failure/rollback, owner-qualified recovery, interrupted text/shape/privacy/crop gestures, lifecycle interruption, paired Duplicate cancellation, narrow320dp/200% text header interactions, and the default-quality GIF profile. Some focused custody tests exercise the actual controller/store directly; they are not claimed as end-to-end phone execution.

## B2 measurement and owner boundary

Lance reports the2–5 minute delay occurred before choosing a save location, on an approximately2–3 second clip with all options at defaults except trim. Phone/Android/source remain unknown.

The representative profile opens the existing1080×1920/150-source-frame motion fixture, trims through the real UI to2–3 seconds, retains Original dimensions/source timing/100% forward/neutral edits, and measures export activation to destination-picker launch. It verifies output dimensions, complete frame schedule/delays/duration/loop, independent decoded-frame samples and unchanged source hash. Its360-second diagnostic bound is not a product acceptance target and does not replace the inherited6s sampled-motion or15s Save fixture gate.

Measurements distinguish preparation queue, processing, provider-copy and response/cancellation stages. Overlapping counters are not summed as disjoint time. No palette, dimensions, quality, source schedule or frame selection has been reduced. Choose a measured bottleneck and record workload-specific targets before evaluating any optimization. F03 remains open until the applicable measured and owner handset evidence exists.

## Delivery gate

Do not deliver this draft as qualified. Complete the integrated candidate and merge JVM/API29/API35/signing checks, resolve reproduced high-impact failures, preserve run/artifact hashes and verify the exact phone APK. Then provide one coherent candidate and a short owner confirmation script. Workspace locks, R1–R11 completion, Video, bilingual final onboarding, website dependency, Play Console setup and public release retain their separate roadmap/owner gates.
