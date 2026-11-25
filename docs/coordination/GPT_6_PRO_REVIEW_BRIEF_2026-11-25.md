# GPT-6 Pro independent repository review brief — 2026-11-25

Owner-requested review handoff. This document packages the complete review instructions, remaining-scope checklist and next test plan; it does not replace the canonical roadmap, capability matrix or work ledger. Publication of this brief is documentation only and supplies no new application or qualification result.

You are GPT-6 Pro, acting as an independent senior Android engineer, product reviewer and release-readiness reviewer for Lance / Greenezone Software LLC.

Review the actual GZ Editor Pro repository work, reconcile what remains in the complete intended product, and specify the next tests. Be candid and evidence-based. Do not presume Codex's implementation or qualification claims are correct, and do not invent findings to make the review appear thorough.

This is a READ-ONLY REVIEW. Do not modify files, implement fixes, build, execute tests, dispatch implementation workers, create branches/PRs, commit, merge, publish, set up Play Console, or change the website. Inspect source, diffs, tests and existing CI/artifact evidence. Propose additional tests without claiming to have executed them. Return the completed review for Lance to bring back to Codex. Keep your review separate from later engineering.

1. REPOSITORY AND EXACT REVIEW BASELINE

Repository:
https://github.com/MrSkyrimFan/GZ-Mobile-001

Verified checkpoint when this prompt was prepared, November 25, 2026:
- Pinned documentation baseline before this review-brief publication: 3cd1ed578d73610548465c29a27bae8e2cef1480
- PR #38 is the documentation refresh:
  https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/38
- Qualified application merge from PR #37:
  46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30
  https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/37
- PR #37 final candidate:
  044e93321292a3678b92394b5efda50aae2e07b9
- Engineering baseline before PR #37:
  6f7c574f223e5349a74e622e97433736a07f4a44
- Recorded candidate/application-merge tree:
  57b3ff2510e059184bfaefee2e781d350bf0930f

First verify these identities. If main has advanced, review the pinned checkpoints and identify subsequent changes separately. Do not silently substitute a newer application or treat a documentation-only commit as a newly tested binary.

Read the PR #37 application diff and relevant surrounding code against its actual base. Verify that PR #38 changes documentation only. GitHub is the primary workspace; do not clone, mirror or check out source onto Lance's SSD/OneDrive. Use repository access and available read-only evidence.

2. READ THE AUTHORITATIVE REQUIREMENTS BEFORE JUDGING SCOPE

Read these files at the pinned documentation commit, following relevant references:
- AGENTS.md
- docs/CONTINUITY.md
- docs/PRODUCT_SPEC.md
- docs/WORK_LEDGER.md
- docs/GZ_ENGINEERING_ROADMAP.md
- docs/CAPABILITY_MATRIX.md
- docs/RELEASE_GATE.md
- docs/B1_WORKSPACE_VERIFICATION.md
- docs/coordination/CHATGPT_RESPONSE_TO_50_2026-11-24.md — all 50 answers
- docs/coordination/ENGINEERING_RESUME_2026-11-24.md

For decision provenance or disputed scope, consult:
- docs/CODEX_ALIGNMENT_HANDOFF_2026-11-24.md
- docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md
- docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md
- docs/ERASER_REPAIR_VERIFICATION.md
- Historical blueprints linked by the roadmap and capability inventory where necessary.

The three-stage alignment exchange is complete. Historical onboarding holds and commands are historical. Do not reopen intake, replay the 25/50-question exchange, restart successful audits, or create a competing master roadmap. The current roadmap and ledger remain canonical. Preserve previously earned, properly scoped evidence; reopen it only for a concrete new failure, changed dependency, evidence gap or owner report.

Lance wants the complete original professional app vision at a very high quality level. Do not silently turn it into a reduced MVP. Translate “AAA quality” into observable correctness, complete workflows, dependable saving/recovery, responsive interaction, output fidelity, accessibility, physical-device qualification and explicit acceptance where required. Avoid invented completion percentages, dates or unsupported claims of parity with other products.

