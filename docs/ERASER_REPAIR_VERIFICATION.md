> **Historical checkpoint — current route updated November 25, 2026.** Follow the [GPT-6.2 Sol Ultra successor handoff](NEW_CHAT_HANDOFF_2026-11-25.md), [current ledger](WORK_LEDGER.md) and [PR #37 / 0.12.11 Test evidence and focused phone script](B1_WORKSPACE_VERIFICATION.md). Current task: read-only successor onboarding, then wait for Lance's ongoing GPT-6 Pro review. Original text below retains its dated evidence; its “current”, “next”, delivery, engineering and test commands are historical, not fresh instructions. Do not replay completed intake, question exchanges, builds or phone scripts. The [current review brief](coordination/GPT_6_PRO_REVIEW_BRIEF_2026-11-25.md) supersedes older review prompts.

# PR #31 — Eraser integrity and GIF export qualification

## Current outcome and exact application

**The named engineering corrections are qualified for owner handset testing in GZ Editor Pro 0.12.10 Test.** [PR #31](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/31) fixes R29-01/R29-02 Regular/Collage Eraser behavior and the GIF Save performance failure observed during qualification. The final candidate and application merge each pass JVM/build/signing and **193/193 cases on both Android 10 and Android 15**, with zero failures, errors or skips. This closes those engineering findings only. Owner handset acceptance, complete feature families, formal workspace locks and commercial release remain open.

| Identity | Exact value |
| --- | --- |
| Qualified application merge | [`3ad65c533f7667a136d4fa1838afcc06d3f484da`](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/3ad65c533f7667a136d4fa1838afcc06d3f484da) |
| Qualified candidate | [`aa86c37c64c5e395d5378df281a0c7583a39d0e0`](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/aa86c37c64c5e395d5378df281a0c7583a39d0e0) |
| Identical candidate/merge source tree | `1c7e3007204192ea240a7fd8d65cbd00cf7f9511` |
| Candidate verification | [Run 45480001019](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45480001019) |
| Independent merged-main verification | [Run 45483297512](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45483297512) |
| Phone APK source | [Merged API35 emulator-exercised artifact 14963127303](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45483297512/artifacts/14963127303) |
| APK filename / launcher | `app-debug.apk` / `GZ Editor Pro 0.12.10 Test` |
| Manifest identity | Version `0.12.10`; version code `22`; package `com.greenezone.gzphotoeditor.phonetest.v22`; min SDK 29; target SDK 36 |
| APK SHA-256 | `ed362e928db9db1f3f37c7a82c99141d64af42db1112ed8eacbdec6f466dfb80` |
| Signing | Android Debug certificate; APK v2 signature independently verified over the delivered APK |
| APK bytes | 32899125 |
| Artifact ZIP SHA-256 | `ab5ad668a034decb54866c3059f0f7d45c48f2bb31220b25d1356be3cb7d8c94` |
| Signing certificate SHA-256 | `40b29579b3c5b21461ac4d5d5e9a4fe9396b9723a59bdad1e24a3dc268a1d3f2` |

[Machine-readable qualification receipt](qualification/PR31_0.12.10.json).

The artifact is a ZIP; extract `app-debug.apk` to install. Package v22 installs separately from the older v21 test app. That preserves the older app and its local projects in their existing storage; it does not automatically transfer projects or verify a same-ID upgrade.

This document may land in a later documentation-only `main` commit. The qualified application/test/Gradle/CI inputs and APK remain those identified above. A later source change needs its own qualification; a documentation commit does not rename or replace the tested application commit. The historical 0.12.9 APK is superseded for this combined phone pass.

## Candidate and merged runtime evidence

| Check | Candidate API29 | Candidate API35 | Merged API29 | Merged API35 |
| --- | ---: | ---: | ---: | ---: |
| Completed Android cases | 193/193 | 193/193 | 193/193 | 193/193 |
| Failures / errors / skips | 0 / 0 / 0 | 0 / 0 / 0 | 0 / 0 / 0 | 0 / 0 / 0 |
| Receipt exit status | 0 | 0 | 0 | 0 |
| Per-test logcats | 193 | 193 | 193 | 193 |
| Edited portrait Save through visible completion | 4,159 ms | 4,580 ms | 3872 ms | 3334 ms |
| Five distinct visible samples after Play | 1,129 ms | 1,661 ms | 1393 ms | 1525 ms |

All original **186** Android case identities remain, plus **five** Eraser cases and **two** detail-cache fidelity cases. The identical 193-case identity digest is:

`b190082b3b60bf0b19ef6f66eafb31ca0b258bc9264c4b6b8168ae50f9f29816`

Digest definition: sorted `classname#testname` strings joined by LF, with no trailing LF. Raw XML counts, individual case outcomes, exit code, per-test logcats and archive hashes are checked together; aggregate green status alone is insufficient.

| Raw receipt | ZIP SHA-256 |
| --- | --- |
| [Candidate API29: 14961524280](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45480001019/artifacts/14961524280) | `d58d3509b2d3da7928117172905fe95288c09788c525389c49c09634583caeb2` |
| [Candidate API35: 14961729116](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45480001019/artifacts/14961729116) | `80e1fab86cb8f2795b4693eb429d1e21651f88cde1bb32ba520d703042dc13a4` |
| [Merged API29: 14963562392](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45483297512/artifacts/14963562392) | `98fd5b062d376c45764e4b763917df849ff2bb7110d4aaef681ef9fa242b9467` |
| [Merged API35: 14963142239](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45483297512/artifacts/14963142239) | `1b7db96a348567041b9f6cbc84fd60b3b2a084a637b9a498a200b1e5f78176e1` |

The portrait test retains the actual 1080×1920 input, trim, Reverse, B&W including clarity, Exposure drag and rapid scrubbing. It exports **180×320, 88 frames, 2.950 seconds**, checks every saved frame delay and independently decodes the first/last reverse marker pixels, then reopens and plays the saved GIF to its last frame. The original **6-second sampled-motion** and **15-second Save** limits are unchanged. Five samples include the initial paused picture and do not establish full-rate playback or edit-to-first-correct-still latency. These are synthetic emulator measurements, not a phone-performance guarantee.

## Closed findings and bounded implementation

| Finding | Demonstrated failure | Corrected behavior and actual regression scope |
| --- | --- | --- |
| R29-01 — Eraser capacity/data loss | At 160 strokes, a first split could produce 161 and rendering/serialization omitted the last unrelated stroke. A later overflow could retain earlier partial erasure. | Every sample uses a bounded result. A refused operation restores the entire pre-gesture document, dirty work and history, clears transient pointer state, and shows the existing notice. Serialization rejects invalid ink cardinality before producing recipe fields. Actual Regular tests verify first and later overflow; the selected Collage-tile case verifies first-sample capacity refusal. Together they cover unrelated pixels, history, named reopen and decoded PNG with parent/sibling isolation. |
| R29-02 — Sparse visible segments | The bounded Draw sampler left long visible segments, while the old Eraser tested only vertices. | Eraser clips rendered segments against the circular/swept brush path, including between pointer samples and accounting for stroke half-width in image-short-edge units. Long actual Draw→Erase flows verify a visible gap, surviving sides, cancellation, Undo/Redo, save/reopen and PNG in Regular and selected Collage tiles. Duplicate-source siblings remain independent. |
| Qualification GIF Save failure | Candidate 1 took 28,331 ms on API29 against the 15,000 ms gate, after functional export/reopen assertions had passed. | An exact detail-aware per-export cache avoids repeated identical tone calculations while retaining the original neighborhood/detail computation and output. Two Android tests compare the cached and uncached raster pixel for pixel. The changed candidate and its application merge pass the unchanged GIF performance/fidelity gates. |

Source references are pinned to the qualified application:

- [Eraser geometry](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3ad65c533f7667a136d4fa1838afcc06d3f484da/app/src/main/java/com/greenezone/gzphotoeditor/editor/InkMath.kt), [actual gesture transaction](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3ad65c533f7667a136d4fa1838afcc06d3f484da/app/src/main/java/com/greenezone/gzphotoeditor/ui/PhotoEditorScreen.kt), [serialization guard](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3ad65c533f7667a136d4fa1838afcc06d3f484da/app/src/main/java/com/greenezone/gzphotoeditor/editor/EditDocumentCodec.kt), and [five actual Eraser interaction cases](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3ad65c533f7667a136d4fa1838afcc06d3f484da/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidRegularEraseInteractionTest.kt).
- [Exact export color cache](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3ad65c533f7667a136d4fa1838afcc06d3f484da/app/src/main/java/com/greenezone/gzphotoeditor/editor/ImagePipeline.kt), [encoder stages](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3ad65c533f7667a136d4fa1838afcc06d3f484da/app/src/main/java/com/greenezone/gzphotoeditor/editor/GifEncoder.kt), [reader stages](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3ad65c533f7667a136d4fa1838afcc06d3f484da/app/src/main/java/com/greenezone/gzphotoeditor/editor/VideoGifSource.kt), [two Android cache-equivalence cases](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3ad65c533f7667a136d4fa1838afcc06d3f484da/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidGifDetailColorCacheTest.kt), and [unchanged portrait playback/export qualification](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3ad65c533f7667a136d4fa1838afcc06d3f484da/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidGifWorkspacePlaybackTest.kt).

The cache key is `(sourceARGB, computedDetail.rawBits)`, so an identical color with a different neighborhood cannot reuse the wrong detail result. Vignette and grain remain excluded because their output depends on position. The cache remains confined to one export and 16,384 entries, with approximately 208 KiB of array payload, 64 KiB above its predecessor. The existing texture-ineligible assertion was replaced by stronger full-pixel equality for the intentionally supported texture path; original alpha checks and coordinate-effect exclusions remain.

Normal Regular rendering and the GIF preview overloads, grading math, detail/denoise processing, mask/privacy/creative order, palette, dimensions, delays and frame count are preserved. No layout/style, mode ownership, Pro production, document schema, dependency or CI workflow change was made. Capacity refusal reveals the existing controls and notice so the failure message is visible; it does not add a new screen or rearrange tools.

Host checks support, but do not replace, the Android evidence. The two original geometry regressions plus 12 targeted geometry cases pass; codec and prior Creative/Crop controls pass. Independent review checked 300 deterministic geometry cases / 173,670 membership assertions across square, portrait and landscape inputs. The actual corrected cache with production tone/detail functions matched 5,068,800 host pixel calculations exactly. Both Android APIs then passed the actual Bitmap equivalence tests, including alpha, changing neighborhoods, positive/negative detail and denoise grades.

Candidate stage measurements recorded rendering at 363 ms / 1,090 ms and reader closure at 6 ms / 6 ms on API29 / API35. Both recorded 5,053,519 cache hits and 15,281 misses. These establish the measured corrected path. They do not retrospectively prove which single old stage consumed the unexplained candidate-1 interval; worker CPU counters also exclude external codec processes.

## Retained failure chain and why each change was justified

### 1. Unchanged production reproduced the Eraser defects

Baseline main was `e04bc08a4800233b0f407b419a91c97ca2c068f6`, with application inputs identical to PR #29 merge `c9efc5aa0673f21b98f150733582a24996b34d10`. Lance explicitly authorized repository repairs after the independent review. That prior checkpoint's four original corrections and 186-case evidence remain credited.

Test-only commit [`9c17cc3d42b390e8cae18f5175d30be66a44fd50`](https://github.com/MrSkyrimFan/GZ-Mobile-001/commit/9c17cc3d42b390e8cae18f5175d30be66a44fd50), [run 45471163485](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45471163485), changed no production code:

- JVM ran 115 cases and failed exactly the two new Eraser geometry/capacity regressions and the over-capacity serialization regression.
- [API35 receipt 14959370204](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45471163485/artifacts/14959370204) completed 191 cases: all original 186 passed and exactly five new Eraser cases failed, zero errors/skips. ZIP SHA-256: `3ae0d85a5f5a2246aca2ff62bb167dc796e05b6d8339a30f78d283d12ffea06d`.
- [API29 receipt 14956768023](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45471163485/artifacts/14956768023) stopped after 20 cases: 19 passed and the existing rotation fixture crashed before Eraser execution. ZIP SHA-256: `5bbd0bd622a20d51bc88b28fe0ed439fe516d8ec51fb3945a72712154dea2dc2`. This abort is not API29 Eraser reproduction evidence.

The API35 failures independently caught a missing last unrelated stroke, retained earlier partial erase after later overflow, changed parent Collage content after refusal, and absent visible gaps in the two long-line workflows. The fixtures seed the dense boundaries automatically.

### 2. API29 rotation harness first applied Compose changes from a worker

The first API29 error at 19:37:57.545 UTC was `CalledFromWrongThreadException` in `CompositionImpl.applyChanges`, through test-only `TestMonotonicFrameClock` and `ApplyingContinuationInterceptor`, ending in `CoroutineScheduler.Worker`. Main-thread SlotTable disposal failed later at 19:37:58.018. This order was independently inspected in the raw log.

The narrow correction changed only the [rotation test's rule import](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/3ad65c533f7667a136d4fa1838afcc06d3f484da/app/src/androidTest/java/com/greenezone/gzphotoeditor/AndroidBackRotationTest.kt) to `androidx.compose.ui.test.junit4.v2.createAndroidComposeRule`. Actual pointer gestures, orientation change/restoration, viewport, layer/tool and Undo assertions remain. The [official migration guide](https://developer.android.com/develop/ui/compose/testing/migrate-v2) documents the queued StandardTestDispatcher replacement for the legacy unconfined rule. No production Pro/lifecycle rewrite or assertion weakening followed this harness evidence. The corrected candidate and merge execute the rotation case successfully on both APIs; physical rotation/lifecycle acceptance remains distinct.

### 3. Candidate 1 passed Eraser, but its GIF speed gate failed

Candidate `fe2a3054082fbcc5edda054ec0a259e097291781`, tree `2c64493a164a5a29a65a293ed580cb9316051978`, [run 45473953140](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45473953140): JVM/build/signing passed, API35 passed 191/191, and API29 passed 190/191. All five Eraser cases and rotation passed on both APIs. No original case was removed.

| Candidate 1 receipt | Result and retained hash |
| --- | --- |
| [API29 14960290828](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45473953140/artifacts/14960290828) | Only portrait Save failed at **28,331 ms > 15,000 ms**. ZIP SHA-256 `cf0a40e2835a10c852360649b1bf452cb12a6c2c0427102784df5bffc1afc03f`. |
| [API35 14960645710](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45473953140/artifacts/14960645710) | All 191 passed; portrait Save 5,946 ms. ZIP SHA-256 `374ed7e83bfe654901a682ca90934efd8b1a62f5216eca5b557b5b3b1adb5913`. |

The identical 191-case set hashes to `7d851781d5221ef6f79cc29bb262ce8c0bffd24b698c3bd53320980f366df415`. The failing timing assertion ran after successful output dimensions, every delay, reverse endpoint decoding, reopening and complete replay. Staging took 28,144 ms. The reader-loop total was 27,193 ms, of which frame reads accounted for 2,007 ms; old counters could not assign the remaining 25,186 ms to one stage. No relevant application GC, codec wait or Compose fault was logged. Other matched cases showed only about 1.08× median slowdown, so the result was not dismissed as runner noise.

### 4. A changed candidate passed the same gate

The B&W recipe includes clarity `.16`, which previously disabled the ARGB-only color cache. The exact detail-aware cache, aggregate profiling and two fidelity cases were added without changing the fixture, output or time budgets. This was a changed implementation, not an unchanged retry. Candidate 2 and its identical-tree application merge passed the results recorded above. Candidate 1 remains a failed, superseded artifact; its partial success is not relabeled as qualification.

## Focused owner phone pass

Use the exact merged v22 APK above and keep originals. This pass checks actual finger feel, the owner's original media and physical Android behavior that emulator receipts cannot establish. There is no need to manufacture a 160-stroke drawing; the seeded boundary/cancel/save/output tests already exercise that limit.

1. **Original GIF clip.** Open the portrait video that originally stopped playing. Confirm normal playback, move both trim handles, scrub rapidly, apply Reverse and B&W, adjust Exposure, then press Play again. Check visible motion and reverse direction, time Save, and reopen the resulting GIF in this app and another viewer. Confirm correct framing, duration and motion. Record the selected output size and any failure; larger phone workloads are not covered by the 180×320 synthetic time result.
2. **Regular Draw and Eraser.** Draw a long line with bends, erase across its middle, and confirm a gap appears while the remaining sides stay. Undo and Redo, save/reopen the project, and export PNG. Repeat a canceled gesture and check it leaves no unwanted change or extra history. Existing Text, Shapes, Blur & cover, layout and tool placement should remain familiar.
3. **Independent Collage tile.** Use the same photo in two tiles with visibly different edits. Draw and erase in one selected tile, return with Done, reorder and Undo/Redo, then save/reopen/export. The neighboring tile must keep its own edits. Cancel a tile gesture and check it does not change the parent document.
4. **Retained affected interactions.** Add an opaque privacy cover, manually crop/flip and inspect the PNG. Check easy crop-handle acquisition and a canceled long Draw. In Pro, try the retained long-lasso/mask and layer/history/save behavior; try both Regular→Pro Yes and No on separate attempts and verify the Regular draft remains when returning. These protect prior fixes; PR #31 does not complete all professional Pro features.
5. **Physical Android Back.** Exercise both screen edges, a canceled half gesture and rapid double Back. Check tool drafts, menus, keyboard and the Android save picker before leaving a workspace. One committed gesture should close the current transient context or pop one appropriate route, preserving the intended work. Report device/Android version, exact steps and whether data or an original changed if any behavior fails.

## Remaining limits and next engineering task

The owner-original video, sustained phone responsiveness/thermal load, OEM codec/provider behavior, physical Back gestures and actual OS process reclamation while an external picker is open are not established by this emulator pass. The earlier 16 MiB legacy-Pro heap condition remains a separate R1/R8 qualification obligation. General durable working drafts, bounded project listing/loading, complete RAW/color/Pro/Collage/GIF families, finished stickers, legacy v4 GIF framing upgrade and commercial release remain tracked work.

**Next engineering task is R1.1: bounded saved-project listing and loading; R1.2 recoverable Regular drafts follows.** See the [single work ledger](WORK_LEDGER.md), [77-family capability inventory](CAPABILITY_MATRIX.md) and [roadmap](GZ_ENGINEERING_ROADMAP.md). R14 is PARTIAL after the named Eraser repairs; the entire Draw/Shapes family is not marked complete. Four formal pre-Video locks remain owner decisions. Video stays gated; final bilingual onboarding follows all editing workspaces; Play Console setup follows the finished app and Greenezone website. This task authorizes no website or unrelated-repository work.
