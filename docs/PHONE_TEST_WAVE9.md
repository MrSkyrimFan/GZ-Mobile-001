# GZ Editor Pro 0.10 | Consolidated field pass

Use only the exact verified merged-main v0.10 debug APK. Back up any saved projects before reinstalling if the debug signing key differs. User previously reported collage fairly good; do **not** repeat every old slider.

1. **GIF from video:** GIF menu should immediately offer "Choose video". Pick an accessible local MP4 2–20 seconds or longer, trim to an interesting section <= 12 seconds, scrub start/end, preview. Confirm it uses actual moving frames, not one still photo.
2. Set frame rate 4 and 10 FPS, 50%/100%/200% speed, reverse, Loop, Fit/Fill, square/wide. Export a real animated .gif; reopen in gallery or a GIF-capable viewer and verify movement order, duration, crop, palette quality and no watermark. Save/reopen the GIF edit project; try a missing/deleted source only if convenient.
3. **Pro layers:** Start with a photo or blank canvas; Add layer → Image, Text, Shape, Brush, Color adjustment. Select via layer strip. Text must remain editable, shapes change type/color, brush strokes draw on photo, adjustment affects layers below. Reorder, Hide, Duplicate, Undo/Redo. Drag image/text/shape in Move & size.
4. Save and reopen the full Pro project. Verify each layer kind, position, opacity, blend, text, shapes, brush marks and grades; export PNG and JPEG. The original photo must remain unchanged. Inspect generated pixels and editability, not just UI responses.
5. **UX:** Confirm that one layer/tool is active at a time, dropdowns remain easy, tool labels fit your screen, canvas is not blocked unnecessarily, Quick guide helps, no crashes/lag and Back doesn't silently discard photo/collage session.
6. **Collage/Regular regression:** Two-photo collage with independent edits, save/export, black privacy bar, crop handles when controls hide, regular photo Looks and JPEG export.
7. No AI, accounts, network permissions, ads, watermarks, subscriptions or microtransactions.

Return **one** combined report with results for items 1–7 and screenshots only for actual defects. Full advanced professional tools/RAW/video timeline still need future engineering.

## Refined one-video landing and memory QA
- Opening GIF maker with no selected video should show **Select one video to turn into a GIF**, a single prominent **Choose video** button, and a discreet **Open saved GIF project** link. Photo-sequence creation must not clutter the first screen.
- During Pro brush strokes and layer drags, the loading spinner must not block the image. Repeated GIF scrubbing and Pro edits should not accumulate bitmap memory or crash. Test a sustained editing session before production sign-off.
