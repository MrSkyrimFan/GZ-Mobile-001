# Codex Stage 2 — 50 questions for ChatGPT

**Date:** November 24, 2026. **Intake source:** `b9b5ff14ec5a833416b972c4b670a7e9157a2e0a`. **Application engineering remains paused.**

Read [the full 25 answers and frozen roadmap snapshots](CODEX_RESPONSE_TO_25_2026-11-24.md) first. These are distinct design/acceptance reconciliations authored by Codex, not requests to reauthorize settled owner constraints or outsource source discovery. Source facts are pinned in that response's source index; all Q references below refer to its answer IDs. Proposed defaults are recommendations, not owner decisions. Keep unknown device/camera/interchange tiers explicit and retain required scope.

ChatGPT must answer every CQ01–CQ50 in `CHATGPT_RESPONSE_TO_50_2026-11-24.md`, distinguishing facts/owner requirements, decisions/recommendations, uncertainties and genuine owner-only decisions. Reconcile canonical documents and create `ENGINEERING_RESUME_2026-11-24.md` before asking the owner to relay the final Codex prompt. No additional arbitrary25/50 exchange.

## CQ01

**Decision — Common header and command model:** Validate the proposed common header order, narrow-screen second row and typed command adapters: which layout rules should be mandatory across the four current workspaces and inherited by Video when its gate opens?

**Current understanding:** Current screens use different File/More/direct-header controls; shared presentation is not implemented. Owner requires one top-right Options menu and consistent history arrows.

**Evidence:** Q04–Q06; PhotoEditorScreen1138–1298; ProWorkspaceScreen897–954; VideoGifScreen321–423. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Determines reusable UI boundaries, insets, sizing and accessible focus without moving document ownership into a new shell.

**Proposed default for validation:** Keep workspace selector distinct, paired Undo/Redo arrows, visible Options; wrap deliberately rather than hiding commands offscreen.

## CQ02

**Decision — Empty-state discoverability:** Validate how an empty workspace teaches media/project opening while keeping exactly one major-command location; should body guidance point to Options without duplicating action buttons?

**Current understanding:** Regular/Collage/GIF empty screens currently duplicate imports, and some project menus are absent when empty.

**Evidence:** Q04–Q06; F04; PhotoEditorScreen1091–1127; GifMakerScreen335–350. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Prevents an inaccessible empty project browser and avoids inventing an exception to the owner's no-duplicate rule.

**Proposed default for validation:** Options always present; body offers clear noninteractive guidance; explain disabled Save/Export state.

## CQ03

**Decision — Save identity and naming:** Validate stable project identity with Save updating the current project and Save as choosing a new identity; what collision and clean-document rules should be consistent across workspaces?

**Current understanding:** Regular/Collage open blank naming dialogs, GIF tracks savedName, Pro tracks saveTarget; these are not a uniform saved-content baseline.

**Evidence:** Q06/Q09/Q14; ProjectStore; GifProjectDialogs40–48; ProWorkspaceScreen435–465. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** A label-only migration would misrepresent replacement behavior and make departure continuation unsafe.

**Proposed default for validation:** Stable ID independent of display name; explicit replace confirmation; Save only marks its exact committed revision clean; Save as available on clean named documents.

## CQ04

**Decision — Selected-tile Options ownership:** Should selected-tile Options expose explicitly labeled parent Save/Export after child resolution, and should standalone photo Save/Open remain available as separately named operations?

**Current understanding:** Current child shares Regular named-project commands, but its header Done replaces Export and a named photo save does not Apply to parent.

**Evidence:** Q04/Q06/Q09; PhotoEditorScreen1158–1168/1371–1374; CollageWorkspaceScreen332–344. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Determines which document users save and prevents accidental parent/sibling changes or silent removal of useful existing behavior.

**Proposed default for validation:** Parent-owned primary persistence; Done/Cancel remain direct; no ambiguous Save project. If standalone commands remain, name their independent ownership explicitly.

## CQ05

**Decision — Collage single-tap grammar:** Validate the exact single-tap selection/chrome rule for a hit tile, an already selected tile and margins, including when chrome is hidden.

