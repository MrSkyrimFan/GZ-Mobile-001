# Regular and Collage Eraser correction — execution evidence

## Authority, scope and current state

On 2026-11-24, Lance supplied the independent PR #29 audit and explicitly requested that this assistant fix the repository. The read-only review has ended; engineering is authorized. This record supports the single [work ledger](WORK_LEDGER.md), not a new roadmap.

Baseline main is `e04bc08a4800233b0f407b419a91c97ca2c068f6`, with application inputs identical to the qualified PR #29 merge `c9efc5aa0673f21b98f150733582a24996b34d10`. The prior 186-case suites and four original repairs remain credited. The 0.12.9 candidate is held for the two additional failures below. Owner handset acceptance and commercial release remain open.

## Findings and observable exits

| Finding | Failure | Required result |
| --- | --- | --- |
| R29-01 | At 160 strokes, first Eraser splitting can produce 161; rendering and serialization drop the final unrelated stroke. Later overflow can retain a partially erased gesture. | All first/later samples obey a bounded transaction. A refused operation restores the entire gesture, dirty document and history. Persistence rejects invalid cardinality rather than saving a prefix. |
| R29-02 | Bounded sampling leaves sparse visible segments, while the old Eraser only tests vertices. | Eraser cuts the rendered segments and the swept path between pointer samples, preserving unaffected sections, properties and limits across save/reopen/output. |

Initial Eraser production scope: `InkMath.kt`, `PhotoEditorScreen.kt`, `EditDocumentCodec.kt`, and the phone-test version. That initial correction does not change GIF or Pro. The later observed GIF performance failure adds the three production files listed below; model/format, layout, workspace ownership, preview routing, dependencies and CI remain unchanged. Capacity refusal reveals the existing controls/notice so its message cannot expire unseen; this is a functional state change, not a layout/style redesign.

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

`AndroidBackRotationTest` still used the legacy unconfined Compose rule. The bounded harness correction changes only that rule import to `androidx.compose.ui.test.junit4.v2.createAndroidComposeRule`. Actual pointer gestures, real orientation/restoration, viewport/tool/history assertions and lifecycle execution remain. No application Pro/lifecycle change or dependency bump is made on this evidence. Android's [legacy API reference](https://developer.android.com/reference/kotlin/androidx/compose/ui/test/junit4/package-summary) and [v2 migration guide](https://developer.android.com/develop/ui/compose/testing/migrate-v2) document the queued StandardTestDispatcher replacement. Final candidate and merged execution must verify the corrected test and the complete regression case set (193 after the GIF-fidelity addition); this diagnosis alone does not establish handset rotation acceptance.

## Original API35 product reproduction

[API35 receipt 14959370204](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45471163485/artifacts/14959370204), ZIP SHA-256 `3ae0d85a5f5a2246aca2ff62bb167dc796e05b6d8339a30f78d283d12ffea06d`, reports 191 cases: 186 passed, 5 failed, zero errors/skips. All five failures are in the new Eraser class; the original 186 cases pass.

- First capacity split: named save lost the final unrelated red stroke.
- Later capacity split: the earlier erase remained instead of rolling back the complete gesture.
- Selected Collage tile at capacity: parent comparison detects the rejected operation changing its document.
- Long Regular line and long selected-tile line: the displayed gap never appears, failing the independently sampled viewport condition.

These are actual unchanged-application Android15 reproductions, supported by three JVM failures. Android10's earlier scheduler abort provides no Eraser reproduction claim. The corrected candidate preserves the five Android case bodies and their assertions, adds 12 geometry JVM cases, and changes only the observed rotation fixture's rule import. Final candidate/merge qualification remains pending.

## Candidate 1 — Eraser passes; GIF speed gate blocks qualification

