# GZ Photo Editor Pro 0.3.0 — ONE integrated phone test

**Build:** Open the successful **merged-main** Android CI run and download its artifact **gz-photo-editor-pro-debug**. This is an unsigned/debug test APK, not the Google Play release. Because the package ID changed, Android may install it alongside the old prototype. Do not treat the old app as the current candidate.

**Prepare:** Four photos: (1) sunny outdoor scene with green/blue, (2) portrait with skin tones, (3) night/dark scene with lights, (4) 12–24 MP photo plus optional >24 MP panorama, and a transparent PNG if available.

## One complete field session

1. Install and open **GZ Photo Editor Pro**. Open outdoor photo through system picker.
2. Work Light, Color, Detail, Film and Color grade sliders. Touch each category's Reset control, compare original, undo/redo. Verify edits are actually visible rather than cosmetic controls.
3. In Color mixer test Green Hue/Saturation/Luminance on grass, Blue on sky and Orange on skin. Verify the chosen band acts more strongly on relevant colors.
4. Open Curves. Master RGB: change Midtone. Then Red, Green, Blue separately. Watch the curve graph change, and the image change appropriately. Reset each curve, undo/redo.
5. Open Masks. Add a radial mask, increase its Exposure and move Center X/Y; vary Size/Feather. Add a linear mask, move Center Y and Tilt and alter Contrast/Saturation. Add up to four masks, delete one, undo, redo. Look for clear changes in the intended region and unwanted seams.
6. Try all 12 looks. Make your own curve adjustment and save a named custom look. Open a second photo, apply the saved look, restart the app and verify it persists. Delete the selected saved look.
7. Zoom and pan; use Fit; toggle histogram; compare; rotate left/right; Flip H/V and every crop ratio. Reset edits and undo recovery.
8. Export **JPEG 85, JPEG 95, JPEG 100 and PNG**, each under a different file name. Open each saved image and inspect visible colors, size and orientation. Original image should remain intact.
9. Repeat the image-export, mask and curve parts on the night photo, a 12MP/24MP image and optionally a >24MP panorama and transparent PNG.
10. Try a few fast slider swipes, leaving/reopening the app and exporting repeatedly. Watch for freezes, stale previews, over-darkness, banding, frame-rate drops, out-of-memory errors, invalid output files or misaligned crop results.

## Report back once

Phone model + Android version, pass/fail for each section, approximate speed on large photos, whether JPEG and PNG open, any color/quality discrepancies and defect screenshots. No need to send giant logs. A successful debug build is NOT proof of real-device professional quality.