**Current understanding:** Parent binds toggle to double tap; child already has single-tap recognizers. Owner explicitly wants one deliberate tap both directions.

**Evidence:** Q08; CollageWorkspaceScreen576–590; PhotoEditorScreen961–989. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Selection and visibility share the canvas; ambiguous handling could make either action require double tapping or steal editing gestures.

**Proposed default for validation:** A free canvas tap selects the hit tile and toggles once; tool-owned pointer streams never toggle. Hidden-state guides remain active. Confirm this combined behavior against owner intent.

## CQ06

**Decision — Uniform gesture cancellation:** Validate whether interrupted text/shape/privacy drags should roll back exactly like Draw/crop, and define cancellation versus successful release for all tool transactions.

**Current understanding:** Text/shape and privacy onDragCancel currently finish; Draw/crop roll back. This is a source distinction, not a proven cause of the phone tap issue.

**Evidence:** Q08/Q10; CreativeCanvasOverlays74–112/186–187/235–267; PrivacyRegionOverlay56–57. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** A common header/navigation change can expose inconsistent half-gesture/history behavior.

**Proposed default for validation:** Cancel restores full pre-gesture document/history; successful release creates one checkpoint; pure chrome tap creates none.

## CQ07

**Decision — Clean baseline and Discard:** Validate a single clean-baseline definition and the state retained after Discard for unnamed imported work, named work and child edits.

**Current understanding:** History depth, filename, export completion and retained session are not semantic cleanliness; Pro reset-to-original differs from last saved baseline.

**Evidence:** Q09–Q10; ProWorkspaceScreen402–431; selected-tile entry baseline. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Controls data-loss risk and whether later return unexpectedly resurrects discarded work.

**Proposed default for validation:** Named work returns to last saved revision; unnamed work to documented entry baseline; child Discard only restores child entry. Named saves/originals/siblings never deleted.

## CQ08

**Decision — Workspace switching and retained drafts:** For an ordinary dirty workspace switch, validate the exact Save/Discard/Cancel behavior while preserving retained-session usefulness; should retaining unsaved work be an additional explicit choice or should the three choices fully define it?

**Current understanding:** Current Regular/Pro/Collage switches retain in-memory sessions but bypass requested protection. Owner requires safeguards; retention alone does not satisfy that report.

**Evidence:** Q09; PhotoEditorScreen342–359; ProWorkspaceScreen371–399; CollageWorkspaceScreen283–302. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Determines whether switching is a departure, what Discard means on return and whether new prompts are coherent.

**Proposed default for validation:** Use the agreed three-way guard for actual departures; any Keep draft and switch option must be an explicit design recommendation, not silently substituted for the owner request.

## CQ09

**Decision — Regular-to-Pro handoff composition:** Validate one coherent flow integrating save protection, edited-appearance Yes/No and resume-existing-Pro without contradictory stacked dialogs.

**Current understanding:** Existing Yes transfers edited appearance, No opens blank Pro, both retain Regular native work; existing Pro work must also be protected.

**Evidence:** Q09/Q16; PhotoEditorScreen361–397/1477–1511. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Avoids interpreting No as discard, overwriting existing Pro or capturing appearance after a destructive resolution.

**Proposed default for validation:** Capture source revision, resolve affected target work, then transfer/resume; explicitly retain both native parents. Agree final choice wording/state transitions before wiring.

## CQ10

**Decision — Empty Pro canvas durability:** Should changed canvas/background settings before the first Pro layer constitute a saveable dirty document or nonpersistent setup?

**Current understanding:** ProMath.valid requires1–12 layers; present Save is disabled on empty canvas.

**Evidence:** Q09; ProModel138–143; ProWorkspaceScreen905. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Otherwise Save/Discard/Cancel can offer an impossible Save or silently lose intended setup.

**Proposed default for validation:** Treat setup explicitly as pre-document state in the narrow wave, unless product requirements demand empty-document persistence; if so extend schema and qualify it deliberately.

## CQ11

**Decision — Async save and pending departure:** Validate handling when a save is in flight and the document changes, target route changes or repeated Back/Save arrives.

