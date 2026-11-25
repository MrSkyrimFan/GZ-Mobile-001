> **Historical checkpoint — current route updated November 25, 2026.** Follow the [GPT-6.2 Sol Ultra successor handoff](NEW_CHAT_HANDOFF_2026-11-25.md), [current ledger](WORK_LEDGER.md) and [PR #37 / 0.12.11 Test evidence and focused phone script](B1_WORKSPACE_VERIFICATION.md). Current task: read-only successor onboarding, then wait for Lance's ongoing GPT-6 Pro review. Original text below retains its dated evidence; its “current”, “next”, delivery, engineering and test commands are historical, not fresh instructions. The v22 delivery task was consumed and the owner subsequently supplied its phone report. Do not replay completed intake, question exchanges, builds or phone scripts. The [current review brief](coordination/GPT_6_PRO_REVIEW_BRIEF_2026-11-25.md) supersedes older review prompts.

# GZ Editor Pro — new-chat handoff, November 24, 2026

## START HERE — immediate owner request

The owner asked to move to a fresh ChatGPT chat at the end of the completed PR #31 repair wave. **First response in the new chat must deliver the exact 0.12.10 Test APK as a fresh direct download and a concise phone-test checklist.** Do not start another repair, bump the version, rebuild the APK, repeat settled CI, reopen the superseded 0.12.9 review or begin R1.1 before addressing this delivery request. The owner has not yet reported the handset results for 0.12.10. Do not infer that the owner accepted the build or any entire workspace.

Repository: [MrSkyrimFan/GZ-Mobile-001](https://github.com/MrSkyrimFan/GZ-Mobile-001). Use the authorized GitHub connector, not a public-web search for private sources. The repository is the primary engineering workspace. This handoff is a navigation and delivery checkpoint, not a second work ledger or replacement roadmap.

Read in this order:
1. [AGENTS.md](../AGENTS.md) — authority, engineering and scope rules.
2. [CONTINUITY.md](CONTINUITY.md) and [WORK_LEDGER.md](WORK_LEDGER.md) — single active state and next task.
3. [ERASER_REPAIR_VERIFICATION.md](ERASER_REPAIR_VERIFICATION.md) and [PR31_0.12.10.json](qualification/PR31_0.12.10.json) — exact repair, test, binary and receipt evidence.
4. [GZ_ENGINEERING_ROADMAP.md](GZ_ENGINEERING_ROADMAP.md), [CAPABILITY_MATRIX.md](CAPABILITY_MATRIX.md), [PRODUCT_SPEC.md](PRODUCT_SPEC.md) and [RELEASE_GATE.md](RELEASE_GATE.md) — complete intended scope, 77-family inventory, requirements and gates. Read the actual documents; do not substitute a generic photo editor plan.
5. [PR #29](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/29), [PR #30](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/30), [PR #31](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/31), [PR #32](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/32) — historical repair and current qualification chain. Earlier [Audit 2](gz-editor-pro-audit-2-20261123/00_REPOSITORY_START_HERE.md), [field guide](gz-editor-pro-field-guide/00_START_HERE.md) and [November 23 handoff](review-handoff-2026-11-23/README.md) retain their original evidence and context, not revived instructions.

## Exact APK — recover and deliver before anything else

**Correct file:** GZ_Editor_Pro_0.12.10_Test_PR31.apk. It is the extracted APK actually exercised by the merged API35 job, not the independent verify-job APK, an earlier candidate or a newly compiled binary.

| Field | Required value |
| --- | --- |
| Launcher | GZ Editor Pro 0.12.10 Test |
| Version / code | 0.12.10 / 22 |
| Package | com.greenezone.gzphotoeditor.phonetest.v22 |
| Android | min SDK 29 (Android 10+); target SDK 36 |
| APK bytes | 32,899,125 |
| SHA-256 | ed362e928db9db1f3f37c7a82c99141d64af42db1112ed8eacbdec6f466dfb80 |
| Signature | Android Debug, APK Signature Scheme v2 verified |
| Qualified application merge | 3ad65c533f7667a136d4fa1838afcc06d3f484da |
| Merged workflow | 45483297512 |
| APK artifact | 14963127303, gz-editor-pro-phone-test |
| Artifact archive SHA-256 | ab5ad668a034decb54866c3059f0f7d45c48f2bb31220b25d1356be3cb7d8c94 |

[Exact source artifact ZIP](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45483297512/artifacts/14963127303) contains app-debug.apk. [Merged workflow](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45483297512) verifies all three jobs. Artifact expiry/access can change; check rather than assuming.

### Reliable direct-download procedure

- The exact extracted APK is saved in the owner's ChatGPT Library at root under the filename GZ_Editor_Pro_0.12.10_Test_PR31.apk. Its Library record and 32,899,125 bytes were confirmed during this handoff; it was materialized into the current container and SHA-256 and ZIP CRC were independently checked again, matching the required binary. The owner-pasted new-chat prompt may include its exact private file ID. Do not store account-specific Library identifiers or temporary signed URLs in this repository.
- In a fresh chat, locate the exact Library record (list/search the Library by filename or use the supplied file ID) and use the supported raw-file materialization capability to put it under /mnt/data. Verify the returned actual path, file size and SHA-256 against the table; test APK ZIP CRC. Rename/copy only after verifying the actual bytes. Use the verified path in a fresh clickable sandbox download link. **Never reuse the old chat's sandbox path without confirming it in the new session.**
- If Library materialization is unavailable, use the authorized GitHub artifact-download action with repository MrSkyrimFan/GZ-Mobile-001 and artifact ID 14963127303. Retrieve the ZIP bytes via the returned supported file reference/download capability, verify its archive digest and CRC, extract only app-debug.apk, verify APK byte count/digest and CRC, then expose the verified APK at a current-session path. Do not assume binary fetch is available via ordinary GitHub text fetch. Do not provide a temporary signed connector URL as a durable permanent link.
- If neither path can materialize the binary, report the actual blocker honestly and give the exact GitHub artifact link above (ZIP; owner extracts app-debug.apk), or the verified Library file card/link when available. Do not fabricate a direct APK link, issue a substitute build or claim a ZIP is an APK.
- Version v22 installs alongside v21 and other versioned test apps. It has separate private project storage; it does not automatically move projects from earlier test apps. Preserve old installations and originals. This is a debug phone-test build, not a Play release, same-ID upgrade qualification or full AAA certification.

## Phone test instructions to give the owner immediately

One combined focused handset pass; no manually constructed 160-stroke fixture (automation already covers this). Tell the owner to report Android version/device, exact steps and output size if anything fails, especially data loss or incorrect output.

1. **Original GIF clip:** open the original portrait video which previously stopped playing. Confirm normal playback, adjust both trim handles, scrub rapidly, apply Reverse and B&W, adjust Exposure, and Play again. Check actual moving picture, direction, framing, duration, responsive controls; time Save; reopen the actual exported GIF in this app and another viewer. Report the selected output size. The measured CI fixture is 180×320; larger workloads are not guaranteed to meet its time.
2. **Regular Draw/Eraser:** draw a long bent line. Erase across its middle and confirm the visible gap while both surviving sides remain. Undo/Redo, save a named project, reopen, export PNG, and inspect the result. Cancel an erase gesture and confirm no unwanted edit/history entry. Existing Text, Shapes, Blur & cover, tools and layout should stay familiar.
3. **Collage independence:** use the same photo in two tiles with different edits. Draw and erase in just one selected tile, return with Done, reorder and Undo/Redo, save/reopen/export. Other tile must retain its own edits. Cancel a selected-tile gesture and confirm parent and sibling remain unchanged.
4. **Retained repairs:** apply opaque privacy cover then manual Crop/flip and export PNG; check crop-handle acquisition and canceled long Draw. In Pro try long lasso, apply a mask, check history and save/reopen. Try Regular → Pro Yes (current edited appearance) and No (empty Pro) separately, then return to Regular and verify source, edits and history remain intact. Named saves and originals should survive.
5. **Physical Android Back:** both screen edges, canceled half gesture, rapid double Back; active tool drafts, menus, keyboard and Android save picker. One committed gesture closes the current transient context or pops one appropriate route without unintentionally discarding work. Real process reclamation while an external picker is open is a separate evidence requirement, not established merely by rotating the activity.

## Work completed and why the previous build was held

### Prior baseline and R0 (PRs #26–30)

PR #26 app merge 372e0e947caacb2ceaf603bc4ff5a60e44cc33ec passed 174/174 on API29 and API35. PRs #27/#28 reconciled the earlier review and engineering guidance. The independent PR #26 review identified four defects; PR #29 fixed them with actual workflows:
- D01: fresh privacy cover remains anchored to source pixels through manual Crop, held and quick gesture ownership, Undo/reopen and output.
- D02: Regular save/export recipe bounds are checked before replacing healthy saved data or opening destination picker; rejection preserves dirty work and previous save.
- D03: long Draw captures late bends and endpoints within 500 points; cancellation restores prior work.
- D04: long Pro Lasso captures the complete contour within 450 points; cancellation, mask application, history and persistence verified.

Also corrected bounded legacy Pro JSON read allocations and the observed Collage test scheduler fault. PR #29 retained 174 prior Android cases and added 12, for 186/186 each API on final candidate and merge. Original failing run 45348806687 and first repair 45349823052 (182/184 each API, two late-corner failures) remain in evidence. Final candidate 6ad22e3fabe3964a662c7e0e39bd08c4a8e8f9ca, run 45352950946; app merge c9efc5aa0673f21b98f150733582a24996b34d10, run 45355074845. PR #30 documentation merge e04bc08a4800233b0f407b419a91c97ca2c068f6 retained identical application inputs, established the single next R1.1 durability item and recorded 0.12.9 as that historical checkpoint.

The owner supplied a further read-only independent review of PR #29, verdict CHANGES REQUIRED despite genuine 186-test receipts. It found two Eraser counterexamples. The owner explicitly instructed this chat to repair them directly in the repository. The former read-only hold was lifted for engineering. The two failures justified reopening ONLY affected behavior:
- R29-01 (pre-existing): splitting a stroke in a 160-stroke document may produce 161, silently dropping the last unrelated stroke in render/save. Later rejection may retain partial prior erasure.
- R29-02 (interaction with new Draw sampler): old Eraser tests vertices, not long visible segments between retained points; it can pass through visible artwork without erasing it. Regular and selected Collage tiles share that route.

### Current correction (PR #31)

Fixed both defects in production, not by deleting tests. InkMath.kt clips visible segments and swept circular pointer paths, including rendered half-width and short-edge aspect geometry. PhotoEditorScreen.kt applies a bounded transaction at every sample: first or later capacity refusal restores the complete pre-gesture document, dirty work and history, clears transient gesture state and reveals the existing notice. EditDocumentCodec.kt rejects invalid ink cardinality before encoding fields. Existing 160-stroke and 500-point budgets, valid legacy recipes, unrelated artwork and independent Collage tiles are preserved.

The unchanged-app reproduction commit 9c17cc3d42b390e8cae18f5175d30be66a44fd50 (run 45471163485) failed exactly three new JVM cases and five new API35 Eraser cases; all 186 original API35 cases passed. API29 separately aborted at the existing rotation test, before Eraser cases, due to worker-thread Compose applyChanges via legacy test scheduler. Changed only the rotation rule import to junit4.v2; gestures and assertions intact, no speculative production lifecycle change. Do not mislabel that abort as API29 Eraser reproduction.

Candidate 1 fe2a3054082fbcc5edda054ec0a259e097291781 (run 45473953140): JVM/build/sign passed; all five Eraser cases and rotation passed both APIs. API35 191/191; API29 190/191 due solely to portrait GIF Save taking 28.331s against the unchanged 15s gate. The output itself passed dimensions, all delays, reverse endpoints and reopened playback. This candidate was held, not retried unchanged or released.

Corrected a demonstrated inefficient GIF calculation: B&W clarity disabled the existing ARGB-only export cache. ImagePipeline.kt now uses an exact bounded per-export key (sourceARGB, computedDetail.rawBits), preserving the original math. Coordinate-dependent grain/vignette stay excluded. Cache is confined to one export, 16,384 entries, about 208 KiB array payload. GifEncoder.kt and VideoGifSource.kt add stage timings. Independent host comparison matched 5,068,800 pixel calculations; two actual Android bitmap-equivalence tests compare cached and uncached outputs. Prior timing counters cannot retrospectively assign the entire failed interval to a single cause. No quality reduction, frame schedule change, timeout relaxation or preview-routing change. Existing texture-cache-null assertion was replaced with stronger pixel-equivalence coverage because the supported path intentionally changed.

Final candidate aa86c37c64c5e395d5378df281a0c7583a39d0e0, qualified app merge 3ad65c533f7667a136d4fa1838afcc06d3f484da, identical tree 1c7e3007204192ea240a7fd8d65cbd00cf7f9511. Candidate run 45480001019 and independent merged-main run 45483297512 each pass JVM/build/signing, Android 10 193/193, Android 15 193/193, no failures/errors/skips. All original 186 test identities plus five Eraser and two cache tests. Raw receipts, digests, individual identities and logs checked. The source repair and failure history are in the [PR #31 qualification record](ERASER_REPAIR_VERIFICATION.md).

| Edited portrait fixture | Candidate Android 10 | Candidate Android 15 | Merged Android 10 | Merged Android 15 |
| --- | ---: | ---: | ---: | ---: |
| Save through UI completion | 4.159s | 4.580s | 3.872s | 3.334s |
| Five distinct sampled pictures including initial still | 1.129s | 1.661s | 1.393s | 1.525s |

Fixture: 1080×1920 source, edited trim/Reverse/B&W/Exposure/scrub; 180×320 GIF, 88 frames, duration 2.950s; all delays and reverse marker pixels independently decoded, then reopened and played to last frame. Six-second sampled-motion and fifteen-second Save gates preserved. Five-picture measurement includes paused initial picture; not proof of edit-to-first-correct-still latency or full-rate playback. Original handset media and physical behavior remain unverified.

[Candidate CI](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45480001019), [merged CI](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45483297512), [merged Android10 receipts](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45483297512/artifacts/14963562392), [merged Android15 receipts](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45483297512/artifacts/14963142239).

PR #32 docs-only merge ebf5158f311d794e3a9ebdf16bf5fa4260e36389 updated nine documentation files and added the machine-readable receipt. All application/test/Gradle/CI inputs identical to PR #31 app merge; no new tested binary and no redundant CI for docs. Main at the start of this new-chat handoff was verified at that commit; any handoff-only successor changes documentation, not qualified app inputs. Check current live main before future work.

## Boundaries and subsequent engineering

- Existing image-first layout/style and separate workspaces are protected. Necessary functional deltas must have demonstrated reason. PR #31 retains layout/style, Pro production, workspace ownership, preview routing, schemas, dependencies, CI. Capacity refusal reveals the existing controls and notice; no redesigned screen. No new permissions or runtime AI.
- Greenezone Software LLC, Android 10+, $2.99 one-time target. Fully offline, no AI/cloud/accounts/ads/subscriptions/IAP/telemetry/watermarks. Preserve source originals, independent 1–20 Collage tiles, old project formats, history, export fidelity. Regular has Text but no new Masks authoring; Pro has layers/masks/selection but no Text authoring; legacy records still render.
- Do not change website, DNS, Play account, XForge, GZFX or any other repo. Video waits until the owner explicitly accepts all four pre-Video workspaces; English/Spanish welcome/help/legal onboarding after all five editing workspaces. Play Console setup only after finished app AND Greenezone website. No production release authorized.
- R1.1 is next after the requested APK/checklist delivery: bounded saved-project listing/loading using lightweight metadata and lazy selected-recipe decode; qualify twelve near-budget saves and corrupt/failed entries, correct selected identity, unchanged healthy bytes and measured memory. Then R1.2 recoverable Regular drafts, with completed edits atomically persisted and transient gestures excluded; true process-loss restoration. Preserve previous evidence, do not restart intake.
- Remain within the single ledger and roadmap: R1 durability, R2 Pro compositor/layer foundation, R3 Pro tools, R4 offline subject/background/warp, R5 Regular RAW/color/photo development, R6 catalog/batch/creativity/stickers (500+ usable rights-cleared assets), R7 GIF/Collage completion, R8 pre-Video qualification and four explicit locks, R9 Video, R10 final bilingual onboarding, R11 website dependency and release. These are not completed families or a percentage estimate.
- Physical phone acceptance, actual OS process loss, OEM/provider behavior, sustained thermal/performance, original clip, broader memory/device tiers, prior legacy Pro 16 MiB condition and legacy v4 GIF framing migration remain open. R14 and G08 are PARTIAL, not complete. Do not claim commercial readiness from emulator counts.

The owner wants direct action, no repetitive questions, no circles, no arbitrary UI changes. Continue authorized engineering only on the recorded task or named new owner finding; exact-source candidate/merge regression qualification is required for app changes. Document failures and limitations transparently. The immediate new-chat task is delivery of the verified APK and test instructions, followed by the owner's results.
