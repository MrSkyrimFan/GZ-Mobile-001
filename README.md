# ChromaVale Photo Studio (working title)

Offline, AI-free photo editing for Android by **Greenezone Software LLC**.

> **Status: Native Android engineering in progress. Not a finished/published app.**
> The name is provisional. Do not publish until branding/trademark clearance, build, QA and Play policy checks are complete.

## Product contract

- $1.99 one-time Google Play purchase, no ads, AI features, subscriptions, IAP, account, analytics, tracking or network dependency.
- Source images stay on the device. Select a photo using Android's system photo picker.
- Non-destructive adjustments, export to a **new** JPEG (95 quality) or PNG file through the system document picker. Never overwrite the input.
- Fifteen+ tonal, color, detail and film controls; a set of looks; compare, reset, undo/redo, rotation/flipping, aspect-ratio crops.
- Shared image mathematics for preview and export, with a bounded preview-size render and original-resolution export subject to a safety pixel cap.
- XForge and GZFX source, budgets and accounts are outside this repository. This repository replaces the earlier empty Godot prototype.

## Project

Native **Kotlin + Jetpack Compose**; Gradle 9.6 / Android Gradle Plugin 9.4, compile SDK 37 and target SDK 36. Android Studio is the recommended local IDE. The former `project.godot` file has been removed.

## Build

Run `gradle :app:testDebugUnitTest :app:assembleDebug` with Gradle 9.6, Android SDK 37, JDK 17, or use the GitHub Actions Android workflow.
No keystore or secrets are committed. GitHub Actions builds **unsigned debug/testing APK only**, not a paid Play Store release.
See `docs/PRODUCT_SPEC.md` and `docs/RELEASE_GATE.md`.

## Privacy

No requested camera, external-storage-wide, INTERNET, media-library-wide, or location permissions. System pickers grant the selected input/output file access. The app processes images entirely on device.

Copyright (c) 2026 Greenezone Software LLC. All rights reserved. No open-source license is granted.