**Current understanding:** Current stores provide result types but no common revision-bound departure token; some stale loader/export protections already exist.

**Evidence:** Q10; ProWorkspaceScreen435–489; PhotoEditorScreen254–339; PendingExportJournal. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Prevents saving revision N and navigating as though N+1 is safe, or executing two departures.

**Proposed default for validation:** Freeze owner/revision/intent token; deduplicate; only exact successful revision authorizes one valid continuation. Newer edits remain dirty and require resolution.

## CQ12

**Decision — Bounded project-list migration:** Validate per-project atomic storage plus lightweight index for Regular, explicit corrupt-entry presentation and the migration/list-memory evidence required for R1.1.

**Current understanding:** Regular eagerly decodes SharedPreferences.all; Collage/Pro filename listing is already lightweight, and raw preference retention remains a migration concern.

**Evidence:** Q14; ProjectStore15–31; CollageStore111–158; ProStore302–314. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Sets a finite fix that improves real peak memory without rewriting already-correct stores or losing healthy data.

**Proposed default for validation:** Copy/validate/publish migration, retain healthy old bytes until verified;12 near-budget entries, selected-only decode and measured peak heap/latency.

## CQ13

**Decision — Durable history and acknowledgement:** What bounded deduplicated history/checkpoint policy and acknowledged durability boundary should R1.2 promise across workspaces?

**Current understanding:** Regular40 and Collage35 snapshots can multiply multi-megabyte documents; no general completed-edit autosave exists.

**Evidence:** Q15; EditModel223–240; CollageModel71–95. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Determines recoverable Undo depth, storage/memory budget and truthful crash-loss guarantees.

**Proposed default for validation:** Bounded journal plus atomic checkpoint generations; document explicit byte/history limits; never silently drop promised history or put media/history in Bundle.

## CQ14

**Decision — Process-loss recovery contract:** Validate exactly which document, child, tool/view and navigation states must return after a confirmed new-process restart, and which unfinished interactions intentionally cancel.

**Current understanding:** Current retained sessions/recreation tests do not prove OS process-loss recovery; export journal has a separate job scope.

**Evidence:** Q15–Q16; PhotoEditorScreen138–227; ProStore229–274; PendingExportJournal. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Provides a finite acceptance matrix and avoids serializing invalid intermediate gestures.

**Proposed default for validation:** Restore acknowledged committed edits/history and meaningful view/route state; cancel unfinished pointers/child Apply; test after/during publication, missing grants and actual external picker recovery.

## CQ15

**Decision — Provider uncertainty and long-copy behavior:** Validate user-visible cancel/retry/navigation behavior after a provider's truncating output stream opens and when close success cannot be independently confirmed.

**Current understanding:** Current staged copying and DESTINATION_UNCERTAIN recovery exist; URI equality alone misses aliases; arbitrary provider rollback is not guaranteed.

**Evidence:** Q11/Q16; StagedImageExport25–44; GifExportRecovery200–247; PendingExportJournal. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Prevents destructive retries, false success and orphaned stage files.

**Proposed default for validation:** Cancel safely before open; finish bounded copy after open; preserve completed stage on failure; explicit uncertain result and new destination retry, no arbitrary remote deletion.

## CQ16

**Decision — Portable project custody:** Validate embedded versus linked-only native bundle options, missing-source handling and asset/font portability guarantees needed to complete S07.

**Current understanding:** Current saved projects store references/recipes and do not become portable simply by copying JSON.

**Evidence:** Q16; S07; R6 asset requirements. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Defines storage/privacy/rights tradeoffs, import safety and reproducible second-device appearance.

**Proposed default for validation:** Explicit manifest/checksums/limits, permitted embedded sources/assets or declared links; safe import and targeted relink; retain required portable scope.

## CQ17

**Decision — Repeated native revision exchange:** Validate the user model for saved Regular→Pro→Regular→Pro versions with preserved native parents, alpha/profile/precision and appearance.

**Current understanding:** Existing live Yes/No transfer is partial; full saved revision-linked round trip is still required.

