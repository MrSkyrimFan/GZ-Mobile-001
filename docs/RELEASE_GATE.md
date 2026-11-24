# GZ Editor Pro engineering and release gates

## Current engineering qualification — PR #31 / 0.12.10 Test

The named R29-01/R29-02 Eraser correction and observed GIF Save performance failure have passed candidate and merged-application qualification. This closes the focused engineering gate and makes **0.12.10 Test** the current combined phone candidate. [Execution evidence, raw receipts and exact APK](ERASER_REPAIR_VERIFICATION.md). The prior 0.12.9 download is historical and superseded for this pass.

- [x] Actual UI regressions show first/later capacity refusal preserves the full gesture baseline, unrelated strokes, dirty state/history, named reopen and decoded output pixels.
- [x] Sparse/long visible segments erase correctly in Regular and the selected Collage tile; cancel, parent history, named reopen and PNG retain sibling independence.
- [x] Exact detail-aware GIF caching preserves uncached Android raster pixels and the established fixture/output/budgets. The observed 28,331 ms failed candidate remains in the evidence chain.
- [x] Final candidate and merge retain all original 186 case identities and add five Eraser plus two cache-fidelity cases. JVM/build/signing and **193/193 per API29/API35** pass with zero failures/errors/skips.
- [x] The merged API35 emulator-exercised APK is independently identified as 0.12.10 / code 22 / phonetest.v22; Android v2 signature and SHA-256 are recorded in the execution receipt.
- [ ] Owner completes the focused combined 0.12.10 phone pass. Automation does not replace original-media, physical Back, OEM/provider, sustained-performance or workspace acceptance evidence.

Qualified application commit: `3ad65c533f7667a136d4fa1838afcc06d3f484da`, tree `1c7e3007204192ea240a7fd8d65cbd00cf7f9511`, identical to candidate `aa86c37c64c5e395d5378df281a0c7583a39d0e0`. [Candidate run](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45480001019) · [merged-main run](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45483297512). Later documentation-only commits record these results without constituting new app/test/Gradle/CI inputs or a new qualified binary. R1.1 remains the selected next engineering task.

### Retained PR #29 checkpoint — historical, not the current download

- [x] R0 fresh-cover/manual-Crop, bounded Regular save/export, complete long Draw/cancel and Pro lasso have regression evidence against actual flows.
- [x] Exact corrected candidate and merged application pass JVM and full API29/API35; preserve prior 174 cases and established GIF budgets. Independently review failure receipts and output assertions.
- [x] Historical exercised v0.12.9/code 21 phone APK has source/run/artifact/hash/package/signature recorded in PR #29. Its four original engineering corrections remain credited; PR #31 supplies the current phone artifact.
- [ ] Owner completes one combined phone pass with the focused new cases plus retained field cases below. Physical acceptance remains separate from CI.
- [ ] All required families in the [capability inventory](CAPABILITY_MATRIX.md) complete the [roadmap](GZ_ENGINEERING_ROADMAP.md); a repaired current build is not the entire professional app.
- [ ] Lance formally locks all four pre-Video workspaces before Video implementation.
- [ ] All five editing workspaces complete/accepted before final English/Spanish welcome and customer legal acceptance.
- [ ] Fully functioning qualified app AND Greenezone website complete before Play Console setup. Account is not set up; website work is outside this repository task.
- [ ] At that final stage: verify actual account-specific requirements/current policies, rights/notices, release signing/AAB/splits/same-ID upgrade, store testing/listing and explicit owner publication approval.

[Single work ledger and current execution receipts](WORK_LEDGER.md). Historical PR #29 app merge `c9efc5aa0673f21b98f150733582a24996b34d10`: candidate and merged-main each pass Verify and 186/186 on both API29/API35; exact APK identity is in [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29). The earlier successor review hold is lifted by the supplied review and owner's engineering request. Existing evidence below retains its original scope/date; it does not close new defects or full release.

## Retained field-correction acceptance

