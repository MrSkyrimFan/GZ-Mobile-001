# GZ Editor Pro — historical capability reference and IP safeguards

**Company:** Greenezone Software LLC | **Product:** GZ Editor Pro (Android) | **Policy:** Offline, no runtime AI, no watermarks, one-time payment.

This is an engineering and compliance roadmap, **not** a claim that trademark/patent clearance has been completed or that no one could sue. Features can generally be built independently, but source code, presets, artworks, branding, patents and libraries require different checks.

**Execution roadmap superseded on 2026-11-24.** Use the [complete roadmap](GZ_ENGINEERING_ROADMAP.md), [capability inventory](CAPABILITY_MATRIX.md) and [single work ledger](WORK_LEDGER.md) for current scope/order/status. This file preserves earlier capability rationale and license research; it is not a second backlog. Current product requirements, owner decisions and release gates control.

## Regular photography features

### Photo library and batch workflow
- Local image catalog with folders/collections, ratings, favorites, keyword tags, EXIF metadata inspection, import duplicates detection, sidecar edit copies and original-file custody.
- Batch apply Looks/grades/crop presets, batch rename and export, multiple format/dimension recipes, queued cancellable jobs and progress receipts.
- Smart collections may use explicit user-defined rules (date, camera, lens, rating, color label), **not AI classification**.
- Entire feature must remain local; no account or cloud upload. Do not request unrestricted library permission when the scoped media picker suffices.

### RAW and professional color pipeline
- Evaluate an offline DNG/RAW decoder with verified commercial use rights, transitive dependencies and attribution. **Do not bundle LibRaw yet.** Its documented LGPL-2.1/CDDL-1.0 licensing carries obligations requiring review before integration.
- Validate camera profile support, demosaicing, linear exposure, color temperature, white balance, highlights, blacks, lens corrections and proper EXIF orientation.
- Upgrade beyond current 8-bit sRGB pipeline: internal 16-bit/float processing, ICC profiles, wide gamut, profile-to-sRGB export, lossless TIFF/PNG high-bit-depth options where supported, numerical reference images and color-chart test suite.
- Avoid claims of lossless RAW editing until the actual pipeline, export bit depth, decode correctness and tests support them.

### Optical correction and protected Regular tools
- Preserve Regular photographic grading and Blur & cover. Restrict selection and new adjustment-mask authoring to Pro, preserving historical Regular masks in saved projects.
- Original CPU/GPU denoise (non-ML), thresholded sharpening, chromatic aberration, distortion, perspective grids, manual straightening, highlight recovery within available source data, selective saturation and tone curves.
- Keep Regular privacy blackout and blur available. Give these tools clear names and verify protected pixels in exports.
- Lens profiles can only ship where created independently or sourced under suitable licenses.

### Composition and creativity
- Regular owns Text authoring, Shapes, drawing and the offline sticker library. Sticker work needs at least 500 usable assets with documented rights, transparent custom imports, transforms and editable project storage. Collage supports those Regular edits independently on each of 20 tiles.
- Every source retains its own `EditDocument` and can be edited in the existing full photo editor. Flatten only at export.
- Future local stickers/texture packs require rights to each asset/font (no copying paid packs).
- Better touch handles and editing overlays must not be coupled to toolbar visibility.

## Pro retouching and compositing

- Pro owns layer-based compositing, editable masks, selections, clone, healing, source stamp, and local retouching for blemishes, skin, eyes, and teeth. Preserve legacy Pro Text data and render it in saved projects.
- Pro's Subject & Background tools include guided subject isolation, edge refinement, background removal or replacement, object removal, and reshaping. Use offline image processing and preserve editable state, Undo/Redo, saved-project reopen, and full-resolution export.

## Performance and reliability
- Decode by tile, GPU shader acceleration where it materially improves responsiveness, separate fast preview vs final export, bounded memory, cancellation, safe failure and atomic local project custody.
- Real 12MP/24MP/oversize photo and 2–20-source collage benchmarks; crash/ANR checks on low-memory Android devices and photo provider expiry.

