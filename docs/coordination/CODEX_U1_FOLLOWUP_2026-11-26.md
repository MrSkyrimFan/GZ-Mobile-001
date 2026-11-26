# Codex continuation — bounded U1 review follow-up

Date: November 26, 2026 (America/Denver).
Status: **Corrections required; ready for the owner's relay to Codex.**
Repository: **MrSkyrimFan/GZ-Mobile-001**. Owner: Lance / Greenezone Software LLC.

## Outcome and authority

Complete U1R-01 and U1R-02 from the [independent U1 review](GPT_6_PRO_U1_REVIEW_2026-11-26.md), dispose of the single U1R-V1 validation item, and deliver one corrected, independently reviewed, engineering-qualified U1 handset candidate. Then remaining R1.2 is the next broader package.

The owner explicitly authorized this review-to-repository-to-Codex workflow. On receiving the relay, proceed under standing branch/PR/review/verified-merge authority; do not request another approval for the same finite work. No new question cycle or full audit is needed. A separate missing phone measurement does not block these code corrections.

The two confirmed defects are medium severity. They do not negate the actual PR #46 qualification, establish lost source/named-project bytes, or justify an application rebuild from scratch. Use the existing native store, tool transitions, rendering semantics, shared header and tests.

## Exact baseline and mandatory intake

