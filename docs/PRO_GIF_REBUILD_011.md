> **Historical checkpoint — current route updated November 25, 2026.** Follow the [GPT-6.2 Sol Ultra successor handoff](NEW_CHAT_HANDOFF_2026-11-25.md), [current ledger](WORK_LEDGER.md) and [PR #37 / 0.12.11 Test evidence and focused phone script](B1_WORKSPACE_VERIFICATION.md). Current task: read-only successor onboarding, then wait for Lance's ongoing GPT-6 Pro review. Original text below retains its dated evidence; its “current”, “next”, delivery, engineering and test commands are historical, not fresh instructions. Do not replay completed intake, question exchanges, builds or phone scripts. The [current review brief](coordination/GPT_6_PRO_REVIEW_BRIEF_2026-11-25.md) supersedes older review prompts.

# GZ Editor Pro v0.11 — GIF and Pro corrective engineering

**Root cause based on owner phone test and source audit**
- The previous GIF preview was a timer that advanced frame indices and kicked off native thumbnail extraction in individual cancelled coroutines. It was NOT actual video playback; trims and speed could appear inert. A sideways-scrolling toolbar could hide Export.
- Pro included a button that opened the **Regular PhotoEditorScreen** for image editing and a second set of Regular Exposure/Light/Color adjustments. This directly contradicted the workspace contract. "Center" silently copied x/y to 0.5 without communicating whether any visible change happened.

**Implemented in feature branch, qualification required:**
- Native Android VideoView media surface for actual forward video playback, silent in GIF workspace; Play/Pause controls native player, and trim and scrub seek it; speed uses MediaPlayer playback parameters where supported. Reverse playback uses bounded decoded-frame preview, since VideoView cannot natively reverse. A static paused preview shows GIF crop/size, while native forward playback previews continuous motion.
- **Fixed-position Save GIF** action, non-hidden; GIF89a output through native local retriever, output filename and MIME, bounded frame budget, progress, source-write protections. Video-specific device codec behavior still requires real device field proof.
- Pro **strictly distinct** from Regular: image/text/shape/brush/effect layers, compositing, opacity/blending, move/center/rotate, selection masks using rectangle/ellipse/freehand, invert/removal, Clone stamp and Soft repair, on-layer paint/eraser, pixel effect layers (grayscale/invert/threshold/posterize) and editable local projects.
- Legacy Pro projects v1/v2 remain loadable using v3 encoder/decoder; no source overwrites or embedded user image bytes. Regular and Collage source and UI intentionally preserved.
- Kotlin math tests and actual Android bitmap/persistence tests for selection pixel masking, healing/clone color, effects, transparent brush erase, and native 2-second MP4 playback (not just extracted frames).

**Not shipped / still to engineer before Photoshop/GIMP parity:** precise object hit-testing, crop/mask resize handles and feather, pixel-based lasso edge refinement, true multi-brush dynamics, higher-end healing, clone sampling with pressure, groups/clipping, advanced layer masks, catalog/RAW/16-bit, color-management, platform-wide accessibility, signed commercial distribution. Video timeline and audio remain LAST, after photo/GIF quality.

**Commercial rules:** $2.99 one-time, offline, no runtime AI, accounts, ads, IAP, watermark, telemetry. Only `MrSkyrimFan/GZ-Mobile-001` authorized. This candidate requires passing exact-commit branch JVM+emulator, PR CI and merged-main CI, then one consolidated owner phone test.
