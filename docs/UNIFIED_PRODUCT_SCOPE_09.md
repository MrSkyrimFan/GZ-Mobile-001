# GZ Editor Pro | Owner-directed non-AI product scope

**Approved direction: November 21, 2026.** Android first; $2.99 **one-time** final retail target. No AI or inference at runtime, accounts, cloud, ads, subscriptions, watermarks, credits, hidden unlocks, or telemetry. Original source media never overwritten. iPhone separately in the future. The design goal is a $100-class perceived value from excellent working features and usable UI, **not a claim of an appraised $100 market value**.

## UI: hard rules
- Top-right dropdown modes: **Photo editing** (regular), **Photo editing Pro**, **Collage maker**, **GIF maker**, **Video editing (Coming soon)**.
- Each mode gets its own workspace and shallow, logically grouped controls. Never mash every option into one screen. Keep main media visible and active touch handles working even when chrome hides.
- Regular: intuitive Looks, Light, Color, Detail, Effects, Privacy, Crop, More. Pro: full editing of selected image layers, layer operations, spatial edits and blend/composition. Collage: original seven patterns and 20 independent tiles. GIF: frames/time/size. **Video editing is the LAST major capability implemented.**
- Preserve prior project data, editing behaviors, no source writes. Deliver one large coherent tested batch, followed by one combined handset verification, not endless tiny builds.

## Engineering state
**Already in merged 0.8:** 56 adjustments, 50 Looks, HSL, color curves, 3 mask types, blur/pixel/opaque privacy, text/fonts/drawing/shapes, manual crop, undo, photo projects, social sizes and true JPEG/PNG/WebP/BMP/TIFF/PDF; 1–20-photo independent 7-layout collage.

**0.9 candidate, do NOT claim release until CI + device passes:** Separate workspace dropdown; simplified Regular UI; Photo Pro independent image layers, order, visibility, opacity, position, size and normal/multiply/screen/overlay compositing, independent full photo editor per layer, editable saved projects and output; GIF maker 2–20 photo frames, reorder, preview, timing, looping, Fit/Fill, dimensions, project saves and locally coded true GIF89a/LZW export. New third-party libraries: none.

**Future professional photo work, ALL explicitly in scope:**
- Proper editable unified image/text/shape/paint/adjustment layers, layer masks, groups, blending, selection tools (rectangle, ellipse, freehand lasso, polygon, colors), editable guides.
- Non-AI clone/heal/spot repair, background selection/removal, gradients/fills, distortion/warp/perspective, editable feathered radial/linear/color/luminance masks, camera/lens corrections.
- RAW/DNG import with properly licensed decoder and camera profiles, demosaic, real white balance, highlight detail handling bounded by source, ICC/color management, internal 16-bit/float color, wide gamut and truthful high-bit TIFF/PNG exports.
- Catalog: local folders, collections, ratings, favorites, tags, duplicate detection, EXIF, smart user-defined filters, sidecar edit copies.
- Batch processing and export: apply grades/Looks/presets, copy/paste settings, rename, multiple formats, queued cancellation, job progress and safe source custody.
- True histograms, clipping, before/after, numerical color/denoise/sharpen and image benchmark suites, performance profiling, device-aware memory caps.
- Advanced typography, vectors, touch handles, layer grouping and editing history, genuine non-AI retouching, photo compositions.

**Future GIF/photo motion:**
- GIF from videos, frame ranges/speed/reverse, per-frame duration, animated captions/overlays, proper crop/resize, optimized palettes/dithering.
- Photos plus audio produce **MP4 video**, not JPEG/PNG or GIF. Local user-owned/imported audio, slideshows, transitions, per-photo duration, deterministic pan/zoom and optional beat-sync. No preloaded copyrighted music.
- Audio on videos: local MP3/WAV/AAC where supported, multiple clips/tracks, trim/split, volume/mute, fades, waveforms, voiceovers, equalization and non-AI noise reduction.

**VIDEO LAST:** local import/player; frame/time timeline, multiple clips, trim/split/join/reorder, speed and crop, effects, fades, transitions, captions/text/shapes, picture-in-picture, overlays/selection/redaction flattened in output, keyframes, audio sync, real MP4/codec exports, preview/final parity, re-encode privacy safety, device performance/thermal tests and supported 1080p/4K tiers. Review Media3/dependencies separately before integration; do not ship placeholders.

## Engineering/legal release gates
- Copyright protects code/art; no Adobe branding, proprietary source, copied artwork/UI/presets or protected streaming ripping. Independently authored tools may still raise patents and commercial licenses.
- Maintain SBOM, transitive licenses, asset/font/music/codec rights, trademark clearance, vulnerability and patent-specific checks before distribution. User media rights remain user's responsibility.
- No guarantee of lawsuit immunity. Commercial release requires real-device QA, pixel/codec and preview/export checks, privacy/metadata policy, Google Play compliance and properly signed AAB.
- **Only authorized repo:** `MrSkyrimFan/GZ-Mobile-001`; no corporate website/DNS, XForge, GZFX, Beside-Me or unrelated projects.
