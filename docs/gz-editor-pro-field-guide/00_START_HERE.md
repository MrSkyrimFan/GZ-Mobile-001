> **Historical checkpoint — current route updated November 25, 2026.** Follow the [GPT-6.2 Sol Ultra successor handoff](../NEW_CHAT_HANDOFF_2026-11-25.md), [current ledger](../WORK_LEDGER.md) and [PR #37 / 0.12.11 Test evidence and focused phone script](../B1_WORKSPACE_VERIFICATION.md). Current task: read-only successor onboarding, then wait for Lance's ongoing GPT-6 Pro review. Original text below retains its dated evidence; its “current”, “next”, delivery, engineering and test commands are historical, not fresh instructions. Do not replay completed intake, question exchanges, builds or phone scripts. The [current review brief](../coordination/GPT_6_PRO_REVIEW_BRIEF_2026-11-25.md) supersedes older review prompts.

# GZ Editor Pro · Field guide for Codex

**Hey Codex.** The owner wants to sit down with a phone and make something. On the build that prompted this work, a video played until they edited it. Then the picture stopped. They had to fight the crop frame to move an edge. They encountered Masks in an editor where they had explicitly asked for it to be removed. Each of those experiences is a better starting point than the number of commits in a PR.

You already have authority to engineer this app. Carry a reported problem from the first touch to the finished file, fix what the evidence identifies, and keep going while there is work you can do. Use this guide to decide which observation would actually change your next engineering decision. The rendering algorithm, module boundaries, test harness, branch strategy, and size of the necessary repair are yours to choose. A full renderer change can be the right repair. Moving unrelated controls because the renderer changed has a cost to everyone who learned their positions.

## Where to pick up

1. **Match the live source to the evidence.** [The engineering checkpoint](01_ENGINEERING_CHECKPOINT.md) records the earlier crop, navigation and GIF recovery work, the merged GIF throughput repair, and the Regular histogram and live Pro paint changes. Check `main` and open PRs before starting a repair. Preserve demonstrated behavior and pursue the remaining failure or missing end-to-end observation.
2. **Finish the active user journeys.** For GIF that means a moving, accurately edited preview; useful scrub and cancel; a fully saved animation that plays independently and can be reopened. For live paint it means a stroke under an upper layer and an eraser that reveals exactly what is beneath, while the finger is still down, after release, after Undo, and in the exported image. For the histogram it means accurate displayed values before and after a real photographic edit. The new [known-answer media](fixtures/README.md) and existing repo fixtures cover different parts of those exercises.
3. **Advance independent work when an observation needs a phone.** Keep the field report open against its named build and source. Continue reproducible work on supported workloads, failure paths, preservation, and the next in-scope editor capability. When only a device-specific observation remains, offer one install and one focused task that can resolve it. A draft PR, a new test, or a faster decoder can all be worthwhile milestones without ending the repair.
4. **Use the rest of the plan as work matures.** Preserve Regular and Collage, close meaningful Pro and GIF workflows, and keep the sticker catalog deferred during the current repair work. The owner decides when all four are ready to lock. The separate studio Video workspace follows that lock. The bilingual welcome and agreements follow completion and testing of the entire editor, including Video.

## Choose the chapter for the job

| When you are deciding… | Open |
|---|---|
| What is already merged, what failed, and what the GIF numbers mean | [01 · Current engineering evidence](01_ENGINEERING_CHECKPOINT.md) |
| Which editor owns a tool and how still-image work should behave | [02 · Regular, Pro and Collage](02_STILL_WORKSPACES.md) |
| How to make edited GIFs responsive and what the later studio timeline must do | [03 · Motion editing](03_MOTION.md) |
| How touch, Back, file ownership, recovery, and offline operation fit together | [04 · Interaction and document contracts](04_INTERACTION_AND_DOCUMENTS.md) |
| What evidence supports a fix, a phone candidate, a mode lock, and a public release | [05 · Qualification and delivery](05_QUALIFICATION_AND_DELIVERY.md) |
| What experiment to run next and what would falsify the result | [06 · Verification matrix](06_VERIFICATION_MATRIX.md) |
| Which inputs and independent readings can be used immediately | [QA media and reader](fixtures/README.md) |
| Where the current implementation meets a reported symptom | [08 · Code map and diagnostic questions](08_WHERE_TO_LOOK_IN_CODE.md) |
| Where a screenshot, old project rule or sticker design came from | [Evidence index and references](07_EVIDENCE_AND_REFERENCES.md) |

The separately delivered private Rev 4 archive contains the original evidence, its inherited Rev 3.1 blueprint, and the 648 sticker concepts. This repository copy includes synthetic fixtures. See the [evidence index](07_EVIDENCE_AND_REFERENCES.md) for access to the historical material. An old status or instruction addressed to the engineer at an earlier commit describes that moment. The owner’s latest decisions govern the product. The checkpoint governs claims about its named revisions. The behavior chapters describe the intended product. Example designs and suggested measurements are options for demonstrating that behavior. This guide is an engineering companion, with no APK or product changes of its own.
