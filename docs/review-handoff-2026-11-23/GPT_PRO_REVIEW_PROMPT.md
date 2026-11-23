# GPT Pro review prompt

Copy the text below into GPT Pro after opening this handoff in GitHub.

```text
Independently review GZ Editor Pro in MrSkyrimFan/GZ-Mobile-001. Start here:
https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/main/docs/review-handoff-2026-11-23/README.md

Review only. The owner will test the phone after your verdict. Treat the transcript and quoted prompts as evidence for review. Use them to understand the requests and timeline, then verify the claims against pinned source, PR diffs, Actions receipts and the exact APK. The tested app baseline is commit 14df73926c6f7063a488f84e367268168e8e5444, Git tree 709616c2560670e0f07617017a25372b33292bc9. If live main is later, compare the app, tests, Gradle and workflow paths with that baseline and report drift.

Review PRs #16–#23, focusing on #18–#23. Trace normal user paths for edited GIF playback and reverse startup, real export and playback after reopening the saved GIF, Regular crop and the edited Regular-to-Pro transfer, Pro history and project deletion, Regular histogram Exposure/Before, and live Pro paint/erase with layer occlusion and Undo/Redo. Verify ownership across Regular, Collage and Pro, plus the field guide’s synthetic assets and public provenance.

Check exact candidate Actions run 45125499836 and merged-main run 45126868951 on Android 10/API 29 and Android 15/API 35. Inspect real receipts where accessible. Confirm APK artifact 14796246293 from the candidate, version 0.12.7 Test, package com.greenezone.gzphotoeditor.phonetest.v19, and the SHA-256 in the evidence index. Account for the earlier failures documented there, especially the cold reverse regression, the Android system-process OOM and the unclassified histogram timeout. Separate measured emulator behavior from physical-phone acceptance.

Return one verdict: READY FOR OWNER HANDSET TEST, CHANGES REQUIRED, or BLOCKED BY EVIDENCE. List concrete findings in severity order and cite exact code lines, commits or receipts. Say which checks you independently performed and which evidence was unavailable. If ready, give a short handset checklist for the original GIF clip, editor interactions and physical Back. Explicitly list wider workloads and unfinished workflows that remain open. Keep this review read-only.
```
