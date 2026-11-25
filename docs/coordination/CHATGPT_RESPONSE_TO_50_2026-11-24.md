> **Historical checkpoint — current route updated November 25, 2026.** Follow the [GPT-6.2 Sol Ultra successor handoff](../NEW_CHAT_HANDOFF_2026-11-25.md), [current ledger](../WORK_LEDGER.md) and [PR #37 / 0.12.11 Test evidence and focused phone script](../B1_WORKSPACE_VERIFICATION.md). Current task: read-only successor onboarding, then wait for Lance's ongoing GPT-6 Pro review. Original text below retains its dated evidence; its “current”, “next”, delivery, engineering and test commands are historical, not fresh instructions. All 25 answers and 50 questions/answers are complete; Stage 3 relay/full reads were consumed before PR #37. Their design contracts remain relevant; their old activation/first-batch commands do not dispatch work. Do not replay completed intake, question exchanges, builds or phone scripts. The [current review brief](GPT_6_PRO_REVIEW_BRIEF_2026-11-25.md) supersedes older review prompts.

# ChatGPT Stage 3 — answers to Codex's 50 questions

**Date:** November 24, 2026 (America/Denver). **Owner:** Lance / Greenezone Software LLC. **Status:** All 50 decisions answered; application engineering activates only when the owner relays the final prompt and Codex reads these answers and the published [engineering-resume instruction](ENGINEERING_RESUME_2026-11-24.md).

## Evidence, authority and completion boundary

Live main at intake exactly matched Stage 2 merge `df0faa23c4908bfd847410c84798a8c3402b27af`, tree `9477d262b9e672ee5abd3ab50116500b22058a84`, [PR #35](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/35). Its difference from Stage 1 merge `b9b5ff14ec5a833416b972c4b670a7e9157a2e0a` is the two Codex documents and WORK_LEDGER only. No newer main work was present at that read; recheck at integration/resume and preserve any authorized later changes.

I read the complete [25-answer response and both frozen roadmap/child-document snapshots](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md) (298,465 characters), all [50 questions](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md) (48,632 characters), the complete Stage 1 handoff and the current instructions, ledger, specification, roadmap, inventory, continuity, release gate and README. Q01–Q25 and CQ01–CQ50 are complete and ordered; the reconciliation retains the same 77 family IDs in the same order. The appendices are historical evidence, not revived next actions or competing roadmaps. Three independent internal review scopes assisted this Stage 3 document review; they do not stand in for these answers or impersonate the owner's external Codex session.

The qualified application remains PR #31 merge `3ad65c533f7667a136d4fa1838afcc06d3f484da`, candidate `aa86c37c64c5e395d5378df281a0c7583a39d0e0`, matching application tree `1c7e3007204192ea240a7fd8d65cbd00cf7f9511`. Recorded candidate/merge JVM/build/signing and 193/193 per API29/API35 are inherited execution evidence, not tests newly run here. The owner's unmentioned supplied phone checks retain owner-reported pass credit. F01–F06, complete feature families, genuine process-loss recovery, physical tiers and formal locks are not declared repaired/completed by these documents.

Every answer separates settled requirements or recorded source facts from our engineering decision, the required evidence, remaining uncertainty and owner-only decisions. Source observations below are attributed to Codex's pinned read-only production inventory and existing receipts, not a new blanket audit. Technical design defaults are decisions for the agreed work, not claims that the owner previously specified them or that they already execute. No required scope, 77-family ID, legacy compatibility, one-time/offline/no-runtime-AI contract or later gate is removed. When a named format/library is implemented later, use its actual primary specification, version and license evidence rather than treating a proposed lane here as certification.

The single [PRODUCT_SPEC](../PRODUCT_SPEC.md) owns the common interaction contract, [GZ_ENGINEERING_ROADMAP](../GZ_ENGINEERING_ROADMAP.md) owns R0–R11 scope, [CAPABILITY_MATRIX](../CAPABILITY_MATRIX.md) owns family status, [RELEASE_GATE](../RELEASE_GATE.md) owns qualification, and [WORK_LEDGER](../WORK_LEDGER.md) owns execution order. This is the requested answer record, not another active backlog. No additional arbitrary question cycle is required.

## CQ01

### Common header and full-image editing

**Question answered.** Common header and command model: Validate the proposed common header order, narrow-screen second row and typed command adapters: which layout rules should be mandatory across the four current workspaces and inherited by Video when its gate opens?

**Settled requirements / recorded facts.** The owner requires the same top-area grammar, Options label and Undo/Redo convention across the four current workspaces. The retained product contract keeps GZ Editor Pro at top left and permits main chrome to hide while active editing guides remain.

**Engineering decision and rationale.** Use one presentation component with typed command/availability adapters; keep native documents, histories, pickers and jobs in existing workspace controllers. Preserve the app title at top left, a distinct workspace selector, paired Undo then Redo arrows, and visible text Options at top right. A genuine Back/child context occupies the same leading/context area everywhere. On narrow widths or large fonts, place the workspace/history group on a deliberate second row; do not horizontally scroll major commands or let Options disappear. Use shared spacing, colors, icon assets, disabled states and touch sizes. Reject Q05's proposed permanently pinned header: a free-canvas hide action hides header and tool decks together, while active handles/guides and tool operation survive. A corresponding tap restores them. Empty workspaces keep the header visible. Provide an accessible reveal action without adding duplicate file commands. Future Video inherits this component only after its existing gate.

**Qualification / next evidence.** Exercise the same header contract in Regular, Pro, parent Collage, selected-tile context and both GIF routes; check narrow/landscape/large-font layouts, actual callback ownership and hidden-chrome editing. Pure view changes leave content/history unchanged.

**Remaining uncertainty.** Exact Compose extraction and wrapping breakpoint are implementation details to settle against the existing layout during B1, using the stated reachable-control exits.

**Owner-only decision.** No new owner decision blocks B1. This is the engineering layout decision implementing the owner's consistency request, not a claim of prior approval of an exact mockup.

**References.** [Codex CQ01](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq01); [Q04](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q04), [Q05](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q05), [Q06](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q06). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ02

### Empty states and one command location

**Question answered.** Empty-state discoverability: Validate how an empty workspace teaches media/project opening while keeping exactly one major-command location; should body guidance point to Options without duplicating action buttons?

**Settled requirements / recorded facts.** Codex records duplicate empty-state import controls and unavailable project opening in some empty states. The owner expressly prohibits duplicate major commands.

**Engineering decision and rationale.** Keep Options and the workspace selector available on every empty workspace. Put a short noninteractive instruction in the body, such as 'Use Options to open a photo or project'; retain the company footer. Remove duplicate clickable Open/Add/Projects cards once their Options action exists. A single contextual Add layer control in Pro is legitimate editing, distinct from opening a new document; do not render a second Add first layer button that invokes the same action. Show unavailable Save/Export with an accessible reason when no supported document exists. Preserve existing reachable functions; do not add inert entries for future photo-sequence, Video or other unimplemented capabilities. On a loaded document, keep direct editing, playback and child Done/Cancel; the prohibition applies to duplicate major file/help actions, not their distinct tool operations.

**Qualification / next evidence.** From each genuinely empty state, open a saved project and supported media through Options; enumerate accessible commands to prove each major action has one location. Check disabled-state explanations without relying on color.

**Remaining uncertainty.** The exact guidance sentence can fit the workspace and available source routes; it must not imply that an absent feature is implemented.

**Owner-only decision.** None required for this consolidation.

**References.** [Codex CQ02](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq02); [Q04](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q04), [Q06](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q06), [Q07](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q07). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ03

### Project identity, Save and Save as

**Question answered.** Save identity and naming: Validate stable project identity with Save updating the current project and Save as choosing a new identity; what collision and clean-document rules should be consistent across workspaces?

**Settled requirements / recorded facts.** The existing stores have differing name/target handling. The owner requires reliable saving and guards, while protected named-save replacement behavior must survive.

**Engineering decision and rationale.** Introduce a stable project identity independent of its display name and track the last successfully saved semantic revision. Save targets that identity; an unnamed Save asks for a name and establishes it only after successful atomic publication. Save as creates a distinct identity and makes that saved copy the active target; renaming is a separate operation, not an accidental copy. A name collision with another project never silently overwrites: offer choose another name or explicit Replace naming that target; replacing keeps the chosen target's identity. Preserve existing confirmation protection for direct replacement. In a dirty-departure flow, Save already confirms writing the displayed current target, so do not stack a second equivalent warning; a different-name collision still needs its own resolution. Clean named Save may be disabled as 'Already saved'; Save as remains available. Cancellation, refused size, failed write or stale completion never changes the active target or declares newer edits clean. Use a minimal compatible identity/baseline adapter in B1; the full per-file/index migration stays R1.1.

**Qualification / next evidence.** Save/open/Undo/Redo across named identities, Save as, collision Replace/Cancel, refused dense saves, interrupted publication and edits during Save preserve the intended target and healthy bytes. A named save does not equal Export.

**Remaining uncertainty.** Existing name normalization and legacy-ID mapping need a documented compatibility rule when the adapter is wired; avoid changing accepted names or allocating IDs by mutable list position.

**Owner-only decision.** None to begin. Any reduction of existing save capacity or compatibility would require an explicit scope decision and is not authorized here.

**References.** [Codex CQ03](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq03); [Q06](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q06), [Q09](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q09), [Q14](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q14). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ04

### Selected-tile command ownership

**Question answered.** Selected-tile Options ownership: Should selected-tile Options expose explicitly labeled parent Save/Export after child resolution, and should standalone photo Save/Open remain available as separately named operations?

**Settled requirements / recorded facts.** The selected tile uses Regular editing as a child transaction. Existing independent photo-project Save/Open does not Apply to the Collage parent.

**Engineering decision and rationale.** Preserve these functions with explicit Options labels 'Save photo project', 'Save photo project as…' and 'Open photo project…'. Explain once in child context that these act on a standalone photo project; Done applies to the selected Collage tile. Keep Collage Save/Export in the parent Options menu for B1, after leaving the child. Do not add parent Save/Export inside the child as a second document scope, and do not add an unqualified new tile-image export path merely for menu symmetry. Child Done and Cancel remain direct. Maintain two independent baselines: child-entry state for Apply/Discard and standalone named-photo saved state for Save. Saving a photo project leaves child changes pending relative to the parent; Cancel restores the entry state but does not delete the explicitly saved photo project. Loading/replacing photo content edits only the child draft, with protection before losing pending child changes. Commit Done by stable parent and tile identity, at most once; a standalone photo save never silently modifies parent or sibling tiles. Dirty child Back offers Apply to tile / Discard tile changes / Cancel (keep editing); Apply or Discard returns only to the parent, and that one Back must not also leave the parent. A workspace-switch or parent-exit request made from the child stages its Apply/Discard disposition, then protects the resulting parent candidate with Save/Discard/Cancel. A later Cancel, save failure or target-load failure preserves the original child draft and parent transaction state; do not commit a provisional discard or lose an unapplied child. An explicitly completed named save remains recorded accurately. Direct Done is an explicit completed parent edit, while direct Cancel restores only child entry; these differ from a still-pending multi-context departure.

**Qualification / next evidence.** Change tile A, save a standalone photo project, Cancel, and confirm parent A/B are unchanged while the saved photo reopens correctly. Repeat with Done, photo-project Open, failed save and duplicate source URIs; parent changes once to the intended tile.

**Remaining uncertainty.** B1 must map the existing child Open behavior precisely and preserve its source-replacement semantics under these labels; any unsupported command remains absent with the full future scope tracked.

**Owner-only decision.** None required; this preserves useful existing behavior while resolving ownership. Adding another parent command surface later needs a concrete usability reason.

**References.** [Codex CQ04](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq04); [Q04](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q04), [Q06](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q06), [Q09](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q09). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ05

### One-tap Collage grammar

**Question answered.** Collage single-tap grammar: Validate the exact single-tap selection/chrome rule for a hit tile, an already selected tile and margins, including when chrome is hidden.

**Settled requirements / recorded facts.** Codex identified parent Collage onDoubleTap as the chrome toggle and onTap as selection; child recognition is separate. The owner requires one deliberate tap in both directions.

**Engineering decision and rationale.** For a completed unconsumed free-canvas tap, select the hit tile if any and toggle chrome exactly once. This applies to selected and unselected tiles, with chrome visible or hidden. A margin/background tap toggles only and keeps the selection. This combined action makes the first tap satisfy the owner request instead of requiring a hidden first-select/second-toggle sequence. Tool handles, paint/erase marks, crop drags, text/shape moves and pinch gestures own their pointer streams and do not also toggle. Fix hit ownership at pointer-down; movement beyond tap slop, cancellation or a second pointer disqualifies a free tap. Preserve established intentional zoom gestures with deterministic arbitration. A rapid double tap is never necessary to hide/show; two completed free taps may restore the starting visibility. Selection/chrome changes create no content history, and guides remain usable when chrome hides.

**Qualification / next evidence.** Actual parent and child pointer cases cover slow taps in both directions, hit/miss/selected/unselected targets, gaps, drag/pinch/cancel and active editing. Assert chrome, selected stable ID, unchanged content/history for free taps and untouched siblings.

**Remaining uncertainty.** The parent cause is source-established; adjust the child only after the named reproduction reveals an actual conflict. Do not assume the parent recognizer exists underneath it.

**Owner-only decision.** No new permission needed. Combined select-and-toggle is the chosen engineering interpretation of the required single tap.

**References.** [Codex CQ05](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq05); [Q08](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q08), [Q10](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q10). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ06

### Cancellation is rollback

**Question answered.** Uniform gesture cancellation: Validate whether interrupted text/shape/privacy drags should roll back exactly like Draw/crop, and define cancellation versus successful release for all tool transactions.

**Settled requirements / recorded facts.** Draw/crop cancellation rolls back, while Codex identified text/shape and privacy drag-cancel paths that currently finish. That distinction is not itself proof of the phone tap cause.

**Engineering decision and rationale.** Use a transaction boundary for every continuous edit. A successful release/explicit Apply publishes one completed content revision and one history checkpoint. Pointer cancellation, interrupted multi-touch, owner/target replacement, tool change during an unfinished gesture, or lifecycle disposal cancels that provisional transaction and restores its full pre-gesture document/history/selection ownership. Text, shape and privacy drags must follow this rule rather than committing on cancel. Do not make Back an Undo; Back cancels an active draft first, then stops for that invocation. A normal completed gesture remains committed when the user later switches tools. View-only fit/zoom/chrome/selection does not dirty the document. An external Save/Export/departure cannot serialize half a gesture: resolve it first under the same rules.

**Qualification / next evidence.** Inject cancellation at beginning/middle/end for each changed gesture path, including second-pointer and recomposition boundaries. Compare complete document/history and independently rendered pixels, then prove one checkpoint on successful completion.

**Remaining uncertainty.** Each existing recognizer must expose its cancellation path; implementation may adapt the mechanism, not the rollback outcome.

**Owner-only decision.** None. This targeted consistency correction is within safe editing, not authorization to redesign working tools.

**References.** [Codex CQ06](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq06); [Q08](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q08), [Q10](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q10). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ07

### Dirty baselines and scoped Discard

**Question answered.** Clean baseline and Discard: Validate a single clean-baseline definition and the state retained after Discard for unnamed imported work, named work and child edits.

**Settled requirements / recorded facts.** A filename, export, nonempty history or retained session does not establish that the current editable content is saved. Pro Delete current work has a different reset contract.

**Engineering decision and rationale.** Dirty compares semantic editable content and source ownership with the document's last successful saved baseline, or its documented import/creation entry baseline when unnamed. Undo back to identical baseline content becomes clean even if history exists; view/selection alone does not make it dirty. Named Discard restores the last saved revision; unnamed Discard restores entry state; child Discard restores only that child's entry state. Retain valid baseline history where supported, but remove the discarded unsaved branch and its redo/recovery records so returning or recovery cannot resurrect it. Never implement departure Discard by calling Pro Delete current work or deleting a named project. For Open/replace, record the chosen disposition provisionally and prepare/validate the new target before replacing the old session. Picker cancellation or load failure preserves the original draft and route; an explicit completed named save remains saved. Apply a pending Discard only when the actual transition succeeds. New/close has no external target but still commits its guarded transition once.

**Qualification / next evidence.** Named and unnamed dirty/clean/Undo-to-baseline routes, failed picker/load, returning to discarded sessions and later recovery preserve original media, healthy named files, unrelated workspaces and sibling tiles.

**Remaining uncertainty.** The minimum B1 adapter must distinguish saved/entry/reset/child baselines explicitly. General durable recovery publication remains R1.2; existing recovery records still cannot restore discarded work.

**Owner-only decision.** None required. Explicit Discard is the user's decision about that working delta; it never means delete source or saved projects.

**References.** [Codex CQ07](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq07); [Q09](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q09), [Q10](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q10). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ08

### Ordinary workspace switches use the same guard

**Question answered.** Workspace switching and retained drafts: For an ordinary dirty workspace switch, validate the exact Save/Discard/Cancel behavior while preserving retained-session usefulness; should retaining unsaved work be an additional explicit choice or should the three choices fully define it?

**Settled requirements / recorded facts.** The owner expects exit protection in Regular, Pro and Collage. Retained in-memory sessions do not satisfy that expectation by themselves.

**Engineering decision and rationale.** Use Save / Discard / Cancel for every ordinary dirty departure: workspace switch, genuine app/system Back leaving the document, New/Open/replacement and another named project. Save retains the successfully saved working session and valid history; Discard retains only the resolved baseline; Cancel leaves the exact current route/work. Clean transitions happen once without an unnecessary dirty prompt. Do not add a fourth 'Keep draft and switch' choice in this wave, or silently reinterpret Discard as keep unsaved. Help, a menu, an inspector and nondeparting tool changes do not trigger a document-departure prompt. The navigation coordinator handles the frontmost transient surface first; one Back cannot dismiss it and also leave the workspace. Apply the same contract to both GIF routes, while accurately labeling their exit work as source-supported consistency coverage, not a newly reported owner GIF-exit failure. The special Regular→Pro continuation is composed explicitly in CQ09.

**Qualification / next evidence.** Cover all departure triggers, both Back edges/canceled predictive gestures, rapid repeated Back, clean work and nested transient surfaces. Check exact one-pop behavior and return-state equality after each disposition.

**Remaining uncertainty.** Existing route stacks differ; adapters may differ internally while exposing these same outcomes.

**Owner-only decision.** None required for this guard. Four formal workspace acceptance locks remain separate.

**References.** [Codex CQ08](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq08); [Q09](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q09). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ09

### One transactional Regular-to-Pro handoff

**Question answered.** Regular-to-Pro handoff composition: Validate one coherent flow integrating save protection, edited-appearance Yes/No and resume-existing-Pro without contradictory stacked dialogs.

**Settled requirements / recorded facts.** The retained owner contract is Yes = bring the current edited Regular appearance into Pro, No = open blank Pro, dismissal = remain in Regular. Native Regular work stays independent. The newer explicit Discard request must also be honored.

**Engineering decision and rationale.** Use one serialized transition coordinator, never overlapping modals. First capture Regular document/revision and gather its ordinary dirty Save/Discard/Cancel disposition without applying a destructive reset. A completed Save may persist immediately; Discard only selects the baseline that will be retained if the transition succeeds. If a Pro session exists, offer a clearly separate 'Resume existing Pro' or 'Start another Pro' choice with Cancel. Resume keeps that Pro document and bypasses image transfer; it must not be presented as No. Starting another Pro retains the established Yes/No appearance prompt, using the resolved Regular revision. If Regular changes were explicitly discarded, label the image as its saved/entry version and invalidate the discarded snapshot; never transfer discarded edits secretly. Before replacing dirty Pro, gather its own Save/Discard/Cancel disposition. Prepare the chosen blank/imported Pro state off the live documents; then commit one transition, retaining each resolved native parent. Any cancel, target mismatch or transfer failure leaves both original in-memory documents/routes intact, except a named save the user explicitly completed. Save/No alone never discards Regular. An explicit Discard may remove only its identified working delta, which is the new owner safety choice. Revalidate the token before commit if either document changes while preparation runs.

**Qualification / next evidence.** Cover Yes/No/dismissal with clean/dirty Regular, existing clean/dirty Pro, Resume, Discard then Yes, failed render/load, save refusal and repeated taps. Assert native parents, chosen rendered appearance, history, source bytes and one route transition.

**Remaining uncertainty.** Exact dialog phrasing can be shortened for the phone, but the destination and which revision will transfer must stay explicit. No new architecture or full saved round-trip is a B1 prerequisite.

**Owner-only decision.** No new product decision required. These are engineering rules reconciling the existing transfer promise with the owner's explicit dirty-discard behavior.

**References.** [Codex CQ09](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq09); [Q09](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q09), [Q16](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q16). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ10

### Empty Pro setup is protected and honestly unsaveable

**Question answered.** Empty Pro canvas durability: Should changed canvas/background settings before the first Pro layer constitute a saveable dirty document or nonpersistent setup?

**Settled requirements / recorded facts.** Codex records that current Pro validity requires 1–12 layers; Save cannot persist a zero-layer canvas through the existing format.

**Engineering decision and rationale.** For B1, classify zero-layer canvas/background changes as explicit pre-document setup, not a fake successfully saved document. Keep changed setup in the session on Cancel and protect departure with 'Discard setup' / 'Cancel'; Save remains unavailable with 'Add a layer before saving this setup' or equivalent truthful reason. Do not create a hidden dummy layer, silently drop setup, or offer an impossible mandatory Save. Once a real supported layer is added, ordinary dirty and Save/Discard/Cancel rules apply. Deliberate named empty-document persistence remains a required R2 canvas/document extension: define a versioned zero-layer representation, size/background/alpha render behavior, reopen and export before enabling it. This temporary implementation boundary is recorded, not a claim that professional empty documents are out of scope.

**Qualification / next evidence.** Change empty canvas dimensions/background, attempt Back/switch/New, cancel and verify setup remains; explicitly Discard and verify reset. Add a layer and check ordinary Save/exit. R2 separately proves persisted zero-layer documents.

**Remaining uncertainty.** Persisting empty projects now would expand B1's model/codec boundary; it is not needed to remove the unsafe exit. Current setup lifetime through process loss remains honestly limited until the durable representation is implemented.

**Owner-only decision.** None required to protect the existing limitation. Removing empty-document durability from required future canvas scope is not authorized.

**References.** [Codex CQ10](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq10); [Q09](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q09), [Q17](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q17). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ11

### Revision-bound Save and one pending departure

**Question answered.** Async save and pending departure: Validate handling when a save is in flight and the document changes, target route changes or repeated Back/Save arrives.

**Settled requirements / recorded facts.** Current stores return save outcomes, but Codex found no common revision-bound departure token. Existing stale-job protections are useful foundations.

**Engineering decision and rationale.** Capture workspace/document ID, source revision, child tile ID when applicable, content revision, intended destination and unique operation token. Deduplicate repeated Save/Back/Done for that token. Only a successful atomic save of the captured revision can authorize one still-valid continuation. If the user has made newer edits, the saved revision may become the persisted baseline, but the newer active content stays dirty; stop and resolve it instead of departing. A canceled naming/save decision invalidates its continuation, so an unrelated later Save cannot execute a stale exit. A failed Save keeps work and offers explicit Retry/Cancel; it never advances the route. A changed destination requires a fresh validated intent. Late callbacks may clean their own resources but cannot mutate a newly opened document. Child and source ownership checks are mandatory, not just an integer revision comparison.

**Qualification / next evidence.** Use controlled slow/failing saves, edits during Save, target replacement, duplicate callbacks and rapid taps. Assert one publication/route execution where appropriate, exact saved/current revisions and intact unrelated documents.

**Remaining uncertainty.** The coordinator may block conflicting edits during a short transition or detect supersession; it must satisfy the same externally observable contract and avoid an unresponsive main thread.

**Owner-only decision.** None required.

**References.** [Codex CQ11](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq11); [Q10](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q10). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ12

### R1.1 bounded project listing and migration

**Question answered.** Bounded project-list migration: Validate per-project atomic storage plus lightweight index for Regular, explicit corrupt-entry presentation and the migration/list-memory evidence required for R1.1.

**Settled requirements / recorded facts.** Codex records eager Regular SharedPreferences.all decoding and already-lightweight Pro/Collage filename listing. Raw preference retention itself can consume memory.

**Engineering decision and rationale.** Use per-project atomic records plus a lightweight metadata/index layer for Regular, with stable IDs. List names/status/metadata without building recipe graphs; decode the selected recipe on IO and reject canceled/stale selections. Migrate by bounded copy, decode/validate, atomic publish and verified index update. Keep healthy original bytes until the corresponding new record has been verified and rollback/recovery is established; do not read all near-budget recipes together or delete the only healthy copy. Expose corrupt, unsupported and unavailable entries with useful status rather than silently dropping them. Preserve ordering, save-count limits, name behavior and previous formats. For Pro/Collage/GIF, fix only demonstrated selected-load/main-thread/identity boundaries; their correct list implementations need no wholesale rewrite. This remains the first durability slice after the owner-correction wave.

**Qualification / next evidence.** Use twelve near-budget saves plus corrupt/truncated/unsupported and failed-write cases. Record decoded-object counts, peak heap including raw storage, list/open latency, cold reopen, chosen-ID integrity under sort/delete and byte equality of unaffected healthy saves.

**Remaining uncertainty.** Exact index schema, migration cleanup timing and absolute memory/latency budgets must be pinned against the measured existing stores before the R1.1 change. A small metadata result alone is not proof of bounded heap.

**Owner-only decision.** No further permission needed. Existing capacity or compatibility cannot be reduced silently to pass the experiment.

**References.** [Codex CQ12](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq12); [Q14](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q14). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ13

### Durable history and honest acknowledgement

**Question answered.** Durable history and acknowledgement: What bounded deduplicated history/checkpoint policy and acknowledged durability boundary should R1.2 promise across workspaces?

**Settled requirements / recorded facts.** Regular and Collage can retain many snapshots of dense recipes; current in-memory retention does not establish durable completed-edit recovery.

**Engineering decision and rationale.** Use a serialized per-document writer, immutable revision IDs, deduplicated document/history records and atomic checkpoint generations with a bounded journal. Media bytes live outside history and saved-instance Bundle. Queue every completed transaction immediately; never persist sampled halves of a pointer gesture. Preserve existing promised logical history depth using structural sharing or disk-backed records, rather than silently cutting Undo depth to fit memory. Bound loaded history graphs, journal bytes, checkpoint retention and aggregate temporary storage; record those implementation budgets with worst-case independent revisions, not only highly compressible edits. A revision is acknowledged durable only after successful complete publication and integrity validation. Show pending/failed persistence honestly and retain the last healthy generation; an older completion must not acknowledge a newer edit. Coalesce writes only when the replacement includes every required retained history state. Backgrounding requests a flush; a crash before acknowledgement may restore the previous complete revision, never a mixed document. Low storage must report recoverable pending work rather than declare success.

**Qualification / next evidence.** Kill before/after acknowledgement and during checkpoint rotation; verify old-or-new whole state, logical Undo/Redo, bounded heap/storage and healthy-source/named-save preservation. Test nondeduplicating worst cases and refusal/retry.

**Remaining uncertainty.** The precise byte budgets and publication mechanism are R1.2 implementation choices backed by measurements. No zero-loss promise is made for an unacknowledged write.

**Owner-only decision.** None needed for this recovery design. Any proposed reduction of promised history/capacity must be brought as an explicit affected-scope decision.

**References.** [Codex CQ13](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq13); [Q15](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q15). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ14

### Actual process-loss restoration

**Question answered.** Process-loss recovery contract: Validate exactly which document, child, tool/view and navigation states must return after a confirmed new-process restart, and which unfinished interactions intentionally cancel.

**Settled requirements / recorded facts.** Activity recreation and retained sessions do not prove recovery in a new process. The export journal has separate job ownership.

**Engineering decision and rationale.** Restore the latest acknowledged committed document/source revision, source-grant status, named/clean baseline, required bounded Undo/Redo, selected stable object/tile, workspace/route and meaningful tool/viewport/playhead. Validate restored targets; if a tool target no longer exists, recover a safe selection and explain it without changing pixels. Persist a completed child working draft separately with parent/tile identity and its entry baseline; never auto-Apply it on restart. Unfinished strokes, drags, lasso sampling and interrupted Apply transactions cancel to their healthy transaction boundary. Do not auto-confirm a dirty departure after restart. Missing/revoked/changed media restores a recoverable recipe with relink status, not an empty success. Recover picker/export tokens independently so a late result cannot bind to another project. Prove Regular first, then Pro/Collage/GIF with their own route/history tests.

**Qualification / next evidence.** Use an external controller and confirmed new PID, with no retained old objects: after acknowledgement, mid-publication, mid-gesture, child open, external picker open, grant loss and export-copy interruption. Check exact documents, history and navigation as well as rendered output.

**Remaining uncertainty.** OS/provider-specific redelivery varies and needs the declared device tests; the app must handle absent/duplicate results safely. Temporary B1 session protection does not close this R1.2 obligation.

**Owner-only decision.** No new decision blocks implementation. Specific physical-device observations may remain scoped evidence gates.

**References.** [Codex CQ14](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq14); [Q15](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q15), [Q16](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q16). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ15

### Provider uncertainty and copy ownership

**Question answered.** Provider uncertainty and long-copy behavior: Validate user-visible cancel/retry/navigation behavior after a provider's truncating output stream opens and when close success cannot be independently confirmed.

**Settled requirements / recorded facts.** Existing export stages precede truncating writes and preserve uncertain-destination recovery. URI equality alone does not prove a destination is distinct from every source alias.

**Engineering decision and rationale.** Before opening a destination, cancellation stops safely and retains the appropriate draft/stage. Once truncating provider copy begins, keep an owned job and finish its byte-bounded copy/close where possible; do not advertise instant cancellation that leaves an untracked partial file. Bound bytes, buffers, jobs and storage, but do not claim arbitrary provider wall time is bounded. Use provider cancellation/timeouts where supported; timeout, process loss, failed close or unverifiable completion becomes an explicit uncertain result with stage retained. A recoverable new-destination retry reuses validated staged bytes and must not rerender or blindly overwrite/delete the ambiguous remote document. Do not report success merely because local encoding completed. Prevent writes to known originals/aliases and use newly created destination ownership; when safe ownership cannot be established, fail recoverably instead of relying on URI-string inequality. Navigation must not orphan the worker, stage or pending recipe. Do not spawn unbounded retries while a blocked provider operation still owns resources.

**Qualification / next evidence.** Use local, write-only, failing/partial/slow and aliasing provider cases; separate open/write/close/readable-result evidence. Verify original hashes, stage survival, one operation owner and correct retry status.

**Remaining uncertainty.** Some providers cannot offer readback or interrupt a blocked call promptly. Report that limitation accurately and preserve recovery; no universal remote rollback guarantee is approved.

**Owner-only decision.** None required for safe handling. Selecting a fresh destination after uncertainty is an explicit user operation.

**References.** [Codex CQ15](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq15); [Q11](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q11), [Q16](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q16). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ16

### Portable native projects

**Question answered.** Portable project custody: Validate embedded versus linked-only native bundle options, missing-source handling and asset/font portability guarantees needed to complete S07.

**Settled requirements / recorded facts.** Current reference/recipe saves are not inherently portable source bundles. Required S07 scope includes second-device import and relink.

**Engineering decision and rationale.** Provide explicit embedded and linked-only native bundle modes. Use a versioned manifest of native document/source revisions, asset/font dependencies, checksums, logical paths, schema/render versions and declared size/count limits. Embed permitted source/asset bytes for self-contained offline use; linked-only packages declare dependencies and never promise identical second-device appearance without them. Preserve source rights/custody and licensed-font restrictions. Import into a new transaction with safe extraction, allocation bounds, checksum validation and collision-free identity mapping; do not overwrite current work or execute packaged content. Preserve missing dependencies as named placeholders with recipes and targeted relink. Never silently substitute artwork/font/media or download a replacement. Include recovery/native lineage and the supported history policy explicitly, rather than assuming copying JSON carries them.

**Qualification / next evidence.** Offline second-device import of a permitted embedded bundle matches editable content and independently decoded output; linked-only/missing/revoked/corrupt/oversized/collision cases recover without source or current-work loss.

**Remaining uncertainty.** Exact container format and asset embedding rights depend on the chosen implementation and each dependency; review them when S07 is executed. The portable capability remains required.

**Owner-only decision.** No new owner permission for engineering. Any proposal to omit required portability or ship a restricted asset needs a concrete scope/rights disposition.

**References.** [Codex CQ16](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq16); [Q16](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q16), [Q21](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q21). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ17

### Repeated native Regular–Pro revision exchange

**Question answered.** Repeated native revision exchange: Validate the user model for saved Regular→Pro→Regular→Pro versions with preserved native parents, alpha/profile/precision and appearance.

**Settled requirements / recorded facts.** The live Yes/No transfer is credited; it does not establish complete saved Regular→Pro→Regular→Pro revision exchange.

**Engineering decision and rationale.** Keep immutable native parents and explicit version branches. Each transferred rendered derivative identifies its parent document/source revision and schema/render/color metadata. Returning edits creates a new version linked to its source parent, rather than rewriting either master or turning Pro layers into falsely editable Regular controls. Preserve alpha, profile and precision wherever the receiving path supports them. If an essential property is unsupported, offer a clearly described compatible-copy choice or block that transfer recoverably; never silently flatten to JPEG/8-bit and call it a lossless native handoff. Label native and rendered derivatives intelligibly in project history. Retain original source grants/relink information and both parent projects. Ordinary explicit Discard affects only the identified working delta, consistent with CQ09; Save/No/Export alone never destroys the other native parent. No Regular Masks or Pro Text authoring is introduced by interchange.

**Qualification / next evidence.** A saved four-leg round trip with cold reopen after each leg proves source lineage, distinct native parent editability, alpha/profile/precision metadata, appearance and independently decoded final pixels; unsupported cases retain both healthy parents.

**Remaining uncertainty.** Full high-precision interchange depends on R5 capabilities. Record supported and unsupported properties at each stage without closing S06 from the current live handoff.

**Owner-only decision.** None for the native version model. A permanent reduction of the required interchange fidelity would require explicit owner scope approval.

**References.** [Codex CQ17](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq17); [Q09](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q09), [Q16](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q16). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ18

### Close GIF performance against the actual workload

**Question answered.** GIF workload and performance acceptance: Validate the minimum workload/device evidence and separate thresholds needed to close the120–300-second phone finding without treating the small emulator fixture as universal.

**Settled requirements / recorded facts.** The owner reports 120–300 seconds. Exact phone/source/settings/provider are unknown. The inherited 180×320, 88-frame, 2.950-second fixture passed at 3.872s/API29 and 3.334s/API35; those numbers do not resolve the phone finding.

**Engineering decision and rationale.** Minimum record: phone model/Android, exact source or a privately retained reproducible equivalent, recipe/trim/reverse/output dimensions/FPS/schedule, destination provider, cold/warm/heat state, and separate tap-to-picker, human picker dwell, confirm-to-readable-file intervals. Inspect supplied media metadata ourselves. Instrument disjoint elapsed intervals for queue/mutex/decode/seek/fallback/render/palette/LZW/stage write-flush-close-rename/provider open-write-close/UI completion; existing overlapping counters cannot simply be summed. Preserve the 6-second sampled-motion and 15-second Save gates and rotated-fixture assertions. After one baseline, before judging a repair, record a workload-specific absolute completion budget, meaningful relative improvement target, memory/storage and responsiveness/cancellation exits. This delegated measurement decision is authorized, not another product-question gate. Use a bounded matched cold/warm sample set, report every sample and median/range honestly, and keep fidelity fixed. As an engineering UI target for the correction, acknowledge the job/cancel request within 250ms on the declared test device; actual worker-stop latency is separately measured by stage.

**Qualification / next evidence.** Close F03 only with actual/equivalent phone workload, independently decoded dimensions/frames/delays/edits, measured stage improvement and focused owner confirmation. Representative gains can be credited separately while F03 stays open.

**Remaining uncertainty.** No defensible universal phone-seconds promise exists yet. Missing owner media holds its reproduction/closure only; instrumentation, B1 and independent durability work can proceed.

**Owner-only decision.** Owner supplies or observes the specific phone case when available and confirms that result. No new authorization is needed to measure or improve it.

**References.** [Codex CQ18](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq18); [Q11](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q11), [Q12](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q12), [Q13](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q13). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ19

### Truthful foreground GIF jobs first

**Question answered.** GIF long-job interaction: Validate whether the first correction should keep navigation blocked during private encoding, with explicit preparation/picker/saving states, or require recoverable background export now.

**Settled requirements / recorded facts.** The current path shares decoder ownership and retains completed staging; general durable background export is not implemented.

**Engineering decision and rationale.** Keep a guarded foreground export in B1/B2 with explicit Preparing, Choosing destination, Saving, Completed/Failed/Uncertain states. Keep the UI responsive and block only mutations/navigation that would invalidate its immutable recipe or orphan resources. During preparation, Cancel requests cooperative termination and becomes Canceled only after the owned work/resources stop. Picker cancellation preserves the defined recovery state. During truncating provider copy use CQ15's protected ownership and uncertainty handling; do not fake completion or cancellation. Show actual stage/progress, without including human picker time in encoder speed claims. Allow only one bounded decoder/job owner unless measured evidence justifies a bounded pipeline. Durable background continuation/navigation remains a later R1/R7 job-custody obligation; it is not a prerequisite for these corrections and must not be silently dropped.

**Qualification / next evidence.** Actual slow preparation, cancel, picker return/cancel, duplicate Export, background/recreation and provider failure cases retain source/draft/stage ownership and truthful visible status. Responsiveness is distinct from completion speed.

**Remaining uncertainty.** The exact cooperative cancellation granularity depends on the measured decoder/provider stages. Do not promise an instantaneous stop for an uninterruptible platform call.

**Owner-only decision.** None to use this bounded first implementation; future background behavior remains tracked.

**References.** [Codex CQ19](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq19); [Q11](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q11), [Q12](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q12), [Q16](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q16). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ20

### GIF frame identity, holds and ping-pong

**Question answered.** GIF frame/segment timeline semantics: Validate stable frame/segment identity, split/remove/duplicate/reorder/per-frame holds and ping-pong endpoint/tick-rounding behavior with concrete short examples.

**Settled requirements / recorded facts.** Current video and photo routes do not provide the full required frame/segment assembly. GIF is silent.

**Engineering decision and rationale.** Use stable segment/frame-instance IDs, immutable source timestamps and rational timeline durations. Split creates adjacent half-open source intervals without duplicate/lost boundary content; Duplicate creates a new instance ID sharing source lineage; reorder changes sequence position only. Keep explicit per-frame/segment holds and loop count in the native recipe. Default ping-pong visits each endpoint once per cycle: A,B,C becomes A,B,C,B; two frames become A,B; one stays A. Each visit retains its own frame hold: holds 120ms,80ms,200ms yield a 480ms A,B,C,B cycle. Reverse is schedule reversal, not negative decoder time or changed source identity. Encode using cumulative boundary rounding into GIF centiseconds to avoid accumulating independent rounding error. Preserve existing representable input delays; if a requested schedule needs unrepresentable/zero ticks or player-incompatible timing, show the resolved schedule and obtain an explicit output choice rather than silently dropping frames or changing FPS. Preserve rational intent in the saved project. This is R7 feature work and is excluded from F03's fidelity-preserving speed patch.

**Qualification / next evidence.** Three-frame and held-frame examples, one/two-frame boundaries, split/join, duplicate/reorder, finite/infinite loops and noninteger frame rates must agree across edited preview, saved reopen and independent output decoding.

**Remaining uncertainty.** Declare the tested minimum-delay/player compatibility policy when implementing the encoder controls; do not claim every external player honors every legal delay.

**Owner-only decision.** None for this deterministic default. A user can explicitly choose different holds/loops; required timeline scope is retained.

**References.** [Codex CQ20](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq20); [Q22](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q22). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ21

### Palette choices and an actual encoded preview

**Question answered.** GIF palette and encoded preview: Which initial palette/dither controls and encoded-preview presentation should qualify professional GIF appearance without confusing normal edited preview with actual indexed output?

**Settled requirements / recorded facts.** The current adaptive palette/encoder is credited within its qualified fixtures; selectable palette/dither and output-appearance preview remain required.

**Engineering decision and rationale.** Keep existing palette/color/encoder semantics unchanged during F03 repair. In R7, initially implement explicit adaptive local versus shared/global palette, declared palette size, and None versus a named deterministic dither, with versioned settings and the existing output behavior retained for legacy recipes. These are first implementation slices, not deletion of the larger palette scope. Distinguish normal edited preview from 'Encoded GIF preview'. The latter must decode the actual staged GIF bytes and reflect indexed color, alpha/disposal, delays, holds and loops; a pre-encode RGB render cannot stand in for it. Changing a palette/dither setting is an explicit user edit/export choice with Undo and saved recipe behavior, not a hidden performance optimization. Temporal stability and size are reported alongside appearance; no setting is described as lossless restoration of unavailable colors.

**Qualification / next evidence.** Independently decode gradients, photographic/noisy scenes, moving transparent objects, repeated colors, disposal/finite-loop and held frames. Compare actual staged playback and output; prove deterministic settings and old-recipe compatibility.

**Remaining uncertainty.** Exact palette algorithm and dither kernel require a bounded quality/resource comparison during R7. Their measured behavior determines the declared choices.

**Owner-only decision.** No new decision blocks engineering. A reduced required palette feature set cannot be represented as full completion.

**References.** [Codex CQ21](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq21); [Q12](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q12), [Q22](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q22). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ22

### Reversible legacy v4 framing

**Question answered.** Legacy GIF framing upgrade: Validate reversible Create upgraded copy, source-backed before/after framing and preservation of explicit manual aspect overrides for legacy v4 projects.

**Settled requirements / recorded facts.** Legacy v4 anamorphic framing is a recorded limitation. Some missing geometry cannot be reconstructed reliably from recipe fields alone.

**Engineering decision and rationale.** Offer 'Create upgraded copy', never silent in-place migration of framing. Reinspect the linked source's actual display geometry, rotation and sample-aspect metadata and distinguish metadata-derived defaults from explicit user aspect/crop overrides. Show before/after from the same source and timing; preserve a deliberate manual override unless the user explicitly changes it. Write a new identity/version with lineage to the untouched original recipe/named save, then adopt it only on confirmation. If the source is missing, require targeted relink before offering a trustworthy geometry comparison. If override provenance is ambiguous, preserve the old rendering and ask the user to choose the shown framing; do not infer intent. Cancel leaves the original active.

**Qualification / next evidence.** Reopen original and upgraded copies, compare pixels/aspect/timing, switch back, and verify source hashes, manual overrides, missing-source and failed-write recovery. G05 stays LIMITATION until this complete route is qualified.

**Remaining uncertainty.** Each legacy field combination needs an explicit mapping or an honest unresolved marker; source reinspection is evidence, not permission to guess absent intent.

**Owner-only decision.** The user explicitly adopts a particular upgraded project. No product-scope clarification is required to build the reversible workflow.

**References.** [Codex CQ22](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq22); [Q22](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q22). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ23

### Pro group, opacity and clipping semantics

**Question answered.** Pro groups and clipping semantics: Validate isolated/pass-through group, opacity/mask order and clipping-base rules before tree/compositor implementation.

**Settled requirements / recorded facts.** Current Pro is a flat layer stack; groups/clipping and a tiled compositor remain open. Legacy flat rendering must remain compatible.

**Engineering decision and rationale.** New groups default to isolated: composite children into transparent group space, apply the group mask and opacity once, then composite the result into its parent using the declared blend operation. Do not apply group opacity separately to overlapping children. Pass-through is explicit: evaluate children against the captured parent backdrop; apply group mask/opacity once to the group's composed change. For the reference definition, interpolate premultiplied color and alpha between the captured backdrop B and fully evaluated result R with weight w = group opacity × mask: B + w(R−B), in the declared compositing domain. Version this behavior and pin its independent oracle before implementation; it is our contract, not a universal claim about another editor. Clipping binds a stable base layer/group ID and uses its declared final alpha coverage. Reordering never silently rebinds it. Hidden/missing base produces no clipped contribution plus an actionable status; deleting/unlinking/rebinding is explicit and undoable, never accidental unclipping. Reject tree cycles and invalid clip dependencies. Legacy flat projects keep their previous render adapter.

**Qualification / next evidence.** Tiny independent reference fixtures cover overlapping children, nested masks/groups, fractional alpha, pass-through adjustments, base hide/delete/reorder and Undo/reopen/export. Use output pixels, not only tree structure.

**Remaining uncertainty.** Blend working-domain details must be pinned per renderer version during R2; compatibility fixtures decide rounding tolerance rather than an unexplained approximation.

**Owner-only decision.** None required for these declared semantics. They do not add Pro Text authoring.

**References.** [Codex CQ23](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq23); [Q17](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q17). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ24

### Capacity increases require aggregate evidence

**Question answered.** Pro capacity tiers and renderer exit: What measured device/effect workloads permit increasing12-layer/4096-edge caps, and what parity/resource proof completes the tiled compositor?

**Settled requirements / recorded facts.** The current qualified baseline has 12-layer/4096-edge limits and whole-bitmap rendering. An 8 MiB cache alone is not a memory-bounded tile compositor. The historical 16 MiB Pro condition is not closed.

**Engineering decision and rationale.** Retain present caps until the new renderer proves a higher named tier. Before raising a limit, budget the aggregate live source decodes, tile interiors/halos, intermediates, masks, histories, proxies/caches, preview and export overlap, including cancellation and replacement peaks. Use a deterministic CPU reference and a bounded tile scheduler with explicit resource ownership/eviction. Complete partial-edge tiles, large transforms, nested group/clip/effect workloads, fractional alpha and overlapping halos. Avoid unbounded concurrency and whole-document fallback hidden behind a tiled API. GPU work follows measured need and independent parity, not a default rewrite. Qualification tiers identify exact device/heap/workloads; total advertised RAM alone is insufficient. Raising a constant or passing a small image does not close P03 or remove the larger professional capacity requirement.

**Qualification / next evidence.** Record open/edit/cancel/Undo/save/cold-reopen/export timings and peak ownership on declared low/mid/high test samples, plus seam/halo/alpha reference parity and teardown after supersession. Retain a specific disposition for the 16 MiB read condition.

**Remaining uncertainty.** Exact larger layer/image limits and support tiers await measurements. This uncertainty does not block B1's controls and guards.

**Owner-only decision.** Shipping capacity exclusions or a reduction of required professional scope remain owner decisions; engineering may qualify progressively larger tiers without repeated permission.

**References.** [Codex CQ24](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq24); [Q17](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q17), [Q23](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q23). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ25

### Subject and Background quality with manual correction

**Question answered.** Subject and Background quality contract: Validate a difficult-edge corpus and correction workflow that defines acceptable conventional subject extraction, refinement and object removal.

**Settled requirements / recorded facts.** Subject extraction/refinement/removal families P12–P15 are open. Existing clone/soft repair is not evidence of segmentation or true inpainting. Runtime AI is excluded.

**Engineering decision and rationale.** Deliver keep/remove assistance and manual trace/refinement with conventional bounded processing. Keep original source, editable alpha/mask, replacement background and operations separate. A user must be able to repair an imperfect edge, restore masked content, refine feather/choke/expand/fringe and preview against light/dark/image backgrounds. Use a finite corpus with hard edges, holes, hair/fur, translucent glass, similar-color foreground/background and contaminated edge colors. Known synthetic boundaries support numerical alpha/error checks; rights-cleared real photographs require visual edge review at output size and zoom. For small-object removal, define the selected region plus any explicitly shown processing/feather support; preserve pixels outside that declared support in a lossless reference. Keep cancellation bounded and reversible, with scratch memory released. Evaluate each candidate algorithm/license against the named corpus before dependency integration; do not promise automatic perfect extraction or reconstruction of unseen detailed scenery.

**Qualification / next evidence.** Complete isolate→refine→replace background and move/refine the subject→restore/cancel→Undo→save/reopen→decoded alpha output, and object removal with outside-region invariance. A menu or simple clone operation cannot close these families.

**Remaining uncertainty.** Algorithm, numeric thresholds and device limits must be chosen from the corpus before implementation acceptance; difficult scenes retain inspectable manual correction and explicit limitations.

**Owner-only decision.** No new question blocks this required family. Any proposal to remove a required correction workflow or ship a materially limited tier needs an explicit disposition.

**References.** [Codex CQ25](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq25); [Q19](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q19). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ26

### Reversible warp, reshaping and material retexture

**Question answered.** Reversible warp and retexture: Validate source-space deformation, freeze regions and texture/shading controls for liquify/mesh/reshape/retexture.

**Settled requirements / recorded facts.** P16 remains open; ordinary image transforms do not implement mesh/liquify, face/body region reshaping or material retexture.

**Engineering decision and rationale.** Bind each deformation/texture operation to an immutable source revision and a versioned source-space mapping. Declare source→deformed object→canvas→viewport transforms and operation order; masks/freeze regions use explicit coordinates rather than current preview pixels. Store editable mesh/displacement, strength, support and freeze/thaw state so reset/replay is deterministic. Preserve frozen structures and protected lines; the boundary transition is part of the operation, not accidental movement. Retexture keeps a separate texture mapping and shading modulation, allowing each to be inspected and adjusted. Texture follows the chosen deformation while original material/source remains recoverable. Cancel restores the whole pre-operation document/history; Apply creates one transaction. Preserve full push/pull/expand/shrink, mesh, selected-region reshaping and retexture/reskin scope.

**Qualification / next evidence.** Use geometric grids/protected lines and folded-material fixtures to inspect deformation, texture coordinates and shading independently; qualify multiple zooms, Undo, reopen and full-resolution decoded output.

**Remaining uncertainty.** The first algorithm's deformation representation and resource limits require R2/R4 evidence. Do not label a simple affine transform as completion.

**Owner-only decision.** None required for the reversible model; all manipulations remain user-directed and offline.

**References.** [Codex CQ26](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq26); [Q19](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q19). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ27

### Explicit content, selection and mask targets

**Question answered.** Selection versus mask target: How should Pro distinguish selected content, transient selection and editable mask target, including link/unlink, target replacement and saved channels?

**Settled requirements / recorded facts.** Current targets and masks are partial. Stable ownership is essential when layers are reordered or replaced.

**Engineering decision and rationale.** Represent content target, transient selection, saved selection/channel, layer mask, group mask and filter mask distinctly. Show a persistent clear content-versus-mask indicator and accessible target name; highlight the same stable object/mask in the stack and canvas. A transient selection constrains an operation without silently becoming a saved mask. Saving a selection/channel is explicit document content. Linked masks follow the declared content transform; unlinked masks keep their own transform and must not move accidentally. Reorder/Undo preserves stable target IDs. Delete/replace/invalid source cancels an affected draft and explains the missing target; never redirect mask painting to image content or the next list index. Commit compound selection operations and mask changes with correct history/persistence. Selection/view changes alone need no content checkpoint, while saved channels/masks do.

**Qualification / next evidence.** Exercise reorder→Undo→immediate paint, target deletion/replacement during async work, linked/unlinked transforms, channel save/reopen and independent content/mask output. Verify no sibling/lower-layer contamination.

**Remaining uncertainty.** Exact visual target badges can use the current style; stable identity and unambiguous ownership are mandatory.

**Owner-only decision.** None. This retains Pro masking and does not expose Regular Masks authoring.

**References.** [Codex CQ27](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq27); [Q17](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q17), [Q18](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q18). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ28

### Frozen donor sampling for retouch

**Question answered.** Retouch donor replay: Validate donor revision and sampling policy for fixed/aligned clone, heal, patch and conventional inpaint.

**Settled requirements / recorded facts.** Codex records immutable donor copying with limited marks; current Soft repair is an opacity blend, not full heal/patch/inpaint.

**Engineering decision and rationale.** Record the donor source/revision, sampling coordinates, fixed versus aligned mode and sampling scope: current layer, current-and-below or visible composite. Freeze the donor result for a committed operation so later unrelated layer edits do not rewrite existing repairs. Define aligned offset continuity across strokes and explicit fixed-source resets. True heal/patch/conventional inpaint keeps editable operation parameters and its declared mask/support, not a misleading label over clone math. An explicit 'Resample repair' or 'Rebase repair' creates a new undoable revision when the user wants new donor content; never do it silently on reopen. Preserve mask/content target ownership and source provenance across transforms. Existing legacy donor operations retain their old render behavior.

**Qualification / next evidence.** Independent known donor patterns establish sampling scope/offset, later-layer-change invariance, rebase Undo, mask boundaries, reopen and exported pixels. Diagnose genuine healing quality separately from copying correctness.

**Remaining uncertainty.** Algorithm and per-operation resource limits need a bounded R3 corpus. Existing 80-mark representation is a foundation, not a final professional acceptance limit.

**Owner-only decision.** None to implement the declared sampling model. Any permanent scope/capacity reduction remains explicit.

**References.** [Codex CQ28](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq28); [Q18](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q18). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ29

### Deterministic brush dynamics and finger behavior

**Question answered.** Brush dynamics and touch fallback: Validate which pressure/tilt/spacing/stabilization behaviors are required initially and how deterministic finger fallback should be presented without shrinking the full brush scope.

**Settled requirements / recorded facts.** Current stroke data does not prove complete pressure/flow/tilt dynamics, and input hardware capabilities vary.

**Engineering decision and rationale.** Use source-space dab scheduling independent of display frame rate/zoom. Serialize the necessary normalized position, time, pressure/tilt and brush-version/seed data or an equivalent replayable operation. Complete explicit hardness, opacity, flow, spacing and stabilization with a visible fixed finger default; do not pretend finger input supplies stylus pressure. Enable pressure/tilt behavior only from usable reported input, and qualify each supported mapping/fallback. Unsupported tilt remains a clearly unavailable hardware-dependent control, not a reason to remove required pressure/touch behavior. Preserve stroke identity, alpha erasing, texture provenance and correct target semantics. Bounded resampling must preserve the whole visible gesture and cancellation guarantees already repaired. Remaining required dynamics stay tracked until delivered.

**Qualification / next evidence.** Replay the same recorded gesture at differing zoom/frame cadence, finger and qualified stylus inputs; compare shape/alpha/output, steady pressure ramps, spacing/hardness/flow, stabilization lag, cancellation and saved reopen.

**Remaining uncertainty.** Exact pressure calibration, stylus devices and texture assets require later hardware/rights evidence; no universal pen compatibility claim is made.

**Owner-only decision.** None for deterministic defaults. Proposed shipping input-device exclusions need an explicit support-tier disposition.

**References.** [Codex CQ29](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq29); [Q18](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q18). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ30

### Declared layered interchange and atomic actions

**Question answered.** Interchange and action failures: Validate the first declared OpenRaster/PSD subset, loss-report presentation and atomic failure semantics for reusable actions.

**Settled requirements / recorded facts.** Native layered fidelity and full P11 interchange/actions remain open; universal PSD compatibility has never been promised.

**Engineering decision and rationale.** Complete native fidelity first. Sequence an initial OpenRaster layered-raster slice, then a named PSD raster-layer subset, after checking the actual primary format specifications, parser/license dependencies and reference files at implementation time. Publish a record-level table: supported representation, appearance-preserving fallback, editability loss, preserved opaque payload, or recoverable refusal. Show material loss before adoption/export; do not silently flatten or reinterpret unsupported groups/masks/effects. Preserve the original file safely; opaque records are inert data with size/validation limits, never executable instructions. Declared first subsets are implementation lanes, not closure of every required interchange child. Reusable actions preflight target types/resources, run against a private document transaction, and either commit the specified result once or roll back completely. Batch/checkpointed partial execution, if later provided, is a separately named mode with per-item receipts. Actions cannot silently retarget absent layers or write external source files.

**Qualification / next evidence.** Independent cross-reader fixtures check pixels, alpha, order and declared records/loss reports, including malformed/oversized/unknown essentials. Inject action failure after intermediate steps and verify exact prior document/history plus retained originals.

**Remaining uncertainty.** Precise PSD/OpenRaster records are not certified by this design answer; pin their actual spec/sample support table during R3. Full required scope remains in P11.

**Owner-only decision.** No new decision blocks native-first engineering. Final advertised interchange coverage or a scope reduction must be explicitly reviewed.

**References.** [Codex CQ30](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq30); [Q18](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q18). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ31

### DNG development and broader camera coverage

**Question answered.** DNG and camera support contract: Validate the first explicit DNG sensor-development variant table and the process for adding broader camera RAW without declaring the first subset complete.

**Settled requirements / recorded facts.** Current sRGB/ARGB8888 decoding is not RAW development. R05 and R06 remain open, and no exact camera support tier is owner-approved.

**Engineering decision and rationale.** Start sensor development with explicitly named single-frame Bayer mosaic and linear DNG lanes, initially uncompressed integer samples with declared bit packing/CFA layout; add compressed and additional variants as separately qualified required follow-ups. The variant table must state black/white levels, linearization, active-area/CFA phase, demosaic, white balance/calibration/profile handling, orientation, compression and essential opcode disposition. A record that is essential but unsupported causes a precise recoverable refusal, not silently wrong colors or geometry. Label embedded JPEG as preview. Use synthetic sensor mosaics plus rights-cleared real negatives with independent numerical/render references; a file opening does not qualify an entire camera model. Record exact sample fingerprints and decoder/profile versions. Broader vendor RAW remains R06 work with exact dependency/transitive/license and camera-sample review; do not add LibRaw or claim commercial rights from this answer. First lanes establish the pipeline and do not mark broader DNG/RAW complete.

**Qualification / next evidence.** Known sensor values, neutral/chart/highlight/noise cases and real negatives exercise development, edit/save/reopen, source preservation and high-precision output. Unsupported/malformed/oversized essentials fail without losing the current document.

**Remaining uncertainty.** Exact compression/opcode/camera tables require primary specification and sample investigation when R5 starts. Camera availability and final supported tiers remain explicit; they do not block B1.

**Owner-only decision.** Only a later shipping coverage exclusion or change to required RAW scope needs owner disposition. Engineering can build the stated first lanes in their scheduled phase.

**References.** [Codex CQ31](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq31); [Q20](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q20). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ32

### Color, alpha and high-bit output

**Question answered.** Color and precision semantics: Validate working-space/alpha/legacy-render boundaries and assign-versus-convert/profile/16-bit export behavior before precision claims.

**Settled requirements / recorded facts.** Current rendering is sRGB/ARGB8888 and TIFF is RGB8. Existing output count is not ICC/float/16-bit qualification.

**Engineering decision and rationale.** Version the new managed pipeline separately from the legacy renderer. Use bounded 32-bit-float reference/working tiles, explicit source/working/output profiles and documented linear versus encoded operation domains. For the first new managed SDR lane, qualify sRGB and Display P3 inputs/output with an explicitly tagged extended-range linear working representation; store the working profile in the recipe and preserve out-of-range values until a deliberate output mapping. Select the exact working primaries/reference conversion before R5 implementation, against independent color fixtures; this is an engineering detail, not a reason for another owner-question cycle. Composite/filter with declared premultiplied alpha, use explicit unpremultiply/re-premultiply boundaries for color operations, and define zero-alpha handling to avoid fringes or undefined samples. Profile assignment changes interpretation; conversion changes samples to preserve the intended appearance. Offer them as distinct actions. Keep legacy projects on their original appearance adapter unless an explicit upgraded copy is adopted. A 16-bit export must contain independently readable 16-bit samples and the declared profile/tags, not padded 8-bit values or a renamed RGB8 writer.

**Qualification / next evidence.** Independent float ramps/charts, gamut-boundary and transparent-edge fixtures cover neutral/legacy pixels, profile assign/convert, proof/output intent and actual PNG/TIFF sample precision. Check reference tolerances before acceptance.

**Remaining uncertainty.** The exact profile engine/working primaries and supported ICC records need bounded technical/license verification during R5. Wide-gamut SDR first is sequencing; HDR stays required.

**Owner-only decision.** No new decision blocks implementation. Permanent color/precision scope reductions are not approved.

**References.** [Codex CQ32](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq32); [Q20](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q20). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ33

### Separate HDR capability claims

**Question answered.** HDR variant qualification: Validate how HDR display/import/tone mapping/export variants should be exposed and qualified while retaining full required HDR scope.

**Settled requirements / recorded facts.** HDR source access alone does not prove HDR editing, display or output. The roadmap separately qualifies SDR color and later HDR variants.

**Engineering decision and rationale.** Maintain an explicit matrix for import/container/codec, transfer function, primaries, signal range, metadata, internal representation, display path and export. Treat HDR import, extended-range editing, HDR display, SDR tone mapping and HDR output as separate capabilities. Qualify named PQ/HLG or other actually supported variants individually with primary format references and real device/output evidence. Explicit SDR conversion is a legitimate user choice with a preview and saved tone-map parameters; never label it preserved HDR. Preserve source/render metadata and original bytes for re-editing. A non-HDR display needs a truthful SDR preview path and capability messaging. No active control may promise an output combination the pipeline cannot create. This applies to future GIF HDR-to-SDR intake and gated Video as well as still images; the full required HDR scope remains tracked.

**Qualification / next evidence.** Highlights, neutral ramps, saturated colors, mixed SDR/HDR composition and metadata are checked with independent readers/reference transforms, declared display devices and decoded exported values. Test unsupported variants recoverably.

**Remaining uncertainty.** Actual codec/display metadata combinations and absolute luminance/tone-map policies require named fixtures and hardware. No universal HDR compatibility is claimed by this agreement.

**Owner-only decision.** Final shipping support exclusions are later scope decisions. They do not justify deleting HDR work or opening Video early.

**References.** [Codex CQ33](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq33); [Q20](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q20), [Q22](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q22), [Q24](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q24). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ34

### Optics and detail need image-quality evidence

**Question answered.** Optics and photographic detail acceptance: What numerical/perceptual reference cases should close lens distortion/CA/vignette/perspective and conventional denoise/sharpen without adding Regular Masks or Histogram UI?

**Settled requirements / recorded facts.** Basic detail helpers exist, while complete optics/denoise/sharpen remains partial. Regular Masks authoring and visible Histogram are excluded.

**Engineering decision and rationale.** Use known distortion grids, calibrated chromatic edge offsets, vignette fields and perspective/straight-line fixtures, together with rights-cleared real scenes. Version manual/lens-profile transforms and record profile provenance. Neutral settings must be identity where defined; correction applies geometry once and preserves crop/source anchoring. Conventional luminance/chroma denoise and sharpening are evaluated for noise reduction, retained low-contrast texture, edge position, halos, clipping and color artifacts. Output sharpening is tied to final output size and is visibly selectable, not silently baked into every export. Numerical scores support, but do not replace, real photographic inspection at relevant display/output sizes. Keep controls in existing Light/Color/Detail/geometry contexts and internal diagnostic math where useful; no general Regular Masks or Histogram UI is reintroduced.

**Qualification / next evidence.** Reference measurements plus real-scene before/after, neutral/legacy controls, Undo/reopen and independently decoded final-size output must agree. Include extreme but supported settings and preserve original hashes.

**Remaining uncertainty.** Actual lens profiles, parameter bounds and perceptual thresholds are chosen from the corpus before the relevant implementation is accepted. Do not claim every lens or noise pattern is corrected.

**Owner-only decision.** None for this quality contract; adding an excluded authoring surface or reducing required optics scope would need an explicit change.

**References.** [Codex CQ34](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq34); [Q07](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q07), [Q20](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q20). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ35

### Inspectable multi-image merging

**Question answered.** Multi-image merge correction policy: Validate inspectable alignment/ghost/seam/focus correction and source-lineage behavior for bracket HDR, panorama and focus stacking.

**Settled requirements / recorded facts.** HDR bracket merge, panorama and focus stack remain required R09 work after single-image geometry/color.

**Engineering decision and rationale.** Keep immutable contributing sources, per-source transforms, exposure/color metadata and merge decisions in the native project. HDR merging exposes alignment and motion/ghost source choices; panoramas expose alignment/source/seam correction; focus stacking exposes focus/depth-transition decisions. Provide bounded manual corrections through task-specific merge controls such as source choice, alignment points and seam/transition adjustment, without creating a general Regular Masks authoring workspace. Preview and final output use the same declared merge recipe. Canceled or failed processing preserves all inputs and the prior document; adopting the result creates a reversible derived revision. Never erase source lineage or report unexplained artifacts as success merely because a file was written.

**Qualification / next evidence.** Separate moving-subject, repeated-pattern, low-texture/parallax and depth-transition fixtures; include bracket extremes and known alignment ground truth. Inspect seams/ghosts/focus boundaries, save/reopen and independently decode output.

**Remaining uncertainty.** Algorithms and numeric acceptance tolerances need measured source corpora. Difficult cases retain explicit correction and recoverable refusal, not a promise of perfect inference.

**Owner-only decision.** No new decision blocks the required merge work in its phase. General Regular mask authoring remains excluded.

**References.** [Codex CQ35](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq35); [Q20](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q20). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ36

### Catalog identity and selective settings sync

**Question answered.** Catalog and selective sync: Validate identity, virtual-copy, sidecar and selective sync rules so photographic settings can transfer without transplanting incompatible geometry/retouch.

**Settled requirements / recorded facts.** Named projects are not a full catalog. Capability families R10/R11, in package R6, require metadata, virtual copies, sidecars/recovery and source-safe selective sync.

**Engineering decision and rationale.** Keep distinct stable identities for source/content revision, catalog item, virtual copy and edit recipe. A URI is an access location. Digests find duplicate candidates; byte confirmation establishes equality, without automatically merging copies or deleting originals. Keep source bytes read-only and store edited metadata in catalog/sidecars or explicit output metadata policy. Selective sync presents a clear set of compatible adjustments: grade/WB/curves may transfer where domains match; source-anchored crop, geometry, retouch, privacy objects and incompatible legacy masks never transplant silently. Each virtual copy retains independent history and source lineage. Backup/restore/relink uses transactional identity mapping and keeps missing entries recoverable. A relink does not become Replace merely because another file has the same name.

**Qualification / next evidence.** The required 100-file catalog/recovery exercise includes duplicate sources, independent copies, absent grants, interrupted sidecars, metadata edits and selective sync across mismatched geometry/precision domains. Reopened outputs and originals remain correct.

**Remaining uncertainty.** Catalog schema/index and exact compatible-setting list are implementation details to pin against the versioned editing model. They do not remove any listed organization features.

**Owner-only decision.** None for these defaults. Final support/scope changes remain explicit.

**References.** [Codex CQ36](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq36); [Q21](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q21). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ37

### Durable batch and per-output results

**Question answered.** Batch commit and retry contract: Validate per-output receipts, destination naming and retry semantics when a multi-output batch stops after some files succeed or a provider result is uncertain.

**Settled requirements / recorded facts.** A single-export journal is only a foundation for batch. External providers cannot be assumed to support one atomic multi-file transaction.

**Engineering decision and rationale.** Freeze job/item/output identity, source revision, recipe/render version, format/options and intended destination. Maintain separate local-staged and externally-published receipts per output. Retain known successful files if another item fails; resume only work known not to have completed. Distinguish Completed, Pending, Canceled, Failed and Destination uncertain. An ambiguous write is not silently retried into the same destination or counted successful/failed by assumption; reconcile if evidence allows, otherwise require an explicit fresh destination. Use deterministic naming/collision rules tied to the output identity and preserve originals. Cancellation stops pending preparation and respects an active provider-copy boundary, then reports all item outcomes truthfully. A crash after provider close but before receipt acknowledgement is explicitly tested as uncertain, rather than creating duplicates to obtain a clean run.

**Qualification / next evidence.** Interrupt before/after staging, during copy and after external publication; resume multiple formats/items with duplicates/collisions and low storage. Prove successful outputs are retained, pending work is idempotent and source hashes unchanged.

**Remaining uncertainty.** Provider reconciliation capabilities differ; document which outcomes can be independently verified. Do not promise arbitrary-provider all-or-nothing rollback.

**Owner-only decision.** None for the durable batch model. Fresh destination selection after uncertainty remains an explicit user choice.

**References.** [Codex CQ37](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq37); [Q16](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q16), [Q21](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q21). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ38

### Typography and independent decoration

**Question answered.** Typography and decorative object quality: Validate Unicode shaping/font fallback, independent-object transforms and layout/output acceptance for Regular and selected tiles.

**Settled requirements / recorded facts.** Regular and selected Collage tiles own Text; Pro preserves legacy Text only. Current font aliases and basic creative objects do not complete typography/decoration.

**Engineering decision and rationale.** Use stable object IDs/order and consistent text layout/shaping for preview and export. Preserve chosen font identity/version, permitted font custody and deterministic fallback information. Support grapheme-safe editing, multiline bounds, bidi/shaping and declared styling without crop/zoom/reopen clipping surprises. A missing/changed font must be reported and recoverable; do not silently rewrite saved appearance. Embed/bundle font data only when its distribution rights allow it. Text, draw, shapes, frames, borders and other decorative objects remain independently selectable, transformable, ordered and editable; changing one cannot mutate siblings or another tile. Preserve legacy data and current stroke/cancellation repairs. Font count or toolbar presence is not completion.

**Qualification / next evidence.** Use a finite corpus with English/Spanish accents, combining marks, emoji, RTL and complex-script examples; multiple sizes/rotation/crop, editable widths/heights, fallback/missing-font, Undo/reopen and independently decoded output. Record the qualified corpus honestly.

**Remaining uncertainty.** Specific font assets, complete script coverage and platform shaping differences need license/sample evidence. Broader required typography stays tracked if a first corpus is narrower.

**Owner-only decision.** None to implement the required workflow. Pro Text authoring remains excluded; permanent language/font scope restrictions need explicit disposition.

**References.** [Codex CQ38](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq38); [Q21](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q21). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ39

### Five hundred finished stickers and durable references

**Question answered.** Finished sticker manifest and retirement: Validate asset quality/count criteria and how removed/reordered packs or custom imports remain usable in old projects.

**Settled requirements / recorded facts.** The requirement is at least 500 finished usable rights-cleared offline assets. The 648-concept planning catalog is not finished artwork.

**Engineering decision and rationale.** Count distinct finished assets, not empty slots, concepts, duplicated file formats or trivial transforms of the same artwork. Each manifest entry has immutable stable ID/version/hash, provenance/license/distribution basis, dimensions/format/alpha, thumbnail, category and search metadata. Inspect every counted asset at useful display/export sizes on light, dark and textured backgrounds. Complete categories/search/favorites/recents and custom transparent PNG/WebP import, with move/scale/rotate/flip/opacity/order/duplicate/delete, history/persistence and selected-tile independence. Retiring a pack from discovery must preserve permitted referenced asset versions or project-owned embedded copies; never substitute different art or break old saves because catalog order changed. Imported custom assets have durable custody/relink status. Keep the browser in Regular/tile scope, not Pro.

**Qualification / next evidence.** Verify all counted manifest assets visually/structurally and exercise a finite set of combined browser/edit/reopen/export workflows, including duplicate instances, retired versions, custom alpha and twenty independent tiles.

**Remaining uncertainty.** Final assets and rights records are production work, not supplied by the concept list. Caches/bundle-size budgets need measurement without lowering the minimum count.

**Owner-only decision.** No new decision needed to fulfill the existing catalog. Reducing the asset requirement or adding prohibited runtime download/account behavior is not authorized.

**References.** [Codex CQ39](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq39); [Q21](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q21). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ40

### Finite complete Collage coverage

**Question answered.** Collage layout acceptance: Validate a finite layout/control corpus proving all seven layouts and 1–20 tiles, including one-tile output, extreme aspect, gaps/borders/rounding/alpha and orientation.

**Settled requirements / recorded facts.** The owner/current model contract is 1–20 tiles and seven layouts; some help still says 2–20.

**Engineering decision and rationale.** Retain a valid one-tile Collage and all seven layouts. Correct touched help to 1–20 and the new single-tap/Options behavior in B1. Use layered coverage: cheap structural geometry for every layout/count pair, independently decoded output at representative and boundary counts including 1 and 20, and pairwise interactions across aspect, Fit/Fill, source orientation, gap/margin/border/rounding and required background/alpha choices. Include extreme supported aspects and maximum control values. Separately run a twenty-tile project with independently identifiable edits and repeated source URIs through reorder, history, save/reopen and output. This bounds redundant combinations without dropping controls or layout families. Apply orientation/clipping exactly once and initialize every output pixel, including borders and transparent gaps.

**Qualification / next evidence.** Check bounds/nonoverlap and expected content regions, then decode real PNG/JPEG or applicable alpha formats. Confirm same-URI independence, correct layout output and source protection at qualified sizes.

**Remaining uncertainty.** Exact representative raster fixtures may be selected by the engineer using current geometry limits. Existing tested combinations remain credited unless changes create a named regression risk.

**Owner-only decision.** None to preserve the 1–20 contract. No reduction to 2–20 or fewer layouts is approved.

**References.** [Codex CQ40](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq40); [Q22](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q22). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ41

### Stable Collage child and source ownership

**Question answered.** Collage child identity and relink: Validate child Apply/Cancel/reorder/late-result behavior and the user-visible distinction between relinking a missing source and replacing a tile.

**Settled requirements / recorded facts.** Duplicate source URIs are legal. Codex records an index-capturing child callback and already-stable replace/relink picker identity in parts of the current route.

**Engineering decision and rationale.** Capture parent document ID, child-entry revision, stable tile ID and source revision. Done applies at most once to that tile, never a mutable index or matching URI, and adds one parent history transaction. Cancel/child Discard restores only its entry draft. If the parent changed, accept only after validating that the same tile/source and compatible baseline still exist; reorder can be rebased by stable ID when no conflicting tile edit occurred. Delete/replace/new-parent or conflicting tile changes reject the stale result and keep recoverable child work, rather than dropping it onto another tile. Relink restores access to the intended source while preserving recipe/object IDs; verify content/geometry compatibility and disclose mismatches. Replace is explicit and undoable: retain the tile's layout-slot identity, adopt the new source and start a neutral per-photo recipe; preview/confirm that old tile edits will reset. Undo restores the complete old source and recipe. Preserve parent layout, sibling edits and duplicate-instance independence.

**Qualification / next evidence.** Done/Cancel, reorder, delete/replace during delayed result, duplicate source, failed relink and mismatched relink cover stable targeting and one transaction. Twenty independently marked outputs survive history/reopen/export.

**Remaining uncertainty.** Existing exclusive child routes may prevent concurrent reordering today; the guard is a minimal safety prerequisite for async/recovery changes, not a new broad concurrency feature.

**Owner-only decision.** None for correct ownership. Intentional replacement/reset is a clearly labeled user action, never an accidental relink.

**References.** [Codex CQ41](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq41); [Q09](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q09), [Q10](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q10), [Q22](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q22). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ42

### Shared accessibility and responsive exits

**Question answered.** Accessibility and responsive acceptance: Validate a finite large-font/screen-reader/keyboard/touch qualification contract for the shared header and all major editing workflows.

**Settled requirements / recorded facts.** The owner requests a polished consistent UI; current fixed rows/different history controls do not prove comprehensive accessibility.

**Engineering decision and rationale.** Adopt 48dp minimum interactive touch targets, 320dp narrow width, portrait/landscape/split layouts and font scaling through 200% as engineering acceptance targets, not measured current capabilities. Keep Options visible/reachable when chrome is shown and always in empty states; preserve ordered Undo/Redo names/disabled reasons. Wrap deliberately and scroll the menu vertically if necessary, never hide major commands in horizontal overflow. Provide logical TalkBack focus, actionable canvas reveal, clear content/mask/tool context, accessible dialogs/progress/errors and focus return after closing a popup. Keyboard/switch traversal and activation must reach actual operations. Spatial tools need meaningful parameter/action alternatives where practical; enumerating labels alone is not a usable workflow. Tapping chrome cannot paint; free-canvas selection toggles only under CQ05's explicit grammar. Pointer cancellation and predictive Back remain safe. Apply the shared-shell matrix in B1, then extend real workflow coverage with later features.

**Qualification / next evidence.** Complete empty open, edit/Undo, Save/Export, dirty Cancel/Discard, child Done/Cancel and Help using touch and accessible navigation at the named window/font boundaries. Check system insets/IME and hidden-chrome recovery.

**Remaining uncertainty.** Device-specific screen-reader quirks and final spatial-tool alternatives require feature-level observation; they stay tracked under Q01/R8 and final all-five qualification.

**Owner-only decision.** None for these quality targets. Final owner usability locks remain separate.

**References.** [Codex CQ42](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq42); [Q05](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q05), [Q23](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q23). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ43

### Provisional physical qualification tiers

**Question answered.** Physical device and media tiers: Which minimum/middle/high physical resource tiers and representative OEM/codec/provider/media cases should define release support and sustained performance budgets?

**Settled requirements / recorded facts.** API29/API35 automation and a scoped owner phone pass are established. Exact owner device, broader physical tiers and the old 16 MiB Pro condition are unresolved.

**Engineering decision and rationale.** Use a bounded test plan with three named samples: constrained resource (the lowest available eligible device, provisionally around 3–4 GB total RAM), middle (about 6–8 GB) and high (about 12 GB or above), recording actual model/SoC/Android/app heap/decoder/provider. These are sample-selection recommendations, not a new minimum-RAM shipping restriction or proof those devices are available. Include API29/API35 automation and current target/platform compatibility before release. Workloads cover existing layer/output limits, 12/24 MP photos, twenty tiles, dense documents, long gestures, actual and representative GIF schedules, low storage/grant loss/providers and cold/warm/sustained use. Record aggregate heap/temp storage, first-correct-pixel latency, cadence, completion and heat with actual sample counts. Keep the 16 MiB Pro condition as a separate bounded compatibility/read test; larger-heap success cannot close it. Use available authorized hardware/CI evidence, list remaining physical gaps honestly, and do not make acquiring a new device a hidden B1 prerequisite.

**Qualification / next evidence.** A fixed matrix identifies source/recipe/device/build, per-lane budget and expected failure/recovery behavior before qualification. Report raw samples/median/range; use percentile claims only with an appropriate sample set.

**Remaining uncertainty.** Exact shipping floor/camera/codec/provider exclusions and owner GIF workload remain unapproved. Measure first; unresolved phone-specific evidence holds only its closure.

**Owner-only decision.** A material shipping support exclusion is a later owner decision. No device purchase, account work or new blanket phone retest is requested now.

**References.** [Codex CQ43](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq43); [Q13](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q13), [Q23](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q23). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ44

### Finite workspace acceptance and four locks

**Question answered.** Owner acceptance dossier and lock boundaries: Validate the exact evidence packet and focused owner tasks for each of four formal workspace locks, preserving already passed supplied checks.

**Settled requirements / recorded facts.** The latest phone report credits unmentioned supplied checks. It does not grant complete Regular, Pro, Collage or GIF locks.

**Engineering decision and rationale.** Prepare one finite dossier per workspace: required family/child exits and status; exact candidate/source/run/APK identity; real edit/cancel/history/save/cold-reopen/recovery and decoded output evidence; source/fault/resource/accessibility proof; declared supported tiers and limitations; and focused owner tasks. Reuse credited results when relevant dependencies are unchanged. Ask the owner only about new behavior or an explicitly named regression/device risk, not the entire old script or manually seeded dense stress fixtures. Block a lock on unresolved required workflow/data-loss/source/privacy/false-save issues unless the owner explicitly changes the affected scope. Record four separate affirmative owner acceptance decisions with the exact build/scope. No test count, percentage, label 'AAA', generic 'looks good' about a small repair, or silence substitutes for those locks.

**Qualification / next evidence.** Cross-check each pre-Video family/child against its dossier and any legitimate explicit scope disposition; retain the owner's prior phone credit and unresolved exceptions separately. Then request each actual lock at R8.

**Remaining uncertainty.** Future family-specific evidence is still to be produced; this defines the stopping condition rather than claiming the present app is finished.

**Owner-only decision.** The four formal locks are owner-only. They are not being requested or granted during Stage 3.

**References.** [Codex CQ44](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq44); [Q02](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q02), [Q23](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q23). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ45

### Future Video clocks and first qualified output

**Question answered.** Video timebase and first qualified lane: Validate the future sequence timebase/source timestamp/rounding and proxy-conform contract, plus the first qualified SDR lane while preserving all later Video scope.

**Settled requirements / recorded facts.** Video implementation remains gated by four explicit locks; full multitrack, nested/multicamera/slideshow and higher-format work remains required.

**Engineering decision and rationale.** After the gate, use stable clip/source IDs, preserved source presentation timestamps, rational sequence time and explicit audio sample clocks. Represent clip intervals as half-open [in,out), with a single documented boundary-rounding policy shared by preview/edit/export; never accumulate floating frame durations or equate VFR frame index with elapsed time. Pin nearest-boundary tie behavior and source-sample selection in the reference schedule before tools depend on it. Proxies retain a reversible mapping to immutable full-resolution sources and are replaced at conform without changing edit decisions. Start with one real H.264/AAC SDR MP4 lane, including true playback/trim/save/recovery/output, then extend the retained precision editing, multitrack/audio/compositing/color/redaction/mastering and measured 1080p/4K/HEVC/HDR tiers. This first lane is sequencing, not a release scope reduction. Do not implement Video screens/engine during B1 simply because the shared header is reusable.

**Qualification / next evidence.** Independent decoded frames/PTS/audio sync across cuts, VFR, speed/reverse, J/L boundaries, proxies/conform, reopen and interrupted jobs must match the reference timeline.

**Remaining uncertainty.** Actual framework/version/codec support and precise integer/rational representation require primary documentation and measured device qualification when R9 opens.

**Owner-only decision.** Four pre-Video locks are prerequisites. No new decision is needed now to preserve this future technical contract.

**References.** [Codex CQ45](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq45); [Q24](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q24). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ46

### Professional audio and photo-plus-audio output

**Question answered.** Professional audio and slideshow acceptance: Validate future sample/channel conversion, mix/headroom/voice/noise-reduction and photo+audio timing acceptance.

**Settled requirements / recorded facts.** V03/V04 require user audio/voice/mixing and real MP4 slideshow; GIF remains silent.

**Engineering decision and rationale.** For the first future audio lane, use 48kHz float internal mixing with explicit mono/stereo mappings, versioned deterministic resampling and preserved original samples/assets. Wider channel/rate variants remain declared follow-ups. Keep headroom internally; normalization is off by default, and clipping prevention/output gain or limiting is an explicit visible choice rather than silently changing the user's sound. Complete clip/track/master gain, range mute/solo, fades/pan, EQ/dynamics/limiter, automation/ducking, buses/stems and voiceover sync with the full roadmap retained. Conventional noise reduction must retain wanted signal as well as reduce noise. Slideshow stores per-photo duration, pan/zoom path, transitions and user audio in the same rational timeline; optional beat-sync uses explicit reproducible event positions, not hidden timing changes. Export is a real MP4 with independently decoded audio and images.

**Qualification / next evidence.** Impulse/tone/noise/speech/music fixtures and listening review cover resampling, gain/headroom, channel mapping, clipping, fades, voice sync, noise attenuation versus desired signal, bus/stem outputs and exact slideshow duration/event timing.

**Remaining uncertainty.** Exact codecs, resampling kernel, channels and device audio paths require later qualification; these defaults are not a universal mastering claim.

**Owner-only decision.** No new approval now. Video implementation remains gated, and bundled third-party music needs actual rights rather than assumed permission.

**References.** [Codex CQ46](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq46); [Q24](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q24). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ47

### Delivered redaction and removal evidence

**Question answered.** Moving redaction and sensitive removal: Validate the future evidence required to prove removed video/audio content is absent from delivered frames/streams/thumbnails, including tracking uncertainty and cut boundaries.

**Settled requirements / recorded facts.** The roadmap requires re-encoded sensitive removal and every affected frame/stream/thumbnail inspection. Preview hiding and edit-list-only trimming are insufficient evidence.

**Engineering decision and rationale.** Version each moving cover/redaction and its tracking/keyframe intervals. Require explicit review of uncertain tracking/cut intervals before claiming the requested concealment is complete; unresolved coverage blocks that sensitive export or requires a clearly chosen safer opaque region. Re-encode affected content and inspect all delivered streams, affected decoded frames and boundary frames, poster/thumbnail/attached media and relevant metadata for retained removed material. Keep source originals intact; the claim concerns the delivered artifact. Distinguish ordinary blur/pixelation effects from strong opaque concealment/removal and avoid an unsupported security guarantee. A cancellation/failure/ambiguous provider write cannot be called a verified completed redaction. Preserve the healthy prior export until the new result is safely published.

**Qualification / next evidence.** Frame-by-frame affected-range and cut-boundary checks, alternate/audio streams, delayed frames, tracking loss and output thumbnails must demonstrate actual absence or intended opaque replacement. Include audio removal/sample boundaries and process/copy interruption.

**Remaining uncertainty.** Tracking quality and device encoder/container behavior require actual future corpus evidence; no probabilistic tracking confidence substitutes for inspected coverage.

**Owner-only decision.** The user resolves uncertain tracked intervals in the editing workflow; public product/privacy claims and publication retain their normal owner gate.

**References.** [Codex CQ47](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq47); [Q24](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q24). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ48

### Final English/Spanish onboarding

**Question answered.** Final bilingual onboarding contract: Validate terminology/glossary, versioned local acknowledgement, decline and returning-user behavior once all-five editing acceptance opens R10.

**Settled requirements / recorded facts.** Final bilingual/customer legal onboarding follows all-five editing qualification and owner acceptance. Current workspace Help still needs consolidation into Options in B1.

**Engineering decision and rationale.** Keep current Help useful now and update only affected interaction instructions during B1. In R10, use a single terminology glossary and complete English/Spanish UI/help/accessibility strings, with human-readable language selection and matching functionality. Store the locally acknowledged document version/language and status; do not add an account, online validation, subscription or IAP unlock. As the proposed flow, decline/cancel preserves projects and returns to an understandable welcome/read-documents/exit route; returning users are not repeatedly asked for an unchanged acknowledged version. A material terms/access change can require a clearly versioned new acknowledgement after the owner approves that policy. String corrections alone must not silently become new consent. Test same-package upgrades and offline return as well as fresh install. Continuous dependency/font/asset diligence stays active during engineering and is separate from these final screens.

**Qualification / next evidence.** Both languages cover fresh/upgrade/offline/decline/revisit/returning-user, changed-version, large-font/TalkBack and editing regressions after integration.

**Remaining uncertainty.** Final consumer legal wording, acknowledgement/reacceptance and paid-access consequences require current research and owner product/legal decisions at R10; this answer does not approve legal terms.

**Owner-only decision.** Final terms and access consequences are owner decisions in their proper phase. They do not block B1 or justify delaying current Help.

**References.** [Codex CQ48](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq48); [Q24](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q24). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ49

### Release ownership and exact artifact dossier

**Question answered.** Release identity and ownership handoff: Validate the exact artifact/signing/upgrade/support dossier needed once the finished-app-and-website gate opens Console work.

**Settled requirements / recorded facts.** Console is not set up. R11 waits for the finished qualified app and owner-confirmed website; production signing/upgrades/publication are not proven by separate debug test packages.

**Engineering decision and rationale.** At R11 assemble exact source/ref/build/toolchain/dependency pins, AAB and installed-split hashes/signature/package identity, release behavior and native/ABI compatibility where relevant. Maintain the exact shipped dependency/transitive/font/asset/profile/codec inventory, provenance and notices. Keep owner-controlled production/upload keys and recovery custody outside the repository; never commit signing secrets. Prove same-package upgrades and source-grant/project migrations using real release paths. Review the actual account's then-current policies/testing/identity/payment requirements with official sources, without assuming an organization-account exemption. Match listing/media/price/territories/privacy/support to demonstrated features. Define data-safe update, support and rollback/migration recovery; a schema downgrade is not automatically safe. Obtain explicit owner approval for the exact public build/listing/price before publishing. No website, DNS, account or unrelated-project work is included in this stage.

**Qualification / next evidence.** Signed install/split and upgrade tests, exact rights/notices manifest, current-account gate evidence, truthful listing and a tested recovery/support plan constitute the release packet.

**Remaining uncertainty.** Future policy/account status, final territories, signing choices and exact support commitments remain to be decided/verified then; no legal or store certification is claimed now.

**Owner-only decision.** Website completion confirmation, account/key ownership decisions and exact public publication approval remain owner-only at R11.

**References.** [Codex CQ49](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq49); [Q24](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q24). The corresponding pinned production paths and inherited receipts are in that response's source index.

## CQ50

### Selected first batch and finite continuation

**Question answered.** Integrated first batch and finite stop: Validate Q25's B1 interaction wave, measured GIF B2 and queued R1.1/R1.2 sequence; identify any concrete minimal prerequisite or affected-task blocker before issuing the final resume instruction.

**Settled requirements / recorded facts.** The exchange has supplied the actual 25 answers, complete historical plans, all 77 mappings and 50 questions. PR #31 remains the qualified application; the new F01–F06 findings justify only their affected repairs.

**Engineering decision and rationale.** Accept B1 first: common header/Options/command cleanup, Collage single-tap, every relevant dirty departure/child transaction, minimal stable save/baseline/result identity and the cancellation consistency changes explicitly named here. Preserve direct editing and all prior output/ownership repairs. B2 workload capture/stage instrumentation may proceed alongside B1 after activation if writers are disjoint; optimize only a measured bottleneck without changing fidelity or established budgets. Then R1.1 → R1.2 → remaining R1, with the complete R2–R11 roadmap intact. Missing owner-specific GIF evidence holds F03 reproduction/closure only; continue independent agreed work and keep its status open. Pin the live engineering baseline after these docs are merged, compare later deltas once, and assign one writer per shared file with a separate reviewer. The resume instruction provides the exact first scope/owners/exits, not another 'agree later' task. Produce one coherent qualified correction candidate, with any unresolved phone-performance evidence stated honestly; do not label all fixes complete or issue serial partial builds to manufacture progress.

**Qualification / next evidence.** Focused actual red-before/green-after route/pointer/save/output cases; trace every existing 193-case assertion through any justified UI-test migration; retain 6s/15s GIF gates; final candidate and merged-app JVM/API29/API35/signing plus independent output evidence and exact APK custody. No app tests/builds run for this documentation stage.

**Remaining uncertainty.** Implementation details or a newly demonstrated prerequisite receive a bounded ledger entry and proceed under existing authority. No arbitrary extra 25/50 cycle, repeated intake or global hold for a task-local observation.

**Owner-only decision.** The final owner-relayed prompt activates Codex after it reads the committed answers/resume. Later formal locks, scope reductions and publication remain owner decisions; routine agreed engineering needs no repeated approval.

**References.** [Codex CQ50](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md#cq50); [Q25](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/df0faa23c4908bfd847410c84798a8c3402b27af/docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md#q25). The corresponding pinned production paths and inherited receipts are in that response's source index.

## Reconciliation outcome

Codex's complete historical plan and all 77 family mappings are accepted as continuity evidence. The canonical documents now select B1, allow independent measured B2 work, retain R1.1 then R1.2 and all R2–R11 children, and credit the scoped phone passes. Specific changes from Codex's proposals are deliberate: the header hides with chrome; selected-tile standalone Save/Open stays distinct from parent Apply; ordinary switches retain Save/Discard/Cancel; Regular→Pro never transfers an explicitly discarded snapshot; changed empty Pro setup is guarded while zero-layer persistence remains R2; provider byte bounds are not universal wall-time bounds. Initial DNG/color/interchange/Video lanes and provisional physical samples never close broader required families.

No additional owner product decision is needed to begin B1. Exact owner GIF workload/phone confirmation holds only F03's specific closure; future shipping exclusions, formal locks, final legal/access terms, website/account/key decisions and publication remain their stated scoped gates. After the owner relays the final prompt, Codex reads this full record and the resume instruction, then engineers the selected batch under the existing branch/PR/verified-merge authority. No app source, tests, build inputs or CI were changed or run by this Stage 3 documentation work.
