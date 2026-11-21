# Focavera 0.2.0 — one-pass Android phone test

Install the APK artifact named `focavera-android-debug` from the successful merged-main GitHub Actions run. This is a debug **test** build, not the Play Store release. For a new package ID, Android treats this as a different app from the older ChromaVale test APK. You may need to enable your device's install-unknown-apps setting for the browser or file manager used.

## Prepare 4 images
1. A normal bright outdoor color photo with sky and grass.
2. A portrait with skin tones and a dark background.
3. A night photo with colored lights and shadows.
4. A high-resolution photo (ideally 12–24 MP) and optionally a PNG with transparency.

## One combined field session
1. Open **Focavera Photo Studio**, use **Open photo**, pick the outdoor photo.
2. In **Light**, change Exposure, Highlights, Shadows, Contrast, Blacks. Each should produce a visibly different preview; press ↺ on one slider, then undo and redo.
3. In **Color**, test Temperature, Tint, Saturation, Vibrance and Hue. In **Color grade**, add blue to Shadows and red to Highlights and compare the result. The effect should be concentrated in those tonal regions.
4. In **Color mixer**, pick Green and change Hue, Saturation, Luminance. Grass should change more than sky. Try Blue on sky, Orange on skin in a portrait. Test each band's three controls.
5. Try a dozen built-in looks. Save a custom look under a name, load a second image, apply the saved look. Fully close and reopen the app and verify the custom preset remains; then test Delete selected.
6. Pinch to zoom, drag to pan and tap **Fit**. Toggle the histogram. Tap **Compare** and **Show edit** and verify the two views differ after editing.
7. Rotate left/right, Flip H and Flip V, and scroll through all crop ratios. Reset edits and use undo to recover.
8. Choose **EXPORT PHOTO**, set JPEG 85 and save to a new name, then JPEG 95/100 and PNG. Open each saved file in the phone gallery. Confirm no original was overwritten and compare dimensions/colors visually.
9. Repeat with the portrait, night photo, PNG transparency, and large image. Record any hangs, crash messages, failed exports, color banding, quality loss, or slow slider response.

## Return one report
- Phone model and Android version.
- Whether the APK installed/opened.
- Which panels/features worked and which didn't.
- Estimated responsiveness (instant, under ~1s, several seconds) at 12 MP / 24 MP.
- Whether JPEG and PNG saved/opened, and any transparency or orientation problems.
- Screenshots of defects if helpful.

The release remains blocked until this physical-device check and missing production gates pass.
