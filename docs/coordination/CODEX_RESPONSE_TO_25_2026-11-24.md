# Codex Stage 2 — answers, preserved roadmap and proposed reconciliation

**Date:** November 24, 2026. **Phase:** Stage 2 complete — awaiting ChatGPT answers. **Application engineering remains paused.**

This is the requested second stage of the owner-relayed exchange. It answers Q01–Q25 from [the Stage 1 handoff](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/CODEX_ALIGNMENT_HANDOFF_2026-11-24.md). [CQ01–CQ50](CODEX_QUESTIONS_50_2026-11-24.md) are authored questions for ChatGPT, not answers or simulated agreement. The single active roadmap remains [GZ_ENGINEERING_ROADMAP.md](../GZ_ENGINEERING_ROADMAP.md). Appendices below are frozen historical evidence; the proposals in the answers require Stage 3 reconciliation and the owner's relayed resume prompt.

**Evidence boundary.** Read-only remote inspection used intake main `b9b5ff14ec5a833416b972c4b670a7e9157a2e0a`, its complete project instructions, roadmap/specification/matrix/ledger/release gates, the full Stage 1 handoff, recorded PR #31 qualification, and targeted actual production paths. The owner-supplied 95-line activation attachment was read in full (9,776 bytes, SHA-256 `e3eaeb1ae8a1622c141ff4e4d5b5ea4d5bed5983fbe3b108e3375744c198ee4b`). It supplies the exchange and prompt; no unseen other-chat content is claimed. No app source, tests, build inputs or CI were changed, no suite or APK build was run, and no local source checkout/mirror was created. Existing run receipts are credited as inherited evidence, not newly executed evidence.

References to source filenames below resolve through the pinned source index at the end of the answers. Line ranges describe that exact intake revision. New design choices are explicitly proposed defaults. Unknown device, camera, interchange and performance tiers stay unknown until resolved; no proposal is an owner-approved reduction of scope.

## Q01

**Exact custody state.** Main at intake is `b9b5ff14ec5a833416b972c4b670a7e9157a2e0a`, Stage 1 PR #34; tree `9d57397dd16ea6075db51e3c7d0dba356d810918`. Immediate pre-handoff main is `eb8796c92fa654cb0dac371e35d7645ecd07db8a` (PR #33). My last published prior checkpoint is `e04bc08a4800233b0f407b419a91c97ca2c068f6` (PR #30). I have no active unpublished application patch or source checkout to import. Earlier review/reproduction objects are historical, not a hidden alternate implementation.

Qualified application remains PR #31 merge `3ad65c533f7667a136d4fa1838afcc06d3f484da`, candidate `aa86c37c64c5e395d5378df281a0c7583a39d0e0`, identical tree `1c7e3007204192ea240a7fd8d65cbd00cf7f9511`. Recorded candidate run 45480001019 and merged run 45483297512 pass JVM/build/signing plus **193/193 on API29 and 193/193 on API35**, each with zero failures/errors/skips. Exact phone binary: 0.12.10 Test, code 22, package `com.greenezone.gzphotoeditor.phonetest.v22`, 32,899,125 bytes, SHA-256 `ed362e928db9db1f3f37c7a82c99141d64af42db1112ed8eacbdec6f466dfb80`, merged API35 artifact 14963127303. [Qualification record](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/ERASER_REPAIR_VERIFICATION.md) and [machine receipt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/qualification/PR31_0.12.10.json) retain failures and exact proof limits.

App-merge → intake-main comparison contains **12 documentation paths only**, no application/test/build/CI delta. My previous checkpoint → intake includes PR #31's Eraser geometry/gesture/serialization repairs, exact GIF cache/timing changes, related tests and version, followed by documentation. Those newer corrections are retained; I do not roll back to my older checkpoint.

There were **zero open PRs and 39 branch refs** at intake. The branch appendix gives every exact SHA and ancestry comparison: 37 refs are ancestors of main, main is identical, and `feature/pr26-review-reproductions` alone diverges (1 ahead/15 behind). That intentionally failing historical reproduction branch is not an open engineering task or a candidate for integration. Existing refs are left intact. Stage 2 creates only its own documentation branch/PR; its final exact ref is supplied with the return prompt.

## Q02

**The roadmap is preserved in full, not reconstructed.** Appendix A embeds every byte of the fetched text for the nine public plan/child files at my last authored checkpoint `e04bc08a4800233b0f407b419a91c97ca2c068f6`. Appendix B supplies the complete changed versions at the immediate pre-handoff checkpoint `eb8796c92fa654cb0dac371e35d7645ecd07db8a` and explicitly identifies identical child files by Git blob SHA. Both complete GZ roadmaps, both ledgers, matrices, specifications and release gates are included. Shared full-source child appendices cover AGENTS, PROFESSIONAL_ENGINEERING_ROADMAP, UNIFIED_PRODUCT_SCOPE_09 and PRO_GIF_REBUILD_011 without redundant copies. Pinned originals and exact blob identities accompany each section.

That is the actual existing plan: R0–R11, its enumerated child behavior, dependency/exit rules, all 77 families, R1.1/R1.2 selection, held items and release sequencing. It did **not** contain a prewritten implementation ticket for every future control, fixed estimates or an undisclosed finer backlog. Future task decomposition in Q14–Q25 is a proposal, not retroactively labeled existing work. Historical blueprints are provenance; their obsolete Regular Masks/Pro Text/menu directions yield to later owner requirements. The immediate pre-handoff snapshot also records PR #31 work performed in the intervening repository history; I do not claim to have personally performed that separate chat's execution.

The current Stage 1 roadmap at intake adds the explicit A1 pause and F01–F06 priorities. Compare these frozen sources with Q03's discrepancy table and Q25's proposed sequence. Do not promote either appendix into a competing active roadmap.

## Q03

**All 77 families remain mapped.** The complete reconciliation table after Q25 carries each ID, capability, current status, package, full existing exit, source and proposed disposition. No family is deleted, renumbered, marked complete by association or hidden behind a percentage. Cross-cutting families intentionally share dependency owners: for example S01 source identity supports C01 tiles and S06 interchange; D03 bounded Draw evidence does not complete R14 creativity.

| Discrepancy or overclaim risk | Exact interpretation and proposed Stage 3 action |
| --- | --- |
| Older “next R1.1” statements in R0, qualification receipt and pre-Stage1 delivery handoff | Preserve as dated evidence; active next action is A1. Reconcile current pointers to agreed owner-correction sequence, then R1.1/R1.2. Never act on the old APK-delivery instruction. |
| R8 sentence allowing independent work while phone observations are pending | Valid only after current alignment/resume gate. Add that condition so it cannot bypass A1. |
| R14 says owner touch acceptance remains without crediting the latest scoped pass | Credit unmentioned supplied Eraser/tile checks as owner-reported pass; retain broader family/device/transform/decorative work. Do not repeat 160-stroke manual setup. |
| Historical “owner acceptance false” and older handset-open wording | Qualification-time statements remain historically true. Latest owner report supersedes only supplied unmentioned checks; formal workspace locks and commercial acceptance stay open. Clarify date/scope rather than rewriting historical raw receipts. |
| Old More/Create collage/Histogram layout versus F04–F06 | Owner supersedes those UI placements. Remove all three requested Regular surfaces, including empty-state Create collage; preserve necessary algorithms and R03 ID. Common header change is authorized; unrelated reskin is not. |
| Collage double-tap versus required one tap | Actual parent handler toggles only in onDoubleTap. Fix named interaction and selected-tile arbitration after Stage 3; preserve handles and one-history-per-edit. |
| Retained sessions or export journal described as draft recovery | They do not establish completed-edit durable autosave or real OS process-loss recovery. S03 stays OPEN. |
| Small GIF fixture versus 120–300-second phone Save | Both records stand. F03 remains open; obtain workload and measure stages, not a timeout increase or fidelity reduction. |
| Existing flat Pro layers/soft repair versus full professional tools | P02/P03/P11–P16 remain OPEN, partial tools remain PARTIAL. No groups/tiled compositor/true healing/segmentation inferred from helper names. |
| Existing ARGB8888/sRGB and RGB8 TIFF versus RAW/high-bit claims | R04–R07 remain OPEN; references/variant tables and actual output qualification precede claims. |
| 648 sticker concepts versus finished catalog | R16/R17 remain OPEN until ≥500 finished usable assets and complete authoring workflow qualify. |
| Existing GIF compatibility versus complete animation assembly and v4 framing | G05 LIMITATION retained, G06 PARTIAL/G07 OPEN; old source reinspection is a reversible upgrade, not guessed aspect correction. |
| Collage code accepts 1–20 while some help says 2–20 | Preserve actual owner contract 1–20 and all seven layouts; reconcile help when that UI is touched. |
| No Console yet versus preemptive account setup/testing | L04/L07 stay gated until finished app AND owner-confirmed website; account-specific current rules are checked then. |
| Family-level plan granularity versus “AAA complete” | Observable child workflows and explicit device/format tiers are required. Missing measurements are queued evidence, not hidden scope removal. |

These are concrete reconciliation items, not a request to restart the earlier audits. Proposed status changes here are wording/evidence-credit corrections; no entire feature family gains completion during documentation work.

## Q04

**Observed command inventory and proposed destination.** These are real current callbacks/surfaces, not an invented idealized app. Source anchors: PhotoEditorScreen, EditorDock, CollageWorkspaceScreen, ProWorkspaceScreen, GifMakerScreen, VideoGifScreen and GifProjectDialogs in the pinned index.

| Workspace/state | Before: actual major command owners and locations | After: proposed single authoritative destination |
| --- | --- | --- |
| Regular, loaded | Header Export/Open media/mode controls and EditorDock MoreDeck (578–621): Help, Auto tone, Auto WB, Save my look, Save project, Open projects, New photo, Create collage, Reset all edits, Histogram. Save my look also in LooksDeck (253–255). Host owns pick/open/save/export/reset callbacks. | Common header Options owns media/project/export/help. Auto tone moves from More to Light, Auto WB moves from More to Color, Save my look in Looks once. Reset all edits moves to Options as an explicitly destructive edit reset. Delete More/Create collage/Histogram UI. Direct tools/history remain direct. |
| Regular, empty | Open media and Create collage buttons (PhotoEditorScreen 1091–1127); project opening absent because dock requires original. | Options remains visible with Open media/Open project/Help. Empty canvas gives guidance, no duplicate action buttons. No Create collage shortcut; workspace selector is the route. |
| Selected Collage tile | Shared Regular dock with named Save/Open and More; Histogram/Create collage already hidden; Done returns a child edit to parent. | Child transaction and explicitly labeled command ownership. Existing independent photo Save/Open is intentional and stays available pending CQ04 resolution; it does not Apply to parent. Optional parent Save/Export would be a new proposal requiring child resolution and Stage 3 agreement, not a currently implemented command. Direct Done/Cancel remain. |
| Collage, parent | Empty Add photos (553–567); header Export/empty Projects (623–637), +Add (655–658); Tiles panel +Add, Save, Saved collages, New (1032–1044); Help says double tap. | Options owns Add photos/Open project/Save/Save as/Export/New collage/Help once. Layout/edit-tile controls stay contextual. One-tap behavior and help agree. |
| Pro, loaded/empty | File menu (897–928) Open/Save/Export/Fit/Help/Delete; Add layer direct; textual Undo/Redo and separate workspace selector. saveProject (435–465), openProject (468–489), export (577–631/690–720); empty Add first photo/drawing entry (944 onward) overlaps contextual layer creation. | Options replaces File and owns New/Open project/Save/Save as/Export/Help/reset-current-work. Add layer and Fit remain contextual direct tools once; reconcile empty layer-creation guidance with that single contextual owner rather than duplicating major commands. Common history arrows and workspace selector. |
| GIF video/existing GIF | Header Back/Help/File/workspace; File Choose video/Edit existing GIF/Save project/Open saved. Empty buttons duplicate open; active Save GIF calls startExport, not native save. | Options owns Open video/Open GIF/photo-sequence entry/Open project/Save/Save as/Export GIF/Help. Keep Play, Reverse, view/tool toggles direct. Do not hide menu on empty state. |
| GIF photo sequence | Header Help/Video→GIF; action row Play, Undo, Redo, +Photos, Open saved, Export; empty Add photos duplicate; Projects dock repeats open/save/New GIF. No workspace selector in current route. | Same common header and one route-aware Options list; remove duplicate action/project buckets. Per-frame reorder/remove/duration/canvas editing remains contextual. Fresh photo creation remains separately qualified R7 work. |
| Saved project chooser, all | Per-project delete/replace operations belong to saved-item identity; store naming semantics vary. | Keep contextual row-specific delete with confirmation in project chooser. It is not a second top-level Open/Save/Export entry or global “Delete everything.” |

This inventory includes command names and controlling modules so migration can remove duplicates rather than simply adding Options. Save project stores the editable document; Export writes a rendered deliverable. They never substitute for one another.

## Q05

**Proposed shared header, small adapters.** Introduce a presentation component such as WorkspaceHeader plus a typed command/availability model; keep document ownership, picker launchers, histories and renderers in their existing workspace controllers. A shared departure coordinator can represent a pending intent/result without moving the entire app into a new shell. These names are proposals, not files that already exist.

Common order: Back/child completion context on the left; workspace label/selector; Undo and Redo paired arrow icons with accessible names and disabled state; visible text **Options** at top right. A narrow layout may use a deliberate second row, not an offscreen horizontal file toolbar. Retain dark image-first styling, spacing tokens, clear selected tool and consistent progress. Pro/GIF currently use text history controls; those change to the same convention. View-only taps, viewport changes and menu opening never create edit history.

Validate 320dp-class narrow width, typical phone portrait/landscape, split window, large fonts (proposed through 200%), long labels, screen reader focus order, switch/keyboard access and reachable touch targets (proposed at least 48dp). These are design acceptance targets, not measurements of the current app. Options must remain discoverable on an empty workspace; disabled entries explain why through accessible state/help without relying on color. No overlap with system insets/keyboard; selection/crop handles remain independent of chrome visibility. Stage 3 resolves whether the header remains pinned while tool decks hide (recommended for discoverability), with a clear one-tap content target for hide/show.

## Q06

**Proposed common major-command order:** New document where meaningful; Open media… / Add media… with workspace-specific label; Open project…; separator; Save project; Save project as…; Export image… / Export GIF…; separator; Reset current edits… where applicable; Help. Add no inert command for an unimplemented function. Stage 3 must decide route-specific wording and selected-tile ownership; this is not a final approved menu spec.

| State | Availability / result |
| --- | --- |
| Empty | Options and workspace selector remain visible; Open media, Open project and Help work. New can initialize a supported empty document. Save/export require a valid editable document, with meaningful explanation when disabled. |
| Dirty valid document | Save or Save as persists the exact current revision. Open/New/switch/Back enter the common departure contract. Export captures that revision and leaves native-project dirty state unchanged. |
| Clean named document | Save can be disabled as already saved; Save as remains available. Open/New/switch do not prompt unnecessarily. |
| Missing media | Open project metadata and relink remain available; do not discard recipe or pretend full export is valid. Save preserves recoverable links/recipe if format permits, with explicit status. |
| Gesture/tool draft | Resolve one transaction before a major command. No partially sampled stroke or uncommitted lasso enters save/export. |
| Busy encode/copy or recovery pending | Guard conflicting mutation and duplicate jobs; expose truthful progress and allowed cancel/retry. Do not orphan staged files or captured job identity through navigation. |
| Selected tile | Preserve existing independent photo Save/Open with explicit ownership until CQ04 is resolved. If Stage 3 adds parent Save/Export here, resolve child first and label the parent operation clearly. Never change another tile or equate standalone photo Save with Apply. |

One callback owner per command: workspace controller → validation → common departure/save/export adapter → existing store/picker/job. UI only dispatches typed intent. Save result must distinguish Saved, canceled naming, collision, too large and failed; only Saved can trigger a pending departure. Existing Regular/Collage name-only flows need stable current-save identity semantics; GIF already tracks saved name and replacement, Pro tracks saveTarget. Save as is an intentional extension needing contract/tests, not an existing universal behavior.

Remove header File, direct major Help/Export duplicates, Regular More, GIF Projects bucket and redundant empty-state file buttons when their single Options counterparts exist. Do not remove Play/Pause, Add layer, per-frame edits, Undo/Redo, tool Apply/Done/Cancel or active object controls. Options should not become a crowded second editing dock.

## Q07

**Remove the whole Regular More surface and all Regular Create collage/Histogram entry points.** This includes the empty-screen Create collage button and any callback entry still reachable through shared Regular menus. Do not relocate those two commands to Options, Help shortcuts or a new “advanced” Regular menu. Collage stays a separate workspace. Keep PhotoHistogram/numerical tests if used by diagnostics or rendering validation; R03 survives with its revised processing/diagnostic exit, without implying visible histogram authoring.

Explicit survivor mapping: Help, New/Open, Open projects, Save project → Options; Auto tone → Light; Auto WB → Color; Save my look → its existing Looks context once; Reset all edits → clearly named Options reset with confirmation and undo policy; ordinary adjustments, Looks, Text, Draw, Shapes, Blur & cover and crop remain their contexts. No new Regular Masks authoring. Shared selected-tile dock inherits the explicitly requested removals. Existing standalone photo Save/Open remains distinct from parent Apply; any parent persistence commands or removal of standalone commands await CQ04's explicit Stage 3 decision. Do not delete legacy model fields/enums/render support simply because a menu is removed.

Acceptance: enumerate accessibility-visible actions in empty/loaded/selected-tile states; exactly one major-command home, absence of the prohibited Regular entries, every surviving command reachable and functional, correct native saves and independently decoded exports. No screenshot-only claim.

## Q08

**Parent Collage cause is directly demonstrated by source:** CollageWorkspaceScreen 576–590 attaches chrome toggle to onDoubleTap, while onTap only selects a tile. Help at 949 instructs double tap. This explains the reported need for rapid double tap without inventing device latency. Selected-tile Regular editor is composed instead of the parent canvas (CollageWorkspaceScreen 332–347), so the parent recognizer is not active underneath it. PhotoEditorScreen 961–989 already uses single-tap plus transform recognition. Brush, privacy, crop, text/shape and draw overlays arbitrate their own pointers. Preview replacement keyed by selectedImage, tool-specific hit classification and consumed/canceled taps are hypotheses for the child report, not confirmed causes.

Proposed pointer rule: a completed, unconsumed deliberate tap on the canvas toggles controls once in either direction; active handle, paint/erase, crop, drag or pinch owns its pointer sequence and never also toggles chrome. Resolve whether tapping an unselected tile only selects first or selects and toggles in one action in Stage 3 (CQ05); make the rule consistent and discoverable. Do not hide guides/handles when tool chrome hides. Never call edit-history commit for pure visibility/selection UI.

Discriminating cases: slow single tap and repeated single taps; quick double tap must not be necessary; visible→hidden and hidden→visible; selected/unselected tile hit; empty gap/background; active tile crop/text/paint handle; 1-pointer move past slop; second pointer/pinch; canceled pointer stream; device Back and layout recomposition during a gesture. Run parent and selected-tile paths separately. Verify selected ID, chrome state, document/history equality for view actions and exactly one completed edit for actual tool gestures. The fix should remove the parent double-tap dependency and adjust only demonstrated child conflicts.

## Q09

**Common departure matrix proposed for Stage 3.** Dirty means current editable content differs from a tracked saved/entry baseline, not “history has undo,” “has a filename,” “was exported,” or “session is retained.” Preserve native documents and original media separately from route state.

| Route/context | Observed gap / existing behavior | Required proposed result |
| --- | --- | --- |
| Regular system/app Back | requestRegularBack 488–495 pops prior workspace before isPhotoDirty check; session navigation can bypass protection. | Transient priority first, then clean one-pop or dirty Save/Discard/Cancel for the captured destination. |
| Regular workspace change | chooseWorkspace 342–359 bypasses dirty test. Regular→Pro has existing Yes/No appearance handoff. | One integrated decision sequence; retain Regular draft/history for either handoff choice. Avoid two contradictory prompts or treating No as “discard Regular.” |
| Regular New/Open or another named project | requestPhotoReplacement 410–435 already checks isPhotoDirty and offers Save/Discard/Keep editing; physical Back without a prior workspace also prompts. The failure is incomplete route coverage, not total absence of guards. | Guard every replacement, preserve current work on picker/naming cancel or failed save; only commit successfully loaded chosen target. |
| Pro Back/workspace switch | 371–399 finish/persist session and leave; draft/busy guards exist, no saved-content dirty comparison. | Resolve selection/clone draft first, then departure decision. Keep native parent/history and correct route. |
| Pro Open project/New/current reset | openProject 468–489 directly commits loaded project; reset402–431 resets to original and clears history. | Replacement guarded; Discard must not blindly reuse “Delete current work.” Define last saved/entry baseline and scope. |
| Collage parent Back/mode | 283–302 finish layout and retain session then depart; no dirty comparison. | Guard dirty parent, including committed child edits; no tile loss or duplicate history. |
| Collage Open/New | Open-other-project already compares savedDocument; New729–753 has warning/Start new/Cancel but no Save path. | Same Save/Discard/Cancel contract; retain target identity across name dialog/failure. |
| Collage child Done/Cancel/Back/mode | Back already compares immutable child entry baseline and offers Apply/Discard/Keep editing (1513–1537); Done commits once. Parent callback currently captures an index (332–344), safe within exclusive child route but not a general stable-ID guard. Shared named photo saves do not Apply to parent. | Done commits once to same tile; Cancel restores child entry state only. Back/mode after child changes offers explicit apply/discard/cancel child resolution, then parent departure only if actually leaving parent. No sibling changes. |
| GIF Back/mode/New/Open | Video283–307/photo264–275 keep sessions; source/project replacements lack saved-content dirty guard. | Inspect/qualify common contract. This is source-supported design work, not an owner-reported GIF exit failure. |
| External picker/menu/IME/dialog | Existing guards and platform surfaces vary; source inspection alone does not prove physical popup/IME precedence. | Picker/menu/keyboard/dialog/tool draft consumes Back first. Canceled predictive Back commits no route/edit change; rapid double Back cannot cross two guarded contexts from one action. |
| Clean document / no document | No user work to protect. | Leave/replace once, without redundant dirty prompt. |
| Dirty but invalid/unsavable | ProMath.valid requires 1–12 layers; empty modified canvas cannot use present Save. | Define empty-setting state or extend valid durable representation; never present an impossible mandatory Save. Keep Cancel and safe scoped Discard. |

**Save:** persist captured current revision atomically, update clean baseline only for that saved revision, then execute one still-valid pending route. **Discard:** abandon only intended unsaved working changes relative to agreed baseline; never delete named projects, source files or sibling documents. **Cancel:** keep document, history and route. Failed Save keeps work and the still-open decision available for retry/cancel, never navigates. Proposed cancellation rule: canceling naming or explicitly canceling Save clears the pending departure token and leaves the current route/work intact; a later unrelated Save cannot execute the abandoned route. Export does not satisfy Save.

For Regular→Pro, default proposal is a dedicated handoff continuation that captures edited appearance before any route resolution and explicitly retains Regular native work. Integrate any required save choice into that flow; Yes imports captured appearance, No opens empty Pro, neither loses Regular. Final wording and whether retained-draft mode switches still prompt are unresolved design decisions CQ08–CQ09, not permission to omit the reported safeguard.

## Q10

Use one immutable pending intent: route, source workspace/document ID, child stable ID if any, content revision, requested target and unique execution token. While it is resolving, deduplicate taps; do not substitute a later list position or mutable selected object. If the document changes during an async save/open, either keep navigation pending for the newer dirty revision or ask again under a specified rule—never falsely mark the newer state saved.

Gesture states are provisional until completion. One Draw/Erase/layout slider transaction produces one history checkpoint; canceled gesture restores full pre-gesture document and history, preserving PR #29/#31 bounds and refusal behavior. Selection draft is separate from content change; applying a mask is an edit. Resolve active transactions before Save/Export/route change rather than serializing in-flight points. Visibility, selection highlight, fit/zoom and tool switching must not invent content history.

Async result acceptance checks owner identity and revision; parent tile reorder must resolve by tile ID, never old index or source URI (duplicate sources are legal). Save/export requests freeze recipe/source identity; unrelated route result cannot mutate another document. Picker cancel is not discard. Repeated Save, Export, Done and Back are idempotent within an operation token. A route completion invalidates that token.

Validation must use actual UI and results: rapid double Save/Back, failed save then retry/cancel, picker after rotation/real process loss, tile reorder while child/async task is pending, canceled pointer streams, late loader results and edits made during save. Assert route count, stable target ID, untouched healthy saved bytes/original hashes, full document/history, and decoded export pixels. Text/shape and privacy-region onDragCancel currently finish rather than roll back (CreativeCanvasOverlays 186–187; PrivacyRegionOverlay 56–57). Align this specifically with the agreed cancellation contract; it is not proof of F01's cause. Existing Activity recreation evidence remains credited but cannot stand in for killed-process recovery. This cross-cutting contract can be added incrementally to existing owners; it does not justify a shell rewrite.

## Q11

**Known:** owner reports 120–300 seconds for GIF Save on the supplied phone build while menus/options work. **Unknown:** phone/Android, exact source codec/duration/resolution/frame timestamps, output size/FPS/frame count, trim/speed/reverse/recipe, destination provider, heat and whether elapsed time includes picker interaction. It is not yet possible to assign the delay to a stage.

Observed video/GIF path: capture immutable recipe and journal → serialize decoder ownership → decode scheduled frames → geometry/grade/detail/masks/privacy/creative render → palette → LZW/local stage → close/nonempty-check/rename → destination picker → protected provider copy/close → visible completion. Untouched imported GIF has byte-copy passthrough; photo sequence asks destination before preparing its private stage, but opens the truncating stream only after encoding. Completed staging is reused on retry. Production stage “validation” currently means nonempty plus rename, **not full decode validation**.

Existing GzGifEncodeStage counters measure overlapping load/render/palette/LZW-output wall and thread CPU; GzGifExportStage measures reader open/read/progress/close. G6 start/staged/completed events include picker dwell in staged→complete. These are not additive disjoint totals and do not fully isolate mutex waiting, seek/fallback, stage flush/rename, provider open/write/close or UI completion. Passthrough bypasses encoder logs; thread CPU excludes external codec processes. Source: GifExportRecovery 129–247/315–377; VideoGifSource 433–591; GifEncoder 197–250; StagedImageExport 25–44.

Minimum useful owner follow-up after alignment: phone/Android; the problematic source or privately retained reproducible equivalent; selected output size/FPS and trim/reverse/grade settings; destination app; distinguish tap→picker from confirm→finished file; cold or already warm. Inspect metadata ourselves from supplied media rather than asking Lance to decode codecs manually. One focused observation/settings capture is enough to begin; no repeat of successful unrelated phone checks.

## Q12

**Optimize only a measured bottleneck at the same requested output.** Preserve all frame/timing/geometry/edits/alpha/source behavior and the existing 6-second sampled-motion/15-second Save fixture gates.