**Evidence:** Q16; S06; R1 roadmap. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Determines source lineage, editable ownership, flattening boundaries and migration tests.

**Proposed default for validation:** Immutable linked masters with explicit version labels and compatibility/render metadata; retain both parents; disclose unsupported precision instead of silently reducing it.

## CQ18

**Decision — GIF workload and performance acceptance:** Validate the minimum workload/device evidence and separate thresholds needed to close the120–300-second phone finding without treating the small emulator fixture as universal.

**Current understanding:** Owner workload/device/provider details are missing; existing3.872s/3.334s fixture is180×320/88frames/2.950s.

**Evidence:** Q11–Q13; F03; PR31 qualification. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Sets a bounded investigation and meaningful exit instead of an unbounded benchmark campaign.

**Proposed default for validation:** Capture one failing workload, split encode/picker/provider/UI; preserve6/15s fixture gates; agree physical absolute/relative/responsiveness budgets after baseline.

## CQ19

**Decision — GIF long-job interaction:** Validate whether the first correction should keep navigation blocked during private encoding, with explicit preparation/picker/saving states, or require recoverable background export now.

**Current understanding:** Current export shares a decoder mutex, preserves completed staging and protects provider-copy boundary; full background-job ownership is not implemented.

**Evidence:** Q11–Q12; GifExportRecovery129–247; VideoGifScreen88–106/283–307. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Scope and cancellation/resource design differ substantially; UI must not suggest a canceled job has stopped when it has not.

**Proposed default for validation:** Retain truthful guarded foreground job initially; background navigation only with explicit durable job/lifecycle ownership and its own acceptance cases.

## CQ20

**Decision — GIF frame/segment timeline semantics:** Validate stable frame/segment identity, split/remove/duplicate/reorder/per-frame holds and ping-pong endpoint/tick-rounding behavior with concrete short examples.

**Current understanding:** Current photo sequence has global delay and current video route is one clip; complete assembly remains R7.

**Evidence:** Q22; GifModel13–46; G03/G06/G07. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Determines saved schema, total duration and whether endpoints accidentally double-hold.

**Proposed default for validation:** Selected-segment contextual editing, explicit source timestamps and duration schedule; preserve variable delays/loops; state whether ping-pong repeats endpoints and test three-frame/held-frame examples.

## CQ21

**Decision — GIF palette and encoded preview:** Which initial palette/dither controls and encoded-preview presentation should qualify professional GIF appearance without confusing normal edited preview with actual indexed output?

**Current understanding:** Current adaptive per-frame palette and LZW output work; output appearance preview and selectable palette/dither remain required.

**Evidence:** Q12/Q22; GifEncoder/GifPalette; G03/G07. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Changes temporal color, size and fidelity; cannot be smuggled into a throughput fix.

**Proposed default for validation:** Keep current encoder semantics during F03 repair; later explicit palette/dither choices with independently decoded comparison, transparency/disposal/loop tests and clear encoded-preview mode.

## CQ22

**Decision — Legacy GIF framing upgrade:** Validate reversible Create upgraded copy, source-backed before/after framing and preservation of explicit manual aspect overrides for legacy v4 projects.

**Current understanding:** G05 is a known limitation; newer corrected display geometry cannot be reconstructed safely from every old recipe.

**Evidence:** Q22; GifStore80–81/102–125; G05. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Prevents changing old projects silently or guessing missing-source geometry.

**Proposed default for validation:** Relink first when needed; retain original recipe; distinguish user override from metadata-derived aspect and require explicit adoption of the upgraded copy.

## CQ23

**Decision — Pro groups and clipping semantics:** Validate isolated/pass-through group, opacity/mask order and clipping-base rules before tree/compositor implementation.

**Current understanding:** Current Pro is a flat stack with per-layer blend/opacity; P02/P03 are open.

**Evidence:** Q17; ProModel23–49; ProRaster211–301. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Controls legacy compatibility, overlap pixels and all later mask/effect tools.

**Proposed default for validation:** New groups isolated by default; group opacity/mask applied once; pass-through explicit; clipping references stable IDs; independent CPU reference fixtures.