Product constraints include:
- Paid once, target US$2.99; offline; no runtime AI/ML, cloud accounts, ads, telemetry, subscriptions, IAPs or watermarks.
- Original media remains protected.
- Dark, image-first interface; distinct Regular, Pro, Collage, GIF and later Video ownership.
- No Regular Masks authoring and no Regular Histogram UI; preserve legacy data and required internal processing.
- No new Pro Text authoring; preserve legacy Text records. Do not import the Regular decoration/development interface into Pro.
- Collage retains 1–20 independently editable tiles and all seven layouts.
- GIF remains silent. Video is a separate later workspace with audio/music.
- Formal workspace locks and publication are owner decisions.
- Play Console is not set up. Lance requires a fully functioning app AND the completed Greenezone website before Console setup. Website implementation is outside this app review.

3. INDEPENDENTLY AUDIT THE RECENT IMPLEMENTATION

Do not substitute documentation or green test counts for code review. Trace changed behavior through actual screen commands, state ownership, persistence, lifecycle, asynchronous results and exports. Inspect all changed production files and their necessary dependencies, plus changed/new tests and CI configuration.

Give particular attention to:
A. Shared header, top-right Options, common Undo/Redo arrows, unique ownership of major commands, hideable chrome, narrow layouts and large fonts. Confirm Regular More/Create collage/Histogram UI removal follows the agreed contract without damaging retained processing.
B. Collage single-tap behavior: tile selection plus exactly one chrome toggle; margin preserves selection; drag, multiple pointers and canceled gestures do not become taps.
C. Save/Discard/Cancel: captured workspace/revision/destination, accurate dirty state, no navigation before verified save, no lost work on cancel/failure, and no stale asynchronous result modifying a replacement document.
D. Named saves: stable identity, expected stored-version checks, overwrite versus Save as, name collisions, verified publication, rollback and healthy-neighbor preservation, deletion and failed-save journal consistency.
E. Child-photo versus parent-Collage transactions: standalone photo Save is not Apply; parent cancellation retains both states; no premature child mutation or loss while a destination is unresolved.
F. Regular-to-Pro Yes/No behavior and Pro resume/start-another/discard choices; preserve source ownership, transfer semantics and durable state.
G. Gesture cancellation and history: text, shapes, privacy, crop and Draw restore document/history/selection on cancel; accepted changes own their corresponding selection updates and logical undo checkpoints.
H. Pro saved/entry/reset baselines and durable journals. The currently guarded but unsaveable zero-layer setup is a known remaining R2 capability, not evidence of completed empty-canvas persistence.
I. Immutable picker/export ownership; delayed callbacks; native restoration permission versus independent export-retry recipes; fail-closed unavailable grants; exact saved GIF identity; route changes, Retry and canceled jobs.
J. GIF foreground state/progress/cancellation and provider-copy behavior. “Canceled” must mean work/resource ownership has actually stopped. Report publication uncertainty honestly; do not claim durable background work from foreground cancellation handling.
K. The Regular SharedPreferences rollback adapter is a bounded B1 repair. Determine whether it is sound within that scope; do not credit it as R1.1 migration or R1.2 durable autosave.

Relevant code is under app/src/main/java/com/greenezone/gzphotoeditor/, including:
- ui/WorkspaceCommands.kt, EditorWorkspaceHeader.kt, WorkspaceDeparture.kt
- ui/PhotoEditorScreen.kt, WorkspaceModeMenu.kt, EditorDock.kt, RegularPhotoDock.kt
- ui/CreativeCanvasOverlays.kt, PrivacyRegionOverlay.kt, PhotoExportController.kt
- ui/ProWorkspaceScreen.kt, CollageWorkspaceScreen.kt
- ui/GifMakerScreen.kt, VideoGifScreen.kt, GifProjectDialogs.kt, GifExportRecovery.kt
- editor/NamedProjectIdentity.kt, ProjectStore.kt, CollageStore.kt, ProStore.kt, GifStore.kt, PendingExportJournal.kt, ProModel.kt
- editor/EditedVideoPreviewReader.kt, VideoGifSource.kt, GifEncoder.kt