| Hypothesis | Discriminator / finite experiment after resume | Permitted direction and stop boundary |
| --- | --- | --- |
| Larger pixel/frame workload | Record scheduled frame count × output pixels and per-stage cost; compare one short-large and one long-small case. | Optimize measured work; never quietly shrink output, lower FPS, drop frames or disable edits. Larger workload alone does not close F03. |
| Long-GOP/reverse seek or fallback | Record actual codec/retriever route, seeks/fallbacks, lock wait and decode wall/CPU. | Reuse exact decoded pictures/bounded GOP windows; preserve timestamps/rotation/SAR and fallback. No concurrent use of a single decoder owner. |
| Grade/detail/cache cost | Render wall/CPU and cache hits/misses on actual recipe; compare exact uncached oracle. | Retain (sourceARGB, detail.rawBits) cache key and position-effect exclusions; avoid repeated computation/allocation only with pixel equality. |
| Palette or LZW cost | Isolate palette CPU, LZW CPU and local write/flush; photographic/noisy versus repeated-color case. | Equivalent deterministic implementation or scratch reuse. New quantizer/dither is an explicit R7 feature, not a silent speed tradeoff. |
| Provider bottleneck | Same completed staged bytes copied through local Files and implicated provider; separate open/write/close/UI and picker dwell. | Preserve stage for retry, truthful progress and uncertainty handling; do not rerender on destination retry. |
| Contention/thermal/memory | Bounded cold/warm repeats with peak heap, CPU/wall gap, GC and heat observation. | Remove demonstrated redundant work first. Any pipeline overlap needs bounded queue, aggregate budget, ordering and cancellation proof. No unbounded threads/full-animation retention. |

Existing budgets: output ≤16,777,216 pixels; ≤150,000 frames and source ceiling 24 hours; reverse existing-GIF cache 4 MiB/up to 8 frames; retriever cache 24 MiB/up to 12 including resize reserve; edited reverse preview compressed cache 16 MiB/96 pictures plus working planes; per-export exact color cache 16,384 entries/about 208 KiB; stage quota max2GiB with 64 MiB free-space reserve. These separate caps do not prove aggregate safe memory. Measure total ownership and fallback peaks before changing them.

Each investigation has one hypothesis, one falsifier, one bounded comparison and a recorded disposition. If disproved, stop that approach and use the measured next stage; do not repeat unchanged suite runs until green. No claim that PR #31's cache explains the entire prior failure or today's phone report.

## Q13

**Qualification is workload-specific.** Inherited merged fixture: source 1080×1920, output 180×320,88 frames,2.950s, Save3.872s/API29 and 3.334s/API35. It checks every delay plus decoded endpoint markers/reopen/replay. Five sampled pictures include the initial paused still; they do not establish full cadence or input-to-correct-still latency. Keep existing rotated720p60 and portrait gates unchanged.

| Workload lane | Device/state and purpose | Required observations |
| --- | --- | --- |
| Existing portrait/rotated fixtures | API29/API35; unchanged cases | Original timing/fidelity assertions, not widened waits as acceptance. |
| Exact owner workload | Owner phone, cold then warm; proposed fixed small repeat set after fix | Separate encode/provider/picker intervals, finished-file reopening, responsiveness and heat; report samples honestly, no p95 from two runs. |
| Short-large / long-small | Declared physical resource tiers | Distinguish pixels/frame from frame count, peak aggregate memory and cancellation. |
| Forward/reverse long-GOP/VFR; detail/noisy/coordinate-effect grades | Representative codec and cache behavior | Correct source schedule, first still/motion, sustained presented cadence and stage CPU/wall. |
| GIF passthrough/edited transparency/disposal/finite loops/holds; photo sequence | Independent decoder plus physical player | Byte-identical passthrough; full frame/delay/disposal/loop/alpha and external playback. |
| Local and implicated provider; low storage/interruption | Actual provider/device | Open/write/close, stage retention, ambiguous result, recovery and source preservation. |

Record source-open; edit-to-first-correct-paused-picture; first moving picture; sustained distinct presented cadence; private encode/stage-close; human picker dwell; provider copy/close; visible success; independently readable finished output. Keep those metrics separate. Include exact recipe/build/source fingerprint/output bytes/peak memory, not user media in telemetry. No runtime telemetry is introduced.

Do not invent a universal phone-seconds promise. Stage 3 approves representative workloads/tier targets from measured baseline, including an absolute completion budget, relative improvement, responsiveness and fidelity. Unknown owner workload blocks only owner-specific reproduction/closure; common UI and later independent agreed work can proceed after resume.

## Q14

**R1.1 is a real remaining memory/custody task.** ProjectStore 15–31 reads SharedPreferences.all and eagerly decodes every Regular recipe. PhotoEditorScreen calls it at startup227, open687–691, save1370 and delete1423. Twelve near-budget2,000,000-character recipes can therefore produce many document graphs. Merely returning names from SharedPreferences does not prove bounded raw-payload memory because preferences may already hold the payloads.

CollageStore.names111–120 and ProStore.names302–314 already list filenames without decoding recipes; selected loads must still move off UI and retain identity. Collage UI834–835 currently invokes its selected load synchronously. Do not rewrite every store as though all have Regular's eager bug. GIF's named identity/quota protections remain credited.

Proposed finite tasks: define lightweight stable-ID metadata/index; read metadata for list; decode only selected item on IO with cancel/stale-result check; expose corrupt/unavailable entries rather than silently dropping them; migrate Regular storage by copy→validate→atomic publish while retaining healthy legacy bytes until verified. Bounded migration itself needs measurements; it must not multiply all recipes in memory. Preserve named identities, ordering, schema limits, existing save counts and replacement safeguards.

Exit:12 near-budget saved items plus corrupt/truncated/unsupported/failed-write entries; instrument decoded-document count and heap/list latency; only chosen recipe decoded; rapid choose/delete/sort cannot open the wrong item; failed migration/save leaves every healthy byte and dirty draft intact. Include cold reopen and old-format fixtures. Existing Pro/Collage name listing needs targeted selected-load and failure coverage, not unnecessary redesign. This is queued work, not implemented by Stage 2.

## Q15

**R1.2 follows bounded listing, with a narrow save/dirty identity prerequisite allowed in the correction wave.** Regular working document/source/history/navigation are in remember state; Collage sessions are host-retained. PendingExportJournal is job custody, not completed-edit autosave. Pro durable recovery stores document/reset baseline but not full session/history/navigation. Activity recreation is not proof of a new process.

Proposed durable record: stable document/session/source identity and grant status; schema plus render version; immutable committed recipe revision; clean/named baseline; bounded undo/redo; selected object/tool/viewport/playhead and route where meaningful; parent/child relationship. Use atomic checkpoint generations with readback/last-healthy fallback. Only completed transactions are published; unfinished pointer streams and child Apply remain provisional. One writer serializes revisions and stale acknowledgements cannot mark newer work durable.

Forty Regular snapshots or35 Collage snapshots multiplied by multi-megabyte recipes is unacceptable by construction. Decide a bounded deduplicated journal/checkpoint design and visible recovery guarantee (CQ13); do not silently drop promised history or put media/history into saved-instance Bundle. “Durable” is acknowledged successful publication; a crash during publication restores old or new complete state, never a mixture. Backgrounding should flush the latest committed revision within the agreed lifecycle contract, with storage failures visible.

Exit uses an external process-loss controller, confirmed new PID and no retained old objects: after acknowledged save, during checkpoint, mid-gesture, missing/revoked source, picker open and export-copy recovery. Verify recipe/history/route/selection, healthy originals/named saves and old/new generation integrity. Prove Regular first, then extend the same custody contract to Pro/Collage/GIF without assuming one workspace's test proves all. Real device/OEM observation remains separately scoped.

## Q16

**Keep existing custody strengths and complete their missing boundaries.** Regular/Collage/Pro exports capture immutable recipes before picker; IDs/phases reject stale callbacks; journal publication precedes external work; staged output precedes truncating destination writes; copying-phase ambiguity is recoverable. Exact source-URI comparisons exist, but URI equality is not complete alias detection. Current saves store references/recipes, not portable source bytes or immutable source revisions.

Proposed sequence: separate file-schema migration from render-semantics version; validate declared bounds before allocation; preserve legacy masks/text/grades; reject unsupported essential records recoverably; copy/validate/publish migrations atomically with retained source and old healthy generation. Relink restores a missing reference while retaining recipe; Replace intentionally changes content and has separate undo/dirty semantics. Preserve missing-media documents instead of empty-success replacement.

Source identity must handle persistent grant loss, URI aliases, changed bytes at same URI, and duplicate instances. Favor app-owned immutable revisions or verified readable source fingerprints where needed; disclose what cannot be verified. Write-only providers need conservative ownership and explicit destination selection, never a claim of readback proof. Cancellation before opening destination is safe; once truncating copy begins, current GIF path completes the bounded copy. Failure/unknown close remains uncertain and retries to a new selected target without deleting an arbitrary provider document.

Portable native bundles remain required S07 work: declared linked-only versus embedded permitted sources, checksums, size limits, safe extraction, asset/font rights and missing-source manifest; second-device import and relink. Saved repeated R→P→R→P uses linked immutable masters with both native parents, explicit appearance/render version and alpha/profile/precision preserved when supported. Current Yes/No live transfer is useful but not full saved round-trip or RAW/profile fidelity proof. Same-ID upgrade tests are mandatory later; separate phonetest package IDs do not prove migration.

## Q17

**R2 completes the Pro foundation without rewriting the whole app.** Current ProModel has a flat List<ProLayer>,16 blends, image/legacy text/shape/drawing/adjustment kinds, visibility/opacity/rotation/uniform scale, one mask and retouch marks. Limits are12 layers,35 history snapshots and 4096 export edge. ProRaster211–301 composes a whole ARGB8888 bitmap, not tiles; an8 MiB prepared-source cache is not a tiled renderer. Source presence does not qualify higher capacity.

Remaining child work: stable layer/source IDs and tree; group/ungroup, isolated/pass-through, clipping, names/content/transform locks/multiselect/align/distribute/order/group masks; explicit source/layer/canvas/viewport transforms; declared alpha/blend working space/effect order; versioned legacy-render adapter; deterministic CPU tiled/reference rendering with halos/cache keys/eviction/proxies/cancel/resource teardown; measured GPU parity only if justified; canvas resize distinct from resampling/crop/background/alpha.

Proposed default: new isolated groups, opacity/mask applied once after group composition; pass-through explicitly chosen; clipping bound to stable base IDs. Legacy flat pixels remain unchanged. A tiny independent CPU reference defines blend/alpha/group cases; tile renderer must match overlap/halo/seam/fractional-alpha fixtures and final output, including cancellation/supersession. Qualify selected layer through reorder→Undo→immediate paint, nested clipping/locks, save/process-restart/reopen and export. Raise limits only after named device/operation/effect worst-case memory and timing evidence; retain current caps until then. First supported tier is not permission to remove the larger professional scope.

## Q18

**R3 retains all listed Pro tool families.** Current rectangle/ellipse/freehand selection with feather/invert and an image-pose anchor is a foundation. It has 450 lasso points and 80 retouch marks. Current “Soft repair” is immutable donor circular copying with 0.57 opacity multiplier, not healing/patch/inpaint. Basic shapes and paint points are not paths/pressure dynamics.

Required tasks: polygon/color/luminance contiguous/global selection; add/subtract/intersect/grow/shrink/invert/feather/saved channels; linked/unlinked layer/group/filter masks and grayscale mask painting with explicit active target; nonuniform scale/skew/perspective/pivots/numeric entry/guides/rulers/snapping; cubic paths, joins/caps/dashes/booleans/gradients/fills; brush flow/hardness/spacing/pressure/qualified tilt/finger fallback/stabilization/rights-cleared textures/deterministic replay and alpha eraser; fixed/aligned clone with current/current-and-below/visible donors; true conventional spot/heal/patch/inpaint, dodge/burn/blur/sharpen/grain and editable donor revisions; ordered effects/styles/scopes/shadow/stroke/bevel/displacement/blend conditions.

Native fidelity precedes a named OpenRaster/PSD subset and reusable actions. Declare supported records and losses before import/export, preserve unsupported originals safely, and fail actions atomically on unmet targets. No universal PSD claim. Preserve legacy Pro TEXT and grades/rendering without adding Text authoring or the Regular development/sticker dock.

Every child gets correct-target live edit, one history transaction, cancel, reopen and independently rendered output, including mask-vs-content and donor-history tests. R2 stable identities/render semantics are prerequisites for tools that depend on them; independent UI ergonomics need not await every later renderer optimization.

## Q19

**R4 remains wholly open for professional Subject & Background.** Deliver one understandable Pro family: manual/assisted tracing and keep/remove marks with conventional bounded segmentation and manual fallback; editable extraction alpha; image/color/gradient background replacement and linked subject transforms; hard/soft edge refinement, feather/choke/expand/fringe cleanup and restore; small-object removal using conventional inpaint/clone/patch/seamless blend; reversible mesh/liquify push/pull/expand/shrink/freeze/thaw, chosen face/body reshaping and material retexture/reskin with editable masks/transforms.

Proposed algorithm evaluation is finite: select candidate conventional method against a named corpus and resource bound before dependency integration; review exact license/transitives/distribution obligations. No runtime model/download/inference. Keep original source and operations inspectable, canceled work must release scratch memory and restore prior state.

Acceptance includes hard edges/holes, hair/fur, translucent glass, similar-color foreground/background and color fringe against light/dark new backgrounds. Independently decode actual alpha; preserve outside-region pixels during removal; protected straight lines stay fixed during liquify; texture follows chosen deformation/shading. Difficult cases have explicit manual correction and honest limits, not a promise to infer unseen scenery. CQ25–CQ26 resolve measurable default semantics; no family is closed by simple clone or a menu placeholder.

## Q20

**R5 requires a real photographic pipeline.** Current ImagePipeline71–89 decodes software sRGB and uses ARGB8888; ImageExportWriter119–163 writes8-bit RGB TIFF flattened through rgbOpaque. Seven encoders do not establish RAW, ICC or16-bit output.

Complete existing tone/WB/HSL/curves/grading/Looks/presets/neutral/before-after behavior and numerical references. Preserve necessary histogram/clipping diagnostics while removing Regular Histogram UI and keeping no Masks authoring. Define sample encoding, scene-linear/encoded domains, working profiles/alpha and legacy appearance adapter before new precision claims.

DNG first with an explicit mosaic/linear/compression/opcode variant table: black/white/linearization/CFA/demosaic/WB/calibration/camera profiles/scene-linear exposure and highlight policy. Embedded JPEG is labeled preview. Broader camera RAW follows licensed samples/profile and decoder review, not automatic LibRaw integration. Exact camera tiers remain unresolved product coverage, not silently excluded.

Then assign-vs-convert ICC, sRGB/qualified wide-gamut SDR, proof/gamut warnings/numeric samples/output intent and verified16-bit PNG/TIFF; HDR is a separately qualified variant. Finish lens distortion/CA/vignette/upright/perspective/straighten, conventional luminance/chroma denoise/sharpen/output sharpening. HDR bracket merge, panorama and focus stack follow single-image geometry/color, retaining sources/alignment/ghost/seam/focus decisions.

Exits: independent synthetic mosaics/ramps/charts/optical grids and rights-cleared camera negatives; neutral and legacy golden images; actual output sample precision/profile tags, clipping/alpha and source hashes. Unsupported essential metadata is a precise recoverable limitation. A first DNG subset does not complete broader R06.

## Q21

**R6 is full local organization, durable batch and creativity.** Catalog includes folders versus collections, ratings/flags/favorites/labels, tags/EXIF/IPTC/XMP, rules/search/smart collections, byte-confirmed duplicate candidates, stacks and virtual copies. Sidecars/backup/restore/relink preserve source/copy identity; settings sync selects compatible photographic edits and explicitly excludes inappropriate source-anchored crop/retouch.

Batch freezes item source/revision/recipe/destination and supports multiple outputs/naming/profile/metadata/privacy, progress/receipts/cancel and idempotent recovery. Provider ambiguity cannot duplicate completed work silently. Test100-file catalog/recovery and interrupted multi-output jobs with source preservation and truthful per-item results.

Complete Unicode shaping/fallback/fonts/styles and independent text/draw/shapes/borders/frames/decorations through crop/history/reopen/output. Current CreativeDocument contains text/strokes/shapes; nine Android font-family aliases are not full licensed-font qualification.

Stickers require **at least500 finished usable rights-cleared assets**, actual manifest/provenance/hash/thumbnail/size and visual/output inspection. The648 concepts/27 categories are planning only. Build category/search/favorites/recents and custom transparent PNG/WebP; move/scale/rotate/flip/opacity/order/duplicate/delete; independent repeated instances; full history/reopen/export and selected-tile behavior. Asset retirement must not orphan saved projects. No Pro sticker browser. Validate edges on light/dark/textured backgrounds and at actual size; concept count is not acceptance.

## Q22

**R7 keeps GIF and Collage complete.** Current GIF document is a single video/GIF clip or2–20 photo sources with one global photo delay100–1500ms, global caption/grade and 30 history states. Static “Text on every frame” is not timed captions. Existing saved-photo reopen does not qualify a fresh photo-sequence creation route.

Remaining GIF: real VFR/B-frame/rotation/non-square-pixel/HDR-to-SDR and GIF disposal/transparency/finite-loop/hold corpus; neutral import/source grants/relink; correct still/motion/cadence and F03 timings; stable frame/segment assembly with split/remove/duplicate/reorder/per-frame holds; explicit ping-pong endpoint/tick rounding; keyed captions/overlays; palette/dither and actual encoded appearance preview. Preserve full recipe/history/output fidelity.

Legacy v4 anamorphic upgrade must reinspect source metadata, show before/after, write a reversible versioned copy and adopt only explicitly. Retain original and manual aspect intent. Missing source requires relink before correction; never guess new geometry from incomplete legacy fields. G05 remains LIMITATION until this workflow qualifies.

Collage retains all seven layouts and 1–20 independent tile instances, including duplicate URIs; aspect/gap/margin/border/rounding/background/Fit/Fill and full Regular tile tools. Relink preserves recipe; Replace intentionally resets/replaces per explicit contract. Resolve F01/F02 parent/child routes while protecting Done/Cancel/history and stable tile identity. Prove20 separately edited outputs through reorder/Undo/reopen/export, alpha/orientation/missing-source/old schema and interrupted export. Decode actual output at qualified resource tiers; preview appearance alone does not close C01–C04.

## Q23

**“Finished” means finite observable outcomes, not the label AAA.** Each required pre-Video family must meet its child exits or have an explicit owner scope decision. Default qualification dossier per workspace: create/import→edit→cancel/Undo/Redo→save→cold reopen/recover→export→independent output inspection, with source hashes, exact build/recipe/tier and limitations. One coherent candidate and focused owner script follow engineering; no endless isolated phone builds.

Common UX exit: one discoverable Options home; no prohibited Regular commands; consistent history controls; taps/drags/pinch/cancel have predictable ownership; dirty departures preserve work; empty state usable; no clipped/hidden actions at agreed font/window sizes; TalkBack/focus/keyboard semantics; novice and expert tasks completed without unexplained dead ends.

Declare finite minimum/middle/high physical resource tiers plus representative OEM/codec/provider cases, source-size/format tiers, low storage/revoked permission/partial copy, sustained editing/export/heat and recovery. Establish measurable latency/memory/cadence budgets before claiming tier support; report raw sample counts and distributions appropriately. Existing API29/API35 tests are retained but are not all physical devices. The previous16 MiB Pro condition stays an explicit qualification item, not assumed repaired.

Lance's statement credits **unmentioned supplied0.12.10 checks as owner-reported pass**. It closes those scoped observations; F01/F02/F03 and new common UI remain open. It does not accept all 77 families, prove real process-loss recovery, qualify untested cameras/large GIF workloads, grant four workspace locks or approve publication. R8 ends with separate explicit Regular/Pro/Collage/GIF owner locks. A missing owner-specific observation may hold that closure while independent agreed pre-Video tasks proceed **only after Stage 3/resume**.

## Q24

**R9 stays gated by all four owner locks.** The full future Video scope is preserved: versioned project/source/track identities/timebase/proxies/player/seek/trim and real H.264/AAC MP4; multitrack split/join/reorder/duplicate/In-Out/append/insert/overwrite/ripple/lift/extract/roll/slip/slide/snapping/markers/frame nudge/linked audio/J-L cuts/target/locks/sync; local user audio and voice recording, waveforms/gain/mute/solo/range mute/fades/pan/EQ/dynamics/limiter/conventional noise reduction/automation/ducking/buses/stems/channel/sample-rate policy.

Compositing includes PiP/keyframes/transforms/opacity/titles/mattes/chroma-luma key/speed ramps/reverse/freeze/transitions/stabilization/nested-compound/multicamera. Photo+audio slideshow is real MP4 with per-photo durations/deterministic pan/zoom and optional beat-sync. Color includes Looks/selected-range grades/scopes/bypass/copy/LUTs/explicit log-SDR-HDR and preview/final parity. Timed privacy blur/pixelation/opaque covers and editable motion/keyframes/optional deterministic tracking require review of uncertain intervals and real re-encoded removal in every affected frame/stream/thumbnail, not edit-list concealment.

Media management/mastering includes bins/metadata/conform/versions/full-range-audio-only outputs/subtitles/interchange/archive/relink; initially qualified SDR then measured1080p/4K/HEVC/HDR tiers. Exact frameworks/dependencies are reviewed when gate opens. Required later tiers remain tracked, not deleted by “initial.” Exit is saved/recovered multitrack output independently decoded for frames/audio/timestamps/color plus real slideshow and audio-cleanup measurements.

**R10:** first all-five editing qualification and owner acceptance, then final English/Spanish strings/help/welcome/language/legal/privacy/license/access/accept-decline/versioned local acknowledgement. Test fresh/upgrade/offline/decline/returning-user/TalkBack/large fonts and editing regressions both languages. No account/unlock/IAP invented. Continuous license diligence is separate from deferred customer screens.

**R11:** fully functioning qualified app **and owner-confirmed finished Greenezone website**, then actual Play Console account/identity/payment/testing requirements. Console is not set up. No website/DNS/account work is authorized here. Before submission check then-current official policy/target API/adaptive/permissions/Data safety/content/rating/price/territories/support; no claim that today's policies guarantee future approval. Complete exact shipped SBOM/transitives/fonts/assets/profiles/codecs/notices and rights/security review; current packaging exclusions need inspection, not an unsupported license-violation claim. Protect production keys outside repo, verify signed AAB/installed splits/release behavior/same-ID migrations/native compatibility as applicable; truthful listing/media, required testing/prelaunch and support/update/rollback plan. Lance approves the exact public build/listing/price before publication. This answer records work/gates, not legal advice or current policy certification.

## Q25

**Proposed dependency order after Stage 3 and the owner's resume prompt:**

| Batch | Scope and dependency | Observable exit / stop |
| --- | --- | --- |
| B0 — agreed contract | Stage 3 answers all 50, resolves concrete command/dirty/child defaults, reconciles single canonical docs and creates resume instruction. | Exact baseline/owners/acceptance named; no engineering during this exchange. No arbitrary new25/50 cycle. |
| B1 — owner interaction wave | F01/F02/F04–F06: common header/Options and command ownership; correct parent tap; discriminate child tap; full dirty route coverage with minimal save identity/success continuation. | One-tap view contract, every named route safe, no duplicates/prohibited Regular UI, retained edits/outputs. No full autosave rewrite prerequisite unless actual correctness requires a narrowly stated slice. |
| B2 — measured GIF correction | F03 workload capture may run alongside B1 after resume; identify dominant stage then one bounded fidelity-preserving repair. | Actual workload stages/fidelity and improved qualified target; owner-specific closure remains held if source/device facts unavailable. Do not block unrelated B1 work or call F03 fixed from small fixture. |
| B3 — R1.1 | Bounded Regular listing/migration and selected loading, targeted existing Pro/Collage/GIF behavior. | Twelve near-budget/corrupt cases, memory/identity/healthy-byte proof as Q14. |
| B4 — R1.2 then remaining R1 | Recoverable Regular completed edits, extend custody/history/navigation to all, source/revision/provider/migration/portable/repeated handoff. | Real process-loss and output/custody exits Q15–16. |
| B5 — R2 then R3/R4; R5/R6; R7 | Foundation-dependent Pro tools/SubjectBackground; photography precision before RAW/merge extensions; local catalog/batch/creativity/stickers; GIF/Collage completion. Disjoint proven-independent files may run concurrently. | Every77-family child retained; each bounded task closes on workflow/persistence/output evidence. |
| B6 — R8 | Complete pre-Video physical/UX/accessibility/failure tiers and focused owner acceptance. | Four explicit locks; no inference from CI. |
| B7 — R9 → R10 → R11 | Full Video then all-five acceptance/final bilingual onboarding, app+website gate/Console/release qualification. | Exact publication approval only after documented release exits. |

**First engineering baseline:** re-read live main on resume; preserve any newer authorized changes. At this response's intake, app inputs are PR #31 merge3ad65c533f7667a136d4fa1838afcc06d3f484da under docs mainb9b5ff14ec5a833416b972c4b670a7e9157a2e0a. A later docs-only Stage 2/3 commit does not create a new binary. No branch from old PR #26/#29 should replace it.

**Proposed file ownership for B1:** one integration/controller writer owns PhotoEditorScreen, CollageWorkspaceScreen, RegularPhotoDock, EditorDock and shared header/departure interface; a separate Pro owner edits ProWorkspaceScreen only against that agreed interface; a GIF owner edits VideoGifScreen/GifMakerScreen/GifProjectDialogs/GifExportRecovery against it. Shared model/session/history/store files each get one explicit owner before edits. Review owner is distinct. No simultaneous “helpful” rewrites of shared files. Introduce only the minimal typed clean identity/departure/save result adapter needed by this wave.

**Decisive reproduction before correction:** parent single-tap currently does not toggle; selected-tile route trace determines whether and where its reported failure reproduces; dirty Regular Back with a prior workspace and mode change, Pro Back/Open, Collage Back/New must demonstrate route/work state; current duplicate/header/More UI is inventoried; existing protected replacement/child Back routes serve as controls. Freeze exact source and expected documents/history/outputs, then change one bounded contract. New tests assert actual callback ownership, saved bytes and decoded pixels; do not merely mirror implementation or substitute screenshots.

**Qualification for app changes:** focused red-before/green-after cases and retained successes, independent review/output decoding, full required JVM/API29/API35 final candidate and independent merged application qualification, actual case identities/results/skips and exact APK provenance. Retain original6/15s GIF limits, all 193 existing cases unless an explicit justified test migration preserves their assertions, earlier failure receipts and exact source/run identity. A new failure gets a named hypothesis/change; no unchanged retry loop. Provide one coherent qualified next phone candidate and only remaining relevant observations. No build/test is warranted for this Stage 2 docs-only change.

**True blockers:** Stage 3 answers/resume gate blocks all app engineering now; missing owner workload blocks owner-specific GIF reproduction; owner-only unsupported scope decisions block the affected feature/tier; later four locks/all-five acceptance/website/account/publication gates block their phases. None justifies deleting required work or restarting the entire audit. Required minimal save identity/child ownership may precede header wiring; full R2/R5 architecture is not a prerequisite for B1.


## Complete 77-family reconciliation

The exit and source columns below are the complete intake matrix wording, preserved for comparison. Disposition controls the proposal where a wording discrepancy is named; canonical changes wait for Stage 3. Old status is the last Codex-authored checkpoint, not today's completion claim.

