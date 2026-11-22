# GZ Editor Pro 0.6.0 — ONE combined Android phone test

Only run this after the **merged-main** GitHub Actions build is green. Download the artifact **gz-editor-pro-debug**, unzip and install `app-debug.apk`. It's a debug test APK, not a paid release. If Android says signature conflict, uninstall the old debug installation first; that removes local projects/presets, not original gallery photos.

## Five-minute practical tour (not 50 separate slider tests)

1. **Launch app.** You should see **GZ Editor Pro**, **Open media** and only **© 2026 Greenezone Software LLC** in the lower-left. No advertising slogans. Open a normal photo; confirm big photo remains in background.
2. **Check UI.** Go to Effects or Light. Read Exposure/Reset labels. Reset must be completely visible. Move a slider fast and release: observe whether preview changes quickly and then becomes sharper. Tap the photo to hide/show controls.
3. **Check Looks.** Open Looks and swipe across the first few, then scroll far enough to see additional styles. Choose None, Golden, Noir, Fashion and back to None.
4. **Paint with hidden UI.** In Masks add Brush, tap picture once to hide controls, then drag across the photo. Tap to restore controls. The image should update after painting while the giant dock stays hidden.
5. **Privacy tools.** Open Blur & cover. Add a Blur region; drag its rectangle over something visible and set Strength high. Add Pixelate; compare. Add Black bar and drag it across an area; it must look completely opaque. Add Blur brush, hide controls and paint. Try Black brush and a solid-color cover.
6. **Save and export.** Save an editable project, close/reopen app and reload. Export one JPEG and one PNG, then open them in Gallery. Verify blur/black bars persist in output and original photo is unmodified. Black bar pixels must be solid, not a semi-transparent darkening effect.
7. **Report once.** Tell me whether UI clipping is gone, hidden painting works, Looks feel useful, preview responsiveness improved, JPEG/PNG exports were correct, and any crashes. Send 2–4 screenshots only for significant defects.

**Privacy note:** Soft blur/pixelation are visual effects, not secure information removal. For reliable covering use solid opaque black or color. No watermark should appear on any output.
