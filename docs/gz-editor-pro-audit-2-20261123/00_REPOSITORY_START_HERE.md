> **Historical checkpoint — current route updated November 25, 2026.** Follow the [GPT-6.2 Sol Ultra successor handoff](../NEW_CHAT_HANDOFF_2026-11-25.md), [current ledger](../WORK_LEDGER.md) and [PR #37 / 0.12.11 Test evidence and focused phone script](../B1_WORKSPACE_VERIFICATION.md). Current task: read-only successor onboarding, then wait for Lance's ongoing GPT-6 Pro review. Original text below retains its dated evidence; its “current”, “next”, delivery, engineering and test commands are historical, not fresh instructions. Do not replay completed intake, question exchanges, builds or phone scripts. The [current review brief](../coordination/GPT_6_PRO_REVIEW_BRIEF_2026-11-25.md) supersedes older review prompts.

# GZ Editor Pro: Independent audit 2, Codex repository intake

**Read this first.** This is the full second independent audit originally delivered as `GZ_Editor_Pro_Codex_Audit_2.zip` on **November 23, 2026**, placed into GitHub as individual documents, source controls, evidence summaries, and synthetic media. The only files not duplicated in this Git tree are the **four already-published GitHub Actions receipt ZIPs**, with exact artifact links, sizes and SHA-256 checksums in [RECEIPTS_ON_GITHUB.md](RECEIPTS_ON_GITHUB.md). Rehydrate those under `evidence/receipts/` only when rerunning `proofs/verify_ci.py`. No app or workflow source is changed by this audit import.

## Exact code baselines

- **App under audit:** `MrSkyrimFan/GZ-Mobile-001`, v0.12.7 Test. Tested candidate `1825bcbaf6c98f7ceced44e2012f97c2f8128d3f`, merged application commit `14df73926c6f7063a488f84e367268168e8e5444`. Both point to source tree `709616c2560670e0f07617017a25372b33292bc9`.
- **Main checked at audit:** `9d55483820ec1dcc39ed8291d7cec84613fcece5`, which adds documentation only relative to the app merge. **Before changing code, Codex must verify the then-current main and avoid treating older findings as current if repaired.**
- **Automation:** final candidate run `45125499836` and merged-main run `45126868951` both passed their Android 10 (API29) and Android 15 (API35) suites with 135/135 each. This does not qualify the owner's phone or commercial release.

## Reading order

1. [Original audit overview](README.md), [repair order and acceptance criteria](FIX_ORDER.md), and [engineering prompt](CODEX_EXECUTION_PROMPT.md).
2. Detailed chapters: [Pro](chapters/10_PRO.md), [GIF](chapters/20_GIF.md), [Regular and Collage](chapters/30_REGULAR_COLLAGE.md), [Android/exports](chapters/40_ANDROID_EXPORT_AND_RECOVERY.md), and [history](chapters/50_VERIFICATION_HISTORY.md).
3. [Lab and runnable controls](LAB.md), [source and output examples](proofs/), [evidence summaries](evidence/), and [GitHub Actions raw receipts](RECEIPTS_ON_GITHUB.md).
4. The earlier repo [field guide](../gz-editor-pro-field-guide/00_START_HERE.md), [product requirements](../PRODUCT_SPEC.md), [release gates](../RELEASE_GATE.md), and [review handoff](../review-handoff-2026-11-23/README.md).

## Highest-priority cases

**PRO-01 and GIF-01:** saving edited B must not silently overwrite saved A. Reopen both after restart. **PRO-05:** a successful Pro save must decode on cold reopen, or the old healthy save must survive a rejected write. **REG-01:** an opaque privacy cover must still obscure the protected subject after Flip and export; compare independently decoded PNG pixels. **REG-03 / AND-01:** replacing work or returning from a destroyed picker must not silently lose or misdirect the user's edits.

The rest of the audit covers Pro stroke limits, panorama hit geometry, output sizing, GIF undo/storage/aspect/delay handling, crop rotation, Collage selection, delete confirmation, missing media, blur cancel/zoom, text graphemes, histogram intermittent failures, CI guard checks and device-only acceptance.

## Scope rules

- **Read-only audit package.** The documents and fixture files are findings, not applied repairs. A green host proof validates the oracle, not the app.
- **No change to the accepted UI layout or styling** without a demonstrated functional need. Keep Regular, Pro, GIF and Collage controls in their established places and preserve the 20 independent Collage tiles.
- No runtime AI, cloud, ads, tracking, subscriptions or microtransactions. The target is $2.99 one-time, offline.
- No silent changes to other Greenezone repositories, business website, accounts or release systems.
- Use actual app UI and save/reopen/export routes. Tests of helpers alone cannot certify end-to-end behavior.
- For failed or unresolved cases, preserve exact inputs, receipt IDs, logs, old/new commits, and whether the case is confirmed in code, reproduced on Android, verified by host control or still conditional.
- Video editing remains last in the product sequence, after owner acceptance of the existing workspaces.

**Start engineering from the current main, not this static audit snapshot.**