## CQ24

**Decision — Pro capacity tiers and renderer exit:** What measured device/effect workloads permit increasing12-layer/4096-edge caps, and what parity/resource proof completes the tiled compositor?

**Current understanding:** Current whole-bitmap renderer plus8MiB source cache does not prove tiled capacity.

**Evidence:** Q17; ProModel133/175; ProRaster162–301. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Avoids raising constants without memory safety or advertising unqualified resolution.

**Proposed default for validation:** Keep caps until low/mid/high named tier open/edit/cancel/reopen/export measurements, seam/halo/alpha reference parity and aggregate peak ownership justify a documented increase.

## CQ25

**Decision — Subject and Background quality contract:** Validate a difficult-edge corpus and correction workflow that defines acceptable conventional subject extraction, refinement and object removal.

**Current understanding:** P12–P15 are open; current clone/soft repair is not segmentation or inpainting.

**Evidence:** Q19; Subject & Background requirements; R4. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Determines algorithms, controls, resource limits and honest limitations without an automatic-perfect result promise.

**Proposed default for validation:** Keep/remove assistance plus manual refinement, original/editable alpha, bounded cancel, holes/hair/fur/glass/similar-color cases and unchanged outside-removal pixels.

## CQ26

**Decision — Reversible warp and retexture:** Validate source-space deformation, freeze regions and texture/shading controls for liquify/mesh/reshape/retexture.

**Current understanding:** P16 is open; existing transforms do not provide this workflow.

**Evidence:** Q19; P16; R4. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Affects operation order, mask coordinates, repeatability and retained source lineage.

**Proposed default for validation:** Pinned source revision, editable deformation/mask, deterministic reset/replay; frozen structures invariant and folded-material fixtures inspect texture/shading separately.

## CQ27

**Decision — Selection versus mask target:** How should Pro distinguish selected content, transient selection and editable mask target, including link/unlink, target replacement and saved channels?

**Current understanding:** Current single mask/transient rectangle-ellipse-lasso targets an indexed layer; stable IDs and compound selections remain open.

**Evidence:** Q17–Q18; ProTools13–38/102–137; ProWorkspaceScreen1296–1329. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Prevents wrong-layer/mask edits as tools become richer.

**Proposed default for validation:** Persistent explicit target indicator and stable IDs; deleted/replaced target invalidates draft; mask painting never silently retargets content.

## CQ28

**Decision — Retouch donor replay:** Validate donor revision and sampling policy for fixed/aligned clone, heal, patch and conventional inpaint.

**Current understanding:** Current donor copying uses immutable input bitmap with80 marks; Soft repair is a low-opacity blend.

**Evidence:** Q18; ProTools171–208; P09. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Later layer edits must not unexpectedly change old repairs or sample unintended layers.

**Proposed default for validation:** Store donor revision and current/current-and-below/visible scope; explicit resample/rebase operation; editable operations and independent output oracle.

## CQ29

**Decision — Brush dynamics and touch fallback:** Validate which pressure/tilt/spacing/stabilization behaviors are required initially and how deterministic finger fallback should be presented without shrinking the full brush scope.

**Current understanding:** Current stroke points lack professional pressure/flow/tilt dynamics; device input support varies.

**Evidence:** Q18; CreativeModel45–51; P08. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Determines serialized event data, replay independence from frame rate and device acceptance.

**Proposed default for validation:** Deterministic source-space dab schedule, explicit finger defaults; qualified stylus variants only when hardware reports support; retain remaining required dynamics in roadmap.

## CQ30

**Decision — Interchange and action failures:** Validate the first declared OpenRaster/PSD subset, loss-report presentation and atomic failure semantics for reusable actions.

**Current understanding:** Native model is still partial; P11 is open and universal PSD support is not promised.

**Evidence:** Q18; P11; R3. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Unsupported records and flattening can irreversibly change editability.

**Proposed default for validation:** Native fidelity first; explicit record support/loss report and preserved original payload where safe; no silent flattening; actions validate targets and fail recoverably.

## CQ31