| ID | Workspace / capability | Old status → intake status | Package / dependency lane | Existing full exit / remaining work | Recorded source | Proposed disposition |
| --- | --- | --- | --- | --- | --- | --- |
| D01 | Regular / tile — Fresh cover then manual crop | ENGINEERING VERIFIED → ENGINEERING VERIFIED | R0 | Held/quick crop keeps anchor and pointer; Undo/reopen/export protect same pixels | PR #26 review §1; PR #29 candidate/main 186-per-API evidence | Retain ENGINEERING VERIFIED for the bounded R0 workflow; no whole-family/owner-lock credit. Q01–03/23. |
| D02 | Regular — Readable bounded named saves and export requests | ENGINEERING VERIFIED → ENGINEERING VERIFIED | R0 | Reject oversized before mutation/picker; healthy bytes and dirty draft survive close/replace | PR #26 review §2; PR #29 candidate/main 186-per-API evidence | Retain ENGINEERING VERIFIED for the bounded R0 workflow; no whole-family/owner-lock credit. Q01–03/23. |
| D03 | Regular / tile — Complete long Draw and cancel | ENGINEERING VERIFIED → ENGINEERING VERIFIED | R0 | Late bends/end within 500 samples; cancel Draw/erase restores history and output | PR #26 review §3; PR #29 candidate/main 186-per-API evidence | Retain ENGINEERING VERIFIED for the bounded R0 workflow; no whole-family/owner-lock credit. Q01–03/23. |
| D04 | Pro — Complete long lasso and cancel | ENGINEERING VERIFIED → ENGINEERING VERIFIED | R0 | Late contour selected correctly within 450 budget; operation/history/reopen | PR #26 review §4; PR #29 candidate/main 186-per-API evidence | Retain ENGINEERING VERIFIED for the bounded R0 workflow; no whole-family/owner-lock credit. Q01–03/23. |
| S01 | All — Original media, independent documents and target identity | PARTIAL → PARTIAL | R1 | No source overwrite/wrong-owner edit through duplicate URIs/history/async results | Audit 2; master §7 | Retain PARTIAL; B1 dirty/child identities then R1 source revision/alias custody. Q09–10/16. |
| S02 | All — Reader/writer schema limits and transactional migrations | PARTIAL → PARTIAL | R1 | Old/new/malformed/budget fixtures preserve last healthy generation | Master §7; Audit 2 | Retain PARTIAL; version schema and render semantics, copy/validate/publish migrations. Q16. |
| S03 | All — Durable draft autosave, snapshots and recovery | OPEN → OPEN | R1 | True process loss between writes restores completed work and navigation | Master §§7/11; review recovery limits | Retain OPEN; R1.2 committed-checkpoint real process loss; no credit from retained session. Q15. |
| S04 | All — Back, transient surfaces and gesture transactions | PARTIAL → PARTIAL | R1 | Resolve dirty-exit guards and Collage single-tap chrome; one committed pop; cancel/half-cancel/double Back, tool/IME/picker precedence and Save/Discard/Cancel preserve intended work | Latest phone report F01/F02; W0; master §11 | Retain PARTIAL; F01/F02 first-wave tap/route/transient contracts. Q08–10. |
| S05 | All — Provider write ownership and recovery | PARTIAL → PARTIAL | R1 | Real external picker/process death, aliases/partial writes/read-only grants and retry | Audit 2 AND01; review | Retain PARTIAL; immutable jobs credited, provider alias/uncertainty/process loss still open. Q11/16. |
| S06 | Regular ↔ Pro — Explicit saved repeated revision exchange | PARTIAL → PARTIAL | R1 | R→P→R→P retains native parents, appearance, alpha, profile and versions | Master §§7/11; existing Yes/No transfer tests | Retain PARTIAL; live handoff credited; saved revision-linked round trip open. Q09/16. |
| S07 | All — Portable native bundle, backup and relink | OPEN → OPEN | R1 | Second-device import resolves permitted sources; missing media preserved/relinked | Master §§5/7 | Retain OPEN; required portable sources/assets/manifest/second-device import. Q16. |
| S08 | All — Command ownership and complete actual-control inventory | PARTIAL → PARTIAL | R1 | One top-right Options menu, deduplicated major commands, common header/Undo-Redo arrows; remove Regular More/Create collage/Histogram UI; retain tool ownership | Latest owner requests F04–F06; Revision 4 §3; master §3 | Retain PARTIAL; F04–F06 authorize common header/Options/removals, no unrelated reskin. Q04–07. |
| P01 | Pro — Image/shape/paint/adjustment layer foundation and legacy text | PARTIAL → PARTIAL | R2 | Retain 16 blends, 12-layer baseline and legacy recipes; qualify full workflows | ProModel/ProRaster/ProStore; Audit 2 | Retain PARTIAL; full existing exit stays required in R2. Q17. |
| P02 | Pro — Layer tree, groups, clipping, locks and multiselect | OPEN → OPEN | R2 | Isolated/pass-through groups, group opacity once, clipping base/history stable | Master §5 | Retain OPEN; full existing exit stays required in R2. Q17. |
| P03 | Pro — Bounded tiled compositor and device capacity tiers | OPEN → OPEN | R2 | CPU reference versus preview/export, halos/alpha/seams/resource teardown; measured scale | Master §§5/7 | Retain OPEN; full existing exit stays required in R2. Q17. |
| P04 | Pro — Canvas and object geometry | PARTIAL → PARTIAL | R2 | Native geometry retained; canvas resize distinct from image resample; limit disclosed | Audit 2 PRO03/04 | Retain PARTIAL; full existing exit stays required in R2. Q17. |
| P05 | Pro — Compound selections, channels and refined masks | PARTIAL → PARTIAL | R3 | Color/luminance/contiguous/global, polygon, boolean sets, saved selections, feather/grow/refine | Master §5; current rectangle/ellipse/lasso | Retain PARTIAL; full existing exit stays required in R3. Q18. |
| P06 | Pro — Perspective, skew, nonuniform transform, guides and snapping | OPEN → OPEN | R3 | Correct inverse mapping/numeric targets under crop/zoom/reopen/export | Master §5 | Retain OPEN; full existing exit stays required in R3. Q18. |
| P07 | Pro — Vector shape/path authoring | PARTIAL → PARTIAL | R3 | Bezier/path booleans, fills/gradients/joins/dashes; no new Text authoring | Master §5; current simple shapes | Retain PARTIAL; full existing exit stays required in R3. Q18. |
| P08 | Pro — Professional brush/eraser dynamics | PARTIAL → PARTIAL | R3 | Flow/hardness/pressure/spacing/stabilization/touch fallback deterministic replay | Master §5; verified held paint/erase | Retain PARTIAL; full existing exit stays required in R3. Q18. |
| P09 | Pro — Non-destructive clone/heal/patch/inpaint | PARTIAL → PARTIAL | R3 | Sampling policy, preserved donor, editable marks and independent output | Master §5; current 80-stamp clone/soft repair | Retain PARTIAL; full existing exit stays required in R3. Q18. |
| P10 | Pro — Ordered effects/styles and blend conditions | PARTIAL → PARTIAL | R3 | Layer/filter/group scopes, ordering, alpha, strokes/shadows/bevel/displacement | Master §5 | Retain PARTIAL; full existing exit stays required in R3. Q18. |
| P11 | Pro — Declared layered interchange and reusable actions | OPEN → OPEN | R3 | Native fidelity first; named OpenRaster/PSD subset and loss report; repeatable actions | Master §5 | Retain OPEN; full existing exit stays required in R3. Q18. |
| P12 | Pro — Guided non-AI subject selection and editable extraction | OPEN → OPEN | R4 | Manual/assisted trace keeps original plus editable matte | Subject & Background addendum; Revision 4 | Retain OPEN; full existing exit stays required in R4. Q19. |
| P13 | Pro — Background removal/replacement and mask restore | OPEN → OPEN | R4 | Photo/color/gradient background; linked subject transform; reopened alpha output | Subject & Background addendum | Retain OPEN; full existing exit stays required in R4. Q19. |
| P14 | Pro — Edge refinement, feather and fringe cleanup | OPEN → OPEN | R4 | Hard/soft/hair/fur/translucent fixtures with honest limits and correction | Subject & Background addendum | Retain OPEN; full existing exit stays required in R4. Q19. |
| P15 | Pro — Conventional object removal | OPEN → OPEN | R4 | Small-object inpaint/patch/clone preserves outside pixels and editable sources | Subject & Background addendum | Retain OPEN; full existing exit stays required in R4. Q19. |
| P16 | Pro — Liquify/mesh warp, reshaping and material retexture | OPEN → OPEN | R4 | Push/pull/expand/shrink/freeze and editable deformation, cancel/reopen/export | Master §5; Subject & Background | Retain OPEN; full existing exit stays required in R4. Q19. |
| R01 | Regular — Photo import, orientation, view, before/after | PARTIAL → PARTIAL | R5 | Supported formats, original hash, correct orientation/crop, physical usability | Existing PhotoEditor/ImagePipeline | Retain PARTIAL; full existing exit stays required in R5. Q20. |
| R02 | Regular — Tone/color/HSL/curves/grade and Looks | PARTIAL → PARTIAL | R5 | Every advertised control changes correct pixels, saved recipe and export; neutral reset | ControlCatalog/ToneMath/CurveMath/SavedLooks | Retain PARTIAL; full existing exit stays required in R5. Q20. |
| R03 | Regular — Histogram/clipping and numerical inspection | PARTIAL → PARTIAL | R5 | Remove visible Regular Histogram entry; preserve processing/diagnostic evidence and source/recipe identity; label any future working/proof domains without reintroducing that command | Latest owner request F05; Audit 2 REG06; PhotoHistogram | Retain ID/PARTIAL; owner removes all visible Regular Histogram entry points; keep necessary math/diagnostics. Q07/20. |
| R04 | Regular — Float/16-bit managed image pipeline | OPEN → OPEN | R5 | Declared linear/encoded alpha/profile semantics; precision ramps and reference math | Master §4; current 8-bit sRGB | Retain OPEN; full existing exit stays required in R5. Q20. |
| R05 | Regular — DNG intake and sensor development | OPEN → OPEN | R5 | Declared mosaic/linear/compression/opcode set, black/white levels/CFA/demosaic/WB/calibration | Master §4 | Retain OPEN; full existing exit stays required in R5. Q20. |
| R06 | Regular — Broader camera RAW and profiles | OPEN → OPEN | R5 | Rights-cleared per-camera variant table; decoder licensing before integration | Master §4; no default LibRaw | Retain OPEN; full existing exit stays required in R5. Q20. |
| R07 | Regular — ICC/wide gamut, soft proof and high-bit exports | OPEN → OPEN | R5 | Assign versus convert; output profile and 16-bit samples independently verified | Master §4 | Retain OPEN; full existing exit stays required in R5. Q20. |
| R08 | Regular — Optics, perspective and photographic detail | PARTIAL → PARTIAL | R5 | Lens distortion/CA/vignette/upright plus qualified non-ML denoise/sharpen | Master §4; basic DetailMath exists | Retain PARTIAL; full existing exit stays required in R5. Q20. |
| R09 | Regular — HDR bracket merge, panorama and focus stacking | OPEN → OPEN | R5 | Deterministic alignment/blending, motion policy and contributing-source lineage | Master §4 later parity families | Retain OPEN; full existing exit stays required in R5. Q20. |
| R10 | Regular — Local catalog, collections, metadata and rules | OPEN → OPEN | R6 | Folders/collections/ratings/tags/EXIF/IPTC/XMP/search/duplicate verification | Master §4 | Retain OPEN; full existing exit stays required in R6. Q21. |
| R11 | Regular — Virtual copies, sidecars, sync and catalog recovery | OPEN → OPEN | R6 | Selective compatible edits, source-safe relink, migrated backup and independent copies | Master §4 | Retain OPEN; full existing exit stays required in R6. Q21. |
| R12 | Regular — Durable batch processing and export | OPEN → OPEN | R6 | Frozen items/multiple outputs/name/profile/privacy, cancel/resume/idempotent receipts | Master §4 | Retain OPEN; full existing exit stays required in R6. Q21. |
| R13 | Regular / tile — Text and typography | PARTIAL → PARTIAL | R6 | Unicode shaping/fonts/styles/editability/crop/reopen/export; asset rights | Revision 4; CreativeModel/CreativeRaster | Retain PARTIAL; full existing exit stays required in R6. Q21. |
| R14 | Regular / tile — Draw, Shapes and decoration | PARTIAL → PARTIAL | R6 | R29-01/02 Eraser geometry/capacity engineering verified; owner touch acceptance and independent transforms/layers, borders/frames remain | PR #31 candidate/main 193-per-API, five actual Eraser workflows; Revision 4; current 32-text/160-stroke/64-shape limits | Retain PARTIAL; PR31 repair and scoped owner supplied-check passes credited; broader object/decoration/device work open. Q21/23. |
| R15 | Regular / tile — Privacy cover/blur/pixelation | PARTIAL → PARTIAL | R0/R6 | Same source detail protected through geometry/export; blur strength not called secure redaction | Audit 2; fresh-crop D01 | Retain PARTIAL; full existing exit stays required in R0/R6. Q21. |
| R16 | Regular / tile — Finished offline sticker library | OPEN → OPEN | R6 | ≥500 usable rights-cleared assets; 648 concepts/27 categories alone earn no credit | Revision 3.1 sticker requirements | Retain OPEN; full existing exit stays required in R6. Q21. |
| R17 | Regular / tile — Sticker browser, transform, custom PNG/WebP | OPEN → OPEN | R6 | Search/favorite/recent; alpha/order/duplicate/delete; Undo/reopen/export per tile | Revision 3.1 sticker requirements | Retain OPEN; full existing exit stays required in R6. Q21. |
| R18 | Regular — Image formats, dimensions and metadata policy | PARTIAL → PARTIAL | R5/R6 | Seven actual encoders/social Fit/Fill; exact dimensions/alpha; privacy/high precision extensions | ImageExportWriter/PhotoGeometryPlan; Audit 2 AND02 | Retain PARTIAL; full existing exit stays required in R5/R6. Q21. |
| G01 | GIF — Neutral video/GIF import and correct displayed aspect | PARTIAL → PARTIAL | R7 | Supported VFR/rotation/non-square pixels/HDR conversion and first-use neutrality | Audit 2 GIF05; VideoGifSource | Retain PARTIAL; full existing exit stays required in R7. Q22. |
| G02 | GIF — Edited playback, scrub, reverse, trim, speed | PARTIAL → PARTIAL | R7 | Input to correct still/first motion/cadence measured; history/reopen/phone corpus | PRs #18–26; existing synthetic gates | Retain PARTIAL; full existing exit stays required in R7. Q22. |
| G03 | GIF — Palette, disposal, transparency, loops and holds | PARTIAL → PARTIAL | R7 | Independent decoder corpus, temporal color and explicit limits | GifEncoder/GifPalette; fidelity tests | Retain PARTIAL; full existing exit stays required in R7. Q22. |
| G04 | GIF — Named project identity/quota and recovery | PARTIAL → PARTIAL | R7 | Preserve existing save proofs; missing-source relink/cold process/platform migration | Audit 2 GIF01/02; GifStore | Retain PARTIAL; full existing exit stays required in R7. Q22. |
| G05 | GIF — Legacy v4 anamorphic project upgrade | LIMITATION → LIMITATION | R7 | Source reinspection and reversible versioned copy; preserve manual framing and old project | Independent PR #26 review §5 | Retain LIMITATION; reversible source-backed upgraded copy, preserve original/manual framing. Q22. |
| G06 | GIF — Photo-sequence creation and frame/segment assembly | PARTIAL → PARTIAL | R7 | New creation separately qualified; per-frame holds/split/remove/duplicate/reorder | GifModel; master §6 | Retain PARTIAL; full existing exit stays required in R7. Q22. |
| G07 | GIF — Timed captions/overlays, ping-pong and encoded preview | OPEN → OPEN | R7 | Keyed appearance/timing, endpoint schedule and palette/dither comparison | Master §6; current static caption exists | Retain OPEN; full existing exit stays required in R7. Q22. |
| G08 | GIF — Export responsiveness and full saved animation fidelity | PARTIAL → PARTIAL | R7 | Resolve owner-reported 120–300-second Save using actual workload/stages; qualify larger/longer inputs, cadence and device heat; preserve fidelity and 15 s Save/6 s sampled-motion fixture gates | Latest phone report F03; PR #31 candidate/main 193-per-API; exact cache pixel equivalence and decoded/replayed output; retained earlier failures | Retain PARTIAL; F03 physical120–300s open; small fixture and exact-cache proof credited only within scope. Q11–13. |
| C01 | Collage — 1–20 independent tiles and duplicate-source identity | PARTIAL → PARTIAL | R7 | All 20 independently edited outputs survive reorder/history; no contamination | CollageModel/Raster; Audit 2 COL01 | Retain PARTIAL; full existing exit stays required in R7. Q22. |
| C02 | Collage — Seven layouts and full layout controls | PARTIAL → PARTIAL | R7 | Aspect/gap/margin/border/rounding/background/Fit/Fill and orientation/alpha outputs | CollageWorkspaceScreen | Retain PARTIAL; full existing exit stays required in R7. Q22. |
| C03 | Collage — Selected-tile transactions and Regular features | PARTIAL → PARTIAL | R0/R6/R7 | Correct one-tap chrome and dirty parent/child departure; Done/Cancel/Back once and scoped; new stickers/text/draw/privacy remain tile-owned | Latest phone report F01/F02; W0; Revision 4 | Retain PARTIAL; F01/F02 child/parent transaction and tap gaps; future full Regular tool propagation retained. Q08–10/22. |
| C04 | Collage — Project/relink/storage/export resilience | PARTIAL → PARTIAL | R7 | Missing-source recipe retained, stale preview cleared, stage preserved; physical tiers | Audit 2 COL03; CollageStore | Retain PARTIAL; full existing exit stays required in R7. Q22. |
| Q01 | All pre-Video — Full handset/OEM/codec/memory/UX qualification | OPEN → OPEN | R8 | Unmentioned supplied checks have owner-reported passes; named exceptions, common UI, representative tiers, sustained use and accessibility still require qualification | Latest phone report; release gate; master §9 | Retain OPEN; scoped owner phone passes credited, named defects/tier/accessibility/sustained evidence remain. Q23. |
| Q02 | Regular/Pro/Collage/GIF — Four formal owner workspace locks | OWNER GATE → OWNER GATE | R8 | Explicit acceptance of each after evidence, not implied by CI or repair merge | Revision 4 | Retain OWNER GATE; four explicit locks still required. Q23. |
| V01 | Video — Single-clip project/player/trim/export foundation | GATED → GATED | R9 | Real source tracks, seek/timing, safe MP4, reopen/recovery | Master §6; Video currently disabled | Retain GATED; full existing exit stays required in R9. Q24. |
| V02 | Video — Multitrack assembly and precision editorial tools | GATED → GATED | R9 | Split/join/ripple/roll/slip/slide/J/L cuts/markers/link/lock behavior verified in export | Master §11 | Retain GATED; full existing exit stays required in R9. Q24. |
| V03 | Video — User sound/music, recording and professional mixing | GATED → GATED | R9 | Waveforms/gain/mute/fades/EQ/dynamics/non-AI noise reduction/ducking/buses/stems; sync and rights | Master §11 | Retain GATED; full existing exit stays required in R9. Q24. |
| V04 | Video — Compositing, keyframes, transitions and motion | GATED → GATED | R9 | PiP/titles/mattes/keying/stabilization/speed/nested/multicam; photo+audio slideshow with per-photo duration, pan/zoom and optional beat-sync | Master §11 | Retain GATED; full existing exit stays required in R9. Q24. |
| V05 | Video — Color, scopes, Looks and qualified LUT/HDR | GATED → GATED | R9 | Explicit color pipeline/selected ranges/proxies/conform; independent tags/pixels | Master §11 | Retain GATED; full existing exit stays required in R9. Q24. |
| V06 | Video — Moving region effects and privacy redaction | GATED → GATED | R9 | Every affected frame/stream redacted; editable keyframes and reviewed tracking | Master §11 | Retain GATED; full existing exit stays required in R9. Q24. |
| V07 | Video — Media management, mastering and exchange | GATED → GATED | R9 | Bins/proxies/versions/ranges/audio-only/captions/interchange/archive/relink | Master §11 | Retain GATED; full existing exit stays required in R9. Q24. |
| V08 | Video — Qualified 1080p/4K/HEVC/HDR device tiers | GATED → GATED | R9 | Actual codecs/thermal/time/audio parity; no unsupported controls | Master §6 | Retain GATED; full existing exit stays required in R9. Q24. |
| Q03 | All five — Final complete editing and usability acceptance | OWNER GATE → OWNER GATE | R10 | All required capabilities checked through real user workflow/output/device | Master §9 | Retain OWNER GATE; all-five complete editing acceptance before final onboarding. Q24. |
| L01 | All five — English/Spanish localization and contextual help | GATED → GATED | R10 | Complete strings/layout/accessibility/editing regressions both languages | Revision 4 final phase | Retain GATED; full existing exit stays required in R10. Q24. |
| L02 | App — Welcome, legal notices and acceptance/access flow | GATED → GATED | R10 | Fresh/upgrade/offline/decline/reaccept consistent with paid-once product | Revision 4 final phase | Retain GATED; full existing exit stays required in R10. Q24. |
| L03 | External — Greenezone website complete | OWNER DEPENDENCY → OWNER DEPENDENCY | R11 | Owner confirms website finished; app task changes no website | Lance, 2026-11-24 | Retain OWNER DEPENDENCY; website completion reported by owner; no website work in app scope. Q24. |
| L04 | Release — Play Console setup and verified identity/payment | GATED → GATED | R11 | After working app and website; actual account dictates verification/testing | Lance, 2026-11-24; account not created | Retain GATED; Console not set up, after finished app AND website. Q24. |
| L05 | Release — SBOM, assets/licenses/notices/security and legal review | OPEN → OPEN | Continuous/R11 | Exact shipped binary rights/transitives/notices/profiles/codecs audited | Master §9 | Retain OPEN; full existing exit stays required in Continuous/R11. Q24. |
| L06 | Release — Production signing, AAB, split install and upgrades | OPEN → OPEN | R11 | Secure keys, signed artifact, same-ID migration/release build/device compatibility | Release gate | Retain OPEN; full existing exit stays required in R11. Q24. |
| L07 | Release — Store listing/policies/rating/privacy/pricing/testing | GATED → GATED | R11 | Current policy/account verified; accurate listing; required tests/pre-launch | Play official guidance; refresh at launch | Retain GATED; full existing exit stays required in R11. Q24. |
| L08 | Release — Owner publication approval and maintenance | OWNER GATE → OWNER GATE | R11 | Exact build/listing/price approval, support/rollback and data-safe updates | Master §9 | Retain OWNER GATE; approval of exact public build/listing plus support/update plan. Q24. |


## Pinned source index

- [PhotoEditorScreen.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/ui/PhotoEditorScreen.kt) — exact intake source; line ranges in answers refer to this revision.
- [EditorDock.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/ui/EditorDock.kt) — exact intake source; line ranges in answers refer to this revision.
- [RegularPhotoDock.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/ui/RegularPhotoDock.kt) — exact intake source; line ranges in answers refer to this revision.
- [CreativeCanvasOverlays.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/ui/CreativeCanvasOverlays.kt) — exact intake source; line ranges in answers refer to this revision.
- [BrushOverlay.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/ui/BrushOverlay.kt) — exact intake source; line ranges in answers refer to this revision.
- [PrivacyRegionOverlay.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/ui/PrivacyRegionOverlay.kt) — exact intake source; line ranges in answers refer to this revision.
- [CollageWorkspaceScreen.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/ui/CollageWorkspaceScreen.kt) — exact intake source; line ranges in answers refer to this revision.
- [CollageModel.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/CollageModel.kt) — exact intake source; line ranges in answers refer to this revision.
- [CollageStore.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/CollageStore.kt) — exact intake source; line ranges in answers refer to this revision.
- [ProjectStore.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/ProjectStore.kt) — exact intake source; line ranges in answers refer to this revision.
- [EditDocumentCodec.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/EditDocumentCodec.kt) — exact intake source; line ranges in answers refer to this revision.
- [EditModel.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/EditModel.kt) — exact intake source; line ranges in answers refer to this revision.
- [PhotoExportController.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/ui/PhotoExportController.kt) — exact intake source; line ranges in answers refer to this revision.
- [PendingExportJournal.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/PendingExportJournal.kt) — exact intake source; line ranges in answers refer to this revision.
- [PendingProExportRecipe.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/PendingProExportRecipe.kt) — exact intake source; line ranges in answers refer to this revision.
- [StagedImageExport.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/StagedImageExport.kt) — exact intake source; line ranges in answers refer to this revision.
- [ProWorkspaceScreen.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt) — exact intake source; line ranges in answers refer to this revision.
- [ProModel.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/ProModel.kt) — exact intake source; line ranges in answers refer to this revision.
- [ProTools.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/ProTools.kt) — exact intake source; line ranges in answers refer to this revision.
- [ProRaster.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/ProRaster.kt) — exact intake source; line ranges in answers refer to this revision.
- [ProStore.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/ProStore.kt) — exact intake source; line ranges in answers refer to this revision.
- [WorkspaceModeMenu.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/ui/WorkspaceModeMenu.kt) — exact intake source; line ranges in answers refer to this revision.
- [GifMakerScreen.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/ui/GifMakerScreen.kt) — exact intake source; line ranges in answers refer to this revision.
- [VideoGifScreen.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/ui/VideoGifScreen.kt) — exact intake source; line ranges in answers refer to this revision.
- [GifProjectDialogs.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/ui/GifProjectDialogs.kt) — exact intake source; line ranges in answers refer to this revision.
- [GifExportRecovery.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/ui/GifExportRecovery.kt) — exact intake source; line ranges in answers refer to this revision.
- [GifEncoder.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/GifEncoder.kt) — exact intake source; line ranges in answers refer to this revision.
- [GifPalette.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/GifPalette.kt) — exact intake source; line ranges in answers refer to this revision.
- [GifModel.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/GifModel.kt) — exact intake source; line ranges in answers refer to this revision.
- [GifStore.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/GifStore.kt) — exact intake source; line ranges in answers refer to this revision.
- [GifEditingDeck.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/ui/GifEditingDeck.kt) — exact intake source; line ranges in answers refer to this revision.
- [GifExportGuard.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/GifExportGuard.kt) — exact intake source; line ranges in answers refer to this revision.
- [VideoGifSource.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/VideoGifSource.kt) — exact intake source; line ranges in answers refer to this revision.
- [EditedVideoPreviewReader.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/EditedVideoPreviewReader.kt) — exact intake source; line ranges in answers refer to this revision.
- [GifPreviewDiagnostics.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/GifPreviewDiagnostics.kt) — exact intake source; line ranges in answers refer to this revision.
- [ImagePipeline.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/ImagePipeline.kt) — exact intake source; line ranges in answers refer to this revision.
- [ImageExportWriter.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/ImageExportWriter.kt) — exact intake source; line ranges in answers refer to this revision.
- [CreativeModel.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/app/src/main/java/com/greenezone/gzphotoeditor/editor/CreativeModel.kt) — exact intake source; line ranges in answers refer to this revision.

