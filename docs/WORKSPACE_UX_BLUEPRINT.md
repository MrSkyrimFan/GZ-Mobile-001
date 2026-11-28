# Workspace interaction and tool-ownership blueprint

**Status** — Controlling workspace behavior. PR #62 passed exact candidate and merged qualification for the reviewed U1 corrections. Focused handset confirmation and wider family exits remain. [Exact qualification](WORK_LEDGER.md#qualified-gif-paused-seek-repair).
Updated November 26, 2026 (America/Denver), preserving the [0.12.13 owner phone report](coordination/OWNER_PHONE_FEEDBACK_2026-11-25_V25.md) and all existing interaction requirements.

This is the detailed interaction contract under [PRODUCT_SPEC](PRODUCT_SPEC.md), not a second roadmap. [GZ_ENGINEERING_ROADMAP](GZ_ENGINEERING_ROADMAP.md) preserves all 77 families; [WORK_LEDGER](WORK_LEDGER.md) alone orders execution. Latest direct owner instructions control. Historical blueprints, CQ answers and receipts remain evidence except for the specifically superseded interaction clauses named below.

## Professional tool ownership

Regular and Pro serve different document workflows. Shared algorithms, file safety and visual components are appropriate; duplicated general-purpose tool decks are not a substitute for distinct workflows.

| Workspace | Primary purpose and tool ownership | Required distinction |
| --- | --- | --- |
| Regular | Professional photographic development: Light, Color, white balance, curves/HSL/grading, Detail, optics/perspective, Looks, crop and output. Full R01–R12/R18 RAW/color/catalog/batch scope remains. Approved Text, Draw, Shapes, privacy and stickers remain R13–R17 and available to Collage photo editing. | A photograph and its editable development recipe. Do not introduce Regular Masks authoring, Histogram UI or Create collage. |
| Pro | Independently editable image, paint, vector-shape and adjustment layers; Layers, Transform, Select & mask, Retouch, Paint, Vector shapes, Blend, Adjustments, Layer effects and Canvas. Full P01–P16 groups/compositing/selection/brush/vector/retouch/interchange/subject work remains. | Show the actual selected layer/object/mask and editing scope. No copied Regular Light/Color/Detail/Effects/Grading deck as Pro's primary workflow. No new Pro Text authoring; retained legacy Text still decodes/renders. |
| Collage | Independent photos in a layout; arrangement, borders/gaps/background and per-photo editing through the protected child transaction. C01–C04 and all seven layouts/1–20 tile scope remain. | A layout of independently editable photos. Tile tools must not edit siblings with the same source URI. |
| GIF | Video/existing-GIF and photo-sequence timing, playback, frames, palette and GIF output. G01–G08 remain, including still-unfinished creation/timeline/fidelity work. | One GIF workspace with route-specific tools, not a second Pro or the future audio/video editor. GIF remains silent. |
| Future Video | Separate professional temporal editing, sound/mixing, multitrack/compositing/color/output V01–V08. | Inherits these control anchors after all four explicit pre-Video locks. No fake enabled Video tools before that gate. |

### Why Pro keeps paint, vectors and adjustments

Official Photoshop documentation describes painting with controllable brush behavior, editable vector shapes/paths, masks, and editable adjustment layers. GIMP likewise treats blend as a layer/composition property. Those professional operations remain required.

In GZ, **Paint** means painting a clearly identified paint layer; the required later R3/P05/P08 mask-painting workflow must explicitly target a mask when implemented. U1 must not expose an unsupported mask-painting action or imply it already exists. **Vector shapes** means selecting and editing a separate shape/path object; **Adjustments** means a named adjustment object with visible stack/mask scope. Regular's Draw/Shapes remain photo decoration with their existing behavior. There is no requirement to duplicate Regular's whole photographic development presentation in Pro.

For U1:

