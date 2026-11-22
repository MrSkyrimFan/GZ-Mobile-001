# GZ Editor Pro — continuity / engineering custody

## Most recent owner directive (2026-11-21)
Preserve the existing easy photo-first UI and all working features. Put **GZ Editor Pro** at top left during editing. Show company **© 2026 Greenezone Software LLC** bottom-left on initial empty screen. Add real editable words/text in many fonts with width/height adjustment, freehand drawings/shapes, a manual draggable crop frame with freeform mode, more export file formats via dropdown, and a **separate** platform sizing dropdown for Instagram/Facebook/TikTok and other popular destinations. One large coherent implementation and test round, not endless microbuilds. User provided four screenshot references after GZ Editor Pro 0.6, verifying Blur & cover tools and opaque bar output. Existing UI must not be spoiled.

## Repos / identifiers
- Single authorized repo: `MrSkyrimFan/GZ-Mobile-001`.
- Baseline GZ Editor Pro 0.6 merge: `2a0f11b7eef2c5b42e5f569890b694a1dd7ba265`.
- Engineering branch: `feature/gz-editor-pro-creation-export-07`.
- Application ID unchanged: `com.greenezone.gzphotoeditor`; launcher name GZ Editor Pro. Target Android10+, compile/target SDK36.
- Price target $1.99 one-time, no AI runtime, ads, watermarks, IAP, subscriptions, tracking, remote services. Open media currently supports **images only**.

## Architecture
- Save source image only by content URI, edit non-destructively.
- Grade + masks + privacy operations + creative overlays share preview/export pipeline; social export composition happens at the end.
- Text/ink/shapes and normalized crop frame persisted in backwards-compatible project codec; regenerate from source at final resolution.
- Export supports JPEG/PNG/WebP lossy/lossless/BMP/TIFF/PDF with matching MIME and real encoders.
- Social presets use specified pixel dimensions with explicit Fit/Fill. Not automatic social upload or guaranteed platform acceptance.
- The prior app UI, horizontal categories, 50 Looks, curves, colors, privacy masks, undo/redo all remain.

## Hard rules
Review source, test JVM and real Android bitmap/Compose, verify PR commit SHA, merged-main workflow and APK artifact. Don't claim final Play-ready app without physical phone and policy release gates. Do not change the Greenezone corporate website, registrar, DNS, XForge, GZFX, Beside-Me or unrelated repo settings.
