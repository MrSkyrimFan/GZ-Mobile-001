# GZ Photo Editor Pro · 0.5.0 · Photo-first interface

**Owner:** Greenezone Software LLC · **Target price:** $1.99 once · **Platform:** Android

**Status:** Android engineering build for consolidated phone validation. **Not a finished/published Play Store product.** Keep the approved name GZ Photo Editor Pro.

## Interface principles (implemented in 0.5.0 source)

- **Photo first:** the photograph fills the available full-screen canvas behind compact editing controls, rather than occupying a short preview box.
- **Tap the photo** to hide every editing panel; tap it again to bring controls back.
- **Filters / Looks:** swipe the image-preview tiles horizontally. Starts at **None**, followed by the original looks and saved custom looks. Tap a look to apply it to the photo.
- **One adjustment at a time:** choose a bottom category (Looks, Light, Color, Mix, Grading, Detail, Effects, Curves, Masks, Crop, More), swipe options left/right, then adjust only the selected parameter with a single slider and Reset action.
- **High-contrast names:** explicit light text and mint selected-state styling. No black-on-black text or huge nested vertical slider list.
- **Non-obscuring layout:** editing controls use a translucent dock over the photo; the top bar holds Open, Undo/Redo, Before, and Export. Preview remains the entire canvas.
- **Zoom and position:** pinch and drag photo, tap Fit if zoomed. Brush mode paints locally with one finger at Fit/1×.
- **Work preservation:** existing project saving, presets, masks, curves, crop/straighten and JPEG/PNG export are retained; package name stays \`com.greenezone.gzphotoeditor\`.
- The photo-first UI is integrated with the existing 56 global controls, 12 built-in looks, three mask types (brush, radial, linear, with up to four instances), four curve channels and existing edit/export pipeline.

## Product contract

- One-time purchase target $1.99; no ads, runtime AI, accounts, tracking, IAP, subscriptions, backend or INTERNET permission.
- Source photos never intentionally overwritten; new-file export is explicit through Android's system document picker.
- Kotlin/Jetpack Compose native Android 10+; debug-signed test APK from GitHub Actions.
- Production photography quality, true RAW/DNG, color-managed high bit depth, precision healing/clone, batch processing and performance stress qualification are **still open**, not advertised as ready.
- Actual Android touch/appearance verification is still required despite automated test success.

## Verification and installation

GitHub Actions workflow: \`GZ Photo Editor Pro Android CI\`, artifact: \`gz-photo-editor-pro-debug\`, APK inside ZIP: \`app-debug.apk\`.

The workflow tests source/Gradle assembly, JVM math and Android emulator bitmap/Activity and photo-first UI behavior. Once merged-main CI is green, use the instructions in [docs/PHONE_TEST_WAVE4.md](docs/PHONE_TEST_WAVE4.md) for **one comprehensive session**.

## Project boundaries

**Do not touch** Greenezone's official website, domain/DNS, XForge, GZFX, Beside-Me, other repositories, accounts or unrelated build budgets. The corporate website is a separate deferred project. Maintain an exact handoff in \`docs/CONTINUITY.md\`.

© 2026 Greenezone Software LLC. All rights reserved.
