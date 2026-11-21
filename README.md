# GZ Photo Editor Pro

**Greenezone Software LLC** · Native Android · All editing on-device

**Engineering status:** Version 0.3.0 development candidate. NOT released or qualified as a professional commercial product. The owner approved the working product name **GZ Photo Editor Pro**. Formal trademark clearance is still a release gate.

## Value proposition

- Intended Google Play price: **$1.99 once**. No AI, ads, subscriptions, in-app purchases, logins, telemetry, account or internet.
- Source images remain unmodified. Android's photo picker imports one photo; Android's system CreateDocument saves a **new** JPEG or PNG.
- Non-destructive edits and 40-entry undo/redo history.
- **53 conventional parametric controls:** 20 tonal/color/detail/film controls, 9 RGB tonal color-grade channels, 24 selective HSL controls across eight color bands.
- **4-channel curves** (master RGB and individual red/green/blue) using five adjustable points per channel; 256-level sampled output.
- **Radial and graduated linear masks**, up to four per image, each with positioning, exposure, contrast and saturation.
- 12 built-in looks and up to 20 user-created local looks; custom looks persist adjustable global grades and curve shapes, but not mask geometry.
- Preview zoom/pan, before/after comparison, optional brightness histogram, rotate both ways, horizontal/vertical flip, nine center-crop ratios, individual slider reset and full reset.
- Export JPEG with quality 85/90/95/100 or PNG. CPU software pipeline shares pixel operations between preview and export.
- Row-buffered processing reduces temporary pixel-array allocations. Preview bounded to 1400px / 2MP; export decode bounded to 12,000px/24MP.

## Technology / build

Kotlin + Jetpack Compose, min SDK 29 (Android 10), compile/target SDK 36, Gradle 9.6, AGP 9.4, JDK 17.

Package/application ID: `com.greenezone.gzphotoeditor`. GitHub workflow: `GZ Photo Editor Pro Android CI`. Workflow artifact: `gz-photo-editor-pro-debug`. This is an debug-signed testing APK only. No signed release AAB or commercial listing exists.

```sh
gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug
```

## Honest feature boundary

No paintbrush masking, clone/heal, portrait retouch, perspective correction, interactive crop handles, DNG/RAW processing, 16-bit HDR/wide-gamut color management, ICC profile preservation, batch export, multilayer retouching, GPU renderer or full-resolution tiled pipeline **yet**. An Android CI pass proves source build and JVM tests, not color fidelity or phone stability.

The project is independent from **XForge, GZFX, Beside-Me and the deferred Greenezone corporate website**. Do not adjust corporate domain, DNS, business accounts or website infrastructure as part of this app repository.

For exact limits, source of truth and handset test process see `docs/PRODUCT_SPEC.md`, `docs/RELEASE_GATE.md`, `docs/CONTINUITY.md` and `docs/PHONE_TEST_WAVE2.md`.

Copyright © 2026 Greenezone Software LLC. All rights reserved. No open-source license granted.
