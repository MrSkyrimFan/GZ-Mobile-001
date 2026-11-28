# Owner scope addendum — Pro remains unfinished

Date: November 27, 2026 (America/Denver).

**Authority and purpose.** Lance supplied this clarification before sending Codex the existing PR #54 continuation: he wants confirmation that the full professional roadmap remains and expressly rejects the current Pro experience, particularly selection and masking, as the finished professional product. This is direct owner feedback and a reaffirmation of the existing required scope. It supplies no new build identity, timed reproduction or numerical completion measurement.

This addendum supplements the current instructions. [GZ_ENGINEERING_ROADMAP](../GZ_ENGINEERING_ROADMAP.md) remains the single active roadmap, [WORK_LEDGER](../WORK_LEDGER.md) orders execution, [CAPABILITY_MATRIX](../CAPABILITY_MATRIX.md) retains all 77 families and required children, and [PRODUCT_SPEC](../PRODUCT_SPEC.md) / [WORKSPACE_UX_BLUEPRINT](../WORKSPACE_UX_BLUEPRINT.md) govern product and interaction behavior. It does not create a second backlog.

## 1. What the current engineering results mean

**Pro is not complete, professionally accepted, or ready for a formal workspace lock.** The intended product still requires substantial implementation and qualification. Fixing a blend control, completing a Save/Open path or passing the current regression suite earns credit for that bounded behavior. It does not establish advanced masking, a complete compositing system, professional usability, or commercial release readiness.

At the reviewed documentation baseline, [main 07578107c15e6df3f08ea30427ea55b9da5ba8e2](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/07578107c15e6df3f08ea30427ea55b9da5ba8e2):

- The [inventory](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/07578107c15e6df3f08ea30427ea55b9da5ba8e2/docs/CAPABILITY_MATRIX.md) contains **77 rows and 77 unique IDs**.
- All **16 Pro families P01–P16 remain unfinished**. P01/P04/P05/P07/P08/P09/P10 are **PARTIAL**; P02/P03/P06/P11/P12/P13/P14/P15/P16 are **OPEN**.
- D04's engineering-verified long-lasso repair is a specific correction. It does not complete P05 selections and masks.
- PARTIAL means a foundation and/or bounded evidence exists while the full listed outcome remains open. OPEN means required work/evidence is undelivered; it is not a claim that no helper code exists.

These are a dated inventory snapshot, not a second status register. Future evidence updates belong in the canonical matrix and ledger. **No percentage of Pro completion is established.** Counting tests, commits, buttons or family rows cannot justify a 10%, 50% or near-finished claim; the families differ greatly in size and remaining work.

The owner's Photoshop/GIMP-style professional target remains. His Snapseed comparison reinforces the need for understandable, useful masking workflows. Neither comparison is a measured feature-parity result. Deliver the full enumerated professional scope with evidence and honest limitations. Do not silently reduce it to basic tools, postpone required pre-release Pro work until after launch, or imply that working controls make it finished.

## 2. Substantial Pro work still required

