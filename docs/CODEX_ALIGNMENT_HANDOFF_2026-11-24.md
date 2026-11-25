# GZ Editor Pro — owner phone report and ChatGPT / Codex alignment

Owner: Lance / Greenezone Software LLC. Report date: **November 24, 2026, America/Denver**. GitHub timestamps may show November 25 UTC.

**Historical Stage 1 record; the requested exchange is now complete.** Codex's actual [Stage 2 response](coordination/CODEX_RESPONSE_TO_25_2026-11-24.md) and [50 questions](coordination/CODEX_QUESTIONS_50_2026-11-24.md) have been read, and ChatGPT's [all 50 answers](coordination/CHATGPT_RESPONSE_TO_50_2026-11-24.md) reconcile the canonical documents. The final owner-relayed prompt and Codex's required reads of the answers/[resume instruction](coordination/ENGINEERING_RESUME_2026-11-24.md) activate engineering. [WORK_LEDGER.md](WORK_LEDGER.md) owns the live phase and selected batch. The original sequence below is preserved as the dated handoff, not a command to restart the exchange or a competing roadmap.

## 1. Authority, purpose and scope

Lance tested the delivered GZ Editor Pro 0.12.10 Test on his phone. His report says that everything worked for the most part and that **if he did not mention an issue from the supplied tests, that check worked**. The previous claim that the owner had not yet tested this build is therefore superseded. Record the unmentioned checklist items as **owner-reported pass**, with the exceptions below. This does not imply acceptance of unimplemented features, all 77 capability families, four formal workspace locks, genuine process-loss recovery or a commercial release.

The owner requests a structured collaboration between the ChatGPT reviewer and his existing Codex engineering session, directly through this repository. The owner will paste a short transition prompt to activate each next session; the durable data, questions, answers and files stay in GitHub. A repository write does not itself start or notify another agent. Review subagents used by either session are not substitutes for the owner's Codex response.

The sequence is fixed:

1. **ChatGPT, now:** commit the phone report, requirements, this handoff and exactly 25 questions.
2. **Codex, next:** inspect current work read-only, answer all 25, supply its complete existing roadmap and reconciliation proposal, then author exactly 50 questions for ChatGPT. Commit its response documents and give the owner a return prompt. Stop before engineering.
3. **ChatGPT, after that return prompt:** read Codex's actual committed responses, answer every one of the 50 questions, reconcile the existing canonical documents, and give the owner the final Codex resume prompt. Only after Codex reads those answers and that resume instruction may application engineering continue.

During this alignment, allowed work is source/document inspection, bounded evidence review and coordination-document changes. Do not edit app source, tests, Gradle, dependencies, manifests, assets or CI; rebuild an APK; rerun the settled suites; start R1.1/R1.2; or implement the newly requested UI. The later engineering authority is preserved but sequenced by this temporary owner-requested hold. If any source work already exists elsewhere, disclose and preserve it rather than deleting or merging it during alignment.

The latest report explicitly authorizes the specified common UI and menu changes once engineering resumes. Older instructions to preserve the existing layout must not block those named changes. Preserve the dark image-first style, functional tool behavior and workspace ownership outside that change. No app-shell rewrite or unrelated reskin is requested.

## 2. Verified baseline and retained engineering evidence