This is a navigation aid, not a limit on the review. Follow the actual diff. Inspect relevant Android/JVM tests, app/build.gradle.kts, .github/workflows/android-ci.yml and .github/scripts/android-test-with-receipts.sh.

4. VERIFY THE EVIDENCE AND ITS LIMITS

Reported CI:
- Candidate run:
  https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45596352650
- Application-merge run:
  https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45600142592
- Both reportedly pass JVM/build/signing and 258/258 instrumented cases on API29 plus 258/258 on API35, with zero failures/errors/skips.
- All 193 inherited Android test identities reportedly remain, with 65 additions and 258 unique identities.
- Recorded sorted Android test-ID SHA-256:
  8daa78d5f2580cdf8fc86f2811e34e5d70c403cf846ad6a6e458b3eab669cbed

Audit changed assertions and test migrations, not just names/counts. Check that tests exercise the relevant production flows and meaningful failure behavior. Identify claims covered only by helpers, static inspection, a narrow fixture or Activity recreation.

Delivered phone artifact:
- GZ Editor Pro 0.12.11 Test / version code 23
- Package: com.greenezone.gzphotoeditor.phonetest.v23
- minSdk29 / targetSdk36
- Merged API35 artifact:
  https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45600142592/artifacts/15017086053
- APK SHA-256:
  969d2c095f856a65f3d6de905e253dfd6e225e479a451edf4b21b2989362f971
- ZIP SHA-256:
  a3aee3437252cf3df4bd7091094baa1c5ad7a4124200e6bcc349256003de0065

The receipt contains the raw artifact identities, signing checks, unsuccessful attempts and full measurement details. Verify what access permits. This separate v23 installation does not prove same-ID production upgrade/migration. Existing older apps/projects remain separate.

Keep these evidence levels distinct:
source inspection; unit/fixture tests; emulator execution; Activity recreation; verified new-process loss; physical-phone observation; formal workspace acceptance; commercial release.

If source, logs or artifacts cannot be accessed, identify the specific missing item and limit the relevant conclusion. Do not invent an inspection or demand an indiscriminate reupload of everything.

5. REVIEW GIF PERFORMANCE WITHOUT HIDING MISSES OR LOWERING QUALITY

Owner's reported failure: saving a roughly 2–3-second video clip took 2–5 minutes BEFORE the destination picker appeared, with the other options at defaults. Phone model/Android version/exact source are unknown. Do not invent them or repeatedly ask Lance for information he already said he does not know.

Representative default-quality fixture:
1080×1920 source, 150 source frames, trim 0–2665 ms, Original size/source FPS/100% speed/forward/neutral, yielding 80 scheduled frames. Recorded output: 995,576-byte GIF, 2670 ms.

Recorded observations, API29 / API35 respectively:
- Original tap-to-picker: 20,932 / 26,262 ms.
- Final merged tap-to-picker: 14,159 / 14,893 ms.
- Original source-reader stage: 15,199 / 22,354 ms.
- Final merged source-reader stage: 8,371 / 10,255 ms.
- Final start acknowledgement: 83 / 92 ms.
- Final cancel acknowledgement: 49 / 50 ms.
- Final worker stop after cancellation: 34 / 17 ms.
- Final observed stopped/canceled result: 138 / 132 ms.

These are individual observations on separate CI hosts, not a matched cold/warm study or a universal handset promise. An earlier API35 start acknowledgement of 257 ms missed its 250 ms target and remains part of the evidence.

Prospective exits remain:
- Tap-to-picker <=10 seconds AND at least 50% improvement: unmet on BOTH APIs.
- Source-reader >=50% improvement: unmet on API29; met in the recorded API35 observations.
- Start/cancel acknowledgement <=250 ms; worker/observed stop <=1000 ms. Final observations pass, with the earlier miss retained.
- Inherited 6-second sampled-motion and 15-second SMALL-fixture Save gates remain separate, unchanged and passing. Do not use the small-fixture limit to reclassify the full-resolution result.

Review both implemented optimizations:
1. Bulk YUV row reads without coordinate/color conversion changes.
2. A reader-owned exact packed-YUV-plus-matrix cache: 4096 keys and 4096 ARGB values, 32,768 bytes of array payload. Verify keys, range/matrix behavior, collisions and exact recomputation on misses; no global cross-reader contamination or added retained reverse-frame cache.

