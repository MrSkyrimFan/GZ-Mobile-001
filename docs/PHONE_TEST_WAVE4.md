# Superseded by PHONE_TEST_WAVE5.md (0.6)

Historical v0.5 photo-first UI test. No retest required.

# GZ Photo Editor Pro 0.5.0 · ONE short user-experience phone test

This session is about **the interface redesign and regression of critical editing**, not testing fifty-six sliders one by one. Download the ZIP artifact \`gz-photo-editor-pro-debug\` from the verified merged-main Actions run, extract \`app-debug.apk\` and install as an in-place update to GZ Photo Editor Pro.

## Prepare two photos

One portrait (a person) and one landscape (sky/trees/buildings). Use real photographs already on your phone. Don't delete originals.

## Do one combined session

1. Open app, tap Open photo, select the portrait. The photograph should now occupy **most of the canvas**, not a miniature horizontal strip. It should remain proportional without stretching.
2. Tap the photograph once. All app editing controls should disappear; photo is unobstructed. Tap again and controls reappear. Pinch zoom and drag; tap Fit.
3. Tap Looks. The first choice is **None**. Swipe left/right through the actual-photo preview tiles, choose Golden, Cinema, B&W, and back to None. The large photo behind must visually change; there should be no giant stacked slider list.
4. Tap Light, swipe the adjustment names horizontally, choose Exposure then Contrast. Only **one** active adjustment slider should appear. Verify its name and numbers are readable (white/mint, not black-on-black). Move the slider and use Reset, Undo/Redo.
5. Switch to Color/Mix/Detail and check that category chips swipe left/right and one chosen slider changes the photo. No need to exercise every tool.
6. Test Curves by moving one RGB point. Test Masks: add Brush and drag finger to paint a local adjustment at Fit, then tap the photo to hide UI and tap to restore. Check drawing didn't accidentally toggle tools during the drag.
7. In Crop, rotate and straighten; return to Looks. Under More, save the editable project and reopen it. Confirm edits were retained.
8. Tap Export and save one JPEG; open it in your gallery. Confirm the original is unchanged.
9. Switch to landscape photo and check how much of the canvas is visible. Rotate phone if desired and confirm top/bottom controls remain reachable.

## Send back one report

- **How large does the photo feel now?** Good / still too small.
- Are **Looks swipe**, **tap to hide**, and **one-at-a-time sliders** comfortable? Pass/fail.
- Is any text still black or clipped? A screenshot if so.
- Did brush masks, save/open project and JPEG export still work?
- Any stutter, sudden crashes, overlapping controls or important missing navigation?

One concise report is enough. Do not send large logs. The product is still an engineering candidate, not commercial 1.0.
