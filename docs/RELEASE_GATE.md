# GZ Editor Pro engineering and release gates

## Current field-correction acceptance

- [ ] Open a rights-cleared 1080×1920 portrait video of approximately five seconds from the GIF workspace. Compare untouched playback with trim around 2.19–4.99 seconds, Reverse, B&W or Punch, speed, crop, Undo/Redo and rapid edits. Confirm changing picture content, correct direction and responsive controls. Reopen a saved project and decode the exported animation independently to check orientation, motion, color, duration and frame schedule. Include 720p60 B-frames and representative phone media.
- [ ] Enter Regular Crop through the normal UI. Drag four corners, four edges and the interior in Freeform and fixed ratio with touch input. Verify predictable anchoring, pointer-down acquisition, letterbox interactions, expected crop pixels and saved-project reopen. Confirm Regular and Collage tile editing omit Masks authoring and retain Text, Blur & cover and old mask-bearing projects.
- [ ] Enter Pro from an edited portrait in Regular. Confirm Yes transfers the current appearance and geometry. Confirm No opens an empty editor and dismissal keeps Regular open. Switch back via the menu and Back, and verify Regular's source, edits, history and output still match. Check Pro Text authoring is absent and legacy text still renders, and exercise File → Delete current work with Cancel and Confirm. Verify originals, Regular edits and named saved projects survive.
- [ ] Exercise Pro layer selection, transform, masks, opacity/blend, painting and repair through pixel change, Undo/Redo, save/reopen and export. Check Back through active tools, menus, dialogs, keyboard and workspaces. Verify physical gestures from both edges, half-cancel and rapid double Back on a handset, including picker/IME ownership.
- [ ] Run JVM and complete API 29/API 35 instrumentation on the repair commit, including independent Collage tile rendering and save/reopen. For a combined phone build, provide the commit, run, APK artifact, APK hash, signing/package/version details, and one compact phone script. Engineering merge authority and owner acceptance are separate. Mark physical-device and formal workspace acceptance only from the corresponding results.

### Regression cases found after Revision 4

- Measure the 1080×1920 edited portrait fixture through the Save GIF command at 180×320 with the complete 88-frame schedule. In the automated test, save within 15 seconds, decode every frame delay and both reverse endpoints, and play the saved file after reopening. Retain the six-second edited preview check.
- On each Android runner, compare the production export of the rotated 720p60 fixture to the per-timestamp reference export. Keep the same canvas size, duration, frame schedule and moving-pixel checks, and require the production path to finish in half the reference time or less.
- Check a small crop's edge midpoint and interior independently of its corners. Exercise locked ratios on wide and tall photos, the first gesture after reopening Crop, and off-center positioning in older saved projects.
- Open a different named Pro project, Undo and Redo across that boundary, and verify Delete current work returns to the correct baseline. Confirm the separate named-delete prompts before deleting a save. Reorder a layer, Undo and Redo, and verify subsequent tools still target it. Exercise a pending tool draft when another layer's media becomes unavailable.
- Widen both ends of a long trim at a fixed frame rate. Keep the selected FPS and stop at its exact frame-budget limit. Cover unspecified decoder color tags, edited playback of metadata-rotated clips, failed-codec fallback and actual edited export.
- Move a square crop off-center, apply a Look, hand it into Pro, and verify the rendered crop, saved edit recipe, and intact Regular draft.

## Retained automated Collage and photo verification (0.8)
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

Refer also to `docs/PROFESSIONAL_ENGINEERING_ROADMAP.md` for lawful original-code Lightroom-class **non-AI** features, RAW/license decisions and later video editing.

## Additional v0.9 gates
- [ ] Top-right mode dropdown on Regular, Pro, GIF and Collage. Video disabled / Coming soon. Regular toolbar compact, no destroyed existing tools.
- [ ] Pro image compositor: independent image edits, blend/position/size/opacity/visibility/order/undo/persist/export.
- [ ] Real GIF89a multi-frame output with tested Android decoder, timing/loop/ordering, saved project/undo and safe source ownership.
- [ ] No new permission, dependency, AI, ad, account, watermark or network regressions.
- [ ] Exact PR HEAD passed CI, merged main Android bitmap/Compose tests passed, expected APK artifact exists.
- [ ] Combined physical phone check `PHONE_TEST_WAVE8.md` for 0.8 + 0.9; commercial release and professional/RAW/video work remain open.

## v0.12.1 independent AAA audit — release gate remains OPEN
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