Candidate `fe2a3054082fbcc5edda054ec0a259e097291781`, tree `2c64493a164a5a29a65a293ed580cb9316051978`, [run 45473953140](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45473953140), passes JVM/assembly/signing. API35 passes 191/191. API29 passes 190/191; its only failure is the unchanged final portrait GIF Save assertion: 28,331 ms exceeds 15,000 ms. Both APIs pass all five Eraser cases and rotation, with no original case removed. The identical 191-case set hashes to `7d851781d5221ef6f79cc29bb262ce8c0bffd24b698c3bd53320980f366df415`.

- [API29 receipt 14960290828](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45473953140/artifacts/14960290828), SHA-256 `cf0a40e2835a10c852360649b1bf452cb12a6c2c0427102784df5bffc1afc03f`.
- [API35 receipt 14960645710](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45473953140/artifacts/14960645710), SHA-256 `374ed7e83bfe654901a682ca90934efd8b1a62f5216eca5b557b5b3b1adb5913`.

The failed timing assertion runs after successful independent output decoding (180×320, 88 frames, every delay, 2.950 seconds, reverse endpoints), reopened playback and completion. Actual staging was 28,144 ms; the source-reader-loop total 27,193 ms includes 2,007 ms measured frame reads. The remaining 25,186 ms cannot be attributed to one stage from old counters. No relevant app GC/codec wait/Compose fault is logged. Matched >1-second cases have approximately 1.08× median slowdown, which does not substantiate blaming runner noise for this interval. This candidate is not qualified and is not being retried unchanged.

### Bounded next correction and new evidence

The B&W preset includes clarity .16, disabling the current ARGB-only export color cache and repeating per-pixel tone processing. Extend that existing per-export cache key to `(sourceARGB, computedDetail.rawBits)`; keep coordinate-dependent vignette/grain excluded. Neighborhood processing, grading math, mask/privacy/creative ordering, resolution, palette, delays and frame count stay intact. A host probe using the production tone/detail math matched 5,068,800 pixels exactly with 99.33% reuse. That supports an exact optimization; it does not retrospectively prove where the old 25-second remainder was spent.

Aggregate wall/thread-CPU stage measurements will distinguish reader open/read/close, rendering, palette, LZW/output and progress. Two small Android bitmap-equivalence cases will compare cached versus uncached output across changing source neighborhoods and grade combinations. The existing texture-cache-null assertion must become full pixel-equivalence coverage because exact texture caching is now intentionally supported; alpha coverage and vignette/grain exclusion stay. The 6-second sampled-motion and 15-second Save gates remain unchanged. Final expected suite: original 186 plus five Eraser and two cache-fidelity cases, 193 total.

### Corrected performance implementation reviewed

The exact detail-aware cache is implemented in `ImagePipeline.kt`; aggregate encoder/render timings are in `GifEncoder.kt`, and separate source-reader open/read/progress/close timings are in `VideoGifSource.kt`. The cache is confined to one export and uses 16,384 entries: 208 KiB of array payload, 64 KiB above the previous cache. Matching requires both the source ARGB and exact detail bits. Masks, privacy and creative compositing remain downstream. The normal Regular render and GIF preview overloads are unchanged.

The actual corrected cache class was extracted verbatim and compiled with the unchanged production tone/detail functions: all 5,068,800 fixture pixel calculations match the direct path, with 5,034,863 hits and 33,937 misses (99.33047% reuse). A host measurement was 336 ms direct versus 38 ms cached; these are host-function results, not Android timing promises. The two actual Android raster-equivalence cases and unchanged Save gate must validate the changed candidate. Independent source review found no blocker and confirmed reader closure, suppressed exceptions, cancellation and bitmap ownership are retained. Thread CPU counters measure the calling worker, not external codec processes.

The intended final 193-case identity SHA-256 is `b190082b3b60bf0b19ef6f66eafb31ca0b258bc9264c4b6b8168ae50f9f29816` (sorted classname#testname, LF separators, no trailing LF). Candidate and merged-main runtime qualification remain pending; no successor APK is qualified in this record yet.