Check unchanged dimensions, schedule/timing, rotation/crop, alpha/color policy, source identity, independently decoded GIF output and selected full-ARGB parity fixtures. Selected-frame parity is not proof of every frame on every device/media type. Reverse storage remains bounded at the recorded 16 MiB cap. No quality reduction is an acceptable hidden optimization.

Audit the CI failure history:
- An intermediate API29 run had five OOM failures under an actual 16 MiB heap growth limit while API35 passed all 258 cases.
- The runner initially wrote the ineffective hw.heapSize key rather than the AVD vm.heapSize key.
- The correction configures the actual AVD key, RAM, explicit guest heap/growth limit and a verified newly started init-owned zygote; final runtime maxima were 256 MiB on API29 and 192 MiB on API35.
- An intermediate preflight failed because pidof returned two zygote PIDs; the eventual selector verifies the init-parent process.
- GIF tests now fail promptly on an actual export-failed state instead of waiting out a long timeout.

Determine whether these are valid environment/diagnostic corrections or conceal a product problem. Preserve the raw failures. A correctly configured emulator does not close low-memory/device qualification or prove the original 16 MiB condition is supported.

Assess whether the coherent B1 phone candidate is appropriate while B2 and F03 remain explicitly open. Do not force approval; distinguish a concrete unsafe regression from an unmet, already disclosed performance exit.

6. RECONCILE THE ENTIRE REMAINING ROADMAP

Return a useful full roadmap assessment, not just “do R1.1 next.” Preserve all 77 capability families and every retained roadmap child. Use the canonical IDs/names/status distinctions. Separate implemented behavior, bounded automated evidence, pending device evidence and genuinely unimplemented scope.

The current ordered scope includes:

- R0 / current corrections: retain qualified historical repairs. B1 needs focused handset confirmation. B2/F03 retain the actual reported phone case, matched performance evidence and unmet targets. Do not automatically restart these packages wholesale.
- R1.1 NEXT: metadata-only project listing and selected-only loading; bounded migration from whole-map SharedPreferences to per-project records/index; copy/validate/publish one entry at a time; preserve stable IDs, names/order, twelve slots, versions/URIs and healthy records; handle corrupt entries, failed publication, stale saves and deletion recovery. A single getString that materializes the whole map is not bounded loading.
- R1.2: acknowledged durable completed-edit drafts, visible pending/failed persistence, bounded deduplicated history retaining logical undo depth, and genuine new-process recovery of document/source permission/baseline/history/route/selection/tool/viewport/child/playhead. Never persist partial gestures as accepted work or silently Apply a child.
- Remaining R1: schema/render-version migration, bounded serialization, source grants/relink, provider aliases/write-only/partial/uncertain results and low storage, immutable jobs and complete Back ownership, repeated native Regular↔Pro exchanges, portable embedded/link-only project bundles, checksum/import fidelity and real same-ID upgrades.
- R2: stable Pro layer tree/groups, isolated/pass-through behavior, clipping/masks/opacity/locks/multiselect/alignment, transforms and defined alpha/blend semantics; bounded reference/tiled compositor, effects halos/caches/proxies and preview/final parity; capacity tiers justified by device measurements; valid zero-layer save/reopen/export. Preserve current caps until evidence supports expansion.
- R3: compound selections/channels/masks, precise transforms/pivots/snapping/guides/paths/booleans/gradients; deterministic brush/eraser dynamics with pressure/tilt/finger fallback; editable clone/heal/patch/inpaint/dodge/burn and donor/sampling policies; ordered effects/styles/conditional blends; native fidelity, explicit OpenRaster/PSD subsets and loss reporting, unsupported-record preservation and atomic actions. No new Pro Text authoring.
- R4: conventional offline, non-AI Subject & Background extraction and replacement, trace/keep/remove/manual correction, editable refined alpha including hair/fur/glass/holes; bounded removal/inpainting; reversible mesh/liquify/freeze/reshape/retexture with preserved outside regions and versioned geometry/source/shading; dependency rights and resource qualification.
- R5: complete Regular numerical tone/WB/HSL/curves/Looks/presets/before-after; float/16-bit color-managed processing and legacy appearance; declared DNG/Bayer/linear development then qualified broader RAW without confusing embedded previews with development; ICC assignment/conversion/wide gamut/proof/16-bit output; distinct HDR import/edit/display/tone-map/export; optics/perspective/detail; HDR brackets, panoramas and focus stacking with alignment/correction. No restored Masks authoring or Histogram UI.
- R6: transactional catalog/folders/collections/ratings/tags/metadata/search/smart collections, byte-confirmed duplicates/stacks/virtual copies, sidecars/backup/relink/selective sync; durable batch with frozen recipes and staged/published/uncertain results; typography/drawing/shapes/frames/borders; at least 500 finished, usable original/licensed stickers with real assets, manifest and visual QA, plus search/favorites/recents/custom PNG/WebP/history/selected-tile behavior. The 648 concepts do not equal 500 finished stickers.
- R7: complete GIF media/timebase/lifecycle/performance matrix, VFR/B-frames/rotation/non-square pixels/disposal/transparency/loops/HDR policy, real edited playback/scrub/reverse/export; frame/segment assembly, split/remove/clone/order/holds/ping-pong/timed overlays/palette/dither/encoded preview; new photo-sequence creation and later durable background custody; explicit reversible legacy-v4 aspect upgrades. Complete all seven Collage layouts and 1–20 independent tiles, full child-photo capabilities, repeated-source independence, fit/fill/layout styling, relink versus replace, history/recovery and output fidelity.
- R8: complete applicable pre-Video families or an explicit owner scope disposition; physical-device/OEM/codec/memory/thermal/storage/provider qualification; real TalkBack/keyboard/large-text/small-screen/usability checks; four finite acceptance dossiers and explicit owner locks for Regular, Pro, Collage and GIF. CI counts are not these locks.
- R9, AFTER ALL FOUR LOCKS: professional separate Video project/timebase/proxies/player/export; precise multitrack/ripple/roll/slip/slide/J-L editing; user audio/voice/waveforms/mixing/fades/pan/EQ/dynamics/limiter/conventional noise reduction/automation/ducking/buses/stems; compositing/PiP/keyframes/captions/masks/keying/ramps/reverse/freezes/transitions/stabilization/nested/multicam; photo-and-music slideshows; Looks/scopes/LUT/log/SDR/HDR; privacy redaction across affected frames/streams/thumbnails with real re-encoding; bins/versions/mastering/ranges/audio/subtitles/interchange/archives/relink; measured resolution/codec/device tiers. SDR-first is sequencing, not removal of later promised scope.
- R10, AFTER ALL FIVE EDITING WORKSPACES ARE COMPLETE/ACCEPTED: full English/Spanish UI/help/accessibility and final welcome/language/privacy/terms/license acceptance/decline/local-access flows; offline/fresh-install/returning-user/same-ID-upgrade/accessibility coverage in both languages. License diligence still happens during earlier engineering.
- R11 LAST: functioning qualified app AND finished Greenezone website before Play Console setup; then verify actual account-specific identity/testing and current Play requirements, rights/SBOM/shipped notices, production key custody/Play Signing/signed AAB/installed splits/same-ID upgrades, release behavior/ABI/native page-size requirements when applicable, truthful price/listing/privacy/Data safety/age/ads/territories/support/store tests, explicit publication approval and maintenance/forward-migration/rollback policy without app telemetry.

Treat this as a reconciliation checklist against the full canonical files, not permission to discard an omitted subfeature. If there is a conflict, cite it and explain the controlling owner decision. Ask only genuinely unresolved product questions; investigate technical uncertainty from available evidence.

7. DEFINE THE NEXT TEST RUN AT THE RIGHT LEVEL

Give Lance a short, practical immediate phone script for the delivered 0.12.11 Test build. Preserve original media and older test installations. Do not require another full historical script or ask him to construct engineering budget fixtures.

