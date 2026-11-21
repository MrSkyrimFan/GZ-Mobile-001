# Project continuity — November 21, 2026

## Source of truth
- Repository: `MrSkyrimFan/GZ-Mobile-001` (separate public Android project).
- Earlier chat: **GZFX 0.0.37 Validation**; it was the conversation where a dinosaur-game starter was replaced by a premium, offline Android photo editor.
- Previous baseline: PR #1, merged at `136e4458c5aec9dbb29c3276298f6f8ee14a6e06`; first Kotlin/Compose photo editor, 20 sliders, 7 looks, JPEG/PNG, compare, undo/redo, 24 MP safety cap, ChromaVale *placeholder* branding.
- ChromaVale was expressly rejected. Focavera, Vuenora and Edivora were screened; Focavera recommended. Current development brand is **Focavera Photo Studio**. A web screening is NOT USPTO registration clearance.
- Product terms are owned by Greenezone Software LLC. Target $1.99 one-time paid Android app, with no runtime AI, ads, subscriptions, IAP, tracking, account or cloud.
- Preferred development cadence: deliver large, integrated feature waves with CI, then one real phone field test with clear instructions. Avoid tiny one-change cycles, blind changes, and unverified claims.

## Repository and company boundary
XForge is the flagship, being handled separately. Do not touch its code, budgets or documents. Do not modify GZFX, Beside-Me, Codex handoffs or unrelated organizations. The Greenezone corporate website plan is one permanent expandable site with About, Contact, Privacy and future Products. It was **deferred** and is not hosted or deployed from this repository. No domain/DNS/site changes are part of the Android app work.

## Focavera Wave 2
- Rename Android label, root project, package/application ID, APK artifact and source tree.
- Preserve the existing editor foundation and add 33 new controls to reach 53.
- Expand built-in presets from 7 to 12; add app-private saved grade looks.
- Add a sampled histogram, pinch/drag preview and fit, per-control reset, bidirectional rotation, vertical flip and crop ratios.
- Replace frame-sized input/output pixel array copies with rolling scanlines; precompute per-grade constants.
- Add selectable JPEG export quality.
- Retain the no-permissions offline architecture and debug-only CI.

## Verification rules
Only trust observed CI/workflow results, actual GitHub commit IDs and real Android handset evidence. Do not claim production quality, trademark clearance, release signing, or Play Store readiness on a green JVM build alone. Do not make changes to the future Greenezone website to satisfy an unverified assumption.