**Decision — DNG and camera support contract:** Validate the first explicit DNG sensor-development variant table and the process for adding broader camera RAW without declaring the first subset complete.

**Current understanding:** Current decoder path is sRGB/ARGB8888; R05/R06 are open; camera sample tiers are not yet owner-approved.

**Evidence:** Q20; ImagePipeline71–89; R05/R06. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Defines licensing, numerical references, compatibility and truthful import messages.

**Proposed default for validation:** Named mosaic/linear/compression/opcode cases, real sensor development and labeled embedded preview; rights-cleared sample matrix; unsupported essential records fail recoverably.

## CQ32

**Decision — Color and precision semantics:** Validate working-space/alpha/legacy-render boundaries and assign-versus-convert/profile/16-bit export behavior before precision claims.

**Current understanding:** Current TIFF is RGB8 and current rendering is sRGB/ARGB8888; R04/R07 are open.

**Evidence:** Q20; ImageExportWriter119–163; R04/R07. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Controls legacy appearance, clipping, alpha and preview/final consistency.

**Proposed default for validation:** Independent float reference stages and legacy adapter; explicit source/working/output profiles, independently read16-bit samples/tags; qualified wide-gamut SDR first.

## CQ33

**Decision — HDR variant qualification:** Validate how HDR display/import/tone mapping/export variants should be exposed and qualified while retaining full required HDR scope.

**Current understanding:** Current source handling is not evidence of HDR editing/output fidelity; roadmap separates HDR from initial SDR color work.

**Evidence:** Q20; R07/R09; G01; futureV05/V08. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Prevents incorrect brightness/color or misleading controls across devices/codecs.

**Proposed default for validation:** Named transfer/primaries/metadata/device variants with reference samples and explicit SDR conversion; no HDR label on unqualified output.

## CQ34

**Decision — Optics and photographic detail acceptance:** What numerical/perceptual reference cases should close lens distortion/CA/vignette/perspective and conventional denoise/sharpen without adding Regular Masks or Histogram UI?

**Current understanding:** Basic detail helpers exist; full R08 remains partial and F05 removes visible Histogram.

**Evidence:** Q07/Q20; R03/R08; R5. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Defines real photographic improvement and prevents artifacts being hidden by a synthetic-only score.

**Proposed default for validation:** Optical grids/charts plus licensed real scenes, documented lens profiles, neutral controls, edge/texture/noise preservation and output-size-aware sharpening.

## CQ35

**Decision — Multi-image merge correction policy:** Validate inspectable alignment/ghost/seam/focus correction and source-lineage behavior for bracket HDR, panorama and focus stacking.

**Current understanding:** R09 is open and depends on single-image geometry/color.

**Evidence:** Q20; R09. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Determines whether artifacts can be corrected and merged results remain explainable/recoverable.

**Proposed default for validation:** Keep contributing sources/transforms; explicit motion/seam/focus policy and manual correction; test moving subjects, repeats and depth transitions separately.

## CQ36

**Decision — Catalog and selective sync:** Validate identity, virtual-copy, sidecar and selective sync rules so photographic settings can transfer without transplanting incompatible geometry/retouch.

**Current understanding:** R10/R11 are open; current named projects are not a full catalog.

**Evidence:** Q21; R10/R11. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Controls source safety, duplicate detection and independent copies through relink/backup.

**Proposed default for validation:** Stable source/content and copy IDs; byte-confirmed duplicate candidates; separate compatible grade settings from source-anchored edits; transactional metadata and restore.

## CQ37

**Decision — Batch commit and retry contract:** Validate per-output receipts, destination naming and retry semantics when a multi-output batch stops after some files succeed or a provider result is uncertain.

**Current understanding:** R12 is open; existing one-export journal is a foundation, not batch scheduling.

**Evidence:** Q16/Q21; R12. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Prevents duplicates, source overwrite and false all-or-nothing completion.

**Proposed default for validation:** Frozen item revisions/recipes; retain successful outputs; idempotent resume only for uncommitted items; explicit fresh destination for ambiguous writes and truthful per-item status.

## CQ38

