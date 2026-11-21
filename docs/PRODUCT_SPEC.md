# ChromaVale Photo Studio — working product specification

**Owner:** Greenezone Software LLC.
**Scope:** Separate from XForge/GZFX/Beside Me. Native Android photo editor.

## Non-negotiables

- Offline, AI-free, advertisement-free, IAP-free and subscription-free.
- One-time commercial price target $1.99; no login, telemetry or server calls.
- Never silently replace source images. System picker for input and create-document for explicit output.
- All edits are parametric and non-destructive, applied from the unmodified original source at export.
- Preview and export must use the exact same edit settings and pixel math. Preview may be resized for responsiveness.
- No broad media permissions, external storage permissions, Internet permission, or background services.
- Full program ownership kept with Greenezone; third-party Android dependencies need license review at release.

## First executable feature wave

- Open photos with Android Photo Picker.
- Adjust exposure, contrast, highlights, shadows, whites, blacks, gamma, saturation, vibrance, temperature, tint, RGB channels, fade, clarity, sharpening, vignette, film grain.
- Curated original grading presets; each editable and undoable.
- Rotate by 90 degrees, horizontal flip, center-crop aspect presets.
- Before/after comparison, original restored with Reset, undo/redo up to 40 snapshots.
- Quality JPEG 95 and PNG export through Android's SAF CreateDocument.
- Scaled interactive preview and high-resolution export with explicit 24-megapixel safety cap.
- Dark premium UI with touch-friendly controls.
- Deterministic automated unit tests on color math, geometry math and snapshot history.

## Beyond the first build (NOT yet implemented)

Selective brush/mask edits, spline RGB/HSL curves, advanced local corrections, perspective,
metadata tools, batch operations, custom user looks, RAW/DNG workflow, true 16-bit processing,
wide-gamut pipeline, GPU-accelerated previews, large-image tiled decode and out-of-memory stress qualification.
No feature may be advertised as shipped before working Android verification.

## Commercial release gates

GitHub CI, Android 10–17 actual device testing, preview/export parity, memory-pressure and
long-session stability, UX accessibility, app name trademark clearance, a public privacy policy,
Google Play organization verification, Play target API compliance, store screenshots and signed AAB.
The debug APK is NOT the release build.