The six existing focused checks are:
1. Common top-right Options and Undo/Redo across Regular, Pro, Collage and the two relevant GIF routes; include large text if he uses it.
2. Two-photo Collage: one tile tap and one margin tap hide/show tools correctly; edit one tile, attempt leaving with Apply, then cancel the parent departure; both child work and parent remain available.
3. Dirty Back and workspace switches in Regular/Pro/Collage: Cancel retains work; Save finishes before leaving; Discard affects only the stated branch. Standalone tile-photo Save does not silently Apply to Collage.
4. Named Save, change and Save again, Save as a different name, reopen both independently; cancel a name collision without losing the current work.
5. Roughly 2–3-second video trim with other options at defaults: time the wait BEFORE the destination picker separately from AFTER destination confirmation; open output and inspect movement, crop/orientation and duration. Ask for optional device/source details only if readily available.
6. Separate GIF preparation cancellation: visible acknowledgement, honest completed-stop state and retained native edits.

For each give setup/actions/expected result/simple pass-fail report. Explain what the user can observe versus what instrumentation must establish. Add a test only for a concrete uncovered risk, and say why. A focused pass does not grant full workspace locks, demonstrate process-death recovery or establish production upgrade/release readiness.

Separately propose the NEXT ENGINEERING tests, without executing them:
- Any necessary bounded correction/regression tests arising from your findings.
- R1.1 baseline and acceptance tests: twelve independently identifiable near-budget projects; actual peak heap including raw preferences/XML/map, list/open latency and a justified numerical budget pinned before implementation; zero steady-state listing recipe decodes and only the selected project's decode on open; copy/validate/publish/index/cleanup fault injection; cold recovery; corrupt/unavailable entries with healthy neighbors; identity/order/slot compatibility; stale-save protection, no deletion resurrection and no dirty/source loss.
- R1.2 later tests must prove actual new-process recovery, not just Activity recreation.
- B2 performance tests need a pinned workload/options/output-fidelity contract, reproducible matched cold/warm sampling and stage-separated timing. Include cancellation, failure and memory/resource limits without silently changing quality.
- Full JVM/API29/API35 and exact artifact/source qualification belong to a changed final application candidate and its merge. Do not demand a rebuild solely because PR #38 updated documentation.
- Broader source/provider/device/upgrade/accessibility/Video/release tests belong to their named roadmap gates.

8. REQUIRED REVIEW OUTPUT

Return the following, in this order:

A. Three explicit verdicts:
   1) Is PR #37 suitable for the focused 0.12.11 handset run?
   2) Which defects, evidence gaps and B2/F03 targets remain?
   3) How far is the complete product from professional release, by concrete remaining packages rather than a fabricated percentage?

B. Prioritized actionable findings. For each: severity, exact commit and file/line reference, reproducible trigger, expected versus actual behavior, user impact, evidence versus hypothesis, smallest coherent correction, decisive regression test, and whether it blocks the phone run, the next engineering item or a later release gate. Separate confirmed defects from verification gaps and planned unimplemented features. If you find no new defect, say so plainly.

C. Evidence reconciliation: exact source/run/APK identities; test/assertion preservation; failed CI attempts and heap correction; GIF gains and misses; what was actually inspected versus unavailable; honest limits of every major qualification claim.

D. Complete remaining roadmap assessment using the canonical R packages and ALL 77 capability IDs. A compact table may use one row per capability with current bounded evidence, remaining behavior, decisive validation, package/dependency and owner gate. Account for all rows; do not omit “later” features or invent a second active roadmap. Highlight any contradictions, missing dependencies or misleading completion language in the current documents.

E. The focused next handset script with a simple response template Lance can fill in, followed by the distinct proposed engineering test plan.

F. A finite paste-back handoff to Codex: first unblocked action, exact baseline and likely files, observable outcome, exit condition and next decisive check. R1.1 remains the next planned independent package unless you substantiate a defect that should take precedence. Keep B2/F03 open without making missing owner-specific information halt unrelated authorized work. Identify any genuinely owner-only decision.

Keep the review direct, precise and coherent. Do not circle back to intake, repeatedly re-audit unchanged successful work, silently reduce scope, declare completion from scaffolding, hide failed measurements or equate documentation with execution. Stop after delivering the review; implementation is a separate subsequent task.
