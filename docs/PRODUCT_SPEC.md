# Focavera Photo Studio — product specification

**Owner:** Greenezone Software LLC
**Product class:** one-time-purchase native Android image editor
**Development branch:** feature/focavera-premium-wave-2
**Product state:** engineering test candidate; no public release.

## Permanent rules

- AI-free, offline, no ads, subscriptions, microtransactions, IAP, accounts or analytics.
- Target price: $1.99 on Google Play. No payment handling inside app.
- No INTERNET permission, cloud processing, broad photo-library permissions, or silent writes to the source URI.
- No changes in XForge, GZFX, Beside-Me or Greenezone corporate website repositories.
- All changes are non-destructive; preview and export share image operations. Preview is scaled and may show slightly different neighborhood detail/grain due to resolution.
- Everything is app-private except photos explicitly saved through Android SAF.
- The new name is **screened for exact public-web conflicts but NOT formally trademark-cleared**.

## Implemented in source (subject to CI and real-device QA)

### Light / global color / detail / film (20)
Exposure, contrast, highlights, shadows, whites, blacks, gamma; saturation, vibrance, temperature, tint, global hue and RGB balance; clarity, sharpening, fade, vignette, film grain.

### Tonal grade (9)
Independent red, green and blue adjustment for shadow, midtone and highlight regions, with overlapping smooth tonal weights.

### Eight-band color mixer (24)
Red, orange, yellow, green, aqua, blue, purple and magenta; each has hue, saturation and luminance. Overlapping hue weights avoid hard segmentation.

### Editing workflow
12 built-in looks; up to 20 saved local grade-only looks; undo/redo 40 checkpoints; compare; reset all or one slider; JPEG 85/90/95/100 and PNG; rotate both directions; horizontal/vertical flips; 9 center-crop ratios; pinch zoom/pan preview; optional sampled luminance histogram.

### Rendering
Software sRGB preview capped at 1400px/2MP, export decode capped at 12000px/24MP. Streaming scanlines replace two full-frame pixel arrays. No third-party image SDK or remote backend. Full-resolution export is rasterized once, then encoded to new file.

## Explicit limitations

- Center-crop ratios only. Freehand/drag crop handles, content-aware crop, mask painting and layer editing are **not implemented**.
- HSV band mixer is 8-bit sRGB, not an ICC/wide-gamut or RAW/linear 16-bit pipeline.
- 24MP is a safety cap. Images over cap are downscaled on export; this is not full native-resolution output for arbitrarily huge sources.
- JPEG alpha/matte and metadata output need device qualification; metadata is not automatically copied.
- Presets are local to a single device. No cloud sync or account.
- GPU acceleration, large-image tiled decoding, manual masking, RGB spline curves, RAW/DNG workflow, background batch export, histogram per-channel view and advanced optical effects are later engineering projects, not shipped features.
- Local JVM tests do not replace Android handset testing, especially decode/encode, pinch gestures and low-memory stability.

## Completion proof required

Clean branch CI + PR CI + merge + merged-main CI + independently confirmed APK artifact + actual phone field report before commercial signing and store validation.
