# Release gate — GZ Photo Editor Pro 0.2.0 engineering candidate

## CI and code
- [ ] Feature-branch Android CI green: JVM unit tests + debug APK assembled
- [ ] Pull-request Android CI green, PR reviewed, merged without overriding someone else's branch
- [ ] Merged-main Android CI green and testing artifact verified
- [ ] Confirm old ChromaVale name/package absent from compiled app
- [ ] Confirm no INTERNET, broad storage/gallery, camera or location permissions

## Physical phone acceptance
- [ ] Pick sources from system Photos, Gallery or Files where supported
- [ ] Exercise 20 original sliders, 9 three-zone sliders, all 24 HSL color-mixer sliders and visual effects
- [ ] Undo/redo, default looks, user-saved look survives app restart, and delete saved look
- [ ] Reset whole edit and one specific slider to neutral
- [ ] Compare, pinch/pan/fit, toggle histogram
- [ ] Rotate both directions, horizontal/vertical flip, and all nine crop ratios
- [ ] Export JPEG 85, 95 and 100, plus PNG; inspect file dimensions, alpha behavior and content
- [ ] Verify source bytes stay unchanged and exported file exists under user-picked path
- [ ] Test 12MP, 24MP, >24MP panorama, portrait EXIF, PNG transparency, and unusual photos
- [ ] Small Android phone; large Android display; background/resume; repeated export; low-memory test
- [ ] Report performance and color artifacts on actual photos

## Commercial readiness
- [ ] GZ Photo Editor Pro word mark, visually/phonetic similar marks, common-law usage and logo cleared; no definitive legal clearance claimed
- [ ] Legal privacy policy posted at the eventual Greenezone official website; site is NOT created/modified by this repo
- [ ] Verify publisher organization, D-U-N-S where applicable, official website and Play policy obligations
- [ ] Accessibility and Google Play requirements rechecked at time of release
- [ ] Signed release AAB, store screenshots/listing, device prelaunch report, owner signoff

Status: **NOT READY FOR GOOGLE PLAY**. CI is necessary but is not hardware QA.
