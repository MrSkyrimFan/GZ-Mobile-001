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

## Collage wave v0.8.0 (2026-11-21 local)
Current verified main baseline before this wave: `7508ff715d39dbb13ae543961b943cee5c78c22b` (GZ Editor Pro 0.7).
Branch: `feature/collage-workspace-photo-08`.
The owner added an explicit 2–20-photo collage requirement, independent photo editing inside tiles and high-quality layout templates. They supplied Lightroom's *functional* feature comparison as roadmap input and prohibit runtime AI, infringement and improperly licensed dependencies. No claims of zero legal risk.

Core source: `CollageModel.kt` for cell geometry/templates, `CollageRaster.kt` for incremental tile composition, `CollageStore.kt` for local project custody and `CollageWorkspaceScreen.kt` for a separate swipeable collage workflow. Route through existing `PhotoEditorScreen` so original photo-session state is retained and each tile opens the existing entire editor.
Fix prior screenshots' crop/blur/text/shape guides: editing overlays remain visible while UI toolbars hide, and the current tool remains interactive. Restore GZ Editor Pro top-left on empty launch.

Next future phase: same-app video editing via independently reviewed Media3 Transformer and frame-indexed edit documents. No video feature is claimed shipped. Source research and legal/patent/license gates live in `docs/PROFESSIONAL_ENGINEERING_ROADMAP.md`.

**Do not alter corporate Greenezone website or other repos.** Test in CI before merging, then one consolidated real-phone check.

## Final integration polishing
The collage header uses two compact rows to prevent clipping, exposes an always-available Add action once photos exist, plus Undo/Redo. Snapshot history includes tile additions/removals/order/edits and layout operations; slider gestures produce one checkpoint. Source-URI export collision is blocked, nesting a collage while editing a tile is disabled, and coroutine cancellation is propagated. Tests added for history and maximum gap with 20 photos.