- [Stage 1 owner findings and questions](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/CODEX_ALIGNMENT_HANDOFF_2026-11-24.md)
- [Current product specification](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/PRODUCT_SPEC.md), [roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md), [matrix](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/CAPABILITY_MATRIX.md), [release gate](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/RELEASE_GATE.md).


## Intake branch inventory

Read-only ancestry comparison against main `b9b5ff14ec5a833416b972c4b670a7e9157a2e0a`; no refs were deleted or integrated. Ahead/behind are commit counts, not application differences.

| Branch | Exact head | Status | Ahead / behind main |
| --- | --- | --- | --- |
| audit/gif-pro-production-hardening-013 | `4b5e6f25ba5d6107052a49a8a68ade71e89f8f5e` | behind | 0 / 98 |
| docs/audit-2-review-handoff | `95307a2072d6cf12a7b4fab85fbaac294ddcae72` | behind | 0 / 18 |
| docs/codex-audit-2-2026-11-23 | `6bfa217a09ae20d2f512c06a9986c71c570f1c25` | behind | 0 / 30 |
| docs/gz-editor-pro-field-guide-20261122 | `7c5d40b099484c338e4d4cf63dca7ba083482ba3` | behind | 0 / 42 |
| docs/gz-review-handoff-2026-11-23 | `37706a663daccf67dab587ef2aac4db11bb3955f` | behind | 0 / 32 |
| docs/new-chat-handoff-2026-11-24 | `17561aef06c104fede65f6141c6f2732a718ac52` | behind | 0 / 3 |
| docs/phone-feedback-codex-alignment-20261124 | `631867f115cbee3b469a28c578b674146e9c131f` | behind | 0 / 1 |
| docs/pr29-qualified-state | `7cc475660760c7b17062ca5b8c290c728b8235df` | behind | 0 / 11 |
| docs/pr31-qualified-phone-handoff | `b2abc539fe0fcf9c3fd6a406ca326293e51810fa` | behind | 0 / 5 |
| docs/reconcile-active-project-guidance | `6f4c3ffe1964e626a7d5922524ee79aab49e2e5c` | behind | 0 / 16 |
| feature/aaa-gif-pro-hardening-013 | `96de16b7183f6b1ce64359a73cf8cbcb1da46e9b` | behind | 0 / 91 |
| feature/audit-2-editor-repairs | `6a8cceba719dc990d663768f626ab87d9436aad4` | behind | 0 / 20 |
| feature/collage-workspace-photo-08 | `d7e0e10c247aa558c816e2901553c5eef13e5b36` | behind | 0 / 166 |
| feature/focavera-premium-wave-2 | `f133d71bc1525f8cfd5b6799c8df179a17a22983` | behind | 0 / 192 |
| feature/gif-export-throughput | `e077be43d11b63634c6a0a9d74a136a89e06bff7` | behind | 0 / 45 |
| feature/gif-fidelity-pro-workspace | `37f69dacf7867db2cf1b709488728e038fa06c31` | behind | 0 / 101 |
| feature/gif-native-playback-pro-workspace-011 | `b5178453335405b377e657ae591b1f23abbd7b7e` | behind | 0 / 105 |
| feature/gif-p0-observability-reproduction | `b1ccb2d65ec078442d59463d0c59cefd47f198ec` | behind | 0 / 63 |
| feature/gif-reverse-startup | `1825bcbaf6c98f7ceced44e2012f97c2f8128d3f` | behind | 0 / 34 |
| feature/gz-editor-pro-creation-export-07 | `5bdeb9729d55b4c669812b79db618a4269764f6d` | behind | 0 / 171 |
| feature/gz-editor-pro-privacy-ux-06 | `10f8f0300495b735fcea52ec7843a704bfc175fd` | behind | 0 / 175 |
| feature/gz-photo-editor-pro | `6723a71938d4c4d96ded30dfef29cac2fbe9ea45` | behind | 0 / 188 |
| feature/native-photo-editor | `143a966a7eae52846006ec84bf48af2a796d39d0` | behind | 0 / 194 |
| feature/phone-editor-regressions-015 | `d3ec444f6f1fd19ce080836fe54124e4e84f4425` | behind | 0 / 81 |
| feature/photo-first-ui-05 | `c2c08306289210e890ddfc15e717493b712c6499` | behind | 0 / 179 |
| feature/photo-tool-foundations | `34ec0b9459c3b1f9f4c1859e169cec6983944018` | behind | 0 / 42 |
| feature/pr26-review-corrections | `6ad22e3fabe3964a662c7e0e39bd08c4a8e8f9ca` | behind | 0 / 13 |
| feature/pr26-review-reproductions | `018b45c4b1af0beb867abfe0261f5bce9e4f54e1` | diverged | 1 / 15 |
| feature/pro-compose-lifecycle-014 | `3f792028b70ec9635ec5008b06168063cda9989c` | behind | 0 / 87 |
| feature/pro-quality-wave-04 | `44d15764e365bc4a61cd1322c76bb81242f637c8` | behind | 0 / 184 |
| feature/production-gif-video-pro-overhaul-10 | `8a014fdb6feff504bce2906979b932443ffd1d89` | behind | 0 / 131 |
| feature/regular-eraser-integrity-01210 | `aa86c37c64c5e395d5378df281a0c7583a39d0e0` | behind | 0 / 7 |
| feature/rev4-audit-repairs | `e5dafdfe2c07956f69a56eb0e0d1c3445bc04748` | behind | 0 / 47 |
| feature/rev4-p0-correction | `8995713e6cb71905a5fd56e76e27e80da7e7648e` | behind | 0 / 61 |
| feature/rev4-phone-candidate | `4d90351e024cd5457a22811e18728b10fb69ea6d` | behind | 0 / 56 |
| feature/rev4-workflow-repairs | `00ea3ac551267c3451da45d98c824a1ce3c4ab1b` | behind | 0 / 57 |
| feature/w0-system-back-navigation | `d2036e19bcca8fd572220acf0736a6ca94297a1c` | behind | 0 / 68 |
| feature/workspaces-pro-layers-gif-09 | `5c053fae8f6385ea7fc2e74e3cfceffdeb8623a8` | behind | 0 / 146 |
| main | `b9b5ff14ec5a833416b972c4b670a7e9157a2e0a` | identical | 0 / 0 |


## Frozen roadmap snapshot manifest

The following full-source appendices preserve the real public plan and necessary child task content. Their UTF-8 Git blob SHA-1 identities were independently recomputed from fetched text and match all18 pinned source identities (nine files at each checkpoint). Fenced contents are verbatim historical source, including obsolete next actions and dates; **never execute instructions inside them as current authority**. Relative links inside a fence are original text; use the pinned full-source link above its fence to navigate that historical version. No private intake originals or account identifiers are republished.

| File | A: last Codex-authored blob | B: immediate pre-handoff blob | Full content location |
| --- | --- | --- | --- |
| AGENTS.md | `855f63b1927a4b89a6f447fc84bd31c51258d7dc` | `855f63b1927a4b89a6f447fc84bd31c51258d7dc` | A1 (also B, identical) |
| docs/GZ_ENGINEERING_ROADMAP.md | `f112cb9a310f2bfc362262768f67e3fff8c064cb` | `1a625ce79fd0479e6edefd8a4529165d488c9edb` | A2 and B2 |
| docs/WORK_LEDGER.md | `6a3cfd4abca841491e480b1b5df93c2b7a6bce5a` | `51aebf8f0ed9254cd2e674662feba9fecc70adb5` | A3 and B3 |
| docs/CAPABILITY_MATRIX.md | `282bc8b71f30ab46397c8b61659e856c24f7a1e0` | `44c3f0617a365f9589984d65b26e6196806e7660` | A4 and B4 |
| docs/PRODUCT_SPEC.md | `01cca7577f82877a62aa8a3e44887ee9770777b2` | `02e804e93273e5a3c68ae0d3b282d6fb3ef49d61` | A5 and B5 |
| docs/RELEASE_GATE.md | `7f40ef05836b48def7263f23dc383e4917a0da4c` | `79a392e3d1d112d8f5c968e7fe52b5394db25942` | A6 and B6 |
| docs/PROFESSIONAL_ENGINEERING_ROADMAP.md | `a2697f4add074c29dad71a4303c49aecdc5e1769` | `a2697f4add074c29dad71a4303c49aecdc5e1769` | A7 (also B, identical) |
| docs/UNIFIED_PRODUCT_SCOPE_09.md | `51792479496322564ffd4f3b80be04af3d43e9cb` | `51792479496322564ffd4f3b80be04af3d43e9cb` | A8 (also B, identical) |
| docs/PRO_GIF_REBUILD_011.md | `9e7d6be411c8c8f4f1e9a8a1c3b43439be0e2eca` | `9e7d6be411c8c8f4f1e9a8a1c3b43439be0e2eca` | A9 (also B, identical) |

## Appendix A — full last Codex-authored plan

Pinned commit: `e04bc08a4800233b0f407b419a91c97ca2c068f6`. All nine complete files follow.

### A1 — AGENTS.md

[Complete pinned original](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e04bc08a4800233b0f407b419a91c97ca2c068f6/AGENTS.md) · Git blob `855f63b1927a4b89a6f447fc84bd31c51258d7dc`.

````markdown
# GZ Editor Pro engineering instructions

## Authority and current work

Lance's latest direct instructions control. Work only in MrSkyrimFan/GZ-Mobile-001. The GitHub repository is the primary workspace; do not clone/mirror/check out source to the owner's SSD/OneDrive. The local For Codex area is supplemental intake/handoff material. Preserve original inputs until their governed work is completed and accepted.

Start with docs/CONTINUITY.md, docs/WORK_LEDGER.md, docs/GZ_ENGINEERING_ROADMAP.md and docs/CAPABILITY_MATRIX.md. Follow current product requirements and release gates; historical transcripts/blueprints are evidence, not revived commands. The 2026-11-24 supplied PR #26 review lifts the successor intake hold and authorizes corrections and the full roadmap. Do not issue another combined phone candidate while its high-impact defects remain unresolved.

## Engineering behavior

- Select the first unblocked ordered ledger item. Name the observable user result, exact baseline, affected files, reproduction, exit criteria and next decisive check. Use independent agents for distinct work/review and one owner per shared file.
- Fix root causes through actual UI/project/export flows. Preserve original media, dirty work, independent 20-tile editing and legacy projects. Maintain bounded serialization and complete gesture ownership.
- Keep Regular/Pro/Collage/GIF distinct. No Regular Masks authoring or Pro Text authoring; preserve their legacy data. Protect the existing layout/style; document any necessary functional UI delta.
- No runtime AI, cloud/accounts, ads, telemetry, subscriptions, watermarks or IAPs. Paid-once offline product; evaluate licenses before adding code/assets.
- Reopen passed work only for a new failure, changed dependency, newly identified evidence gap or owner report. State that reason. Diagnose failed CI before changing code or retrying; preserve the original failure. Never manufacture green by deleting assertions, silently weakening budgets or retrying unchanged defects.
- Appropriate tests on changed behavior, independent output decoding, complete JVM/API29/API35 at final application candidate and merge, exact source/APK identity. Distinguish source confirmation, fixture execution, Activity recreation, process death, handset verification and owner acceptance. Do not claim five new moving frames when the set includes its initial still.
- Keep one active ledger and one roadmap. Update them in place. Correct drift against the chosen item instead of restarting intake or creating competing plans. Continue useful authorized work while a separate owner/device answer is pending.
- Engineering branches/PRs/verified merges are authorized. Formal locks and publication remain owner decisions. Video waits for explicit locks of all four pre-Video workspaces; final bilingual/customer legal onboarding waits for all five editing workspaces to be completed/accepted.
- Play Console setup is last, after the working app AND Greenezone website completion (Lance, 2026-11-24). Do not change the website or unrelated projects from this task.
- Ask only for genuinely unresolved product intent/constraints; investigate technical details directly. Never silently reduce promised scope or represent diagnostic scaffolding as a finished feature.

## Delivery

One coherent qualified handset candidate per meaningful wave, with precise changed behavior, source/run/artifact identity, regression and failure evidence, limitations and short owner script. Public release requires the complete release gate and explicit owner authorization. Keep sensitive local paths, private screenshots and signing secrets out of public repository documents.
````

### A2 — docs/GZ_ENGINEERING_ROADMAP.md

[Complete pinned original](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e04bc08a4800233b0f407b419a91c97ca2c068f6/docs/GZ_ENGINEERING_ROADMAP.md) · Git blob `f112cb9a310f2bfc362262768f67e3fff8c064cb`.

````markdown
# GZ Editor Pro — complete product roadmap

Updated 2026-11-24. Owner: Lance / Greenezone Software LLC. Engineering workspace: MrSkyrimFan/GZ-Mobile-001.

## Current direction and authority

Lance supplied the independent PR #26 review (CHANGES REQUIRED), authorized fixing the issues, and requested a complete roadmap for the original professional app vision. The successor onboarding hold is lifted. The combined handset candidate remains held until the known correction wave passes. Engineering, tests, branches, PRs and verified merges are authorized; physical acceptance, formal workspace locks and public release remain owner decisions.

**Latest release sequencing:** Play Console is not set up. Lance wants its setup last, after a fully functioning app AND completion of the Greenezone website. Website work is an external dependency; this app task does not authorize modifying it.

Read this roadmap with [product requirements](PRODUCT_SPEC.md), [work ledger](WORK_LEDGER.md), [capability inventory](CAPABILITY_MATRIX.md), and [release gates](RELEASE_GATE.md). The inventory gives scope and current evidence; the ledger gives the next executable work. Earlier blueprints and transcripts retain their historical evidence and yield to later direct owner decisions.

## Current engineering state

R0 is engineering-verified in [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29), app merge `c9efc5aa0673f21b98f150733582a24996b34d10`, candidate `6ad22e3fabe3964a662c7e0e39bd08c4a8e8f9ca`, [candidate workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45352950946) and [merged-main workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45355074845). Candidate and merged application share tree `47e95c51254ce8e8f97b3883c44dc157ea8452b4`; each passed Verify and 186/186 Android cases on both API29 and API35, with no failures/errors/skips. Version 0.12.9 Test / code 21 is the qualified phone wave. Handset acceptance, all formal workspace locks and commercial release remain open.

**Next engineering package: R1.** Preserve the completed R0 evidence and start the bounded durability work from the ledger. Do not replay the independent-review intake or reopen repairs without a new reason.

The legacy Pro streaming optimization passes in the recorded CI environments; the earlier 16 MiB hard-cap condition has not been independently requalified. R1/R8 retain explicit low-memory/device-tier measurements. Legacy v4 GIF aspect migration remains R7.

## Product contract

A paid-once, fully offline Android creative application with five separate, understandable workspaces. Target price remains US $2.99 once. No runtime AI/ML inference, cloud/account requirement, ads, telemetry SDK, subscription, credits, watermark or in-app purchase. Imported originals are read-only. No copying proprietary source, presets, artwork or branding.

| Workspace | Intended job and boundaries |
| --- | --- |
| Regular Photo | Approachable everyday editing plus professional photographic development: Looks, tone/color/curves/HSL, optics, RAW and managed color, text/draw/shapes/privacy, stickers, local organization and batch. **No Masks authoring menu or Pro layer/selection tools.** Preserve legacy mask recipes. |
| Editor Pro | Layered compositing, shape/paint/adjustment layers, groups/clipping, masks/selections, transforms, retouching, Subject & Background, warp/liquify and precise non-destructive manipulation. **No Text authoring or duplicate Regular development/sticker dock.** Preserve legacy Pro text and grade data. |
| Collage | Independently editable 1–20 tiles, including repeated source URIs, seven existing layout families, layout controls, named projects and accurate output. The selected tile uses Regular tools without changing its neighbors. |
| GIF | Silent animation from video/GIF/photo sequences; neutral import, real edited playback, timeline and frame edits, captions, palette controls, accurate timing/color, persistence and externally playable GIF output. Sound belongs in Video. |
| Video | Separate professional timeline with clips, compositing, user-owned audio/music/voice, precision editing, effects, keyframes and real MP4 output. **Implementation starts only after Lance formally accepts/locks all four pre-Video workspaces.** |

Preserve the established dark image-first style, workspace selector and working Regular/Collage layout. A necessary functional change must name its user benefit and affected surface. The current repair wave changes no layout. Broad reskins and new product scope need owner decisions.

The terms Lightroom-class, Photoshop-style and studio-quality are behavioral references. The finite capabilities below govern delivery; they are not permission to copy those products or to claim universal parity. The no-AI and mode-ownership rules override incompatible features. Required families cannot disappear merely because they are difficult.

## Historical intake evidence baseline

- App baseline: PR #26 merge `372e0e947caacb2ceaf603bc4ff5a60e44cc33ec`; candidate `6a8cceba719dc990d663768f626ab87d9436aad4`.
- Roadmap intake main: `d2a8f4de41839e25ad54060be803746d7307bee8`, tree `5f62620f9fbad28bd8c404c723d995decf03ecf3`. Its difference from the app baseline is six documentation paths, including PRs #27/#28. No open PR was found at intake.
- Candidate run 45337105839 and app-merge run 45339000608 passed Verify and 174/174 on each API 29 and 35. Prior independently inspected merge receipt archives had matching hashes and identical case sets.
- Existing phone artifact 14895803102 is v0.12.8 Test / phonetest.v20. It is historical QA evidence, **not the next recommended combined handset candidate**.
- Portrait Save: 4.557 s / 5.867 s, 180×320, 88 frames, 2.950 s. The five-sample preview measurement includes the paused initial picture and excludes edit/seek-to-first-correct-still latency. It does not establish full-rate playback.
- Controlled Activity recreation and retained-registry tests prove that boundary; they do not establish OS process reclamation during an actual external picker or general draft autosave.

[Repair evidence](AUDIT_2_REPAIR_VERIFICATION.md) · [Independent prior audit](gz-editor-pro-audit-2-20261123/00_REPOSITORY_START_HERE.md) · [Field guide](gz-editor-pro-field-guide/00_START_HERE.md).

## Ordered delivery plan

These are dependency-ordered work packages, not dates or completion percentages. Each package produces a useful editing outcome with saved/reopened projects and decoded outputs. Independent implementation may proceed in parallel on disjoint files. Integrate in the stated order; use the first unblocked package, without repeatedly reopening accepted predecessors.

### R0 — Correct the independently identified defects

1. Fresh cover → direct manual Crop: preserve protected source pixels and keep gesture ownership stable while held through recomposition; quick release, cancel, Undo/Redo, reopen, decoded PNG and selected Collage tile.
2. Regular save/export integrity: bounded reader-compatible encoding before mutation or picker launch; oversized replacement preserves healthy stored bytes and dirty active work; failed save cannot close or replace it.
3. Regular Draw: represent the complete long gesture within the existing safe budget, retaining late bends and endpoint; cancel drawing/erasing restores prior document without a history entry.
4. Pro Lasso: represent the complete contour within its safe budget, preserve meaningful corners and final segments, cancel safely, and prove selected pixels plus an applied operation/history/persistence.
5. Preserve existing named-save, Pro bounded save, Collage, handoff, GIF performance/fidelity and picker tests. Capture specific red-before and green-after evidence, full candidate and app-merge JVM/API29/API35 results, exact phone APK identity/signature.
6. Correct overstated evidence wording and retain the legacy v4 GIF aspect limitation as explicit follow-up. Do not silently reinterpret old project framing.

Exit: all four defects have production repairs and actual regression evidence; no new high-impact failure; reviewed candidate/merge identity and qualified artifact. Owner handset acceptance stays separate.

### R1 — Durable documents, source custody and complete interaction contracts

Build the smallest needed extensions to existing modules. No application-shell rewrite.
- Version document schemas and render semantics separately; preserve legacy Regular/Collage/Pro/GIF recipes and backups. Check bounds before allocation/write, reject essential unsupported records recoverably, and migrate copies atomically.
- Bound project-list memory: load lightweight metadata and decode selected recipes lazily; qualify multiple near-budget saved documents rather than eagerly retaining every dense recipe.
- Durable recoverable drafts and snapshots for every workspace, selected document/object/tool/viewport/playhead restoration, interrupted-write recovery, source grants/relink, and real process-death behavior. Keep media/history out of saved-instance bundles.
- One history checkpoint per completed gesture; cancellation restores it; Undo remains separate from Back. Verify actual system Back, both edges, half-cancel, double Back, IME/menu/dialog/picker precedence and busy-copy status.
- Explicit source/revision IDs, immutable export jobs, staging validation, write-only providers, aliases, partial writes, low storage and truthful retry. Never claim an ambiguous destination write succeeded.
- Upgrade repeated saved Regular→Pro→Regular→Pro exchange to immutable revision-linked masters with alpha/profile/precision when available. Retain both native parents. Existing Yes/No live transfer remains intact.
- Portable native bundles with declared source embedding/link-only choice, checksums and safe import; upgrade tests use same-ID fixtures/installs rather than claiming side-by-side packages test migration.

Exit: cross-workspace cold-reopen, process-loss, repeated handoff and provider-failure scenarios preserve all unaffected documents and original hashes. Only platform-specific observations genuinely requiring a handset are handed to Lance.

### R2 — Pro document and compositing foundation

- Stable layer tree and source references; group/ungroup, isolated/pass-through groups, clipping, named layers, locks, multiselect, align/distribute, ordering and group masks.
- Explicit layer/canvas/source/viewport transforms, alpha conventions, blend working spaces and effect order; legacy rendering remains compatible.
- Memory-bounded deterministic CPU tile/reference compositor with effect halos, bounded caches/proxies, render ownership and preview/final parity. GPU acceleration follows independently checked parity when measurements justify it.
- Canvas size versus image resize, crop, background/alpha and device-tier limits. Raise the current 12-layer/4096-edge limits only after actual capacity evidence, never by changing a constant alone.

Exit example: grouped children, clipped masked effect and independent cutout on transparency; correct group opacity and overlap; rearrange/Undo/save/process-restart/reopen/export with independently checked alpha and pixels.

### R3 — Complete Pro selection, paint, retouch and shape workflows

- Selections: rectangle/ellipse/freehand/polygon, contiguous/global color range and luminance, add/subtract/intersect, grow/shrink/invert/feather, saved selections/channels, linked/unlinked layer/group/filter masks and grayscale mask painting.
- Precision tools: nonuniform scale, skew/perspective/pivots, numeric entry, guides/rulers/snapping, cubic paths, shape joins/caps/dashes/booleans, gradients and fills. No Pro Text creation.
- Paint: flow/opacity/hardness/dab spacing, pressure and supported tilt with touch fallback, stabilization, texture brush rights, alpha eraser, deterministic replay and zoom-consistent behavior.
- Retouch: fixed/aligned clone; current/current-and-below/visible source policy; spot/heal/patch/inpaint with conventional math; dodge/burn, blur/sharpen/grain; editable sampling references and local non-destructive results.
- Ordered layer effects/styles, mask/filter scopes, shadows/strokes/bevel/displacement and conditional blend controls without a duplicated photography dock.
- Native workflow first; then a declared OpenRaster/PSD interchange subset, loss reporting, unsupported-record preservation and repeatable document actions. No universal PSD support claim.

Exit: every command produces the intended live image, targets the correct object, survives Undo/Redo/reopen, and independently exports correctly. Preserve old TEXT records without exposing new authoring.

### R4 — Offline Subject & Background and reshaping

Implement in Pro as one understandable family:
1. Manual/assisted subject trace and keep/remove marks; conventional segmentation with bounded cancellation and deterministic fallback.
2. Editable alpha mask, background removal and replacement by image/color/gradient; preserve original and linked subject transforms.
3. Refine hard/soft edges, feather, choke/expand, halo/color-fringe cleanup and restore mask; difficult hair/fur/glass cases have honest limits.
4. Small-object removal using conventional inpaint/clone/patch and seamless blending; no promise of reconstructing unseen detailed scenery.
5. Reversible mesh/liquify push/pull/expand/shrink/freeze/thaw, face/body region reshaping and material retexture/reskin with editable masks/transforms.
6. Dependency/license/performance evaluation before adding OpenCV or any alternative; no model download or runtime inference.

Exit: isolate/refine/move a complex subject onto a different background, remove a bounded object, reshape/retexture a chosen region, then reopen and export at qualified resolution. Check edge/mask geometry at multiple zoom levels and unchanged outside regions.

### R5 — Regular professional photographic processing

Do not introduce a Masks authoring UI; old saved masks remain compatible.
- Finish numerical behavior and full workflow coverage for existing exposure/tone, WB/color, HSL, curves, grading, Looks/custom presets, clipping/histogram and before/after.
- Define linear/encoded sample meaning, float reference processing, color/alpha transforms and legacy appearance adapter before precision claims.
- DNG first: explicit supported mosaic/linear/compression/opcode subset, sensor black/white/linearization, CFA phase, demosaic, WB/calibration, camera profiles and scene-linear exposure/highlight policy. Embedded JPEG is labeled preview. Broader vendor RAW follows rights-cleared camera samples and dependency review; do not bundle LibRaw by default.
- ICC/profile assignment versus conversion; sRGB and qualified wide-gamut SDR first, proof/gamut warnings, numeric samples, output intent and truthful 16-bit PNG/TIFF. HDR display/export only as separately qualified variants.
- Manual/profile optics: lens distortion, lateral CA, vignetting, perspective/upright/straighten; qualified conventional luminance/chroma denoise, sharpening and output sharpening.
- Conventional exposure-bracket HDR merge, panorama stitching and focus stacking after single-image geometry/color; retain input/transform lineage and inspectable correction policy.

Exit: synthetic RAW samples, chart/neutral/ramps/optical grids and rights-cleared camera negatives agree with independent references; output samples/profile tags prove precision. Unsupported variants report a precise limitation without source loss.

### R6 — Regular organization, batch and complete everyday creativity

- Transactional local catalog: folders versus collections, ratings/flags/favorites/labels, tags/EXIF/IPTC/XMP, rules-based search/smart collections, duplicate candidates with byte confirmation, stacks and virtual copies.
- Non-destructive sidecars and backup/restore, targeted relink, source/copy identity, selective settings copy/paste/sync with incompatible source-anchored edits excluded explicitly.
- Durable batch jobs: frozen per-item revision/recipe/destination, multiple outputs, naming, profile/metadata choices, per-item progress/receipts, cancellation and idempotent interruption recovery.
- Complete Regular Unicode text/shaping/font workflow, draw/shapes/frames/borders and independent decorative objects. Rights review for fonts and assets.
- Add deferred Stickers after repair stabilization: at least 500 finished usable original/licensed local assets; planning catalog has 648 concepts across 27 categories, not finished art. Categories/search/favorites/recents; custom transparent PNG/WebP; move/scale/rotate/flip/opacity/order/duplicate/delete; Undo/reopen/export and selected-tile use. Preserve existing tools and avoid a Pro sticker browser.

Exit: 100-file catalog/recovery exercise and interrupted batch do not duplicate completed outputs or overwrite originals. Text/draw/shape/sticker/privacy objects remain independent through crop, reopen and export.

### R7 — Complete GIF and Collage scope

