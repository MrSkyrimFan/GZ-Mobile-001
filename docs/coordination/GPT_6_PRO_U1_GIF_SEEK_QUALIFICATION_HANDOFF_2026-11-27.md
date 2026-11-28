# GZ Editor Pro GIF paused seek qualification and review handoff

## Result

The GIF preview now continues to the newest queued seek when an older paused read fails. The candidate and exact application merge passed the complete canonical qualification. The v27 APK below is the focused U1 handset build. Physical handset observations and product acceptance remain open. [GIF repair PR #60](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/60) · [Final qualification PR #62](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/62)

- Reviewed documentation baseline `ac140e45e78ccec557b970ac3e0cde77cfc05a72` and exact prior application merge `259900f8663ef967e24054db2beab7d0d5b62136`. The independent GPT-6 Pro review accepted the Regular parser and asked for app-side events covering actual Pause, seek delivery, decoding and display. [Prior review package](GPT_6_PRO_R11_SELECTED_OPEN_REVIEW_HANDOFF_2026-11-27.md)
- Final candidate `dda4e5617b23cfcbce11c9472551349404251fed` and exact merge `4114238397a2bb8c27a302082b68aec35eb0bdae` share tree `6f09276cd155c9e65c38706d4a8ef87bdb3d9c63`. [Canonical candidate run](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46112340733) · [Exact merge run](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46114849773)
- The product change is limited to `GifFramePreview.kt` and `GifPausedFrameRequests.kt`, with a two-case permanent JVM regression. The original GIF Android test, all 313 Android identities, recovery assertions and CI controllers are preserved. Additional final test inputs are the scoped Compose rule migrations, the existing-deadline wait for the visible GIF route after Pro Save and the exact recovered host Back dismissal step. [Final source diff](https://github.com/MrSkyrimFan/GZ-Mobile-001/compare/ac140e45e78ccec557b970ac3e0cde77cfc05a72...dda4e5617b23cfcbce11c9472551349404251fed)

## Controlled cause and correction

The original exact PR #50 merged API35 timeout remains a preserved result. Native MP4 playback passed frame 5 and paused. The previous test evidence did not record actual seek acceptance, requested generation or visual assignment. The historical run cannot establish which failure mechanism occurred. [Original failed receipt](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46096683742/artifacts/15244311620)

The new API35 diagnostic uses the real two-second MP4, native Pause and accessible scrub action. It records actual handler state, request binding, generation, native disposal, consumption, read and guarded assignment or rejection with a single monotonic clock. A coroutine gate holds an older nonfinal paused request while the real slider accepts and binds the final request. The three trials use the standard frame-16 assertion and 15-second wait. The diagnostic scratch code records into the existing bounded process-local buffer and exports only numeric stages and failure types, with no media contents, filenames or network behavior.

- Baseline `4b87bd8e56cc72edb568ebd103f7b84cbada18f9` passed the stock case and the held older-success case. With injected `IOException`, the actual final scrub returned true, serial 3 bound frame 15 zero-based, and older serial 2 threw. The entire collector ended. The final request was never consumed or assigned. The slider remained 15 and the displayed still was absent. A synthetic fixture cleanup exception then obscured the JUnit primary error. The app-side trace was captured first. ZIP SHA-256 `476164898bf33a20dffcd2fbcb8ee10b6f3be6bd9755963ff4d2505ba5896454`. [Baseline diagnostic](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46103361109/artifacts/15245499882)
- Corrected `8929f8a6c8898864f848b42adcd63f8eb20833e4` passed all three Android cases and both focused JVM tests. With the same injection, the old read failed first and the new request was then consumed, read, assigned and visible as frame 16. On older success the old result was rejected. This is a causal test of the reproduced collector defect. ZIP SHA-256 `6909e303d35ffedc096fcb73250c1b7b94e879f2256d2a59a14169816e707e8b`. [Corrected diagnostic](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46104196442/artifacts/15246629316)
- The first JVM propagation check used object identity across a coroutine wrapper. Raw XML showed two distinct instances with the same expected cancellation type and message. The corrected permanent test checks exact runtime type and message or cause identity, with zero reader-reset, error and presentation callbacks. The older-read conflation regression was identical. [First JVM receipt](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46103912458/artifacts/15245734359)

The implementation keeps the existing paused flow conflation and routes an ordinary decode failure to the individual request. It clears and closes that reader under the existing ownership lock, contains ordinary cleanup failure and keeps later requests collectible. It tests the current frame and generation on Main before showing a failure. Cancellation and out-of-memory behavior continue through the existing outer handling. The existing dismissible notice can remain visible if an error was published while genuinely current and a later seek succeeds. No diagnostic hooks are in the candidate.

## Export recovery test scheduling correction

PR #60's final candidate passed both API suites. Its exact merge `d5394b8d28ce08872d58a69559e2afbbd94000b9` then stopped on API35 with 58 passes and one failure among 59 recorded cases. The affected test was `AndroidExportRecoveryTest#delayedDestinationWritesTheMirroredPhotoAfterActivityRecreationOnlyOnce`. The provider destination stream opened. At 04:16:34.549 UTC the legacy Compose test clock applied UI changes on `DefaultDispatcher-worker-2` and threw `CalledFromWrongThreadException`. The main-thread reentrant layout exception followed 5 ms later. The exported bytes and the later assertions were still unobserved in that attempt. The separate force-stop stages passed in 5279 → 5317 and proved both named Opens and durable native edits. The exact PR #60 API29 lane retained 313/313 and its independent 18811 → 18857 process proof. [API29 receipt](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46106207990/artifacts/15248635339) [Exact PR #60 merge failure](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46106207990/artifacts/15248115944)

PR #61's initial candidate changed only the affected test class import to the v2 `createAndroidComposeRule`. The queued scheduler matches the existing Pro export recovery and GIF departure recreation fixtures. The class retains its activity recreation, picker restore, duplicate/cancel behavior, PNG pixels and dimensions, journal, model and workspace assertions. The API35 targeted verification completed all three class methods. [Scoped test evidence](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46107093104/artifacts/15247978861) · [Android Compose testing guidance](https://developer.android.com/develop/ui/compose/testing/migrate-v2)

## Pro Save and GIF route acknowledgement

The first PR #61 candidate `293e6916fe9bc43b76231c04fe559f519ed74c53` passed API35 at 313/313 and the separate process stages on both APIs. API29 passed 312/313. `AndroidBackNavigationTest#loadedRegularProGifBackRestoresProThenLoadedRegular` failed while asserting that the visible GIF header had appeared. The test had already observed the named Pro save. The app publishes that row on IO, then acknowledges the result and commits the destination on Main. The final test waits for both the named save and the visible GIF header within the same existing 15-second deadline. Its visible header, actual Back, Pro viewport, layer and Undo assertions remain. [Prior API29 failure](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46107094336/artifacts/15248169728) · [Prior API35 pass](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46107094336/artifacts/15249011242)

The targeted API29 run passed the exact navigation test on its verified 256 MiB guest. Its log reported namedSaveObservedBeforeDestination=false, so that run did not reproduce the prior timing gap. [Targeted API29 route receipt](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46109158352/artifacts/15248931798)

## Exact PR #61 merge evidence and further harness corrections

PR #61 candidate `ebcf5fa26456bee45578c3308bbf75b4467a5c2b` passed both whole suites. The exact merge `40de7e508a46943eb35f3854c6d344f7f303c6cf` failed API35 after 87 recorded ordinary cases. The secondary SlotTable disposal exception followed a wrong-thread Compose test recomposition at 05:24:44.243 UTC in `AndroidGifProjectIdentityTest#deletingNamedBackupOnlyProjectReclaimsCapacityWithoutLosingCurrentEdits`. This fixture uses a photo recipe, with no paused video request. The API35 ordinary suite stopped before the GIF final-frame and selected-Open cases. [Exact raw merged receipt](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46111172396/artifacts/15250115941)

The separate seed passed, confirmed a new PID 5815 → 5852, and restored exact Regular state and its initial Pro return. The resumed test then hit its existing 10-second timeout after the raw Back key failed to dismiss the retained export dialog. Later named Opens and native edits were not reached. The raw receipt does not identify a device-key delivery cause. API29 on that same initial merge passed 313/313 and separate PID 18500 → 18545. [API29 scoped credit](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46111172396/artifacts/15250931077) PR #62 invokes the Back dispatcher on the exact recovered MainActivity, keeps the original dismissal deadline and verifies the identical export record afterwards. This is the host Back path. The actual shell force-stop, distinct new PID, both named Opens, durable native edits, named versions and all original proof remain required. [Focused process evidence](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46112220053/artifacts/15250092216)

PR #62 also replaces the legacy Compose rule import in the GIF project identity class with the queued v2 rule. Test identities, fixtures, waits, edits and assertions remain. [Focused GIF identity evidence](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46112304338/artifacts/15250810996)

## Independent verification

The source reviewer inspected the paused request isolation, cleanup and cancellation and separately confirmed that the final test refinements preserve the original deadlines and custody assertions. The evidence verifier cross-checked exact Actions jobs, artifact uploads, archive integrity, full test identities and the separate reports of new-process recovery. The rolling auditor reviewed each qualification decision independently of the implementation and guarded the exact candidate, merge and handset identity boundaries. The GitHub source and raw receipts documented here are the review inputs.

## Exact unprofiled qualification

Both final canonical workflows passed the complete JVM suite, debug builds/signing and real Android tests. The final candidate’s first push attempt was canceled when a delayed older push for the same feature ref entered the workflow concurrency group. The exact candidate branch did not move. The canceled Android jobs were recovered on the same SHA in attempt 2. Its fresh Android receipts and the successful Verify result provide the candidate qualification. Ordinary test inventory is 313 exact IDs on each API at both inputs, all positive with zero failures, errors or skips. Sorted LF-joined `classname#method` SHA-256 `b3e75ca493b1e5b8bcc2b14f5e9ea70c7ad21edb0a868db220407666c536f377`. The separate external force-stop stages both pass in new PIDs, including Regular and Pro named Open, two durable native edits and retained named versions. The Regular acceptance limits, fixture, list counts, original assertions and actual guest managed heap sizes remain in force.

| Input | Ordinary suite | Seed → reopen PID | Four Regular Opens, ms | Regular warm median, ms | Max selected managed growth, MiB | Raw receipt |
| --- | ---: | --- | --- | ---: | ---: | --- |
| Candidate API29 | 313/313 | 18809 → 18854 | 110.120 / 137.294 / 126.243 / 117.599 | 126.243 | 17.46 | [15250919636](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46112340733/artifacts/15250919636) |
| Candidate API35 | 313/313 | 9212 → 9253 | 85.106 / 78.685 / 78.837 / 79.140 | 78.837 | 15.50 | [15251310207](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46112340733/artifacts/15251310207) |
| Exact merge API29 | 313/313 | 18860 → 18904 | 44.740 / 44.451 / 44.020 / 36.833 | 44.020 | 18.00 | [15252130467](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46114849773/artifacts/15252130467) |
| Exact merge API35 | 313/313 | 9434 → 9476 | 88.691 / 98.218 / 97.347 / 90.274 | 97.347 | 14.43 | [15251428707](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46114849773/artifacts/15251428707) |

| Raw receipt origin | Artifact | ZIP SHA-256 |
| --- | ---: | --- |
| Candidate API29 | 15250919636 | `c8fec25a5ca9b45afe8ff3f91a313219eff3e167c77ba33388febfc8f1d1da71` |
| Candidate API35 | 15251310207 | `d1cafba267f249da02dcee67b5c9b444bca7b03d7a1097b4d35569255c78f0a5` |
| Exact merge API29 | 15252130467 | `80fcd8ae491174e5de8c91f0fda4d17100fad82606608bbbffd38adae3bd33c5` |
| Exact merge API35 | 15251428707 | `37bcff9c4db2ba7b91d19874a639977298edf93622e4033f6e83358c44f538b3` |

## Exact handset build

[Download GZ Editor Pro 0.12.15 Test from the merged API35 run](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/46114849773/artifacts/15252211361). The ZIP contains one APK. The manifest and Android v2 signature and covered-content digest were verified from the exercised APK bytes. [APK v2 verification format](https://source.android.com/docs/security/features/apksigning/v2)

| Property | Value |
| --- | --- |
| Display name | **GZ Editor Pro 0.12.15 Test** |
| Package | `com.greenezone.gzphotoeditor.phonetest.v27` |
| Version / code | 0.12.15 / 27 |
| Minimum / target SDK | 29 / 36 |
| APK size | 34,423,532 bytes |
| Archive SHA-256 | `c9f7f3d00431859986284f28a51aca9eb3834b5ce1e371d547a08015a71a7a38` |
| APK SHA-256 | `45232cb9b32b60a4083a3558e07cbc238357bd5881bb8caaa86a98bf0cc72957` |
| V2 signer certificate SHA-256 | `5d4eea69ee1fbf8338577e2204ad43d682be44b1fd32b200b66239aeb220b710` |
| Signed content SHA-256 | `d5154166d8020dd70439b0587a968f0d5ac6e0cffd7d3dd41f13637a6beff5a5` |

For focused handset confirmation, open a short MP4 in GIF Video, start playback, pause around the middle, then scrub to the end and back. Confirm the displayed still follows the slider and Play remains available. Report any missed frame or error with device and Android version. Keep earlier handset passes credited. Existing original or installed project data stays protected.

## Continuation and scope

Continue the first remaining R1.2 slice with an ordinary unlinked Regular photo import, completed edit, first durable acknowledged working generation and force-stop/new-PID restore of source, entry/clean baselines, history and route. Preserve exact source custody, intentional departure and canceled replacement, child and linked boundaries and independent export retry. Use the single ledger for the next finite slice. [R1.2 custody contract](CHATGPT_RESPONSE_TO_50_2026-11-24.md#cq14) · [Active work ledger](../WORK_LEDGER.md)

All 77 capability rows and required children remain. All 16 Pro families remain unfinished, with seven PARTIAL and nine OPEN. The repair credits only this GIF behavior and exact engineering qualification. Professional selection and masking, compositing, Subject & Background, remaining R1–R11, B2/F03, RV5, device qualification, workspace locks and owner release gates remain open. Preserve the website-before-Console and editing-acceptance-before-final-bilingual-onboarding order. [Owner scope](OWNER_PRO_SCOPE_ADDENDUM_2026-11-27.md) · [Roadmap](../GZ_ENGINEERING_ROADMAP.md) · [Release gates](../RELEASE_GATE.md)
