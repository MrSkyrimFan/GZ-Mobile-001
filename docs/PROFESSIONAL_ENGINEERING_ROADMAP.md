# GZ Editor Pro — Professional capability roadmap and IP safeguards

**Company:** Greenezone Software LLC | **Product:** GZ Editor Pro (Android) | **Policy:** Offline, no runtime AI, no watermarks, one-time payment.

This is an engineering and compliance roadmap, **not** a claim that trademark/patent clearance has been completed or that no one could sue. Features can generally be built independently, but source code, presets, artworks, branding, patents and libraries require different checks.

## Professional photography features we intend to build ourselves

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

### Manual local editing, retouching and optical correction
- Layered luma/color-range masks, feathered gradients and editable brushes.
- Original CPU/GPU denoise (non-ML), thresholded sharpening, chromatic aberration, distortion, perspective grids, manual straightening, highlight recovery within available source data, selective saturation and tone curves.
- Clone/heal/source-stamp tools and blemish/object removal by original pixel processing rather than generative AI.
- Skin retouch tools, local highlights, eye/teeth adjustments, privacy blackout and blur with clear distinction between cosmetic blur and genuine redaction.
- Lens profiles can only ship where created independently or sourced under suitable licenses.

### Compositing / creativity
- Independently editable text, shapes, drawing, brand-neutral user-chosen templates, collages of up to 20 photos, reusable layout recipes, background colors, border/radius/spacing settings.
- Every source retains its own `EditDocument` and can be edited in the existing full photo editor. Flatten only at export.
- Future local stickers/texture packs require rights to each asset/font (no copying paid packs).
- Better touch handles and editing overlays must not be coupled to toolbar visibility.

### Performance and reliability
- Decode by tile, GPU shader acceleration where it materially improves responsiveness, separate fast preview vs final export, bounded memory, cancellation, safe failure and atomic local project custody.
- Real 12MP/24MP/oversize photo and 2–20-source collage benchmarks; crash/ANR checks on low-memory Android devices and photo provider expiry.

## Separate upcoming video editor inside the same GZ Editor Pro app

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
