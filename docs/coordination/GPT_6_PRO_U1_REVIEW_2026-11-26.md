# Independent U1 review — 0.12.14 Test

Date: November 26, 2026 (America/Denver). Repository: MrSkyrimFan/GZ-Mobile-001.
Owner: Lance / Greenezone Software LLC. Review only; no application implementation or new qualification run.

## Verdict and next action

**Corrections required before recommending U1 as the clean focused handset candidate or dispatching remaining R1.2.** Two medium-severity, source-confirmed defects remain: native-edit shortcuts can leave Pro's canvas inert after a linked round trip, and failed linked-project Open paths retain staged native document state.

The PR #46 candidate and application merge genuinely passed their recorded qualification. Those results remain credited. This review identifies paths missing from that coverage; it does not invalidate the functioning blend repair, restart the old audit, or demonstrate security compromise, lost source files or corrupted named saves.

Execute the finite [Codex U1 follow-up](CODEX_U1_FOLLOWUP_2026-11-26.md). The [single ledger](../WORK_LEDGER.md#next-engineering-action--u1-review-follow-up) owns dispatch. Preserve all 77 families and the existing product contract.

## Exact reviewed checkpoint

| Item | Identity |
| --- | --- |
| Application merge / PR | [e6321322526521ace13a45c231b25bb08f91c565](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/e6321322526521ace13a45c231b25bb08f91c565) / [PR #46](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/46) |
| Candidate | 95929d3a292101de0bd7ceaa0c831ca9737c67cc |
| Independently compared complete application trees | Both e15ba826d15cfb42449461f2528718e9efca5819 |
| Documentation intake / live main at review | [e6f5a7b8cd499547c014e45124d1c26be7277661](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/e6f5a7b8cd499547c014e45124d1c26be7277661) / PR #47 |
| Intake documentation tree | 9d9dcb654daa9f23b13ef5e2b64895c3b0be1899 |
| Intake difference from app merge | Ten Markdown paths only; no changed app, tests, build or CI |
| Prior inherited application | b0f0d0e5e2d879ddddedede442ba92ef30b69a78 / PR #43 / 279 cases |
| Reviewed implementation handoff | [Complete PR #47 handoff](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6f5a7b8cd499547c014e45124d1c26be7277661/docs/coordination/GPT_6_PRO_U1_IMPLEMENTATION_HANDOFF_2026-11-25.md) |

Root reviewed the full U1 implementation handoff, current instructions and canonical product/UX/roadmap/matrix/ledger/gates/continuity records; pinned source and PR patches; native storage/codec/publication; raw final receipts and APK bytes. Three independent source-review scopes covered Pro tools/blends, Regular–Pro bridge/save/recovery, and shared commands/Collage/GIF. The tree contains one repository AGENTS.md. The external Codex machine and its routing notes were not inspected or changed by this review.

## U1R-01 — P2: Layers shortcuts open native tools with an inert canvas

**Status:** confirmed by exact source control flow; no new emulator or handset execution claimed.

**Trigger:** start with an image-containing Pro document with at least one linked Regular finish. An edited Pro → Regular → Pro Bring round trip, or opening that linked named project, establishes this state. While its finished appearance is showing, select **Layers → Select / mask** or **Layers → Clone / repair**. Follow the displayed drag/hold instructions.

| Source | Consequence |
| --- | --- |
| [ProWorkspaceScreen.kt:213–217](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt#L213-L217) and [912–915](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt#L912-L915) | A linked document initially shows finished output, including after named Open. |
| [ProWorkspaceScreen.kt:313–326](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt#L313-L326) | The normal tool-selection transition clears finished-output preview for an editing tool. |
| [ProWorkspaceScreen.kt:1743–1750](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt#L1743-L1750) | Both Layers shortcuts bypass that transition and only change the selected section. |
| [ProWorkspaceScreen.kt:1562–1571](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt#L1562-L1571) | Finished-output state disables canvas editing and omits active layer/selection/retouch guides. |
| [ProCanvasSurface.kt:79–84](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProCanvasSurface.kt#L79-L84) | The disabled canvas returns before installing gesture handling. |
| [ProPhotoshopPanels.kt:71–83](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProPhotoshopPanels.kt#L71-L83) and [111–126](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProPhotoshopPanels.kt#L111-L126) | The panels nevertheless instruct the user to drag a selection or hold a clone source/destination. |
| [ProWorkspaceScreen.kt:1695–1717](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt#L1695-L1717) | The visible Edit native layers control belongs to the Layers row, which disappears after these shortcuts change section. |

**User impact:** enabled shortcuts lead to apparently broken tools in the exact mixed-workspace workflow U1 promises. Reopening Tools and selecting the same tool is a workaround, not the intended interaction. No content loss is established.

**Smallest correction:** use the same guarded native-edit transition for these shortcuts as for the ordinary Tools chooser. Preserve the linked finish, native parent, target, geometry and operation guards. Do not remove the finish or flatten the master to make the canvas respond.

**Decisive verification:** establish the real linked state, tap each Layers shortcut through pointer coordinates, then draw/apply a mask or hold source/destination to commit a retouch on the intended image. Check actual native mutation, single history entry, Undo/Redo, retained finish and saved/reopened/output result. A test selecting the tool through Tools misses the defect.

## U1R-02 — P2: Failed Open retains native staging leases

**Status:** confirmed source resource-lifetime defect; accumulated OOM or storage exhaustion has not been measured.

**Primary source:** [PhotoEditorScreen.kt:1286–1310](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/ui/PhotoEditorScreen.kt#L1286-L1310). The helper prepares a native working root, then tries to prepare its Regular photo. Missing source access, allocation failure or cancellation after acquisition can exit before the prepared handle is returned or released.

The related caller paths also need the same finite cleanup correction:

- [Working draft Open:1337–1367](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/ui/PhotoEditorScreen.kt#L1337-L1367): prepared active/displaced handles are not released in failure/cancellation cleanup.
- [Regular linked named Open:2388–2399](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/ui/PhotoEditorScreen.kt#L2388-L2399): cleanup discards the bitmap but omits the native prepared handle when publication fails.
- Pro named Open already releases its returned handle on unsuccessful transfer at [1844–1848](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/ui/PhotoEditorScreen.kt#L1844-L1848); the helper's earlier failure remains shared.

**Why this is retained state rather than merely leftover temporary filenames:**

- [NativeWorkStore.kt:73–107](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/editor/NativeWorkStore.kt#L73-L107) creates fresh root/owner identities for named Open and protects the prepared object.
- [NativeWorkStore.kt:438–442](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/editor/NativeWorkStore.kt#L438-L442) and [509–512](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/editor/NativeWorkStore.kt#L509-L512) hold those objects as strong IdentityHashMap keys for the process. The key retains the whole NativeWorkState and its history graph.
- [NativeWorkStore.kt:423–435](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/editor/NativeWorkStore.kt#L423-L435) protects staged manifests and their reachable records from collection.
- Successful acknowledge correctly removes the exact handles at [138–139](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/editor/NativeWorkStore.kt#L138-L139). This finding does not accuse successful Open of leaking.

**Trigger:** save a linked project whose retained Regular source is still the original content URI, for example Regular → Bring to Pro → named Pro Save. Open from a state that must decode that photo, with the URI now unreadable. Native metadata preparation succeeds; photo preparation fails. Repeat Open after dismissing its error.

**User impact:** each failed named Open can retain another cloned document/history state and protected manifest until process exit. Per-operation delta budgets do not bound accumulated retained graphs. Current work and saved bytes appear preserved; the problem is failure-path memory/storage custody.

**Smallest correction:** make acquisition, transfer and release explicit for every prepared handle in these paths. Use cancellation-safe finally cleanup and the existing exact-handle release operation. Published/named/draft/retry roots retain their assets independently. Do not clear the staging map globally or delete assets merely because one Open failed.

**Decisive verification:** drive repeated failed UI Opens with a deterministic unreadable source, plus a failed/canceled working-draft publication. After each attempt, outstanding leases return to their starting count; unreferenced manifests become collectable under the existing collection policy; current route/work/history and all named/draft/retry references remain intact. Restore source access and successfully reopen. Do not demand immediate deletion if another legitimate root retains an asset.

## U1R-V1 — finite validation item: canceled inline Pro Save acknowledgement

**Priority:** P3 investigation until a controlled execution establishes impact. This is not a third demonstrated data-loss defect.

When an inline switch Save completes after Cancel keeps Pro open, [PhotoEditorScreen.kt:1129–1139](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/ui/PhotoEditorScreen.kt#L1129-L1139) updates the parent session. Pro's same-owner child initializes save target/history/baselines with remembered state at [ProWorkspaceScreen.kt:229–245](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt#L229-L245) and publishes its local snapshot back through [451–465](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt#L451-L465) and [942](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/ui/ProWorkspaceScreen.kt#L942). The child is keyed by owner at [PhotoEditorScreen.kt:1785–1793](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/e6321322526521ace13a45c231b25bb08f91c565/app/src/main/java/com/greenezone/gzphotoeditor/ui/PhotoEditorScreen.kt#L1785-L1793).

This suggests the named record can be successfully written while the still-open Pro session loses its acknowledged target/clean-baseline update. No wrong-document overwrite or content loss was demonstrated.

Run one held-publication reproduction: begin unnamed Pro switch Save, Cancel while publication is held, release successful publication, then use ordinary Save. The exact completed save must be acknowledged without a new naming requirement; Cancel must still keep the route; newer edits must remain dirty. If it passes, record the evidence and close the concern without changing production. If it fails, correct only the revision-bound metadata acknowledgement and preserve newer work. Do not reset all remembered UI state on every session update.

## Nonblocking observations and explicit evidence limits

- **U1R-N1, P3:** Collage's tile action at CollageWorkspaceScreen.kt:1565 says Remove, whereas the blueprint specifies Remove photo. Its actual scope is the selected tile and Undo, distinct from global Delete. A wording-only correction may accompany this batch; it is not a standalone delivery hold.
- AndroidWorkspaceHeaderLayoutTest checks the production shared header at 320dp/200% fonts with synthetic commands. It does not mount every full workspace, the new media/Delete menu combination and the lower Tools row in landscape. New command tests cover the real bars at ordinary metrics. Whole-screen large-text/landscape/TalkBack handset acceptance remains open.
- The force-stop seed is programmatically constructed. The separate UI round-trip tests exercise edited creation and Save from each mode; the process lane exercises new-process recovery, both named UI Opens and durable native edits. These are complementary evidence, not one uninterrupted UI-created journey spanning process loss.
- The all-mode blend evidence is also complementary: all sixteen actual pointer selections; every mode through named persistence and independent decoded PNG; an actual Multiply UI-to-file journey; independent raster equations. Do not describe sixteen separate complete UI-to-export journeys.
- Earlier failed U1 runs and the isolated legacy Compose GIF timeout are preserved in the ledger. This review inspected the final raw candidate/merge archives, not every superseded diagnostic archive. Their recorded harness explanations do not close B2/F03.
- No physical handset execution, same-ID upgrade, broad device acceptance or commercial release certification was performed here.

## Independently inspected raw qualification

Both final runs report successful Verify, API29 and API35 jobs at the exact stated heads. Decoded Verify logs contain completed JVM/assembly and Android v2 signing checks. The separately downloaded final receipt archives passed ZIP integrity and SHA-256 checks:

| Lane | Raw archive | SHA-256 | Ordinary XML | Seed → reopen PID |
| --- | --- | --- | --- | --- |
| Candidate API29 | [15085910573](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45749303161/artifacts/15085910573) | 1ef092f8b5ac5db9b3c78e3a170aa90ed9f73aca71e85bc418e3765c7562883b | 303 / 0 failures / 0 errors / 0 skips | 18384 → 18429 |
| Candidate API35 | [15085334133](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45749303161/artifacts/15085334133) | c30f2df2cb4418cf877b1431b9b76d206672ca412e93d5d6a424c28ac232bdb3 | 303 / 0 / 0 / 0 | 9246 → 9285 |
| Merge API29 | [15086148186](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45751851847/artifacts/15086148186) | 2a0e0364a9223f224dc345535056eeb071e5c7c5dc9d533168f75f7d52eb7119 | 303 / 0 / 0 / 0 | 18848 → 18894 |
| Merge API35 | [15086916614](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45751851847/artifacts/15086916614) | f4b84944144d9fed644be9c53450f101fcd9496451e0459491546820944f936b | 303 / 0 / 0 / 0 | 9242 → 9287 |

Every ordinary suite has 303 unique classname#method IDs. Sorted with LF separators and no trailing LF, all four hash to **cf4ccb11cfc6d38dfd82318cab74e5e9074c0e7dd4740c2d84685b96d75084df**.

The independently downloaded prior [279-case receipt 15073071414](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45720375226/artifacts/15073071414) has archive SHA-256 **3584d40aa5b8a738655695be371dd8e5bb53021924300671bb3f85fff45f4cd9** and identity hash **da266500ad9047c83f6589c0d4b2ded2b6a1eae438881adb5c50e7c378514686**. Set comparison shows **zero missing inherited identities and 24 additions**. Identity retention alone is not assertion preservation; reviewers also inspected affected test migrations.

Each separate seed and reopen transcript reports OK (1 test), outside the ordinary suite count. The runner source explicitly force-stops between them. The raw process logs record both named UI Opens, two durable native edits, retained versions, linked finishes/history, a parked draft and retired-root protection. Cold host recovery is 1598/1912 ms candidate and 897/1452 ms merge; observed managed increases are 8.04/10.96 and 9.51/9.97 MiB.

### Inherited R1.1 budgets remain supported

Parsed raw GzR11Acceptance lines reproduce the ledger's bounded observations:

| Lane | Slowest list ms | Slowest selected Open ms | Warm Open median ms | Migration ms |
| --- | ---: | ---: | ---: | ---: |
| Candidate API29 | 3.072 | 149.784 | 148.775 | 2038.331 |
| Candidate API35 | 2.773 | 219.267 | 201.647 | 2972.273 |
| Merge API29 | 1.855 | 100.067 | 98.582 | 1653.183 |
| Merge API35 | 2.489 | 546.617 | 192.482 | 3004.474 |

All lists decode zero recipes/access zero legacy preference maps; selected Open decodes one. Observed heap increases and these timings remain inside the original limits. These measurements do not exercise U1R-02's repeated failed native Open.

### APK bytes

The actual merged handset ZIP and APK were downloaded and inspected without installing or executing them.

| Property | Independently observed value |
| --- | --- |
| Artifact | [15085954966](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45751851847/artifacts/15085954966), one app-debug.apk |
| ZIP SHA-256 | a669238751a4c4e42fc26e34c41d692ed0d72a2eaf329b91fae61ca010348b4d |
| APK size | 34,374,380 bytes |
| APK SHA-256 | f2ce7fbd2b3c69c107749ba9a419f389a673a1098a8cc22979c9d860b72ac7ee |
| Binary manifest | 0.12.14 / code 26 / com.greenezone.gzphotoeditor.phonetest.v26 / min SDK29 / target SDK36 |
| V2 signer certificate SHA-256 | 19726dc1336f784ed9f03251d4e7b44821f1565d3e01604e8fbb7b965e202f9c |
| Independent v2 verification | Certificate/public-key agreement, RSA PKCS#1 SHA-256 signature over complete signed data, and APK section/chunk content digest verified from bytes |

APK integrity is established. It does not repair the source defects or confer clean U1 handset acceptance. The existing artifact remains evidence; the next recommended combined candidate follows the bounded corrections.

## Retained successful implementation scope

The inspected paths support the repaired blend selection/target guards, layer-oriented Pro controls, separate Delete/Reset, Collage parent/child ownership and both GIF routes, protected provider copy and independent retry custody, symmetric Bring and inline Save-to-blank, repeated native round trips, equal-PNG/different-parent replay prevention and new-process named recovery.

The identical-PNG comparison checks the full derivative reference, including native parent revision. Linked final Save retains native objects and Regular finish recipes from either workspace. These strengths stay; the correction is not a rendering, storage-architecture or UI-shell replacement.

All **77 capability rows** remain intact: D01–D04, S01–S08, P01–P16, R01–R18, G01–G08, C01–C04, Q01–Q03, V01–V08 and L01–L08. R1.2 remains the next broader package after this follow-up, with the already qualified Regular/Pro minimum credited accurately. B2's numerical misses, F03's default-setting 2–3-second phone workload/pre-picker 120–300-second wait, RV5, broad device checks and the complete R1–R11 child scope remain open.

Four explicit owner locks precede Video. All-five editing acceptance precedes final English/Spanish onboarding. The working app and finished Greenezone website precede Play Console; publication requires explicit owner approval. No runtime AI, ads, subscriptions, IAP, watermark or cloud; $2.99 once. No C++/GPU rewrite is part of this correction.

## Primary implementation research

Official references checked November 26, 2026:

- [Android Compose state](https://developer.android.com/develop/ui/compose/state): remembered initialization is retained across recomposition; changed incoming parameters alone do not synchronize separate remembered mutable state. This supports investigating U1R-V1; it does not prove its runtime impact or justify blanket key-based resets.
- [Kotlin NonCancellable](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-non-cancellable/) and [withContext](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/with-context.html): cleanup may need a cancellation-safe context, while dispatcher return has prompt cancellation behavior. Apply this to explicit stage ownership, including cancellation during acquisition/result delivery. Do not detach work with launch(NonCancellable) or swallow cancellation.

Actual GZ source establishes the findings. External documentation informs the smallest safe correction; it does not replace the reproduction and qualification requirements in the follow-up.
