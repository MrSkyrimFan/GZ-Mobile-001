# Regular and Collage Eraser correction — execution evidence

## Authority, scope and current state

On 2026-11-24, Lance supplied the independent PR #29 audit and explicitly requested that this assistant fix the repository. The read-only review has ended; engineering is authorized. This record supports the single [work ledger](WORK_LEDGER.md), not a new roadmap.

Baseline main is `e04bc08a4800233b0f407b419a91c97ca2c068f6`, with application inputs identical to the qualified PR #29 merge `c9efc5aa0673f21b98f150733582a24996b34d10`. The prior 186-case suites and four original repairs remain credited. The 0.12.9 candidate is held for the two additional failures below. Owner handset acceptance and commercial release remain open.

## Findings and observable exits

| Finding | Failure | Required result |
| --- | --- | --- |
| R29-01 | At 160 strokes, first Eraser splitting can produce 161; rendering and serialization drop the final unrelated stroke. Later overflow can retain a partially erased gesture. | All first/later samples obey a bounded transaction. A refused operation restores the entire gesture, dirty document and history. Persistence rejects invalid cardinality rather than saving a prefix. |
| R29-02 | Bounded sampling leaves sparse visible segments, while the old Eraser only tests vertices. | Eraser cuts the rendered segments and the swept path between pointer samples, preserving unaffected sections, properties and limits across save/reopen/output. |

Production scope: `InkMath.kt`, `PhotoEditorScreen.kt`, `EditDocumentCodec.kt`, and the phone-test version. No model/format, layout, workspace ownership, GIF, Pro, dependency or CI change is intended. Capacity refusal reveals the existing controls/notice so its message cannot expire unseen; this is a functional state change, not a layout/style redesign.

## Evidence status

- Host JVM baseline: the unchanged production sampler and Eraser fail both new `InkMathRegressionTest` cases for the intended reasons: sparse segment remained one run, and a 160-stroke split exceeded the accepted limit.
- Test-only GitHub reproduction: commit `9c17cc3d42b390e8cae18f5175d30be66a44fd50`, [run 45471163485](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45471163485). Production was unchanged. The JVM lane ran 115 tests and failed exactly the two Eraser geometry/capacity regressions and the new over-capacity serialization regression. API29 stopped after 20 discovered cases (19 passed, one existing rotation test crashed) before the Eraser class; it is not Eraser reproduction evidence. API35 completed all 191 cases: all 186 existing cases passed and exactly the five new Eraser cases failed at the intended defect assertions. The API29 abort is preserved, not retried unchanged.
- Candidate/merge qualification: pending. No repaired APK is qualified yet.

## Verification contract

The new Android class uses actual Regular controls, injected pointer events, named project dialogs, Undo/Redo, dirty-state Back, complete parent Collage editing, duplicate-source sibling isolation and independently decoded PNG exports. Boundary documents are seeded automatically; no owner is asked to manually create 160 strokes.

Geometry checks cover sparse lines, swept motion, aspect/short-edge size, round-cap thickness, singleton/tangent/no-hit behavior and bounded fragments. Independent review uses a separate geometry membership oracle. Preserve all existing 186 Android cases, 500-point Draw and 450-point Lasso limits, save-size limits, and the established 6-second sampled-motion/15-second GIF Save gates.

Final delivery requires JVM/assembly/signature verification and full API29/API35 on the final candidate and application merge, matching source tree, raw XML case identity and exact emulator-exercised APK manifest/hash/signature. Physical Back, owner-original media, process loss and provider/OEM behavior retain their separate evidence limits. Continue with the selected R1.1 only after this correction is qualified.

## Implemented correction and independent host review

The replacement Eraser clips the visible polyline against the circular/swept brush path in image-short-edge units, including rendered half-width for round caps. Both the first pointer and subsequent samples use the same bounded result. A capacity rejection restores the complete pre-gesture document, clears transient pointer state and leaves existing history and dirty state intact. Serialization rejects an invalid stroke count before producing any recipe fields; valid legacy format/decoding are unchanged.

The two original math regressions plus 12 targeted geometry cases pass against the correction in a host JVM. The codec's two regressions and all 13 existing Creative/Crop JVM controls pass. A separate reviewer compiled the actual production geometry and checked 300 deterministic cases / 173,670 geometric membership assertions over square, portrait and landscape inputs; all passed. This host execution is deliberately distinguished from Android and handset execution.

No existing Android case or assertion is removed. The one observed rotation-test scheduler failure is corrected by changing only its rule import to the established v2 API, as detailed below. The new five-case suite covers first and later overflow, complete rollback, long sparse lines, cancellation, history, dirty Text, named reopen, selected Collage tiles, independent sibling edits and PNG decoding. The intended successor is 0.12.10 Test, code 22; it is not qualified until both candidate and merged-main gates finish.

## Baseline API29 scheduler failure (separate from product regressions)

The [API29 receipt 14956768023](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45471163485/artifacts/14956768023), ZIP SHA-256 `5bbd0bd622a20d51bc88b28fe0ed439fe516d8ec51fb3945a72712154dea2dc2`, contains 20 cases: 19 pass and `rotationRetainsProSelectedToolViewportAndUndo` crashes before the new Eraser cases execute.

The first error at 19:37:57.545 UTC is `CalledFromWrongThreadException` on a background worker during `CompositionImpl.applyChanges`, reached through `TestMonotonicFrameClock`, `ApplyingContinuationInterceptor` and `CoroutineScheduler.Worker`. The main-thread SlotTable disposal exception follows at 19:37:58.018. Independent review confirmed this ordering in the raw per-test logcat.

`AndroidBackRotationTest` still used the legacy unconfined Compose rule. The bounded harness correction changes only that rule import to `androidx.compose.ui.test.junit4.v2.createAndroidComposeRule`. Actual pointer gestures, real orientation/restoration, viewport/tool/history assertions and lifecycle execution remain. No application Pro/lifecycle change or dependency bump is made on this evidence. Android's [legacy API reference](https://developer.android.com/reference/kotlin/androidx/compose/ui/test/junit4/package-summary) and [v2 migration guide](https://developer.android.com/develop/ui/compose/testing/migrate-v2) document the queued StandardTestDispatcher replacement. Final candidate and merged execution must verify the corrected test and the complete 191-case set; this diagnosis alone does not establish handset rotation acceptance.

## Original API35 product reproduction

[API35 receipt 14959370204](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45471163485/artifacts/14959370204), ZIP SHA-256 `3ae0d85a5f5a2246aca2ff62bb167dc796e05b6d8339a30f78d283d12ffea06d`, reports 191 cases: 186 passed, 5 failed, zero errors/skips. All five failures are in the new Eraser class; the original 186 cases pass.

- First capacity split: named save lost the final unrelated red stroke.
- Later capacity split: the earlier erase remained instead of rolling back the complete gesture.
- Selected Collage tile at capacity: parent comparison detects the rejected operation changing its document.
- Long Regular line and long selected-tile line: the displayed gap never appears, failing the independently sampled viewport condition.

These are actual unchanged-application Android15 reproductions, supported by three JVM failures. Android10's earlier scheduler abort provides no Eraser reproduction claim. The corrected candidate preserves the five Android case bodies and their assertions, adds 12 geometry JVM cases, and changes only the observed rotation fixture's rule import. Final candidate/merge qualification remains pending.