- Reviewed application: **e6321322526521ace13a45c231b25bb08f91c565**, [PR #46](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/46), 0.12.14 Test/code26/phonetest.v26.
- Qualified candidate: **95929d3a292101de0bd7ceaa0c831ca9737c67cc**; identical complete tree **e15ba826d15cfb42449461f2528718e9efca5819**.
- Documentation intake: **e6f5a7b8cd499547c014e45124d1c26be7277661**, PR #47, tree **9d9dcb654daa9f23b13ef5e2b64895c3b0be1899**. Its ten-path difference from the application merge is Markdown only.
- Use the exact **new documentation commit in the owner's relay** for this handoff, its paired review and reconciled active instructions. Compare live main with that commit once and preserve any newer authorized work. Branch from current authorized main, not an obsolete app checkpoint.
- Read this handoff and [the review](GPT_6_PRO_U1_REVIEW_2026-11-26.md) in full, then current [AGENTS](../../AGENTS.md), [PRODUCT_SPEC](../PRODUCT_SPEC.md), [WORKSPACE_UX_BLUEPRINT](../WORKSPACE_UX_BLUEPRINT.md), [WORK_LEDGER](../WORK_LEDGER.md), [GZ_ENGINEERING_ROADMAP](../GZ_ENGINEERING_ROADMAP.md), [CAPABILITY_MATRIX](../CAPABILITY_MATRIX.md), [RELEASE_GATE](../RELEASE_GATE.md) and [CONTINUITY](../CONTINUITY.md). Inspect the actual relevant source and tests.
- Reconcile only actual applicable project-local instructions/routing caches in your environment. Record paths and governing refs; preserve original intake. Replace stale immediate R1.2 dispatch with this bounded follow-up, followed by R1.2. Do not claim to change an unseen machine, global instructions or unavailable configuration.
- Keep source and new handoffs in the repository, without SSD/OneDrive mirrors or zip relays. Do not repeat frozen Stage 1–3 appendices or the 25/50 exchange.

## Smallest repair blueprint

### U1R-01: one guarded transition for native editing

**User-visible exit:** after an edited linked return to Pro or a linked named Open, both Layers shortcuts immediately activate their actual native editing target. The selection or retouch instruction shown on screen must be usable.

ProWorkspaceScreen.kt:1745–1750 currently changes section directly. Route these actions through the same guarded transition used by Tools, which also leaves finished-output preview when selecting an editing tool. Keep the exact selected layer, pending-draft cancellation, busy/export/departure restrictions and native/finished geometry distinction.

Required invariants:

- Preserve the native parent and all Regular linked finishes; never flatten or delete the finish to avoid disabled input.
- Selecting a tool is not a content edit and must not create Undo history.
- A completed mask or retouch edit affects one intended native image layer and creates the appropriate single history transaction; Undo/Redo remains correct.
- The linked finished appearance remains available and recomputes from the changed native parent.
- Do not enable all canvas input unconditionally: finished-output preview and protected operations retain their intended guards.

The proposed reuse of selectTool is a narrow implementation recommendation, not a requirement to ignore an actual guard/draft dependency you discover. Record any necessary adjacent file change before editing.

### U1R-02: explicit prepared-handle ownership through every exit

**User-visible exit:** failed, canceled or stale linked project/draft Opens preserve current work and do not accumulate abandoned native state.

The initial targets are PhotoEditorScreen.kt:1286–1310, 1337–1367 and 2388–2399. For each acquisition of a NativeWorkPrepared, identify exactly which caller owns it until successful publication or cleanup. Do the same for any displaced prepared root and prepared photo.

- If photo preparation fails before prepareNamedWorkspace returns, release the exact acquired handle.
- Caller finally blocks release all handles still owned by that operation on publication failure, stale ownership, cancellation and normal completion. Successful acknowledge already drops those staging handles; a later exact-handle release is harmless under the existing store contract.
- Preserve NonCancellable cleanup semantics and rethrow cancellation. Include cancellation during preparation/result delivery: a suspended acquisition can lose its return value on dispatcher cancellation, so caller-only cleanup after assignment may not cover every boundary.
- Keep the existing identity-based protection for concurrent staged work. Never clear all stages, force eviction of dirty drafts, or delete shared named/draft/retry assets to make the count fall.
- Release a lease separately from collecting assets. Existing reference collection and legitimate retained roots determine when bytes may be removed.
- Preserve named ID/version/bytes, the active route/document/history/clean baseline, source bytes, unrelated drafts and independent export retries throughout a rejected Open.

NativeWorkStore may receive a minimal ownership/testability adjustment if required to cover acquisition/result delivery, but this is not permission for a new schema or storage engine. Preserve current 6s/10s time limits, memory limits, manifest/store/draft/geometry bounds and R1.1 reader/list limits.

### U1R-V1: one deterministic inline Save validation

This is a source-supported concern, not an established corruption claim. Before changing this behavior:

1. Begin inline Save current work from an unnamed Pro document toward Regular. Hold the actual named publication at a deterministic test seam.
2. Cancel the switch while the save is held; keep Pro mounted. Release a successful save.
3. Verify exact named identity/version, unchanged route, a correctly acknowledged save target and baseline, and ordinary Save reuse without another name prompt.
4. Add or retain a newer edit in the controlled variant; it must remain dirty. A stale success must not authorize navigation or clean the newer revision.

If this passes, retain the result and close U1R-V1 without production changes. If it fails, synchronize only the exact acknowledged save metadata into the current owning Pro state. Do not key/recreate the whole editor on every session update, erase history/drafts, make Cancel unavailable as a workaround, or relabel a completed save as failed.

### Optional U1R-N1

Collage's contextual Remove may be renamed **Remove photo** to match the already agreed target-specific language. This is optional, has no independent delivery hold, and authorizes no Collage behavior change. Existing tile-removal/Undo coverage is sufficient unless a real dependency changes.

## File ownership before edits

Assign actual writer names in the ledger; these roles are boundaries, not claims of existing assignments.

| Owner | Files / responsibility |
| --- | --- |
| Integration/host writer | PhotoEditorScreen.kt; exact prepared-handle ownership and, only if U1R-V1 reproduces, host save acknowledgement. Own active docs, final branch/merge and artifact identity. |
| Pro writer | ProWorkspaceScreen.kt; Layers shortcuts and the smallest confirmed Pro-side acknowledgement change if needed. ProPhotoshopPanels/ProCanvasSurface stay unchanged unless a concrete dependency requires it. |
| Custody reviewer or assigned storage writer | NativeWorkStore.kt only for a justified acquisition/release boundary or bounded test seam. No parallel host writes. Reuse NativeWorkModel/Files/Codec unchanged unless a demonstrated dependency requires otherwise. |
| Test owner(s) | Extend the existing U1 Pro and Regular departure/native recovery fixtures, or add a narrowly named follow-up Android class. Assign each test file once; use separate test files to avoid concurrent edits. |
| Independent reviewers | Pro actual input and finish preservation; host/resource failure/cancellation ownership; cumulative evidence and untouched capability scope. Reviewers do not replace execution. |
| Optional Collage writer | Only the Remove photo label if elected; record the owner before touching CollageWorkspaceScreen.kt. |

Do not alter the GIF engine, R1.1 migration/indexing, proven Eraser/crop paths, style/layout anchors, build architecture or CI selection to fix unrelated work. Standard test version allocation is needed for the corrected delivery; check the next unused side-by-side identity at that time. No native/C++ dependency is justified by these findings.

## Finite verification plan

### Reproduce the named paths first

Use unchanged reviewed production with test-only additions, targeted to the reported conditions. Record exact test source/head, fixture and results. Keep harness/setup failures distinct from product reproduction. Do not rerun every historic audit.

| Check | Decisive observations |
| --- | --- |
| Linked Pro Layers → Select / mask | Use a real linked image document and pointer tap on the shortcut. Canvas becomes editable; actual drag produces a selection; Apply changes the intended layer mask. A tool chosen through Tools alone does not exercise the failure. |
| Linked Pro Layers → Clone / repair | Same entry state and actual shortcut tap; hold source then destination to commit a real retouch mark. Check target/guide/history and an independent pixel effect with suitable contrasting source content. |
| Native lineage after either edit | Original source hashes and retained finish count/references remain; Undo/Redo restores correct states; named Save/reopen and decoded output still contain the intended operation and linked finish. Reuse existing helpers; no separate broad compositor audit. |
| Repeated failed named Open | A linked named record retains an original URI made unreadable for the test. Drive real Open and error dismissal repeatedly, from a state requiring photo decode. Active work and named bytes stay unchanged; outstanding leases return to baseline after every failure; unreferenced manifests are collectable under the existing policy. Restore access and reopen successfully. |
| Failed/stale/canceled working-draft Open | Exercise acquired active and displaced handles through a rejected publication and cancellation. No stage ownership remains abandoned, no draft is silently evicted, no route changes, and shared named/retry assets remain valid. Include the Regular linked named Open failure exit. |
| Acquisition cancellation | Deterministically cancel around the IO-return/handle transfer boundary as well as after acquisition. Prove cleanup even if the returned value never reaches the caller; do not infer this from an ordinary thrown decode exception. |
| U1R-V1 held inline Save | Exact acknowledged target/clean baseline, Cancel retains Pro, newer edits remain dirty. Publish pass or reproduce/fix outcome; no stronger claim without the observation. |

A bounded test-only inspection seam can count outstanding stage leases. Prefer exact ownership assertions over a flaky total-heap threshold. Optional heap/asset observations may support the impact, but do not invent a numeric acceptance budget after a run. Collection assertions must distinguish unreferenced manifests from assets legitimately held by named/active/draft/export roots.

### Cumulative candidate and exact merge

After targeted fixes pass and independent review resolves findings:

- Preserve **all 303 current Android identities and their meaningful assertions**, including all 279 inherited identities. Add the focused regressions; do not force a predetermined new count.
- Run complete JVM, build/signing and API29/API35 qualification on the final application candidate and exact merged application. Preserve current workflow's independent seed → force-stop → new-PID reopen lane, both named UI Opens and durable native-layer edits.
- Preserve all-mode blend/independent raster assertions, equal-PNG/different-parent replay, repeated mixed edits ending in Save from both modes, R1.1 list/open/migration budgets, GIF fidelity/performance assertions and independent export-retry custody.
- Keep original red/failing receipts and diagnose each failure before a bounded next change. Do not retry an unchanged application failure, relax a limit or remove a difficult assertion to manufacture green.
- Record exact app/candidate/tree, runs, per-API counts/unique inventory, reviewer dispositions and any exclusions. Pin the merged emulator-exercised APK size/hash/package/version and verify its signature.
- Do not rebuild or rerun app suites for documentation-only follow-ups. Do not claim handset or commercial acceptance from these automated checks.

## Completion criteria and return to the owner

The batch is complete when U1R-01 and U1R-02 have red-before/green-after evidence for their real entry/failure paths, U1R-V1 has a precise pass or qualified correction disposition, all inherited gates pass at candidate and merge, and independent review confirms no unresolved regression in the touched scope.

Update the canonical ledger/status pointers in place, retaining this review and PR #46 evidence. Then deliver **one** corrected APK with its exact identity and a short script covering:

1. Linked Regular↔Pro editing, both repaired Layers shortcuts, Undo/Redo, final Save/reopen and editable native layer.
2. Newly changed common controls and disposable Delete/Reset checks only where this follow-up changed behavior or identifies a concrete regression risk.
3. One landscape/large-text look at Options, Tools and Undo/Redo in the changed routes.
4. A concise failure report: device/Android, build, workspace/target, action and result; one recording if needed.

Engineers own injected source failures, lease accounting, controlled cancellation and force-stop tests. Do not ask Lance to manufacture failures or repeat the complete already-passed phone script.

Give Lance a copy-ready return prompt pointing ChatGPT to exact merged application/docs/review-disposition commits, raw runs and the corrected artifact. Request a focused review of these changes and their evidence. After the bounded correction review, **remaining R1.2** resumes within the existing roadmap; a pending independent handset observation holds only its relevant acceptance, not every authorized task.

## Scope and gates preserved

All 77 capability families and full children remain required. U1's minimum Regular/Pro custody is credited; it does not close all-workspace drafts/history/recovery, portable bundles, full S06, R2–R11 or professional feature parity. B2's measured numerical misses, F03's 120–300-second matched phone workload, RV5 stage/retry ownership, same-ID upgrades and broader device qualification remain explicit.

The optional wording nit and U1R-V1 probe do not authorize a new UI overhaul. No runtime AI/ML, ads, subscriptions, IAP, watermark, cloud/accounts or source overwrite. Fully offline, $2.99 once. Four explicit owner locks before Video; all-five editing acceptance before final English/Spanish onboarding; finished app plus Greenezone website before Console; explicit approval before publication.

No work on the Greenezone website or unrelated projects. Follow the original Kotlin plan; consider other processing architectures only when future measured requirements justify them.
