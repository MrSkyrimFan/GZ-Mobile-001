# GZ Editor Pro 0.7 — ONE consolidated Android field test

Download the debug APK from the successful **merged-main GitHub Actions** artifact `gz-editor-pro-debug`. Extract `app-debug.apk` and install, retaining app data where Android signing permits. Keep existing photos untouched.

**Prepare:** one portrait, one outdoor photo and one high-resolution image.

1. **Layout and typography:** Open media and check GZ Editor Pro appears top-left. Choose **Text**, Add text, enter a line such as `GZ Sample 2026`, change among several fonts, adjust Size, Width, Height and Rotation. Drag the text directly on the photo; make it bold, change color, add outline/shadow. Hide the interface and move the selected text, then restore controls.
2. **Drawing:** Select Draw, choose red/blue, draw several lines across an unimportant photo, change brush size and opacity, use Eraser, and Undo stroke. Hide controls and draw again. Make sure the original photo remains untouched.
3. **Shapes:** Add a rectangle, ellipse and arrow. Change their color, size, rotation, fill, then drag one into position. Confirm you can delete only the selected shape.
4. **Manual crop:** Open Crop, choose Freeform, drag all four white corner handles and move the frame. Choose 4:5 and resize again; confirm the aspect stays fixed. Leave Crop to see the actual resulting image. Undo/Redo and straighten once.
5. **Export formats:** Tap Export. Choose PNG from the **Format** dropdown and Original from the **Platform** dropdown, save the image and open it in Gallery. Repeat JPEG and WebP. Save BMP/TIFF/PDF using My Files if the Gallery cannot display them.
6. **Social presets:** Choose Instagram Portrait (1080×1350) and Fit. Save, inspect that the whole photo remains visible with padding as needed. Repeat with Fill and confirm it crops the edges. Try TikTok 1080×1920. Verify saved dimensions in photo details.
7. **Persistence / regression:** Save an editable project with a text object and free crop, close/reopen the app and load it. Check the text/crop is still editable. Make one Blur & cover or Looks adjustment, then export without watermark.

**Return one report:** Did all seven sections pass? Note any clipping, oversized dock, slow drawing, distorted text, crop misalignment, wrong format, original overwritten, or app crash. Two or three screenshots of actual defects are enough. No giant logs.

This is one test of the entire wave. No microtesting every slider.