| Item | Exact recorded value |
| --- | --- |
| Repository | [MrSkyrimFan/GZ-Mobile-001](https://github.com/MrSkyrimFan/GZ-Mobile-001) |
| Main observed before this documentation change | `eb8796c92fa654cb0dac371e35d7645ecd07db8a` — PR #33 handoff merge |
| Main tree at observation | `8151c1924fb14577ae01b4ffb1d4658d09fb7920` |
| Qualified application merge | `3ad65c533f7667a136d4fa1838afcc06d3f484da` — PR #31 |
| Qualified candidate | `aa86c37c64c5e395d5378df281a0c7583a39d0e0` |
| Identical candidate / application-merge tree | `1c7e3007204192ea240a7fd8d65cbd00cf7f9511` |
| Candidate / merged workflows | [45480001019](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45480001019) / [45483297512](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45483297512) |
| Recorded qualification | JVM / build / signing pass; Android 10 and Android 15 each 193/193 on candidate and merge; zero failures/errors/skips |
| Phone build | GZ Editor Pro 0.12.10 Test; code 22; `com.greenezone.gzphotoeditor.phonetest.v22` |
| Delivered APK | `GZ_Editor_Pro_0.12.10_Test_PR31.apk`; 32,899,125 bytes |
| APK SHA-256 | `ed362e928db9db1f3f37c7a82c99141d64af42db1112ed8eacbdec6f466dfb80` |
| Source artifact | [Merged API35 exercised APK, 14963127303](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45483297512/artifacts/14963127303) |

The direct APK was recovered and its exact size, SHA-256 and archive CRC verified in the preceding delivery. This task does not require another download or build. The current live-source check found no open pull requests. Comparing the qualified application merge to the observed main found ten documentation paths changed and no app/test/build/CI changes. Codex must check live refs again when it starts; a branch or unpublished session state is not inferred absent from that PR check.

Read the active sources in this order:

- [AGENTS.md](../AGENTS.md), this handoff, [CONTINUITY.md](CONTINUITY.md), and [WORK_LEDGER.md](WORK_LEDGER.md).
- [ERASER_REPAIR_VERIFICATION.md](ERASER_REPAIR_VERIFICATION.md) and [PR31_0.12.10.json](qualification/PR31_0.12.10.json).
- [GZ_ENGINEERING_ROADMAP.md](GZ_ENGINEERING_ROADMAP.md), [CAPABILITY_MATRIX.md](CAPABILITY_MATRIX.md), [PRODUCT_SPEC.md](PRODUCT_SPEC.md), and [RELEASE_GATE.md](RELEASE_GATE.md).
- [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29), [PR #30](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/30), [PR #31](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/31), and [PR #32](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/32) as the prior repair/qualification chain.

The [previous delivery handoff](NEW_CHAT_HANDOFF_2026-11-24.md), [Audit 2 entry](gz-editor-pro-audit-2-20261123/00_REPOSITORY_START_HERE.md), [field guide](gz-editor-pro-field-guide/00_START_HERE.md) and [older review handoff](review-handoff-2026-11-23/README.md) retain their dated evidence. Do not revive their old immediate tasks or redo their full audits.

### Work already credited

PR #29 corrected fresh privacy-cover anchoring through manual Crop; bounded Regular save/export rejection before damaging healthy saves; complete long Draw and cancellation within the existing 500-point cap; and complete Pro lasso within its 450-point cap. Its final candidate and merge passed 186/186 per API, retaining all 174 original cases. Its observed storage and test-scheduler corrections remain credited.

PR #31 corrected visible/swept segment erasure and complete rollback when an erase would exceed the 160-stroke limit, in Regular and independent selected Collage tiles. It preserved unrelated artwork, dirty work, history, named reopen and decoded PNG output. It also corrected the GIF Save failure observed in its small qualification fixture with an exact bounded detail-aware export cache. The final 193-case set retains all 186 earlier identities, five Eraser cases and two actual Android cache-equivalence cases.

Preserve the failed reproduction and candidate history. The original API29 rotation abort was a test-harness failure before Eraser execution; candidate 1's 28.331-second GIF Save failed the unchanged 15-second limit; the changed final candidate and merge passed. None of these records is rewritten as though the failures never occurred.

The merged 180×320, 88-frame, 2.950-second portrait GIF fixture saved in 3.872 seconds on Android 10 and 3.334 seconds on Android 15. The new physical report below is a different evidence item with unspecified workload details. It neither erases that scoped success nor is resolved by citing it.

## 3. Owner phone report and requested changes

The original report is normalized below without changing its meaning. The owner described the interface as inconsistent and difficult to navigate and asked for a polished, deliberate, natural experience. This is an engineering and usability requirement, not a request for unsupported marketing claims.

| ID | Owner finding / instruction | Current disposition and required outcome |
| --- | --- | --- |
| F01 | Collage requires a quick double tap to hide or restore the UI while editing. | Owner-reported defect. One deliberate tap must hide chrome and one tap restore it in both parent Collage and the selected-tile editing route. |
| F02 | Leaving Collage, Regular and Pro does not display the expected safety/discard menu for unsaved work. | Owner-reported defect. Protect the correct dirty document on every relevant exit or replacement route; preserve work when the user cancels or saving fails. |
| F03 | GIF Save can take approximately 2–5 minutes, although the menus and options now work. | Owner-reported performance defect, approximately 120–300 seconds. Diagnose and improve the real workload; preserve the functioning controls and output fidelity. |
| F04 | Major file/help commands are scattered or hidden behind horizontal scrolling. | Requested change. A clearly titled **Options** dropdown in the top-right is the one discoverable home for major actions in every workspace. |
| F05 | Create collage and Histogram should not be options in Regular, and the More section should disappear entirely. | Requested change. Remove those Regular UI entries and the More section; keep Collage reachable through the workspace selector. |
| F06 | The workspaces use inconsistent top areas and Undo/Redo presentation. | Requested change. Use common header structure, visual style, positions and interaction rules across Regular, Pro, Collage and GIF; future Video inherits them when its gate opens. |

**Positive result:** the owner explicitly reports that unmentioned checks in the supplied phone script worked. This includes the tested editing/menu interactions not listed as exceptions. Do not keep describing the whole phone pass as unperformed or reopen the Eraser/Crop/lasso repairs merely to generate more work.

**Not supplied:** device model, Android version, clip duration/resolution/codec, selected GIF output dimensions/FPS/frame count, exact recipe, provider, and timing split between encoding and final copy. The report is not an instrumented performance trace. Record these as unknown. Codex should define the smallest useful collection plan and proceed with source analysis; Stage 2 does not require the owner to repeat all tests or hand-build dense stroke fixtures.

### F01 — single-tap interaction contract

The intended behavior is one tap to hide and one to show the editing chrome, with active crop/brush/selection handles still visible and usable. Inspect parent Collage and selected-tile event handling separately. Identify who consumes pointer events and how a tap is distinguished from a drag, pinch or active-tool gesture. Retain established tool use and any existing deliberate zoom gesture. Do not "fix" this by merely shortening a double-tap window, swallowing brush input or toggling chrome after every editing gesture.

Acceptance after engineering: actual pointer tests and phone review demonstrate both toggle directions, with tools active and inactive, no spurious edits/history, no sibling changes and no hidden editing handles.

### F02 — complete safe-departure contract

Inventory physical Android Back, app navigation/close, workspace selection, New/Open media, replacement and opening a different saved project. Regular, Pro and Collage are reported failing. Check GIF against the common contract without inventing a GIF defect before evidence.

The working contract for reconciliation is **Save / Discard / Cancel** when leaving dirty work at a route boundary that needs protection:

- Cancel stays with the same document, selection and history.
- Save completes the requested departure only after a successful durable project save. A failed, refused or canceled save keeps the work and route intact.
- Discard affects only the intended working changes. It never deletes source media, another workspace, sibling tiles or named saved projects.
- Back first dismisses the active menu, keyboard, dialog or tool context as appropriate. A single committed Back must not also fall through into an unconfirmed destructive departure.
- Clean documents do not need a false dirty warning. Returning from a canceled half Back gesture must not commit an exit.
- A selected Collage tile is a child transaction. Done, Cancel, child Back and parent exit must each target the right state and apply at most once.
- Retaining an in-memory draft is not permission to ignore the owner's stated workspace-exit safety expectation. Explicitly reconcile mode switching with the existing Regular→Pro Yes/No transfer and retained Regular draft, without duplicate or contradictory dialogs.

Codex must provide the full state/route table and propose any remaining semantic choices in its 50 questions. These clauses are the proposed implementation contract translating the owner's safety request; do not claim the current build already implements it.

### F03 — GIF performance without output shortcuts

The controls now work on the phone; retain that success. Measure source open/decode/seek, rendering and effects, palette work, GIF encoding, staging/validation, destination-provider copy and visible UI completion separately. Include source characteristics, canvas, frame schedule, effects and device/thermal state in a reproducible workload record.

Keep existing quality math, dimensions, timing, frame count, Reverse, transparency/disposal/loop behavior and source originals. Do not silently lower FPS/resolution, drop frames, remove effects, change defaults, weaken the 6-second sampled-motion / 15-second Save fixture gates or use an unchanged retry as a repair.

A 120–300-second owner report does not identify a bottleneck by itself. Optimization hypotheses need measurements. Bound memory, caches, workers and temporary storage, and preserve cancellation and truthful provider-failure reporting. Agree a realistic workload/device acceptance table in Stage 3; do not promise a universal Save time before the phone workload is known.

### F04–F06 — common shell and command ownership

The required destination is a common UI grammar, implemented through reusable components where appropriate. Regular, Pro, Collage and GIF should look like parts of one application. Keep the same header alignment, spacing, typography, control sizes, Undo/Redo convention and Options position. Different editing surfaces, layers, timelines and tool decks retain their appropriate functions.

**Chosen consistent history presentation:** use the same paired Undo/Redo arrow icons, in the same order and header positions, with accessible names "Undo" and "Redo", consistent disabled states and adequate touch targets. This choice follows the owner's instruction to choose one convention; it is not a claim that he specified a particular icon asset.

**Options** is the visible top-right title. Its core commands are:

| Command family | Shared meaning and workspace adaptation |
| --- | --- |
| New / Open media | New photo or source in Regular; the appropriate new/open document or media action in Pro, Collage and GIF. Protect current work first. |
| Open projects | Open the selected workspace's saved editable project. |
| Save project | Save editable work. Keep distinct Save as / rename behavior only where it represents a different purposeful action. |
| Export | Produce a finished image or animation through that workspace's existing export options. Save project and Export are not interchangeable. |
| Help | One directly discoverable dropdown item opening relevant help. Do not bury it in a scrolled tool strip or a second More/File path. |
| Other major workspace commands | Include only relevant commands, with clear ownership and destructive confirmations. Reuse existing working callbacks where sound. |

Codex must supply exact labels, order, availability and before/after locations for every workspace. Shared conceptual commands may use source-appropriate names. The workspace selector remains clear and separate from file actions; do not force the owner through an ambiguous second overflow to switch modes.

Remove redundant visible entry points for the same action, including old File/More routes and scattered header/dock file/help buttons after their authoritative Options destination exists. Inventory empty-state launch controls too; do not silently create an exception to the owner's no-duplicates rule. Selection-specific Done/Cancel, playback, tool controls and Undo/Redo are different action families and keep their necessary direct access. Do not hide editing under Options.

Remove Regular's More section entirely. Move its still-required major commands to Options. **Do not move Histogram into Options or another Regular menu under a new label. Do not move Create collage into Regular's Options.** The separate Collage workspace remains. Keep underlying histogram math/diagnostic tests where needed; hiding that UI does not justify deleting tested image processing. Reconcile the older R03 histogram requirement explicitly, and do not silently reintroduce it in future Regular work.

The owner authorizes this bounded UI consolidation. Preserve existing editing output, source custody, the separate workspaces and legacy documents. No Regular Masks authoring or Pro Text authoring. Future Video inherits the common shell only after the four pre-Video workspace locks; a shared component is not permission to begin Video now.

## 4. Complete remaining scope to preserve

The [canonical roadmap](GZ_ENGINEERING_ROADMAP.md) and [77-family inventory](CAPABILITY_MATRIX.md) remain authoritative. The summary below is a navigation aid, not a substitute for their full content or for Codex's full-roadmap response.

| Package | Required remaining outcome |
| --- | --- |
| R0 / prior correction evidence | Preserve completed PR #29 and PR #31 corrections and failure history. Reopen only a named new owner finding or demonstrated dependency/evidence gap. |
| R1 | Durable documents, source/provider custody, safe Back/history, bounded project metadata/loading, recoverable working drafts, schema/render migration, saved repeated Regular↔Pro exchange and portable bundles. R1.1 remains bounded listing/loading; R1.2 remains recoverable Regular drafts. |
| R2 | Pro layer tree, groups/clipping/locks, object/canvas geometry and memory-bounded reference compositor with preview/export parity. |
| R3 | Complete Pro selections/masks, paths/shapes, precision transforms, paint/eraser dynamics, clone/heal/patch, effects and declared interchange/actions; no Text authoring. |
| R4 | Fully offline, non-AI Subject & Background: selection/extraction, removal/replacement, refine/fringe cleanup, conventional object removal and reversible mesh/liquify/reshaping/retexture. |
| R5 | Regular photographic development, DNG/RAW, numerical/float/16-bit/color/profile management, optics, denoise/sharpen and HDR/panorama/focus stacking, honoring the new visible-UI removals. |
| R6 | Catalog/metadata/virtual copies/sidecars/recovery, durable batch, typography/decoration, and at least 500 finished usable rights-cleared offline stickers with custom transparent imports and independent tile use. The 648 concepts are not 648 finished assets. |
| R7 | Full GIF import/playback/timeline/frame assembly/timing/palette/captions/persistence and reversible legacy v4 aspect migration; complete seven-layout, 1–20-independent-tile Collage with accurate save/reopen/export. |
| R8 | Representative device/OEM/codec/memory, accessibility, sustained performance and failure qualification, then four separate explicit owner locks. |
| R9 | Gated professional Video: multitrack and precision timeline, user audio/voice and mixing, keyframes/compositing/color/effects, photo+audio slideshows, moving redaction, mastering/relink/recovery and real verified output. |
| R10 | Complete all five editing workspaces and obtain editing acceptance, then final English/Spanish welcome, help, localization and customer legal/access flows. |
| R11 | Finished app AND Greenezone website before Play Console setup; current account-specific policies, rights/SBOM/notices, production signing/AAB/splits/same-ID upgrades, store testing/listing and explicit publication approval. |

The new owner findings take priority for reconciliation. Stage 3 will select the dependency-aware first engineering batch, normally the named phone defects and common chrome, while retaining R1.1/R1.2 and the full sequence. A prerequisite durability slice may be coupled to exit safety when justified; do not silently replace the whole roadmap with a UI-only plan.

Non-negotiable product contract: Android 10+, fully offline, $2.99 one-time target, no runtime AI/ML inference, cloud/account requirement, ads, telemetry, subscriptions, credits, IAP/microtransactions or watermark. Preserve originals, legacy project formats, named-save identity, independent 1–20 tiles and source/recipe/export fidelity. Engineering may use AI assistance; the application must not acquire runtime AI.

Do not modify the Greenezone website, DNS, Play account, XForge, GZFX, Beside-Me or any other repository. The old 16 MiB legacy-Pro heap condition, genuine process-loss/picker behavior, broader device/media tiers and legacy v4 GIF framing obligation stay tracked. A successful supplied phone script does not close those separate evidence requirements.

## 5. ChatGPT's 25 questions for Codex

Answer **Q01–Q25 exactly once, in order**, with the same identifiers. Use actual source paths and pinned refs where relevant. Separate observed facts, evidence inherited from earlier receipts, owner requirements, proposals and unknowns. Do not run a new blanket audit to answer a question, and do not invent evidence or claim a proposed fix is implemented.

### Q01 — Exact engineering state

What is your exact current engineering state, including live main, your working baseline, open branches/PRs and any uncommitted or unpublished work, and which application changes are newer than the qualified 0.12.10 merge?

Include exact refs, file ownership and divergence. Preserve unrelated or unpublished work; explain discrepancies before integration.

### Q02 — Your full existing roadmap

What is the complete roadmap you previously created and have actually been using for GZ Editor Pro?

Supply its **full content**, including every phase, child task, status, prerequisite, acceptance criterion and deferred item, with its original location and revision. A link, outline, abbreviated R0–R11 table or freshly invented substitute is insufficient. If it is identical to the canonical repository roadmap, identify that fact and include the complete content at a pinned ref. Preserve the pre-handoff plan separately from proposed changes. If an earlier plan cannot be recovered, say exactly what is missing and label any reconstruction.

### Q03 — Full-scope reconciliation

How does your operative roadmap map to all 77 capability families and the canonical R0–R11 plan, and what omissions, contradictions, duplicate work or unsupported completion claims need correction?

Provide a discrepancy table with source, affected family, existing status, proposed disposition and rationale. Preserve completed repairs and every required child capability; do not manufacture a new completion percentage.

### Q04 — Current command inventory

Where does every workspace currently expose New/Open media, Open projects, Save, Save as, Export, Help, workspace switching, Undo/Redo and destructive commands, and which entry points duplicate or conceal those actions?

Give current path/callback ownership and a before/after command-location table, including parent Collage, selected-tile editing and empty states.

### Q05 — Shared header and history convention

What common header arrangement and reusable implementation will give Regular, Pro, Collage and GIF the same structure, spacing, paired Undo/Redo arrow icons and top-right Options menu while preserving clear workspace selection and image-first editing?

Include a compact layout specification for narrow phones, landscape, large fonts, accessibility labels and disabled states. Future Video must inherit the design only when its implementation gate opens.

### Q06 — Options menu contents and action ownership

What exact Options menu will each workspace expose, including labels, order, clean/dirty/empty/busy availability and one authoritative visible location for each major action?

Show how project Save differs from Export, how media labels adapt to the workspace, how existing callbacks remain correctly scoped, and which redundant File/More/header/dock entries are removed.

### Q07 — Regular cleanup and surviving controls

Which bounded UI changes remove the entire Regular More section, Create collage command and Histogram entry while preserving all other required controls, document compatibility and tested image processing?

List where surviving commands move. Reconcile R03 explicitly; do not put Histogram or Create collage back into Regular under another menu.

### Q08 — Collage single-tap behavior

What source-supported explanation and decisive interaction test will identify and correct the rapid-double-tap requirement for hiding/restoring Collage chrome?

Inspect parent and selected-tile routes separately, pointer consumption and recomposition/gesture ownership. Preserve active guides, brush/drag/pinch input, selection, history and sibling independence. If the cause is not yet established, state the hypothesis and discriminating check.

### Q09 — Safe departures across workspaces

What full navigation and dirty-state matrix will protect Regular, Pro and Collage when leaving, switching modes, opening/replacing media or opening a different project, including Save / Discard / Cancel and failed-save behavior?

Cover physical Back, app routes, transient tool/menu/dialog/keyboard precedence, half-cancel/double Back, clean documents, child tile Done/Cancel, and the retained Regular→Pro Yes/No handoff. Check GIF against the common rule without assuming a reported GIF exit failure.

### Q10 — History, selection and asynchronous command integrity

How will the common controls and departure handling preserve one history checkpoint per completed gesture, cancellation rollback, selected-object identity and immutable asynchronous command ownership?

Include rapid taps, double submissions, pending picker/export/save callbacks, layer reordering, parent/child transactions and protection against a result affecting a newer document.

### Q11 — The phone GIF slowdown

What evidence is available for the reported 120–300-second GIF Save, what workload facts are still unknown, and what measurements distinguish decode/seek, effects/rendering, palette/encoding, staging, provider copy and visible completion?

Explain the limits of the small qualified fixture and propose the minimum useful owner-media/settings capture. Do not treat absent details as permission to dismiss the report or block all response work.

### Q12 — GIF improvement hypotheses

Which concrete performance hypotheses and bounded implementation strategies could reduce the phone Save time while preserving pixels, dimensions, frames, timing, edits and source media?

Tie each hypothesis to evidence or a discriminating experiment, and include memory/cache/worker limits, cancellation, temporary storage, thermal behavior and provider recovery. Stage 2 proposes the work; it does not implement it.

### Q13 — GIF acceptance and workload coverage

What workload/device table, stage timings and independently decoded outputs will establish acceptable GIF Save performance after correction while preserving the existing CI limits?

Distinguish cold/warm, small/large and short/long exports, first correct picture versus sustained playback, and UI completion versus final file availability. Do not invent a universal seconds target or reduce fidelity to meet it.

### Q14 — R1.1 bounded saved-project loading

What exact R1.1 implementation and acceptance plan provides lightweight listing metadata and lazy selected-recipe decoding across twelve near-budget saves plus corrupt/failed entries?

Name affected stores/UI callers, migration compatibility, correct selected identity, healthy-byte preservation and measured memory bounds.

### Q15 — R1.2 and actual process-loss recovery

How will R1.2 atomically persist completed Regular edits, exclude transient gestures and restore source identity/grants, document/history/navigation state after genuine process loss?

Describe interruption boundaries and a real process-loss check distinct from Activity recreation, then explain the planned extension to Pro, Collage and GIF.

### Q16 — Shared document and export custody

What remains for schema/render-version compatibility, transactional migration, immutable export jobs, ambiguous/partial provider writes, source relinking, portable bundles and repeated saved Regular↔Pro exchange?

Identify unsupported cases and bounded recovery while preserving originals, native parents, alpha/profile/precision where available and older named projects.

### Q17 — R2 Pro foundation

How will the complete Pro layer tree, groups, clipping, locks, canvas/object geometry and memory-bounded reference compositor be delivered while preserving legacy pixels?

Define group opacity/blend/mask semantics, preview/export parity, cache/resource ownership and evidence required before raising the existing layer or image limits.

### Q18 — R3 professional Pro tools

What complete workflows and acceptance evidence remain for selections/masks, precision transforms, paths/shapes, brush/eraser dynamics, clone/heal/patch, ordered effects, declared layered interchange and reusable actions?

Distinguish existing foundations from finished workflows, target ownership and Undo/save/export behavior. Preserve legacy Text without Pro Text authoring.

### Q19 — R4 Subject & Background

What conventional offline implementation and qualification plan will deliver subject extraction, background removal/replacement, edge refinement, small-object removal and reversible reshaping/retexture without runtime AI?

Include cancellation, editable masks/layers, difficult-edge limits, independent output and dependency/asset licensing evaluation.

### Q20 — R5 photographic development

How will Regular's remaining tone/color, RAW/DNG, float/16-bit precision, profile management, optics, denoise/sharpen and multi-image processing be completed and independently qualified?

Define supported variants and reference evidence without claiming unsupported RAW/ICC/HDR behavior. Honor no Regular Masks authoring and the new removal of its visible Histogram entry.

### Q21 — R6 organization, batch and creativity

What remains for local catalog/metadata, virtual copies/sidecars/recovery, durable batch, Unicode typography, decoration and at least 500 finished rights-cleared offline stickers?

Cover interrupted/idempotent jobs, source protection, original/licensed asset provenance, custom transparent PNG/WebP and independent selected-tile editing. A concept catalog is not finished artwork.

### Q22 — R7 GIF and Collage completion

What capabilities and evidence remain for GIF frame/segment assembly, holds/loops/timing, captions/palette, media compatibility and reversible legacy v4 framing, and for all seven Collage layouts with 1–20 independent tiles?

Include duplicate-source identity, relink versus replace, history, missing media, named reopen, accurate output and resource limits. Preserve the newly reported working controls.

### Q23 — R8 definition of a finished, usable product

What concrete definition of "good" will govern consistent touch/menu behavior, accessibility, representative device/media/memory tiers, prolonged use, storage/provider failures and the four owner workspace locks?

Use observable end-to-end exits and explicit limitations. Explain what the current owner-reported passes close and what they do not.

### Q24 — Complete gated Video, onboarding and release

What full work remains for professional Video, final English/Spanish onboarding and commercial release, and how will the required gates remain enforced?

Retain precision multitrack editing, user audio/voice/mixing, compositing/keyframes/color, photo+audio slideshow, moving redaction, real output and recovery. Preserve four pre-Video locks, all-five editing acceptance, website-before-Console sequencing, signing/upgrade/store qualification and explicit publication approval.

### Q25 — Proposed integrated execution sequence

After the complete question exchange, what dependency-ordered batches should address the new phone findings and common UI, then continue R1.1/R1.2 and the full roadmap?

For the proposed first batch, state exact baseline, affected files, one owner per shared file, observable exit, decisive reproduction, regression/output evidence and candidate/merge qualification. Identify true blockers and any justified prerequisite slice. Do not start implementation until Stage 3 answers and the resume instruction exist.

## 6. Required Codex Stage 2 deliverables

Create these files in the repository; they do not exist merely because their names appear here:

| File to create | Required contents |
| --- | --- |
| `docs/coordination/CODEX_RESPONSE_TO_25_2026-11-24.md` | Q01–Q25 answers with references; the complete pre-handoff operative roadmap as a clearly labeled evidence snapshot; full discrepancy/coverage table; proposed menu/exit/engineering sequence. |
| `docs/coordination/CODEX_QUESTIONS_50_2026-11-24.md` | Exactly CQ01–CQ50, authored by Codex after its inspection and answers. |
| Existing `docs/WORK_LEDGER.md` | Update the alignment phase to "Stage 2 complete — awaiting ChatGPT answers" and link the actual committed response paths/ref. Keep engineering paused. |

The full returned roadmap is a comparison snapshot, not a second active master plan. If that roadmap has referenced child documents, include their complete necessary task content or a pinned full-source appendix; do not replace them with "see previous session." Keep your original roadmap and proposed reconciliation distinct. Do not silently overwrite the canonical roadmap or owner requirements during Stage 2.

Use a documentation-only branch and reviewable PR. Verify the diff contains only the coordination responses and necessary ledger/continuity status updates. Follow authorized repository merging rules; a docs-only reviewed merge is allowed and does not require rebuilding the app. If a merge is blocked, leave a readable committed branch and identify the exact ref in the return prompt. Do not force-push, overwrite another agent's work or misrepresent an unmerged branch as main.

### Requirements for Codex's 50 questions

- Exactly 50 substantive, distinct questions, with headings `## CQ01` through `## CQ50`.
- For each: state the decision to resolve, your current understanding, relevant source/ref, why it affects implementation or acceptance, and a proposed default where useful.
- Cover the full app: common shell/Options/command ownership; dirty departures and document recovery; GIF performance/fidelity; Regular RAW/color; Pro layers/tools/Subject & Background; Collage; catalog/batch/text/stickers; accessibility/device qualification; and gated Video/onboarding/release.
- Do not ask ChatGPT to reauthorize settled constraints such as $2.99, no runtime AI, no ads, workspace separation or the Video gate. Use those as inputs.
- Investigate technical facts available in the source yourself. Questions should resolve design/acceptance decisions or contradictions, not outsource a basic source read.
- Do not invent fifty owner uncertainties merely to fill the count. Questions may ask ChatGPT to validate reasoned engineering tradeoffs against the recorded requirements.
- Unknown device/camera/interchange tiers must remain explicit. ChatGPT can recommend a bounded default, but neither agent may represent an unapproved product-scope reduction as the owner's decision.
- Do not write ChatGPT's answers, simulate agreement or begin repairs in the same response.

Before handing back: verify 25 answers, a genuinely full roadmap snapshot, all 77 family mappings/dispositions and exactly 50 questions. Do not repeatedly restart this intake after a small correction; resolve a named missing item directly.

### Prompt Codex must give the owner at completion

Supply a copy-ready prompt addressed to ChatGPT using this structure, filled with real refs and links:

> Continue the GZ Editor Pro three-stage alignment in MrSkyrimFan/GZ-Mobile-001. Codex Stage 2 is complete at [exact commit and branch or merged PR]. Read docs/CODEX_ALIGNMENT_HANDOFF_2026-11-24.md and the current WORK_LEDGER, then read in full [actual CODEX_RESPONSE_TO_25 link/path] and [actual CODEX_QUESTIONS_50 link/path] at that ref. The response includes all 25 answers and my full operative roadmap snapshot. Review the proposed reconciliation, answer CQ01–CQ50 in the repository, update the single canonical roadmap/ledger/specification where justified, and provide the final prompt for Codex to read those answers and resume the agreed engineering batch. Application engineering remains paused until that exchange is complete. No APK rebuild or app-source changes were made during Stage 2 [include any actual exception honestly].

Codex must stop after publishing that response and prompt. The owner carries the activation prompt; no ZIP transfer is required.

## 7. ChatGPT Stage 3 obligations

On the owner's return prompt, read the actual response at its supplied ref and compare it with live main. Verify that it contains the full 25 answers, full roadmap and exactly 50 questions. Missing technical evidence gets a specific disposition, not a new blanket audit.

Answer every CQ identifier in `docs/coordination/CHATGPT_RESPONSE_TO_50_2026-11-24.md`, quoting or restating enough of the question for the answer to be self-contained. For each, distinguish:

- settled owner requirement or measured fact;
- engineering decision/recommendation and rationale;
- remaining uncertainty and how it will be resolved;
- any actual owner-only product scope/publication decision.

Reconcile conflicts in the existing PRODUCT_SPEC, GZ_ENGINEERING_ROADMAP, CAPABILITY_MATRIX, RELEASE_GATE, CONTINUITY and WORK_LEDGER. Preserve all 77 family IDs and necessary scope. Approve a common command/menu/dirty-exit contract and first engineering batch with evidence expectations. Do not treat a recommended design as previously owner-approved where it is not.

Create `docs/coordination/ENGINEERING_RESUME_2026-11-24.md` only after the answers and reconciliation are complete. It must link the reviewed Codex response, all 50 ChatGPT answers, final canonical documents and agreed batch; record exact refs, scope, owners/file boundaries, observable exits and genuine blockers. The ledger becomes "Alignment complete — ready for owner to relay Codex resume prompt." A concrete unresolved blocker can hold its affected task while independent agreed work proceeds; do not fabricate agreement to clear a gate.

Return a copy-ready Codex prompt referencing those exact committed documents and explicitly directing it to read them before beginning the agreed engineering. The three-stage documentation exchange is then finished; do not require another arbitrary 25/50 cycle.

## 8. Engineering rules after the exchange

Continue the existing application through bounded, evidence-led changes. No reset, rebuild-from-scratch, competing master backlog or feature disappearance. Use distinct implementation/review owners, one writer per shared file, and integrate through the repository.

For actual app changes, preserve earlier successes, reproduce the named defect through real UI/project/export routes, independently inspect outputs, and retain required full JVM/API29/API35 qualification at the final candidate and merge. Keep old failure receipts and exact source/run/APK identity. Documentation-only work does not create a new qualified binary.

Provide one coherent next phone candidate when the agreed correction wave is qualified. Ask the owner only for the remaining device-specific observations, with the smallest useful script and workload information. Do not ask him to repeat successful checks without a named regression risk, build 160 strokes by hand, or mediate files through ZIP folders.

Formal workspace locks, Video authorization, customer onboarding timing, website dependency and production publication remain as defined in the canonical roadmap. This handoff authorizes the requested coordination and future agreed corrections; it does not declare the app finished.
