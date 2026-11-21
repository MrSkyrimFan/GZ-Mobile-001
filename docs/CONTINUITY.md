# GZ Photo Editor Pro — exact continuity (2026-11-21)

## Business and name
**Owner-approved:** GZ Photo Editor Pro. ChromaVale and Focavera were rejected. Greenezone Software LLC owns product. Price target $1.99 one-time. Strictly no runtime AI, ads, accounts, microtransactions, subscription, analytics or internet.

## Code custody
Main baseline after PR #3 merged: `618c220a48c9ba42d8b06d659a52e5fce5e49761`.
Current integration work: `feature/pro-quality-wave-04`. Source proof must include both math unit tests and actual Android emulator bitmap/Activity tests. After PR approval, verify main workflow and explicit debug APK artifact. Do not call untested code production-ready.

### 0.4 engineering wave
56 global controls, sharpen threshold, texture and edge-aware noise reduction. New direct finger-painted local BRUSH alongside radial/linear masks. Versioned deterministic edit codec and named local project library. Safe crop/straightening/reframe, conservative Auto Tone/Auto WB, device-memory-aware export limit, input URI persistence attempts, transparent JPEG white compositing, temporary Bitmap cleanup and export failure cleanup. Run emulator tests and a consolidated **one-time phone field session**.

## Critical boundaries
Official Greenezone corporate website was deferred. Existing goals were About/Contact/Privacy/future Products; actual live DNS/hosting/registrar setup must NOT be guessed or modified from this Android repo. XForge, GZFX, Beside-Me, other repos and accounts are out of scope. Keep offline/no INTERNET security stance.

## Working practice
One substantial coherent engineering batch, source and unit tests, emulator tests, final merged-main verification, then one complete phone test. Avoid repetitive micro APKs. Report failures and factual uncertainty honestly, especially Android phone/picker memory behaviors. Save continuity files in this repo.