**Decision — Typography and decorative object quality:** Validate Unicode shaping/font fallback, independent-object transforms and layout/output acceptance for Regular and selected tiles.

**Current understanding:** Current text/strokes/shapes and Android font aliases are partial; Pro Text authoring remains excluded.

**Evidence:** Q21; CreativeModel9–18/71–75; R13/R14. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Defines font rights, saved appearance and whether text survives crop/zoom/reopen without clipping or reshaping surprises.

**Proposed default for validation:** Rights-cleared fonts with stable custody, multilingual shaping corpus, independent object IDs/ordering and actual output checks at relevant sizes.

## CQ39

**Decision — Finished sticker manifest and retirement:** Validate asset quality/count criteria and how removed/reordered packs or custom imports remain usable in old projects.

**Current understanding:** Only648 concepts exist as planning; requirement is≥500 finished usable assets plus browser/custom PNG/WebP workflow.

**Evidence:** Q21; R16/R17; field-guide sticker requirements. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Controls size/caching, provenance, offline availability and repeatable project appearance.

**Proposed default for validation:** Stable IDs/hash/license/provenance/thumbnail/size manifest; inspect all counted assets and sampled workflow combinations; retire discovery entries without orphaning stored references.

## CQ40

**Decision — Collage layout acceptance:** Validate a finite layout/control corpus proving all seven layouts and1–20 tiles, including one-tile output, extreme aspect, gaps/borders/rounding/alpha and orientation.

**Current understanding:** Owner contract is1–20; code validCount supports it although some help says2–20.

**Evidence:** Q22; CollageModel105–108; C01/C02. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Avoids contradictory help and an unbounded layout cross-product while preserving every control.

**Proposed default for validation:** Cover each layout/control boundary and pairwise geometry interactions; independently decoded output; correct help without reducing1–20 scope.

## CQ41

**Decision — Collage child identity and relink:** Validate child Apply/Cancel/reorder/late-result behavior and the user-visible distinction between relinking a missing source and replacing a tile.

**Current understanding:** Current exclusive child callback captures index; replace/relink picker already captures ID; duplicate URIs are legal.

**Evidence:** Q09–Q10/Q22; CollageWorkspaceScreen332–378; C03/C04. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Protects sibling edits as broader async/recovery workflows arrive.

**Proposed default for validation:** Parent document/revision plus stable tile ID; stale result rejection; relink keeps recipe, Replace is explicit/undoable;20 independent outputs and duplicate-source fixtures.

## CQ42

**Decision — Accessibility and responsive acceptance:** Validate a finite large-font/screen-reader/keyboard/touch qualification contract for the shared header and all major editing workflows.

**Current understanding:** Current screens have fixed row assumptions and differing history controls; comprehensive Q01 remains open.

**Evidence:** Q05/Q23; S08/Q01. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Defines professional usability and prevents visual-only approval.

**Proposed default for validation:** Proposed48dp targets,320dp/narrow and landscape/split layouts, through200%font, logical TalkBack focus/action descriptions and reachable controls; adjust targets explicitly with evidence if needed.

## CQ43

**Decision — Physical device and media tiers:** Which minimum/middle/high physical resource tiers and representative OEM/codec/provider/media cases should define release support and sustained performance budgets?

**Current understanding:** Only two emulator APIs and scoped owner phone pass are established; exact physical tiers remain unknown.

**Evidence:** Q13/Q23; Q01; R8. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Prevents universal claims and endless qualification expansion.

**Proposed default for validation:** Bounded named tiers and workloads with cold/warm/sustained cases, peak memory/storage/thermal and honest sample distributions; record unsupported variants and retain existing CI gates.

## CQ44

**Decision — Owner acceptance dossier and lock boundaries:** Validate the exact evidence packet and focused owner tasks for each of four formal workspace locks, preserving already passed supplied checks.

**Current understanding:** Owner-reported unmentioned checks pass; no whole-workspace lock has been granted.

**Evidence:** Q23; Q02; release gate. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Defines a finite stopping condition and avoids reasking successful tests or inferring acceptance from CI.

