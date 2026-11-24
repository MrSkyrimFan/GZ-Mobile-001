# GZ Editor Pro — continuity / engineering custody

## Current owner direction (2026-11-24)

Lance supplied GPT-6 Pro's independent PR #26 review (CHANGES REQUIRED), authorized fixing the defects and requested the complete intended professional-app roadmap. **The successor read-only hold is lifted.** Engineering/PRs/verified merges are authorized. R0 has now passed candidate and merged-main qualification. The qualified v0.12.9 Test phone wave is ready for owner acceptance; formal workspace locks and release remain open. Formal workspace locks and publication remain owner decisions.

Start with the [single work ledger](WORK_LEDGER.md), [complete roadmap](GZ_ENGINEERING_ROADMAP.md), [77-family capability inventory](CAPABILITY_MATRIX.md), [product requirements](PRODUCT_SPEC.md) and [release gates](RELEASE_GATE.md). R0 covers fresh-cover manual Crop, Regular serialization limits, complete long Draw/cancel and complete Pro lasso. Its linked PR is the exact current execution receipt. The gate has passed; proceed to R1 without replaying intake. [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29), app merge `c9efc5aa0673f21b98f150733582a24996b34d10`, candidate `6ad22e3fabe3964a662c7e0e39bd08c4a8e8f9ca`, [candidate workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45352950946) and [merged-main workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45355074845). Both complete Android suites passed 186/186 on candidate and merge; source trees are identical. The exact APK identity is in the PR.

Play Console is not set up. Setup occurs last, after a fully functioning app AND completion of the Greenezone website. No website work is authorized here. Video waits for four formal pre-Video locks; final English/Spanish customer onboarding follows all five editing workspaces.

## Prior verified application baseline (historical)

PR #26 merge `372e0e947caacb2ceaf603bc4ff5a60e44cc33ec` passed JVM and 174/174 tests on each API29/API35 at candidate and merge. Version 0.12.8 Test is prior engineering evidence, not the newly recommended combined candidate. Intake main `d2a8f4de41839e25ad54060be803746d7307bee8` includes documentation refreshes only. [Prior repair evidence](AUDIT_2_REPAIR_VERIFICATION.md) · [Prior merged workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45339000608).

The original November 23 handoff required waiting for the review. Lance has now supplied it and authorized action; do not revive that historical hold. The private complete source transcript and intake archives remain supplemental evidence.

## Revision 4 and the engineering field guide (2026-11-22)

The [engineering field guide](gz-editor-pro-field-guide/00_START_HERE.md) records verified repair revisions, known-answer fixtures, and end-to-end checks for the current workspaces. Use the live repository and the [release gates](RELEASE_GATE.md) to confirm which results apply to the build under review.

[Product requirements](PRODUCT_SPEC.md) reflect the owner's latest instructions and Revision 4. Use these to resolve conflicts with earlier handoffs and version records. Check live GitHub refs before issuing builds or relying on historical branch IDs. Work directly in the authorized GitHub repository. Keep owner evidence and the active task package in the supplemental intake until the work it governs is complete. Keep original handoff archives intact.

The 0.12.3 handset test was the original Revision 4 baseline. The follow-up repair verifies the issues found in its post-merge audit, including limited GIF trim budgets, small Crop hit targets, and Pro reset and layer ownership across Undo. Field qualification still includes real post-edit GIF motion, finger-friendly Regular crop, workspace tool ownership, Pro File reset and Back, and the confirmation-based Regular-to-Pro handoff. Protect working Regular and independent Collage workflows. Pro tools may be reorganized where the existing interface obstructs the job. Follow [Release gates](RELEASE_GATE.md) for engineering verification and owner acceptance.

## Repos / identifiers
- Single authorized repo: `MrSkyrimFan/GZ-Mobile-001`.
- Baseline GZ Editor Pro 0.6 merge: `2a0f11b7eef2c5b42e5f569890b694a1dd7ba265`.
- Historical 0.7 engineering branch: `feature/gz-editor-pro-creation-export-07`.
- Application ID unchanged: `com.greenezone.gzphotoeditor`; launcher name GZ Editor Pro. Target Android 10+, compile/target SDK36.
- Retail target $2.99 one-time. Offline editing without runtime AI, ads, watermarks, in-app purchases, subscriptions, tracking or remote services.

