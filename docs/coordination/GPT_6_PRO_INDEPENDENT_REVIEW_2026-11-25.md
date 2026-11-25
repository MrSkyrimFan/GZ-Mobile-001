# Independent review of GZ Editor Pro 0.12.11 Test

## A. Three explicit verdicts

### 1. Is PR #37 suitable for the focused phone run?

**Yes—for the focused diagnostic run described below. It is not yet a clean B1 acceptance or release candidate.**

The delivered APK matches the recorded application checkpoint. I independently checked the four final Android XML reports, the inherited test identities, the APK bytes and manifest, the changed implementation and tests, and relevant CI logs.

I found three interaction issues that warrant a small follow-up: Pro layer commands can change selection while their document edit is rejected; Collage’s header lacks its former system-bar inset; and Collage’s tap handling does not fully implement the agreed pointer contract. These findings do not establish a general crash or data-loss blocker for the six ordinary phone checks. They do prevent an unqualified claim that the shared UI work is finished.

You can use the existing **0.12.11 Test** to collect the remaining phone evidence. Codex should address these findings before treating the corrected UI as accepted.

### 2. What remains defective or unproven?

**Three interaction findings, two durability defects, a specific nested-transaction test gap, and the disclosed GIF performance misses remain.**

The representative full-resolution GIF preparation improved, but still misses the **10-second maximum and 50% improvement targets on both APIs**. The actual reported **120–300-second handset workload remains unresolved**. The small passing GIF fixture does not close it.

### 3. How far is the complete product from professional release?

**Substantial required engineering remains across R1–R7, followed by physical qualification and the gated Video, onboarding and release phases.** This is a repaired and increasingly protected application foundation, not the finished professional product.

All **77 capability families** remain accounted for. The canonical inventory contains four bounded engineering-verified corrections, 31 partial families, 25 open families, one compatibility limitation, 12 gated families, three owner gates and one owner dependency. Those categories are not a completion percentage. Their full reconciliation appears below. [Canonical capability inventory](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3cd1ed578d73610548465c29a27bae8e2cef1480/docs/CAPABILITY_MATRIX.md)

**Review boundary:** I made no repository changes, builds, application executions or test reruns. Three read-only review scopes assisted with Regular/Collage, Pro/storage and GIF; I reconciled their findings with the source and artifact evidence.

---

## B. Prioritized actionable findings

All application references in this section are pinned to **`46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30`**. The review IDs below distinguish these observations from the existing owner findings F01–F06.

### RV1 — P2: Pro layer actions can change selection while Save/export rejects their edit

