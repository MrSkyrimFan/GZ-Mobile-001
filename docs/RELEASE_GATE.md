# GZ Photo Editor Pro — release gates (v0.4.0 candidate)

## Automated
- [ ] Branch CI: compile, JVM unit tests, APK artifact, no permissions/brand drift
- [ ] Android 15 emulator bitmap tests: rotation/crop, safe straighten, preview/export pixel parity, JPEG alpha flattening, project roundtrip, Activity starts
- [ ] PR CI green, source diff reviewed, merge with expected commit
- [ ] Merged-main CI green, artifact available for download
- [ ] Confirm manifest no INTERNET, CAMERA, broad media library, location or billing permission

## ONE real Android phone qualification session
- [ ] Launch and open photos from Gallery/Photos/Files
- [ ] Global tonal, color-grade/HSL, detail denoise/texture, film controls
- [ ] Auto tone and white balance, compare, undo/redo/reset
- [ ] RGB/master curves, histogram
- [ ] Radial, linear, and finger-painted BRUSH masks with resizing/softening and local edits
- [ ] Straighten, center/reposition crop, 90° rotates, flips, zoom/pan and Fit
- [ ] Save local named project, kill and restart app, reload settings and original; preset save/delete
- [ ] Export 4 JPEG/PNG variants and open output files; compare against source; test transparency
- [ ] 12MP, 24MP, >24MP images and long-session memory; note output resolution caps
- [ ] No crashes, stale previews, bad banding/darkness, poor speed or broken output

## Before commercial release (all currently unclaimed)
- [ ] Name/brand trademark clearance and formal owner approval
- [ ] Real photo quality evaluation, mobile UI accessibility, dynamic text/insets, multiple devices/API levels
- [ ] Better performance for demanding file sizes, modern high-bit-depth color profile and RAW assessment
- [ ] Privacy policy published on official Greenezone website, without changing that site blindly
- [ ] Play organization account verification, store listing, current policy, required target SDK
- [ ] Production signing, release AAB, store prelaunch checks, app ownership and license review

A green CI verifies tests and debug build, **not premium market parity**.
