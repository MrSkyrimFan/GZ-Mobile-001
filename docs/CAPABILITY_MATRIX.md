# GZ Editor Pro — capability and evidence inventory

Snapshot 2026-11-24 against main `d2a8f4de41839e25ad54060be803746d7307bee8` (app-identical to PR #26). See [roadmap](GZ_ENGINEERING_ROADMAP.md) for complete behavior and dependencies and [work ledger](WORK_LEDGER.md) for active progress. This is a finite family inventory, not a claim that every reachable UI control has already been exhaustively qualified.

**Statuses:** DEFECT = confirmed review finding, correction required; PARTIAL = implemented foundation and/or bounded fixture evidence, full listed outcome still open; OPEN = required work/evidence not delivered, not a blanket assertion that no helper exists; LIMITATION = known compatibility limit; GATED = implementation/setup waits for named prerequisite; OWNER GATE/DEPENDENCY = explicit external decision. No row is owner-accepted merely because tests pass. R0 dispositions advance in the work ledger; this baseline table stays dated until the next capability revision.

| ID | Workspace | Capability | Baseline status | Package | Required exit / remaining work | Requirement/evidence source |
| --- | --- | --- | --- | --- | --- | --- |
| D01 | Regular / tile | Fresh cover then manual crop | DEFECT | R0 | Held/quick crop keeps anchor and pointer; Undo/reopen/export protect same pixels | PR #26 review §1 |
| D02 | Regular | Readable bounded named saves and export requests | DEFECT | R0 | Reject oversized before mutation/picker; healthy bytes and dirty draft survive close/replace | PR #26 review §2 |
| D03 | Regular / tile | Complete long Draw and cancel | DEFECT | R0 | Late bends/end within 500 samples; cancel Draw/erase restores history and output | PR #26 review §3 |
| D04 | Pro | Complete long lasso and cancel | DEFECT | R0 | Late contour selected correctly within 450 budget; operation/history/reopen | PR #26 review §4 |
| S01 | All | Original media, independent documents and target identity | PARTIAL | R1 | No source overwrite/wrong-owner edit through duplicate URIs/history/async results | Audit 2; master §7 |
| S02 | All | Reader/writer schema limits and transactional migrations | PARTIAL | R1 | Old/new/malformed/budget fixtures preserve last healthy generation | Master §7; Audit 2 |
| S03 | All | Durable draft autosave, snapshots and recovery | OPEN | R1 | True process loss between writes restores completed work and navigation | Master §§7/11; review recovery limits |
| S04 | All | Back, transient surfaces and gesture transactions | PARTIAL | R1 | One committed pop; cancel/IME/picker/half-canceled Back preserve draft; real handset evidence | W0; master §11 |
| S05 | All | Provider write ownership and recovery | PARTIAL | R1 | Real external picker/process death, aliases/partial writes/read-only grants and retry | Audit 2 AND01; review |
| S06 | Regular ↔ Pro | Explicit saved repeated revision exchange | PARTIAL | R1 | R→P→R→P retains native parents, appearance, alpha, profile and versions | Master §§7/11; existing Yes/No transfer tests |
| S07 | All | Portable native bundle, backup and relink | OPEN | R1 | Second-device import resolves permitted sources; missing media preserved/relinked | Master §§5/7 |
| S08 | All | Command ownership and complete actual-control inventory | PARTIAL | R1 | Each reachable command targets one scope; absent forbidden tools; no silent missing functions | Revision 4 §3; master §3 |
| P01 | Pro | Image/shape/paint/adjustment layer foundation and legacy text | PARTIAL | R2 | Retain 16 blends, 12-layer baseline and legacy recipes; qualify full workflows | ProModel/ProRaster/ProStore; Audit 2 |
| P02 | Pro | Layer tree, groups, clipping, locks and multiselect | OPEN | R2 | Isolated/pass-through groups, group opacity once, clipping base/history stable | Master §5 |
| P03 | Pro | Bounded tiled compositor and device capacity tiers | OPEN | R2 | CPU reference versus preview/export, halos/alpha/seams/resource teardown; measured scale | Master §§5/7 |
| P04 | Pro | Canvas and object geometry | PARTIAL | R2 | Native geometry retained; canvas resize distinct from image resample; limit disclosed | Audit 2 PRO03/04 |
| P05 | Pro | Compound selections, channels and refined masks | PARTIAL | R3 | Color/luminance/contiguous/global, polygon, boolean sets, saved selections, feather/grow/refine | Master §5; current rectangle/ellipse/lasso |
| P06 | Pro | Perspective, skew, nonuniform transform, guides and snapping | OPEN | R3 | Correct inverse mapping/numeric targets under crop/zoom/reopen/export | Master §5 |
| P07 | Pro | Vector shape/path authoring | PARTIAL | R3 | Bezier/path booleans, fills/gradients/joins/dashes; no new Text authoring | Master §5; current simple shapes |
| P08 | Pro | Professional brush/eraser dynamics | PARTIAL | R3 | Flow/hardness/pressure/spacing/stabilization/touch fallback deterministic replay | Master §5; verified held paint/erase |
| P09 | Pro | Non-destructive clone/heal/patch/inpaint | PARTIAL | R3 | Sampling policy, preserved donor, editable marks and independent output | Master §5; current 80-stamp clone/soft repair |
| P10 | Pro | Ordered effects/styles and blend conditions | PARTIAL | R3 | Layer/filter/group scopes, ordering, alpha, strokes/shadows/bevel/displacement | Master §5 |
| P11 | Pro | Declared layered interchange and reusable actions | OPEN | R3 | Native fidelity first; named OpenRaster/PSD subset and loss report; repeatable actions | Master §5 |
| P12 | Pro | Guided non-AI subject selection and editable extraction | OPEN | R4 | Manual/assisted trace keeps original plus editable matte | Subject & Background addendum; Revision 4 |
| P13 | Pro | Background removal/replacement and mask restore | OPEN | R4 | Photo/color/gradient background; linked subject transform; reopened alpha output | Subject & Background addendum |
| P14 | Pro | Edge refinement, feather and fringe cleanup | OPEN | R4 | Hard/soft/hair/fur/translucent fixtures with honest limits and correction | Subject & Background addendum |
| P15 | Pro | Conventional object removal | OPEN | R4 | Small-object inpaint/patch/clone preserves outside pixels and editable sources | Subject & Background addendum |
| P16 | Pro | Liquify/mesh warp, reshaping and material retexture | OPEN | R4 | Push/pull/expand/shrink/freeze and editable deformation, cancel/reopen/export | Master §5; Subject & Background |
| R01 | Regular | Photo import, orientation, view, before/after | PARTIAL | R5 | Supported formats, original hash, correct orientation/crop, physical usability | Existing PhotoEditor/ImagePipeline |
| R02 | Regular | Tone/color/HSL/curves/grade and Looks | PARTIAL | R5 | Every advertised control changes correct pixels, saved recipe and export; neutral reset | ControlCatalog/ToneMath/CurveMath/SavedLooks |
| R03 | Regular | Histogram/clipping and numerical inspection | PARTIAL | R5 | Displayed-source/recipe identity retained; new working/proof domains labeled | Audit 2 REG06; PhotoHistogram |
| R04 | Regular | Float/16-bit managed image pipeline | OPEN | R5 | Declared linear/encoded alpha/profile semantics; precision ramps and reference math | Master §4; current 8-bit sRGB |
| R05 | Regular | DNG intake and sensor development | OPEN | R5 | Declared mosaic/linear/compression/opcode set, black/white levels/CFA/demosaic/WB/calibration | Master §4 |
| R06 | Regular | Broader camera RAW and profiles | OPEN | R5 | Rights-cleared per-camera variant table; decoder licensing before integration | Master §4; no default LibRaw |
| R07 | Regular | ICC/wide gamut, soft proof and high-bit exports | OPEN | R5 | Assign versus convert; output profile and 16-bit samples independently verified | Master §4 |
| R08 | Regular | Optics, perspective and photographic detail | PARTIAL | R5 | Lens distortion/CA/vignette/upright plus qualified non-ML denoise/sharpen | Master §4; basic DetailMath exists |
| R09 | Regular | HDR bracket merge, panorama and focus stacking | OPEN | R5 | Deterministic alignment/blending, motion policy and contributing-source lineage | Master §4 later parity families |
| R10 | Regular | Local catalog, collections, metadata and rules | OPEN | R6 | Folders/collections/ratings/tags/EXIF/IPTC/XMP/search/duplicate verification | Master §4 |
| R11 | Regular | Virtual copies, sidecars, sync and catalog recovery | OPEN | R6 | Selective compatible edits, source-safe relink, migrated backup and independent copies | Master §4 |
| R12 | Regular | Durable batch processing and export | OPEN | R6 | Frozen items/multiple outputs/name/profile/privacy, cancel/resume/idempotent receipts | Master §4 |
| R13 | Regular / tile | Text and typography | PARTIAL | R6 | Unicode shaping/fonts/styles/editability/crop/reopen/export; asset rights | Revision 4; CreativeModel/CreativeRaster |
| R14 | Regular / tile | Draw, Shapes and decoration | PARTIAL | R6 | Complete bounded gestures; independent transforms/layers; borders/frames | Revision 4; CreativeDecks; current 32-text/160-stroke/64-shape limits |
| R15 | Regular / tile | Privacy cover/blur/pixelation | PARTIAL | R0/R6 | Same source detail protected through geometry/export; blur strength not called secure redaction | Audit 2; fresh-crop D01 |
| R16 | Regular / tile | Finished offline sticker library | OPEN | R6 | ≥500 usable rights-cleared assets; 648 concepts/27 categories alone earn no credit | Revision 3.1 sticker requirements |
| R17 | Regular / tile | Sticker browser, transform, custom PNG/WebP | OPEN | R6 | Search/favorite/recent; alpha/order/duplicate/delete; Undo/reopen/export per tile | Revision 3.1 sticker requirements |
| R18 | Regular | Image formats, dimensions and metadata policy | PARTIAL | R5/R6 | Seven actual encoders/social Fit/Fill; exact dimensions/alpha; privacy/high precision extensions | ImageExportWriter/PhotoGeometryPlan; Audit 2 AND02 |
| G01 | GIF | Neutral video/GIF import and correct displayed aspect | PARTIAL | R7 | Supported VFR/rotation/non-square pixels/HDR conversion and first-use neutrality | Audit 2 GIF05; VideoGifSource |
| G02 | GIF | Edited playback, scrub, reverse, trim, speed | PARTIAL | R7 | Input to correct still/first motion/cadence measured; history/reopen/phone corpus | PRs #18–26; existing synthetic gates |
| G03 | GIF | Palette, disposal, transparency, loops and holds | PARTIAL | R7 | Independent decoder corpus, temporal color and explicit limits | GifEncoder/GifPalette; fidelity tests |
| G04 | GIF | Named project identity/quota and recovery | PARTIAL | R7 | Preserve existing save proofs; missing-source relink/cold process/platform migration | Audit 2 GIF01/02; GifStore |
| G05 | GIF | Legacy v4 anamorphic project upgrade | LIMITATION | R7 | Source reinspection and reversible versioned copy; preserve manual framing and old project | Independent PR #26 review §5 |
| G06 | GIF | Photo-sequence creation and frame/segment assembly | PARTIAL | R7 | New creation separately qualified; per-frame holds/split/remove/duplicate/reorder | GifModel; master §6 |
| G07 | GIF | Timed captions/overlays, ping-pong and encoded preview | OPEN | R7 | Keyed appearance/timing, endpoint schedule and palette/dither comparison | Master §6; current static caption exists |
| G08 | GIF | Export responsiveness and full saved animation fidelity | PARTIAL | R7 | Retain current 15 s/6 s CI gates; larger sizes/longer media/device heat/cadence separately | PR #26 raw receipts; review qualifications |
| C01 | Collage | 1–20 independent tiles and duplicate-source identity | PARTIAL | R7 | All 20 independently edited outputs survive reorder/history; no contamination | CollageModel/Raster; Audit 2 COL01 |
| C02 | Collage | Seven layouts and full layout controls | PARTIAL | R7 | Aspect/gap/margin/border/rounding/background/Fit/Fill and orientation/alpha outputs | CollageWorkspaceScreen |
| C03 | Collage | Selected-tile transactions and Regular features | PARTIAL | R0/R6/R7 | Done/Apply once, Cancel/Back scoped; new stickers/text/draw/privacy remain tile-owned | W0; Revision 4 |
| C04 | Collage | Project/relink/storage/export resilience | PARTIAL | R7 | Missing-source recipe retained, stale preview cleared, stage preserved; physical tiers | Audit 2 COL03; CollageStore |
| Q01 | All pre-Video | Full handset/OEM/codec/memory/UX qualification | OPEN | R8 | Named device fixtures, sustained use, accessibility, no unresolved required workflow | Release gate; master §9 |
| Q02 | Regular/Pro/Collage/GIF | Four formal owner workspace locks | OWNER GATE | R8 | Explicit acceptance of each after evidence, not implied by CI or repair merge | Revision 4 |
| V01 | Video | Single-clip project/player/trim/export foundation | GATED | R9 | Real source tracks, seek/timing, safe MP4, reopen/recovery | Master §6; Video currently disabled |
| V02 | Video | Multitrack assembly and precision editorial tools | GATED | R9 | Split/join/ripple/roll/slip/slide/J/L cuts/markers/link/lock behavior verified in export | Master §11 |
| V03 | Video | User sound/music, recording and professional mixing | GATED | R9 | Waveforms/gain/mute/fades/EQ/dynamics/non-AI noise reduction/ducking/buses/stems; sync and rights | Master §11 |
| V04 | Video | Compositing, keyframes, transitions and motion | GATED | R9 | PiP/titles/mattes/keying/stabilization/speed/nested/multicam; photo+audio slideshow with per-photo duration, pan/zoom and optional beat-sync | Master §11 |
| V05 | Video | Color, scopes, Looks and qualified LUT/HDR | GATED | R9 | Explicit color pipeline/selected ranges/proxies/conform; independent tags/pixels | Master §11 |
| V06 | Video | Moving region effects and privacy redaction | GATED | R9 | Every affected frame/stream redacted; editable keyframes and reviewed tracking | Master §11 |
| V07 | Video | Media management, mastering and exchange | GATED | R9 | Bins/proxies/versions/ranges/audio-only/captions/interchange/archive/relink | Master §11 |
| V08 | Video | Qualified 1080p/4K/HEVC/HDR device tiers | GATED | R9 | Actual codecs/thermal/time/audio parity; no unsupported controls | Master §6 |
| Q03 | All five | Final complete editing and usability acceptance | OWNER GATE | R10 | All required capabilities checked through real user workflow/output/device | Master §9 |
| L01 | All five | English/Spanish localization and contextual help | GATED | R10 | Complete strings/layout/accessibility/editing regressions both languages | Revision 4 final phase |
| L02 | App | Welcome, legal notices and acceptance/access flow | GATED | R10 | Fresh/upgrade/offline/decline/reaccept consistent with paid-once product | Revision 4 final phase |
| L03 | External | Greenezone website complete | OWNER DEPENDENCY | R11 | Owner confirms website finished; app task changes no website | Lance, 2026-11-24 |
| L04 | Release | Play Console setup and verified identity/payment | GATED | R11 | After working app and website; actual account dictates verification/testing | Lance, 2026-11-24; account not created |
| L05 | Release | SBOM, assets/licenses/notices/security and legal review | OPEN | Continuous/R11 | Exact shipped binary rights/transitives/notices/profiles/codecs audited | Master §9 |
| L06 | Release | Production signing, AAB, split install and upgrades | OPEN | R11 | Secure keys, signed artifact, same-ID migration/release build/device compatibility | Release gate |
| L07 | Release | Store listing/policies/rating/privacy/pricing/testing | GATED | R11 | Current policy/account verified; accurate listing; required tests/pre-launch | Play official guidance; refresh at launch |
| L08 | Release | Owner publication approval and maintenance | OWNER GATE | R11 | Exact build/listing/price approval, support/rollback and data-safe updates | Master §9 |

## Source map

Current implementation references: [Regular controls](../app/src/main/java/com/greenezone/gzphotoeditor/editor/ControlCatalog.kt), [Regular host](../app/src/main/java/com/greenezone/gzphotoeditor/ui/PhotoEditorScreen.kt), [creative model](../app/src/main/java/com/greenezone/gzphotoeditor/editor/CreativeModel.kt), [Pro model](../app/src/main/java/com/greenezone/gzphotoeditor/editor/ProModel.kt), [Pro tools](../app/src/main/java/com/greenezone/gzphotoeditor/editor/ProTools.kt), [GIF model](../app/src/main/java/com/greenezone/gzphotoeditor/editor/GifModel.kt), [GIF project codec](../app/src/main/java/com/greenezone/gzphotoeditor/editor/GifStore.kt), [Collage model](../app/src/main/java/com/greenezone/gzphotoeditor/editor/CollageModel.kt), [build configuration](../app/build.gradle.kts), [existing evidence](AUDIT_2_REPAIR_VERIFICATION.md).

Master means the supplied revision 3.1 full blueprint; revision 4 owns later tool-placement overrides. Private originals remain in the owner's supplemental intake. Source presence supports implementation inventory only; execution claims require named tests/runs. Full project transfer, RAW/color, higher limits and later video are not inferred from existing basic editors. The current shared Panel.MASKS enum is compatibility infrastructure, not authority to expose a Regular Masks authoring menu.

Maintain child acceptance cases when executing a family; every child inherits its ID/owner. Do not inflate completion by counting helpers, commits, screenshots or proposed sticker concepts. Preserve all required child behaviors in the roadmap; a new scope decision must name which row changes and why.