## Architecture
- Save source image only by content URI, edit non-destructively.
- Grade + masks + privacy operations + creative overlays share preview/export pipeline; social export composition happens at the end.
- Text/ink/shapes and normalized crop frame persisted in backwards-compatible project codec; regenerate from source at final resolution.
- Export supports JPEG/PNG/WebP lossy/lossless/BMP/TIFF/PDF with matching MIME and real encoders.
- Social presets use specified pixel dimensions with explicit Fit/Fill. Not automatic social upload or guaranteed platform acceptance.
- Preserve the photo-first Regular interface, Looks, curves, color, Blur & cover, and Undo/Redo. Legacy Regular masks remain readable and renderable. New masking and selections belong in Pro.

## Hard rules
Trace owner-visible failures through production entry points. Test JVM and Android bitmap/Compose behavior, inspect rendered output, and link verification to the exact candidate commit. Engineering and verified merge authority follow the current owner instructions above. Handset qualification, formal workspace locks, and commercial release each require their own evidence or owner decision. Keep changes inside the GZ app repository and its supplemental intake.

## Collage wave v0.8.0 (2026-11-21 local)
Verified main baseline at the start of the historical v0.8 wave: `7508ff715d39dbb13ae543961b943cee5c78c22b` (GZ Editor Pro 0.7).
Branch: `feature/collage-workspace-photo-08`.
The owner added an explicit 2–20-photo collage requirement, independent photo editing inside tiles and high-quality layout templates. They supplied Lightroom's *functional* feature comparison as roadmap input and prohibit runtime AI, infringement and improperly licensed dependencies. No claims of zero legal risk.

Core source: `CollageModel.kt` for cell geometry/templates, `CollageRaster.kt` for incremental tile composition, `CollageStore.kt` for local project custody and `CollageWorkspaceScreen.kt` for a separate swipeable collage workflow. Route through existing `PhotoEditorScreen` so original photo-session state is retained and each tile opens the existing entire editor.
Fix prior screenshots' crop/blur/text/shape guides: editing overlays remain visible while UI toolbars hide, and the current tool remains interactive. Restore GZ Editor Pro top-left on empty launch.

The later Video phase is gated by formal owner acceptance of all four current workspaces. It can then evaluate Media3 Transformer and frame-indexed edit documents. No video feature is claimed shipped. Source research and legal/patent/license gates live in `docs/PROFESSIONAL_ENGINEERING_ROADMAP.md`.

**Do not alter corporate Greenezone website or other repos.** Test in CI before merging, then one consolidated real-phone check.

## Final integration polishing
The collage header uses two compact rows to prevent clipping, exposes an always-available Add action once photos exist, plus Undo/Redo. Snapshot history includes tile additions/removals/order/edits and layout operations; slider gestures produce one checkpoint. Source-URI export collision is blocked, nesting a collage while editing a tile is disabled, and coroutine cancellation is propagated. Tests added for history and maximum gap with 20 photos.

## Nov 21 v0.9 owner continuation
- Version 0.8 is merged on main `6080b98de997806ee3300d1ef68ebbb84422f072` with passed merged-main CI. The owner has **not tested 0.8 on their phone** yet.
- New non-negotiable interface: top-right dropdown with independent Regular, Pro, Collage, GIF and Video (Coming soon) workspaces. Do not cram all features into regular editor.
- Target $2.99 **one time**, no AI/ads/IAP/cloud. Prior features and files preserved. **Video last.**
- Branch `feature/workspaces-pro-layers-gif-09`: Pro image-layer foundation and local GIF89a creation, plus persistent offline projects. Not a complete Photoshop/Lightroom or video implementation. Details and missing work in `UNIFIED_PRODUCT_SCOPE_09.md`.
- The historical Wave 8 example is `PHONE_TEST_WAVE8.md`. Use the current repair PR and release gates for the next combined owner phone check. No corporate site, XForge, GZFX or Beside-Me changes.
