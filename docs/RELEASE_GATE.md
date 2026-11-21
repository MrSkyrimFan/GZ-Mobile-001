# GZ Photo Editor Pro — 0.3.0 engineering release gates

## A. CI/source
- [ ] Feature-branch CI succeeds, JVM math/history tests and debug APK build
- [ ] Pull request CI succeeds, code review and verified merge, main CI succeeds
- [ ] Brand strings, package ID, app label, artifact names show only approved GZ Photo Editor Pro
- [ ] No INTERNET, broad media/storage, camera, location, accounts, ads, analytics, IAP, AI
- [ ] No accidental changes to Greenezone corporate website or other project repositories

## B. One comprehensive Android phone field test
- [ ] Install/open APK on actual Android phone, identify Android version
- [ ] Test photo picker with JPEG, PNG and high-resolution images
- [ ] Exercise light/color/detail/film, color grading, all eight HSL bands
- [ ] Master/R/G/B five-knot curves produce smooth predictable changes; Reset and undo work
- [ ] Add radial and linear masks, position/feather/tilt each, test local exposure/contrast/saturation
- [ ] Confirm max four masks, remove, undo/redo, and compare view shows unchanged original pixels
- [ ] Test saved looks including curves after app restart; inspect deletion and default looks
- [ ] Check histogram, pinch/zoom/drag/fit, transformations and all crop ratios
- [ ] JPEG 85/95/100 and PNG export, actual gallery open, untouched source and correct dimensions
- [ ] At least a 12MP photo, 24MP photo, >24MP image and low-memory test
- [ ] Fast slider swipes, rotation while editing, app background/resume, repeated exports, UI on small screens

## C. Production
- [ ] Trademark: live USPTO similar/exact marks, state/common-law usage, logo and owner signoff
- [ ] Visual quality/precision vs raw reference and sRGB color targets, no unacceptable banding/artifacts
- [ ] Performance, memory, stability and device coverage acceptable
- [ ] App accessibility / touch targets / dynamic color / insets / user data review
- [ ] Official Greenezone privacy policy published at approved website (outside this app repo)
- [ ] Google Play organization verification, title/listing/policy reviews and target API compliant
- [ ] Signed production AAB, Play prelaunch tests, screenshots, store listing and owner approval

Do NOT label this release-ready or a superior commercial app solely from source compile/test.