- [ ] Open a rights-cleared 1080×1920 portrait video of approximately five seconds from the GIF workspace. Compare untouched playback with trim around 2.19–4.99 seconds, Reverse, B&W or Punch, speed, crop, Undo/Redo and rapid edits. Confirm changing picture content, correct direction and responsive controls. Reopen a saved project and decode the exported animation independently to check orientation, motion, color, duration and frame schedule. Include 720p60 B-frames and representative phone media.
- [ ] Enter Regular Crop through the normal UI. Drag four corners, four edges and the interior in Freeform and fixed ratio with touch input. Verify predictable anchoring, pointer-down acquisition, letterbox interactions, expected crop pixels and saved-project reopen. Confirm Regular and Collage tile editing omit Masks authoring and retain Text, Blur & cover and old mask-bearing projects.
- [ ] Enter Pro from an edited portrait in Regular. Confirm Yes transfers the current appearance and geometry. Confirm No opens an empty editor and dismissal keeps Regular open. Switch back via the menu and Back, and verify Regular's source, edits, history and output still match. Check Pro Text authoring is absent and legacy text still renders, and exercise File → Delete current work with Cancel and Confirm. Verify originals, Regular edits and named saved projects survive.
- [ ] Exercise Pro layer selection, transform, masks, opacity/blend, painting and repair through pixel change, Undo/Redo, save/reopen and export. Check Back through active tools, menus, dialogs, keyboard and workspaces. Verify physical gestures from both edges, half-cancel and rapid double Back on a handset, including picker/IME ownership.
- [x] Audit 2 engineering gate for v0.12.8 Test. The exact candidate and merged application commit passed JVM verification and 174 of 174 instrumented tests on Android 10 and on Android 15. The emulator-exercised APK identity and hash are recorded in the repair evidence. Owner phone checks above remain open. [Repair evidence](AUDIT_2_REPAIR_VERIFICATION.md) · [Candidate workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45337105839) · [Merged-main workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45339000608) · [APK artifact](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45339000608/artifacts/14895803102).

### November 23 independent audit cases