## Separate Video editor after owner workspace locks

Begin Video only after the owner formally locks Regular, Collage, Pro and GIF. Finish the full app and owner qualification before English/Spanish onboarding or legal acceptance flows.

**Do not claim video functions exist yet.** Android's official Media3 Transformer supports clip trimming, track removal, video rotation and effects, transcoding and image-to-video. The `androidx/media` project has Apache License 2.0 notices; verify exact versions and dependency manifests before use.

Stages:
1. Local video picker, playback, trim/split/join clips, mute whole track and specified time ranges, volume, crop/rotate, frame/time cursor, export progress, cancel and robust file ownership.
2. Shared text/draw/mask/blur/opaque-cover semantics with time-indexed or keyframed positions and frame-accurate previews. A cover that conceals a person must be flattened in every exported video frame, not merely drawn as a removable preview overlay.
3. Transitions, speed, multi-track audio, fades, subtitles, image montage, still-photo collages to clips, 1080p/4K presets subject to Android decoder/encoder constraints, and professional project save/reopen.
4. Regression decode of finished video, frame sampling, audio track inspection, bitrate/codec/metadata tests, timeline seek accuracy, and device thermal/performance checks.

**Important privacy rule:** Android documentation explicitly cautions that fast MP4 edit-list trimming may retain sensitive pre-roll content. For privacy redaction or destructive removal of sensitive video/audio, avoid an edit-list-only export and prefer safe re-encoding plus output verification.

Sources:
- U.S. Copyright Act, 17 U.S.C. §102(b): https://uscode.house.gov/uscode/cite.html?req=granuleid:USC-prelim-title17-section102
- Adobe third-party trademark policy: https://www.adobe.com/legal/permissions/trademarks.html
- LibRaw licensing: https://www.libraw.org/docs
- Media3 Transformer: https://developer.android.com/media/media3/transformer/transformations
- AndroidX Media Apache 2.0: https://github.com/androidx/media/blob/release/LICENSE

## IP/legal release gate (mandatory before launch)

- Original code, datasets, UI and graphics; no reverse engineering of proprietary binaries/secret APIs, copied presets or brand imitation.
- Maintain a complete dependencies/assets/licenses/SBOM ledger, check transitives, notices, copyright and patent license obligations, build reproducibility and vulnerability advisories.
- Distinguish copyright (expression), trademark (source identifiers), patent (claimed methods), privacy rights, users' uploaded content and app-store requirements.
- Search USPTO exact and confusingly similar marks, state marks and common-law usage for **GZ Editor Pro** before commercial launch. Name is an owner decision, not a verified registered mark.
- Investigate material patent freedom-to-operate questions for specialized processing. A copyright-safe original implementation could still raise a patent issue.
- Respect platform and app-store policies. Users remain responsible for rights to photos/video they edit; don't bundle copyrighted music, stock photos or fonts without rights.
- Review published privacy policy, terms, accessibility, age/content rules, SDK target, billing, licensing and export metadata leakage before distribution.
- No claim of *zero lawsuit risk* or legal immunity. Counsel can be the final audit after the engineering, documentation and research work is done in-house.

**Scope boundary:** Never change Greenezone's corporate website, DNS, accounts, XForge, GZFX or Beside-Me as part of this app wave.

## Historical owner scope and UX record for v0.9
- Updated final product price: **$2.99 one-time**, not $1.99.
- Hard UI requirement: top-right separated workflows for Regular Photo, Pro Photo, Collage, GIF and disabled Video Coming soon. Keep regular easy, expert tools in named Pro sections.
- Complete feature-by-feature non-AI photography, Photoshop-style editing, frame animations, user-audio, slide shows, export and video-last development scope in `UNIFIED_PRODUCT_SCOPE_09.md`. Do not silently skip any requested capability, but do not call it implemented unless tested.
- The Wave 8 document records the original v0.9 phone test plan. Use the release gates for the current owner test. [Historical Wave 8 plan](PHONE_TEST_WAVE8.md) · [Current release gates](RELEASE_GATE.md).
