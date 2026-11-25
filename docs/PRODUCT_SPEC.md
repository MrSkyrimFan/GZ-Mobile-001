# GZ Editor Pro product requirements

> **Current work — November 25, 2026:** Lance relayed the completed [GPT-6 Pro review](coordination/GPT_6_PRO_INDEPENDENT_REVIEW_2026-11-25.md). The [work ledger](WORK_LEDGER.md) selects a bounded B1 follow-up for Pro layer actions, Collage header/taps, and nested departure evidence. PR #37 supplies the qualified 0.12.11 Test APK for focused diagnostic handset checks. R1.1 follows the correction wave. B2/F03 and the separate acceptance gates stay open.

## Latest owner requirements — shared UI and safe departure

The November 24, 2026 phone report says all unmentioned supplied tests worked. Named exceptions are Collage's quick-double-tap requirement, missing dirty-exit safeguards in Regular/Pro/Collage, and GIF Save taking 2–5 minutes. GIF controls now work. See the [owner phone report and Codex alignment handoff](CODEX_ALIGNMENT_HANDOFF_2026-11-24.md) for the full report, exact 25 questions and acceptance criteria.

The following were the November 24 requested targets. PR #37 subsequently supplied bounded B1 implementation and qualification recorded in [B1 evidence](B1_WORKSPACE_VERIFICATION.md); focused handset confirmation, B2 numerical exits and whole-family acceptance remain open:

- Use one consistent header/style/spacing/control arrangement across Regular, Pro, Collage and GIF, inherited by future Video only after its gate. Preserve the dark image-first editing surfaces and separate toolsets.
- Put a clearly titled **Options** dropdown at the top-right in every workspace. Major commands belong there: New/Open media, Open projects, Save project, Export and Help, with source-appropriate labels and relevant additional actions. Distinguish editable-project Save from finished-file Export.
- Remove duplicate visible locations for the same major action, including surviving File/More/header/dock equivalents. Keep direct editing, playback, Undo/Redo and selection-specific Done/Cancel controls. Inventory empty-state duplicates explicitly.
- Use the same paired Undo/Redo arrow icons in the same order/positions, with accessible names and consistent enabled/disabled behavior.
- Remove Regular's **More** section entirely. Remove its **Create collage** and **Histogram** UI entries rather than moving them to Options. Collage remains a separate workspace; preserve needed underlying image-processing/diagnostic code.
- Restore one-tap hide/show in parent Collage and selected-tile editing while keeping active guides and gestures usable.
- Protect dirty work on relevant workspace exit and replacement routes through the agreed Save/Discard/Cancel contract. Cancel or failed/canceled Save retains the exact work/route; Discard affects only the intended working changes. Preserve transient Back precedence, originals, named saves, independent tiles and Regular→Pro transfer custody.
- Measure and improve the actual phone GIF Save workload without silently reducing dimensions/FPS/frames/quality/effects or weakening existing fixture gates. Device/source/output details remain unknown until supplied or measured.

The owner explicitly authorizes these named UI changes after the required alignment. Older blanket layout-preservation wording does not block them. A1's 25/50 exchange and canonical decisions are complete in the [Stage 3 answers](coordination/CHATGPT_RESPONSE_TO_50_2026-11-24.md). The owner relay and full reads of those answers and the [resume instruction](coordination/ENGINEERING_RESUME_2026-11-24.md) were completed on November 25 before PR #37. The current successor/review gate above now governs the next action.

## Agreed interaction contract — Stage 3

