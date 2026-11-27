# GZ Editor Pro product requirements

> **Current work — November 26, 2026.** [PR #49](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/49) contains the U1 review corrections. Exact [PR #50](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/50) candidate is held at **717.745 ms** against the **650 ms** API35 R1.1 per-Open limit. [Current disposition](WORK_LEDGER.md#u1-review-correction-and-qualification-hold). PR #46 / **0.12.14 Test, code 26** retains its recorded 303/303 engineering qualification. Focused review and a qualified replacement APK precede the next handset recommendation. Remaining R1.2 and later gates stay in scope.

## Latest owner requirements — professional workspaces and simple navigation

The [0.12.13 phone report](coordination/OWNER_PHONE_FEEDBACK_2026-11-25_V25.md) credits all unmentioned supplied instructions 1–5 and opens specific Pro blend/input, workflow ownership and navigation corrections. Preserve those passes and earlier repairs. The [WORKSPACE_UX_BLUEPRINT](WORKSPACE_UX_BLUEPRINT.md) is the detailed normative interaction contract under this specification; the single roadmap and ledger retain all 77 families.

Regular is a professional photographic development studio. Pro is a Photoshop/GIMP-style layered composition workflow. Keep authentic Pro paint/masks/vector/adjustment capabilities with explicit layer targets; replace duplicated generic Regular panels and misleading labels. Shared numerical kernels do not require identical tool decks. Full R01–R18 and P01–P16 scope remains, including approved Regular/tile creative tools, later RAW/color/catalog/batch and deeper Pro professional features.

Shared Tools, Options, Undo/Redo, workspace selection and context controls occupy the same anchored positions across Regular, Pro, Collage and both GIF routes. Future gated Video inherits them. Keep the existing dark image-first styling, hideable chrome and guides. No duplicate major-action homes. Regular More/Create collage/Histogram UI stays removed.

**Delete** clears the current working session to a true empty workspace. **Reset all edits** retains existing media and performs one undoable neutral reset. **Regular↔Pro** has one four-choice surface: **Discard / Save current work / Bring current work / Cancel**. Discard opens blank; Save succeeds then opens blank; Bring carries current edits and editable native lineage with no named/hard save. Save-to-blank is the explicitly documented engineering interpretation; Bring is the path for continuing edits. Preserve unrelated destination work as a local draft without a second mandatory decision. Final Save from either workspace retains the complete linked native work.

The latest report expressly authorizes these changes and necessary dependencies. Do not modify unrelated passed behavior without owner authorization. This is a targeted correction to the active contract, not another planning cycle or authority for a wholesale rebuild.

## Agreed interaction contract — Stage 3

**Amended by the latest owner report and [workspace blueprint](WORKSPACE_UX_BLUEPRINT.md).** Keep the original [50 answers](coordination/CHATGPT_RESPONSE_TO_50_2026-11-24.md) as dated rationale/evidence. Its no-fourth-choice, Resume/Yes-No, one-way live transfer and reset-as-Delete provisions are superseded. The consumed activation hold must not return. Unaffected identity, source, cancel, child and export protections below remain required.

### Common header and one major-command home

Reuse EditorWorkspaceHeader: brand/Options first row, workspace/Undo/Redo second row, shared lower Tools anchor and context slots. Follow the blueprint's precise placement, accessibility/insets, empty/loaded/busy behavior and source-specific adapters. Hide header/tools together after a free canvas tap; never hide active editing guides.

One Options menu has the fixed common sequence: New / Open media; Open project (including distinct Working drafts); Save project; Save project as; Export; Reset all edits; Delete; Help. Relevant media variants are choices within the first source action. Unsupported current-state actions explain their reason; future tools are not fabricated. Direct Add layer, object/frame editing, playback and child Done/Cancel remain editing controls. A second blank New action identical to Delete is removed. Saved-record deletion stays explicitly targeted inside the project picker.

The selected Collage photo keeps explicit photo-project Save/Open ownership and its protected Reset behavior, not parent Save/Export. Global Delete explicitly identifies and clears the parent Collage; do not turn it into tile reset/removal. See the blueprint's child command exception and reset profiles. Auto tone remains in Regular Light, Auto WB in Color and Save my look once in Looks.

### Save identity and content baselines

Stable project IDs are independent of display names. Save updates the active named identity; unnamed Save names once on success. Save as creates a new identity and independent mutable lineage. Replacement requires a specifically identified target and explicit consent; a dirty-dialog Save need not repeat an equivalent warning. Only atomically published captured content becomes saved; newer edits stay dirty. Export writes the active rendered output and never marks native work clean.

Dirty compares semantic content/source ownership with the saved or entry baseline, not filename/history length/export/session retention. Undo to an equivalent baseline becomes clean. View/selection/chrome do not add content history. Named Discard restores the saved baseline; unnamed Discard removes the unsaved branch to its entry baseline; neither deletes named records or originals. The Regular↔Pro Discard choice still enters a blank destination.

Delete removes the active working-session root and its exclusive unsaved continuation, history and binding; Reset retains the blueprint's current-media profile and is undoable. They have separate semantic command IDs and must not share Pro's old reset-as-Delete implementation. Linked-session Save persists native parents and derivatives with exact revision ownership; a flattened active bitmap alone is insufficient.

### Departure, cancellation and asynchronous ownership

Top-level Regular↔Pro uses the single four-choice contract for nonempty work. A truly empty source can change to an empty target directly. The minimum durable source/destination custody must be ready before replacing either active slot; no named Save is forced by Bring. Inline naming/collision/failure handling belongs to the same switch surface. No Resume/Start another/Yes-No cascade remains.

Other ordinary dirty Back/app-exit/workspace/New/Open/replace routes keep Save / Discard / Cancel. Clean transitions do not prompt falsely. Picker/loader/save failure or cancellation retains work and route; stage destructive disposition until a usable target and acknowledged custody exist. An explicitly completed named Save remains recorded even if a subsequent step is canceled.

Resolve frontmost menu/IME/dialog/tool context first; one Back cannot also leave the document. Canceled predictive Back commits nothing. Completed gestures commit once; pointer cancel/interrupted multitouch/target invalidation restores full pre-gesture document/history. Back cancels a draft, not Undo.

Bind pending intents and results to workspace/document/source revision, target and child identity, plus one execution token. Deduplicate Save/Back/Done; canceled naming/save cannot be revived by an unrelated later save. Stale successful save cannot mark newer content clean or authorize departure. Late results may clean only their own resources.

Zero-layer changed Pro setup currently cannot be named-saved; show an honest unavailable reason. Do not add a fake image. The four-choice transfer surface does not imply full zero-layer persistence is implemented; R2 retains that work. Protected output-copy phases retain ownership and honest unavailable navigation; cancelable phases settle before destructive changes.

### Collage child and single-tap rules

A completed unconsumed free tap selects the hit tile, if any, **and toggles chrome once**; a margin tap toggles without changing selection. Apply in either visibility state. Handle/paint/crop/drag/pinch-owned streams never also toggle; canceled or moved/multitouch streams are not free taps. Retain intentional established zoom behavior and active guides. The parent double-tap requirement is removed; adjust child recognition only for reproduced conflicts.

Child entry baseline and standalone-photo save baseline are separate. Saving a photo project does not Apply to parent or make the child clean relative to its entry; Cancel leaves that explicit named photo save intact but restores the child entry. Done applies one transaction to stable parent/tile identity, not index or source URI. Dirty child Back offers **Apply to tile / Discard tile changes / Cancel (keep editing)**. Apply/Discard returns only to the parent; the same Back must not also leave the parent. A child workspace-switch/parent-exit request stages that child disposition, then applies Save/Discard/Cancel to the resulting parent candidate. A later Cancel, failed Save or failed target load preserves the original child draft and parent transaction state; do not commit provisional discard or lose unapplied work. An explicitly completed named save remains recorded accurately. Direct Done is a completed parent edit, and direct Cancel restores only child entry. A source/project replacement inside the child protects its pending draft without writing siblings or silently applying it to the parent.

Relink preserves intended source identity and recipe after compatibility checks. Replace explicitly resets the selected photo recipe to neutral for the new source while retaining the layout slot; Undo restores the complete previous tile. Stale child results require parent/tile/source validation; compatible reorder can resolve by stable ID, conflicting edits/replace/delete cannot. Keep recoverable child work on a conflict.


The newly authorized global Delete is the explicit exception to ordinary child/parent staged departure: its single confirmation names the whole collage and pending child changes, then clears that working session atomically. Unrelated child Apply/Cancel/standalone-save behavior remains protected. A selected tile is not a second top-level Regular↔Pro transfer.

### Symmetric Regular–Pro working session

Follow the [complete switch/lineage contract](WORKSPACE_UX_BLUEPRINT.md#regular--pro-one-four-choice-switch). Both directions carry current appearance, alpha/geometry and native edit lineage, preserve displaced unrelated drafts, reject stale or failed preparation, and let the owner save at the end. Minimal R1.2/S06 acknowledged native generations are a prerequisite for truthful durability, not permission to complete all R1/R2 before fixing the reported controls. Cold reopen and real editable Pro layers after repeated exchange are required.

### GIF jobs and performance evidence

Keep the functioning GIF controls and current encoder/color semantics. A guarded foreground job is the B1/B2 implementation: Preparing → Choosing destination → Saving → truthful terminal status. Cancel acknowledgement is distinct from worker termination; do not say Canceled before the worker releases ownership. After a truncating provider copy starts, complete tracked copy where possible; byte/resource bounds do not guarantee arbitrary-provider wall time. Failure/unknown close/timeout retains the stage with explicit destination uncertainty and a fresh-destination retry, not false success or destructive retry.

Measure the actual 120–300-second workload with disjoint stage times and a declared device/recipe/provider. Existing 6-second sampled-motion and 15-second Save fixture gates remain. Before judging a B2 repair, set workload-specific absolute/relative/fidelity/memory/response exits from its measured baseline. The retained 250ms job/cancel-request acknowledgement target is distinct from actual worker-stop time. The B1 receipt records measured final observations and the earlier 257ms miss; neither establishes a universal device guarantee. The owner-specific case stays open until actual/equivalent workload and focused phone evidence exist. Background export/recovery and broader R7 fidelity/performance remain tracked.


Latest unmentioned functional phone checks receive pass credit. No new timings or matched-source/provider data were supplied, so B2's recorded numeric misses and F03 quantitative closure remain open. Do not convert absent numerical data into a performance pass or describe all previously passed GIF controls as unconfirmed.

## Current scope

**Current engineering result.** [PR #43](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/43) qualifies the Regular saved-project metadata index and selected-only decode, legacy migration and rollback, and safe Pro deletion of unreadable records. The application merge is `b0f0d0e5e2d879ddddedede442ba92ef30b69a78`. See the [exact R1.1 receipt](WORK_LEDGER.md#completed-r11--bounded-saved-projects-and-pro-rv4).

**Retained PR #31 result (historical):** PR #31 corrected the independent PR #29 Eraser findings and the earlier observed GIF Save failure. Its app merge `3ad65c533f7667a136d4fa1838afcc06d3f484da`, 0.12.10 Test/code22, passed 193/193 per API29/API35 at candidate and merge. Lance subsequently supplied scoped phone passes and named exceptions. [Retained evidence](ERASER_REPAIR_VERIFICATION.md).

Eraser cuts rendered line segments and the swept brush path. A result exceeding the existing stroke limit restores the whole gesture, preserving unrelated artwork, dirty state and history; persistence rejects an invalid count. The GIF optimization keeps exact pixel results, output size, frames, delays and established budgets. Mode boundaries and the established visual style remain required alongside the newly authorized common chrome. The recorded qualification closes its named engineering corrections; the latest phone findings remain open, and full Draw/GIF families and commercial release are not complete.

### Retained PR #29 baseline

The November 23 audit and PR #26 evidence record the prior application baseline. Lance supplied the independent follow-up review on November 24 and authorized corrections and the complete professional-app roadmap. The former owner-review hold is lifted; the four corrections are now engineering-verified in [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29), with 186/186 Android cases per platform on candidate and merge. That v0.12.9 Test checkpoint is historical. The owner phone report supplies scoped passes and exceptions. A1, B1 engineering and R1.1 engineering remain credited. PR #46 records bounded U1 qualification, including the minimum R1.2/S06 bridge custody slice. The [independent U1 review](coordination/GPT_6_PRO_U1_REVIEW_2026-11-26.md) led to the two targeted corrections and a reproduced held-Save issue addressed in PR #49. Full qualification remains held and remaining R1.2 retains its broader durability scope. [Active roadmap](GZ_ENGINEERING_ROADMAP.md) · [Capability inventory](CAPABILITY_MATRIX.md) · [Work ledger](WORK_LEDGER.md) · [Continuity](CONTINUITY.md) · [Release gates](RELEASE_GATE.md).

- **Regular and Collage.** Preserve the familiar photo-first interface, Text, Draw, Shapes, Blur & cover, and independent edits across all 20 Collage tiles. Regular and the selected-tile editor have no visible Masks authoring. Keep saved mask data, rendering and round-trip compatibility. Give all four Crop corners and all four edges density-aware touch targets of at least 48 dp and select the drag target on pointer down. Preserve predictable Freeform and fixed-ratio geometry and exact exported crops.
- **Regular ↔ Pro.** Use the single four-choice working-session contract above. Bring is symmetric and creates no named save. Preserve native parents, repeated edits and final linked Save from either mode; Discard and successfully completed Save open blank destinations.
- **Pro.** Layers, masks, selections, object/canvas geometry, vector shapes, painting and retouch belong here. Pro has no new Text authoring; legacy Text still renders. Use clear layer-oriented tools and real tappable Blend controls. Options → Delete empties the working session; Reset all edits retains its media per the blueprint. Preserve qualified history, cancellation, source and saved-project safety.
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
- The latest 0.12.13 report credits unmentioned supplied checks 1–5 and protects them. U1 requests focused confirmation only of the newly changed behavior and concrete regression risks; no replay of the complete successful script. Older phone/engineering receipts retain their exact bounded scope.
- Commercial release still blocked by real-device UX/memory/color, legal/privacy/trademark, Play compliance and production signed AAB.

**No external website or project changes.**

## Major 0.8 integration
- Independent collage document of 1–20 image content URIs and full per-tile EditDocument. Do not flatten individual edits before final export.
- Offline seven template compositions, per-tile edit/reorder/replace/delete, aspect/spacing/radius/border/background/Fill-Fit, bounded preview and output, saved named collages.
- Android picker may fall back to ACTION_OPEN_DOCUMENT without enforcing maximum selection; always clamp imported sources to twenty.
- The photo editor itself is retained as the individual tile editor. The parent photo edit session stays intact when opening/closing Collage.
- Selected crop/blur/text/shape/brush editing guides are **always visible for the active tool**, independent of the main toolbar hidden state. Main UI chrome can be hidden to reveal the whole photo.
- Home screen GZ Editor Pro title restored without marketing slogans.
- The single active [GZ engineering roadmap](GZ_ENGINEERING_ROADMAP.md) retains RAW/color/catalog/batch/video and engineering/license gates. PROFESSIONAL_ENGINEERING_ROADMAP.md is historical source material, not a second active plan.

## Qualification
Android source + math + emulator compositor and UI checks, exact merge SHA, main APK artifact, one field pass. These are not commercial release certification.
