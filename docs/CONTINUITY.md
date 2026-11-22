# GZ Photo Editor Pro · continuity after photo-first redesign (2026-11-21)

**Approved name:** GZ Photo Editor Pro, Greenezone Software LLC. All prior invented names ChromaVale/Focavera are rejected. $1.99 once, on-device, no AI, ads, IAP, subscriptions, accounts, analytics, internet. Never deviate without an owner decision.

**Repository:** \`MrSkyrimFan/GZ-Mobile-001\` main baseline v0.4.0 merge \`1c46465d996f989c25b48f4df109164ab21d52d1\`. v0.5.0 branch \`feature/photo-first-ui-05\`; must finish CI/PR/merge/main verification before classifying as a valid Android APK.

## User field evidence and intent

Three uploaded phone screenshots from 2026-11-21 show **unacceptable UX**: minuscule horizontal preview, huge vertical sliders, illegible black label text on dark surface, messy horizontal tabs, excessive occupied space. User explicitly demanded full-screen photo background, horizontally swipeable filters beginning at None, one selectable tool at a time, tap photo to hide all UI / tap to restore. This is not optional, and was more important than another random feature batch.

## Engineering decisions

- Preserve trusted 0.4.0 image engine, 56 global controls, mask/curve/project/preset history and build tools.
- Replace \`ui/PhotoEditorScreen.kt\` with image-first canvas; add \`ui/EditorDock.kt\` for compact horizontal filter/tool navigation, add \`ui/EditorDialogs.kt\` for saving/exporting dialogs; wire \`ui/BrushOverlay.kt\` to handle photo taps while brush mode is selected.
- Remove obsolete \`ui/EditorExtras.kt\`.
- Full canvas image can be tapped to hide/show controls. Top minimal overlay contains open/undo/redo/before/export. Bottom dock over image shows Looks/Light/Color/Mix/Grading/Detail/Effects/Curves/Masks/Crop/More, one slider at a time. Looks thumbnails use original photo and are computed on background thread.
- Same \`com.greenezone.gzphotoeditor\` package. Bump version 0.5.0 / code 5.
- Expand Android emulator Compose tests for first open, Looks None-to-Golden selection, and one-slider-per-tool behavior.
- One consolidated on-phone validation of photo size, filter swipes, tap-to-hide and touch controls after green merged-main CI. **Do not burden user with micro iteration.**

**Boundaries:** This Android repo must not touch Greenezone corporate website, registrar, DNS, XForge, GZFX, Beside-Me or other GitHub repos. The future Greenezone website was deferred, with general About/Contact/Privacy/Products structure; exact hosting details are not proven by this app repo. Do not assume them.

**Proof policy:** Compiler + JVM + Android emulator + artifact + on-phone field evidence are separate levels. Do not claim commercial release-ready based on green CI.
