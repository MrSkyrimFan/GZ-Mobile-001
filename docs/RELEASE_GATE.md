# GZ Editor Pro 0.7 engineering and release gates

## Automated
- [ ] Exact branch build + JVM tests + debug APK pass
- [ ] Android emulator image-format headers, social sizing, crop, text/ink/shapes pixel tests pass
- [ ] Android Compose UX tests pass for text, drawing, crop, export dropdown and original tools
- [ ] Old v0.6 project data still loads with all old grading, masks and privacy
- [ ] New editable text/ink/shapes/crop survive save/reopen and undo/redo
- [ ] Export format MIME and file bytes agree, no watermark or source writes
- [ ] Crop frame UI is correctly mapped to photo and respects aspect when constrained
- [ ] Company branding in top-left and copyright remain; previous UI functions preserved
- [ ] PR review and exact merge; merged-main full workflow success and correct downloadable artifact

## One real phone session
- [ ] Test multiple fonts, multiline text, drag+resize, color, rotation, save and export
- [ ] Draw with different colors, thickness, opacity and eraser; add/change a shape
- [ ] Crop by dragging corners and moving the box in freeform and 4:5; verify exported area
- [ ] Export JPEG, PNG, WebP, BMP and TIFF/PDF where phone can inspect; verify file types and content
- [ ] Instagram portrait and TikTok 9:16 social presets: Fit vs Fill outputs correct dimensions and composition
- [ ] Existing Looks, Privacy black covering, masks and Undo/Redo still work
- [ ] No clipped controls, visual overlaps, severe slider lag or crashes

## Still before commercial release
- [ ] Device QA beyond emulator, memory and performance under large photos
- [ ] 16-bit/wide-gamut/RAW workflows, clone/heal, file-size ceilings and accessibility
- [ ] Branding/trademark clearance, correct website privacy/legal disclosures, organization Play account
- [ ] Signed release AAB, prelaunch report, security/license review and store compliance

Do NOT represent this engineering APK as the final 1.0 paid release.