The following is a concise map to existing requirements, not newly invented scope. Read the full canonical children in [R2](../GZ_ENGINEERING_ROADMAP.md#r2--pro-document-and-compositing-foundation), [R3](../GZ_ENGINEERING_ROADMAP.md#r3--complete-pro-selection-paint-retouch-and-shape-workflows), [R4](../GZ_ENGINEERING_ROADMAP.md#r4--offline-subject--background-and-reshaping) and the matrix.

| Families | Required professional outcome still to complete |
| --- | --- |
| P01–P04 / R2 | Native editable layer/document foundation; groups, isolated/pass-through semantics, clipping, locks, multiselect, group masks and ordering; deterministic memory-bounded tiled compositing; explicit alpha/geometry/effect order; canvas versus image resize and deliberate empty-canvas persistence; preview/export agreement and measured device capacity. Current 12-layer/4096-edge limits stay until evidence supports changing them. |
| P05 / R3 | Compound selections, polygon/freehand/shape tools, contiguous/global color and luminance selection; add/subtract/intersect/invert/grow/shrink/feather; saved selections/channels; linked/unlinked layer/group/filter masks and editable grayscale mask painting. Existing rectangle/ellipse/lasso and a Select / mask label do not complete this scope. |
| P06–P07 / R3 | Precise nonuniform scale, skew/perspective/pivots, numeric entry, guides/rulers/snapping and editable vector paths; cubic curves, booleans, fills/gradients, joins/caps/dashes. |
| P08–P10 / R3 | Clearly targeted paint/mask tools with flow, opacity, hardness, spacing, stabilization and supported stylus/touch behavior; deterministic replay and alpha erasing; fixed/aligned clone and deliberate donor sampling; editable heal/patch/conventional inpaint; ordered effects/styles and meaningful layer/filter/group scope. Current Soft repair is not evidence of a complete heal/patch/inpaint workflow. |
| P11 / R3 | Native document fidelity, then explicitly declared layered OpenRaster/PSD subsets, visible loss reporting, preservation of unsupported records and repeatable document actions. |
| P12–P16 / R4 | Offline conventional subject extraction with editable masks; background removal/replacement and restore; difficult-edge refinement, feather/choke/expand and fringe cleanup; source-safe object removal; reversible mesh/liquify, freeze/thaw, reshaping and material retexture. |

**Mask painting is still required future R3/P05/P08 work.** The [workspace blueprint](../WORKSPACE_UX_BLUEPRINT.md#why-pro-keeps-paint-vectors-and-adjustments) already prohibits presenting U1 as if that workflow were implemented. Keep that distinction visible. Preserve authentic Pro paint, vector and adjustment capabilities while developing their full layer/mask workflows.

Required masks and selections must be accessible through the actual editor, with a clear current target, useful controls and correct output. A renamed panel, a thumbnail, a single synthetic fixture or a helper method is insufficient evidence of a complete workflow.

## 3. What successful masking and Pro editing must let the owner do

Use the existing [professional acceptance chain](../GZ_ENGINEERING_ROADMAP.md#meaning-of-professional--aaa-quality) and [workspace interaction contract](../WORKSPACE_UX_BLUEPRINT.md), rather than creating another approval system. The following outcomes make the already-required R2/R3/R4 children concrete; they are not all assigned to the current scheduling task.

1. **Choose and understand the target.** Select a layer, its content, a transient selection, a saved channel or a layer/group/filter mask deliberately. Display the active target accurately. Invalidation or switching must not redirect a brush or adjustment to another target.
2. **Build and revise the selection or mask.** Combine and correct selections; paint/erase/restore the mask with the implemented controls; feather, expand/choke and refine the edge; preserve the original image and editable mask. Required tools must be findable and work with ordinary real touch input.
3. **Judge the result on useful images.** Isolate a subject and inspect its edges against light, dark and replacement-image backgrounds. Cover hard edges, holes, soft edges, hair/fur and translucent or similar-color cases with manual correction and explicit supported limits. Conventional algorithms and honest difficult-edge limits remain compatible with the no-runtime-AI contract.
4. **Continue a real composition.** Transform the subject or edit a background while respecting linked/unlinked mask geometry and leaving unrelated layers unchanged. Apply the intended local adjustment or retouch using declared scope and donor policy.
5. **Keep the work editable and produce the right output.** Undo/Redo and cancellation preserve the intended state; Save, cold reopen and required Regular↔Pro exchange retain supported native lineage. Independently decoded exported pixels and alpha agree at the qualified output tier.
6. **Make the workflow usable on the declared devices.** Keep common Tools, Options, Undo/Redo and workspace controls in the established positions; retain clear tool-specific targets. Measure responsiveness, memory and long-session behavior on the required workloads, and meet the existing accessibility/device gates. Source safety, fault recovery and the owner's explicit workspace acceptance remain part of completion.

Each future Pro batch must name its existing family/child scope and deliver a bounded, usable editing outcome with the relevant evidence. Preserve prior passed behavior and test the dependencies that the authorized change touches. Do not ask Lance to repeat an entire successful phone script for each batch. Missing owner/device evidence holds only the closure it actually affects.

## 4. The rest of the application remains in scope

The [ordered roadmap](../GZ_ENGINEERING_ROADMAP.md#ordered-delivery-plan) retains all required children. This summary provides orientation and does not replace it.

| Package | Remaining obligation |
| --- | --- |
| R1 | Remaining R1.2 recoverable drafts, bounded history and all-workspace new-process state; complete source/provider custody, RV5 export recovery, native exchange and portable bundles. Subtract the already-qualified R1.1 and minimum U1 custody work accurately. |
| R2–R4 | Complete the professional Pro foundation, full selection/paint/retouch/vector/effects/interchange workflows, and Subject & Background / reshaping scope above. |
| R5 | Professional Regular photography, full numerical processing, RAW, precision/color management, optics/detail, HDR merge, panorama and focus stacking under declared supported tiers. |
| R6 | Local catalog/metadata/virtual copies/sidecars, durable batch, full typography/creative objects and at least 500 finished rights-cleared stickers with usable browsing/editing. |
| R7 | Full GIF timelines/frames/palette/encoded-preview/media/recovery scope, legacy aspect migration and actual-workload performance; complete independent 20-tile Collage workflows, resilience and output. B2/F03 remains open. |
| R8 | Complete pre-Video capability, device, accessibility, performance and usability evidence; four separate explicit owner workspace locks. |
| R9 | The separate professional Video editor after those four locks, including timeline, audio, compositing, color, redaction, mastering and qualified output tiers. |
| R10 | All-five editing qualification and acceptance, then final English/Spanish localization, onboarding/help/legal/access flows. |
| R11 | Finished app and Greenezone website, then Play Console, release rights/signing/AAB/upgrade/store requirements and explicit owner publication approval. |

Regular remains professional photographic development; Pro remains layer/object/mask compositing. Retain the shared layout and command homes, distinct Delete versus Reset, and the symmetric four-choice Regular↔Pro workflow. This feedback does not add a Regular Masks/Histogram/Create collage UI or new Pro Text authoring, and does not remove authentic Pro paint/vector/adjustment tools.

Preserve the offline product, no runtime AI/ML, no ads, subscriptions, IAP or watermark, protected originals, and existing commercial and release requirements. Kotlin remains the app foundation; C++/GPU work is selected only when the existing engineering evidence and roadmap justify it. No engine rewrite is ordered here.

## 5. Attach this to the existing Codex continuation

Read this addendum with current AGENTS, the canonical roadmap, matrix, product/interaction contracts and ledger. Reconcile any actual project-local cached task directions you encounter to those repository instructions; record the affected paths and refs. Do not claim to change unseen files or override higher-priority rules.

**Keep the immediate next action exactly bounded by PR #54's [final paired scheduling check](R11_PAIRED_SCHEDULING_CHECK_2026-11-26.md).** PR #50 remains draft at candidate `27406f4aed8efa42a6078e1108c53304d4a0043e`; the corrected handset APK remains held. This scope reminder neither waives the 650 ms/300 ms limits nor adds another trace execution or a new qualification barrier. Observe the no-app preflight, at-most-one paired workload, finite disposition, supported prospective correction/environment path and established exact candidate/merge qualification.

After that disposition, continue the ledger's remaining R1.2 and dependency-ordered professional work under the existing authority. Do not restart the 25/50 exchange, repeat settled audits, rebuild the shell or rewrite the complete roadmap. Keep progress focused on delivered editing workflows.

Every continuation must distinguish:

- The exact bounded behavior implemented and the evidence for it.
- The outstanding child scope and current acceptance state of affected families.
- The next finite engineering action under the one active ledger.

This addendum changes documentation only. It does not implement features, run application tests, rebuild an APK, assign a completion percentage, erase prior successful repairs, close a family, or grant a workspace/release approval.
