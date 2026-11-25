# GZ Editor Pro product requirements

## Latest owner requirements — shared UI and safe departure

The November 24, 2026 phone report says all unmentioned supplied tests worked. Named exceptions are Collage's quick-double-tap requirement, missing dirty-exit safeguards in Regular/Pro/Collage, and GIF Save taking 2–5 minutes. GIF controls now work. See the [owner phone report and Codex alignment handoff](CODEX_ALIGNMENT_HANDOFF_2026-11-24.md) for the full report, exact 25 questions and acceptance criteria.

The following are requested targets, **not claims that the current APK already implements them**:

- Use one consistent header/style/spacing/control arrangement across Regular, Pro, Collage and GIF, inherited by future Video only after its gate. Preserve the dark image-first editing surfaces and separate toolsets.
- Put a clearly titled **Options** dropdown at the top-right in every workspace. Major commands belong there: New/Open media, Open projects, Save project, Export and Help, with source-appropriate labels and relevant additional actions. Distinguish editable-project Save from finished-file Export.
- Remove duplicate visible locations for the same major action, including surviving File/More/header/dock equivalents. Keep direct editing, playback, Undo/Redo and selection-specific Done/Cancel controls. Inventory empty-state duplicates explicitly.
- Use the same paired Undo/Redo arrow icons in the same order/positions, with accessible names and consistent enabled/disabled behavior.
- Remove Regular's **More** section entirely. Remove its **Create collage** and **Histogram** UI entries rather than moving them to Options. Collage remains a separate workspace; preserve needed underlying image-processing/diagnostic code.
- Restore one-tap hide/show in parent Collage and selected-tile editing while keeping active guides and gestures usable.
- Protect dirty work on relevant workspace exit and replacement routes through the agreed Save/Discard/Cancel contract. Cancel or failed/canceled Save retains the exact work/route; Discard affects only the intended working changes. Preserve transient Back precedence, originals, named saves, independent tiles and Regular→Pro transfer custody.
- Measure and improve the actual phone GIF Save workload without silently reducing dimensions/FPS/frames/quality/effects or weakening existing fixture gates. Device/source/output details remain unknown until supplied or measured.

The owner explicitly authorizes these named UI changes after the required alignment. Older blanket layout-preservation wording does not block them. [A1 in WORK_LEDGER](WORK_LEDGER.md) keeps application engineering paused until Codex answers 25, provides its full roadmap, asks 50, and ChatGPT answers/reconciles them and issues the final resume prompt.

## Current scope

