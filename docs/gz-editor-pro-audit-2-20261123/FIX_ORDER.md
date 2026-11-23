# Fix order and acceptance matrix

Start with the potential loss of a user's saved work and the privacy of their output. Work touching unrelated components can run in parallel. An engineering milestone may close a specific finding; name it and continue the other reproducible work. The ordering below is a recommendation, not a dependency between every item. Use the existing workspace menus and the product's agreed ownership.

## First repair group

| Priority | Journey | Closure evidence |
|---|---|---|
| High · [PRO-01](chapters/10_PRO.md) and [GIF-01](chapters/20_GIF.md) | Save A, open B, edit B, accept the default Save. Follow Undo across Open where supported. | Relaunch. A has its prior recipe and pixels. B has the new ones. Deliberate replacement and canceled replacement work. Exercise workspace switch and each reachable GIF project route. |
| High · [PRO-05](chapters/10_PRO.md) | Replace a known-good disposable Pro project with a dense, model-valid drawing near the nested recipe limit. | Reported success guarantees a successful independent reload with matching content. A rejected write preserves the previously readable project. Repeat with and without a reset baseline and with an older file. |
| High · [REG-01](chapters/30_REGULAR_COLLAGE.md) | Cover the marker, flip, export a PNG and reopen. | External pixel checks confirm the moved subject is still covered, orientation changed and unrelated pixels survived. Undo, redo, named reopen and handoff retain protection. The standalone marker is safe to share. |
| High · [REG-03](chapters/30_REGULAR_COLLAGE.md) | From dirty Regular or Collage work, open another source or named project. | An accidental switch cannot destroy the only recoverable copy. Exercise cancel and successful replace through the existing UI; keep the working Back and tile-Apply paths. |
| High when the host is destroyed · [AND-01](chapters/40_ANDROID_EXPORT_AND_RECOVERY.md) | Pick a destination, destroy the app host, return from the picker. | Produce the correct file once or recover the correct draft with an explicit retry. A stale result must never write a different source, and partial-write failure must never report success. |

## Subsequent engineering work

| Finding | User-visible test to run |
|---|---|
| [COL-01 / COL-03](chapters/30_REGULAR_COLLAGE.md) | Distinct A/B/C tiles, reorder → Undo → edit. Check which tile's pixels change. Open B with missing media after previewing A; the preview and hit targets must describe the same layout. Verify all 20 independent tiles after save/reopen. |
| [COL-02](chapters/30_REGULAR_COLLAGE.md) | In Regular and Collage, choose a disposable named save and exercise delete/cancel. Check both the named and currently open work. Pro already has a confirmation path. |
| [REG-02](chapters/30_REGULAR_COLLAGE.md) | Off-center free and locked crop → rotate → export. Compare against rotating an already-cropped reference, including bounds and markers. |
| [PRO-02 / REG-04](chapters/10_PRO.md) | Draw a long stroke reaching beyond its old cutoff; erase on a covered lower layer; cancel a separate gesture. Test both live and committed pixels, one-step Undo and reopen. Regular has its own zoom, mapping and cancellation issues. [Regular details](chapters/30_REGULAR_COLLAGE.md) |
| [PRO-03 / PRO-04](chapters/10_PRO.md) and [AND-02](chapters/40_ANDROID_EXPORT_AND_RECOVERY.md) | Test a 40:1 panorama, an 80×80 handoff and a 24 MP Regular input. Check guide/hit/retouch coordinates, the displayed planned dimensions and the externally decoded PNG dimensions. |
| [REG-05 / REG-06](chapters/30_REGULAR_COLLAGE.md) | Use a caption with joined characters and positive spacing. For the histogram, record the actual gesture value, preview identity and bin counts when the real Exposure/Before sequence fails. Preserve the 10-second failing receipt. |
| [GIF-02 / GIF-03](chapters/20_GIF.md) | Undo the first import, recover it with Redo, and manage 12 disposable named projects. A user should be able to reclaim a slot while preserving other work. |
| [GIF-04 / GIF-05](chapters/20_GIF.md) | Decode the round moving marker in both sample-aspect videos. Edit the two-frame long-hold GIF and reopen it. Verify the image's shape and timing with independent tools. Keep device-dependent cases labeled. |
| [QA-01](chapters/50_VERIFICATION_HISTORY.md) | A controlled obsolete brand alone should fail its CI check. Preserve permission and package checks and verify the precise handset artifact after build. |

## A return package the owner can trust

1. Record the exact starting and replacement SHAs. For changed behavior, retain a red observation and a green observation on comparable supported Android configurations. Use existing automated suites as regression protection. When an issue cannot be reproduced, describe the precise input, device, paths and counters examined and what is still unknown.
2. Test the normal UI save and open paths. To prove project isolation, reopen both A and B after restarting. To prove an export, inspect the final file's pixels, dimensions, alpha or delays with a reader independent of the feature. For latency, time the first visibly changed picture from the actual touch and measure the cold waiting period separately. A decoder callback occurs earlier than screen presentation.
3. Re-run Android 10 and 15 suites on the candidate and relevant merge. Attach the exact handset artifact's job, package/version, APK hash and signature, the raw result archives, and the change list. Expect test counts to grow as specific regressions are added. A frozen count of 135 is useful only for comparing the archived baseline.
4. Once the reproducible safety issues are addressed, the short phone pass should use **a duplicate of the owner's original clip**. Play it unedited, trim, reverse, grade, scrub quickly, play to the end, save and reopen in GZ and an external viewer. Compare neutral output with the source, then the edited result. Try Regular crop handles and flip-after-cover, a cropped handoff into Pro, project A/B saves, lower-layer brush/erase, a small histogram Exposure change, and the Android Back gestures in chapter 40. Record phone model, OS, source dimensions/codec, output size and the first action that failed. Reproduce remaining device-only failures without asking the owner to repeat tests already settled in software.

## Product commitments during repairs

Regular owns photographic edits, text, Blur & cover and later stickers. Masks belong to Pro; old masks still render in existing Regular and tile recipes. Collage owns its independent 20-photo layout and tile edits. Pro owns layered image manipulation, masks and retouching; preserve legacy Pro text rendering and current authoring boundaries. GIF owns timing, reverse, motion-specific looks and its editable projects. A photo can move from Regular to Pro with committed edits and return to its recoverable Regular draft.

Keep the present layout and styling. Useful repairs may involve substantial changes inside rendering, storage or state management. Share only behavior proven equivalent across workspaces. A loss of one private save, or an exposed region in an exported photo, matters more than a higher test count.

Professional RAW/color/optics, catalog and batch work, Pro groups and Subject & Background remain product work. The earlier sticker inventory is 648 design concepts; final offline artwork, custom imports and implementation are separate deliverables. Studio Video follows the owner's acceptance of earlier workspaces. English/Spanish onboarding and legal agreements come after the full editor, including Video, is built and tested. The owner retains the workspace and release decisions. [Pinned product spec](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/14df73926c6f7063a488f84e367268168e8e5444/docs/PRODUCT_SPEC.md) · [Field guide](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/14df73926c6f7063a488f84e367268168e8e5444/docs/gz-editor-pro-field-guide/00_START_HERE.md)