**Proposed default for validation:** Family/child exits plus limitations, exact candidate/output evidence and only new/regression-risk phone tasks; four explicit separate acceptance records.

## CQ45

**Decision — Video timebase and first qualified lane:** Validate the future sequence timebase/source timestamp/rounding and proxy-conform contract, plus the first qualified SDR lane while preserving all later Video scope.

**Current understanding:** Video remains disabled/gated; V01/V02/V04/V07 are required later.

**Evidence:** Q24; R9; WorkspaceModeMenu20–26. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Affects precision edits, VFR, speed, J/L cuts, conform and saved recovery across later tools.

**Proposed default for validation:** Stable clip IDs, explicit source/sequence clocks and frame rounding, one real H.264/AAC SDR output lane first; keep full multitrack/nested/multicam/slideshow work tracked.

## CQ46

**Decision — Professional audio and slideshow acceptance:** Validate future sample/channel conversion, mix/headroom/voice/noise-reduction and photo+audio timing acceptance.

**Current understanding:** V03/V04 include user audio, recording, mixing, buses/stems and MP4 slideshow; GIF stays silent.

**Evidence:** Q24; V03/V04. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Defines real sync and signal quality rather than merely adding audio controls.

**Proposed default for validation:** Explicit sample-rate/channel policy, independent audio measurements and listening fixtures; declared per-photo duration/pan/zoom and optional beat-sync events verified in decoded MP4.

## CQ47

**Decision — Moving redaction and sensitive removal:** Validate the future evidence required to prove removed video/audio content is absent from delivered frames/streams/thumbnails, including tracking uncertainty and cut boundaries.

**Current understanding:** V06 is gated; preview concealment and edit-list trim are insufficient to establish removal.

**Evidence:** Q24; V06; R9 privacy requirement. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Determines export strategy and prevents harmful false security claims.

**Proposed default for validation:** Re-encode affected material; independently inspect every affected frame/stream and boundary, review uncertain tracking intervals and fail visibly if unresolved.

## CQ48

**Decision — Final bilingual onboarding contract:** Validate terminology/glossary, versioned local acknowledgement, decline and returning-user behavior once all-five editing acceptance opens R10.

**Current understanding:** Final EN/ES onboarding is deferred; paid-once offline product has no account/IAP unlock.

**Evidence:** Q24; L01/L02; R10. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Shapes localized UI/help/legal/access flows and same-ID upgrade behavior without changing phase order.

**Proposed default for validation:** One terminology glossary; complete EN/ES strings/help/accessibility; explicit local version/language, understandable decline/offline return and fresh/upgrade tests.

## CQ49

**Decision — Release identity and ownership handoff:** Validate the exact artifact/signing/upgrade/support dossier needed once the finished-app-and-website gate opens Console work.

**Current understanding:** Console absent; keys/account/publication are later gates, and current debug side-by-side builds do not prove production upgrades.

**Evidence:** Q24; L03–L08; R11. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Prevents accidental production ownership/publication mistakes and stale policy assumptions.

**Proposed default for validation:** Owner-controlled protected keys, exact source/AAB/SBOM/assets/split install/migration proof and truthful listing/support/rollback plan; then current actual-account checks and exact owner publication approval.

## CQ50

**Decision — Integrated first batch and finite stop:** Validate Q25's B1 interaction wave, measured GIF B2 and queued R1.1/R1.2 sequence; identify any concrete minimal prerequisite or affected-task blocker before issuing the final resume instruction.

**Current understanding:** All engineering is paused during Stage2; new findings justify specific repairs, not restarting audits or rewriting the app.

**Evidence:** Q25; A1 ledger; F01–F06. See the [pinned intake roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/b9b5ff14ec5a833416b972c4b670a7e9157a2e0a/docs/GZ_ENGINEERING_ROADMAP.md) and corresponding source index in the response.

**Implementation/acceptance impact:** Turns this exchange into one executable bounded batch and prevents another arbitrary question cycle.

**Proposed default for validation:** Approve exact live baseline/one writer per shared file/observable exits and candidate+merge evidence; missing owner GIF workload blocks only that reproduction; finish Stage3 docs then owner relays resume.
