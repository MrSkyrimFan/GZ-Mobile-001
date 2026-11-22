# GZ Editor Pro 0.7 integrated creative/export wave

## Owner decisions and field evidence
- Preserve the user-approved photo-first dark UI and existing editing functions. Do not restructure Looks, Light, Color, Masks, Blur & cover or move buttons gratuitously.
- Restore full **GZ Editor Pro** top-left title; keep company `© 2026 Greenezone Software LLC` bottom-left on empty main screen.
- Add flexible typography with multiple fonts and editable width/height; drawing and shapes; manual crop handles/freeform; export file-format dropdown separate from destination-platform dropdown.
- Existing screenshots demonstrated that the privacy tools, uncropped photo canvas and Reset were now useful. Do not regress those.
- User requests one large coherent engineering batch and **one** Android phone verification, not countless little pushes.

## Engine / document changes
`CreativeModel.kt`, `CreativeRaster.kt`, `InkMath.kt`, `CropMath.kt`, `ImageExportWriter.kt`, `CreativeCanvasOverlays.kt`, `CreativeDecks.kt`, edit/codec/render/UI integration and automated tests.

Text, ink and shapes remain editable data in `EditDocument.creative`, with bounds and colors encoded in the existing version-1 keyed local project format. Free crop is `CropFrame` normalized on the oriented/straightened photo; legacy crop center settings continue to load for previously created projects. Main render draws pixel adjustments, privacy operations, then creative overlays, preserving source image. Export sizes are composed **after** all edits on the rendered copy.

## Export behavior
File formats: JPEG, PNG, WebP lossy, WebP lossless, BMP 24-bit RGB, TIFF uncompressed RGB 8-bit, PDF one page. Browser/Gallery preview support for BMP/TIFF is not universal. JPEG/BMP/TIFF/PDF flatten transparency to white. Quality only for lossy formats or WebP lossless compression effort. WebP lossless on Android 10 uses the documented legacy 100-quality format.

Social presets: original, Instagram square/portrait/story/reel cover, Facebook square/landscape/story, TikTok photo 9:16, YouTube HD/4K thumbnail, Pinterest pin, LinkedIn square, X landscape, Snapchat story. Fit pads to exact canvas size and preserves all content, Fill crops edges and fills full canvas. No automatic posting, uploader, network or API.

## Verification levels
- Separate pure Kotlin and Android emulator tests, including crop geometry and corner ratio, codec legacy/snapshot roundtrip, text fonts and raster pixels, shapes and ink, export format signatures, social exact target size, alpha behavior.
- PR CI, merge verified SHA, merged-main CI and exact APK artifact.
- One field session on Samsung with short steps for text editing, brush and eraser, shape placement, drag crop, export dropdowns and existing privacy control regression.
- Commercial release still blocked by real-device UX/memory/color, legal/privacy/trademark, Play compliance and production signed AAB.

**No external website or project changes.**
