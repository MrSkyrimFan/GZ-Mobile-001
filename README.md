# GZ Photo Editor Pro Photo Studio

Native, offline Android photo editing by **Greenezone Software LLC**.

> **Status: engineering test candidate, NOT published or production-qualified.**
> GZ Photo Editor Pro is the development brand following public-web screening. Full USPTO/state/common-law clearance and owner approval remain required before publication.

## Product contract

- Planned **$1.99 single purchase** on Google Play. No ads, AI, in-app purchases, subscriptions, accounts, trackers, telemetry, or remote API.
- Choose a source via Android's system photo picker. Use Android's create-document flow for a **new** JPEG or PNG export. Never overwrite a source.
- Non-destructive snapshots: grade, rotation, horizontal/vertical mirror and center-crop ratio.
- **53 real parametric controls**: existing 20 light/color/detail/film controls, nine three-zone RGB color-grade controls, 24 eight-band HSL mixer controls.
- **12 built-in looks** and up to 20 app-private custom looks, with undo/redo and reset.
- Pinch/drag preview, optional luminance histogram, compare view, one-control reset and up to 40 undo snapshots.
- JPEG quality 85/90/95/100, PNG, scaled preview and a 24 MP export decode cap. Both paths use the same rendering logic.
- Row-buffered CPU rendering avoids two full-frame `IntArray` allocations in previous 0.1.0 code.
- All edits run locally; network access is not requested.

## Technology

Kotlin + Jetpack Compose, JDK 17, Gradle 9.6, AGP 9.4, compile/target SDK 36, min SDK 29 (Android 10). Package/namespace: `com.greenezone.gzphotoeditor`.

Previous dinosaur prototype was deleted in PR #1. This is not a Godot project. There is no relationship to the XForge/GZFX/Beside-Me source trees or the future Greenezone corporate site.

## Build and field test

GitHub Actions `GZ Photo Editor Pro Android CI` runs JVM unit tests and assembles the unsigned/debug test APK, artifact `gz-photo-editor-pro-debug`. It **does not** produce a paid Google Play release AAB or sign production packages. Download the APK from the completed CI run's **Artifacts** section, install on Android and follow `docs/PHONE_TEST_WAVE2.md`. A local build requires JDK 17, Gradle 9.6 and Android SDK 36:

```shell
gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug
```

See `docs/PRODUCT_SPEC.md`, `docs/RELEASE_GATE.md` and `docs/CONTINUITY.md`.

## Privacy

No camera, INTERNET, broad media permissions or location permissions. Custom looks store only parameter numbers and user-chosen look names in app-private preferences, never your images. Android's system picker provides temporary scoped file access. Exports contain edited pixels; no separate EXIF/metadata-copy feature exists.

Copyright © 2026 Greenezone Software LLC. All rights reserved. No open-source license granted.
