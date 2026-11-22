# GZ Photo Editor Pro · UI/UX redesign · 0.5.0

## Evidence

The user installed and ran the v0.4 Android debug APK on a Samsung phone and provided three screenshots on 2026-11-21. Verified defects: photograph restricted to a short landscape preview; very long vertically scrolled adjustment lists; unreadably dark labels on a dark background; filter/tool discovery inaccessible; excessive occupied UI height. User explicitly requested a large image in the background, swipeable filters beginning with None, and tap-image-to-hide UI.

## Implemented UI changes

- New image-first \`PhotoEditorScreen\`, new \`EditorDock\`, new \`EditorDialogs\`. Obsolete vertically stacked \`EditorExtras\` UI removed.
- Entire phone-sized canvas holds image, with proportional Fit scaling; pinch/drag still available. Full image can be viewed without editing panels using single tap.
- Top minimal overlay actions: Open, Undo, Redo, Before/Edited, Export; visible status messages and zoom Fit.
- Bottom translucent dock: swipeable category chips; each category shows a short horizontal list of **tools**, never a long column.
- Looks deck starts at None, followed by original color-presets and named local looks. Small thumbnail samples are rendered from the actual opened photo on a background dispatcher. Tap a tile to apply. Filter catalog remains unchanged.
- Light/Color/Mix/Grading/Detail/Effects: one current control and one slider, reset at hand.
- Curves: channel and knot selectors, miniature curve graph, one selected knot slider.
- Masks: mask chips, add/remove/undo stroke/clear, one selected adjustment; brush stroke overlay remains interactive when editing at Fit.
- Crop: scrollable rotation/flip actions and aspect options; horizontal/vertical reframe and straighten share one selected control.
- More: auto tone/WB, save look, save/editable project, open projects, reset, new photo.
- Explicit high-contrast text colors and readable selected states; Android status/navigation insets respected while photo remains behind translucent overlays.
- No changes to rendering or data/document codecs, to prevent lost project compatibility. APK shares the same application ID and updates the 0.4 installation in place when signed consistently.

## Limits and release gates

Compiled/tested UI still needs on-device usability feedback; thumb size, real touch gestures, scroll behavior, blur/focus, contrast, dialogs and hand/device form factors require actual testing. No claims of verified full commercial Photoshop/Lightroom parity.

Long-term professional parity work remains separate: RAW/16-bit ICC workflow, healing/cloning, robust batching, performance and memory benchmarks, professional cropping/transforms, GPU/tiled processing, accessibility and release signing.

Company official website, DNS, hosting, XForge/GZFX/Beside-Me and any other repository **must remain untouched**.
