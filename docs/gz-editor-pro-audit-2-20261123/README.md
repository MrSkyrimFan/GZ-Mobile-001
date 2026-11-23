# GZ Editor Pro · Independent engineering audit 2

**Decision for the audited build: CHANGES REQUIRED.** The most immediate risks are losing the wrong named project and exporting a photo that exposes content the user already covered. A second Pro save issue can accept a dense recipe that its own reader refuses. These can be investigated with two disposable projects and a synthetic marker image before asking the owner for another phone session.

The owner asked for an audit and a Codex handoff. The app, repository, PRs and CI were read-only during this work. This package contains research, example tests, original synthetic inputs, past Android evidence and offline checks. The candidate APK identifies the baseline and is linked from GitHub; there is no replacement build in this bundle.

## Start with the decision you need to make

| Question | Open |
|---|---|
| Which issues go first, and what observation would close each one? | [Fix order and acceptance matrix](FIX_ORDER.md) |
| What should I paste into Codex? | [Copy-ready prompt](START_CODEX.txt) |
| How can a Pro save overwrite a different project or create unreadable contents? Why do paint and geometry disagree? | [Pro projects and drawing](chapters/10_PRO.md) |
| What actually improved in GIF Maker? Which save, undo, geometry and timing cases are still at risk? | [GIF Maker](chapters/20_GIF.md) |
| Can an export expose a covered subject? What happens to crops, captions, blur gestures and 20-photo collages? | [Regular and Collage](chapters/30_REGULAR_COLLAGE.md) |
| What can be lost when the picker is open, and which dimensions leave the export writer? | [Android, document providers and output](chapters/40_ANDROID_EXPORT_AND_RECOVERY.md) |
| What did PRs #16–#23, the red runs and the signed APK actually prove? | [Verification history](chapters/50_VERIFICATION_HISTORY.md) |
| How can I reproduce the independent checks on my computer? | [Offline lab](LAB.md) |

## What changed since the first review

The first review identified Pro's stale save name and 500-point stroke limit, a real and unexplained histogram timeout, and an overstatement about the time to the first displayed reverse GIF image. It verified the APK, the final four emulator lanes, stronger crop/handoff/project tests, faster GIF export, and the field-guide controls. It kept the original clip, physical Back, process recovery and broader workloads open. Those findings and their evidence are included here.

This pass followed neighboring actions. For example, a successful write can still target the wrong file, and a successful encode can still yield data the reader rejects. A privacy bar can look correct in the frame where it was placed, then miss the protected material after a flip. A Collage reorder can look correct until Undo makes the *next* edit apply to another tile. The linked chapters give exact paths, counterexamples, repair options and independent acceptance evidence.

## Evidence boundaries

| Kind | Meaning in this bundle |
|---|---|
| Raw Android observation | XML, exit status and per-case logs from an identified earlier Actions emulator run. Four unmodified final archives are included. |
| Pinned source finding | A traced state transition, numeric limit or render/write path in the tested app. Reproduction on a real Android installation is explicitly identified when available. |
| Host control | Independently executed code using synthetic inputs, reference output or numerical boundaries. It tests the proposed detector or the stated arithmetic. The CI shell control executes the actual baseline commands on temporary files. |
| Conditional finding | Requires a specific input, provider behavior or lifecycle event. Each chapter identifies the condition and an experiment to settle it. |
| Proposed Android test | Work for a repair branch. The example Kotlin file was reviewed against source signatures and was not compiled or run in this audit. |
| Owner observation | Original media/device experience. The owner's clip and physical device were unavailable here. No private photo or original clip was bundled. |

Read **pinned source** when locating a defect, then compare the current branch before editing. At this audit, the tested baseline and current main had identical app/test/build/CI inputs. Each package has a distinct app data area. A named save created in v0.12.7 belongs to that QA install. [Application merge](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/14df73926c6f7063a488f84e367268168e8e5444) · [Audited main comparison](https://github.com/MrSkyrimFan/GZ-Mobile-001/compare/14df73926c6f7063a488f84e367268168e8e5444...9d55483820ec1dcc39ed8291d7cec84613fcece5) · [Exact APK ZIP, artifact 14796246293](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45125499836/artifacts/14796246293)
