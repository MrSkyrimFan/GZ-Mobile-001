# GZ Photo Editor Pro — engineering contract 0.4

**Brand** owner-approved: GZ Photo Editor Pro
**Company:** Greenezone Software LLC
**Business model:** $1.99 one-time Google Play paid app
**Architecture:** offline on-device native Android; zero runtime AI or telemetry.

## Quality goals

This is not an arbitrary slider counter. Actual source and output integrity, non-destructive work, color accuracy, small phones, 12–24MP photograph throughput, device memory safety, robust save/restore and predictable edits are release criteria. Build success alone is insufficient.

## Functional feature wave

1. 56 independent global light/color/tonal/detail/film controls. Detail now includes independent texture, thresholded sharpening and edge-aware noise reduction.
2. RGB master + three independent RGB tone curves, adjustable fixed x-anchors.
3. Eight hue bands with independent hue/saturation/luminance, three-zone RGB color grading.
4. Four editable radial, linear and brush local masks with radius, feather, exposure, contrast, saturation. Brush vector data capped; index grid reduces scanning outside painted paths.
5. Up to twelve saved local project snapshots with original content-URI references and versioned bounded document data. Can reopen if image provider retains access. Up to twenty saved grade/curve presets.
6. Safe undo/redo, compare, histogram, zoom/pan, rotate/mirror, centered/repositionable aspect crop, ±15° straightening, auto tone/auto white balance.
7. Cancellable render with five scanline buffers, gamma LUT, cached color/mask trig, sRGB 8-bit decoding and export, adaptive per-device export-size limit max 24MP.
8. High-quality JPEG 85/90/95/100 and PNG new-file save, JPEG alpha flattened white, no writes to source.

## Engineering safeguards and honest known limits

- Project storage is app-private and references original images, rather than duplicating them. The original URI might become inaccessible after restart if a picker/provider cannot grant persistable access.
- Original image is **never intentionally overwritten**: output URI checked for source equality; CreateDocument opens another destination. A partial target file is removed after failed export where provider allows it.
- Adaptive memory cap protects low-heap devices by downscaling export. Fidelity for huge photography files is not automatically preserved.
- Geometry operations create intermediate Android Bitmaps. Source and temporary ownership is explicit; dedicated emulator tests cover pixels, JPEG matte, project saving and Activity startup.
- Preview and export use identical pixel math but are processed at different sizes, so texture/denoise and grain may differ at native zoom.
- Current curves remain 5-anchor master/individual-channel, not arbitrary spline node editing. Brush paints a feathered adjustment region, not healing/cloning.
- sRGB8 output does not preserve arbitrary ICC, high-bit-depth source, HDR metadata, RAW demosaicing or floating-point edits.
- High-resolution memory pressure, EXIF cases, provider behavior, full color accuracy and touch UX remain phone qualification gates.

## Next unshipped engineering

True layers, retouch/heal/clone, arbitrary crop/free rotation and perspective, high bit-depth color-managed pipeline, RAW/DNG engine, optical correction, batch export, GPU/tiled rendering, sophisticated interactive mask overlay / brush hardness, extensible project export/import and professional UI accessibility audit. None can be represented as already shipped.

## Project boundary

The official Greenezone corporate website is a separately deferred project; DO NOT write to its DNS, hosting, organization settings or other repos. This Android app is independent of XForge/GZFX/Beside-Me.
