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
- Test-only GitHub reproduction: pending execution. It adds the same assertions before production correction. Preserve actual Android failures separately from any fixture/scheduler issue.
- Candidate/merge qualification: pending. No repaired APK is qualified yet.

## Verification contract

The new Android class uses actual Regular controls, injected pointer events, named project dialogs, Undo/Redo, dirty-state Back, complete parent Collage editing, duplicate-source sibling isolation and independently decoded PNG exports. Boundary documents are seeded automatically; no owner is asked to manually create 160 strokes.

Geometry checks cover sparse lines, swept motion, aspect/short-edge size, round-cap thickness, singleton/tangent/no-hit behavior and bounded fragments. Independent review uses a separate geometry membership oracle. Preserve all existing 186 Android cases, 500-point Draw and 450-point Lasso limits, save-size limits, and the established 6-second sampled-motion/15-second GIF Save gates.

Final delivery requires JVM/assembly/signature verification and full API29/API35 on the final candidate and application merge, matching source tree, raw XML case identity and exact emulator-exercised APK manifest/hash/signature. Physical Back, owner-original media, process loss and provider/OEM behavior retain their separate evidence limits. Continue with the selected R1.1 only after this correction is qualified.
