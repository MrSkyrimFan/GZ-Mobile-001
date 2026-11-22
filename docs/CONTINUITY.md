# GZ Editor Pro — canonical handoff, November 21, 2026

## Locked decisions
- Owner's current commercial name: **GZ Editor Pro**. Previous names ChromaVale, Focavera, GZ Photo Editor Pro are superseded for public branding.
- Publisher: Greenezone Software LLC. Price target $1.99 once. No AI at runtime, ads, watermarks, subscriptions, IAP, accounts, telemetry or INTERNET.
- On empty main screen: remove "GZ", "your photo deserves the whole screen" and "no cloud, no ads, no AI" messaging. Use title GZ Editor Pro, button **Open media**, and a small **© 2026 Greenezone Software LLC** at bottom left.
- Privacy editing (blur, pixelate, black bars/covering, adjustable brush masking) is essential for creators who need to share edited images publicly. Blur is not guaranteed private; provide opaque censor bars.
- Massive coherent engineering batches, substantial automated verification, and **one consolidated on-phone test**, not repeated tiny changes.

## Actual field evidence
The user uploaded FIVE Android photo editing screenshots after verifying 0.5: top shows GZ/Open/Undo/Redo/Before/Export; Reset is visibly cut off in Effects and Brush. Brush controls obscure the editing area; the green guide disappears when chrome hidden, but dark adjustment strokes remain. The owner wants brush editing to continue with controls hidden. User reports sluggish tool/slider reactions and wants far more Looks. This is first-hand user evidence; the screenshots alone cannot prove rendering correctness or speed.

## Engineering custody
- Repo `MrSkyrimFan/GZ-Mobile-001`; 0.5.0 baseline `81f17e227134a311e643d5fe60a6e7f5511756df`; new branch `feature/gz-editor-pro-privacy-ux-06`, PR #6.
- No Java package rename. Keep `com.greenezone.gzphotoeditor` to preserve project data on successful in-place APK updates.
- New `PrivacyEdit`, `PrivacyKind`, `PrivacyMath`, `PrivacyRaster` plus `PrivacyDeck` and `PrivacyRegionOverlay`. Extend codec while supporting existing v1 projects.
- `ImagePipeline.render` applies privacy effects **after grading** in both scaled preview and original-file export, no watermarks.
- Works with legacy Masks and new Privacy separately; painting remains active without chrome, but guide is hidden. Direct dragging positions privacy rectangles.
- 50 Looks, two-stage low-res responsive preview then full-frame, Reset bounded size, new official company copyright on empty screen.
- Verify PR, unit/Android emulator tests and main merge before sending link. Final commercial 1.0 not yet certified.

## Strict project separation
Greenezone official corporate website (deferred), website hosting, DNS, XForge, GZFX, Beside-Me, and all unrelated accounts/repositories are outside scope. The exact company website configuration remains unverified from this Android repo. Do not touch or invent it.
