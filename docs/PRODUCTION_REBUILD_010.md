> **Historical checkpoint — current route updated November 25, 2026.** Follow the [GPT-6.2 Sol Ultra successor handoff](NEW_CHAT_HANDOFF_2026-11-25.md), [current ledger](WORK_LEDGER.md) and [PR #37 / 0.12.11 Test evidence and focused phone script](B1_WORKSPACE_VERIFICATION.md). Current task: read-only successor onboarding, then wait for Lance's ongoing GPT-6 Pro review. Original text below retains its dated evidence; its “current”, “next”, delivery, engineering and test commands are historical, not fresh instructions. Do not replay completed intake, question exchanges, builds or phone scripts. The [current review brief](coordination/GPT_6_PRO_REVIEW_BRIEF_2026-11-25.md) supersedes older review prompts.

# GZ Editor Pro 0.10 — user-field corrective rebuild

## Decision and evidence
- **Owner report:** v0.9 collage works fairly well; GIF-maker photo-sequence-first UX does not fulfill video-to-GIF intent; Pro image stack is confusing, inadequate and unlike an actual layered editor; regular UI is learnable but needs a tutorial page eventually. User demands AAA-level engineering quality with real regressions, not fake controls.
- **Do not falsely claim** that one patch equals a million-dollar corporation's complete app. The product target is the actual quality of every tool, consistent and readable controls, stable projects and safe exports. Price target remains **$2.99 one-time**, offline, no runtime AI/ads/account/tracking/microtransactions.
- **Protected baseline:** v0.9 merged `main` commit `19e54836cf850fd7af1f9eaef06b0ce366c5f9c9`; independent collage workspace and approved regular editor retained. New code is on `feature/production-gif-video-pro-overhaul-10`; only authorized repository may be changed.

## Implemented engineering (candidate until CI/device pass)
### Video to GIF
- Android native media picker with video-only filter and source URI custody. Metadata inspected off UI thread.
- MediaMetadataRetriever scaled decoded frames at selected millisecond timestamps. Bounded 0.5–12 second clip; 4/6/8/10 FPS and maximum 120 output frames. Native decoder's nearest-frame accuracy varies with source codec and device.
- Clip start/end, timeline frame scrub, video preview, forward/reverse, speed options, loop, Fit/Fill, canvas ratio/size, local project save, Undo/Redo and GIF89a LZW export; frames processed one at a time rather than keeping all raw frames in memory. Original videos are never written to.
- Existing saved photo-sequence GIF projects remain recoverable through **Open saved GIF project**, without putting photo-sequence creation on the main screen. GIF cannot contain sound; photo/video+audio MP4 functionality waits for the later video phase.
- v2 GIF project format supports video-source settings and retains v1 photo-sequence compatibility. Synthetic local 2s H.264 test fixture exists solely for Android instrumentation decoding; no third-party runtime encoder or decoder added.

### Pro overhaul
- True ordered image/text/shape/drawing/adjustment layer kinds with independent visibility, opacity, blend and saved edit data, plus Undo/Redo.
- Vector text/shapes/brushes reuse existing author-owned raster pipeline directly on Canvas, not flattened onto source images. Adjustment layer grades combined underlying pixels. Image layer retains full standalone editing controls.
- Direct on-canvas move/painting, simplified selected-layer inspector, Add layer chooser, typography/shapes/colors, basic brush history, Quick guide. Existing Pro v1 saved project payload decodes through v2 migration.

## Explicit future professional engineering work (not shipped)
- Accurate layer-selection hit testing and resize/rotation handles; photo clipping and feather masks; multiple editable selection shapes, lasso/magic-color, source clone/healing, paint eraser, editable adjustment masks, group/clipping, dedicated text layout handles.
- RAW/DNG, 16-bit/float managed ICC color, histogram/clipping and lens profiles, professional denoise, catalog and batch workflow, accessibility/TalkBack and orientation support, atomic safe-save rollback, performance and huge-file memory profiling.
- GIF palette optimization/quantization, better quality/dithering, animated overlays, frame-accurate extraction via MediaCodec when warranted. Full video/audio timeline **remains last**, as previously ordered.
- Proper interactive onboarding/tutorial page, searchable context help and screen-reader instructions.

## Test and release gates
- JVM geometry, strict codec/model decode and v1 migration tests.
- Android emulator text/shape/brush/composite pixels, video-to-GIF using actual synthetic MP4 through owned MediaStore content URI, GIF89a decoder, legacy photo/collage regression, full editor UI smoke.
- Full feature/UX/power/crash pass on the physical Android phone; test results must not be inferred from emulator. A one-pass checklist is in `docs/PHONE_TEST_WAVE9.md`.
- No claim of patent/trademark clearance, universal media codec support, Play Store readiness or full Photoshop parity. Conduct IP and source dependency/licensing review before commercial distribution.

**Scope:** Do not change Greenezone website/DNS, XForge, GZFX, Beside-Me or unrelated repositories.
