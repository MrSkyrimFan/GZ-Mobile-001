# GZ Editor Pro

**Greenezone Software LLC** · Android photo editing · Target **$1.99 one-time** purchase

**Status: version 0.7.0 engineering qualification candidate. Not published on Google Play.**

## Core contract
- Fully offline. No runtime AI, subscriptions, ads, account, telemetry, in-app purchases, watermarks or network permission.
- Imported photos never intentionally overwritten. Android system photo picker and save-as document provider.
- All edits are parameters in saved local project snapshots; previous projects remain decodable.
- Compact image-first interface: GZ Editor Pro top-left, Open media, Undo/Redo, Before, Export, swipeable bottom tool dock, tap to hide editing chrome, company copyright bottom-left on empty main page.
- Preserve all prior 56 global adjustments, 50 Looks, HSL color mixer, grading, 4 curve channels, 3 adjustment-mask types, Blur & cover, crop/rotate/flip/straighten, project/preset storage, JPEG/PNG export and transparent output where supported.

## New editing capabilities
- **Text:** editable typed captions (up to 500 characters each), 9 built-in Android font choices, colors, size, width, height, move/resize via on-image frame, rotation, bold, italic, alignment, outline/outline color, shadow, opacity and character spacing. Up to 32 text objects.
- **Draw:** real colored vector drawing, adjustable brush thickness and opacity, 11 color presets, vector eraser and Undo/Clear. Up to 160 strokes, capped to 500 path points each in saved project data.
- **Shapes:** rectangle, ellipse, line, arrow, triangle and star; position, resize, rotate, fill/stroke, color and opacity. Up to 64 objects.
- **Manual crop:** draggable on-photo corner handles, move box, freeform crop and constrained aspect ratios; previous preset ratios and straighten preserved. New crop frame saved in editable project and respected by export; image stays uncropped while setting frame, then crops on exiting Crop.
- **Export dropdowns:** one for **actual image file format** (JPEG, PNG, WebP lossy, WebP lossless, BMP, TIFF and one-page PDF) and another for **destination size**, including Instagram, Facebook, TikTok, YouTube, Pinterest, LinkedIn, X and Snapchat layout presets. Presets output exact requested pixels; Fit keeps the whole edited picture with optional borders, Fill crops edges. JPEG/WebP quality options.
- BMP and TIFF use local streaming uncompressed writers. PDF uses Android's native PDF engine. No filename-extension spoofing.
- All creative layers are composited in preview/export after privacy redaction; no branding, timestamp or watermark added to the exported photo.

## Limits and safeguards
- Android 10+ (API29), JDK17, Kotlin/Compose, Gradle9.6, compile/target API36; package stays `com.greenezone.gzphotoeditor` to preserve installation/project continuity when signing keys agree.
- Renderer uses 8-bit sRGB, not a RAW/16-bit managed photo pipeline. New TIFF is uncompressed 8-bit RGB, not 16-bit.
- Social dimensions are practical composition presets, **not guarantees of platform acceptance**, safe-area rules or uploaded file size. Platforms may change their policies.
- Export presets may upscale lower-resolution source photos; this cannot recover missing detail. Fit can add margins; Fill can crop. The user selects explicitly.
- Drawing eraser splits vector strokes at erased points, not a pixel-based clone/heal tool. No video editing despite the button label Open media.
- Very large photo output is limited by device memory. Custom-font file imports, movable individual crop-edge handles, text-on-path, arbitrary masking or PSD project import remain unimplemented.
- Blur/pixelation are not secure redaction. Use a fully opaque black covering for sensitive material.

## Build and test
GitHub Actions **GZ Editor Pro Android CI** runs Android compilation, JVM geometry/codec tests, Android emulator image/format/Compose tests and uploads debug-signed artifact `gz-editor-pro-debug` containing `app-debug.apk`. A passing automated build does **not** establish physical-device usability, commercial readiness or professional color fidelity.

See `docs/CONTINUITY.md`, `docs/PRODUCT_SPEC.md`, `docs/RELEASE_GATE.md`, `docs/PHONE_TEST_WAVE6.md`.

**Boundaries:** do not change Greenezone corporate website, DNS, hosting, XForge, GZFX, Beside-Me or any unrelated repository.

Copyright © 2026 Greenezone Software LLC. All rights reserved.