**Current engineering result:** [PR #31](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/31) corrects the two independent PR #29 Eraser findings and the observed GIF Save performance failure. Its qualified **0.12.10 Test / code 22** build was delivered for the owner's combined handset pass; the latest owner result and named exceptions are recorded above. Candidate and application merge each pass 193/193 per API29/API35. The exact app commit is `3ad65c533f7667a136d4fa1838afcc06d3f484da`; later documentation-only commits record this result without changing the tested app. [Evidence and APK](ERASER_REPAIR_VERIFICATION.md) · [Current alignment and queued R1.1/R1.2](WORK_LEDGER.md).

Eraser cuts rendered line segments and the swept brush path. A result exceeding the existing stroke limit restores the whole gesture, preserving unrelated artwork, dirty state and history; persistence rejects an invalid count. The GIF optimization keeps exact pixel results, output size, frames, delays and established budgets. Mode boundaries and the established visual style remain required alongside the newly authorized common chrome. The recorded qualification closes its named engineering corrections; the latest phone findings remain open, and full Draw/GIF families and commercial release are not complete.

### Retained PR #29 baseline

The November 23 audit and PR #26 evidence record the prior application baseline. Lance supplied the independent follow-up review on November 24 and authorized corrections and the complete professional-app roadmap. The former owner-review hold is lifted; the four corrections are now engineering-verified in [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29), with 186/186 Android cases per platform on candidate and merge. That v0.12.9 Test checkpoint is historical and superseded for current phone testing by PR #31. The latest phone report above supplies scoped passes and exceptions; A1 alignment is current, with R1.1/R1.2 retained as queued durability work. [Active roadmap](GZ_ENGINEERING_ROADMAP.md) · [Capability inventory](CAPABILITY_MATRIX.md) · [Work ledger](WORK_LEDGER.md) · [Continuity](CONTINUITY.md) · [Release gates](RELEASE_GATE.md).

- **Regular and Collage.** Preserve the familiar photo-first interface, Text, Draw, Shapes, Blur & cover, and independent edits across all 20 Collage tiles. Regular and the selected-tile editor have no visible Masks authoring. Keep saved mask data, rendering and round-trip compatibility. Give all four Crop corners and all four edges density-aware touch targets of at least 48 dp and select the drag target on pointer down. Preserve predictable Freeform and fixed-ratio geometry and exact exported crops.
- **Regular → Pro.** From a loaded standalone Regular photo, selecting Photo editing Pro presents a choice to continue the current image with its edits. Yes opens the current edited result in Pro, preserving its orientation, crop and appearance. No opens an empty Pro editor with “Select an image to edit.” Dismissing the choice leaves Regular open. Regular keeps its own source, edits and history when the user switches back, and stays independent of any Pro changes. Saving, deleting or opening a new Regular image follows the normal Regular commands. A failed handoff keeps the Regular work safe and explains the failure.
- **Pro.** Masks, selections, layers, canvas tools and retouching belong here. Pro has no Text creation or editing. Preserve and render legacy Pro Text layers and keep Shapes accessible. Provide Options → Delete current work, moving the existing File command into the common dropdown, with an explicit discard warning and confirmation. Cancel keeps the current workspace intact. Confirmation clears the current working edits and history and returns to the original or blank starting state. Preserve source media and named saved projects. Preserve Undo/Redo behavior through the common arrow controls. Back closes the active transient context first and then returns to the correct workspace with recoverable work. Make controls meaningful for the selected layer and explain blend behavior and targets without changing correct blend math.
- **GIF.** Playback must visibly advance and stay responsive after trim, Reverse, Looks, speed, canvas and other edits. Verify preview, scrubbing, project reopen and actual animated export on representative media. Use the owner's portrait workflow as a reproduction target. A moving frame number alone cannot demonstrate playback.
- **Later pre-Video scope.** Regular gets an offline sticker menu with at least 500 usable finished assets, transparent custom PNG/WebP imports, editing and persistence, and independent sticker editing on selected Collage tiles. Pro gets the offline Subject & Background tools for subject selection, background removal or replacement, object removal, edge refinement, and reshaping, with editable masks/layers and full-resolution export. See the [complete roadmap](GZ_ENGINEERING_ROADMAP.md).
- **Complete scope.** The roadmap and capability inventory include required RAW/color/catalog/batch, full Pro/Subject & Background, GIF/Collage, Video, reliability and release families. Present buttons and narrow passing fixtures are foundations, not completion of the full professional scope.
- **Account timing.** Play Console is not set up. Lance requires setup last, after a fully functioning app and completion of the Greenezone website. Website implementation is outside this task.
- **Release decisions.** Regular and Collage retain their protected provisional status. GIF and Pro remain open for engineering and handset qualification. The separate Video editor stays disabled until the owner formally locks all four pre-Video workspaces. Bilingual English/Spanish welcome and legal acceptance belong to the final phase. The app stays offline with a one-time purchase and no runtime AI.

## Retained photo and Collage requirements from 0.8

- Preserve the user-approved photo-first dark UI and working Regular tools, with the specifically authorized common header/Options and More/Create collage/Histogram removal above.
- Restore full **GZ Editor Pro** top-left title; keep company `© 2026 Greenezone Software LLC` bottom-left on empty main screen.
- Add flexible typography with multiple fonts and editable width/height; drawing and shapes; manual crop handles/freeform; export file-format dropdown separate from destination-platform dropdown.
- Existing screenshots demonstrated that the privacy tools, uncropped photo canvas and Reset were now useful. Do not regress those.
- User requests one large coherent engineering batch and **one** Android phone verification, not countless little pushes.

## Engine / document changes
`CreativeModel.kt`, `CreativeRaster.kt`, `InkMath.kt`, `CropMath.kt`, `ImageExportWriter.kt`, `CreativeCanvasOverlays.kt`, `CreativeDecks.kt`, edit/codec/render/UI integration and automated tests.

Text, ink and shapes remain editable data in `EditDocument.creative`. The keyed local format continues to read version 1. A recipe with source-anchored privacy geometry uses version 2. Free crop is `CropFrame` normalized on the oriented/straightened photo; legacy crop center settings continue to load for previously created projects. Main render draws pixel adjustments, privacy operations, then creative overlays, preserving source image. Export sizes are composed **after** all edits on the rendered copy.

## Export behavior

Export a captured recipe and source. Persist the pending operation before opening Android’s destination picker. After a possible interrupted provider write, offer the retained recipe with a fresh destination. Regular and Pro show the expected dimensions after crop, orientation and device export limits.
File formats: JPEG, PNG, WebP lossy, WebP lossless, BMP 24-bit RGB, TIFF uncompressed RGB 8-bit, PDF one page. Browser/Gallery preview support for BMP/TIFF is not universal. JPEG/BMP/TIFF/PDF flatten transparency to white. Quality only for lossy formats or WebP lossless compression effort. WebP lossless on Android 10 uses the documented legacy 100-quality format.

Social presets: original, Instagram square/portrait/story/reel cover, Facebook square/landscape/story, TikTok photo 9:16, YouTube HD/4K thumbnail, Pinterest pin, LinkedIn square, X landscape, Snapchat story. Fit pads to exact canvas size and preserves all content, Fill crops edges and fills full canvas. No automatic posting, uploader, network or API.

## Verification levels
- Separate pure Kotlin and Android emulator tests, including crop geometry and corner ratio, codec legacy/snapshot roundtrip, text fonts and raster pixels, shapes and ink, export format signatures, social exact target size, alpha behavior.
- PR CI, merge verified SHA, merged-main CI and exact APK artifact.
- The delivered 0.12.10 phone session is reported: unmentioned supplied checks worked, with the named exceptions above. After the agreed corrections, request only the focused remaining phone observations and concrete regression checks, including realistic GIF Save settings/timing and the new common UI/dirty-exit behavior.
- Commercial release still blocked by real-device UX/memory/color, legal/privacy/trademark, Play compliance and production signed AAB.

**No external website or project changes.**

## Major 0.8 integration
- Independent collage document of 1–20 image content URIs and full per-tile EditDocument. Do not flatten individual edits before final export.
- Offline seven template compositions, per-tile edit/reorder/replace/delete, aspect/spacing/radius/border/background/Fill-Fit, bounded preview and output, saved named collages.
- Android picker may fall back to ACTION_OPEN_DOCUMENT without enforcing maximum selection; always clamp imported sources to twenty.
- The photo editor itself is retained as the individual tile editor. The parent photo edit session stays intact when opening/closing Collage.
- Selected crop/blur/text/shape/brush editing guides are **always visible for the active tool**, independent of the main toolbar hidden state. Main UI chrome can be hidden to reveal the whole photo.
- Home screen GZ Editor Pro title restored without marketing slogans.
- Professional and legal roadmap: `docs/PROFESSIONAL_ENGINEERING_ROADMAP.md` (RAW/color/catalog/batch/video) with original engineering and license gates.

## Qualification
Android source + math + emulator compositor and UI checks, exact merge SHA, main APK artifact, one field pass. These are not commercial release certification.
