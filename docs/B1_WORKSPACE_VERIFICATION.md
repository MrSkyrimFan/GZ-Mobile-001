# B1 workspace correction verification

> **Current work — November 25, 2026:** [PR #41](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/41) qualifies **0.12.12 Test / code 24 / phonetest.v24** for focused handset confirmation. Candidate and merge passed JVM/build/signing and **265/265 on each of API29 and API35**, with all inherited cases retained. R1.1 begins with the saved-project baseline in the [work ledger](WORK_LEDGER.md). B2/F03 and wider acceptance remain open. See the exact evidence below.

## PR 41 follow-up — 0.12.12 Test

Candidate `59b117b0277d3f55c74d3864e2468cd79c8bec70` ([run 45703112963](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45703112963)) and merge `9a58c72cdcf379885150b55b498a2f0ef326dee1` ([run 45706255531](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45706255531)) share tree `2880c62d0ee69c2ac44ef61e44abcd3bc73fec60`. Each passed JVM/build/signing and 265/265 on both API29/API35, with zero failures, errors, skips or duplicates. Both preserve all 258 inherited identities and add seven. The sorted `class#test` lines joined by LF have SHA-256 `81239ac3268d6488012aad837fde48854fb3074d933759e1b78cdb9e7b0bfcf8`.

[Test-only baseline 45700173405](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45700173405) at `c6eccf3dee2ff2ea34e387191d1c95a6534b088f` produced three intended failures per API: Pro changed selection during a held named Save, the Collage header overlapped the 66 px top inset, and a drag hid Collage chrome. All inherited cases, the cross-boundary tap fixture, and the three nested departure workflows passed on that baseline. The corrected tests cover Pro's blocked layer actions, Collage Options in the actual activity across portrait and landscape, free tap and invalid streams, and child/parent Cancel, failed Save, and Save-before-departure.

- [gz-editor-pro-emulator-receipts 15064391565](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45700173405/artifacts/15064391565) · `sha256:3dbaaeb9d1a6cb2fdaa8f3d038680612ddc83217d870fe39c4e9582999a22af5`
- [gz-editor-pro-emulator-api29-receipts 15064296673](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45700173405/artifacts/15064296673) · `sha256:589719777432a93c4bd3dd68199f0cc87297ae13cfe9b3a900f8b04039b17f94`
- [gz-editor-pro-emulator-api29-receipts 15064513878](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45703112963/artifacts/15064513878) · `sha256:a1fafca494e9ba169395c322b6ad84515c36ecc931d6bf5292bbf91a75214759`
- [gz-editor-pro-emulator-receipts 15063429798](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45703112963/artifacts/15063429798) · `sha256:030770982e55fc4eaa9bf47f3eeb57f84747b5ab37f1cf973f757f6fb6569b78`
- [gz-editor-pro-emulator-receipts 15067295124](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45706255531/artifacts/15067295124) · `sha256:22c6c7b85848663d336d433e56067b6f46b4bfd77aa2d301a2db70f588c41bfd`
- [gz-editor-pro-emulator-api29-receipts 15064624747](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45706255531/artifacts/15064624747) · `sha256:88e1819c9493d54f4cfc11dbabd9f9e47704681d2614d3b6bcac3daf57f39ba3`

[Exact merged API35 APK ZIP](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45706255531/artifacts/15065588960) · code 24 · `com.greenezone.gzphotoeditor.phonetest.v24` · min29 / target36 · 33783857 APK bytes. APK SHA-256 `75a8a2f5f9a1caf16641b15625a9f9fb135bfcf7fff32070d731bc1f907b5943`; ZIP SHA-256 `edd754087395af66884afe2a6f622155cb84d44e2022ae812ced5eeef8459637`; verified Android v2 signer certificate SHA-256 `560a72d91aa9a7fc73b53d29d8146011d142e75cbbffe38a839d425fb3cfaa54`. It installs beside older test packages. For v24, follow the applicable [focused review handset checks](coordination/GPT_6_PRO_INDEPENDENT_REVIEW_2026-11-25.md#e-focused-phone-script-and-separate-engineering-plan). B2 targets and F03 remain open.

## Retained PR 37 qualification — 0.12.11 Test

The original PR #37 qualification and performance evidence follows.

## Authority and exact baseline

Lance relayed the completed Stage 3 answers/resume prompt. The full CQ01–CQ50, active instructions, specification, roadmap, 77-family inventory and release gate were read before source edits. Baseline main was `6f7c574f223e5349a74e622e97433736a07f4a44`, tree `99239d1fd9efc25a4324aa39bb6ebe44a02395ff`. The inherited qualified app was PR #31 merge `3ad65c533f7667a136d4fa1838afcc06d3f484da`, 0.12.10 Test/code22. The following PR #37 qualification supersedes its phone download and retains its regression evidence.

[PR #37](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/37) implements B1 plus B2 measurements. Engineering branch is `codex/b1-workspace-contract-20261125`; `feature/b1-contract-verification-20261125` invokes the unchanged push-triggered full matrix. This document is an evidence receipt; [WORK_LEDGER](WORK_LEDGER.md) and [GZ_ENGINEERING_ROADMAP](GZ_ENGINEERING_ROADMAP.md) remain the single execution record and roadmap.

## Qualified candidate, merge and exact phone artifact

| Evidence | Exact identity / result |
| --- | --- |
| Final candidate | `044e93321292a3678b92394b5efda50aae2e07b9`; [full run 45596352650](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45596352650) |
| Application merge | `46ffc891d7a02fffc0b2a4e8f807de8fbefc2c30`; [full run 45600142592](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45600142592) |
| Shared candidate/merge tree | `57b3ff2510e059184bfaefee2e781d350bf0930f` |
| Candidate and merge results | JVM/build/signing pass; **258/258 per API29 and API35**, zero failures/errors/skips |
| Retained method set | All 193 inherited cases plus 65 added cases; 258 unique Android identities, zero missing/duplicates |
| Phone identity | **GZ Editor Pro 0.12.11 Test**, version code 23, `com.greenezone.gzphotoeditor.phonetest.v23`, minSdk 29/targetSdk 36 |
| Merged API35 exercised artifact | [15017086053](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45600142592/artifacts/15017086053), APK 33,767,477 bytes; ZIP SHA-256 `a3aee3437252cf3df4bd7091094baa1c5ad7a4124200e6bcc349256003de0065` |
| Exact APK SHA-256 | `969d2c095f856a65f3d6de905e253dfd6e225e479a451edf4b21b2989362f971` |
| Signature | Independently verified for API29–36; Android APK Signature Scheme v2 valid; signer certificate SHA-256 `a72deddffe1cdb7eabfb7ffb5853c42dcb1c14b8bac779d65b33e533f737de59` |

This separately installed phone package is for focused testing; it does not establish a same-ID production upgrade. Later documentation-only main revisions do not create another binary or replace the qualified application identity above. Full failure history below is retained. No workspace lock, whole-family completion, universal media claim or public release acceptance follows from these checks.

Full candidate XML archives were read and matched GitHub's published ZIP digests: API29 artifact `15014039551`, SHA-256 `6bd2168262ca87bcdeab274f4e309c691c94a95e66989c13fa7102b35f2de5f4`; API35 `15015510834`, `2fb63d2305e09e1b26c4231643e7dedda637be5aa709431a91bbb718d1d9dac0`. Merged API29: `15017235171`, `56a28424c9232f0de23cd15cac8da6a7d1120d98ba9ba850d65ad823aa03af00`. Merged API35: `15016297183`, `b30657e49ff3f2f7e3f59baa37bbe0ea32c30087916a607790399543cf63b7ac`. Every full XML declares 258 tests and zero failures/errors/skips, matching all 258 expected case identities with zero missing/extra/duplicates. Sorted identity SHA-256: `8daa78d5f2580cdf8fc86f2811e34e5d70c403cf846ad6a6e458b3eab669cbed`.

The candidate phone artifact `15014628554` was independently checked before merge: ZIP SHA-256 `2331ed8a9523bb44d956b04bc4cfce3686ff0108718b2a9a1aaf6cee6a720058`, APK SHA-256 `a0dc759c749555d2344f68029afa3a9f50e7418f174f3d88458781fb238476ee`, valid v2 signature for API29–36 with no errors/warnings, and the same declared v23 package/version/SDK/label. Its signer certificate SHA-256 is `534841609eced8454465ca782b285dd3a62da7c24dc438bbb0dda548cc5ce4c8`. The merged artifact in the table is the delivery, separately verified; temporary candidate verification is not a handset installation or upgrade test.

### Final representative observations, with targets unchanged

These are individual executions on separate CI hosts, **not a matched cold/warm sample study**. The same default-quality fixture is 1080×1920, 150 source frames, trim 0–2665 ms, Original/source FPS/100% forward/neutral, yielding 80 scheduled frames. Initial baseline heap properties were not recorded; the final measured runtime limits below are explicit.

| Metric | Candidate API29 | Candidate API35 | Merge API29 | Merge API35 |
| --- | ---: | ---: | ---: | ---: |
| Application maximum heap (MiB) | 256 | 192 | 256 | 192 |
| Export activation to picker (ms) | 13666 | 13601 | 14159 | 14893 |
| Source reader wall (ms) | 7995 | 9050 | 8371 | 10255 |
| Source reader calling-thread CPU (ms) | 7082 | 8187 | 6988 | 9023 |
| Pipeline (ms) | 11596 | 12073 | 12174 | 13484 |
| Start UI acknowledgement (ms) | 65 | 102 | 83 | 92 |
| Cancel UI acknowledgement (ms) | 22 | 66 | 49 | 50 |
| Cancel worker stop (ms) | 43 | 15 | 34 | 17 |
| Observed cancel-command to completed notice (ms) | 137 | 162 | 138 | 132 |
| GIF bytes | 995576 | 995576 | 995576 | 995576 |
| Verified frames | 80 | 80 | 80 | 80 |
| Encoded duration (ms) | 2670 | 2670 | 2670 | 2670 |

All four final observations retain 995,576 output bytes, 80 frames, 2670 ms complete scheduled duration, unchanged source bytes and the same independently decoded sample errors as the respective original API baseline. Every selected-frame scalar/bulk/cache comparison and inherited 6 s motion/15 s Save gate passes. The default full-resolution profile is a separate workload; its preparation time is not compared against the smaller fixture's 15 s gate.

The **10,000 ms absolute AND 50% picker-reduction targets remain unmet on both APIs**. Relative to the original 20,932/26,262 ms baselines, candidate preparation is 13,666/13,601 ms and merged preparation 14,159/14,893 ms. The **50% source-reader reduction remains unmet on API29** (original 15,199 ms; candidate 7,995/merge 8,371) and is met for these API35 observations (original 22,354 ms; candidate 9,050/merge 10,255). Final start/cancel acknowledgements are below 250 ms and observed cancellation below 1000 ms; retain the earlier 257 ms start miss rather than deleting it or claiming a universal response guarantee.

Each completed original-size profile reports 32,768 bytes of exact-color cache payload, 4,320 bytes of sample-row arrays, 165,885,502 exact-cache hits and 2,498 misses. The reverse cap and full-frame buffers are unchanged. Source-reader/bitmap/encoder counters overlap and must not be summed as disjoint stages. Actual owner-media/phone measurements and the required matched sample study remain open.


The fixed targets are retained. The two bounded optimization experiments were evaluated with representative gains and unmet exits; do not recast the separate-host observations as a matched cold/warm study. B2 target completion and F03 remain open, including the 13.698-second/257-ms API35 sample below. The next performance decision requires matched measurements on the declared workload and an evidence-backed cause; no third speculative optimization is a prerequisite for delivering this regression-qualified B1 candidate. Independent R1.1 work follows the current ledger and successor/review gate; this documentation handoff adds no application qualification.

## Reproductions and diagnosed failures

| Input / run | Observed result | Disposition |
| --- | --- | --- |
| Test-only `84ecf372d3c76b12f95784916d23fef7b40f18c7`; [45568857304](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45568857304) | API35: 193 inherited passes, 6 new failures. Three intended dirty-route failures; photo-GIF scroll and two Collage URI setup defects. API29 stopped after seven cases from system_server 16 MiB heap exhaustion. | Retained failure evidence. Fix test setup only and explicitly configure AVD RAM 2048M / heap 256M; no assertion/timing relaxation. |
| Corrected test-only `cf94cf3c4be22f6fbd473d5cb97a76aefbb70bc9`; [45571000015](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45571000015) | JVM/build/signing pass; both APIs: 199 cases, 6 intended failures, zero errors/skips; all 193 inherited cases pass. | Executed red evidence for Pro Back/mode, photo/video GIF Back, Collage Back and single-tap chrome. API35 XML artifact 15002133591 ZIP SHA-256 `72f8f8dadd66cdc25d4e7ddf7bf583c36be8341b00fbce157b8267618d41c742`; API29 artifact 15001949288, ZIP SHA-256 `6681da6fab5164acb508f8db22696890cf728268bb1d14fba00d8dd25594a92a`. Both full XML receipts confirm exactly the six named failures. |
| Integrated `a990aab0a03de36127d56cd6100f02b872f344c3`; [45575188527](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45575188527) | App compilation, JVM tests and debug assembly completed. Android test compilation rejected an invalid onNode import. | Remove the import, preserve test body. |
| `f61c786e0da534fdc06d5683b7cf82047a6182a6`; [45576278160](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45576278160) | App compilation/JVM/debug assembly completed. Android test cleanup used nonexistent GifStore.delete. | Use existing deleteProject API; no test assertion changes. |
| `0cc910ce1ca486f5a3da296855fe3f516a9bc193`; [45576913155](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45576913155) | Production compilation rejected recoveryResolved in the wrong function. | Correct exact closeCurrentPhoto/commitDiscard blocks; the independent reviewer found the same mistake. |

Integrated `805f97a3dc05702ec9c424de3d8e2a68573d0657`, [run 45577375539](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45577375539), passed production/test compilation, JVM tests, debug assembly and signature verification. Final source review confirmed the recovery/header correction. The first full run is recorded below; this compile-only checkpoint is historical.

### First integrated full execution — retained failures

Candidate `c819db57bc7fb33d0c46d83019cb5d9e7c428caa`, [run 45577786262](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45577786262), passed build/JVM/signature checks and executed all 250 Android cases without skips: API29 had 14 failures, API35 had 13. Neither is qualified. Full XML ZIPs were independently read and SHA-256 matched: API29 artifact `15006357100` `79e6f611001920c7f306bc01c4f8268f4cf9fd065877fd20ab4c66bb2a76e4d2`; API35 artifact `15006226855` `1334b8e73c77e985ed1e6c1b3eb3cc09986740adbea9e584810cc2592fdf4956`.

Diagnosed repairs are bounded to those failures: exact saved GIF identity recovery; AtomicFile obstruction/rollback and saved GIF read preflight; actual asynchronous Pro completion; dialog-scoped selectors and held-pointer semantic actions; loaded crop readiness; Collage relink readiness; and the API29 portrait capture viewport. Original method identities, content/source/history/fidelity assertions and existing budgets remain protected. Subsequent execution must verify the corrections; a diagnosis is not a pass.

### Second integrated execution and bounded follow-up

Candidate `713ed1917804b74f1cf4d93b46889c17dbd1f875`, [run 45583464308](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45583464308), passed build/JVM/signature checks. API29 passed **258/258**, zero failures/errors/skips. API35 passed **257/258**, with one test observation race and zero errors/skips. Full XML ZIP identities: API29 artifact `15008911956`, SHA-256 `6e743c79875da1920069fe20035e1bc46f83c4879c424b6f200c6f3637aedf45`; API35 artifact `15007774538`, SHA-256 `60f3a48640298fc7cdc1be87dd054841e511d5b1100007ffee349ef8549f4fa2`. Both archives were read completely and matched their published digests.

The sole API35 failure is ordinaryProDiscardReadsFreshRecoveryBeforeItNavigates. Source diagnosis confirms durable revoke → original baseline publication → session publication → departure callback. The fixture's departure counter updates synchronously but its external document mirror updates on the next composition. The corrected test retains the departure wait, then asserts exact count/document on Compose idle; all journal assertions remain. This is not a production ordering change.

The first bulk-row experiment passed all exact selected-frame ARGB, reverse-cache, output fidelity/schedule, source-protection and inherited timing tests on both APIs. Its measured row arrays are 4,320 bytes for the portrait fixture and 5,120 for the rotated fixture. However, it **did not meet the prospective preparation/source-reader speed targets**:

| Post-bulk-row metric | API29 | API35 |
| --- | ---: | ---: |
| Preparation to picker | 17,929 ms | 16,110 ms |
| Source reader wall / calling-thread CPU | 12,008 /11,407 ms | 13,640 /13,204 ms |
| Pipeline | 16,037 ms | 15,182 ms |
| Start UI acknowledgement | 122 ms | 88 ms |
| Cancel UI acknowledgement / worker stop | 59 /38 ms | 53 /16 ms |
| Observed cancel-command to completed notice | 103 ms | 104 ms |
| Output bytes / frames / duration | 995,576 /80 /2670 ms | 995,576 /80 /2670 ms |

Sampled independent decoded-color errors are identical to the corresponding baseline API. All three cache-equivalence cases and both preparation/cancellation cases pass. These observations do not close handset F03.

**Second bounded B2 experiment, recorded before its production patch:** source-reader CPU remains dominant. Test exact memoization of repeated packed YUV samples inside that reader, without changing PreviewColor.pixel arithmetic. Use one reader-owned direct-mapped cache of at most 4,096 keys plus 4,096 ARGB values (32 KiB array payload), with the color-matrix identity in each key; collisions recompute the original result. Do not attach this cache to retained reverse pictures or share mutable global caches. Retain the uncached scalar oracle, bounded reverse storage, existing row-memory bounds and all prior absolute/relative/fidelity/response targets unchanged. Add replacement/matrix-identity tests and aggregate conversion/cache counters. This experiment has one explicit causal hypothesis: repeated exact sample colors can avoid repeated floating-point conversion while preserving every pixel.

The GIF implementation owner returned the bounded reader memo and four JVM regressions; integration source review confirms matrix-qualified exact keys, collision recomputation, reader-only ownership, unchanged pixel arithmetic and preserved scalar oracle. GzGifReaderStage records bitmap materialization (sampling/reverse inflate + lookup/conversion + bitmap allocation), reverse sample packing and cache hit/miss/storage totals once on close. These counters are nested within sourceRead and are not additive to it. Other production files are unchanged by this experiment. Evaluate the resulting run against the original 10,000 ms /50% targets and record any unmet condition explicitly.

### Third integrated execution: representative improvement, invalid API29 heap

Candidate `32341cac97c0fe3b4595ff4dd909aeac2ffd9490`, [run 45589117594](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45589117594), passed build/JVM/signature checks including all four exact-YUV-cache JVM cases. API35 passed **258/258**, zero failures/errors/skips. API29 passed **253/258**, with five failures, zero errors/skips. The complete XML archives match GitHub's ZIP digests: API35 artifact `15012080963`, SHA-256 `ab5ccbb7a4a81a7de2fee7ad4071d14c634cda19cdda2539058d1769e01f0707`; API29 artifact `15011324585`, SHA-256 `3cfac8033731e25796e8aa87d2df5a25935818d6d7e8a2661a647efe72ee63f2`.

The API29 process reports `growth limit 16777216` (16 MiB): native portrait exact-pixel comparison cannot allocate its 8,294,416-byte array; rotated reverse storage cannot allocate its 2,764,816-byte array; both original-size GIF profiles fail frame allocation before a picker exists and then time out; the inherited Regular below-budget Save/read fixture logs a 1,999,296-byte allocation OOM at the same 16 MiB limit before its readable-project assertion fails. These receipts are failures, not a qualified API29 result. Workflow inputs requested `ram-size: 2048M` and `heap-size: 256M`, while launch logs report `qemu.dalvik.vm.heapsize=512m`; configured inputs therefore do not establish the effective ART application growth limit. A bounded infrastructure diagnosis and preflight correction precede any retry. No application memory/fidelity gate is waived.

API35's second bounded optimization observation: preparation-to-picker **13,698 ms** versus original **26,262 ms**; source reader **9,339 ms wall /8,818 ms CPU**; pipeline **12,212 ms**; bitmap materialization **9,069 ms wall /8,765 ms CPU**; 165,885,502 exact-cache hits /2,498 misses, 32,768-byte cache payload and 4,320-byte row arrays. Output remains **995,576 bytes /80 frames /2670 ms**, with identical sampled decoded-color errors to that API's baseline and passing exact selected-frame comparisons. Start acknowledgement was **257 ms**; cancel acknowledgement **36 ms**, worker stop **19 ms**, observed cancel completion **84 ms**.

The original **10,000 ms** absolute and **50% picker improvement** targets remain unmet on this API (47.8% observed picker reduction); source-reader reduction exceeds 50%. The 257 ms start sample misses the 250 ms engineering response target. These are single observations on separate CI hosts, not a completed matched cold/warm sample study or handset proof. Representative gains may be credited under CQ18; B2 target completion and F03 handset closure remain open. The two bounded experiments are evaluated with unmet exits, not successful B2 completion. CQ18/CQ50 permit a coherent regression-qualified B1 candidate and subsequent R1.1 work while these numeric misses and F03 remain open; they do not require a third speculative optimization. Candidate and merged-main qualification are still required before delivery.

### Bounded CI and diagnostic correction after candidate 3

The [runner implementation](https://github.com/reactivecircus/android-emulator-runner/blob/v2/src/emulator-manager.ts#L40) writes its heap input as `hw.heapSize`; the [AOSP AVD schema](https://github.com/aosp-mirror/platform_external_qemu/blob/master/android/avd/hardware-properties.ini#L263) defines integer-megabyte `vm.heapSize`. [Android 10 VM startup](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-10.0.0_r47/core/jni/AndroidRuntime.cpp#L770) reads maximum heap and growth limit separately, with a 16m default if the maximum property is absent. The failed run did not record guest properties, so the exact missing/restricted property cannot be reconstructed. The ineffective input and observed 16 MiB budget are verified; no stronger causal claim is made.

The correction explicitly sets API29 AVD `vm.heapSize=256` and `-memory 2048`, then uses a CI-only emulator/API29 guard to record the old guest values, set both `dalvik.vm.heapsize` and `dalvik.vm.heapgrowthlimit` to `256m`, restart the framework and require a fresh zygote plus the exact properties before the unchanged full suite. ADB calls and stop/boot waits are bounded; failure stops the job. No app `largeHeap`, test exclusion or acceptance relaxation is introduced. API35 execution settings are retained.

The GIF profile now fails immediately when the actual UI reports `GIF export failed.`, preserving its explanation instead of waiting six minutes for impossible success. Expected cancellation remains success; every existing timeout/assertion/workload/fidelity check is unchanged. Both profile paths record `Runtime.maxMemory()` to expose the actual app budget. Root readback review retained all 25 assertion lines byte-for-byte. The script received source review; a local bash syntax check was unavailable because bash is not installed, so execution of this changed CI setup is still required.

### CI preflight refinement, before Android instrumentation

Candidate `f4802bb807e3f0a96fc20b4335b3e17c06453377`, [run 45595436544](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45595436544), passes build/JVM/signing. API29 job `139140147036` stops in preflight with **zero Android tests executed**. The old guest values are maximum `512m` and empty growth limit; after framework restart both are exactly `256m`. The guard rejects `pidof zygote64` returning `3180 4097` instead of a single PID (old PID `1664`). This is a process-identification assumption in the new CI guard, not an application regression or proof of an Android suite pass.

The bounded correction selects the single `zygote64` with parent PID1 from Android's process table and requires a changed owner PID, `init.svc.zygote=running` and both exact heap properties. This checks the init-owned VM rather than counting same-named children. All budgets, full-suite selection and application code are unchanged. Candidate `044e93321292a3678b92394b5efda50aae2e07b9` supersedes the unqualified run. Its API35 job was canceled by branch concurrency; no full API35 pass is credited to candidate 4. The preflight failure is retained rather than retried unchanged.

## Implemented contract and finite review

The common header places the wrapping brand and Options at opposite edges on its first row, with workspace/Back and history on the second. Major actions have one Options location. Regular More/Create collage/Histogram entries are removed; processing/legacy compatibility remain. Collage free-canvas single taps select a hit tile and toggle chrome once; canceled/multi-pointer/moved gestures do neither.

Save/Discard/Cancel operates on captured native owners, revisions and expected saved-project identities. Child photo saves remain separate from Apply to Collage. A child-to-workspace departure stages child resolution until the parent decision and target preparation succeed. Regular-to-Pro resolves the intended photo appearance and existing Pro work before publication. Save acknowledgement follows verified publication; failed/canceled work stays open.

Stable save UUIDs are sidecars to compatible existing payloads. Update/Replace authorize the exact identity and stored-byte version; delete/recreate gets a new identity. File publication preserves verified old bytes until commit. Regular preferences retain a bounded previous-value record through verification and rollback. This is B1's minimum safe adapter, not the R1.1 per-project migration or R1.2 autosave completion.

Finite independent reviews found and corrected stale native-export restoration, post-publication rollback, late picker/dialog ownership, typed Retry/Cancel, child Photo routing, active pointer rollback, paired content/selection mutations, anonymous recovered-draft baselines, GIF name-only recovery aliases, Pro Delete recovery and narrow-header placement. Reviewers identify source findings separately from runtime results. Export retry keeps its captured recipe independently of native draft disposition; unreadable recovery permissions cannot count as a successful live Discard.

## Existing assertion migration

All **193 original Android method identities** are retained. The first integrated run executed **250 cases**; its failures are preserved above. The follow-up adds exact saved-GIF recovery, bulk/scalar ARGB equivalence and actual preparation cancellation regressions; the final combined source contains **258 cases**, all 193 inherited identities retained with zero duplicates. Required candidate and merged-main qualification is recorded above.

| Existing files / scope | Authorized route changes | Retained checks |
| --- | --- | --- |
| Nine Pro files; two assigned files unchanged, 31 cases | Options replaces File; Undo/Redo use accessible descriptions; named Save updates directly; Save as handles copy/collision. Dirty replacement explicitly saves; hidden work is saved separately before testing reopen history. | All 210 assertion-bearing lines retained modulo selector/default-copy-name text; layer geometry, masks, blend equations, source pixels, render samples, reset and named history ownership. |
| Four Regular files, 24 cases | Options replaces More/direct commands; child commands are photo-labeled; failed Save resolves before replacement picker. | 151 of 152 non-UI assertion lines retained; failed-save picker count strengthens 2→1. Dense-recipe budget, stroke/capacity/endpoints, sibling tiles, decoded PNG, source/history remain. |
| Regular histogram method | Production Histogram entry removed. Test-only dialog consumes samples from the actual hidden-chrome canvas, excluding letterboxing/seam interpolation. | Same method, full-bitmap RGB/luminance counts, chart assertions and 50%→0%→50% clipping expectations; additionally checks More/Histogram production absence. |
| Four GIF files, 16 cases | Options/current command labels; explicit save decisions; history semantics; named identity checks. | All 16 methods; scheduling, metadata, independent decoded frames, source bytes, export recovery and existing 6s/15s gates remain. |
| AndroidBackNavigationTest,19 cases | Common menus/appearance labels and child Apply/Discard. Retained-history departures explicitly save a fixture and clean it after the test. Async Pro reopen waits for the actual restored preview before export. | Every source-byte, sampled-pixel/crop, timing, viewport, selection, history and one-commit assertion retained. |
| AndroidExportRecoveryTest,3 cases | Options routes; restored draft's dirty Back now explicitly guards; restored native Collage saved through Save as. | Exact output MIME/dimensions/pixels, duplicate and canceled destination bytes, pending journal and restored native recipe checks. |
| CollageExportUiTest and CollageReopenUiTest,8 cases | Options owns opening/export; original slot assertion now compares the persistent Options control before/after load, then opens the actual Export action. | All original output pixels/format/dimensions/source checks, tile identity, relink, failed atomic-write bytes, exact saved document; same bounds comparison for the surviving common command location. |
| PhotoFirstUiTest,13 cases | Empty guidance points to Options; Pro uses its legitimate Add layer path; Collage opens via workspace menu; common Back semantics. | All numeric/document/tool ownership/grade/export-format checks; obsolete direct-action expectations map to their one Options entry and no Create collage duplicate. |

New tests cover actual dirty routes, delayed/duplicate picker delivery, save failure/rollback, owner-qualified recovery, interrupted text/shape/privacy/crop gestures, lifecycle interruption, paired Duplicate cancellation, narrow 320dp/200% text header interactions, and the default-quality GIF profile. Some focused custody tests exercise the actual controller/store directly; they are not claimed as end-to-end phone execution.

## B2 measurement and owner boundary

Lance reports the 2–5 minute delay occurred before choosing a save location, on an approximately 2–3 second clip with all options at defaults except trim. Phone/Android/source remain unknown.

The representative profile opens the existing 1080×1920 / 150-source-frame motion fixture, trims through the real UI to 2–3 seconds, retains Original dimensions/source timing/100% forward/neutral edits, and measures export activation to destination-picker launch. It verifies output dimensions, complete frame schedule/delays/duration/loop, independent decoded-frame samples and unchanged source hash. Its 360-second diagnostic bound is not a product acceptance target and does not replace the inherited 6s sampled-motion or 15s Save fixture gate.

Measurements distinguish preparation queue, processing, provider-copy and response/cancellation stages. Overlapping counters are not summed as disjoint time. No palette, dimensions, quality, source schedule or frame selection has been reduced. The first execution measures the following default-quality workload. Both lanes use 80 scheduled frames, trim 0–2665 ms, 1080×1920, and produce 995,576 bytes /2670 ms of GIF timing.

| Metric | API29 x86_64 | API35 x86_64 |
| --- | ---: | ---: |
| Export activation to picker | 20,932 ms | 26,262 ms |
| Pipeline | 19,119 ms | 24,853 ms |
| Source reader wall / calling-thread CPU | 15,199 /14,673 ms | 22,354 /21,718 ms |
| Palette / LZW including output | 2,523 /1,063 ms | 1,517 /895 ms |
| Provider copy after picker | 58 ms | 33 ms |
| Start UI acknowledgement | 178 ms | 121 ms |
| Decoded sampled mean channel error, first/middle/last | 2.757 /2.642 /2.642 | 2.155 /2.064 /2.079 |

**B2 prospective bounded experiment, recorded before its production patch:** the source reader dominates and its CPU time nearly matches wall time. Inspection shows Original-size sampling performs three absolute direct ByteBuffer reads per output pixel; the existing filtered-downscale branch already bulk-reads rows. Hypothesis: applying the same bounded row access to unfiltered sampling will remove per-pixel buffer overhead without changing any sample coordinate or color calculation. This is an attribution to the reader, not a claim that platform decode alone is slow.

The integration owner exclusively owns EditedVideoPreviewReader.kt and its focused equivalence tests for this experiment. Exit targets for the same fixture and settings on each named CI API: preparation-to-picker at most 10,000 ms AND at least 50% lower than that API's recorded baseline; source-reader wall at least 50% lower; exact scalar-versus-bulk decoded ARGB equality on representative native-size/rotation/reverse samples; unchanged complete GIF dimensions/frame schedule/delays/duration/loop and existing independent decoding/error thresholds; unchanged original source bytes. Preserve all inherited 6s motion /15s Save checks.

The added row cache must scale with source row width, not retained frame count; at the 1080-wide fixture it must stay below 16 KiB additional row storage. Existing 16 MiB reverse-picture cap and full-frame working buffers remain unchanged. Start/cancel visible acknowledgement target is 250 ms; cancellation of this local fixture should release the worker within 1,000 ms. Record actual measurements and explicitly retain any unmet target instead of changing it after the run. These are bounded CI-workload targets, not handset or universal supported-media guarantees.

F03 remains open until the applicable measured and owner handset evidence exists.

### Post-review changes included in final qualification

The bulk-row source reviewer found no production correctness issue and requested two stronger test details: include a neighboring reverse timestamp so the test consumes an actually retained packed picture, and use the strict less-than row-cache bound. Both were applied. The exact-pixel comparison covers selected native-size, rotated, reverse-cache, anamorphic and filtered frames; it does not claim every frame of every supported source.

The empty staging-directory regression additionally verifies both legacy/named Save and load paths, old bytes/identity, retained obstruction and cleared pending metadata. The shared reader preserves the healthy base without invoking destructive AtomicFile cleanup in that specific state; publication rejects non-file transaction occupants before changing bytes.

The first-run fixes retain the original method identities and non-UI assertions. Collision cancellation now asserts the entire canceled dialog is gone; held-pointer toolbar commands use the actual semantic click without introducing an invalid second DOWN; dialog actions are scoped to their modal; crop/relink and Pro completion wait for their actual state. The Pro hidden-save fixture now uses a distinct name inside the existing 28-character input limit. These changes do not alter content, quality or timing acceptance checks.

A separate source review checked the new actual-UI cancellation profile and its timing report. The canceled notice follows worker join and journal clearance; the test observes completion including dispatch/polling overhead, while worker-stop and visible-frame acknowledgement are logged separately. It cancels during frame preparation, not necessarily at an exact frame number. The final candidate and merge execution above supply the bounded runtime result.

The final targeted Help read confirms the changed Pro, GIF and Photo instructions use Options/Save project/Export and the Collage Help describes the common command location and independent tile workflow. This is source confirmation of the renamed-command documentation, not a separate UI execution claim.

## Delivery gate

The integrated candidate and merge JVM/API29/API35/signing checks and exact phone-APK verification above qualify this coherent B1 handset candidate. Its focused script follows. B2 numerical/actual-phone exits remain explicitly open; do not label the wave fully performance-complete. Workspace locks, R1–R11 completion, Video, bilingual final onboarding, website dependency, Play Console setup and public release retain their separate roadmap/owner gates.

## Focused handset confirmation after a qualified artifact is delivered

1. Open the same photo in Regular, visit Pro/Collage/GIF, and confirm Options remains at the top right with one New/Open/project/export/help location. Check larger text if used on the phone.
2. In a two-photo Collage, tap a tile and a margin once each to hide/show tools. Edit only one tile, try leaving with Apply and then cancel the parent departure; confirm both the child work and parent remain available.
3. Make a visible change in Regular, Pro and Collage. Try Back and a workspace switch. Cancel must keep the work; Save must finish before leaving; Discard must remove only the specified unsaved branch. A standalone tile-photo Save must still leave its parent Apply decision.
4. Save a named project, change it and Save again, then use Save as for a second name. Reopen both and confirm the intended edits and independent targets. Cancel a name collision and remain in the current work.
5. Repeat the reported approximately 2–3 second video trim with all other options at defaults. Note the approximate wait until the save-location picker, separately from the wait after choosing the destination. Open the completed GIF and confirm movement, crop/orientation and duration.
6. During a separate GIF preparation, use the visible cancellation action. Confirm the interface acknowledges it and reports completion only when the worker has stopped. Retained native edits should still be available.

These focused owner observations are distinct from emulator qualification. They do not grant workspace locks or release acceptance.
