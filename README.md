# GZ Editor Pro

> **Current work — November 25, 2026.** [PR #43](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/43) qualifies **0.12.13 Test / code 25 / phonetest.v25**. Candidate and merge passed JVM, build, signing, and **279/279 on API29 and API35**. R1.2 is next. Handset, B2/F03, RV5, workspace and release gates remain open. See the [exact R1.1 receipt](docs/WORK_LEDGER.md#completed-r11--bounded-saved-projects-and-pro-rv4).

**Greenezone Software LLC** · Android photo editing · Target **$2.99 one-time** purchase

**B1 interaction corrections are engineering-qualified for handset confirmation in [PR #37](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/37).** The final candidate `044e93321292a3678b92394b5efda50aae2e07b9` and application merge `46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30` pass JVM/build/signing and **258/258 cases on each of Android 10/API29 and Android 15/API35**, zero failures/errors/skips. All 193 inherited Android identities remain. The original B1 checkpoint supplied **0.12.11 Test / code 23 / phonetest.v23**. [Exact runs, raw failures, APK identity and focused phone script](docs/B1_WORKSPACE_VERIFICATION.md).

Common Options/header/history, Collage single tap, captured Save/Discard/Cancel, selected-tile/parent transactions, stable named-save identity, gesture cancellation and native/export recovery received bounded source and runtime verification. These results do not lock a workspace or complete its full capability family. B2's two measured reader optimizations preserve tested output; the prospective 10-second /50% preparation targets remain unmet, and the earlier acknowledgement miss and individual-sample limits remain recorded. F03's actual phone workload, matched cold/warm measurement and owner confirmation remain open. See the receipt for every observation, including the misses.

**Next engineering package is R1.2 recoverable Regular working drafts.** R1.1 saved-project durability and Pro RV4 are engineering-qualified in [PR #43](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/43). All 77 capability families and remaining R1–R11 children retain their agreed scope. Four explicit workspace locks precede Video. Final bilingual and customer onboarding follow acceptance of all five workspaces. Play Console setup follows the working app and Greenezone website.

Lance's 0.12.10 phone report credited unmentioned supplied checks and identified the interaction/GIF exceptions corrected or retained above. [Owner report](docs/CODEX_ALIGNMENT_HANDOFF_2026-11-24.md). PR #29/#31 correction evidence remains credited; their APKs are historical and superseded for this focused phone pass.

[Product requirements](docs/PRODUCT_SPEC.md) · [Single roadmap](docs/GZ_ENGINEERING_ROADMAP.md) · [77-family inventory](docs/CAPABILITY_MATRIX.md) · [Work ledger](docs/WORK_LEDGER.md) · [Release gates](docs/RELEASE_GATE.md). Earlier version sections below are historical checkpoints.

## Core contract
- Fully offline. No runtime AI, subscriptions, ads, account, telemetry, in-app purchases, watermarks or network permission.
- Imported photos never intentionally overwritten. Android system photo picker and save-as document provider.
- All edits are parameters in saved local project snapshots; previous projects remain decodable.
- Compact image-first interface with a consistent shared header, Undo/Redo arrow icons and one top-right Options dropdown for major file/help commands. The agreed common-chrome contract has bounded B1 engineering qualification; historical UI descriptions below describe earlier builds. Keep tool-specific controls, tap-to-hide behavior and the company footer without duplicate major command locations.
- Preserve the existing Regular photo adjustments, Looks, color, curves, Text, Blur & cover, crop and export. Regular and Collage tile editing have no Masks authoring. Older saved masks must continue to render and round-trip. Pro owns selection and masking, and preserves legacy Text layers without Text authoring.
- From a loaded Regular photo, choosing Pro offers to bring the current edited image into Pro. Choosing No opens an empty Pro editor. Regular keeps its own resolved source, edits and history when the user returns; only an explicit scoped Discard removes its working delta, as specified in the agreed transactional handoff.

## v0.12.1 independent quality review and safeguards
- Source and merged-main APK were audited. The prior v0.12.0 release **does not pass full AAA/commercial qualification**. The exact gaps, independent checks, and required signoff criteria are recorded in `docs/AAA_RELEASE_AUDIT_013.md`.
- GIF output: no longer requires a new Android Save As destination to support read-before-write, which blocks some writable document providers. Android Save As guarantees it will create a distinct filename when a name is taken. Temporary output has a bounded device-storage ceiling with a 64 MiB safety margin and explicit error messages.
- Pro project persistence: replaced a non-atomic copy fallback with Android `AtomicFile`, bounded reads, backup recovery and crash-safety tests.
- Automation now verifies APK signing and runs separate real Android emulators at API29 (minimum supported) and API35. Synthetic color-gradient/transparent animated GIF and interrupted Pro project recovery tests add coverage. These are tests, **not proofs of universal device, color-management or feature completeness**.
- No code from Regular Photo, Collage, Greenezone corporate website, XForge, GZFX or unrelated projects is changed.

## v0.12 GIF conversion and Pro canvas

The emulator job produces `gz-editor-pro-phone-test`. This versioned debug install has its own app identity and local project storage, so a handset can keep the previous build and its projects. In a local build, `-PgzPhoneTestInstall=true` selects the same package.

- Video imports begin with the full duration, source dimensions and frame timing, forward playback and neutral edits. GIF output uses an adaptive palette per frame. GIF color depth and delay precision still apply.
- Reverse, trim, speed, scrub and playback share an ordered frame schedule with export. Open an existing GIF for editing, or save an untouched imported GIF byte for byte. Optional tools include photo adjustments, HSL, curves, Looks, caption placement, rotate, mirror and framing.
- In Pro, tap the canvas to hide or restore the controls. Drag to use the selected tool. Pinch and pan to inspect detail. Hold in Layers to select an object, in Brush to sample a color, or in Clone & repair to sample and stamp.
- The layer stack supports image grading, masked adjustment layers, linked image masks, feathered selections and 16 blend modes. Search Help in GIF maker, File → Pro help in Pro, or More → Help in the regular photo editor for step-by-step instructions.

## v0.11 hard separation and interaction repairs
- **GIF Play/Pause is native video playback**, not a timer that tries to decode still frames on demand. Trim and scrub seek the real video, speed controls the native player, the **Save GIF** action is always visible and output remains a genuine animated GIF. When paused, a separately rendered frame reflects the GIF Fit/Fill/aspect settings; reverse uses timestamped frame preview because Android's native player cannot play backward. Video decoding and output codecs remain device-dependent.
- **Pro no longer launches Regular photo editing** or duplicates the Regular Light/Color/Looks tool strip. Pro is specifically for image/text/shape/brush/effect layers, on-canvas transform/rotation/centering, rectangle/ellipse/freehand layer masks, Clone stamp, Soft repair, painting and real brush erasure, blend/compositing effects, history and editable projects.
- Pro project storage is v3, automatically reading legacy v1/v2 projects. Regular and collage workspaces remain separate and unchanged.
- These are major architectural corrections, not a claim of full Photoshop/GIMP parity. Missing professional selection refinement, smarter healing, advanced eraser/brush dynamics, groups/adjustment masks and production optimization are tracked in `docs/PRO_GIF_REBUILD_011.md`.

## Major production repair wave (v0.10)
- **GIF is now video-first**: import an actual local video, preview and select a clip up to twelve seconds, choose 4/6/8/10 FPS, 50%/100%/200% speed, reverse or forward, trim/scrub, Fit/Fill, canvas output and loop. Android's MediaMetadataRetriever decodes timestamped video frames one by one; a genuine GIF89a file is exported. Previously saved photo-sequence projects remain accessible through **Open saved GIF project**, but the new-project screen is video-first and offers no photo-sequence creation controls. No audio is included in GIFs. This is *not* the full video editor, which remains a later phase.
- **Photo Pro uses a typed layer stack**: bitmap, text, shape, vector brush and non-destructive global adjustment layers can be independently ordered, hidden, styled, drawn or edited. One selected layer at a time, with direct canvas gestures, grouped inspectors, controls for blending/opacity/transforms, local project persistence and quick help. Earlier v1 Pro projects are still decodable. No claim of Photoshop feature parity yet.
- The existing regular photo editor and seven-layout collage were not redesigned. The complete user feedback and remaining professional-grade work are in `docs/PRODUCTION_REBUILD_010.md`.
- Qualification requires exact branch/PR + merged-main JVM and Android instrumentation checks and a physical handset review. Android's native frame retrieval is timestamp-approximate and some hardware codecs may refuse particular HDR/protected videos. GIF output uses indexed colors with frame-adaptive palettes.

## Separate editing workspaces (v0.9)
- **Top-right Photo ▾** selects Photo editing (regular), Photo editing Pro, Collage maker, GIF maker or Video editing (Coming soon and disabled). Each workspace owns its editing controls.
- **Regular Photo editing** has a compact set of familiar core controls, with previous source image and editing session retained when switching workspaces.
- **Photo Pro** has real image layers (up to 12), independent existing photo editor per layer, reorder/duplicate/hide/delete, position/size/opacity and Normal/Multiply/Screen/Overlay blending. Non-destructive project save/reopen, Undo/Redo and full image export. Text/drawing/shapes are editable *inside* each image layer. A complete universal Photoshop-style layer stack, heal and selection tools remain a future engineering target.
- **GIF maker** accepts 2–20 photos, reorder/remove/preview/play, frame timing, loop, output size/ratio/Fit/Fill, editable local projects and actual animated GIF89a with original-code LZW encoding. GIFs do not carry audio; a photo with music needs future MP4 video export.
- **Video editing comes last**, is not implemented, and has no fake working controls. The complete non-AI Lightroom/Photoshop-like still-photo, GIF, music and video scope is in `docs/UNIFIED_PRODUCT_SCOPE_09.md`.
- No additional third-party dependencies, accounts, AI, ads, watermarks or online services were introduced for this wave.

## New collage workspace (v0.8)

- Open **Create collage** from the empty main screen or from **More** in the editor.
- Import 1–20 photos through Android's scoped multi-photo picker. The app enforces the 20-photo cap even on picker fallbacks.
- Arrange in seven original patterns: Grid, Side by side, Stacked, Featured, Filmstrip, Magazine and Mosaic.
- Six aspect ratios; adjustable tile spacing, outer margin, rounded corners, border, color/transparent background, Fit whole photo or Fill tile; Standard/High/Ultra quality with adaptive device-memory cap.
- Tap a collage tile, choose **Photos → Edit selected** to open the **entire existing photo editor** on that specific tile, including original grading, masks, text, crop, blur/covers and drawing. **Done** writes edits back to that tile only. Other source photos and their edits stay independent.
- Add, replace, reorder and remove photo tiles, with bounded **collage Undo/Redo** for layout and tile changes. Save up to twelve named local editable collages referencing your original image URIs. If a media provider revokes access later, replace the affected tile; photos are not silently copied to cloud.
- Export the completed collage using existing real JPEG/PNG/WebP/BMP/TIFF/PDF encoders and optional social size Fit/Fill presets; no watermarks.
- Active crop/blur/text/shape and brush guides now remain visible even while the main editing toolbar is hidden, so tool selection is not lost during photo interaction.
- Minimal empty landing screen includes **GZ Editor Pro** top-left, **Open media** and **Create collage**, with company copyright bottom-left.

## New editing capabilities
- **Text:** editable typed captions (up to 500 characters each), 9 built-in Android font choices, colors, size, width, height, move/resize via on-image frame, rotation, bold, italic, alignment, outline/outline color, shadow, opacity and character spacing. Up to 32 text objects.
- **Draw:** real colored vector drawing, adjustable brush thickness and opacity, 11 color presets, vector eraser and Undo/Clear. Up to 160 strokes, capped to 500 path points each in saved project data.
- **Shapes:** rectangle, ellipse, line, arrow, triangle and star; position, resize, rotate, fill/stroke, color and opacity. Up to 64 objects.
- **Manual crop:** draggable on-photo corner and edge handles, a movable crop frame, freeform crop and constrained aspect ratios; previous preset ratios and straighten preserved. New crop frame saved in editable project and respected by export; image stays uncropped while setting frame, then crops on exiting Crop.
- **Export dropdowns:** one for **actual image file format** (JPEG, PNG, WebP lossy, WebP lossless, BMP, TIFF and one-page PDF) and another for **destination size**, including Instagram, Facebook, TikTok, YouTube, Pinterest, LinkedIn, X and Snapchat layout presets. Presets output exact requested pixels; Fit keeps the whole edited picture with optional borders, Fill crops edges. JPEG/WebP quality options.
- BMP and TIFF use local streaming uncompressed writers. PDF uses Android's native PDF engine. No filename-extension spoofing.
- All creative layers are composited in preview/export after privacy redaction; no branding, timestamp or watermark added to the exported photo.

## Limits and safeguards
- Android 10+ (API29), JDK17, Kotlin/Compose, Gradle9.6, compile/target API36. The production application ID is `com.greenezone.gzphotoeditor`; versioned phone-test builds use a separate suffix, currently `com.greenezone.gzphotoeditor.phonetest.v25`. Their local project storage is separate.
- Renderer uses 8-bit sRGB, not a RAW/16-bit managed photo pipeline. New TIFF is uncompressed 8-bit RGB, not 16-bit.
- Social dimensions are practical composition presets, **not guarantees of platform acceptance**, safe-area rules or uploaded file size. Platforms may change their policies.
- Export presets may upscale lower-resolution source photos; this cannot recover missing detail. Fit can add margins; Fill can crop. The user selects explicitly.
- Drawing Eraser cuts visible vector segments along the swept brush path. If a result would exceed the existing 160-stroke limit, the whole gesture is restored with a visible notice; unrelated artwork is preserved. Video editing is not yet implemented and stays disabled in the mode menu.
- Very large photo output is limited by device memory. Custom-font file imports, text-on-path, automatic subject isolation and PSD project import remain unimplemented.
- Blur/pixelation are not secure redaction. Use a fully opaque black covering for sensitive material.

## Build and test

The [qualified application commit `9a58c72c`](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/9a58c72cdcf379885150b55b498a2f0ef326dee1) matches the reviewed candidate tree. [Merged verification](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45706255531) passed JVM/build/signing and 265/265 on both Android API29 and API35.

[Download the merged API35 phone APK ZIP](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45720375226/artifacts/15073420431). Extract `app-debug.apk` for **GZ Editor Pro 0.12.13 Test**, code 25, `com.greenezone.gzphotoeditor.phonetest.v25`, Android 10+. Each package has separate project storage. See the [exact R1.1 receipt](docs/WORK_LEDGER.md#completed-r11--bounded-saved-projects-and-pro-rv4).

See `docs/CONTINUITY.md`, `docs/PRODUCT_SPEC.md`, `docs/RELEASE_GATE.md`.

**Boundaries:** do not change Greenezone corporate website, DNS, hosting, XForge, GZFX, Beside-Me or any unrelated repository.

Copyright © 2026 Greenezone Software LLC. All rights reserved.