GIF:
- Broaden real media and lifecycle coverage: VFR/B-frames/rotations/non-square pixels/disposal/transparency/finite loops/HDR-to-SDR policy, source grant loss and relink.
- Measure source-open, edit-to-first-correct-still, scrub, first moving frame, sustained presented cadence, reverse, full Save and copy separately. Preserve current portrait and rotated 720p60 gates.
- Add explicit frame/segment assembly, split/remove/duplicate/reorder, per-frame holds, ping-pong endpoint rules, keyed captions/overlays, palette/dither choices and encoded appearance preview.
- Qualify new photo-sequence creation separately from compatibility with existing saved sequences.
- Legacy v4 anamorphic migration: source reinspection, reversible versioned upgrade with before/after preview, preserve old framing and manual aspect intent until adopted; missing source cannot trigger guessed migration.

Collage:
- Preserve all seven layout families and 1–20 independent tiles; layout/aspect/gap/margin/border/rounding/background/fit/fill; same-URI identity; selected-tile full Regular features.
- Qualify relink versus replacement, selection/history/Done/Cancel/Back, old projects, all 20 tile outputs, alpha/orientation/memory and interrupted export.

Exit: declared source/format matrix, full editable-project recovery and external animation/image outputs match required content and timing; physical performance documented. A passing small fixture is not a universal throughput claim.

### R8 — Pre-Video acceptance and formal owner locks

Close every required pre-Video inventory row or obtain a specific owner scope decision; no silent omission. Complete representative phone/OEM/codec/memory tiers, accessibility, novice and expert tasks, prolonged thermal/storage/provider tests and repeated editing/reopen/export.

Give Lance one coherent verified candidate and focused handset script. Repair his findings. Record his formal acceptance of Regular, Collage, Pro and GIF separately. CI success alone cannot open Video. Continue independent authorized pre-Video work while only a genuinely device-specific observation is pending.

### R9 — Separate professional Video workspace, after R8

1. Versioned video project, source/track metadata, stable clip IDs, timeline/timebase, playback/seek, proxies, transactional edits, trim and real H.264/AAC MP4 export.
2. Assembly: split/join/reorder/duplicate, source/sequence In/Out, append/insert/overwrite, ripple/lift/extract/roll/slip/slide, snapping/markers/frame nudge, linked/unlinked sound, J/L cuts, track targeting/locks/sync.
3. Audio: imported local audio/video streams and voiceover, waveforms, clip/track/master gain, mute/solo/range mute, fades/pan/EQ/dynamics/limiter, conventional non-AI noise reduction, automation, ducking, buses/stems and channel/sample-rate policy. No bundled unlicensed music.
4. Compositing: multitrack video, PiP, keyframed transforms/opacity, captions/titles, masks/chroma/luma key, speed ramps/reverse/freeze, transitions and conventional stabilization; later nested/compound/multicamera within the same finite studio scope. Photo-plus-audio slideshow authoring includes per-photo duration, deterministic pan/zoom and optional beat-sync.
5. Color/effects: shared original Looks and selected-range grades, scopes, bypass/copy settings, qualified LUT import, explicit log/SDR/HDR transforms and preview/final parity.
6. Timed blur/pixelation/opaque redaction with editable keyframes and optional deterministic tracking; review uncertain intervals/cuts. Re-encode sensitive removals and inspect every affected frame/stream/thumbnail; edit-list-only hiding is insufficient.
7. Mastering: local bins, metadata, full-resolution conform, sequence versions, full/range/audio-only exports, subtitles/burn-in/sidecars, declared interchange subset, portable archive/relink.
8. Initial qualified SDR preset, then measured 1080p/4K/HEVC/HDR/device tiers. Pin and verify Media3 or other dependencies then; older references to framework limitations are not current implementation guarantees.

Exit: multitrack project with precision edits, user audio, transitions, captions and moving redaction survives process loss and independently decoded MP4 agrees in frames/audio/timestamps/color. A photo-plus-music slideshow preserves declared per-photo durations and pan/zoom in a real MP4; optional beat-sync aligns the declared events. Audio-cleanup fixtures measure noise attenuation and retained desired signal. GIF remains silent.

### R10 — Full editing qualification, then final customer onboarding

First qualify all five workspaces and shared interactions, formats, migration, accessibility and performance on the final editing build. After owner editing acceptance, implement complete English/Spanish localization, welcome/language selection, contextual help, final privacy/terms/license documents, acceptance/decline and local access flow as specified. Do not invent an in-app purchase/unlock product.

Re-run editing plus fresh-install/upgrade/offline/decline/returning-user flows in both languages, large fonts and TalkBack. Third-party license diligence is continuous during engineering, separate from the deferred consumer legal screens.

### R11 — Website dependency, Play Console and release

This stage waits for the fully functioning qualified app AND Lance's Greenezone website completion.
- Owner establishes the appropriate verified Play Console account, organization identity/payment profile and contacts; determine applicable testing requirements from that actual account. No account creation now.
- Recheck current Google Play policies at submission: target API, supported devices/adaptive behavior, permissions, Data safety/privacy URL, content rating/target audience, ads declaration, paid listing/territories and support contacts. Offline does not exempt the app from accurate disclosures.
- Complete actual dependencies/transitives/assets/fonts/music/profiles SBOM and shipped notices; audit the present packaging exclusions before trusting notices. Review rights/trademarks/material codec or patent questions for shipped features.
- Secure production upload key, Play App Signing and signed AAB; protect key custody outside repository. Test actual generated/installed splits, same-ID upgrade/project migration, release build behavior, ABI and 16 KB page compatibility if native libraries are added.
- Verify store copy/screenshots/video against actual finished features; no blanket AAA/Photoshop parity claim. Run required internal/closed testing and pre-launch checks; account-specific production-access gate where applicable.
- Lance reviews exact release candidate, price/listing and authorizes public publication. Define support/update/rollback/migration response and monitor platform-provided quality signals without adding prohibited in-app telemetry.