- Rename ambiguous new-layer actions to **Paint layer**, **Shape layer**, and **Adjustment layer**. Paint's empty-target action is **Add paint layer**. Brush remains the tool within Paint, with a visible target; it is not a second generic Draw mode.
- Replace the top-level Tone & color deck with a layer-oriented adjustment chooser/properties view. Reuse proven processing kernels and saved data. New generic adjustment creation starts neutral; choosing a named effect such as Black & white applies that effect explicitly. Do not silently change old GRAYSCALE records.
- Each adjustment/filter property has one editing home with a target description: selected layer, the composition below an adjustment, or an implemented mask. Do not claim clipping or group behavior before that capability exists.
- Preserve native per-image grades in legacy projects. Expose their existing editable settings through the selected layer's properties, with accurate scope and no second global Regular-style deck. A new label alone is insufficient if the same generic deck remains the main Pro workflow.
- Keep actual shape/paint/mask/clone/blend engines and independent saved objects. Advanced path geometry, brush dynamics, groups, filters and retouch children remain explicit R2/R3 work; U1 does not pretend to complete them.
- Existing original style and passed Regular creative tools stay intact. Do not remove required capabilities merely to eliminate repeated words such as color or brush.

### Primary research and limits

Official sources consulted November 25, 2026:

| Reference | Fact used in this design |
| --- | --- |
| [Adobe: Lightroom Develop](https://helpx.adobe.com/lightroom-classic/desktop/help/applying-adjustments-develop-module-basic.html) | Photograph development, tonal/color/detail corrections, history and output motivate Regular's ownership. |
| [Adobe: Brush/Pencil](https://helpx.adobe.com/photoshop/desktop/apply-painting-techniques/brushes-presets/paint-with-brush-or-pencil-tool.html) | Brush painting has controllable size/hardness/opacity/flow and pressure behavior. |
| [Adobe: shapes and paths](https://helpx.adobe.com/photoshop/desktop/draw-shapes-paths/draw-lines-curves/understand-shapes-paths-and-drawing-modes.html) | Editable vector shapes/paths are distinct from painted pixels and can support selections/masks. |
| [Adobe: adjustment layers](https://helpx.adobe.com/photoshop/desktop/create-manage-layers/color-adjustment-fill-layers/work-with-adjustment-and-fill-layers.html) and [targeted masks](https://helpx.adobe.com/photoshop/desktop/create-manage-layers/color-adjustment-fill-layers/use-layer-masks-to-target-adjustment-or-fill-layers.html) | Tonal/color adjustments can remain editable and be limited by masks. |
| [Adobe: nondestructive techniques](https://helpx.adobe.com/photoshop/desktop/create-manage-layers/create-layer-compositions/non-destructive-editing-techniques.html) | Retained source, editable adjustments, masks and separate-layer retouch are substantive professional workflows. |
| [GIMP: layer modes](https://docs.gimp.org/3.0/en/gimp-concepts-layer-modes.html) and [Adobe: blending descriptions](https://helpx.adobe.com/photoshop/desktop/repair-retouch/adjust-light-tone/blending-mode-descriptions.html) | Visible blend effects depend on underlying content and pixel values. An unchanged special case is not evidence that a tap should be ignored. |
| [Adobe: Lightroom external editing](https://helpx.adobe.com/lightroom-classic/desktop/work-with-external-editors/open-lightroom-photos-external-applications.html) | Its external copy/Smart Object/Save flow does not provide GZ's requested no-save bidirectional in-app session contract. GZ must implement and qualify that requirement itself. |

These sources inform workflow roles, not brand-copying, feature-completion claims, imported dependencies or permission for runtime AI. The canonical non-AI capability inventory controls GZ's full scope.

## Shared control locations

Reuse EditorWorkspaceHeader and existing controllers. Do not replace the application shell or move passed editing algorithms into a new architecture.

| Logical slot | Contract in every workspace |
| --- | --- |
| Header row 1 | GZ Editor Pro at the leading edge; text **Options** at the trailing/top-right edge, outside system bars/cutouts. |
| Header row 2 | Workspace selector at leading edge; common Undo then Redo arrow icons at trailing edge. Context Back uses the same reserved leading context position when applicable. |
| Canvas | Central image-first surface, with the existing selection/crop/brush guides. No permanent header over a hidden-chrome image. |
| Tool bar at the lower safe edge | **Tools** entry at the leading edge in every loaded workspace; selected-tool/target context in the middle; expand/collapse control at the trailing edge. Tool content opens above this bar in the same panel region. No workspace-specific top-right Tools location competing with Options. |
| Context actions | Child Done/Cancel and playback have distinct reserved context rows adjacent to the lower panel. They never replace or relocate Options, Tools, Undo/Redo or workspace selection. |
| Empty state | Shared header visible; same tool-bar position with an honest no-media state. Body guidance is noninteractive. No duplicate Open/Save/Export cards or fake enabled tools. |

"Same location" means the same anchored slot/order for the same window class, not identical absolute pixel coordinates on every phone. Portrait/landscape, safe insets, 320dp width and 200% font must preserve reachable controls without overlap. Keep the existing dark palette, typography, spacing tokens and image-first character. Shared controls have accessible names, a minimum 48dp touch target, honest enabled state and a concise reason when unavailable.

A completed unconsumed free-canvas tap hides/restores header and tool chrome once. Active guides remain usable; preserve the existing accessible reveal action. Drag/pinch/paint/crop/handle/multitouch/canceled streams must not toggle chrome. The previously qualified Collage pointer-down selection and slop repair remain protected.

## One Options menu and command ownership

Use a shared ordered command model and per-workspace adapters. Major commands have one visible clickable home. Keep the following common slots/order; contextual labels may identify image/GIF/photo-project output:

1. **New / Open media…** — a compact workspace-specific source chooser; one source can go straight to its existing picker.
2. **Open project…** — one picker for named projects and clearly separated Working drafts.
3. **Save project**.
4. **Save project as…**.
5. **Export image…** or **Export GIF…**.
6. **Reset all edits**.
7. **Delete**.
8. **Help**.

Source chooser choices are genuinely distinct: Regular New photo; Pro New canvas or Open photo as new document; Collage Add photos and a distinct layout-creation action only if implemented; GIF Open video/Open GIF and the currently supported photo-sequence Add photos path. A command that merely clears to the same empty state is **Delete**, not another New GIF/New collage alias. Do not expose unfinished photo-sequence creation merely to make a menu look complete. Adding a layer is a direct composition-edit action, not a duplicate Open new document action.

Keep command slots visible with an applicable reason when current state disables them; do not add nonfunctional future capabilities. The child photo context keeps its already-qualified explicit **Open photo project / Save photo project / Save photo project as** labels and does not gain a new standalone tile-export route. A child unavailable Export slot directs the user to Done and parent export without silently applying the child. Saved-record deletion stays in the project picker, named **Delete saved project** with the exact record target; it is not the current-work Delete.

Retain Regular Auto tone in Light, Auto WB in Color and Save my look once in Looks. More, Regular Create collage and Regular Histogram stay removed. Help must explain the current controls and target, not old File/More routes.

## Delete and Reset are different transactions

These are engineering decisions implementing O25-03. They do not authorize deletion of source files or named saved projects.

### Delete

**Options → Delete** clears the active top-level working session and returns to that workspace's true empty state. One confirmation identifies the current work and says original files and saved projects are kept. If already empty, explain that there is no work to delete.

After confirmed success: no loaded media association, selected object, edit/redo history, active save-target binding, pending picker/transfer continuation or native recovery root may resurrect that working session. Invalidate tokens before releasing owned assets; use a durable tombstone/publication boundary. Linked Regular/Pro views of the same working session are cleared together. Unrelated draft roots and named saves remain intact. Release only exclusively owned derivative assets after reference checks.

In a selected Collage photo, Delete still means the top-level Collage work. Confirmation must explicitly say **Delete this collage and its pending photo edits?** Commit parent/child invalidation once, close the child and show empty Collage; do not add a child Apply/Discard cascade. Removing a layer, photo or frame is a separate contextual **Remove layer / Remove photo / Remove frame** action with its own target and history. These are not aliases for global Delete.

Both GIF routes clear the same GIF owner. Independent staged export retry data is not an editing draft: preserve its separate custody. During an irreversible/protected provider-copy phase, disable Delete with an accurate reason; during cancelable preparation, use the existing owned cancellation and do not clear until it has settled. Never claim successful deletion while a write that can restore it remains unowned.

### Reset all edits

Reset keeps the current media and workspace open and removes the edits applied to those inputs. It is **one undoable content transaction**, not a history clear, saved-project overwrite or Delete. A named saved project remains unchanged until explicit Save. Do not equate Reset with reverting to the latest named save.

| Context | Retained media/input | Neutral reset result |
| --- | --- | --- |
| Regular | Same photo/source identity, orientation and source grant | Empty development/creative recipe, neutral crop/geometry as source permits; preserve the current working identity. Existing undoable Regular behavior is protected. |
| Pro | Every currently present imported image asset, stable IDs/order and creation canvas setup; no loss of a later-imported image merely because an old entry baseline predates it | Neutral image-edit/mask/blend/opacity/transform state using the recorded source-placement baseline; remove all composition-authored paint, vector, adjustment, effect and legacy Text content in the active document, including content loaded from a named project, as one undoable transaction. Membership depends on native content kind and the source-bound baseline, never on whether it was created before or after opening this session. No-source canvas remains a neutral canvas with its creation dimensions, unlike Delete's empty entry screen. |
| Parent Collage | All current photos, stable tile identities/order and creation layout/aspect | Neutral per-photo recipes and layout styling using the documented creation defaults; no photo removal or sibling identity merge. |
| Selected Collage photo | That child photo/source only | Existing child-photo reset behavior; parent and siblings remain unchanged until Done. Display the photo target next to these contextual commands. This nested editor is not a separate Regular↔Pro bridge. |
| GIF video/existing GIF | Same source and its neutral import timing/orientation/aspect baseline | Full import range, forward/default speed, neutral effects/framing/captions and source loop/timing defaults. Do not change source bytes or conflate the two source types. |
| GIF photo sequence | All current source photos/frame IDs/order | Neutral frame edits, timing/loop/canvas defaults recorded for that sequence; no frame deletion. |

An imported raster is an input asset; Reset cannot unpaint pixels baked into that source. Native paint/vector/adjustment/legacy Text records are authored edits and are removed from the active reset result, while Undo and unchanged named saves retain them. Linked native parent inputs are retained under the explicit rule below.

Record these source-bound baselines in native/recovery storage. Legacy migration must preserve media and derive a valid baseline without silently resetting to a guessed or unrelated source. If it cannot, report the specific limitation and keep the work intact.

A Pro composition brought into Regular is Regular's current input appearance: Reset there removes the Regular edits applied to that input, not hidden edits inside its retained Pro parent. The confirmation/target text must make the retained input clear. Returning to Pro exposes that native parent for its own editing/reset. Reset must not destroy linked-parent custody or apply a finish twice.

## Regular ↔ Pro: one four-choice switch

For a top-level switch with nonempty current work, including a clean loaded document, show **one surface** with exactly these four choices. The target workspace is named. A truly empty source can switch directly to an empty target, preserving an unrelated target draft as below. Do not silently resume unrelated work. Ordinary Back/exit or switches involving Collage/GIF retain the protected Save/Discard/Cancel rules; Bring is not an accidental cross-owner feature.

| Choice | Required result |
| --- | --- |
| **Discard** | Remove only the current unsaved branch relative to its named-save/entry baseline; discard its redo/recovery continuation, preserve named saves and originals, and enter a **blank destination**. Do not carry the discarded snapshot into it. |
| **Save current work** | Successfully publish the exact native working revision and all linked editable lineage to the selected named identity, then enter a **blank destination**. This blank destination is an explicit engineering interpretation distinguishing Save from Bring; show that consequence in the choice text. |
| **Bring current work** | Carry the current edited appearance and native lineage into the destination, with no named Save, naming prompt, export, picker or forced hard save. Keep unsaved status accurate. |
| **Cancel** | Keep document(s), route, history, selection, baselines and pending work exactly as before the request. |

Unnamed Save reveals the name field **inside the same decision surface**. Collision replacement/renaming and failure feedback stay within that surface, bound to the captured revision and explicit target; no extra navigation dialog. Canceling naming cancels the pending switch. Clean named Save may reuse its verified saved revision. An unsupported zero-layer setup keeps all four choices visible with an accurate unavailable Save reason; do not fake persistence. If Bring has no renderable media, explain that case instead of creating a bogus image. Full named zero-layer canvas support stays R2 unless a narrowly justified prerequisite is brought forward and qualified.

No mandatory **Resume existing / Start another / Yes bring / No blank** sequence remains. No second target Save/Discard modal. No "keep draft" substitute for Discard. Close the transient Options/Tools surface before the single switch decision; one Back dismisses only its current context.

### Existing target and working-draft custody

If the other workspace contains unrelated work, preserve it automatically as a distinct local working draft before replacing its active slot. Open project contains the Working drafts section; do not introduce another major command. List only actual acknowledged recoverable roots, scoped to the applicable workspace/session; never list an in-memory placeholder as durable. U1 requires these roots for Regular/Pro, not completed all-workspace draft autosave. Other owners may show a section only for roots they can actually restore. A short status line can say previous work remains there. Named saved projects and working drafts must be visibly distinguished.

Automatic private recovery checkpoints are durability, not a user-requested named project save. Bring must not create a named record, increment a named version, mark dirty work clean or require a save location. If required custody fails or its declared bound is exhausted, retain both originals and the route and report the failure. No silent eviction of dirty work.

When the two views already belong to the same linked working session, switch that session rather than creating a new duplicate draft at every hop. Stable working-session/document/source/revision identities, immutable generations and explicit roots separate that session from unrelated drafts.

### Editable round trip and final Save

Required sequence: **Regular → Pro → Regular → Pro**, with real edits on every leg, and the symmetric starting case.

Use the smallest versioned manifest/adapter around existing native codecs. Retain native Regular recipes and sources, native Pro layers/masks/objects, their immutable revision relationships, the active output revision, and declared geometry/alpha/profile/precision/render semantics. Preserve currently supported precision; never degrade through an unnecessary JPEG or silently advertise future high-bit support.

Regular edits a composite appearance on a Pro→Regular transfer. A rendered derivative is acceptable only as a reconstructible, owned bridge asset whose exact native parent remains durable and editable. It cannot replace the layered master. On the return to Pro, preserve the Regular finish as an explicit linked revision and keep the original Pro layers directly editable. Editing a retained parent must invalidate/recompute downstream output correctly or create an explicit retained branch without silently losing the finish. No double-application on an unchanged return; no repeated lossy flattening.

**Final Save from either workspace must persist the complete linked native lineage**, not only the active bitmap or recipe. Cold reopen must recover the same appearance and editable Pro layers. Save as creates independent identity/revision ownership with safe shared immutable assets or copies; editing one saved identity must not corrupt another. Export renders the active result and never falsely marks native work saved.

The minimum Regular-and-Pro durable checkpoint/lineage slice is brought forward from existing R1.2/S03/S06 for this flow. It does not close all-workspace autosave, all R1 history/provider/bundle work or R2 groups/compositor work. In-memory retention alone is insufficient.

### Transaction and failure rules

Capture source, destination, child exclusion and expected revisions once. Prepare/validate derived content and displaced-target custody before committing route or destructive disposition. Bind a single execution token to that captured intent. Repeated taps do not execute twice.

Cancel, low storage, failed save/derivative/checkpoint, missing grants or a stale source/target keep work and route intact; an explicitly completed named Save remains accurately recorded. A stale successful save cannot declare a newer edit clean or authorize its departure. Late picker/render/save callbacks can clean up only their own resources. On actual process loss, restore the last acknowledged native generation without auto-confirming the pending switch or reapplying a partial gesture.

### U1 review follow-up — native editing and preparation ownership

These are enforcement details for the existing editable-round-trip and failure contracts, not new product scope.

- Any actual entry point into a native Pro editing tool, including Layers shortcuts, uses the same guarded transition out of finished-output preview. The displayed selection/retouch instructions must match the active target and usable canvas. Keep the native parent and linked Regular finishes intact; tool selection alone creates no content history.
- Every prepared native-work handle has an explicit owner until transfer or release. Failed source decode, stale/publication failure and cancellation, including acquisition/result delivery, release only the operation's own leases. Reference-aware collection still preserves named/active/draft/export roots; success and failure do not justify clearing global stages.
- A completed inline named Save remains accurately acknowledged if its switch is canceled. Cancel keeps the route, and a stale save result cannot clean newer edits. A controlled held-publication test reproduced the U1R-V1 acknowledgement issue. The replacement uses exact-owner acknowledgement.

The [completed correction dispatch](coordination/CODEX_U1_FOLLOWUP_2026-11-26.md) records reproductions and file boundaries. [The ledger](WORK_LEDGER.md#qualified-gif-paused-seek-repair) records the resolved hold and exact qualification.

## Pro Blend: usable property, truthful preview

The owner named ten unclickable modes. The current model advertises **Normal plus fifteen**. Preserve every value and legacy record.

A valid selected layer and the shared operation-availability rule govern property editing. Allow a legitimate blend value to be selected even when the layer is hidden, has zero opacity, is at the bottom of a transparent composition or currently lacks overlapping content. Such states can explain the preview; they must not make an ordinary-looking entire list silently untappable. Locked/unsupported targets, if implemented, need an honest reason. Preserve busy/export/pending-departure mutation guards.

Provide one discoverable **Blend mode** selector with the current value and selected layer visible. All sixteen choices must be reachable without hunting through an unmarked horizontal list. Restore Normal must use the same guard; selection of the existing value is not a new content edit. Opacity and all related controls reflect the same ownership/availability logic.

Distinguish input receipt, model/property change and visible pixel change. Neutral colors or absent overlap can legitimately show no image difference. Use concise context guidance, not a modal lecture or dismissal of the phone report. Preserve qualified PR #41 selection guards and blend math unless a new independent reproduction identifies a calculation defect.

## U1 acceptance — finite, observable and cumulative

U1 is the owner's correction wave within the existing roadmap. Its detailed execution/files are in the [handoff](coordination/CODEX_WORKSPACE_CORRECTION_HANDOFF_2026-11-25.md). Before production fixes, record red reproductions against the qualified application; distinguish a source-supported condition from a reproduced handset state.

| Exit | Required evidence |
| --- | --- |
| U1-01 Blend | Actual coordinate taps through the real tool panel for all sixteen modes; contrasting overlapping patches with independent pixel expectations; white/black/transparent, bottom/hidden/zero-opacity/empty-ink/adjustment-only/nonoverlap cases; scroll does not select; selected label/model/history, Undo/Redo, native reopen and decoded export agree. Busy and stale callbacks change nothing. |
| U1-02 Tool purpose | Actual reachable inventory shows separate Regular photo development and Pro layer/mask/vector/adjustment roles; no copied top-level Regular deck, misleading Draw / Brush entry or generic adjustment that unexpectedly turns grayscale. Legacy grades/paint/shapes remain editable and export unchanged until deliberately edited. |
| U1-03 Common controls and clear/reset | Empty/loaded/busy states in Regular, Pro, parent/child Collage and both GIF routes; same anchored shared controls, one command home, 320dp/200% font/portrait/landscape/insets/pointer reachability; Delete genuinely empties and does not resurrect after Undo/restart/late result; Reset retains the specified media and Undo restores the pre-reset work. Named projects/original hashes/unrelated drafts/export retry custody survive. |
| U1-04 Symmetric switch | Both directions/all four choices/clean-dirty/named-unnamed/empty; no extra Resume/Yes-No/target prompt; inline naming/collision/failure; unrelated target preservation; four-leg edited round trip, process loss, final Save from either mode, cold reopen, editable native Pro parents, Save-as independence, alpha/geometry/appearance and no repeated finish. |
| U1-05 Integration | Preserve inherited meaningful coverage and budgets; map any assertions changed solely because the owner superseded old behavior. Candidate and exact app merge pass JVM/build/signing and the full API29/API35 suites. Publish all original failures, unique test inventory, artifact/source/hash/signature and one focused physical script. No silent numeric relaxation, deleted failure cases or claimed handset acceptance from emulators. |

Actual rendered coordinates and pointer input are needed for the reported blend/header/dock reachability, not only semantics clicks or direct callbacks. Pure renderer tests remain useful independent output evidence but do not substitute for the UI route. Meaningful process-loss tests must establish a new process; Activity recreation is separately labeled.

Professional completion is broader than U1: a required operation must be findable, understandable, applied to the intended target, revisable, undoable, saved/reopened and correctly exported within measured resource limits. The 77-family roadmap remains the full obligation. U1 may not call the app Photoshop-complete or release-ready.

## Protected behavior and explicit supersession

The latest owner protects unmentioned supplied phone checks 1–5. Preserve earlier repairs and child transactions, R1.1 listing/migration/stale-selection safety, source bytes, GIF controls/encoder fidelity and all established budgets. Necessary regression checks for these authorized changes are allowed; unrelated refactors or behavior changes are not. Record an unrelated concern and obtain the owner's direction before altering protected passed behavior.

Superseded active clauses: old mandatory Resume/Start-another and Yes/No bridge; a prohibition on the fourth Regular↔Pro choice; one-way-only live transfer; reset-to-original named Delete; copied generic Pro development decks; inconsistent shared-control locations. Historical snapshots and original test receipts are retained and clearly historical. Update active tests only for those named contract changes, retaining their former safety assertions.

No runtime AI/ML, ads, subscriptions, IAP, watermark, account/cloud dependency or source overwrite. Offline $2.99 once. Four explicit owner locks precede Video; all-five editing acceptance precedes final English/Spanish onboarding; finished app and Greenezone website precede Play Console setup; publication needs explicit owner approval. B2 numeric/F03 matched-workload gaps, RV5 and other roadmap children are not closed by this document.