These are engineering decisions implementing the latest owner requirements; they are not claims that 0.12.10 already implements them. The full rationale and edge cases are in [CQ01–CQ17](coordination/CHATGPT_RESPONSE_TO_50_2026-11-24.md#cq01), [CQ41](coordination/CHATGPT_RESPONSE_TO_50_2026-11-24.md#cq41) and [CQ42](coordination/CHATGPT_RESPONSE_TO_50_2026-11-24.md#cq42). They are the contract for B1 in the [resume instruction](coordination/ENGINEERING_RESUME_2026-11-24.md).

### Common header and one major-command home

Retain GZ Editor Pro at top left, distinct workspace selection, paired Undo then Redo arrow icons and visible text **Options** at top right. Use the same presentation component/tokens/adapters across Regular, Pro, Collage and both GIF routes. Existing controllers keep document/history/picker/job ownership. On narrow widths or large fonts, wrap the workspace/history group onto a deliberate common second row; no horizontal scrolling for file/help actions. Contextual Back and child Done/Cancel occupy consistent context space. Options stays available on empty workspaces, with noninteractive body guidance and the company footer; remove duplicate clickable Open/Add/Projects cards.

A completed free-canvas tap hides or restores **both header and tool chrome**. Do not keep a permanently pinned header over the image. Active guides/handles remain usable; an accessible reveal action is required. Empty workspaces keep the header visible. Direct tools, playback, Add layer, Undo/Redo and child Done/Cancel remain direct; Options does not become an editing dock.

Major-command order is source creation/opening, Open project, Save project, Save project as, Export, relevant explicit reset, Help. Omit unsupported commands; never add a nonfunctional future entry. Distinct editing operations are not duplicate file actions.

| Workspace/context | Single Options ownership and labels |
| --- | --- |
| Regular | **New photo…** opens/replaces photo media through the guard; **Open project…**, **Save project**, **Save project as…**, **Export image…**, **Reset all edits…**, **Help**. Do not also expose a second Open media button with the same action. |
| Pro | **New canvas…**, **Open photo as new document…** where supported by the guarded existing source path, **Open project…**, **Save project**, **Save project as…**, **Export image…**, **Delete current work…**, **Help**. Add image/drawing/other layers and Fit remain single contextual editing actions, not duplicated file commands. |
| Parent Collage | **New collage…**, **Add photos…**, **Open project…**, **Save project**, **Save project as…**, **Export image…**, **Help**. Add appends to this collage; New is a guarded fresh document. Remove header/dock copies of these commands. Layout, tile order and Edit selected remain contextual. |
| GIF video/existing-GIF route | **Open video…**, **Open GIF…**, **Open project…**, **Save project**, **Save project as…**, **Export GIF…**, **Help**, plus a supported guarded New action only if it is a distinct operation. Do not expose an unimplemented photo-creation route; completing that remains R7. |
| GIF photo-sequence route | Same header and project/save/export/help ownership. **Add photos…** appears once in Options for the existing sequence; Open video/Open GIF switches source route through the guard. Preserve frame edits/Play directly. Remove duplicate Projects buckets, header file/help/export and empty import buttons. |
| Selected Collage tile | Explicit **Open photo project…**, **Save photo project**, **Save photo project as…** and supported source-replacement/reset/help commands operate on the child photo draft/standalone photo save. Keep Collage Save/Export on the parent after Done/Cancel; do not add a new tile-image export route merely for symmetry. |

Existing working callbacks should be adapted, not discarded because a label changes. A genuinely distinct New versus Add operation remains understandable and guarded. Saved-item delete stays in the project chooser with target-specific confirmation; it is not a duplicate global major action. Regular Auto tone moves to Light, Auto WB to Color and Save my look remains once in Looks. Remove **More entirely**, including its navigation surface; remove all Regular **Create collage** and **Histogram** entry points, including empty states. Do not relocate or rename those two commands into another Regular menu. Preserve underlying compatible histogram math/diagnostics and legacy data.

### Save identity and content baselines

Use a stable project ID independent of display name, an explicit last-successful-save baseline and an import/creation entry baseline. Save updates the active identity; unnamed Save names once on success. Save as creates a new identity/active target. Collisions require a clearly identified Replace or another name; no silent overwrite. Preserve direct replacement safeguards, but a dirty-dialog Save for the displayed current target need not repeat an equivalent warning. Only successfully atomically published captured content becomes saved; newer edits remain dirty. Clean named Save may say Already saved; Save as remains available. Export writes a rendered file and does not mark native work clean.

Dirty compares semantic content/source ownership with the saved or entry baseline, not filename/history length/export/session retention. Undo back to equivalent baseline becomes clean. View/selection/chrome changes create no content history. Named Discard restores last saved content, unnamed Discard entry state; remove the discarded unsaved history/redo/recovery branch. This is distinct from Pro Delete current work, which retains its explicit reset-to-original/blank and history-clearing semantics. Neither operation deletes named projects or originals.

### Departure, cancellation and asynchronous ownership

| State/route | Required behavior |
| --- | --- |
| Clean or genuinely empty | One transition, without a false dirty prompt. |
| Ordinary dirty Back/app exit/workspace switch/New/Open/replace/Open project | **Save / Discard / Cancel**, even when an in-memory session is retained. Save waits for exact successful publication; Cancel keeps work/route/history; Discard scopes only the intended delta. No silent Keep draft substitution or fourth switch choice in B1. |
| Picker/loader/naming cancellation or failure | Current work and route stay intact. Stage any destructive disposition until the chosen target is usable; failure does not commit Discard. A named save explicitly completed remains saved. |
| Menu/IME/dialog/tool context | Resolve the frontmost context first. One Back cannot dismiss it and also leave the document. Canceled predictive Back commits nothing. |
| Continuous gesture | Successful release/Apply commits once. Pointer cancel, interrupted multi-touch or target/lifecycle invalidation restores the full pre-gesture document/history. Align text/shape/privacy drag-cancel with Draw/crop. Back cancels a draft, not Undo. |
| Changed zero-layer Pro setup | Current schema cannot Save it. Explain the unavailable Save; protect with **Discard setup / Cancel** and preserve setup on Cancel. No fake layer or false save. Named zero-layer persistence remains required R2 canvas work. |
| Export preparation/provider copy | Truthful owned job/progress and allowed cancellation. No navigation/result may orphan the stage or mutate another document; protected copy and uncertainty follow the export contract. |

One pending intent binds workspace/document/source revision, target route, child/tile identity where relevant and an execution token. Deduplicate repeated Save/Back/Done. Canceled naming/save invalidates its continuation; unrelated later saves cannot revive it. A stale successful save may update its recorded saved revision, never declare a newer edit clean or authorize its departure. Reject late results for a new document/owner and clean only their own resources.

### Collage child and single-tap rules

A completed unconsumed free tap selects the hit tile, if any, **and toggles chrome once**; a margin tap toggles without changing selection. Apply in either visibility state. Handle/paint/crop/drag/pinch-owned streams never also toggle; canceled or moved/multitouch streams are not free taps. Retain intentional established zoom behavior and active guides. The parent double-tap requirement is removed; adjust child recognition only for reproduced conflicts.

Child entry baseline and standalone-photo save baseline are separate. Saving a photo project does not Apply to parent or make the child clean relative to its entry; Cancel leaves that explicit named photo save intact but restores the child entry. Done applies one transaction to stable parent/tile identity, not index or source URI. Dirty child Back offers **Apply to tile / Discard tile changes / Cancel (keep editing)**. Apply/Discard returns only to the parent; the same Back must not also leave the parent. A child workspace-switch/parent-exit request stages that child disposition, then applies Save/Discard/Cancel to the resulting parent candidate. A later Cancel, failed Save or failed target load preserves the original child draft and parent transaction state; do not commit provisional discard or lose unapplied work. An explicitly completed named save remains recorded accurately. Direct Done is a completed parent edit, and direct Cancel restores only child entry. A source/project replacement inside the child protects its pending draft without writing siblings or silently applying it to the parent.

Relink preserves intended source identity and recipe after compatibility checks. Replace explicitly resets the selected photo recipe to neutral for the new source while retaining the layout slot; Undo restores the complete previous tile. Stale child results require parent/tile/source validation; compatible reorder can resolve by stable ID, conflicting edits/replace/delete cannot. Keep recoverable child work on a conflict.

### Regular-to-Pro sequence

Use one coordinator and serialized decisions. Capture Regular identity/revision and gather its dirty disposition without destructive mutation. If a Pro session exists, distinguish **Resume existing Pro** from **Start another Pro**, with Cancel. Resume is never the meaning of No. Starting another Pro preserves the established Yes/No prompt: Yes imports the **resolved** Regular appearance, No opens blank Pro. If the user explicitly chose Discard, label the saved/entry appearance and never secretly transfer the discarded snapshot.

Protect dirty existing Pro before replacement. Prepare the target first, then commit once and retain the resolved native parents. Cancel/failure leaves original working documents/routes intact, apart from an explicitly completed named save. Save, No or exporting alone never discards Regular; explicit Discard may remove only its identified working delta. Preserve source/appearance/history under each choice and reject a stale continuation.

### GIF jobs and performance evidence

Keep the functioning GIF controls and current encoder/color semantics. A guarded foreground job is the B1/B2 implementation: Preparing → Choosing destination → Saving → truthful terminal status. Cancel acknowledgement is distinct from worker termination; do not say Canceled before the worker releases ownership. After a truncating provider copy starts, complete tracked copy where possible; byte/resource bounds do not guarantee arbitrary-provider wall time. Failure/unknown close/timeout retains the stage with explicit destination uncertainty and a fresh-destination retry, not false success or destructive retry.

Measure the actual 120–300-second workload with disjoint stage times and a declared device/recipe/provider. Existing 6-second sampled-motion and 15-second Save fixture gates remain. Before judging a B2 repair, set workload-specific absolute/relative/fidelity/memory/response exits from its measured baseline. The retained 250ms job/cancel-request acknowledgement target is distinct from actual worker-stop time. The B1 receipt records measured final observations and the earlier 257ms miss; neither establishes a universal device guarantee. The owner-specific case stays open until actual/equivalent workload and focused phone evidence exist. Background export/recovery and broader R7 fidelity/performance remain tracked.


## Current scope

**Current engineering result:** [PR #37](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/37), application merge `46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30`, supplies qualified **0.12.11 Test / code 23**, with final candidate and merge 258/258 on each API29/API35 plus JVM/build/signing. Common UI, safe departures, save/child/async ownership and the tested GIF optimizations have bounded evidence; handset confirmation, B2's unmet numerical/matched-sample exits, F03 and full product/release acceptance remain open. [Current evidence and APK](B1_WORKSPACE_VERIFICATION.md). PR #38/#39 and this handoff are documentation-only developments.

**Retained PR #31 result (historical):** PR #31 corrected the independent PR #29 Eraser findings and the earlier observed GIF Save failure. Its app merge `3ad65c533f7667a136d4fa1838afcc06d3f484da`, 0.12.10 Test/code22, passed 193/193 per API29/API35 at candidate and merge. Lance subsequently supplied scoped phone passes and named exceptions. [Retained evidence](ERASER_REPAIR_VERIFICATION.md).

Eraser cuts rendered line segments and the swept brush path. A result exceeding the existing stroke limit restores the whole gesture, preserving unrelated artwork, dirty state and history; persistence rejects an invalid count. The GIF optimization keeps exact pixel results, output size, frames, delays and established budgets. Mode boundaries and the established visual style remain required alongside the newly authorized common chrome. The recorded qualification closes its named engineering corrections; the latest phone findings remain open, and full Draw/GIF families and commercial release are not complete.

### Retained PR #29 baseline

The November 23 audit and PR #26 evidence record the prior application baseline. Lance supplied the independent follow-up review on November 24 and authorized corrections and the complete professional-app roadmap. The former owner-review hold is lifted; the four corrections are now engineering-verified in [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29), with 186/186 Android cases per platform on candidate and merge. That v0.12.9 Test checkpoint is historical and superseded first by PR #31 and now by PR #37 for current focused phone testing. The latest phone report above supplies scoped passes and exceptions; A1 activation and B1 engineering are complete, and R1.1/R1.2 remain queued durability work after the current review gate. [Active roadmap](GZ_ENGINEERING_ROADMAP.md) · [Capability inventory](CAPABILITY_MATRIX.md) · [Work ledger](WORK_LEDGER.md) · [Continuity](CONTINUITY.md) · [Release gates](RELEASE_GATE.md).

- **Regular and Collage.** Preserve the familiar photo-first interface, Text, Draw, Shapes, Blur & cover, and independent edits across all 20 Collage tiles. Regular and the selected-tile editor have no visible Masks authoring. Keep saved mask data, rendering and round-trip compatibility. Give all four Crop corners and all four edges density-aware touch targets of at least 48 dp and select the drag target on pointer down. Preserve predictable Freeform and fixed-ratio geometry and exact exported crops.
- **Regular → Pro.** From a loaded standalone Regular photo, selecting Photo editing Pro presents a choice to continue the current image with its edits. Yes opens the current edited result in Pro, preserving its orientation, crop and appearance. No opens an empty Pro editor with “Select an image to edit.” Dismissing the choice leaves Regular open. Regular keeps its own resolved source, edits and history when the user switches back, and stays independent of any Pro changes. Only an explicit scoped Discard may remove its working delta; apply the transactional sequence above so No never means Discard. Saving, deleting or opening a new Regular image follows the normal Regular commands. A failed handoff keeps the Regular work safe and explains the failure.
- **Pro.** Masks, selections, layers, canvas tools and retouching belong here. Pro has no Text creation or editing. Preserve and render legacy Pro Text layers and keep Shapes accessible. Provide Options → Delete current work, moving the existing File command into the common dropdown, with an explicit discard warning and confirmation. Cancel keeps the current workspace intact. Confirmation clears the current working edits and history and returns to the original or blank starting state. Preserve source media and named saved projects. Preserve Undo/Redo behavior through the common arrow controls. Back closes the active transient context first and then returns to the correct workspace with recoverable work. Make controls meaningful for the selected layer and explain blend behavior and targets without changing correct blend math.
- **GIF.** Playback must visibly advance and stay responsive after trim, Reverse, Looks, speed, canvas and other edits. Verify preview, scrubbing, project reopen and actual animated export on representative media. Use the owner's portrait workflow as a reproduction target. A moving frame number alone cannot demonstrate playback.
- **Later pre-Video scope.** Regular gets an offline sticker menu with at least 500 usable finished assets, transparent custom PNG/WebP imports, editing and persistence, and independent sticker editing on selected Collage tiles. Pro gets the offline Subject & Background tools for subject selection, background removal or replacement, object removal, edge refinement, and reshaping, with editable masks/layers and full-resolution export. See the [complete roadmap](GZ_ENGINEERING_ROADMAP.md).
- **Complete scope.** The roadmap and capability inventory include required RAW/color/catalog/batch, full Pro/Subject & Background, GIF/Collage, Video, reliability and release families. Present buttons and narrow passing fixtures are foundations, not completion of the full professional scope.
- **Account timing.** Play Console is not set up. Lance requires setup last, after a fully functioning app and completion of the Greenezone website. Website implementation is outside this task.
- **Release decisions.** Regular and Collage retain their protected provisional status. GIF and Pro remain open for engineering and handset qualification. The separate Video editor stays disabled until the owner formally locks all four pre-Video workspaces. Bilingual English/Spanish welcome and legal acceptance belong to the final phase. The app stays offline with a one-time purchase and no runtime AI.

## Retained photo and Collage requirements from 0.8

- Preserve the user-approved photo-first dark UI and working Regular tools, with the specifically authorized common header/Options and More/Create collage/Histogram removal above.
- Restore full **GZ Editor Pro** top-left title; keep company `© 2026 Greenezone Software LLC` bottom-left on empty main screen.
- Add flexible typography with multiple fonts and editable width/height; drawing and shapes; manual crop handles/freeform; export file-format dropdown separate from destination-platform dropdown.
- Existing screenshots demonstrated that the privacy tools, uncropped photo canvas and Reset were now useful. Do not regress those.
- User requests one large coherent engineering batch and **one** Android phone verification, not countless little pushes.

## Engine / document changes
`CreativeModel.kt`, `CreativeRaster.kt`, `InkMath.kt`, `CropMath.kt`, `ImageExportWriter.kt`, `CreativeCanvasOverlays.kt`, `CreativeDecks.kt`, edit/codec/render/UI integration and automated tests.

Text, ink and shapes remain editable data in `EditDocument.creative`. The keyed local format continues to read version 1. A recipe with source-anchored privacy geometry uses version 2. Free crop is `CropFrame` normalized on the oriented/straightened photo; legacy crop center settings continue to load for previously created projects. Main render draws pixel adjustments, privacy operations, then creative overlays, preserving source image. Export sizes are composed **after** all edits on the rendered copy.

## Export behavior

Export a captured recipe and source. Persist the pending operation before opening Android’s destination picker. After a possible interrupted provider write, offer the retained recipe with a fresh destination. Regular and Pro show the expected dimensions after crop, orientation and device export limits.
File formats: JPEG, PNG, WebP lossy, WebP lossless, BMP 24-bit RGB, TIFF uncompressed RGB 8-bit, PDF one page. Browser/Gallery preview support for BMP/TIFF is not universal. JPEG/BMP/TIFF/PDF flatten transparency to white. Quality only for lossy formats or WebP lossless compression effort. WebP lossless on Android 10 uses the documented legacy 100-quality format.

Social presets: original, Instagram square/portrait/story/reel cover, Facebook square/landscape/story, TikTok photo 9:16, YouTube HD/4K thumbnail, Pinterest pin, LinkedIn square, X landscape, Snapchat story. Fit pads to exact canvas size and preserves all content, Fill crops edges and fills full canvas. No automatic posting, uploader, network or API.

## Verification levels
- Separate pure Kotlin and Android emulator tests, including crop geometry and corner ratio, codec legacy/snapshot roundtrip, text fonts and raster pixels, shapes and ink, export format signatures, social exact target size, alpha behavior.
- PR CI, merge verified SHA, merged-main CI and exact APK artifact.
- The delivered 0.12.10 phone session is reported: unmentioned supplied checks worked, with the named exceptions above. After the agreed corrections, request only the focused remaining phone observations and concrete regression checks, including realistic GIF Save settings/timing and the new common UI/dirty-exit behavior.
- Commercial release still blocked by real-device UX/memory/color, legal/privacy/trademark, Play compliance and production signed AAB.

**No external website or project changes.**

## Major 0.8 integration
- Independent collage document of 1–20 image content URIs and full per-tile EditDocument. Do not flatten individual edits before final export.
- Offline seven template compositions, per-tile edit/reorder/replace/delete, aspect/spacing/radius/border/background/Fill-Fit, bounded preview and output, saved named collages.
- Android picker may fall back to ACTION_OPEN_DOCUMENT without enforcing maximum selection; always clamp imported sources to twenty.
- The photo editor itself is retained as the individual tile editor. The parent photo edit session stays intact when opening/closing Collage.
- Selected crop/blur/text/shape/brush editing guides are **always visible for the active tool**, independent of the main toolbar hidden state. Main UI chrome can be hidden to reveal the whole photo.
- Home screen GZ Editor Pro title restored without marketing slogans.
- Professional and legal roadmap: `docs/PROFESSIONAL_ENGINEERING_ROADMAP.md` (RAW/color/catalog/batch/video) with original engineering and license gates.

## Qualification
Android source + math + emulator compositor and UI checks, exact merge SHA, main APK artifact, one field pass. These are not commercial release certification.
