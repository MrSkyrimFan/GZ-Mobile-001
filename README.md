# GZ Photo Editor Pro

**Owner:** Greenezone Software LLC · **Platform:** Android · **Commercial goal:** $1.99 single purchase

**Status:** 0.4.0 engineering qualification candidate. No production release or Play Store listing exists. The owner approved the product name; trademark clearance remains a release requirement.

## What the application actually does

- Entirely **offline**. No AI, ad SDKs, trackers, accounts, subscriptions, microtransactions, internet permission or back-end dependency.
- Source image selected with the Android system photo picker. Edits are parametric and never overwrite the source. Exports use the system's new-file CreateDocument picker.
- **56 global editing controls**, including light, tonal balance, color, 8-band HSL (24), 3-zone RGB grading (9), structure/clarity, sharpening, sharpening threshold, edge-aware noise reduction, vignette and grain.
- Four-channel tone curves (master RGB, red, green, blue) with five adjustable y-points and a per-channel curve preview.
- **Four local masks** maximum, now including radial, graduated linear and **real finger-painted brush** masks. Masks apply exposure/contrast/saturation, have feather and radius controls, and participate in undo.
- Brush strokes are captured as bounded normalized paths. A 32×32 segment-indexed spatial grid keeps most per-pixel work local. Up to 640 stroke points per mask. Brush masks are vector-edit data, not separate raster layers.
- 12 included looks, 20 local user-created grade/curve looks, 12 saved editable photo projects. Projects retain image URI and edit settings, including brush paths. Source photo must remain accessible and some providers may not persist picker permissions.
- Undo/redo (40 checkpoints), compare, image zoom and pan, reset, sampled histogram, rotate left/right, horizontal/vertical flip, 9 aspect crop options and repositioning, ±15° straighten with safe inset.
- Conservative **Auto tone** and **Auto WB** controls for rapid starting adjustments, always overridable by manual edits.
- JPEG output at quality 85/90/95/100 and PNG. Transparent pixels are composited over white when saving JPEG; PNG retains alpha. System creates a new destination file.
- Software sRGB (8-bit) render pipeline, cancellable sliding-row CPU processing, separate sharpening/texture/denoise math, sampled gamma LUT, precomputed color hue and local mask transforms.
- Preview target up to 1400px/2MP. Export limit **adapts to device memory**, maximum 24MP. This is for crash resistance; huge photographs may be downsampled.

## Engineering stack and validation

Kotlin + Jetpack Compose · Android 10+ (min API29) · target/compile SDK36 · Gradle9.6 · AGP9.4 · JDK17. Package `com.greenezone.gzphotoeditor`.

```sh
gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug
gradle --no-daemon :app:connectedDebugAndroidTest
```

GitHub Actions runs independent JVM mathematical tests, a debug APK assembly, and Android 15/API35 emulator-based bitmap/Activity tests. A green emulator does not prove photographic quality on real hardware. The artifact `gz-photo-editor-pro-debug` contains `app-debug.apk` (debug signed). **Not** a production AAB.

See `docs/PRODUCT_SPEC.md`, `docs/RELEASE_GATE.md`, `docs/PHONE_TEST_WAVE3.md`, `docs/CONTINUITY.md`.

## Limitations honestly retained

- No advanced healing/clone/content-aware/object removal yet.
- No RAW/DNG development, ICC profile-aware wide gamut, 16-bit TIFF, HDR pipeline, true layers or arbitrary polygon selections.
- Brush is a local *adjustment mask*, not a retouch/healing brush.
- No full image batch processing or cloud sync.
- Color-correction quality, actual phone responsiveness, memory stress and store compliance need further verification. Do not describe the current APK as a $49.99-level proven replacement for established professional products.

All company website work and the XForge, GZFX and Beside-Me projects remain outside this repository and must not be touched for photo-editor development.

Copyright © 2026 Greenezone Software LLC. All rights reserved.