Policy reference snapshot read 2026-11-24: [target API](https://support.google.com/googleplay/android-developer/answer/11926878), [new personal-account testing](https://support.google.com/googleplay/android-developer/answer/14151465), [Data safety](https://support.google.com/googleplay/android-developer/answer/10787469), [signing](https://developer.android.com/studio/publish/app-signing), [app setup](https://support.google.com/googleplay/android-developer/answer/9859152), [organization details](https://support.google.com/googleplay/android-developer/answer/13628312). These links must be rechecked at R11; this snapshot is not account verification or permission to begin setup.

## Meaning of professional / AAA quality

This is an acceptance standard, not a certification or an unsupported marketing adjective.

A required feature is accepted only when: correct mode and understandable entry → responsive real edit → correct target/pixels/timing → coherent Undo/Redo and cancel → safe save/cold-reopen → independent export → fault recovery/source protection → accessibility/device qualification → owner acceptance where required. Record build, fixture, device, expected result, actual evidence and limitations separately.

Release blockers include reproducible data loss, source overwrite, wrong-owner changes, privacy exposure, false save success, required migration failure, crash/ANR in supported workloads, missing promised workflow, or unsupported claims. Lower-severity findings require an explicit disposition; they are not erased by a test count.

Performance goals from older blueprints conflict in places and are **provisional**. Calibrate one named device/workload table in R1/R8 rather than adopting whichever number is easiest. Retain already-established CI budgets unchanged unless a documented product decision and evidence justify revision. Record cold versus warm, input-to-correct-pixels, sustained cadence, median/p95, memory/temp storage and thermal behavior. Cover 12/24 MP and declared larger tiers, 20-tile Collage, long gestures, layered Pro and representative media. Include API29 and35 automated lanes plus target/current-platform compatibility before release.

## Execution controls that prevent drift and repetition

1. Select the first unblocked ledger item; state its user-visible result, exact baseline, owner, affected files, reproduction, exit criteria and next decisive observation before editing.
2. Use parallel agents for distinct implementation or independent review. One owner per shared file; root integrates reviewed blobs. Keep all repository engineering remote; no local clone/mirror.
3. Preserve the existing evidence and corrected behavior. Reopen a passed item only for changed dependencies, new failure, materially weaker evidence or an owner report. State the reason.
4. A failed run triggers diagnosis of exact failure/phase, one causal hypothesis and a bounded correction/experiment. Do not retry an unchanged application failure or widen a timeout until green. A justified infrastructure retry keeps the original result and rationale.
5. Run appropriate tests once per changed candidate. Full API29/API35 and JVM at the final candidate and merge remain required; do not rerun settled receipt parsing or rebuild documentation-only changes to manufacture progress.
6. UI source presence, a helper probe, fixture success, handset success and owner acceptance are distinct statuses. Do not call whole families complete from a button, compile or narrow synthetic test.
7. Keep a single active ledger and this roadmap. Update status/evidence in place; retain earlier runs as history. Do not create another competing master plan or let handoffs revive stale commands.
8. If work drifts, identify the mismatch against the selected item, stop that extra change, reconcile the ledger, and continue the correct next action. Do not restart intake or replay completed repairs.
9. Missing product intent prompts a specific question. Missing technical detail is investigated by the engineer. While waiting, continue independent authorized work. No guessed loss of scope, new public release, or premature Video/onboarding work.
10. Every handoff states exact completed behavior, evidence, remaining work and next action. An enabling diagnostic is not the delivered repair. No numeric completion percentage without a defined denominator.
11. Keep original intake inputs until the work they govern is completed and accepted; do not archive a full blueprint after one repair wave.
12. Public launch and formal locks remain explicit owner decisions. Routine verified engineering does not need repeated permission.

## Requirement provenance and unresolved product decisions

Reconciled sources: original full blueprint revision 2; revision3.1 master sections 4–11 and feature/sticker audits; revision4 owner overrides; Subject & Background addendum; full 201-message source transcript; repository PRODUCT_SPEC/UNIFIED_PRODUCT_SCOPE_09/PROFESSIONAL_ENGINEERING_ROADMAP and field guide; prior Audit 2 and the supplied independent PR #26 review. Private screenshots/original archives remain in the supplemental intake and are not published here.

Settled overrides: Regular has no new Masks authoring; Pro has no Text authoring; actual edited Regular→Pro Yes/No prompt and retained Regular draft; stickers deferred during repairs; Video waits for four formal locks; consumer onboarding follows all editing; Play Console setup follows working app and website. Keep these in one place.

Still requiring a later named decision: exact shipping device/performance/RAW-camera/interchange tiers; specialized CMYK/spot/prepress, photo books, tethering/plugins or vendor ecosystem support if desired; final distribution territories/support/legal choices. These are not silent additions or omissions from the enumerated core. Record scope changes explicitly with Lance.
````

### A3 — docs/WORK_LEDGER.md

[Complete pinned original](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e04bc08a4800233b0f407b419a91c97ca2c068f6/docs/WORK_LEDGER.md) · Git blob `6a3cfd4abca841491e480b1b5df93c2b7a6bce5a`.

````markdown
# GZ Editor Pro — single active work ledger

Updated 2026-11-24. Read with [roadmap](GZ_ENGINEERING_ROADMAP.md), [capability inventory](CAPABILITY_MATRIX.md) and [release gates](RELEASE_GATE.md). This ledger controls execution order; the linked engineering records control run results. Historical green counts do not close newly reported defects.

## Current state — R0 engineering verified; R1 next

[PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29), app merge `c9efc5aa0673f21b98f150733582a24996b34d10`, candidate `6ad22e3fabe3964a662c7e0e39bd08c4a8e8f9ca`, [candidate workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45352950946) and [merged-main workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45355074845). Both source trees match, and each full candidate/merge Android lane passed 186/186 with zero failures/errors/skips. Verify/JVM/assembly/signature checks passed. The exact exercised phone APK and its hash/package/certificate are in the PR execution receipt.

The four review defects are corrected. Additional qualification repairs cover broad late-corner sampling, bounded legacy Pro reading, and the observed Compose test scheduler issue. These results do not close handset acceptance, full feature families, smallest-heap qualification or commercial release.

**Active next item: R1.1 — bounded saved-project listing and loading.** Inventory current state/source ownership once, then load lightweight project metadata and decode only the selected recipe. Qualify a full twelve-save set containing near-budget recipes, failed/corrupt entries, correct selected identity and unchanged saved bytes, with measured memory. Preserve the existing saved projects through any required storage migration. Pin the baseline, affected files and decisive fault experiment before editing; do not restart blueprint intake.

**Following item R1.2 — recoverable Regular working drafts.** Persist completed edits atomically, keep transient gestures out of checkpoints, retain source identity/grants and restore the selected work after genuine process loss. Carry the same contracts to Pro/Collage/GIF in subsequent R1 slices.

## Completed wave R0 — independent PR #26 corrections

**Owner:** root integration, with distinct Regular gesture, storage and Pro selection owners. **App baseline:** `372e0e947caacb2ceaf603bc4ff5a60e44cc33ec`. **Intake main:** `d2a8f4de41839e25ad54060be803746d7307bee8` (documentation-only difference). The owner supplied the review and authorized engineering. The former successor read-only hold is lifted.

| Item | Required behavior | Production scope | Decisive evidence |
| --- | --- | --- | --- |
| D01 | A newly drawn privacy cover remains over the same source pixels during the first manual Crop; held gesture survives recomposition | PhotoEditorScreen preview identity and crop anchoring | Actual held/quick pointer flow, Undo/Redo, reopen, PNG and selected-tile isolation |
| D02 | Oversized Regular save/export rejects before storage mutation or destination picker; healthy save and dirty work survive | ProjectStore; PhotoExportController; actionable save error | Allowed dense draw fixture, last healthy bytes unchanged, readable smaller recipe; actual Save-before-close/replace remains open |
| D03 | Long Regular Draw retains late bends and endpoint within budget; cancellation makes no edit | CreativeCanvasOverlays; PhotoEditorScreen; bounded sampler | Actual >500-sample Regular/tile gestures, saved/exported pixels, cancellation and history |
| D04 | Long Pro lasso represents the full contour rather than a truncated polygon | ProStrokeSampling; ProWorkspaceScreen | Actual >450-sample lasso, late-region applied mask pixels, Undo/Redo/save/reopen; cancellation restores prior selection |

**Test-only baseline:** [commit 018b45c](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/018b45c4b1af0beb867abfe0261f5bce9e4f54e1), [workflow 45348806687](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45348806687). Three new Android test classes add ten cases against unchanged production. Keep actual failures, including any harness defect, distinct from intended product reproductions.

**Corrected candidate / final result:** [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29), [repair branch](https://github.com/MrSkyrimFan/GZ-Mobile-001/tree/feature/pr26-review-corrections) and its pull request, [branch workflows](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions?query=branch%3Afeature%2Fpr26-review-corrections). The PR description is the execution receipt for exact corrected commit, red/green case disposition, full candidate/merge jobs, independent review, APK identity and remaining owner checks. The PR now carries the completed execution receipt; this ledger records the resulting state. Use its exact qualified artifact, not an earlier candidate download.

### Qualification findings retained with their causes

- Initial unchanged-app run 45348806687: the ten new cases reproduced the reported defects. Gesture/lasso cases reached their intended assertions on both APIs; storage cases reached false-success assertions on API 35 and exhausted memory inside the unbounded production encoder on API 29. Android 35 later aborted in a separate existing Collage v1 test-rule scheduler failure; Android 29 also exposed a legacy Pro read allocation failure. Do not describe that run as a clean full-suite reproduction.
- Initial correction run 45349823052 at `a3cf1b66e38034d41d51e35dce31ad2cdf6d9108`: both APIs completed the identical 184-case set, 182 passed/2 failed/0 skipped. All 174 prior cases passed. The two failures retained the endpoint but lost a broad late Draw corner; the assertions were not relaxed.
- Follow-up uses area-aware bounded sampling with explicit reversal protection, keeping the existing 450/500 caps. It also migrates only the observed Collage test and new Regular gesture fixture to the standard v2 scheduler.
- The baseline legacy Pro failure occurred in JSON unescaping: its 7,591,944-byte transient allocation exceeded the 16 MiB heap observed in that run. The merged read optimization reduces copies while preserving format, file/recipe limits, validation, backup behavior and full recovery equality. Later larger-heap success is not evidence that this memory failure disappeared.
- Final changed-candidate and merged-application results, exact artifact identity and any further diagnostic disposition belong in [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29). Preserve all earlier failures. These bounded qualification repairs do not close the broader R1 durability/memory package.

**State transition completed:** the PR receipt closes R0 engineering. Proceed to R1. Reopen an R0 behavior only for a named new failure, changed dependency, weaker evidence or owner report. Handset acceptance remains separately open.

**Preserve:** all original 174 Android cases, established GIF performance/fidelity budgets, mode boundaries, named project identity, immutable picker recipes, all source files and independent Collage tiles. New version 0.12.9 / code 21 isolates the next phone test from prior builds; separate packages do not prove same-ID upgrade behavior.

## Ordered remaining work

| Order | Package / completion target | Prerequisite and current disposition |
| --- | --- | --- |
| R1 | Durable documents, lazy project metadata loading, process-loss recovery, source/provider custody, Back/history, saved repeated exchange and portable bundles | ACTIVE NEXT — begin R1.1 above; required families S01–S08 remain open/partial |
| R2 | Pro layer tree/groups/clipping and memory-bounded reference compositor | R1 contracts; preserve legacy pixels |
| R3 | Full Pro selection, paint, retouch, shapes, transforms, effects and declared interchange | R2 foundation |
| R4 | Offline Subject & Background, refine/removal and reshaping | R2/R3 mask/geometry foundation; no runtime AI |
| R5 | Regular photographic development, RAW/color/optics and multi-image processing | R1/R2 shared custody/color/geometry; preserve Regular without Masks authoring |
| R6 | Catalog, metadata/virtual copies, durable batch, text/creativity and at least 500 finished stickers | R1 plus qualified processing; stickers no longer omitted after repair stabilization |
| R7 | Full GIF frame/timeline/palette workflow, reversible legacy v4 aspect upgrade and complete Collage qualification | Shared foundations and selected-tile features |
| R8 | Pre-Video device/accessibility/performance qualification and four explicit owner locks | Every required pre-Video family accepted or specifically dispositioned by Lance |
| R9 | Separate complete professional Video workspace | Blocked on all four owner locks, never on CI count alone |
| R10 | Full five-workspace editing acceptance, then English/Spanish welcome/help/legal/acceptance | All editing completed/accepted; consumer onboarding stays last among app features |
| R11 | Finished website, Play Console/account, signed AAB/store testing and authorized release | Fully functioning qualified app AND Greenezone website complete; account not set up |

Subdivide a selected package into bounded deliverables with affected files and observable exits before implementation. Do not treat these family rows as single giant commits, invent completion percentages, silently remove difficult capabilities, or create another master backlog.

## Next action — execute the selected R1.1

Begin the bounded saved-project listing/loading slice already selected above. Pin app baseline `c9efc5aa0673f21b98f150733582a24996b34d10`, owner, affected files, expected visible result and the decisive full-list/selected-load memory experiment before editing. Complete that slice with saved-byte preservation and selected-document identity evidence; R1.2 recoverable Regular drafts follows. Do not reopen item selection or restart blueprint intake without a named new dependency, failure or owner direction.

## Pending owner/device decisions

- Qualified v0.12.9 Test handset wave (owner acceptance still open): fresh-cover Crop, long Draw, Pro lasso, dense Save rejection, prior portrait GIF workflow and physical Back/picker/provider checks. Use the exact APK and focused script in the PR execution receipt.
- Four formal pre-Video workspace locks remain open; a limited successful phone session does not close the full product inventory.
- Shipping device/performance, RAW-camera and interchange support tiers are explicit later decisions, informed by implementation evidence.
- Greenezone website completion is an external dependency, not app-engineering scope. Play Console setup waits for that plus the functioning app; no account work now.
- Exact production signing/account/listing and public release need Lance at R11. Routine authorized engineering continues without repeated permission.

## Failure and drift rule

For a failed check, record the exact symptom and phase, causal hypothesis and bounded next correction. Do not retry unchanged application failures, relax an established budget for green, replay passed intake, or call a helper result full workflow acceptance. Reopen settled work only for a new failure, relevant dependency change, weaker evidence or an owner report, with the reason recorded.
````

### A4 — docs/CAPABILITY_MATRIX.md

[Complete pinned original](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e04bc08a4800233b0f407b419a91c97ca2c068f6/docs/CAPABILITY_MATRIX.md) · Git blob `282bc8b71f30ab46397c8b61659e856c24f7a1e0`.

````markdown
# GZ Editor Pro — capability and evidence inventory

Updated 2026-11-24 after qualified app merge `c9efc5aa0673f21b98f150733582a24996b34d10` (PR #29). The full scope inventory originated against `d2a8f4de41839e25ad54060be803746d7307bee8`; R0 evidence is now advanced below. See [roadmap](GZ_ENGINEERING_ROADMAP.md) for complete behavior and dependencies and [work ledger](WORK_LEDGER.md) for active progress. This is a finite family inventory, not a claim that every reachable UI control has already been exhaustively qualified.

**Statuses:** ENGINEERING VERIFIED = stated bounded workflows pass candidate/merge checks, with owner/device gates separate; DEFECT = confirmed review finding, correction required; PARTIAL = implemented foundation and/or bounded fixture evidence, full listed outcome still open; OPEN = required work/evidence not delivered, not a blanket assertion that no helper exists; LIMITATION = known compatibility limit; GATED = implementation/setup waits for named prerequisite; OWNER GATE/DEPENDENCY = explicit external decision. No row is owner-accepted merely because tests pass. R0 closes only its listed corrections; no other family inherits completion from that result.

| ID | Workspace | Capability | Current status | Package | Required exit / remaining work | Requirement/evidence source |
| --- | --- | --- | --- | --- | --- | --- |
| D01 | Regular / tile | Fresh cover then manual crop | ENGINEERING VERIFIED | R0 | Held/quick crop keeps anchor and pointer; Undo/reopen/export protect same pixels | PR #26 review §1; PR #29 candidate/main 186-per-API evidence |
| D02 | Regular | Readable bounded named saves and export requests | ENGINEERING VERIFIED | R0 | Reject oversized before mutation/picker; healthy bytes and dirty draft survive close/replace | PR #26 review §2; PR #29 candidate/main 186-per-API evidence |
| D03 | Regular / tile | Complete long Draw and cancel | ENGINEERING VERIFIED | R0 | Late bends/end within 500 samples; cancel Draw/erase restores history and output | PR #26 review §3; PR #29 candidate/main 186-per-API evidence |
| D04 | Pro | Complete long lasso and cancel | ENGINEERING VERIFIED | R0 | Late contour selected correctly within 450 budget; operation/history/reopen | PR #26 review §4; PR #29 candidate/main 186-per-API evidence |
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
````

### A5 — docs/PRODUCT_SPEC.md

[Complete pinned original](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e04bc08a4800233b0f407b419a91c97ca2c068f6/docs/PRODUCT_SPEC.md) · Git blob `01cca7577f82877a62aa8a3e44887ee9770777b2`.

````markdown
# GZ Editor Pro product requirements

## Current scope

The November 23 audit and PR #26 evidence record the prior application baseline. Lance supplied the independent follow-up review on November 24 and authorized corrections and the complete professional-app roadmap. The former owner-review hold is lifted; the four corrections are now engineering-verified in [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29), with 186/186 Android cases per platform on candidate and merge. Qualified v0.12.9 Test handset acceptance remains open, and R1 durability is next. [Active roadmap](GZ_ENGINEERING_ROADMAP.md) · [Capability inventory](CAPABILITY_MATRIX.md) · [Work ledger](WORK_LEDGER.md) · [Continuity](CONTINUITY.md) · [Release gates](RELEASE_GATE.md).

- **Regular and Collage.** Preserve the familiar photo-first interface, Text, Draw, Shapes, Blur & cover, and independent edits across all 20 Collage tiles. Regular and the selected-tile editor have no visible Masks authoring. Keep saved mask data, rendering and round-trip compatibility. Give all four Crop corners and all four edges density-aware touch targets of at least 48 dp and select the drag target on pointer down. Preserve predictable Freeform and fixed-ratio geometry and exact exported crops.
- **Regular → Pro.** From a loaded standalone Regular photo, selecting Photo editing Pro presents a choice to continue the current image with its edits. Yes opens the current edited result in Pro, preserving its orientation, crop and appearance. No opens an empty Pro editor with “Select an image to edit.” Dismissing the choice leaves Regular open. Regular keeps its own source, edits and history when the user switches back, and stays independent of any Pro changes. Saving, deleting or opening a new Regular image follows the normal Regular commands. A failed handoff keeps the Regular work safe and explains the failure.
- **Pro.** Masks, selections, layers, canvas tools and retouching belong here. Pro has no Text creation or editing. Preserve and render legacy Pro Text layers and keep Shapes accessible. Provide File → Delete current work with an explicit discard warning and confirmation. Cancel keeps the current workspace intact. Confirmation clears the current working edits and history and returns to the original or blank starting state. Preserve source media and named saved projects. Keep the existing Undo/Redo controls. Back closes the active transient context first and then returns to the correct workspace with recoverable work. Make controls meaningful for the selected layer and explain blend behavior and targets without changing correct blend math.
- **GIF.** Playback must visibly advance and stay responsive after trim, Reverse, Looks, speed, canvas and other edits. Verify preview, scrubbing, project reopen and actual animated export on representative media. Use the owner's portrait workflow as a reproduction target. A moving frame number alone cannot demonstrate playback.
- **Later pre-Video scope.** Regular gets an offline sticker menu with at least 500 usable finished assets, transparent custom PNG/WebP imports, editing and persistence, and independent sticker editing on selected Collage tiles. Pro gets the offline Subject & Background tools for subject selection, background removal or replacement, object removal, edge refinement, and reshaping, with editable masks/layers and full-resolution export. See the [complete roadmap](GZ_ENGINEERING_ROADMAP.md).
- **Complete scope.** The roadmap and capability inventory include required RAW/color/catalog/batch, full Pro/Subject & Background, GIF/Collage, Video, reliability and release families. Present buttons and narrow passing fixtures are foundations, not completion of the full professional scope.
- **Account timing.** Play Console is not set up. Lance requires setup last, after a fully functioning app and completion of the Greenezone website. Website implementation is outside this task.
- **Release decisions.** Regular and Collage retain their protected provisional status. GIF and Pro remain open for engineering and handset qualification. The separate Video editor stays disabled until the owner formally locks all four pre-Video workspaces. Bilingual English/Spanish welcome and legal acceptance belong to the final phase. The app stays offline with a one-time purchase and no runtime AI.

## Retained photo and Collage requirements from 0.8

- Preserve the user-approved photo-first dark UI and the familiar position of working Regular tools.
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
- One phone session exercises the consolidated current repair. Include the owner’s GIF workflow, Regular crop and privacy, named Pro and GIF projects, Collage tile edits and relink, and physical Back and picker behavior.
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
````

### A6 — docs/RELEASE_GATE.md

[Complete pinned original](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e04bc08a4800233b0f407b419a91c97ca2c068f6/docs/RELEASE_GATE.md) · Git blob `7f40ef05836b48def7263f23dc383e4917a0da4c`.

````markdown
# GZ Editor Pro engineering and release gates

## Current independent-review correction gate (2026-11-24)

- [x] R0 fresh-cover/manual-Crop, bounded Regular save/export, complete long Draw/cancel and Pro lasso have regression evidence against actual flows.
- [x] Exact corrected candidate and merged application pass JVM and full API29/API35; preserve prior 174 cases and established GIF budgets. Independently review failure receipts and output assertions.
- [x] Qualified exercised v0.12.9/code 21 phone APK has source/run/artifact/hash/package/signature recorded in the repair PR. This engineering gate is complete; owner phone acceptance remains below.
- [ ] Owner completes one combined phone pass with the focused new cases plus retained field cases below. Physical acceptance remains separate from CI.
- [ ] All required families in the [capability inventory](CAPABILITY_MATRIX.md) complete the [roadmap](GZ_ENGINEERING_ROADMAP.md); a repaired current build is not the entire professional app.
- [ ] Lance formally locks all four pre-Video workspaces before Video implementation.
- [ ] All five editing workspaces complete/accepted before final English/Spanish welcome and customer legal acceptance.
- [ ] Fully functioning qualified app AND Greenezone website complete before Play Console setup. Account is not set up; website work is outside this repository task.
- [ ] At that final stage: verify actual account-specific requirements/current policies, rights/notices, release signing/AAB/splits/same-ID upgrade, store testing/listing and explicit owner publication approval.

[Single work ledger and current execution receipts](WORK_LEDGER.md). Qualified app merge `c9efc5aa0673f21b98f150733582a24996b34d10`: candidate and merged-main each pass Verify and 186/186 on both API29/API35; exact APK identity is in [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29). The earlier successor review hold is lifted by the supplied review and owner's engineering request. Existing evidence below retains its original scope/date; it does not close new defects or full release.

## Retained field-correction acceptance

- [ ] Open a rights-cleared 1080×1920 portrait video of approximately five seconds from the GIF workspace. Compare untouched playback with trim around 2.19–4.99 seconds, Reverse, B&W or Punch, speed, crop, Undo/Redo and rapid edits. Confirm changing picture content, correct direction and responsive controls. Reopen a saved project and decode the exported animation independently to check orientation, motion, color, duration and frame schedule. Include 720p60 B-frames and representative phone media.
- [ ] Enter Regular Crop through the normal UI. Drag four corners, four edges and the interior in Freeform and fixed ratio with touch input. Verify predictable anchoring, pointer-down acquisition, letterbox interactions, expected crop pixels and saved-project reopen. Confirm Regular and Collage tile editing omit Masks authoring and retain Text, Blur & cover and old mask-bearing projects.
- [ ] Enter Pro from an edited portrait in Regular. Confirm Yes transfers the current appearance and geometry. Confirm No opens an empty editor and dismissal keeps Regular open. Switch back via the menu and Back, and verify Regular's source, edits, history and output still match. Check Pro Text authoring is absent and legacy text still renders, and exercise File → Delete current work with Cancel and Confirm. Verify originals, Regular edits and named saved projects survive.
- [ ] Exercise Pro layer selection, transform, masks, opacity/blend, painting and repair through pixel change, Undo/Redo, save/reopen and export. Check Back through active tools, menus, dialogs, keyboard and workspaces. Verify physical gestures from both edges, half-cancel and rapid double Back on a handset, including picker/IME ownership.
- [x] Audit 2 engineering gate for v0.12.8 Test. The exact candidate and merged application commit passed JVM verification and 174 of 174 instrumented tests on Android 10 and on Android 15. The emulator-exercised APK identity and hash are recorded in the repair evidence. Owner phone checks above remain open. [Repair evidence](AUDIT_2_REPAIR_VERIFICATION.md) · [Candidate workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45337105839) · [Merged-main workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45339000608) · [APK artifact](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45339000608/artifacts/14895803102).

### November 23 independent audit cases

Use the [audit repair evidence index](AUDIT_2_REPAIR_VERIFICATION.md) and [PR #26](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/26) for the exact candidate and merged application commit, receipt archives, and APK identity.

- Exercise default Save across named project opens and Undo/Redo in Pro and GIF. Check confirmed replacement and deletion, and verify old and newly written editable projects after reopen. Reject a Pro recipe that exceeds its reader budget while preserving the previous save.
- Follow protected source pixels through flips, quarter turns, straightening, crop and reopen. Check decoded exported pixels for Regular and a selected Collage tile. Verify named deletion, dirty-photo replacement, duplicate-source tile movement, relink and staged storage failure.
- Confirm a delayed Android picker result can write the original immutable recipe after an Activity recreation. Check cancellation and duplicate delivery, and verify retry to a fresh destination after an ambiguous write. Check real process reclamation with a PID change and a separate picker on a handset.
- Verify video GIF crop, aspect, timeline, delays, export time, reopen and playback with independent media decoders while keeping normal-resolution baseline performance. Check both Android 10 and 15.
- Physical Back and provider-specific behavior remain handset acceptance cases. Exercise both edges, canceled gestures, picker and keyboard ownership, source grant loss, and a provider with aliases or a partial write.

### Regression cases found after Revision 4

- In Pro, check brush opacity and masking against an occluding image while the pointer is still down. Check that live erasing reveals the lower layer or transparency and that a second pointer cancels without an Undo entry. Verify committed paint and erase through raster output and Undo/Redo.
- In Regular, open Histogram from More. Check RGB and luminance bins, clipped channel counts, a visible Exposure edit, and Before view. Keep the measurement on displayed preview pixels so its precision and scope match the app's current sRGB pipeline.
- Measure the 1080×1920 edited portrait fixture through the Save GIF command at 180×320 with the complete 88-frame schedule. In the automated test, save within 15 seconds, decode every frame delay and both reverse endpoints, and play the saved file after reopening. Retain the six-second edited preview check.
- On each Android runner, compare the production export of the rotated 720p60 fixture to the per-timestamp reference export. Keep the same canvas size, duration, frame schedule and moving-pixel checks, and require the production path to finish in half the reference time or less.
- Check a small crop's edge midpoint and interior independently of its corners. Exercise locked ratios on wide and tall photos, the first gesture after reopening Crop, and off-center positioning in older saved projects.
- Open a different named Pro project, Undo and Redo across that boundary, and verify Delete current work returns to the correct baseline. Confirm the separate named-delete prompts before deleting a save. Reorder a layer, Undo and Redo, and verify subsequent tools still target it. Exercise a pending tool draft when another layer's media becomes unavailable.
- Widen both ends of a long trim at a fixed frame rate. Keep the selected FPS and stop at its exact frame-budget limit. Cover unspecified decoder color tags, edited playback of metadata-rotated clips, failed-codec fallback and actual edited export.
- Move a square crop off-center, apply a Look, hand it into Pro, and verify the rendered crop, saved edit recipe, and intact Regular draft.

## Historical v0.8 Collage and photo verification criteria
- [ ] Main original photo edit screen unchanged aside from GZ brand on landing and Create collage entry.
- [ ] Crop/blur/text/shapes/brush active guides persist while main toolbar is hidden, and editing remains possible.
- [ ] Pure layout checks: all seven arrangements, counts 1–20, nonoverlap, bounds, tap selection, max spacing and budget.
- [ ] Android bitmap render checks: 2 independent edits, exactly 20 tiles, no source overwrite, PNG/JPEG image output.
- [ ] Local collage project encode/decode/save/load with distinct per-tile color, privacy and creative layers.
- [ ] Collage Undo/Redo restores layout, tile order and per-tile edits, with bounded snapshots and one checkpoint per slider gesture.
- [ ] Missing photo raises tile-specific error and offers replacement rather than silently omitting content.
- [ ] Import count hard capped at twenty, including Android picker fallback.
- [ ] Collage exports via existing seven real file encoders/social sizing choices; no AI, ads, network or watermarks.
- [ ] All prior JVM, Android raster and Compose tests continue passing.
- [ ] PR head CI pass, exact SHA merge, merged-main CI pass, verified debug APK artifact.

## One phone field pass
- [ ] Add 2 photos side by side; edit each tile independently in full editor, return with Done.
- [ ] Add 6–20 photos (as many as convenient), try Grid/Mosaic/Featured templates and aspect, spacing, margin/background.
- [ ] Reorder, remove, replace; save named collage, reopen and verify per-tile adjustments.
- [ ] Export JPEG and PNG, check distinct photos/content preserved, no watermarks.
- [ ] In regular editor, hide tools while editing crop/blur/text/brush; selected handles must stay present.
- [ ] Check original standalone photo editor, saved projects, privacy black bars and JPEG export still work.

## Commercial gate remains open
Native app profiling, large-photo/20-tile memory pressure, long session/ANR, color accuracy, Android API/device coverage, media URI grant lifetime, trademark/patent/license/SBOM and privacy/Play/store legal checks, production signed AAB. No final 1.0 claim.

Refer to `docs/GZ_ENGINEERING_ROADMAP.md` for lawful original-code Lightroom-class **non-AI** features, RAW/license decisions and later video editing.

## Historical v0.9 verification criteria
- [ ] Top-right mode dropdown on Regular, Pro, GIF and Collage. Video disabled / Coming soon. Regular toolbar compact, no destroyed existing tools.
- [ ] Pro image compositor: independent image edits, blend/position/size/opacity/visibility/order/undo/persist/export.
- [ ] Real GIF89a multi-frame output with tested Android decoder, timing/loop/ordering, saved project/undo and safe source ownership.
- [ ] No new permission, dependency, AI, ad, account, watermark or network regressions.
- [ ] Exact PR HEAD passed CI, merged main Android bitmap/Compose tests passed, expected APK artifact exists.
- [ ] Historical 0.8 and 0.9 physical phone check `PHONE_TEST_WAVE8.md`; commercial release and professional/RAW/video work remain open.

## Historical v0.12.1 independent AAA audit criteria

The commercial gate above remains open.
- Source audit: `docs/AAA_RELEASE_AUDIT_013.md`. Owner requested complete AAA verification; **not achieved** at this stage.
- [ ] API29 minimum-device and API35 full Android emulator tests green at identical source SHA.
- [ ] Real GIF color-gradient and moving-transparent-object output decoded without ghosting or color damage.
- [ ] GIF Save As works with a write-only document provider; original source and existing files remain unchanged.
- [ ] GIF exports respect device free-space quota, cancel gracefully and clean up incomplete outputs.
- [ ] Pro save/overwrite, interrupted AtomicFile recovery, fail-closed malformed input, Undo/Redo and source URI access revalidated.
- [ ] Long-session, large-media, thermal/battery/ANR, wide color gamut/ICC and multiple real phones validated.
- [ ] True Photoshop/GIMP parity: advanced layers/groups, masks, smart selections, retouching and production-scale tools.
- [ ] Lightroom-class RAW/16-bit/float/ICC management, catalog, batch and device profiling.
- [ ] Accessibility and complete tutorial onboarding, licensing/SBOM/commercial payments and production Play-signed App Bundle.
- Signed side-by-side APK may be supplied only as *noncommercial QA* with explicit warnings; it is **not** an AAA or Play Store release.
````

### A7 — docs/PROFESSIONAL_ENGINEERING_ROADMAP.md

[Complete pinned original](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e04bc08a4800233b0f407b419a91c97ca2c068f6/docs/PROFESSIONAL_ENGINEERING_ROADMAP.md) · Git blob `a2697f4add074c29dad71a4303c49aecdc5e1769`.

````markdown
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
````

### A8 — docs/UNIFIED_PRODUCT_SCOPE_09.md

[Complete pinned original](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e04bc08a4800233b0f407b419a91c97ca2c068f6/docs/UNIFIED_PRODUCT_SCOPE_09.md) · Git blob `51792479496322564ffd4f3b80be04af3d43e9cb`.

````markdown
# GZ Editor Pro | Owner-directed non-AI product scope (0.9 record)

For current engineering, follow [Revision 4 product requirements](PRODUCT_SPEC.md). This record predates the Regular Masks and Pro Text authoring restrictions, confirmation-based Regular-to-Pro handoff, and formal pre-Video workspace locks. Read its dated inventories as historical.

**Approved direction: November 21, 2026.** Android first; $2.99 **one-time** final retail target. No AI or inference at runtime, accounts, cloud, ads, subscriptions, watermarks, credits, hidden unlocks, or telemetry. Original source media never overwritten. iPhone separately in the future. The design goal is a $100-class perceived value from excellent working features and usable UI, **not a claim of an appraised $100 market value**.

## UI: hard rules
- Top-right dropdown modes: **Photo editing** (regular), **Photo editing Pro**, **Collage maker**, **GIF maker**, **Video editing (Coming soon)**.
- Each mode gets its own workspace and shallow, logically grouped controls. Never mash every option into one screen. Keep main media visible and active touch handles working even when chrome hides.
- Regular: intuitive Looks, Light, Color, Detail, Effects, Privacy, Crop, More. Pro: full editing of selected image layers, layer operations, spatial edits and blend/composition. Collage: original seven patterns and 20 independent tiles. GIF: frames/time/size. **Video editing is the LAST major capability implemented.**
- Preserve prior project data, editing behaviors, no source writes. Deliver one large coherent tested batch, followed by one combined handset verification, not endless tiny builds.

## Engineering state
**Already in merged 0.8:** 56 adjustments, 50 Looks, HSL, color curves, 3 mask types, blur/pixel/opaque privacy, text/fonts/drawing/shapes, manual crop, undo, photo projects, social sizes and true JPEG/PNG/WebP/BMP/TIFF/PDF; 1–20-photo independent 7-layout collage.

**0.9 candidate, do NOT claim release until CI + device passes:** Separate workspace dropdown; simplified Regular UI; Photo Pro independent image layers, order, visibility, opacity, position, size and normal/multiply/screen/overlay compositing, independent full photo editor per layer, editable saved projects and output; GIF maker 2–20 photo frames, reorder, preview, timing, looping, Fit/Fill, dimensions, project saves and locally coded true GIF89a/LZW export. New third-party libraries: none.

**Future professional photo work, ALL explicitly in scope:**
- Proper editable unified image/text/shape/paint/adjustment layers, layer masks, groups, blending, selection tools (rectangle, ellipse, freehand lasso, polygon, colors), editable guides.
- Non-AI clone/heal/spot repair, background selection/removal, gradients/fills, distortion/warp/perspective, editable feathered radial/linear/color/luminance masks, camera/lens corrections.
- RAW/DNG import with properly licensed decoder and camera profiles, demosaic, real white balance, highlight detail handling bounded by source, ICC/color management, internal 16-bit/float color, wide gamut and truthful high-bit TIFF/PNG exports.
- Catalog: local folders, collections, ratings, favorites, tags, duplicate detection, EXIF, smart user-defined filters, sidecar edit copies.
- Batch processing and export: apply grades/Looks/presets, copy/paste settings, rename, multiple formats, queued cancellation, job progress and safe source custody.
- True histograms, clipping, before/after, numerical color/denoise/sharpen and image benchmark suites, performance profiling, device-aware memory caps.
- Advanced typography, vectors, touch handles, layer grouping and editing history, genuine non-AI retouching, photo compositions.

**Future GIF/photo motion:**
- GIF from videos, frame ranges/speed/reverse, per-frame duration, animated captions/overlays, proper crop/resize, optimized palettes/dithering.
- Photos plus audio produce **MP4 video**, not JPEG/PNG or GIF. Local user-owned/imported audio, slideshows, transitions, per-photo duration, deterministic pan/zoom and optional beat-sync. No preloaded copyrighted music.
- Audio on videos: local MP3/WAV/AAC where supported, multiple clips/tracks, trim/split, volume/mute, fades, waveforms, voiceovers, equalization and non-AI noise reduction.

**VIDEO LAST:** local import/player; frame/time timeline, multiple clips, trim/split/join/reorder, speed and crop, effects, fades, transitions, captions/text/shapes, picture-in-picture, overlays/selection/redaction flattened in output, keyframes, audio sync, real MP4/codec exports, preview/final parity, re-encode privacy safety, device performance/thermal tests and supported 1080p/4K tiers. Review Media3/dependencies separately before integration; do not ship placeholders.

## Engineering/legal release gates
- Copyright protects code/art; no Adobe branding, proprietary source, copied artwork/UI/presets or protected streaming ripping. Independently authored tools may still raise patents and commercial licenses.
- Maintain SBOM, transitive licenses, asset/font/music/codec rights, trademark clearance, vulnerability and patent-specific checks before distribution. User media rights remain user's responsibility.
- No guarantee of lawsuit immunity. Commercial release requires real-device QA, pixel/codec and preview/export checks, privacy/metadata policy, Google Play compliance and properly signed AAB.
- **Only authorized repo:** `MrSkyrimFan/GZ-Mobile-001`; no corporate website/DNS, XForge, GZFX, Beside-Me or unrelated projects.
````

### A9 — docs/PRO_GIF_REBUILD_011.md

[Complete pinned original](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e04bc08a4800233b0f407b419a91c97ca2c068f6/docs/PRO_GIF_REBUILD_011.md) · Git blob `9e7d6be411c8c8f4f1e9a8a1c3b43439be0e2eca`.

````markdown
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
````

## Appendix B — full immediate pre-handoff plan

Pinned commit: `eb8796c92fa654cb0dac371e35d7645ecd07db8a`. Files whose blob equals Appendix A use that identical full-source content; every changed file is embedded below in full.

### B1 — AGENTS.md

[Complete pinned original](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/eb8796c92fa654cb0dac371e35d7645ecd07db8a/AGENTS.md) · Git blob `855f63b1927a4b89a6f447fc84bd31c51258d7dc`.

Identical to the full text in **A1** above (same verified Git blob). This is shared snapshot content, not an omitted or newly summarized task list.

### B2 — docs/GZ_ENGINEERING_ROADMAP.md

[Complete pinned original](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/eb8796c92fa654cb0dac371e35d7645ecd07db8a/docs/GZ_ENGINEERING_ROADMAP.md) · Git blob `1a625ce79fd0479e6edefd8a4529165d488c9edb`.

````markdown
# GZ Editor Pro — complete product roadmap

Updated 2026-11-24. Owner: Lance / Greenezone Software LLC. Engineering workspace: MrSkyrimFan/GZ-Mobile-001.

## Current direction and authority

Lance supplied the independent PR #26 review (CHANGES REQUIRED), authorized fixing the issues, and requested a complete roadmap for the original professional app vision. The successor onboarding hold is lifted. The completed correction wave now supplies a qualified 0.12.10 Test handset build; owner acceptance remains open. Engineering, tests, branches, PRs and verified merges are authorized; physical acceptance, formal workspace locks and public release remain owner decisions.

**Latest release sequencing:** Play Console is not set up. Lance wants its setup last, after a fully functioning app AND completion of the Greenezone website. Website work is an external dependency; this app task does not authorize modifying it.

Read this roadmap with [product requirements](PRODUCT_SPEC.md), [work ledger](WORK_LEDGER.md), [capability inventory](CAPABILITY_MATRIX.md), and [release gates](RELEASE_GATE.md). The inventory gives scope and current evidence; the ledger gives the next executable work. Earlier blueprints and transcripts retain their historical evidence and yield to later direct owner decisions.

## Current engineering state

**Current qualified correction:** [PR #31](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/31), application merge `3ad65c533f7667a136d4fa1838afcc06d3f484da`, closes only R29-01/R29-02 Eraser engineering and the GIF Save performance failure observed during qualification. Candidate `aa86c37c64c5e395d5378df281a0c7583a39d0e0` and the merge share tree `1c7e3007204192ea240a7fd8d65cbd00cf7f9511`. [Candidate run 45480001019](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45480001019) and [merged run 45483297512](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45483297512) pass JVM/build/signing and **193/193 per Android API**, with zero failures/errors/skips. [Execution receipt and exact 0.12.10 Test APK](ERASER_REPAIR_VERIFICATION.md).

All original 186 Android case identities and established GIF budgets remain; five Eraser and two cache-equivalence cases extend them. The original red reproduction, separate rotation-harness abort, and candidate 1 GIF speed failure remain in the receipt. Existing layout/style and mode ownership are preserved. Later documentation-only commits do not supersede the qualified application identity. **R1.1 bounded saved-project listing/loading is the next engineering task; R1.2 recoverable Regular drafts follows.** Handset acceptance, whole-family completion and commercial release remain open.

### Prior verified PR #29 checkpoint

R0 is engineering-verified in [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29), app merge `c9efc5aa0673f21b98f150733582a24996b34d10`, candidate `6ad22e3fabe3964a662c7e0e39bd08c4a8e8f9ca`, [candidate workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45352950946) and [merged-main workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45355074845). Candidate and merged application share tree `47e95c51254ce8e8f97b3883c44dc157ea8452b4`; each passed Verify and 186/186 Android cases on both API29 and API35, with no failures/errors/skips. Version 0.12.9 Test / code 21 was that checkpoint’s qualified phone wave and is now superseded by 0.12.10 Test. Handset acceptance, all formal workspace locks and commercial release remain open.

**Next roadmap package: R1, starting with R1.1.** Preserve the completed R0 evidence and follow the active ledger. Do not replay the independent-review intake or reopen repairs without a new reason.

The legacy Pro streaming optimization passes in the recorded CI environments; the earlier 16 MiB hard-cap condition has not been independently requalified. R1/R8 retain explicit low-memory/device-tier measurements. Legacy v4 GIF aspect migration remains R7.

## Product contract

A paid-once, fully offline Android creative application with five separate, understandable workspaces. Target price remains US $2.99 once. No runtime AI/ML inference, cloud/account requirement, ads, telemetry SDK, subscription, credits, watermark or in-app purchase. Imported originals are read-only. No copying proprietary source, presets, artwork or branding.

| Workspace | Intended job and boundaries |
| --- | --- |
| Regular Photo | Approachable everyday editing plus professional photographic development: Looks, tone/color/curves/HSL, optics, RAW and managed color, text/draw/shapes/privacy, stickers, local organization and batch. **No Masks authoring menu or Pro layer/selection tools.** Preserve legacy mask recipes. |
| Editor Pro | Layered compositing, shape/paint/adjustment layers, groups/clipping, masks/selections, transforms, retouching, Subject & Background, warp/liquify and precise non-destructive manipulation. **No Text authoring or duplicate Regular development/sticker dock.** Preserve legacy Pro text and grade data. |
| Collage | Independently editable 1–20 tiles, including repeated source URIs, seven existing layout families, layout controls, named projects and accurate output. The selected tile uses Regular tools without changing its neighbors. |
| GIF | Silent animation from video/GIF/photo sequences; neutral import, real edited playback, timeline and frame edits, captions, palette controls, accurate timing/color, persistence and externally playable GIF output. Sound belongs in Video. |
| Video | Separate professional timeline with clips, compositing, user-owned audio/music/voice, precision editing, effects, keyframes and real MP4 output. **Implementation starts only after Lance formally accepts/locks all four pre-Video workspaces.** |

Preserve the established dark image-first style, workspace selector and working Regular/Collage layout. A necessary functional change must name its user benefit and affected surface. PR #31 changed no layout or style. Broad reskins and new product scope need owner decisions.

The terms Lightroom-class, Photoshop-style and studio-quality are behavioral references. The finite capabilities below govern delivery; they are not permission to copy those products or to claim universal parity. The no-AI and mode-ownership rules override incompatible features. Required families cannot disappear merely because they are difficult.

## Historical intake evidence baseline

- App baseline: PR #26 merge `372e0e947caacb2ceaf603bc4ff5a60e44cc33ec`; candidate `6a8cceba719dc990d663768f626ab87d9436aad4`.
- Roadmap intake main: `d2a8f4de41839e25ad54060be803746d7307bee8`, tree `5f62620f9fbad28bd8c404c723d995decf03ecf3`. Its difference from the app baseline is six documentation paths, including PRs #27/#28. No open PR was found at intake.
- Candidate run 45337105839 and app-merge run 45339000608 passed Verify and 174/174 on each API 29 and 35. Prior independently inspected merge receipt archives had matching hashes and identical case sets.
- Existing phone artifact 14895803102 is v0.12.8 Test / phonetest.v20. It is historical QA evidence, **not the next recommended combined handset candidate**.
- Portrait Save: 4.557 s / 5.867 s, 180×320, 88 frames, 2.950 s. The five-sample preview measurement includes the paused initial picture and excludes edit/seek-to-first-correct-still latency. It does not establish full-rate playback.
- Controlled Activity recreation and retained-registry tests prove that boundary; they do not establish OS process reclamation during an actual external picker or general draft autosave.

[Repair evidence](AUDIT_2_REPAIR_VERIFICATION.md) · [Independent prior audit](gz-editor-pro-audit-2-20261123/00_REPOSITORY_START_HERE.md) · [Field guide](gz-editor-pro-field-guide/00_START_HERE.md).

## Ordered delivery plan

These are dependency-ordered work packages, not dates or completion percentages. Each package produces a useful editing outcome with saved/reopened projects and decoded outputs. Independent implementation may proceed in parallel on disjoint files. Integrate in the stated order; use the first unblocked package, without repeatedly reopening accepted predecessors.

### R0 — Correct the independently identified defects

1. Fresh cover → direct manual Crop: preserve protected source pixels and keep gesture ownership stable while held through recomposition; quick release, cancel, Undo/Redo, reopen, decoded PNG and selected Collage tile.
2. Regular save/export integrity: bounded reader-compatible encoding before mutation or picker launch; oversized replacement preserves healthy stored bytes and dirty active work; failed save cannot close or replace it.
3. Regular Draw: represent the complete long gesture within the existing safe budget, retaining late bends and endpoint; cancel drawing/erasing restores prior document without a history entry.
4. Pro Lasso: represent the complete contour within its safe budget, preserve meaningful corners and final segments, cancel safely, and prove selected pixels plus an applied operation/history/persistence.
5. Preserve existing named-save, Pro bounded save, Collage, handoff, GIF performance/fidelity and picker tests. Capture specific red-before and green-after evidence, full candidate and app-merge JVM/API29/API35 results, exact phone APK identity/signature.
6. Correct overstated evidence wording and retain the legacy v4 GIF aspect limitation as explicit follow-up. Do not silently reinterpret old project framing.

The original four defects passed that engineering exit in PR #29. The separately demonstrated R29-01/R29-02 Eraser follow-up and observed GIF Save failure passed their focused candidate/merge exit in PR #31. Both histories remain credited; neither closes the complete feature families or owner handset acceptance. Follow the selected R1.1 instead of reopening this wave without new evidence.

### R1 — Durable documents, source custody and complete interaction contracts

Build the smallest needed extensions to existing modules. No application-shell rewrite.
- Version document schemas and render semantics separately; preserve legacy Regular/Collage/Pro/GIF recipes and backups. Check bounds before allocation/write, reject essential unsupported records recoverably, and migrate copies atomically.
- Bound project-list memory: load lightweight metadata and decode selected recipes lazily; qualify multiple near-budget saved documents rather than eagerly retaining every dense recipe.
- Durable recoverable drafts and snapshots for every workspace, selected document/object/tool/viewport/playhead restoration, interrupted-write recovery, source grants/relink, and real process-death behavior. Keep media/history out of saved-instance bundles.
- One history checkpoint per completed gesture; cancellation restores it; Undo remains separate from Back. Verify actual system Back, both edges, half-cancel, double Back, IME/menu/dialog/picker precedence and busy-copy status.
- Explicit source/revision IDs, immutable export jobs, staging validation, write-only providers, aliases, partial writes, low storage and truthful retry. Never claim an ambiguous destination write succeeded.
- Upgrade repeated saved Regular→Pro→Regular→Pro exchange to immutable revision-linked masters with alpha/profile/precision when available. Retain both native parents. Existing Yes/No live transfer remains intact.
- Portable native bundles with declared source embedding/link-only choice, checksums and safe import; upgrade tests use same-ID fixtures/installs rather than claiming side-by-side packages test migration.

Exit: cross-workspace cold-reopen, process-loss, repeated handoff and provider-failure scenarios preserve all unaffected documents and original hashes. Only platform-specific observations genuinely requiring a handset are handed to Lance.

### R2 — Pro document and compositing foundation

- Stable layer tree and source references; group/ungroup, isolated/pass-through groups, clipping, named layers, locks, multiselect, align/distribute, ordering and group masks.
- Explicit layer/canvas/source/viewport transforms, alpha conventions, blend working spaces and effect order; legacy rendering remains compatible.
- Memory-bounded deterministic CPU tile/reference compositor with effect halos, bounded caches/proxies, render ownership and preview/final parity. GPU acceleration follows independently checked parity when measurements justify it.
- Canvas size versus image resize, crop, background/alpha and device-tier limits. Raise the current 12-layer/4096-edge limits only after actual capacity evidence, never by changing a constant alone.

Exit example: grouped children, clipped masked effect and independent cutout on transparency; correct group opacity and overlap; rearrange/Undo/save/process-restart/reopen/export with independently checked alpha and pixels.

### R3 — Complete Pro selection, paint, retouch and shape workflows

- Selections: rectangle/ellipse/freehand/polygon, contiguous/global color range and luminance, add/subtract/intersect, grow/shrink/invert/feather, saved selections/channels, linked/unlinked layer/group/filter masks and grayscale mask painting.
- Precision tools: nonuniform scale, skew/perspective/pivots, numeric entry, guides/rulers/snapping, cubic paths, shape joins/caps/dashes/booleans, gradients and fills. No Pro Text creation.
- Paint: flow/opacity/hardness/dab spacing, pressure and supported tilt with touch fallback, stabilization, texture brush rights, alpha eraser, deterministic replay and zoom-consistent behavior.
- Retouch: fixed/aligned clone; current/current-and-below/visible source policy; spot/heal/patch/inpaint with conventional math; dodge/burn, blur/sharpen/grain; editable sampling references and local non-destructive results.
- Ordered layer effects/styles, mask/filter scopes, shadows/strokes/bevel/displacement and conditional blend controls without a duplicated photography dock.
- Native workflow first; then a declared OpenRaster/PSD interchange subset, loss reporting, unsupported-record preservation and repeatable document actions. No universal PSD support claim.

Exit: every command produces the intended live image, targets the correct object, survives Undo/Redo/reopen, and independently exports correctly. Preserve old TEXT records without exposing new authoring.

### R4 — Offline Subject & Background and reshaping

Implement in Pro as one understandable family:
1. Manual/assisted subject trace and keep/remove marks; conventional segmentation with bounded cancellation and deterministic fallback.
2. Editable alpha mask, background removal and replacement by image/color/gradient; preserve original and linked subject transforms.
3. Refine hard/soft edges, feather, choke/expand, halo/color-fringe cleanup and restore mask; difficult hair/fur/glass cases have honest limits.
4. Small-object removal using conventional inpaint/clone/patch and seamless blending; no promise of reconstructing unseen detailed scenery.
5. Reversible mesh/liquify push/pull/expand/shrink/freeze/thaw, face/body region reshaping and material retexture/reskin with editable masks/transforms.
6. Dependency/license/performance evaluation before adding OpenCV or any alternative; no model download or runtime inference.

Exit: isolate/refine/move a complex subject onto a different background, remove a bounded object, reshape/retexture a chosen region, then reopen and export at qualified resolution. Check edge/mask geometry at multiple zoom levels and unchanged outside regions.

### R5 — Regular professional photographic processing

Do not introduce a Masks authoring UI; old saved masks remain compatible.
- Finish numerical behavior and full workflow coverage for existing exposure/tone, WB/color, HSL, curves, grading, Looks/custom presets, clipping/histogram and before/after.
- Define linear/encoded sample meaning, float reference processing, color/alpha transforms and legacy appearance adapter before precision claims.
- DNG first: explicit supported mosaic/linear/compression/opcode subset, sensor black/white/linearization, CFA phase, demosaic, WB/calibration, camera profiles and scene-linear exposure/highlight policy. Embedded JPEG is labeled preview. Broader vendor RAW follows rights-cleared camera samples and dependency review; do not bundle LibRaw by default.
- ICC/profile assignment versus conversion; sRGB and qualified wide-gamut SDR first, proof/gamut warnings, numeric samples, output intent and truthful 16-bit PNG/TIFF. HDR display/export only as separately qualified variants.
- Manual/profile optics: lens distortion, lateral CA, vignetting, perspective/upright/straighten; qualified conventional luminance/chroma denoise, sharpening and output sharpening.
- Conventional exposure-bracket HDR merge, panorama stitching and focus stacking after single-image geometry/color; retain input/transform lineage and inspectable correction policy.

Exit: synthetic RAW samples, chart/neutral/ramps/optical grids and rights-cleared camera negatives agree with independent references; output samples/profile tags prove precision. Unsupported variants report a precise limitation without source loss.

### R6 — Regular organization, batch and complete everyday creativity

- Transactional local catalog: folders versus collections, ratings/flags/favorites/labels, tags/EXIF/IPTC/XMP, rules-based search/smart collections, duplicate candidates with byte confirmation, stacks and virtual copies.
- Non-destructive sidecars and backup/restore, targeted relink, source/copy identity, selective settings copy/paste/sync with incompatible source-anchored edits excluded explicitly.
- Durable batch jobs: frozen per-item revision/recipe/destination, multiple outputs, naming, profile/metadata choices, per-item progress/receipts, cancellation and idempotent interruption recovery.
- Complete Regular Unicode text/shaping/font workflow, draw/shapes/frames/borders and independent decorative objects. Rights review for fonts and assets.
- Add deferred Stickers after repair stabilization: at least 500 finished usable original/licensed local assets; planning catalog has 648 concepts across 27 categories, not finished art. Categories/search/favorites/recents; custom transparent PNG/WebP; move/scale/rotate/flip/opacity/order/duplicate/delete; Undo/reopen/export and selected-tile use. Preserve existing tools and avoid a Pro sticker browser.

Exit: 100-file catalog/recovery exercise and interrupted batch do not duplicate completed outputs or overwrite originals. Text/draw/shape/sticker/privacy objects remain independent through crop, reopen and export.

### R7 — Complete GIF and Collage scope

GIF:
- Broaden real media and lifecycle coverage: VFR/B-frames/rotations/non-square pixels/disposal/transparency/finite loops/HDR-to-SDR policy, source grant loss and relink.
- Measure source-open, edit-to-first-correct-still, scrub, first moving frame, sustained presented cadence, reverse, full Save and copy separately. Preserve current portrait and rotated 720p60 gates.
- Add explicit frame/segment assembly, split/remove/duplicate/reorder, per-frame holds, ping-pong endpoint rules, keyed captions/overlays, palette/dither choices and encoded appearance preview.
- Qualify new photo-sequence creation separately from compatibility with existing saved sequences.
- Legacy v4 anamorphic migration: source reinspection, reversible versioned upgrade with before/after preview, preserve old framing and manual aspect intent until adopted; missing source cannot trigger guessed migration.

Collage:
- Preserve all seven layout families and 1–20 independent tiles; layout/aspect/gap/margin/border/rounding/background/fit/fill; same-URI identity; selected-tile full Regular features.
- Qualify relink versus replacement, selection/history/Done/Cancel/Back, old projects, all 20 tile outputs, alpha/orientation/memory and interrupted export.

Exit: declared source/format matrix, full editable-project recovery and external animation/image outputs match required content and timing; physical performance documented. A passing small fixture is not a universal throughput claim.

### R8 — Pre-Video acceptance and formal owner locks

Close every required pre-Video inventory row or obtain a specific owner scope decision; no silent omission. Complete representative phone/OEM/codec/memory tiers, accessibility, novice and expert tasks, prolonged thermal/storage/provider tests and repeated editing/reopen/export.

Give Lance one coherent verified candidate and focused handset script. Repair his findings. Record his formal acceptance of Regular, Collage, Pro and GIF separately. CI success alone cannot open Video. Continue independent authorized pre-Video work while only a genuinely device-specific observation is pending.

### R9 — Separate professional Video workspace, after R8

1. Versioned video project, source/track metadata, stable clip IDs, timeline/timebase, playback/seek, proxies, transactional edits, trim and real H.264/AAC MP4 export.
2. Assembly: split/join/reorder/duplicate, source/sequence In/Out, append/insert/overwrite, ripple/lift/extract/roll/slip/slide, snapping/markers/frame nudge, linked/unlinked sound, J/L cuts, track targeting/locks/sync.
3. Audio: imported local audio/video streams and voiceover, waveforms, clip/track/master gain, mute/solo/range mute, fades/pan/EQ/dynamics/limiter, conventional non-AI noise reduction, automation, ducking, buses/stems and channel/sample-rate policy. No bundled unlicensed music.
4. Compositing: multitrack video, PiP, keyframed transforms/opacity, captions/titles, masks/chroma/luma key, speed ramps/reverse/freeze, transitions and conventional stabilization; later nested/compound/multicamera within the same finite studio scope. Photo-plus-audio slideshow authoring includes per-photo duration, deterministic pan/zoom and optional beat-sync.
5. Color/effects: shared original Looks and selected-range grades, scopes, bypass/copy settings, qualified LUT import, explicit log/SDR/HDR transforms and preview/final parity.
6. Timed blur/pixelation/opaque redaction with editable keyframes and optional deterministic tracking; review uncertain intervals/cuts. Re-encode sensitive removals and inspect every affected frame/stream/thumbnail; edit-list-only hiding is insufficient.
7. Mastering: local bins, metadata, full-resolution conform, sequence versions, full/range/audio-only exports, subtitles/burn-in/sidecars, declared interchange subset, portable archive/relink.
8. Initial qualified SDR preset, then measured 1080p/4K/HEVC/HDR/device tiers. Pin and verify Media3 or other dependencies then; older references to framework limitations are not current implementation guarantees.

Exit: multitrack project with precision edits, user audio, transitions, captions and moving redaction survives process loss and independently decoded MP4 agrees in frames/audio/timestamps/color. A photo-plus-music slideshow preserves declared per-photo durations and pan/zoom in a real MP4; optional beat-sync aligns the declared events. Audio-cleanup fixtures measure noise attenuation and retained desired signal. GIF remains silent.

### R10 — Full editing qualification, then final customer onboarding

First qualify all five workspaces and shared interactions, formats, migration, accessibility and performance on the final editing build. After owner editing acceptance, implement complete English/Spanish localization, welcome/language selection, contextual help, final privacy/terms/license documents, acceptance/decline and local access flow as specified. Do not invent an in-app purchase/unlock product.

Re-run editing plus fresh-install/upgrade/offline/decline/returning-user flows in both languages, large fonts and TalkBack. Third-party license diligence is continuous during engineering, separate from the deferred consumer legal screens.

### R11 — Website dependency, Play Console and release

This stage waits for the fully functioning qualified app AND Lance's Greenezone website completion.
- Owner establishes the appropriate verified Play Console account, organization identity/payment profile and contacts; determine applicable testing requirements from that actual account. No account creation now.
- Recheck current Google Play policies at submission: target API, supported devices/adaptive behavior, permissions, Data safety/privacy URL, content rating/target audience, ads declaration, paid listing/territories and support contacts. Offline does not exempt the app from accurate disclosures.
- Complete actual dependencies/transitives/assets/fonts/music/profiles SBOM and shipped notices; audit the present packaging exclusions before trusting notices. Review rights/trademarks/material codec or patent questions for shipped features.
- Secure production upload key, Play App Signing and signed AAB; protect key custody outside repository. Test actual generated/installed splits, same-ID upgrade/project migration, release build behavior, ABI and 16 KB page compatibility if native libraries are added.
- Verify store copy/screenshots/video against actual finished features; no blanket AAA/Photoshop parity claim. Run required internal/closed testing and pre-launch checks; account-specific production-access gate where applicable.
- Lance reviews exact release candidate, price/listing and authorizes public publication. Define support/update/rollback/migration response and monitor platform-provided quality signals without adding prohibited in-app telemetry.

Policy reference snapshot read 2026-11-24: [target API](https://support.google.com/googleplay/android-developer/answer/11926878), [new personal-account testing](https://support.google.com/googleplay/android-developer/answer/14151465), [Data safety](https://support.google.com/googleplay/android-developer/answer/10787469), [signing](https://developer.android.com/studio/publish/app-signing), [app setup](https://support.google.com/googleplay/android-developer/answer/9859152), [organization details](https://support.google.com/googleplay/android-developer/answer/13628312). These links must be rechecked at R11; this snapshot is not account verification or permission to begin setup.

## Meaning of professional / AAA quality

This is an acceptance standard, not a certification or an unsupported marketing adjective.

A required feature is accepted only when: correct mode and understandable entry → responsive real edit → correct target/pixels/timing → coherent Undo/Redo and cancel → safe save/cold-reopen → independent export → fault recovery/source protection → accessibility/device qualification → owner acceptance where required. Record build, fixture, device, expected result, actual evidence and limitations separately.

Release blockers include reproducible data loss, source overwrite, wrong-owner changes, privacy exposure, false save success, required migration failure, crash/ANR in supported workloads, missing promised workflow, or unsupported claims. Lower-severity findings require an explicit disposition; they are not erased by a test count.

Performance goals from older blueprints conflict in places and are **provisional**. Calibrate one named device/workload table in R1/R8 rather than adopting whichever number is easiest. Retain already-established CI budgets unchanged unless a documented product decision and evidence justify revision. Record cold versus warm, input-to-correct-pixels, sustained cadence, median/p95, memory/temp storage and thermal behavior. Cover 12/24 MP and declared larger tiers, 20-tile Collage, long gestures, layered Pro and representative media. Include API29 and35 automated lanes plus target/current-platform compatibility before release.

## Execution controls that prevent drift and repetition

1. Select the first unblocked ledger item; state its user-visible result, exact baseline, owner, affected files, reproduction, exit criteria and next decisive observation before editing.
2. Use parallel agents for distinct implementation or independent review. One owner per shared file; root integrates reviewed blobs. Keep all repository engineering remote; no local clone/mirror.
3. Preserve the existing evidence and corrected behavior. Reopen a passed item only for changed dependencies, new failure, materially weaker evidence or an owner report. State the reason.
4. A failed run triggers diagnosis of exact failure/phase, one causal hypothesis and a bounded correction/experiment. Do not retry an unchanged application failure or widen a timeout until green. A justified infrastructure retry keeps the original result and rationale.
5. Run appropriate tests once per changed candidate. Full API29/API35 and JVM at the final candidate and merge remain required; do not rerun settled receipt parsing or rebuild documentation-only changes to manufacture progress.
6. UI source presence, a helper probe, fixture success, handset success and owner acceptance are distinct statuses. Do not call whole families complete from a button, compile or narrow synthetic test.
7. Keep a single active ledger and this roadmap. Update status/evidence in place; retain earlier runs as history. Do not create another competing master plan or let handoffs revive stale commands.
8. If work drifts, identify the mismatch against the selected item, stop that extra change, reconcile the ledger, and continue the correct next action. Do not restart intake or replay completed repairs.
9. Missing product intent prompts a specific question. Missing technical detail is investigated by the engineer. While waiting, continue independent authorized work. No guessed loss of scope, new public release, or premature Video/onboarding work.
10. Every handoff states exact completed behavior, evidence, remaining work and next action. An enabling diagnostic is not the delivered repair. No numeric completion percentage without a defined denominator.
11. Keep original intake inputs until the work they govern is completed and accepted; do not archive a full blueprint after one repair wave.
12. Public launch and formal locks remain explicit owner decisions. Routine verified engineering does not need repeated permission.

## Requirement provenance and unresolved product decisions

Reconciled sources: original full blueprint revision 2; revision3.1 master sections 4–11 and feature/sticker audits; revision4 owner overrides; Subject & Background addendum; full 201-message source transcript; repository PRODUCT_SPEC/UNIFIED_PRODUCT_SCOPE_09/PROFESSIONAL_ENGINEERING_ROADMAP and field guide; prior Audit 2 and the supplied independent PR #26 review. Private screenshots/original archives remain in the supplemental intake and are not published here.

Settled overrides: Regular has no new Masks authoring; Pro has no Text authoring; actual edited Regular→Pro Yes/No prompt and retained Regular draft; stickers deferred during repairs; Video waits for four formal locks; consumer onboarding follows all editing; Play Console setup follows working app and website. Keep these in one place.

Still requiring a later named decision: exact shipping device/performance/RAW-camera/interchange tiers; specialized CMYK/spot/prepress, photo books, tethering/plugins or vendor ecosystem support if desired; final distribution territories/support/legal choices. These are not silent additions or omissions from the enumerated core. Record scope changes explicitly with Lance.
````

### B3 — docs/WORK_LEDGER.md

[Complete pinned original](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/eb8796c92fa654cb0dac371e35d7645ecd07db8a/docs/WORK_LEDGER.md) · Git blob `51aebf8f0ed9254cd2e674662feba9fecc70adb5`.

````markdown
# GZ Editor Pro — single active work ledger

Updated 2026-11-24. Read with [roadmap](GZ_ENGINEERING_ROADMAP.md), [capability inventory](CAPABILITY_MATRIX.md) and [release gates](RELEASE_GATE.md). This ledger controls execution order; the linked engineering records control run results. Historical green counts do not close newly reported defects.

## Current state — PR #31 engineering complete; owner phone pass open

[PR #31](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/31) is merged at application commit `3ad65c533f7667a136d4fa1838afcc06d3f484da`. It shares tree `1c7e3007204192ea240a7fd8d65cbd00cf7f9511` with candidate `aa86c37c64c5e395d5378df281a0c7583a39d0e0`. [Candidate run 45480001019](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45480001019) and [merged-main run 45483297512](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45483297512) each pass JVM/assembly/signing and **193/193 on API29 and API35**, zero failures/errors/skips. The current combined phone candidate is **0.12.10 Test / code 22 / phonetest.v22**. [Raw receipts, exact APK, retained failures and phone checklist](ERASER_REPAIR_VERIFICATION.md).

Later documentation-only commits reconcile this state without replacing that tested application commit or APK. Owner handset acceptance, the complete Draw/GIF families, all workspace locks and commercial release remain open. R14 is PARTIAL, not complete. The four original PR #29 corrections remain credited; its 0.12.9 artifact is historical and superseded for current delivery.

### Completed scoped follow-up — R29-01/R29-02 and observed GIF Save failure

**Reopening reason and authority:** Lance supplied the independent 0.12.9 review and explicitly requested repository fixes. A supported 160-stroke erase could omit unrelated artwork, and the bounded Draw sampler exposed visible segments the point-only Eraser could not cut. These named failures justified the follow-up without reopening the full blueprint.

**Delivered result:** Regular and the selected Collage-tile Eraser cut visible segments and the swept brush path. First/later capacity overflow restores the entire pre-gesture document, dirty work and history with feedback through the existing notice. Named saves reject invalid ink cardinality rather than encoding a prefix. Five actual UI regressions verify seeded boundaries, long Draw→Erase, cancellation, history, dirty-state Back, named reopen, decoded PNG and full-parent duplicate-source Collage isolation. All original 186 Android case identities remain.

**Qualification extension, resolved:** candidate 1 passed the Eraser cases on both APIs but failed the unchanged API29 portrait GIF Save gate at 28,331 ms. That failure was preserved and diagnosed; the application was not retried unchanged. A bounded exact `(sourceARGB, computedDetail.rawBits)` export cache and stage measurements were added. Two actual Android bitmap-equivalence cases retain full pixels across changing neighborhoods/grades. The 6-second sampled-motion and 15-second Save gates, source fixture, 180×320 canvas, 88 frames and complete delays remain unchanged. Candidate and merged-main now pass those gates. The old timing counters cannot retrospectively assign the entire failed interval to one stage; physical performance remains a separate gate.

**Scoped production files:** `InkMath.kt`, `PhotoEditorScreen.kt`, `EditDocumentCodec.kt`, `ImagePipeline.kt`, `GifEncoder.kt`, `VideoGifSource.kt`, and test version. The one observed rotation-harness fault was corrected through its rule import only. No layout/style, mode ownership, Pro production, preview routing, schema, dependency or workflow change. Baseline main was `e04bc08a4800233b0f407b419a91c97ca2c068f6`, with the same application as PR #29 merge `c9efc5aa0673f21b98f150733582a24996b34d10`.

### Next selected engineering item — R1.1 bounded saved-project listing and loading

**Observable result:** opening the saved-project list reads lightweight metadata and decodes only the selected recipe. A full twelve-save set containing near-budget recipes and failed/corrupt entries remains bounded in memory; selection opens the intended project and existing saved bytes remain intact.

Inventory the current state/source ownership once, pin the current main and compare app/test/Gradle/CI inputs against the qualified application, identify affected files, and define the decisive fault experiment before editing. Preserve saved projects through any required storage migration. Keep the existing UI and list behavior; do not restart blueprint intake or reopen the completed repair wave without a new reason. R1.1 is selected but has not been implemented by PR #31.

**Following item R1.2 — recoverable Regular working drafts.** Persist completed edits atomically, keep transient gestures out of checkpoints, retain source identity/grants and restore selected work after genuine process loss. Carry the same contracts to Pro/Collage/GIF in subsequent R1 slices. Handset testing of 0.12.10 remains a separate owner gate; it does not imply all workspaces are accepted.

## Completed wave R0 — independent PR #26 corrections

**Owner:** root integration, with distinct Regular gesture, storage and Pro selection owners. **App baseline:** `372e0e947caacb2ceaf603bc4ff5a60e44cc33ec`. **Intake main:** `d2a8f4de41839e25ad54060be803746d7307bee8` (documentation-only difference). The owner supplied the review and authorized engineering. The former successor read-only hold is lifted.

| Item | Required behavior | Production scope | Decisive evidence |
| --- | --- | --- | --- |
| D01 | A newly drawn privacy cover remains over the same source pixels during the first manual Crop; held gesture survives recomposition | PhotoEditorScreen preview identity and crop anchoring | Actual held/quick pointer flow, Undo/Redo, reopen, PNG and selected-tile isolation |
| D02 | Oversized Regular save/export rejects before storage mutation or destination picker; healthy save and dirty work survive | ProjectStore; PhotoExportController; actionable save error | Allowed dense draw fixture, last healthy bytes unchanged, readable smaller recipe; [failed Save-before-close/replace keeps the dirty editor open](../app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidRegularSaveBudgetTest.kt) |
| D03 | Long Regular Draw retains late bends and endpoint within budget; cancellation makes no edit | CreativeCanvasOverlays; PhotoEditorScreen; bounded sampler | Actual >500-sample Regular/tile gestures, saved/exported pixels, cancellation and history |
| D04 | Long Pro lasso represents the full contour rather than a truncated polygon | ProStrokeSampling; ProWorkspaceScreen | Actual >450-sample lasso, late-region applied mask pixels, Undo/Redo/save/reopen; cancellation restores prior selection |

**Test-only baseline:** [commit 018b45c](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/018b45c4b1af0beb867abfe0261f5bce9e4f54e1), [workflow 45348806687](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45348806687). Three new Android test classes add ten cases against unchanged production. Keep actual failures, including any harness defect, distinct from intended product reproductions.

**Corrected candidate / final result:** [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29), [repair branch](https://github.com/MrSkyrimFan/GZ-Mobile-001/tree/feature/pr26-review-corrections) and its pull request, [branch workflows](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions?query=branch%3Afeature%2Fpr26-review-corrections). The PR description is the execution receipt for exact corrected commit, red/green case disposition, full candidate/merge jobs, independent review, APK identity and remaining owner checks. The PR now carries the completed execution receipt; this ledger records the resulting state. Use its exact qualified artifact, not an earlier candidate download.

### Qualification findings retained with their causes

- Initial unchanged-app run 45348806687: the ten new cases reproduced the reported defects. Gesture/lasso cases reached their intended assertions on both APIs; storage cases reached false-success assertions on API 35 and exhausted memory inside the unbounded production encoder on API 29. Android 35 later aborted in a separate existing Collage v1 test-rule scheduler failure; Android 29 also exposed a legacy Pro read allocation failure. Do not describe that run as a clean full-suite reproduction.
- Initial correction run 45349823052 at `a3cf1b66e38034d41d51e35dce31ad2cdf6d9108`: both APIs completed the identical 184-case set, 182 passed/2 failed/0 skipped. All 174 prior cases passed. The two failures retained the endpoint but lost a broad late Draw corner; the assertions were not relaxed.
- Follow-up uses area-aware bounded sampling with explicit reversal protection, keeping the existing 450/500 caps. It also migrates only the observed Collage test and new Regular gesture fixture to the standard v2 scheduler.
- The baseline legacy Pro failure occurred in JSON unescaping: its 7,591,944-byte transient allocation exceeded the 16 MiB heap observed in that run. The merged read optimization reduces copies while preserving format, file/recipe limits, validation, backup behavior and full recovery equality. Later larger-heap success is not evidence that this memory failure disappeared.
- Final changed-candidate and merged-application results, exact artifact identity and any further diagnostic disposition belong in [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29). Preserve all earlier failures. These bounded qualification repairs do not close the broader R1 durability/memory package.

**Retained state transition:** PR #29 closed its original R0 reproductions. The newly demonstrated Eraser failures reopened only that behavior and are now engineering-verified in PR #31, together with the observed GIF Save correction. R1.1 is next. Reopen a settled behavior only for a named new failure, changed dependency, weaker evidence or owner report; record that reason. Handset acceptance remains separately open.

**Preserve:** all original 174 Android cases, established GIF performance/fidelity budgets, mode boundaries, named project identity, immutable picker recipes, all source files and independent Collage tiles. Historical version 0.12.9 / code 21 isolated that phone test from prior builds. Current 0.12.10 / code 22 is also a separate test package; neither proves same-ID upgrade behavior.

## Ordered remaining work

| Order | Package / completion target | Prerequisite and current disposition |
| --- | --- | --- |
| R1 | Durable documents, lazy project metadata loading, process-loss recovery, source/provider custody, Back/history, saved repeated exchange and portable bundles | NEXT — begin R1.1 above; required families S01–S08 remain open/partial |
| R2 | Pro layer tree/groups/clipping and memory-bounded reference compositor | R1 contracts; preserve legacy pixels |
| R3 | Full Pro selection, paint, retouch, shapes, transforms, effects and declared interchange | R2 foundation |
| R4 | Offline Subject & Background, refine/removal and reshaping | R2/R3 mask/geometry foundation; no runtime AI |
| R5 | Regular photographic development, RAW/color/optics and multi-image processing | R1/R2 shared custody/color/geometry; preserve Regular without Masks authoring |
| R6 | Catalog, metadata/virtual copies, durable batch, text/creativity and at least 500 finished stickers | R1 plus qualified processing; stickers no longer omitted after repair stabilization |
| R7 | Full GIF frame/timeline/palette workflow, reversible legacy v4 aspect upgrade and complete Collage qualification | Shared foundations and selected-tile features |
| R8 | Pre-Video device/accessibility/performance qualification and four explicit owner locks | Every required pre-Video family accepted or specifically dispositioned by Lance |
| R9 | Separate complete professional Video workspace | Blocked on all four owner locks, never on CI count alone |
| R10 | Full five-workspace editing acceptance, then English/Spanish welcome/help/legal/acceptance | All editing completed/accepted; consumer onboarding stays last among app features |
| R11 | Finished website, Play Console/account, signed AAB/store testing and authorized release | Fully functioning qualified app AND Greenezone website complete; account not set up |

Subdivide a selected package into bounded deliverables with affected files and observable exits before implementation. Do not treat these family rows as single giant commits, invent completion percentages, silently remove difficult capabilities, or create another master backlog.

## Next action — execute the selected R1.1

Deliver bounded saved-project listing/loading using its observable result above, followed by R1.2. Preserve the qualified 0.12.10 artifact for the owner’s combined phone pass. Routine authorized engineering can continue while that separate pass is pending; Video and workspace acceptance still require the existing explicit owner gates. Do not restart the correction intake, rerun settled qualification to manufacture progress, or create another master backlog.

## Pending owner/device decisions

- Current 0.12.10 Test handset acceptance remains open. Use the exact merged API35 APK and focused script in the PR #31 execution receipt: original portrait GIF, visible Eraser gaps and independent Collage tiles, retained fresh-cover Crop/Draw/Pro lasso, Regular→Pro custody, and physical Back/picker/provider behavior. The 160-stroke boundaries are already covered by seeded automation; do not ask Lance to draw 160 strokes by hand.
- Four formal pre-Video workspace locks remain open; a limited successful phone session does not close the full product inventory.
- Shipping device/performance, RAW-camera and interchange support tiers are explicit later decisions, informed by implementation evidence.
- Greenezone website completion is an external dependency, not app-engineering scope. Play Console setup waits for that plus the functioning app; no account work now.
- Exact production signing/account/listing and public release need Lance at R11. Routine authorized engineering continues without repeated permission.

## Failure and drift rule

For a failed check, record the exact symptom and phase, causal hypothesis and bounded next correction. Do not retry unchanged application failures, relax an established budget for green, replay passed intake, or call a helper result full workflow acceptance. Reopen settled work only for a new failure, relevant dependency change, weaker evidence or an owner report, with the reason recorded.
````

### B4 — docs/CAPABILITY_MATRIX.md

[Complete pinned original](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/eb8796c92fa654cb0dac371e35d7645ecd07db8a/docs/CAPABILITY_MATRIX.md) · Git blob `44c3f0617a365f9589984d65b26e6196806e7660`.

````markdown
# GZ Editor Pro — capability and evidence inventory

Updated 2026-11-24 after qualified application merge `3ad65c533f7667a136d4fa1838afcc06d3f484da` (PR #31), 0.12.10 Test. The full 77-family inventory originated against `d2a8f4de41839e25ad54060be803746d7307bee8`; PR #29’s original R0 evidence remains credited. PR #31 adds bounded Eraser and GIF performance evidence without completing their entire families. [Current receipt](ERASER_REPAIR_VERIFICATION.md). See [roadmap](GZ_ENGINEERING_ROADMAP.md) for complete behavior and dependencies and [work ledger](WORK_LEDGER.md) for active progress. This is a finite family inventory, not a claim that every reachable UI control has already been exhaustively qualified.

**Statuses:** ENGINEERING VERIFIED = stated bounded workflows pass candidate/merge checks, with owner/device gates separate; DEFECT = confirmed review finding, correction required; PARTIAL = implemented foundation and/or bounded fixture evidence, full listed outcome still open; OPEN = required work/evidence not delivered, not a blanket assertion that no helper exists; LIMITATION = known compatibility limit; GATED = implementation/setup waits for named prerequisite; OWNER GATE/DEPENDENCY = explicit external decision. No row is owner-accepted merely because tests pass. R0 closes only its listed corrections; no other family inherits completion from that result.

| ID | Workspace | Capability | Current status | Package | Required exit / remaining work | Requirement/evidence source |
| --- | --- | --- | --- | --- | --- | --- |
| D01 | Regular / tile | Fresh cover then manual crop | ENGINEERING VERIFIED | R0 | Held/quick crop keeps anchor and pointer; Undo/reopen/export protect same pixels | PR #26 review §1; PR #29 candidate/main 186-per-API evidence |
| D02 | Regular | Readable bounded named saves and export requests | ENGINEERING VERIFIED | R0 | Reject oversized before mutation/picker; healthy bytes and dirty draft survive close/replace | PR #26 review §2; PR #29 candidate/main 186-per-API evidence |
| D03 | Regular / tile | Complete long Draw and cancel | ENGINEERING VERIFIED | R0 | Late bends/end within 500 samples; cancel Draw/erase restores history and output | PR #26 review §3; PR #29 candidate/main 186-per-API evidence |
| D04 | Pro | Complete long lasso and cancel | ENGINEERING VERIFIED | R0 | Late contour selected correctly within 450 budget; operation/history/reopen | PR #26 review §4; PR #29 candidate/main 186-per-API evidence |
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
| R14 | Regular / tile | Draw, Shapes and decoration | PARTIAL | R6 | R29-01/02 Eraser geometry/capacity engineering verified; owner touch acceptance and independent transforms/layers, borders/frames remain | PR #31 candidate/main 193-per-API, five actual Eraser workflows; Revision 4; current 32-text/160-stroke/64-shape limits |
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
| G08 | GIF | Export responsiveness and full saved animation fidelity | PARTIAL | R7 | PR #31 restores the unchanged 15 s Save/6 s sampled-motion fixture gates; original owner media, larger/longer inputs, cadence and device heat remain | PR #31 candidate/main 193-per-API; exact detail-cache pixel equivalence and saved GIF decoding/replay; retained earlier failures |
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

Current implementation references: [Regular controls](../app/src/main/java/com/greenezone/gzphotoeditor/editor/ControlCatalog.kt), [Regular host](../app/src/main/java/com/greenezone/gzphotoeditor/ui/PhotoEditorScreen.kt), [creative model](../app/src/main/java/com/greenezone/gzphotoeditor/editor/CreativeModel.kt), [Pro model](../app/src/main/java/com/greenezone/gzphotoeditor/editor/ProModel.kt), [Pro tools](../app/src/main/java/com/greenezone/gzphotoeditor/editor/ProTools.kt), [GIF model](../app/src/main/java/com/greenezone/gzphotoeditor/editor/GifModel.kt), [GIF project codec](../app/src/main/java/com/greenezone/gzphotoeditor/editor/GifStore.kt), [Collage model](../app/src/main/java/com/greenezone/gzphotoeditor/editor/CollageModel.kt), [build configuration](../app/build.gradle.kts), [current correction evidence](ERASER_REPAIR_VERIFICATION.md), [prior evidence](AUDIT_2_REPAIR_VERIFICATION.md).

Master means the supplied revision 3.1 full blueprint; revision 4 owns later tool-placement overrides. Private originals remain in the owner's supplemental intake. Source presence supports implementation inventory only; execution claims require named tests/runs. Full project transfer, RAW/color, higher limits and later video are not inferred from existing basic editors. The current shared Panel.MASKS enum is compatibility infrastructure, not authority to expose a Regular Masks authoring menu.

Maintain child acceptance cases when executing a family; every child inherits its ID/owner. Do not inflate completion by counting helpers, commits, screenshots or proposed sticker concepts. Preserve all required child behaviors in the roadmap; a new scope decision must name which row changes and why.
````

### B5 — docs/PRODUCT_SPEC.md

[Complete pinned original](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/eb8796c92fa654cb0dac371e35d7645ecd07db8a/docs/PRODUCT_SPEC.md) · Git blob `02e804e93273e5a3c68ae0d3b282d6fb3ef49d61`.

````markdown
# GZ Editor Pro product requirements

## Current scope

**Current engineering result:** [PR #31](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/31) corrects the two independent PR #29 Eraser findings and the observed GIF Save performance failure. Its qualified **0.12.10 Test / code 22** build is ready for the owner’s combined handset pass. Candidate and application merge each pass 193/193 per API29/API35. The exact app commit is `3ad65c533f7667a136d4fa1838afcc06d3f484da`; later documentation-only commits record this result without changing the tested app. [Evidence and APK](ERASER_REPAIR_VERIFICATION.md) · [Next task: R1.1, then R1.2](WORK_LEDGER.md).

Eraser cuts rendered line segments and the swept brush path. A result exceeding the existing stroke limit restores the whole gesture, preserving unrelated artwork, dirty state and history; persistence rejects an invalid count. The GIF optimization keeps exact pixel results, output size, frames, delays and established budgets. Existing layout/style and mode boundaries remain required. This closes the named engineering corrections, not handset acceptance, full Draw/GIF families or commercial release.

### Retained PR #29 baseline

The November 23 audit and PR #26 evidence record the prior application baseline. Lance supplied the independent follow-up review on November 24 and authorized corrections and the complete professional-app roadmap. The former owner-review hold is lifted; the four corrections are now engineering-verified in [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29), with 186/186 Android cases per platform on candidate and merge. That v0.12.9 Test checkpoint is historical and superseded for current phone testing by PR #31. Handset acceptance remains open, and R1.1 is the next engineering task. [Active roadmap](GZ_ENGINEERING_ROADMAP.md) · [Capability inventory](CAPABILITY_MATRIX.md) · [Work ledger](WORK_LEDGER.md) · [Continuity](CONTINUITY.md) · [Release gates](RELEASE_GATE.md).

- **Regular and Collage.** Preserve the familiar photo-first interface, Text, Draw, Shapes, Blur & cover, and independent edits across all 20 Collage tiles. Regular and the selected-tile editor have no visible Masks authoring. Keep saved mask data, rendering and round-trip compatibility. Give all four Crop corners and all four edges density-aware touch targets of at least 48 dp and select the drag target on pointer down. Preserve predictable Freeform and fixed-ratio geometry and exact exported crops.
- **Regular → Pro.** From a loaded standalone Regular photo, selecting Photo editing Pro presents a choice to continue the current image with its edits. Yes opens the current edited result in Pro, preserving its orientation, crop and appearance. No opens an empty Pro editor with “Select an image to edit.” Dismissing the choice leaves Regular open. Regular keeps its own source, edits and history when the user switches back, and stays independent of any Pro changes. Saving, deleting or opening a new Regular image follows the normal Regular commands. A failed handoff keeps the Regular work safe and explains the failure.
- **Pro.** Masks, selections, layers, canvas tools and retouching belong here. Pro has no Text creation or editing. Preserve and render legacy Pro Text layers and keep Shapes accessible. Provide File → Delete current work with an explicit discard warning and confirmation. Cancel keeps the current workspace intact. Confirmation clears the current working edits and history and returns to the original or blank starting state. Preserve source media and named saved projects. Keep the existing Undo/Redo controls. Back closes the active transient context first and then returns to the correct workspace with recoverable work. Make controls meaningful for the selected layer and explain blend behavior and targets without changing correct blend math.
- **GIF.** Playback must visibly advance and stay responsive after trim, Reverse, Looks, speed, canvas and other edits. Verify preview, scrubbing, project reopen and actual animated export on representative media. Use the owner's portrait workflow as a reproduction target. A moving frame number alone cannot demonstrate playback.
- **Later pre-Video scope.** Regular gets an offline sticker menu with at least 500 usable finished assets, transparent custom PNG/WebP imports, editing and persistence, and independent sticker editing on selected Collage tiles. Pro gets the offline Subject & Background tools for subject selection, background removal or replacement, object removal, edge refinement, and reshaping, with editable masks/layers and full-resolution export. See the [complete roadmap](GZ_ENGINEERING_ROADMAP.md).
- **Complete scope.** The roadmap and capability inventory include required RAW/color/catalog/batch, full Pro/Subject & Background, GIF/Collage, Video, reliability and release families. Present buttons and narrow passing fixtures are foundations, not completion of the full professional scope.
- **Account timing.** Play Console is not set up. Lance requires setup last, after a fully functioning app and completion of the Greenezone website. Website implementation is outside this task.
- **Release decisions.** Regular and Collage retain their protected provisional status. GIF and Pro remain open for engineering and handset qualification. The separate Video editor stays disabled until the owner formally locks all four pre-Video workspaces. Bilingual English/Spanish welcome and legal acceptance belong to the final phase. The app stays offline with a one-time purchase and no runtime AI.

## Retained photo and Collage requirements from 0.8

- Preserve the user-approved photo-first dark UI and the familiar position of working Regular tools.
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
- One phone session exercises the consolidated current repair. Include the owner’s GIF workflow, Regular crop and privacy, named Pro and GIF projects, Collage tile edits and relink, and physical Back and picker behavior.
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
````

### B6 — docs/RELEASE_GATE.md

[Complete pinned original](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/eb8796c92fa654cb0dac371e35d7645ecd07db8a/docs/RELEASE_GATE.md) · Git blob `79a392e3d1d112d8f5c968e7fe52b5394db25942`.

````markdown
# GZ Editor Pro engineering and release gates

## Current engineering qualification — PR #31 / 0.12.10 Test

The named R29-01/R29-02 Eraser correction and observed GIF Save performance failure have passed candidate and merged-application qualification. This closes the focused engineering gate and makes **0.12.10 Test** the current combined phone candidate. [Execution evidence, raw receipts and exact APK](ERASER_REPAIR_VERIFICATION.md). The prior 0.12.9 download is historical and superseded for this pass.

- [x] Actual UI regressions show first/later capacity refusal preserves the full gesture baseline, unrelated strokes, dirty state/history, named reopen and decoded output pixels.
- [x] Sparse/long visible segments erase correctly in Regular and the selected Collage tile; cancel, parent history, named reopen and PNG retain sibling independence.
- [x] Exact detail-aware GIF caching preserves uncached Android raster pixels and the established fixture/output/budgets. The observed 28,331 ms failed candidate remains in the evidence chain.
- [x] Final candidate and merge retain all original 186 case identities and add five Eraser plus two cache-fidelity cases. JVM/build/signing and **193/193 per API29/API35** pass with zero failures/errors/skips.
- [x] The merged API35 emulator-exercised APK is independently identified as 0.12.10 / code 22 / phonetest.v22; Android v2 signature and SHA-256 are recorded in the execution receipt.
- [ ] Owner completes the focused combined 0.12.10 phone pass. Automation does not replace original-media, physical Back, OEM/provider, sustained-performance or workspace acceptance evidence.

Qualified application commit: `3ad65c533f7667a136d4fa1838afcc06d3f484da`, tree `1c7e3007204192ea240a7fd8d65cbd00cf7f9511`, identical to candidate `aa86c37c64c5e395d5378df281a0c7583a39d0e0`. [Candidate run](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45480001019) · [merged-main run](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45483297512). Later documentation-only commits record these results without constituting new app/test/Gradle/CI inputs or a new qualified binary. R1.1 remains the selected next engineering task.

### Retained PR #29 checkpoint — historical, not the current download

- [x] R0 fresh-cover/manual-Crop, bounded Regular save/export, complete long Draw/cancel and Pro lasso have regression evidence against actual flows.
- [x] Exact corrected candidate and merged application pass JVM and full API29/API35; preserve prior 174 cases and established GIF budgets. Independently review failure receipts and output assertions.
- [x] Historical exercised v0.12.9/code 21 phone APK has source/run/artifact/hash/package/signature recorded in PR #29. Its four original engineering corrections remain credited; PR #31 supplies the current phone artifact.
- [ ] Owner completes one combined phone pass with the focused new cases plus retained field cases below. Physical acceptance remains separate from CI.
- [ ] All required families in the [capability inventory](CAPABILITY_MATRIX.md) complete the [roadmap](GZ_ENGINEERING_ROADMAP.md); a repaired current build is not the entire professional app.
- [ ] Lance formally locks all four pre-Video workspaces before Video implementation.
- [ ] All five editing workspaces complete/accepted before final English/Spanish welcome and customer legal acceptance.
- [ ] Fully functioning qualified app AND Greenezone website complete before Play Console setup. Account is not set up; website work is outside this repository task.
- [ ] At that final stage: verify actual account-specific requirements/current policies, rights/notices, release signing/AAB/splits/same-ID upgrade, store testing/listing and explicit owner publication approval.

[Single work ledger and current execution receipts](WORK_LEDGER.md). Historical PR #29 app merge `c9efc5aa0673f21b98f150733582a24996b34d10`: candidate and merged-main each pass Verify and 186/186 on both API29/API35; exact APK identity is in [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29). The earlier successor review hold is lifted by the supplied review and owner's engineering request. Existing evidence below retains its original scope/date; it does not close new defects or full release.

## Retained field-correction acceptance

- [ ] Open a rights-cleared 1080×1920 portrait video of approximately five seconds from the GIF workspace. Compare untouched playback with trim around 2.19–4.99 seconds, Reverse, B&W or Punch, speed, crop, Undo/Redo and rapid edits. Confirm changing picture content, correct direction and responsive controls. Reopen a saved project and decode the exported animation independently to check orientation, motion, color, duration and frame schedule. Include 720p60 B-frames and representative phone media.
- [ ] Enter Regular Crop through the normal UI. Drag four corners, four edges and the interior in Freeform and fixed ratio with touch input. Verify predictable anchoring, pointer-down acquisition, letterbox interactions, expected crop pixels and saved-project reopen. Confirm Regular and Collage tile editing omit Masks authoring and retain Text, Blur & cover and old mask-bearing projects.
- [ ] Enter Pro from an edited portrait in Regular. Confirm Yes transfers the current appearance and geometry. Confirm No opens an empty editor and dismissal keeps Regular open. Switch back via the menu and Back, and verify Regular's source, edits, history and output still match. Check Pro Text authoring is absent and legacy text still renders, and exercise File → Delete current work with Cancel and Confirm. Verify originals, Regular edits and named saved projects survive.
- [ ] Exercise Pro layer selection, transform, masks, opacity/blend, painting and repair through pixel change, Undo/Redo, save/reopen and export. Check Back through active tools, menus, dialogs, keyboard and workspaces. Verify physical gestures from both edges, half-cancel and rapid double Back on a handset, including picker/IME ownership.
- [x] Audit 2 engineering gate for v0.12.8 Test. The exact candidate and merged application commit passed JVM verification and 174 of 174 instrumented tests on Android 10 and on Android 15. The emulator-exercised APK identity and hash are recorded in the repair evidence. Owner phone checks above remain open. [Repair evidence](AUDIT_2_REPAIR_VERIFICATION.md) · [Candidate workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45337105839) · [Merged-main workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45339000608) · [APK artifact](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45339000608/artifacts/14895803102).

### November 23 independent audit cases

Use the [audit repair evidence index](AUDIT_2_REPAIR_VERIFICATION.md) and [PR #26](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/26) for the exact candidate and merged application commit, receipt archives, and APK identity.

- Exercise default Save across named project opens and Undo/Redo in Pro and GIF. Check confirmed replacement and deletion, and verify old and newly written editable projects after reopen. Reject a Pro recipe that exceeds its reader budget while preserving the previous save.
- Follow protected source pixels through flips, quarter turns, straightening, crop and reopen. Check decoded exported pixels for Regular and a selected Collage tile. Verify named deletion, dirty-photo replacement, duplicate-source tile movement, relink and staged storage failure.
- Confirm a delayed Android picker result can write the original immutable recipe after an Activity recreation. Check cancellation and duplicate delivery, and verify retry to a fresh destination after an ambiguous write. Check real process reclamation with a PID change and a separate picker on a handset.
- Verify video GIF crop, aspect, timeline, delays, export time, reopen and playback with independent media decoders while keeping normal-resolution baseline performance. Check both Android 10 and 15.
- Physical Back and provider-specific behavior remain handset acceptance cases. Exercise both edges, canceled gestures, picker and keyboard ownership, source grant loss, and a provider with aliases or a partial write.

### Regression cases found after Revision 4

- In Pro, check brush opacity and masking against an occluding image while the pointer is still down. Check that live erasing reveals the lower layer or transparency and that a second pointer cancels without an Undo entry. Verify committed paint and erase through raster output and Undo/Redo.
- In Regular, open Histogram from More. Check RGB and luminance bins, clipped channel counts, a visible Exposure edit, and Before view. Keep the measurement on displayed preview pixels so its precision and scope match the app's current sRGB pipeline.
- Measure the 1080×1920 edited portrait fixture through the Save GIF command at 180×320 with the complete 88-frame schedule. In the automated test, save within 15 seconds, decode every frame delay and both reverse endpoints, and play the saved file after reopening. Retain the six-second edited preview check.
- On each Android runner, compare the production export of the rotated 720p60 fixture to the per-timestamp reference export. Keep the same canvas size, duration, frame schedule and moving-pixel checks, and require the production path to finish in half the reference time or less.
- Check a small crop's edge midpoint and interior independently of its corners. Exercise locked ratios on wide and tall photos, the first gesture after reopening Crop, and off-center positioning in older saved projects.
- Open a different named Pro project, Undo and Redo across that boundary, and verify Delete current work returns to the correct baseline. Confirm the separate named-delete prompts before deleting a save. Reorder a layer, Undo and Redo, and verify subsequent tools still target it. Exercise a pending tool draft when another layer's media becomes unavailable.
- Widen both ends of a long trim at a fixed frame rate. Keep the selected FPS and stop at its exact frame-budget limit. Cover unspecified decoder color tags, edited playback of metadata-rotated clips, failed-codec fallback and actual edited export.
- Move a square crop off-center, apply a Look, hand it into Pro, and verify the rendered crop, saved edit recipe, and intact Regular draft.

## Historical v0.8 Collage and photo verification criteria
- [ ] Main original photo edit screen unchanged aside from GZ brand on landing and Create collage entry.
- [ ] Crop/blur/text/shapes/brush active guides persist while main toolbar is hidden, and editing remains possible.
- [ ] Pure layout checks: all seven arrangements, counts 1–20, nonoverlap, bounds, tap selection, max spacing and budget.
- [ ] Android bitmap render checks: 2 independent edits, exactly 20 tiles, no source overwrite, PNG/JPEG image output.
- [ ] Local collage project encode/decode/save/load with distinct per-tile color, privacy and creative layers.
- [ ] Collage Undo/Redo restores layout, tile order and per-tile edits, with bounded snapshots and one checkpoint per slider gesture.
- [ ] Missing photo raises tile-specific error and offers replacement rather than silently omitting content.
- [ ] Import count hard capped at twenty, including Android picker fallback.
- [ ] Collage exports via existing seven real file encoders/social sizing choices; no AI, ads, network or watermarks.
- [ ] All prior JVM, Android raster and Compose tests continue passing.
- [ ] PR head CI pass, exact SHA merge, merged-main CI pass, verified debug APK artifact.

## One phone field pass
- [ ] Add 2 photos side by side; edit each tile independently in full editor, return with Done.
- [ ] Add 6–20 photos (as many as convenient), try Grid/Mosaic/Featured templates and aspect, spacing, margin/background.
- [ ] Reorder, remove, replace; save named collage, reopen and verify per-tile adjustments.
- [ ] Export JPEG and PNG, check distinct photos/content preserved, no watermarks.
- [ ] In regular editor, hide tools while editing crop/blur/text/brush; selected handles must stay present.
- [ ] Check original standalone photo editor, saved projects, privacy black bars and JPEG export still work.

## Commercial gate remains open
Native app profiling, large-photo/20-tile memory pressure, long session/ANR, color accuracy, Android API/device coverage, media URI grant lifetime, trademark/patent/license/SBOM and privacy/Play/store legal checks, production signed AAB. No final 1.0 claim.

Refer to `docs/GZ_ENGINEERING_ROADMAP.md` for lawful original-code Lightroom-class **non-AI** features, RAW/license decisions and later video editing.

## Historical v0.9 verification criteria
- [ ] Top-right mode dropdown on Regular, Pro, GIF and Collage. Video disabled / Coming soon. Regular toolbar compact, no destroyed existing tools.
- [ ] Pro image compositor: independent image edits, blend/position/size/opacity/visibility/order/undo/persist/export.
- [ ] Real GIF89a multi-frame output with tested Android decoder, timing/loop/ordering, saved project/undo and safe source ownership.
- [ ] No new permission, dependency, AI, ad, account, watermark or network regressions.
- [ ] Exact PR HEAD passed CI, merged main Android bitmap/Compose tests passed, expected APK artifact exists.
- [ ] Historical 0.8 and 0.9 physical phone check `PHONE_TEST_WAVE8.md`; commercial release and professional/RAW/video work remain open.

## Historical v0.12.1 independent AAA audit criteria

The commercial gate above remains open.
- Source audit: `docs/AAA_RELEASE_AUDIT_013.md`. Owner requested complete AAA verification; **not achieved** at this stage.
- [ ] API29 minimum-device and API35 full Android emulator tests green at identical source SHA.
- [ ] Real GIF color-gradient and moving-transparent-object output decoded without ghosting or color damage.
- [ ] GIF Save As works with a write-only document provider; original source and existing files remain unchanged.
- [ ] GIF exports respect device free-space quota, cancel gracefully and clean up incomplete outputs.
- [ ] Pro save/overwrite, interrupted AtomicFile recovery, fail-closed malformed input, Undo/Redo and source URI access revalidated.
- [ ] Long-session, large-media, thermal/battery/ANR, wide color gamut/ICC and multiple real phones validated.
- [ ] True Photoshop/GIMP parity: advanced layers/groups, masks, smart selections, retouching and production-scale tools.
- [ ] Lightroom-class RAW/16-bit/float/ICC management, catalog, batch and device profiling.
- [ ] Accessibility and complete tutorial onboarding, licensing/SBOM/commercial payments and production Play-signed App Bundle.
- Signed side-by-side APK may be supplied only as *noncommercial QA* with explicit warnings; it is **not** an AAA or Play Store release.
````

### B7 — docs/PROFESSIONAL_ENGINEERING_ROADMAP.md

[Complete pinned original](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/eb8796c92fa654cb0dac371e35d7645ecd07db8a/docs/PROFESSIONAL_ENGINEERING_ROADMAP.md) · Git blob `a2697f4add074c29dad71a4303c49aecdc5e1769`.

Identical to the full text in **A7** above (same verified Git blob). This is shared snapshot content, not an omitted or newly summarized task list.

### B8 — docs/UNIFIED_PRODUCT_SCOPE_09.md

[Complete pinned original](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/eb8796c92fa654cb0dac371e35d7645ecd07db8a/docs/UNIFIED_PRODUCT_SCOPE_09.md) · Git blob `51792479496322564ffd4f3b80be04af3d43e9cb`.

Identical to the full text in **A8** above (same verified Git blob). This is shared snapshot content, not an omitted or newly summarized task list.

### B9 — docs/PRO_GIF_REBUILD_011.md

[Complete pinned original](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/eb8796c92fa654cb0dac371e35d7645ecd07db8a/docs/PRO_GIF_REBUILD_011.md) · Git blob `9e7d6be411c8c8f4f1e9a8a1c3b43439be0e2eca`.

Identical to the full text in **A9** above (same verified Git blob). This is shared snapshot content, not an omitted or newly summarized task list.
