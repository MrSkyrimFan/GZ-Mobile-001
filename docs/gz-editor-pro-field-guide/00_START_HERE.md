# GZ Editor Pro · Field guide for Codex

**Hey Codex.** The owner wants to sit down with a phone and make something. On the build that prompted this work, a video played until they edited it. Then the picture stopped. They had to fight the crop frame to move an edge. They encountered Masks in an editor where they had explicitly asked for it to be removed. Each of those experiences is a better starting point than the number of commits in a PR.

You already have authority to engineer this app. Carry a reported problem from the first touch to the finished file, fix what the evidence identifies, and keep going while there is work you can do. Use this guide to decide which observation would actually change your next engineering decision. The rendering algorithm, module boundaries, test harness, branch strategy, and size of the necessary repair are yours to choose. A full renderer change can be the right repair. Moving unrelated controls because the renderer changed has a cost to everyone who learned their positions.

## Where to pick up

1. **Check what landed while this document was being written.** The recorded comparison starts at PR #19. GIF throughput has since merged through PR #20, and PR #21 now targets main with the Regular histogram and live Pro paint. Match the live SHAs to [the engineering checkpoint](01_ENGINEERING_CHECKPOINT.md). Carry successful work forward. On the photographed GIF recipe, retest edited playback and export separately. The #21 test import was already repaired. Read the corrected run, then follow the first behavioral failure or missing end-to-end observation.
2. **Finish the active user journeys.** For GIF that means a moving, accurately edited preview; useful scrub and cancel; a fully saved animation that plays independently and can be reopened. For live paint it means a stroke under an upper layer and an eraser that reveals exactly what is beneath, while the finger is still down, after release, after Undo, and in the exported image. For the histogram it means accurate displayed values before and after a real photographic edit. The new [known-answer media](fixtures/README.md) and existing repo fixtures cover different parts of those exercises.
3. **Advance independent work when an observation needs a phone.** Keep the field report open against its named build and source. Continue reproducible work on supported workloads, failure paths, preservation, and the next in-scope editor capability. When only a device-specific observation remains, offer one install and one focused task that can resolve it. A draft PR, a new test, or a faster decoder can all be worthwhile milestones without ending the repair.
4. **Use the rest of the plan as work matures.** Preserve Regular and Collage, close meaningful Pro and GIF workflows, and return to the queued sticker catalog. The owner decides when all four are ready to lock. The separate studio Video workspace follows that lock. The bilingual welcome and agreements follow completion and testing of the entire editor, including Video.

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

The enclosed Rev 4 archive contains the original evidence, its inherited Rev 3.1 blueprint, and the 648 sticker concepts. An old status or instruction addressed to the engineer at an earlier commit describes that moment. The owner’s latest decisions govern the product. The checkpoint governs claims about its named revisions. The behavior chapters describe the intended product. Example designs and suggested measurements are options for demonstrating that behavior. This guide is an engineering companion, with no APK or product changes of its own.
