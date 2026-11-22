# SUPERSEDED by PHONE_TEST_WAVE4.md (photo-first UI)

Historical v0.4 field guide only. Do not ask the user to repeat this test.

# GZ Photo Editor Pro 0.4.0 — ONE complete Android phone field test

This test is intended to cover the entire major engineering wave at once. We are **not** doing twenty tiny one-slider tests. First run all source and Android emulator CI. Download the `gz-photo-editor-pro-debug` artifact from the green **main** GitHub Actions run, unzip it, then install the `app-debug.apk`. This is a debug-signed engineering APK, not a commercial Play Store release.

## Prepare 5 existing photos

- **A. Outdoor/daylight:** blue sky, greenery, bright highlights.
- **B. Person/portrait:** skin, dark background and fine hair detail.
- **C. Night:** dark shadows, headlights or colorful light.
- **D. High resolution:** one 12MP/24MP photo, plus a >24MP panorama if available.
- **E. Transparent PNG:** if possible, an image with a transparent background.

## One session, steps

1. Launch GZ Photo Editor Pro; pick **A**. Check preview is correctly oriented and doesn't show black wedges.
2. Test Auto tone and Auto WB, then Undo/Redo and Compare. Check the image is not drastically darker or oddly tinted.
3. In **Light** change Exposure, Shadows, Highlights, Contrast. In **Color** change Temperature and Saturation. In **Color mixer** change Green and Blue Hue/Sat/Luminance. In **Color grade** change Shadow Blue, Highlight Red.
4. In **Detail**, change Noise reduction, Texture, Sharpen and Sharpen threshold while viewing fine textures on **B**. Check for water-paint smearing, halos, or excessive grain. Test Film's Grain and Vignette.
5. In **Curves** adjust the RGB middle point, then Red, Green and Blue individually, compare and reset. Verify graph and photo agree.
6. In **Masks**, add **Radial** and adjust its center, radius, feather and exposure. Add **Linear** and change Tilt/Center Y. Add **Brush**, then **drag your finger across the photo** to paint. Adjust brush size/soft edge and exposure. Try Undo brush stroke and full Undo. Brush painting works best with Fit zoom (1.0×) and edit view, not Compare.
7. In TRANSFORM rotate left/right, flip H/V, set crop to 1:1 or 4:5, change Reframe Horizontal/Vertical, then Straighten to around +7°. Check no transparent corners, center shift and crop are coherent.
8. Apply several built-in looks. Save a named custom look and verify reopening the app preserves it. Save an **editable project** with a non-neutral curve, brush mask, crop and straighten. Fully close and reopen app, go to **Projects**, load the project and confirm edits restored. If the original provider revoked access, report the exact error.
9. Export A as **JPEG 85**, **JPEG 95**, **JPEG 100** and **PNG** using distinct filenames through Android's save picker. Open each in Photos/Gallery. Confirm they are nonempty, correctly oriented, and the original is unchanged.
10. Repeat key color, mask and export checks using B, C and D. Try E exported as JPEG (transparent area should turn white) and PNG (alpha should stay transparent). On D record original vs output pixel resolution, and roughly how long export takes.
11. Stress check: fast slider swipes, 3 exports consecutively, app background/return, and switch between photo A/B. Look for frozen or stale previews, crashes, thermal issues, distortion or saving failures.

## Bring back one short report

- Phone model and Android version.
- Did it install/open? Can it save and reopen a named project?
- Controls/masks/curves/presets: pass or list of failures.
- JPEG/PNG: saved/opened/alpha/rotation okay?
- High-resolution image size in vs size out; estimated export seconds.
- Any significant artifacts or slowdown, with 2–4 screenshots if relevant.

No massive diagnostic logs needed. This app is not ready for a paid Google Play launch until the field report and remaining release gates are addressed.
