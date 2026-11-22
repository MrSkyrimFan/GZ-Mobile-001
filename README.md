# GZ Editor Pro

**Greenezone Software LLC** · Native Android photo editor · Target price **$1.99 once**

**Engineering status:** v0.6.0 test candidate. This is not a final commercial or Google Play release.

## Product contract
Offline and private: no runtime AI, advertisements, watermarks, subscriptions, microtransactions, accounts, telemetry, remote services, or INTERNET permission. All source photos stay unchanged. The app imports images with Android's photo picker and exports new files through Android's system document picker. The **Open media** button currently supports photos; video editing is not implemented.

## What works in source (subject to CI and phone validation)

- Full-screen image-first editing, horizontally swipeable tool deck, tap picture to hide/show interface, pinch and pan, and visible Undo/Redo and Export.
- **GZ Editor Pro** label, no home-screen marketing slogans; just **© 2026 Greenezone Software LLC** at bottom-left of main/empty screen.
- **50 built-in Looks** with photo-based thumbnails, starting with **None**, plus saved local custom grades/curves.
- **56 global adjustments** for exposure, color, details, HSL, tonal RGB and film; master/R/G/B tone curves.
- Up to four local radial, linear and brush adjustment masks for exposure, contrast and saturation.
- New **Blur & cover** deck with up to 12 independent privacy edits: blur rectangle, pixelate rectangle, black opaque censor bar, opaque solid-color cover, blur brush and black brush.
- Privacy rectangles can be repositioned by dragging the photo; brushes paint by dragging **even when the editing UI is hidden**. Green stroke guides disappear with the controls, leaving the image visible.
- Pixelated and blurred regions have adjustable strength; rectangles have width/height and soft-edge controls. For reliable concealment, use a fully opaque black bar or covering, **not blur/pixelation**. Brush coverage is not a substitute for solid bar redaction at soft edges.
- Non-destructive privacy effects are applied to the **actual rendered image in preview and exported output**, after color grading. Existing undo/redo, named editable projects and JSON-like bounded local project codec persist these edits.
- Reset no longer lives in a 26dp-high row: controls use a 46dp-high row and a bounded 70dp Reset button, fixing previous clipped text.
- Two-stage preview: low-resolution fast render while moving controls, then full preview after the gesture. Brush painting no longer requests a full CPU render for every touch point.
- JPEG quality 85/90/95/100 and PNG export, adaptive maximum 24MP output decode, original-file safety guard, no branding added to exported pixels.

## Tech and build
Kotlin / Jetpack Compose, JDK17, AGP9.4, Gradle9.6, Android API29+ with target API36. **Important: application ID remains `com.greenezone.gzphotoeditor`** so existing saved projects can remain accessible when the APK updates with the same signing key. New app display name is **GZ Editor Pro**.

```sh
gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug
gradle --no-daemon :app:connectedDebugAndroidTest
```

GitHub Actions workflow: **GZ Editor Pro Android CI**. Artifact: **gz-editor-pro-debug** containing test APK `app-debug.apk`. Debug builds are not paid Play Store releases. See `docs/PHONE_TEST_WAVE5.md`, `docs/CONTINUITY.md`, `docs/PRODUCT_SPEC.md`, `docs/RELEASE_GATE.md`.

**Limits:** No real video support, RAW/DNG workflow, 16-bit ICC-managed editing, heal/clone/object removal, full batch export, secure automatic redaction audit, or verified app-store commercial quality yet. Blur/pixelate are cosmetic, not security guarantees. On-phone responsiveness and privacy export correctness require field testing.

**Company boundary:** The future Greenezone official website, XForge, GZFX and Beside-Me are unrelated. Nothing in this app repository should change their code, hosting or DNS.

Copyright © 2026 Greenezone Software LLC. All rights reserved.