**Evidence:** `commit()` returns immediately when Pro is busy, exporting or handling a departure. The Behind, Above, Duplicate and Delete handlers can nevertheless change `selected`, call `selectLayer()` or clear drafts after that rejected call. The availability condition is not applied to the entire action. An ordinary asynchronous update of a named save leaves these controls present. [Commit guard, lines 330–343](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt#L330-L343) · [Layer handlers, lines 1555–1588](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt#L1555-L1588)

**Trigger and impact:** Start a delayed Save of a dirty, named, multilayer Pro project, then use one of those layer commands. The document operation is refused, but selection can move anyway. Duplicate on the final layer can select an index beyond the unchanged list. The next edit may target an unexpected layer, or the active-layer controls may disappear.

**Expected:** An unavailable action changes neither content nor associated selection/drafts/history.

**Smallest correction:** Guard each complete action, including callback-time validation, or make the transaction report acceptance and perform its selection changes only when accepted.

**Decisive test:** Hold Save/export behind a controllable latch; attempt all four actions; assert unchanged document, selection, drafts and history. Release the operation and verify normal actions and Undo/Redo once each.

**Disposition:** Source-confirmed; not executed during this review. This is a bounded B1 correction before broad new implementation, not evidence of corrupted saved bytes.

### RV2 — P2: Collage’s header lost its system-bar protection

**Evidence:** `MainActivity` enables edge-to-edge rendering. Collage is rendered directly without an outer safe-area wrapper, and its new top-aligned header column has no status-bar/cutout padding. PR #37 removed the previous Collage `statusBarsPadding()` wrapper; the shared header does not replace it. [MainActivity, lines 15–18](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30/app/src/main/java/com/greenezone/gzphotoeditor/MainActivity.kt#L15-L18) · [Collage header, lines 1038–1047](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30/app/src/main/java/com/greenezone/gzphotoeditor/ui/CollageWorkspaceScreen.kt#L1038-L1047) · [Shared header, lines 60–70](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30/app/src/main/java/com/greenezone/gzphotoeditor/ui/EditorWorkspaceHeader.kt#L60-L70)

**Trigger and impact:** Open empty or populated Collage through the actual activity with visible system bars or a cutout. Its top controls begin in the unsafe area and can overlap system content.

**Expected:** The common header’s actionable bounds remain inside the usable window area.

**Smallest correction:** Restore inset handling at one consistent boundary, without double-padding the other workspaces.

**Decisive test:** Use the actual `MainActivity` → Collage route, visible insets, portrait and landscape/cutout configurations, and a real touch on Options. Retain the narrow-width and large-font checks.

**Evidence gap explaining the miss:** The current layout test mounts the header in a generic `ComponentActivity`; its internal geometry checks do not exercise this edge-to-edge hierarchy. [Layout test, lines 65–84](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidWorkspaceHeaderLayoutTest.kt#L65-L84)

**Disposition:** Confirmed source regression; phone appearance was not reproduced here. Blocks clean header acceptance, not document integrity.

### RV3 — P2: Collage’s tap ownership is incomplete

**Evidence:** The parent canvas uses `detectTapGestures(onTap = …)`, hit-tests the callback’s **release position**, and toggles chrome. It does not explicitly capture the hit at pointer-down or implement the agreed movement/second-pointer rejection. CQ05 requires those distinctions. [Collage handler, lines 975–999](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30/app/src/main/java/com/greenezone/gzphotoeditor/ui/CollageWorkspaceScreen.kt#L975-L999) · [CQ05 contract](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3cd1ed578d73610548465c29a27bae8e2cef1480/docs/coordination/CHATGPT_RESPONSE_TO_50_2026-11-24.md#cq05)

**Trigger and impact:** A valid tap that drifts across a tile boundary can select the release-point tile instead of the down-point tile. In-bounds swipes and multipointer streams also need decisive testing: the caller currently provides no explicit safeguards against treating them as a free tap.

**Confidence:** The down-versus-up contract mismatch is visible in application code. The additional drag/multipointer behavior is source-supported, but I did not execute it against the APK’s Foundation 1.11.0 dependency.

**Smallest correction:** Give the recognizer explicit pointer-down ownership, tap-slop rejection, second-pointer/cancel handling and one completed toggle. Preserve its accessible reveal action.

**Decisive test:** Cover selected/unselected tiles and margins, both chrome states, down-in-A/up-in-B below slop, in-bounds movement beyond slop, two pointers and cancellation. Assert selection, visibility and unchanged document/history.

**Disposition:** Resolve this bounded contract issue before accepting Collage tap behavior. Do not restart the whole gesture audit.

### RV4 — P2: An unreadable Pro save consumes a slot but cannot be deleted through the UI

**Evidence:** Pro lists saved names without decoding their recipes. However, both the Delete UI and `deleteNamed()` require `loadNamed()` to decode the project successfully first. A malformed, unsupported or over-budget recipe therefore remains listed but cannot reach confirmed deletion. It still counts toward the twelve-save limit. [ProStore, lines 302–314 and 381–394](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30/app/src/main/java/com/greenezone/gzphotoeditor/editor/ProStore.kt#L302-L394) · [Delete UI, lines 2037–2046](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt#L2037-L2046)

**Expected:** Record identity and expected stored-byte version should support an explicit, safe deletion even when the editable payload cannot be decoded.

**Smallest correction:** Expose lightweight record identity/status independently of recipe decoding. Keep stale-target protection; do not revert to unchecked deletion by name.

**Decisive test:** Seed unreadable A beside healthy B. Cancel preserves A; confirmed deletion removes only A and frees its slot; stale/recreated A is rejected; failure/recovery cannot resurrect A or damage B.

**Disposition:** Source-confirmed regression, not device-executed. Include it in **R1.1 acceptance**. It does not block a healthy-project phone run. This finding is specifically traced for Pro, not claimed uniformly across all stores.

### RV5 — P2: Failed GIF stage transfer can destroy the only prepared retry output

**Evidence:** Retry writes a new journal request, then transfers the completed stage. `transfer()` ignores `renameTo()`’s result and deletes the previous stage regardless. If the move fails, the good bytes can be lost and the new request must regenerate them. [Retry, lines 199–215](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30/app/src/main/java/com/greenezone/gzphotoeditor/ui/GifExportRecovery.kt#L199-L215) · [Transfer, lines 508–511](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30/app/src/main/java/com/greenezone/gzphotoeditor/ui/GifExportRecovery.kt#L508-L511) · [Journal replacement, lines 102–109](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30/app/src/main/java/com/greenezone/gzphotoeditor/editor/PendingExportJournal.kt#L102-L109)

**Trigger and impact:** Inject a failed stage rename during retry. If source permission is also unavailable, an already encoded export can no longer finish from its prepared bytes.

**Smallest correction:** Coordinate journal replacement and stage custody. Retain a usable reference and the old bytes until the new stage is positively established; checking the rename alone is insufficient if the journal has already moved on.

**Decisive test:** Prepared stage exists, source access is revoked, first transfer fails, recovery remains usable, then a successful retry publishes those bytes without reopening the source.

**Disposition:** Existing code, unchanged by PR #37. This is a bounded **R1 export-recovery correction**, not a newly measured handset regression or ordinary phone-run blocker.

### EG1 — Missing evidence: the complete nested Collage cancellation transaction

The source deliberately stages child and parent work, but the inspected tests do not establish this entire real-screen sequence:

**Dirty child → workspace departure → Apply to tile → parent Cancel**, or **failed parent Save → Cancel**.

The standalone child test verifies a callback payload; it does not mount and resolve the actual parent transaction. Add one integration test covering those two failures and successful parent Save/departure, checking child draft, parent content, route, tile identity, saved baseline and history. **This is a verification gap, not a claim that the transaction is broken.** [Child test, lines 345–365](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidRegularDepartureContractTest.kt#L345-L365) · [Parent transaction implementation](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30/app/src/main/java/com/greenezone/gzphotoeditor/ui/CollageWorkspaceScreen.kt#L443-L575)

### DOC1 — P3: the product specification’s “current result” paragraph is stale

`PRODUCT_SPEC.md` still calls PR #31 / 0.12.10 the current engineering result and describes B1 activation as future work. Correct that paragraph to identify PR #37 while preserving the historical evidence. The completed Stage 3 records may retain their historical wording; they must not reinstate an activation hold. [Stale paragraph, lines 84–92](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3cd1ed578d73610548465c29a27bae8e2cef1480/docs/PRODUCT_SPEC.md#L84-L92)

---

## C. Evidence reconciliation

### Exact source and artifact custody

Live `main` still matched the supplied review-brief commit at the end of inspection.

| Item | Verified identity |
|---|---|
| Current main / PR #39 brief | `ac4e1ee2652db139d8862d91a660abe82450bc57` |
| Canonical documentation checkpoint / PR #38 | `3cd1ed578d73610548465c29a27bae8e2cef1480` |
| Actual PR #37 base | `6f7c574f223e5349a74e622e97433736a07f4a44` |
| Final application candidate | `044e93321292a3678b92394b5efda50aae2e07b9` |
| Qualified application merge | `46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30` |
| Identical candidate/merge tree | `57b3ff2510e059184bfaefee2e781d350bf0930f` |
| Candidate run | [45596352650](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45596352650) |
| Application-merge run | [45600142592](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45600142592) |
| Delivered APK | [Merged API35 artifact 15017086053](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45600142592/artifacts/15017086053) |
| APK manifest | 0.12.11, code 23, `com.greenezone.gzphotoeditor.phonetest.v23`, minSdk 29 / targetSdk 36 |
| APK size | **33,767,477 bytes** |

**APK SHA-256**

```text
969d2c095f856a65f3d6de905e253dfd6e225e479a451edf4b21b2989362f971
```

**Artifact ZIP SHA-256**

```text
a3aee3437252cf3df4bd7091094baa1c5ad7a4124200e6bcc349256003de0065
```

I downloaded and hashed the actual artifact in memory and parsed its manifest and v2 signer certificate. The certificate fingerprint matches the receipt. **I did not rerun `apksigner`; cryptographic signature acceptance remains supported by the existing verification evidence.** PR #38 changed eight Markdown files; PR #39 changed only the review brief. Neither produced a new application. [Verification receipt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3cd1ed578d73610548465c29a27bae8e2cef1480/docs/B1_WORKSPACE_VERIFICATION.md#exact-checkpoint-and-artifact-custody)

### Tests and assertions

The four final XML reports independently show:

| Checkpoint | Android 10 / API29 | Android 15 / API35 |
|---|---:|---:|
| Candidate | 258 passed; 0 failures/errors/skips | 258 passed; 0 failures/errors/skips |
| Application merge | 258 passed; 0 failures/errors/skips | 258 passed; 0 failures/errors/skips |

All four contain the same **258 unique identities**. Comparison with the prior qualified report retains all **193 inherited identities**, with 65 additions. The documented sorted-ID digest matches:

```text
8daa78d5f2580cdf8fc86f2811e34e5d70c403cf846ad6a6e458b3eab669cbed
```

The source review went beyond counting names. The inspected migrations retain the material geometry, source-byte, raster, saved-state, history and GIF fidelity assertions. Removed File/More routes were moved to Options; the Histogram test retains numerical processing checks while asserting absence of the production UI. Failed Save-before-replacement now prevents the picker, a stronger expectation.

The **6-second edited-motion** and **15-second small-export** assertions remain. The larger profile’s 360-second wait is diagnostic, not a new performance acceptance limit. [Assertion migration record](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3cd1ed578d73610548465c29a27bae8e2cef1480/docs/B1_WORKSPACE_VERIFICATION.md#existing-assertion-migration) · [Retained playback and export tests](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidGifWorkspacePlaybackTest.kt)

### Failed CI attempts and heap correction

The receipt retains the intended red reproductions, test-setup/compiler failures, integrated runtime failures and the later observation race. I additionally inspected the raw final XML, relevant verification logs, the intermediate OOM XML/logs and the failed preflight log.

The API29 OOM run really had a **16,777,216-byte growth limit**. Failures included exact-frame arrays, reverse storage, original-size GIF preparation and the inherited Regular save/read fixture. They were not passing results.

The eventual CI correction uses the actual AVD `vm.heapSize` setting, explicit guest maximum/growth properties and a freshly started init-owned zygote. The intermediate two-PID preflight failed before Android tests ran; the corrected selector verifies the proper process. Final measured maxima are **256 MiB on API29** and **192 MiB on API35**. No app `largeHeap`, test exclusion or relaxed fidelity limit was introduced. [CI workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30/.github/workflows/android-ci.yml) · [Guarded test script](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30/.github/scripts/android-test-with-receipts.sh) · [Retained failure history](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3cd1ed578d73610548465c29a27bae8e2cef1480/docs/B1_WORKSPACE_VERIFICATION.md#reproductions-and-diagnosed-failures)

**Assessment:** These are valid environment and diagnostic corrections. They do not establish that the app supports a 16 MiB heap, and the exact cause of the earlier effective guest limit cannot be reconstructed from properties that were never recorded.

### GIF gains and misses

The representative workload is a 1080×1920 source, a 0–2665 ms trim, original dimensions/source timing, neutral forward playback, and 80 output frames.

| Measurement | API29 baseline → merge | API35 baseline → merge | Assessment |
|---|---:|---:|---|
| Tap → destination picker | 20,932 → **14,159 ms** | 26,262 → **14,893 ms** | About 32.4% / 43.3% faster; both targets missed |
| Source-reader wall time | 15,199 → **8,371 ms** | 22,354 → **10,255 ms** | About 44.9% / 54.1%; API29 misses 50% |
| Final start acknowledgement | **83 ms** | **92 ms** | Final samples below 250 ms |
| Final cancel acknowledgement | **49 ms** | **50 ms** | Final samples below 250 ms |
| Worker stop after cancellation | **34 ms** | **17 ms** | Final samples below 1,000 ms |
| Observed canceled result | **138 ms** | **132 ms** | Final samples below 1,000 ms |

The earlier **257 ms start acknowledgement miss remains part of the record**. These are individual observations from separate hosts, not matched cold/warm performance evidence.

The row-read optimization and reader-local exact YUV cache preserve the reviewed conversion semantics. The cache uses 32,768 bytes of array payload, includes matrix/range identity and recomputes collisions. No new retained reverse-frame cache was added. Selected full-ARGB comparisons, complete schedule checks and independent sampled GIF decoding support bounded fidelity. They do not establish every frame of every media type on every device. [Reader implementation](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30/app/src/main/java/com/greenezone/gzphotoeditor/editor/EditedVideoPreviewReader.kt#L202-L378) · [Measured results and limitations](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3cd1ed578d73610548465c29a27bae8e2cef1480/docs/B1_WORKSPACE_VERIFICATION.md)

**Still unavailable:** the owner’s exact original source/device workload and a new handset result. Real process-loss, broader provider/OEM behavior, production same-package upgrades and release acceptance also remain unproven. No required repository source or final artifact was inaccessible during this review.

---

## D. Complete roadmap reconciliation

### All 77 capability families

These are the existing canonical statuses, not new completion claims:

- **EV:** engineering verified for the bounded correction.
- **P:** partial foundation/evidence; full outcome remains.
- **O:** required work or evidence remains open.
- **L:** known compatibility limitation.
- **G:** gated by the specified prerequisite.
- **OG / OD:** owner gate / owner dependency.

The following tables retain every ID and capability. The package descriptions afterward preserve the child scope behind the compact validation column. [Canonical matrix](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3cd1ed578d73610548465c29a27bae8e2cef1480/docs/CAPABILITY_MATRIX.md)

#### Historical corrections and shared foundations — 12 families

| ID | Capability | Status | Required evidence / remaining outcome | Package |
|---|---|---|---|---|
| D01 | Fresh cover then manual crop | EV | Retain anchor, held-pointer, history/reopen/output protection | R0 |
| D02 | Readable bounded named saves and export requests | EV | Retain pre-mutation refusal and healthy/dirty-state preservation | R0 |
| D03 | Complete long Draw and cancel | EV | Retain whole gesture, endpoint, cancellation and output evidence | R0 |
| D04 | Complete long lasso and cancel | EV | Retain full contour, mask pixels, history and reopen | R0 |
| S01 | Original media, independent documents and target identity | P | Complete source/owner protection across history, async results and duplicates | R1 |
| S02 | Reader/writer schema limits and transactional migrations | P | Old/new/malformed/budget cases retain last healthy generation | R1 |
| S03 | Durable draft autosave, snapshots and recovery | O | Acknowledged edits/history/route recover in a new process | R1 |
| S04 | Back, transient surfaces and gesture transactions | P | Resolve RV1–RV3, nested evidence and full navigation ownership | R1 |
| S05 | Provider write ownership and recovery | P | Resolve RV5; external picker/process loss, aliases and uncertain writes | R1 |
| S06 | Explicit saved repeated revision exchange | P | Repeated Regular↔Pro retains native parents, alpha/profile/versions | R1 |
| S07 | Portable native bundle, backup and relink | O | Verified second-device import, source custody and missing-media recovery | R1 |
| S08 | Command ownership and complete actual-control inventory | P | Complete common Options/header/accessibility and unique command ownership | R1 |

#### Pro — 16 families

| ID | Capability | Status | Required evidence / remaining outcome | Package |
|---|---|---|---|---|
| P01 | Image/shape/paint/adjustment layer foundation and legacy text | P | Complete workflows while preserving blends, legacy records and current limits | R2 |
| P02 | Layer tree, groups, clipping, locks and multiselect | O | Reference pixels for nested groups, clipping, opacity and history | R2 |
| P03 | Bounded tiled compositor and device capacity tiers | O | Seam/halo/alpha parity, aggregate memory and measured capacity | R2 |
| P04 | Canvas and object geometry | P | Distinct resize/resample; valid zero-layer save/reopen/export | R2 |
| P05 | Compound selections, channels and refined masks | P | Complete selection algebra, channels and mask ownership | R3 |
| P06 | Perspective, skew, nonuniform transform, guides and snapping | O | Precise transforms agree across view, reopen and output | R3 |
| P07 | Vector shape/path authoring | P | Bézier/boolean/fill/stroke workflows and decoded output | R3 |
| P08 | Professional brush/eraser dynamics | P | Deterministic pressure/flow/spacing/stabilization and touch fallback | R3 |
| P09 | Non-destructive clone/heal/patch/inpaint | P | Frozen donor/sampling, editable operations and output quality | R3 |
| P10 | Ordered effects/styles and blend conditions | P | Correct scope, order, alpha and style interactions | R3 |
| P11 | Declared layered interchange and reusable actions | O | Supported records/loss reports, native fidelity and atomic actions | R3 |
| P12 | Guided non-AI subject selection and editable extraction | O | Correctable extraction with retained original and matte | R4 |
| P13 | Background removal/replacement and mask restore | O | Image/color/gradient replacement, restore and reopened alpha | R4 |
| P14 | Edge refinement, feather and fringe cleanup | O | Difficult-edge corpus and manual correction | R4 |
| P15 | Conventional object removal | O | Editable removal with unchanged pixels outside declared support | R4 |
| P16 | Liquify/mesh warp, reshaping and material retexture | O | Reversible deformation/freeze/retexture through reopen/export | R4 |

#### Regular and selected-tile creativity — 18 families

| ID | Capability | Status | Required evidence / remaining outcome | Package |
|---|---|---|---|---|
| R01 | Photo import, orientation, view, before/after | P | Format/orientation/source fidelity and physical usability | R5 |
| R02 | Tone/color/HSL/curves/grade and Looks | P | Every advertised adjustment matches recipe and output | R5 |
| R03 | Histogram/clipping and numerical inspection | P | Retain correct diagnostic math; no Regular Histogram UI | R5 |
| R04 | Float/16-bit managed image pipeline | O | Reference precision, alpha and profile-domain behavior | R5 |
| R05 | DNG intake and sensor development | O | Declared sensor variants and genuine development evidence | R5 |
| R06 | Broader camera RAW and profiles | O | Qualified camera variants, samples and dependency rights | R5 |
| R07 | ICC/wide gamut, soft proof and high-bit exports | O | Assign/convert/proof and actual tagged high-bit samples | R5 |
| R08 | Optics, perspective and photographic detail | P | Lens/detail quality against numerical and photographic references | R5 |
| R09 | HDR bracket merge, panorama and focus stacking | O | Alignment, correction, source lineage and final output | R5 |
| R10 | Local catalog, collections, metadata and rules | O | Full organization/metadata/search and duplicate verification | R6 |
| R11 | Virtual copies, sidecars, sync and catalog recovery | O | Independent copies, selective sync, backup and relink | R6 |
| R12 | Durable batch processing and export | O | Frozen jobs, per-output receipts and interruption-safe resume | R6 |
| R13 | Text and typography | P | Unicode/font/layout/editability and output fidelity | R6 |
| R14 | Draw, Shapes and decoration | P | Retain Eraser repairs; finish independent objects, frames/borders | R6 |
| R15 | Privacy cover/blur/pixelation | P | Geometry/output protection; truthful concealment claims | R0/R6 |
| R16 | Finished offline sticker library | O | At least 500 finished usable rights-cleared assets | R6 |
| R17 | Sticker browser, transform, custom PNG/WebP | O | Discovery, transforms, durable assets and independent tile output | R6 |
| R18 | Image formats, dimensions and metadata policy | P | Exact format/dimensions/alpha plus precision/privacy extensions | R5/R6 |

#### GIF and Collage — 12 families

| ID | Capability | Status | Required evidence / remaining outcome | Package |
|---|---|---|---|---|
| G01 | Neutral video/GIF import and correct displayed aspect | P | VFR/rotation/non-square pixels/HDR policy and neutral intake | R7 |
| G02 | Edited playback, scrub, reverse, trim, speed | P | Correct still/motion/cadence across media and physical devices | R7 |
| G03 | Palette, disposal, transparency, loops and holds | P | Independent decoder corpus and temporal appearance | R7 |
| G04 | Named project identity/quota and recovery | P | Missing-source relink, true process loss and migration | R7 |
| G05 | Legacy v4 anamorphic project upgrade | L | Reversible inspected copy preserving old/manual framing | R7 |
| G06 | Photo-sequence creation and frame/segment assembly | P | New creation, holds, split/remove/duplicate/reorder | R7 |
| G07 | Timed captions/overlays, ping-pong and encoded preview | O | Exact schedules and preview decoded from staged output | R7 |
| G08 | Export responsiveness and full saved animation fidelity | P | B2 targets, actual F03 workload and sustained device fidelity | R7 |
| C01 | 1–20 independent tiles and duplicate-source identity | P | All twenty marked outputs survive reorder/history/reopen | R7 |
| C02 | Seven layouts and full layout controls | P | Complete layout/count/control geometry and raster coverage | R7 |
| C03 | Selected-tile transactions and Regular features | P | RV2/RV3/EG1 plus full child creativity and ownership | R0/R6/R7 |
| C04 | Project/relink/storage/export resilience | P | Missing-source, interrupted storage/output and physical tiers | R7 |

#### Qualification, Video and release — 19 families

| ID | Capability | Status | Required evidence / remaining outcome | Package |
|---|---|---|---|---|
| Q01 | Full handset/OEM/codec/memory/UX qualification | O | Named physical/resource/accessibility workflows | R8 |
| Q02 | Four formal owner workspace locks | OG | Explicit Regular, Pro, Collage and GIF acceptance | R8 |
| V01 | Single-clip project/player/trim/export foundation | G | Real Video media/timing/project/recovery/output | R9 |
| V02 | Multitrack assembly and precision editorial tools | G | Complete editorial operations with decoded timing proof | R9 |
| V03 | User sound/music, recording and professional mixing | G | Audio processing, sync, mixing, buses/stems and rights | R9 |
| V04 | Compositing, keyframes, transitions and motion | G | Compositing/motion/nested/multicam and photo-plus-audio slideshow | R9 |
| V05 | Color, scopes, Looks and qualified LUT/HDR | G | Declared color processing, conform, tags and pixels | R9 |
| V06 | Moving region effects and privacy redaction | G | Verified affected frames, streams and thumbnails | R9 |
| V07 | Media management, mastering and exchange | G | Media organization, mastering, interchange and archives | R9 |
| V08 | Qualified 1080p/4K/HEVC/HDR device tiers | G | Actual codec/resource/thermal/audio qualification | R9 |
| Q03 | Final complete editing and usability acceptance | OG | All five completed editing workspaces accepted | R10 |
| L01 | English/Spanish localization and contextual help | G | Complete bilingual functionality/layout/accessibility | R10 |
| L02 | Welcome, legal notices and acceptance/access flow | G | Approved offline fresh/upgrade/decline/reaccept behavior | R10 |
| L03 | Greenezone website complete | OD | Owner confirms completion of the separate website | R11 |
| L04 | Play Console setup and verified identity/payment | G | Finished app and website, then actual account requirements | R11 |
| L05 | SBOM, assets/licenses/notices/security and legal review | O | Exact shipped dependency/asset/notice evidence | Continuous/R11 |
| L06 | Production signing, AAB, split install and upgrades | O | Secure signing, actual splits and same-ID migrations | R11 |
| L07 | Store listing/policies/rating/privacy/pricing/testing | G | Current account/policy evidence and truthful listing | R11 |
| L08 | Owner publication approval and maintenance | OG | Exact public-build approval and data-safe maintenance | R11 |

### Required package scope and dependencies

The following is a reconciliation of the existing roadmap, not a replacement backlog. **All canonical child requirements continue to govern even where a table cell uses shorthand.** [Full R0–R11 roadmap](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3cd1ed578d73610548465c29a27bae8e2cef1480/docs/GZ_ENGINEERING_ROADMAP.md)

**R0 and the current correction work.** Preserve the completed crop, bounded-save, Draw, lasso and Eraser evidence. Follow up only on the named review findings and affected dependencies. B1 still needs focused handset confirmation; B2/F03 retain their measured misses and actual-workload gap.

**R1.1 — the next independent package.** Replace eager whole-map Regular project loading with lightweight metadata and selected-only recipe loading. Use per-project records/generations and a verified authoritative index. Migration must copy, validate and publish one legacy entry at a time while preserving identities, versions, names/order, twelve slots, source URIs and healthy bytes. Merely replacing `prefs.all` with `getString` does not prevent the preference map from being materialized. Include corrupt/unavailable records, failed publication, stale saves, index/cleanup interruption and deletion recovery. RV4 needs a bounded Pro metadata/deletion adapter, not a wholesale Pro rewrite.

**R1.2 and remaining R1.** Persist acknowledged completed edits with visible pending/failed persistence, atomic generations and deduplicated history retaining logical Undo depth. Restore actual new-process document/source permission, baselines, history, route, selection, tool, viewport, child and playhead state. Never checkpoint a partial gesture as accepted work or auto-Apply a child. Continue versioned migrations, bounded serialization, source grants/relink, aliases/write-only/partial/uncertain provider results, low-storage behavior, immutable jobs, complete Back ownership, repeated native Regular↔Pro exchanges, portable embedded/link-only bundles, checksum fidelity and genuine same-ID upgrades.

**R2 — Pro foundations.** Build stable layer trees, isolated groups by default, explicit pass-through semantics, group mask/opacity applied once, clipping-base identity, locks, multiselect/alignment and declared alpha/blend domains. Preserve legacy flat rendering. The compositor needs independent reference parity, bounded tiles/halos/intermediates/caches/proxies and preview/final agreement. Keep **12 layers / 4096-edge limits** until aggregate device evidence supports expansion; retain the unresolved 16 MiB read condition. Valid zero-layer projects must preserve size/background/alpha through save, reopen and export.

**R3 — complete Pro tools.** Retain contiguous/global color and luminance selection, boolean selection operations, grow/shrink/invert/feather, saved channels and linked/unlinked layer/group/filter masks. Complete nonuniform scale, skew/perspective, pivots, numeric controls, rulers/snapping/guides, cubic paths, booleans, gradients and joins/caps/dashes. Brush/eraser work includes flow, hardness, pressure/tilt, spacing, stabilization, deterministic finger fallback and texture rights. Retouch includes clone/heal/patch/inpaint/dodge/burn, fixed/aligned sampling and frozen donors; explicit resampling is undoable. Finish ordered blur/sharpen/grain and shadow/stroke/bevel/displacement/conditional-blend scopes. Native fidelity precedes declared OpenRaster/PSD subsets, loss reporting, inert unsupported-record preservation and atomic actions. Preserve legacy Pro text without adding Text authoring.

**R4 — conventional Subject & Background and reshaping.** Complete manual/assisted trace and keep/remove correction, editable alpha for hair/fur/glass/holes, feather/choke/expand and fringe cleanup. Replacement includes image/color/gradient backgrounds and linked subject transforms. Bounded object removal preserves pixels outside declared support. Liquify/mesh, push/pull/expand/shrink, freeze/thaw, face/body-region reshaping and material retexture/reskin remain reversible, source-bound and versioned. Geometry, shading, resource limits and dependency rights need their own evidence; runtime AI remains excluded.

**R5 — professional photographic processing.** Finish tone/WB/HSL/curves/Looks/presets/before-after and numerical clipping diagnostics. Add managed float/16-bit processing while preserving legacy appearance. DNG development needs declared Bayer/linear, compression/opcode, black/white/linearization, CFA/demosaic, WB/calibration/profile and highlight behavior. Broader RAW follows qualified camera samples and licensing; embedded JPEG is only a preview. ICC assignment/conversion, wide gamut, proofing and real tagged high-bit samples remain required. HDR import/edit/display/tone-map/output are separate claims. Finish distortion, lateral CA, vignetting/upright, luminance/chroma denoise and output sharpening, then bracket HDR, panorama and focus stacking with inspectable alignment/correction and source lineage. No Regular Masks authoring or Histogram UI returns.

**R6 — organization, batch and everyday creativity.** Keep distinct source/catalog/copy/revision identities; deliver folders, collections, ratings/tags, EXIF/IPTC/XMP, search/smart collections, byte-confirmed duplicates, stacks and virtual copies. Sidecars, backup/relink and selective sync must reject incompatible source-anchored edits. Qualify the 100-file catalog/recovery exercise. Batch needs frozen recipes, naming/profile/metadata policies and per-output staged/published/uncertain receipts, without duplicate completed outputs on resume. Typography includes Unicode shaping/font custody and independent text/draw/shapes/frames/borders. Deliver **at least 500 finished usable rights-cleared stickers**, visually checked with stable asset manifests, browser/search/favorites/recents, custom PNG/WebP, transforms/history and selected-tile independence. Retired/custom references must remain usable; 648 concepts and trivial duplicates do not meet the asset count.

**R7 — GIF and Collage completion.** Qualify VFR/B-frames/rotation/non-square pixels/HDR policy, transparency/disposal/loops, neutral intake and real edited playback. Measure source-open, first correct still, scrub, first motion, sustained cadence, reverse, Save and provider copy separately. Finish photo-sequence creation, frame/segment split/remove/clone/order/holds, single-endpoint ping-pong, cumulative tick rounding, timed overlays, palette/dither and preview decoded from actual staged GIF bytes. Durable background custody remains later work. Legacy v4 upgrades require source reinspection, before/after preview, explicit adoption and an untouched original/manual framing; missing sources cannot justify guessed migration. Collage retains seven layouts, 1–20 independently edited tiles, repeated-source independence, all layout styling, complete child features, relink-versus-replace semantics, history/recovery, alpha/orientation/memory and interrupted-output proof.

**R8 — physical qualification and four locks.** Complete applicable pre-Video families or obtain an explicit owner scope disposition. Use named physical devices, actual heaps, media and providers for memory/thermal/storage/codec/OEM evidence. Qualify 48 dp controls, 320 dp width, 200% fonts, system insets/IME, TalkBack, keyboard and practical workflows. Proposed resource tiers are test samples, not approved shipping exclusions. Produce four finite dossiers and obtain four explicit owner locks.

**R9 — professional Video, only after those four locks.** Preserve rational source/sequence clocks, half-open intervals, source timestamps and reversible proxy mapping. Scope includes In/Out, append/insert/overwrite, lift/extract, split/join, ripple/roll/slip/slide/J-L, snapping/markers/frame nudging and track targeting/locks/sync. Audio includes user music/voice, waveforms, clip/track/master gain, mute/solo/range mute, fades/pan, channel/sample-rate policy, EQ/dynamics/limiter, conventional noise reduction, automation/ducking and buses/stems. Finish PiP/compositing/keyframes/captions/masks/keying, ramps/reverse/freezes/transitions/stabilization/nested/multicam, and photo-plus-music slideshows with per-photo duration, deterministic pan/zoom and optional beat-sync. Keep Looks/scopes/LUT/log/SDR/HDR, reviewed moving redaction with real re-encoding and all affected streams/thumbnails checked, bins/versions/conform/mastering/ranges/audio-only/subtitles/burn-in/sidecars/interchange/archive/relink and measured codec/resolution tiers. The first H.264/AAC SDR lane is a sequence step, not the finished Video scope.

**R10 — all-five acceptance, then final bilingual onboarding.** Complete and accept all five editing workspaces before final English/Spanish UI/help/accessibility and welcome/language/privacy/terms/license/access flows. Use a shared glossary and versioned local acknowledgements. Decline/cancel preserves projects. Qualify fresh, returning, offline and same-ID-upgrade behavior in both languages. Final terms, reacceptance and access consequences remain owner decisions.

**R11 — release last.** The functioning qualified app **and finished Greenezone website** precede Play Console setup. Then verify actual account-specific identity/testing and current policy requirements; exact shipped SBOM/assets/notices and packaging exclusions; production key custody, Play Signing, AAB/installed splits, release behavior, same-ID upgrades and applicable ABI/native page-size support. Finish truthful price/listing/privacy/Data safety/rating/ads/territories/support/store evidence, explicit publication approval and maintenance/forward-migration/recovery policies. Do not assume a schema downgrade is safe.

The fixed business and product requirements remain **$2.99 once, fully offline, no runtime AI/ML, ads, subscriptions, IAP, watermark, telemetry or cloud-account dependence**. Shipping device/camera/interchange exclusions and specialized additions outside the canonical scope still require explicit owner decisions.

---

## E. Focused phone script and separate engineering plan

### Phone setup

Use **GZ Editor Pro 0.12.11 Test**, leaving older test installations and their projects intact. Choose ordinary photos and export to new filenames. These are the six focused checks; no dense stress fixtures or repeat of the entire historical script is needed.

| Check | Actions | Expected result / what to report |
|---|---|---|
| **1. Common header and Options** | Visit Regular, Pro, Collage and both photo/video GIF routes. Open Options; check Undo/Redo. Use your normal text size, including large text if enabled. In Collage, inspect the top edge and briefly rotate. | Consistent arrows/header; major commands in one Options menu; no Regular More/Histogram/Create collage. Report clipping or unreachable Options. The Collage inset observation specifically checks RV2. |
| **2. Collage tap and child cancellation** | Add two photos. Tap a tile once, then a margin once. Edit one tile, request a workspace change, choose Apply to tile, then Cancel the parent departure. Also try one canvas swipe and a two-finger touch. | Each ordinary tap toggles once; margin preserves selection; drag/two-finger input should not toggle. Parent cancellation preserves child work and parent state. The extra gesture observations target RV3; the nested flow targets EG1. |
| **3. Dirty departures** | In Regular, Pro and Collage, make a small change and try Back/workspace switching. Check Cancel, then Save-before-leave, then Discard using a separate small change. In a tile, distinguish standalone photo Save from Done/Apply. | Cancel retains work; Save succeeds before leaving; Discard affects only the stated branch. Photo Save does not silently Apply to the parent. Record the workspace and choice if anything differs. |
| **4. Named project identity** | Use Pro for this focused check: save A, change and Save again, Save as B, then reopen A and B. Start a conflicting-name save and cancel it. | A updates correctly; B is independent; canceled collision retains the current work. No need to damage a project or manufacture a storage failure. |
| **5. Default-quality GIF timing** | Use the former slow source if still available; otherwise trim an ordinary video to roughly 2–3 seconds with other options at defaults. Time Export → destination picker, then destination confirmation → completion separately. Open the result. | Record both times, movement, crop/orientation and duration. Note whether the command visibly responds promptly. Optional device/source details are useful only if readily available. |
| **6. GIF preparation cancellation** | Start another export and cancel while it is preparing, before destination copying. | Observe acknowledgement, a truthful stopping/canceled sequence, no late picker, and retained editable work. Report visible delays; instrumentation must establish actual worker/resource termination. |

A pass supplies focused handset evidence. It does not grant the four workspace locks, prove process-death recovery or establish production upgrades/release readiness.

**Simple result template**

```text
Build: GZ Editor Pro 0.12.11 Test
Phone / Android, only if readily available:

1. Options/header: PASS / FAIL — details:
2. Collage taps + nested Cancel: PASS / FAIL — details:
3. Dirty departures: PASS / FAIL — workspace and choice:
4. Save A / update A / Save as B / collision Cancel: PASS / FAIL:
5. GIF:
   Export → picker:
   Destination confirmed → completion:
   Output movement/orientation/crop/duration:
6. GIF Cancel:
   Visible response:
   Finished cancellation:
   Edits retained / late picker:

Screenshot or short recording of any failure, if convenient:
```

### Proposed engineering tests — not executed in this review

1. **Bounded B1 follow-up:** Actual-activity inset tests for RV2; controlled busy-state layer actions for RV1; real pointer-stream tests for RV3; actual nested parent/child Cancel, failed Save and successful departure for EG1. Capture a failing reproduction before its corresponding correction; retain any disproving result honestly.

2. **R1.1 baseline before migration:** Twelve independently identifiable near-budget projects, with corrupt/unavailable variants. Measure actual peak heap—including raw preference/XML/map costs—and list/open latency. Pin justified numerical exits before implementation. Steady-state listing should decode zero recipes; selected Open should decode only its selected payload.

3. **R1.1 fault and identity acceptance:** Interrupt copy, validation, payload publication, index publication and cleanup; recover cold. Preserve IDs/order/versions/slots, healthy neighbors, dirty work and originals. Reject stale saves/deletes and prevent deletion resurrection. Include RV4’s unreadable Pro deletion through the real UI.

4. **R1 recovery:** Exercise RV5 with source access unavailable and injected transfer failure. R1.2 later requires a verified PID change and restoration of acknowledged document/history/route/child state—not merely Activity recreation.

5. **B2 measured performance:** Use pinned workloads/options and matched baseline/candidate cold/warm samples on recorded device/heap configurations. Separate queue/decode/render/palette/encode/stage/provider/UI intervals; overlapping counters must not be added. Preserve dimensions, schedule, quality, source hashes, decoded output, memory bounds and cancellation/failure behavior. Keep the existing absolute/relative targets and earlier misses.

6. **Changed-candidate qualification:** Run required JVM, full API29/API35, build/signing and output checks on the final changed candidate and its merge; retain the 258 inherited identities and substantive assertions plus justified additions. Record exact source/run/artifact hashes. Documentation-only changes do not justify rebuilding the app. Broader device/provider/accessibility/upgrade and release checks remain at their named gates.

---

## F. Finite handoff to Codex

The first action is a **small B1 follow-up for RV1–RV3 and EG1**, with the stale status paragraph corrected. **R1.1 remains the next independent package**, including RV4; RV5 belongs to the bounded export-recovery work. Missing owner-specific GIF information must not stop unrelated authorized engineering.

No additional product questionnaire is needed for those actions. Formal workspace locks, material scope/support exclusions, final legal/access decisions, website/account/key decisions and public publication remain owner decisions at their established gates.

Continue GZ Editor Pro in MrSkyrimFan/GZ-Mobile-001 from the completed independent read-only review.

Exact reviewed checkpoints:
- Current main / PR #39 brief: ac4e1ee2652db139d8862d91a660abe82450bc57
- Canonical documentation checkpoint: 3cd1ed578d73610548465c29a27bae8e2cef1480
- Qualified PR #37 application: 46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30
- Final candidate: 044e93321292a3678b92394b5efda50aae2e07b9
- Identical candidate/merge tree: 57b3ff2510e059184bfaefee2e781d350bf0930f

Read AGENTS.md, the current ledger, the complete review brief, PRODUCT_SPEC, roadmap, matrix, release gates and B1 verification receipt. The prior 25/50 alignment is complete and activated. Compare live main once and preserve any newer authorized work.

First bounded action: reproduce and correct these B1 findings, using one writer per shared file and independent review.

RV1 — ProWorkspaceScreen.kt:
commit() at lines 330–343 rejects edits while busy/exporting/departing, but layer handlers at 1555–1588 can still change selection or clear drafts. Guard the whole action or tie selection to accepted transactions. Test Behind/Above/Duplicate/Delete during a controlled pending Save/export; document, selection, drafts and history must remain unchanged until the command is available.

RV2 — CollageWorkspaceScreen.kt:
The header at 1038–1047 lost status-bar/cutout protection in the edge-to-edge MainActivity hierarchy. Restore inset handling once at the correct boundary. Test actual MainActivity → empty/populated Collage, visible system insets, portrait/landscape, physical Options hit targets, narrow width and large fonts.

RV3 — CollageWorkspaceScreen.kt:
The canvas handler at 975–999 hit-tests release position and lacks explicit CQ05 down/slop/second-pointer ownership. Add actual-pointer regressions for down-in-one-tile/up-in-another below slop, movement beyond slop, multiple pointers, cancel and normal tile/margin taps in both chrome states. Correct demonstrated mismatches without changing the agreed single-tap behavior or accessible reveal action. Record evidence if a suspected stream behavior does not reproduce.

EG1 — Add the missing real parent+child integration evidence:
Dirty child → workspace departure → Apply to tile → parent Cancel;
Apply → failed parent Save → Cancel;
and successful Apply → parent Save → departure.
Check exact child draft, parent document, route, stable tile, saved baseline and history. A standalone child callback test is insufficient.

DOC1 — Correct PRODUCT_SPEC.md’s stale “current engineering result” paragraph at lines 84–92. PR #31 remains historical evidence; PR #37 is the qualified 0.12.11 application. Do not revive the old activation hold.

Keep the first follow-up bounded. Capture the relevant red reproduction before each correction; preserve previous repairs and existing test assertions. Qualify one coherent changed candidate and its merge with JVM, full API29/API35, build/signing, independent output checks and exact APK/source/run/hash custody. Do not rebuild merely for documentation.

Completion criteria:
- Rejected Pro actions leave all related state unchanged.
- Collage header controls remain reachable outside unsafe system areas.
- Collage pointer grammar satisfies CQ05 without document/history changes.
- Nested child/parent cancel and failed-save paths preserve both states.
- Existing 258 identities and substantive assertions remain, with justified new cases.
- Failures and limitations remain recorded; no claim that B2/F03 is fixed.

Then continue the canonical next independent package, R1.1, without waiting for unrelated GIF information:
- Measure the twelve-project/list/selected-open baseline and pin numerical heap/latency exits before migration.
- Likely Regular files: ProjectStore.kt, PhotoEditorScreen.kt, EditorDialogs.kt, plus a bounded record/index helper.
- Preserve stable IDs, versions, names/order, quota, URIs, healthy bytes and B1 ownership.
- Use metadata-only steady-state listing and selected-only decoding; migrate legacy storage one entry at a time with verified publication and cold recovery.
- Include RV4: ProStore.kt lines 302–394 and ProWorkspaceScreen.kt lines 2037–2046 make unreadable saves undeletable. Provide record identity/status independently of payload decoding and safely confirm deletion with stale-version protection. Treat this as a bounded Pro adapter, not a wholesale migration.
- R1.2 follows with acknowledged completed-edit drafts and genuine new-process recovery.

Retain RV5 in R1 export recovery:
GifExportRecovery.kt lines 199–215 replaces the journal request before transfer; lines 508–511 ignore rename failure and delete the prior stage. Coordinate stage/journal custody so a failed transfer preserves usable prepared bytes. Test failed transfer with source permission revoked, followed by successful retry without source access. This is preexisting code, not a PR #37 regression.

B2/F03 remain open:
The representative merge takes 14,159/14,893 ms before the picker on API29/API35. Both miss the <=10,000 ms AND >=50% improvement targets. API29 also misses 50% reader reduction. Preserve the earlier 257 ms acknowledgement miss, the unchanged 6-second motion/15-second small-fixture gates, fidelity and quality. Obtain matched measurements before another optimization; the owner’s actual 120–300-second workload still needs relevant phone evidence.

Return a finite receipt for the bounded follow-up: findings reproduced or disproved, exact corrections and tests, candidate/merge/artifact identities, remaining evidence gaps, and the next R1.1 measurement/action. Avoid serial partial APKs and another broad planning cycle.

Preserve all 77 families and every roadmap child. Keep the paid-once/offline/no-AI/no-ads/no-subscription/no-IAP/no-watermark/no-telemetry constraints, original media, dark styling and workspace ownership. Video waits for four explicit owner locks; final bilingual onboarding follows all-five editing acceptance; Play Console follows the finished app AND website; public publication requires explicit owner approval.