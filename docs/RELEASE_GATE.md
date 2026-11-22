# GZ Editor Pro — engineering candidate 0.6.0 release gates

## Build and source
- [ ] Branch and PR Android CI compile, JVM tests and debug APK pass
- [ ] Android emulator tests pass including real opaque censor/blur/pixel output and Compose UI
- [ ] v0.6 app launcher says GZ Editor Pro, applicationId unchanged, no old UI slogans
- [ ] Open media label visible; supports images only (do not pretend video support)
- [ ] No internet, ads, subscriptions, accounts, AI or watermarks in output
- [ ] Full preview/export parity for privacy marks; original never overwritten
- [ ] Older saved projects remain decodable and new privacy edits roundtrip correctly
- [ ] Photo canvas remains full-screen; hidden UI still allows the selected brush to paint
- [ ] Reset fully visible at phone screen width; no text clipping
- [ ] One fast-preview then full-preview progressive render for slider operations
- [ ] 50 Looks, swipeable category navigation and mask/history controls preserved
- [ ] PR merge with exact SHA and verify merged-main CI + APK artifact

## User field test (ONE combined session)
- [ ] Install and open updated APK on real Android phone
- [ ] All source-branding, 50 Looks, hidden brush painting, touch positioning, Reset and preview responsiveness
- [ ] Blur, pixelate, black bar, solid-color cover and both privacy brush tools are functional
- [ ] Opaque black bars persist in exported JPEG/PNG; blur/pixelate are visually distinct
- [ ] Save/reload privacy edits, original intact; export works
- [ ] Any major slow operations, black bands, UI obstruction, crash or stale preview recorded

## Commercial 1.0 (still open)
- [ ] Professional visual quality and color-fidelity qualification
- [ ] Real low-memory stress with 12/24MP and oversized images
- [ ] Crop/render/masking performance on multiple Android versions
- [ ] Accessibility, high contrast, touch targets, proper insets
- [ ] Trademark/publishing, privacy policy on *existing* Greenezone official site, Play organization verification
- [ ] Signed release AAB, store screenshots, production prelaunch and owner signoff

No commercial release claim on green debug APK alone.
