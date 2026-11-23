# GZ Editor Pro engineering evidence

[Return to the handoff](README.md) · [Transcript](01-transcript-intake-and-initial-work.md) · [GPT Pro prompt](GPT_PRO_REVIEW_PROMPT.md)

## Exact review target

| Item | Pinned identity |
|---|---|
| Final app on `main` | [`14df73926c6f7063a488f84e367268168e8e5444`](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/14df73926c6f7063a488f84e367268168e8e5444) |
| Exact tested PR #23 head | [`1825bcbaf6c98f7ceced44e2012f97c2f8128d3f`](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/1825bcbaf6c98f7ceced44e2012f97c2f8128d3f) |
| Identical full Git tree of those two commits | `709616c2560670e0f07617017a25372b33292bc9` |
| Original base through final app changes | [Compare `1b995e8987ae` to `14df73926c6f`](https://github.com/MrSkyrimFan/GZ-Mobile-001/compare/1b995e8987ae96a841bf56dd22910deb784af054...14df73926c6f7063a488f84e367268168e8e5444) |
| Candidate instrumented run | [#242](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45125499836) |
| Final merged instrumented run | [#244](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45126868951) |
| Exact handset ZIP from the candidate | [v0.12.7 Test, artifact 14796246293](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45125499836/artifacts/14796246293) |
| APK identity | `app-debug.apk`, package `com.greenezone.gzphotoeditor.phonetest.v19`, version code 19, API 29 minimum, API 36 target, verified v2 debug signature |
| ZIP SHA-256 | `ca4d508657f656e1b707838f9e71684d508d9fac334b756f16e5d5b39a813287` |
| APK SHA-256 | `618e3182e0e19f191c08b58078feb5e158ad8f894fe85ead0ea1ee56bae9b5e4` |

The ZIP contains one APK, 32,391,213 bytes. The intended APK comes from the candidate API 35 job that exercised it. A separate job can generate a different debug APK. Check an advanced `main` against the pinned application, build and workflow paths before applying this evidence to a newer build. Each side-by-side test package owns its own saved data. [APK audit record](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/23)

## Pull requests and scope

| Pull request | Repository state | Exact final PR head |
|---|---|---|
| [#16 W0: make Android Back state-safe across editor workspaces](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/16) | Merged. `df72426541d0` | [`d2036e19bcca`](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/d2036e19bcca8fd572220acf0736a6ca94297a1c) |
| [#17 GIF P0: add local preview diagnostics and edited playback coverage](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/17) | Closed. Its exact head was integrated as an ancestor of #18. | [`b1ccb2d65ec0`](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/b1ccb2d65ec078442d59463d0c59cefd47f198ec) |
| [#18 Repair edited GIF playback, crop gestures, and Pro workflows](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/18) | Merged. `593a37dcc920` | [`4d90351e024c`](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/4d90351e024cd5457a22811e18728b10fb69ea6d) |
| [#19 Fix the Revision 4 crop, Pro history, and GIF audit findings](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/19) | Merged. `433c4ce5e120` | [`e5dafdfe2c07`](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/e5dafdfe2c07956f69a56eb0e0d1c3445bc04748) |
| [#20 Stream decoded video frames for faster GIF export](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/20) | Merged. `390bd45116b6` | [`e077be43d11b`](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/e077be43d11b63634c6a0a9d74a136a89e06bff7) |
| [#21 Add Regular photo histograms and true live Pro paint](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/21) | Merged. `3a7047170187` | [`34ec0b9459c3`](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/34ec0b9459c3b1f9f4c1859e169cec6983944018) |
| [#22 Docs: GZ Editor Pro field guide for Codex (research, workflows and QA fixtures)](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/22) | Merged. `dc67de64e4f9` | [`7c5d40b09948`](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/7c5d40b099484c338e4d4cf63dca7ba083482ba3) |
| [#23 Eliminate a repeated decode when starting reverse GIF playback](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/23) | Merged. `14df73926c6f` | [`1825bcbaf6c9`](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/1825bcbaf6c98f7ceced44e2012f97c2f8128d3f) |

- **#16–#18.** Read the early W0 and GIF work in order. W0 established Back dispatch, route and draft handling. Early GIF work added diagnostic and reproduction support plus initial product changes, but the detailed user reports were still open at that checkpoint. #18 integrated the inspected W0 and GIF lineage and repaired edited playback, crop and tool boundaries, edited transfer, Pro controls and project behavior. The stacked #17 head is an ancestor of the #18 merge. [Ancestry comparison](https://github.com/MrSkyrimFan/GZ-Mobile-001/compare/b1ccb2d65ec078442d59463d0c59cefd47f198ec...593a37dcc9208516913700776feaa5e8ec812348) · [Original repair candidate](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45108068745)
- **#19.** Post-merge audit repairs cover small crop targets and exact fixed-ratio panorama export, a genuinely cropped and graded Regular-to-Pro transfer, layer ownership after reorder and Undo, missing media, saved-project deletion confirmation, reset history, GIF trim frame budget and decoder compatibility. Final pre- and post-merge suites ran 131 cases per API. [Audit candidate](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45113528565) · [Audit merged verification](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45114713078)
- **#20.** The exporter streams eligible video frames in sequence and uses a bounded reverse cache. The test checks actual UI save, frame duration, decoded pixels and replay after reopening. The rotated 720p60 comparison tests the sequential production path against the per-timestamp reader. [Candidate](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45117813338) · [Merged verification](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45119204940)
- **#21.** Regular’s RGB and luminance histogram follows the displayed preview, Exposure and Before. Pro’s in-progress brush and eraser composite at their real layer depth. Tests sample while the pointer is held, with an occluding layer, cancellation, committed transparency and Undo/Redo. [Photo candidate](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45120533306) · [Post-merge checks that exposed a remaining GIF startup issue](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45121820609)
- **#22.** The reviewed guide contains 19 guide files plus one continuity-link change. The 18 public manifest entries matched, all five binary fixtures retained their reviewed bytes, and the checker accepted positive controls and rejected wrong direction, missing middle frames, frozen counters and altered delays. The guide’s 18-frame clock exercise and the app’s timed 88-frame fixture are separate. Historical personal media stayed in its private delivery. App, test, Gradle and workflow paths matched the preceding photo merge. [Guide on the verified app baseline](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/14df73926c6f7063a488f84e367268168e8e5444/docs/gz-editor-pro-field-guide/00_START_HERE.md) · [Reviewed diff](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/22/files)
- **#23.** Final route repair uses the bitmap codec on the actual paused first reverse frame, reuses bounded prepared samples on Play and keeps the source identity attached to the cache. The Android UI test asserts the pre-Play codec route. Another test verifies prepared pictures and the rotated output survive reader closure. Source and reopened-output tests continue to check reported motion, visible pixels and exact timing. [Initial input-pump and direct reader proof](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45123930781) · [Final candidate](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45125499836) · [Final merged verification](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45126868951)

## Raw Android evidence and performance

Exact candidate #242 and exact merged-main #244 each executed the same **135 unique cases on Android 10 and 135 on Android 15**, with zero failures, errors or skips. Their ordinary verify jobs passed. The PR-event verify jobs provide compilation and unit evidence. In those events the Android emulator jobs are skipped by workflow conditions, so the full push runs below supply the instrumentation evidence.

| Receipt | ZIP SHA-256 |
|---|---|
| [#242 API 29, 14796316382](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45125499836/artifacts/14796316382) | `3fec021df9c8dc7ddb72959c41de39c3251e237f4644edd9d4172b2a2b0b56e5` |
| [#242 API 35, 14796246301](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45125499836/artifacts/14796246301) | `176057cde433d6a04d3d4e33119225a4de0588567cbae8e38fbc1d335387c28e` |
| [#244 API 29, 14796482327](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45126868951/artifacts/14796482327) | `54cfc58519a09bcfd6c66614792768a1b5d74a8d8e95e2df9b5400d9485854b2` |
| [#244 API 35, 14796696168](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45126868951/artifacts/14796696168) | `3d7b2d68f4a13aa544505e507402990fec48dd75ed2a49a474e46be66693600d` |

The stable case-set SHA-256 is `090ea7c391f23023283d984543afc148c299700ca784095f38a7aa52747cfcae`. It hashes the sorted unique `classname#testname` entries joined with LF and no final newline. The ZIPs contain per-case logs, XML and exit status. An unavailable or expired artifact is a review limitation. Use the PR records as a distinct source and report the difference.

| Measurement on the edited 1080 × 1920 source, 180 × 320 rendered GIF, 88 frames, 2.950 s | API 29 | API 35 |
|---|---:|---:|
| #223 original Save observation | 77.091 s | 39.102 s |
| #230 throughput merge Save | 5.128 s | 6.210 s |
| #244 final Save | 4.797 s | 4.927 s |
| #244 first edited image displayed after Play | 272 ms | 304 ms |
| #244 five distinct visible samples after Play | 1.117 s | 1.632 s |
| #244 rotated 720p60 production export | 1.980 s | 2.252 s |
| #244 same-run rotated per-timestamp reference | 23.577 s | 15.318 s |

The #223 measurement included metadata readback. The revised Save observation ends when the UI shows the saved-output action. The rotated comparison checks both production and reference on the same runner while checking decoded color and motion. In the portrait case, Play is timed after the paused picture is visible. Time from initial trim or scrub to the first paused picture, larger GIF sizes, codec variety and physical-device speed remain separate observations. [Original baseline](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45114713078) · [Final full run](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45126868951)

## Earlier negative evidence

- **Early coverage gap.** The W0 and diagnostic milestones did not yet cover source-level GIF and crop defects from the user’s field reports. The original transcript includes the user’s objections and the engineering reassessment. #18 and #19 provide the later repair receipts. [Early red GIF job](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45097323174) · [Revision 4 P0](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/18) · [Audit fixes](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/19)
- **#228 and #229 photo instrumentation.** An unresolved test swipe import prevented instrumentation compilation in #228. The next run caught an invalid test assumption about zero alpha at a partially covered pixel on a 100 px raster. The revised test renders at 512 px and checks exact transparency well inside the erased region and blue outside it. Review the corrected fixture and unchanged product path. [#228](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45118499065) · [#229](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45119141835) · [#231](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45120533306)
- **#234 API 29 actual product failure.** Only four visible samples arrived in 6.081 s. First-frame decoding took 5.171 s and the first image appeared at 5.221 s. The playback test stopped before export. This drove #23. [Exact failure receipt](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45121820609/artifacts/14794321837)
- **#236 app-identical guide merge baseline.** API 29 logged a successful portrait exercise and then the Android system process failed with an OOM in the system update receiver. Only 58 cases were reported. API 35 completed 135 cases with a single real histogram test timeout waiting for the Exposure-adjusted white clipping. The old receipt did not preserve slider value or displayed counts, so its cause remains unclassified. The final candidate and final merged runs passed the real Exposure and Before test on both APIs. [Incomplete API 29 receipt](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45122428539/artifacts/14794313135) · [API 35 timeout receipt](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45122428539/artifacts/14794877499)
- **#240 scope gap.** Its two full lanes were green, but source inspection showed that the paused UI still chose a retriever. Direct reader reuse in that run proved a narrower path. #242 and #244 add proof of the actual paused UI route without lowering the existing six-second motion gate. [#240](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45123930781) · [#242](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45125499836) · [#244](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45126868951)

## Source entry points for independent review

| Inspect | What a useful review checks |
|---|---|
| [VideoGifSource.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/14df73926c6f7063a488f84e367268168e8e5444/app/src/main/java/com/greenezone/gzphotoeditor/editor/VideoGifSource.kt#L373) and [EditedVideoPreviewReader.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/14df73926c6f7063a488f84e367268168e8e5444/app/src/main/java/com/greenezone/gzphotoeditor/editor/EditedVideoPreviewReader.kt#L56) | Sequential/reverse export, source-timestamp identity, bounded cache, fallback, release and color handling. |
| [GifFramePreview.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/14df73926c6f7063a488f84e367268168e8e5444/app/src/main/java/com/greenezone/gzphotoeditor/ui/GifFramePreview.kt#L71) and [AndroidGifWorkspacePlaybackTest.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/14df73926c6f7063a488f84e367268168e8e5444/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidGifWorkspacePlaybackTest.kt#L405) | Real paused route, visible frame motion, timing gate, actual saved-file decoding and reopened playback. |
| [AndroidRegularToolsTest.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/14df73926c6f7063a488f84e367268168e8e5444/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidRegularToolsTest.kt#L152) and [ProCanvasInteractionTest.kt](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/14df73926c6f7063a488f84e367268168e8e5444/app/src/androidTest/java/com/greenezone/gzphotoeditor/ProCanvasInteractionTest.kt#L93) | Exact histogram bins, a real Exposure gesture, Before, visible paint under an upper layer, erase alpha, cancel and one undoable commit. |
| [Revision 4 audit files](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/19/files) and [release gates](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/14df73926c6f7063a488f84e367268168e8e5444/docs/RELEASE_GATE.md) | Crop hit targets and export, edited-image transfer, layer/history and project behavior, workspace separation, Collage regressions and older projects. |

## Open acceptance

Owner handset testing remains pending for the original affected clip, physical Back gestures, realistic media providers and wider workload performance. The GIF’s saved-output replay is verified. Cold reopen and relink of a named editable GIF project are still distinct work. Regular’s professional RAW/color and optics work and Pro’s groups, Subject & Background, and fuller normal-UI layered project reopen and PNG exercise remain open. Stickers are deferred. The separate Video workspace remains gated by the owner’s pre-Video acceptance. [Product requirements](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/14df73926c6f7063a488f84e367268168e8e5444/docs/PRODUCT_SPEC.md) · [Release gates](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/14df73926c6f7063a488f84e367268168e8e5444/docs/RELEASE_GATE.md)
