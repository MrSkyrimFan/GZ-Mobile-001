# GZ Editor Pro 0.8 — collage engineering and release gates

## Mandatory automated verification
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