Use the [audit repair evidence index](AUDIT_2_REPAIR_VERIFICATION.md) and [PR #26](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/26) for the exact candidate and merged application commit, receipt archives, and APK identity.

- Exercise default Save across named project opens and Undo/Redo in Pro and GIF. Check confirmed replacement and deletion, and verify old and newly written editable projects after reopen. Reject a Pro recipe that exceeds its reader budget while preserving the previous save.
- Follow protected source pixels through flips, quarter turns, straightening, crop and reopen. Check decoded exported pixels for Regular and a selected Collage tile. Verify named deletion, dirty-photo replacement, duplicate-source tile movement, relink and staged storage failure.
- Confirm a delayed Android picker result can write the original immutable recipe after an Activity recreation. Check cancellation and duplicate delivery, and verify retry to a fresh destination after an ambiguous write. Check real process reclamation with a PID change and a separate picker on a handset.
- Verify video GIF crop, aspect, timeline, delays, export time, reopen and playback with independent media decoders while keeping normal-resolution baseline performance. Check both Android 10 and 15.
- Physical Back and provider-specific behavior remain handset acceptance cases. Exercise both edges, canceled gestures, picker and keyboard ownership, source grant loss, and a provider with aliases or a partial write.

### Regression cases found after Revision 4

- In Pro, check brush opacity and masking against an occluding image while the pointer is still down. Check that live erasing reveals the lower layer or transparency and that a second pointer cancels without an Undo entry. Verify committed paint and erase through raster output and Undo/Redo.
- In Regular, open Histogram from More. Check RGB and luminance bins, clipped channel counts, a visible Exposure edit, and Before view. Keep the measurement on displayed preview pixels so its precision and scope match the app's current sRGB pipeline.
- Measure the 1080×1920 edited portrait fixture through the Save GIF command at 180×320 with the complete 88-frame schedule. In the automated test, save within 15 seconds, decode every frame delay and both reverse endpoints, and play the saved file after reopening. Retain the six-second edited preview check.
- On each Android runner, compare the production export of the rotated 720p60 fixture to the per-timestamp reference export. Keep the same canvas size, duration, frame schedule and moving-pixel checks, and require the production path to finish in half the reference time or less.
- Check a small crop's edge midpoint and interior independently of its corners. Exercise locked ratios on wide and tall photos, the first gesture after reopening Crop, and off-center positioning in older saved projects.
- Open a different named Pro project, Undo and Redo across that boundary, and verify Delete current work returns to the correct baseline. Confirm the separate named-delete prompts before deleting a save. Reorder a layer, Undo and Redo, and verify subsequent tools still target it. Exercise a pending tool draft when another layer's media becomes unavailable.
- Widen both ends of a long trim at a fixed frame rate. Keep the selected FPS and stop at its exact frame-budget limit. Cover unspecified decoder color tags, edited playback of metadata-rotated clips, failed-codec fallback and actual edited export.
- Move a square crop off-center, apply a Look, hand it into Pro, and verify the rendered crop, saved edit recipe, and intact Regular draft.

## Historical v0.8 Collage and photo verification criteria
- [ ] Main original photo edit screen unchanged aside from GZ brand on landing and Create collage entry.
- [ ] Crop/blur/text/shapes/brush active guides persist while main toolbar is hidden, and editing remains possible.
- [ ] Pure layout checks: all seven arrangements, counts 1–20, nonoverlap, bounds, tap selection, max spacing and budget.
- [ ] Android bitmap render checks: 2 independent edits, exactly 20 tiles, no source overwrite, PNG/JPEG image output.
- [ ] Local collage project encode/decode/save/load with distinct per-tile color, privacy and creative layers.
- [ ] Collage Undo/Redo restores layout, tile order and per-tile edits, with bounded snapshots and one checkpoint per slider gesture.
- [ ] Missing photo raises tile-specific error and offers replacement rather than silently omitting content.
- [ ] Import count hard capped at twenty, including Android picker fallback.
- [ ] Collage exports via existing seven real file encoders/social sizing choices; no AI, ads, network or watermarks.
- [ ] All prior JVM, Android raster and Compose tests continue passing.
- [ ] PR head CI pass, exact SHA merge, merged-main CI pass, verified debug APK artifact.

## One phone field pass
- [ ] Add 2 photos side by side; edit each tile independently in full editor, return with Done.
- [ ] Add 6–20 photos (as many as convenient), try Grid/Mosaic/Featured templates and aspect, spacing, margin/background.
- [ ] Reorder, remove, replace; save named collage, reopen and verify per-tile adjustments.
- [ ] Export JPEG and PNG, check distinct photos/content preserved, no watermarks.
- [ ] In regular editor, hide tools while editing crop/blur/text/brush; selected handles must stay present.
- [ ] Check original standalone photo editor, saved projects, privacy black bars and JPEG export still work.

## Commercial gate remains open
Native app profiling, large-photo/20-tile memory pressure, long session/ANR, color accuracy, Android API/device coverage, media URI grant lifetime, trademark/patent/license/SBOM and privacy/Play/store legal checks, production signed AAB. No final 1.0 claim.

Refer to `docs/GZ_ENGINEERING_ROADMAP.md` for lawful original-code Lightroom-class **non-AI** features, RAW/license decisions and later video editing.

## Historical v0.9 verification criteria
- [ ] Top-right mode dropdown on Regular, Pro, GIF and Collage. Video disabled / Coming soon. Regular toolbar compact, no destroyed existing tools.
- [ ] Pro image compositor: independent image edits, blend/position/size/opacity/visibility/order/undo/persist/export.
- [ ] Real GIF89a multi-frame output with tested Android decoder, timing/loop/ordering, saved project/undo and safe source ownership.
- [ ] No new permission, dependency, AI, ad, account, watermark or network regressions.
- [ ] Exact PR HEAD passed CI, merged main Android bitmap/Compose tests passed, expected APK artifact exists.
- [ ] Historical 0.8 and 0.9 physical phone check `PHONE_TEST_WAVE8.md`; commercial release and professional/RAW/video work remain open.

## Historical v0.12.1 independent AAA audit criteria

The commercial gate above remains open.
- Source audit: `docs/AAA_RELEASE_AUDIT_013.md`. Owner requested complete AAA verification; **not achieved** at this stage.
- [ ] API29 minimum-device and API35 full Android emulator tests green at identical source SHA.
- [ ] Real GIF color-gradient and moving-transparent-object output decoded without ghosting or color damage.
- [ ] GIF Save As works with a write-only document provider; original source and existing files remain unchanged.
- [ ] GIF exports respect device free-space quota, cancel gracefully and clean up incomplete outputs.
- [ ] Pro save/overwrite, interrupted AtomicFile recovery, fail-closed malformed input, Undo/Redo and source URI access revalidated.
- [ ] Long-session, large-media, thermal/battery/ANR, wide color gamut/ICC and multiple real phones validated.
- [ ] True Photoshop/GIMP parity: advanced layers/groups, masks, smart selections, retouching and production-scale tools.
- [ ] Lightroom-class RAW/16-bit/float/ICC management, catalog, batch and device profiling.
- [ ] Accessibility and complete tutorial onboarding, licensing/SBOM/commercial payments and production Play-signed App Bundle.
- Signed side-by-side APK may be supplied only as *noncommercial QA* with explicit warnings; it is **not** an AAA or Play Store release.
