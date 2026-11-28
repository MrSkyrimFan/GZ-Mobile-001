# GZ Editor Pro

> **Current work — November 26, 2026.** [PR #49](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/49) contains the U1 review corrections. Exact [PR #50](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/50) candidate is held at **717.745 ms** against the **650 ms** API35 R1.1 per-Open limit. [Current disposition](docs/WORK_LEDGER.md#u1-review-correction-and-qualification-hold). PR #46 / **0.12.14 Test, code 26** retains its recorded 303/303 engineering qualification. Focused review and a qualified replacement APK precede the next handset recommendation. Remaining R1.2 and later gates stay in scope.

**Greenezone Software LLC** · Android photo editing · Target **$2.99 one-time** purchase

**Current engineering and phone status** — The U1 corrections have focused runtime evidence. The exact qualification and handset recommendation await the API35 R1.1 disposition. Earlier owner passes retain their recorded scope.

**Next engineering action** — Reconcile the [exact qualification hold](docs/WORK_LEDGER.md#u1-review-correction-and-qualification-hold). Remaining R1.2 follows under the single roadmap.

[Product requirements](docs/PRODUCT_SPEC.md) · [Workspace blueprint](docs/WORKSPACE_UX_BLUEPRINT.md) · [GPT-6 Pro review handoff](docs/coordination/GPT_6_PRO_U1_FOLLOWUP_REVIEW_HANDOFF_2026-11-26.md) · [Single roadmap](docs/GZ_ENGINEERING_ROADMAP.md) · [77-family inventory](docs/CAPABILITY_MATRIX.md) · [Work ledger](docs/WORK_LEDGER.md) · [Release gates](docs/RELEASE_GATE.md).

## Core contract
- Fully offline. No runtime AI, subscriptions, ads, account, telemetry, in-app purchases, watermarks or network permission.
- Imported photos never intentionally overwritten. Android system photo picker and save-as document provider.
- All edits are parameters in saved local project snapshots; previous projects remain decodable.
- Compact image-first interface with a consistent shared header, Undo/Redo arrow icons and one top-right Options dropdown for major file/help commands. The agreed common-chrome contract has bounded B1 engineering qualification; historical UI descriptions below describe earlier builds. Keep tool-specific controls, tap-to-hide behavior and the company footer without duplicate major command locations.
- Preserve the existing Regular photo adjustments, Looks, color, curves, Text, Blur & cover, crop and export. Regular and Collage tile editing have no Masks authoring. Older saved masks must continue to render and round-trip. Pro owns selection and masking, and preserves legacy Text layers without Text authoring.
- Regular↔Pro uses one Discard / Save current work / Bring current work / Cancel surface. Bring carries current edits and editable native lineage without a named Save; Save succeeds then opens blank, Discard opens blank, Cancel changes nothing. PR #49 implements the linked native-edit, failed-Open ownership and held-Save acknowledgement repairs. The current engineering hold applies to full candidate qualification.

> **Historical version notes below:** descriptions of File/More, earlier tool placement, old transfer rules, limits and prior next actions are dated evidence. Current PRODUCT_SPEC/WORKSPACE_UX_BLUEPRINT and the ledger supersede conflicting directions. The completed historical stages are not new dispatch gates.

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
- Android 10+ (API29), JDK17, Kotlin/Compose, Gradle9.6, compile/target API36. The production application ID is `com.greenezone.gzphotoeditor`; versioned phone-test builds use a separate suffix, currently `com.greenezone.gzphotoeditor.phonetest.v26`. Their local project storage is separate.
- Renderer uses 8-bit sRGB, not a RAW/16-bit managed photo pipeline. New TIFF is uncompressed 8-bit RGB, not 16-bit.
- Social dimensions are practical composition presets, **not guarantees of platform acceptance**, safe-area rules or uploaded file size. Platforms may change their policies.
- Export presets may upscale lower-resolution source photos; this cannot recover missing detail. Fit can add margins; Fill can crop. The user selects explicitly.
- Drawing Eraser cuts visible vector segments along the swept brush path. If a result would exceed the existing 160-stroke limit, the whole gesture is restored with a visible notice; unrelated artwork is preserved. Video editing is not yet implemented and stays disabled in the mode menu.
- Very large photo output is limited by device memory. Custom-font file imports, text-on-path, automatic subject isolation and PSD project import remain unimplemented.
- Blur/pixelation are not secure redaction. Use a fully opaque black covering for sensitive material.

## Build and test

The [qualified application commit e6321322](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/e6321322526521ace13a45c231b25bb08f91c565) matches the PR #46 candidate tree. [Merged verification](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45751851847) passed JVM/build/signing and 303/303 on each Android API. See the [U1 qualification receipt](docs/WORK_LEDGER.md#completed-u1--coherent-workspace-workflow).

[Retained 0.12.14 APK evidence ZIP](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45751851847/artifacts/15085954966). This contains **GZ Editor Pro 0.12.14 Test**, code 26, `com.greenezone.gzphotoeditor.phonetest.v26`, Android 10+. Its versioned package keeps separate local project storage. The next handset recommendation is held on the exact R1.1 qualification result above.

See `docs/CONTINUITY.md`, `docs/PRODUCT_SPEC.md`, `docs/RELEASE_GATE.md`.

**Boundaries:** do not change Greenezone corporate website, DNS, hosting, XForge, GZFX, Beside-Me or any unrelated repository.

Copyright © 2026 Greenezone Software LLC. All rights reserved.
