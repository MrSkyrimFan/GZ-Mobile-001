# Independent release qualification, v0.12 source audit (November 22, 2026)

**Verdict: full AAA/Photoshop/Lightroom-class release NOT QUALIFIED.**
This file is a corrective engineering audit, not a marketing claim. The user requested
an extensive independent review before downloading v0.12. Source and CI receipts were
examined at merge `9cd83f68267c2a488fa9d307ec4064df55df1617`.

## Confirmed good evidence
- GitHub merged-main run 45003033632: build + unit verification + Android API35
  emulator tests PASS; 60 emulator tests at the exact merge.
- The Android test suite exercises GIF imported-frame timing, byte-for-byte
  unmodified source-GIF copy, reverse re-encoding, GIF playback UI, 2s MP4 source
  decode, Pro painting with hidden controls, selections and feathered masks.
- Generated APK 0.12.0 Test is a signed debug APK; v12 package suffix permits
  side-by-side installation rather than uninstalling existing local projects.
- App manifest requests no unrestricted media, network or storage permissions.
- Existing Regular and Collage workspaces were preserved.

## Confirmed missing work / BLOCKER for full production declaration
1. Pro is capped at 12 layers and lacks real layer groups, adjustment/clipping
   mask groups, pressure-sensitive brush engine, advanced healing and production
   object-selection ergonomics. It is NOT functionally identical to Photoshop/GIMP.
2. RAW/DNG camera development, 16-bit/float color management, accurate ICC/HDR,
   histogram/profiling and Lightroom-class local catalog/batch workflows are OPEN.
3. The GIF format is fundamentally limited to 256 palette colors per frame,
   10ms timing resolution, no audio, and may produce much larger files.
   No converter can make a GIF byte-identical to an ordinary video.
4. Original video export defaults to source framing/timing and no user effects,
   but source-to-output objective PSNR/SSIM, large real-world sources,
   VFR/B-frame/HDR corpus, animation disposal/background, memory, file
   size and thermal thresholds are not comprehensively qualified.
5. CI had API35-only Android emulation, no Android API29 minimum-version coverage
   or broad device and landscape/orientation tests; no physical phone test by owner.
6. Release gate requires licensing/third-party notice/SBOM audit, source custody,
   accessibility, Play-compliant billing/privacy and production signing.
7. A normal GIF export used to require the new SAF output URI to support
   `openInputStream` and be readably empty, which is not guaranteed by all
   writable providers. Android's ACTION_CREATE_DOCUMENT Save As semantics protect
   existing filenames without this check.

## This hardening branch
- Remove premature destination reads from GIF export while retaining a source
  URI collision guard and best-effort cleanup of an incomplete newly created file.
- Introduce bounded private temp-file output with a 64MiB safety margin and a
  clear action-oriented disk-limit error. Tests verify writes fail before the
  forbidden data is copied.
- Increase build version only for a separate 0.12.1 engineering candidate.
- Continue qualification on Android API35 and Android API29 as feasible.

**Release discipline:** Green emulator tests are necessary but not sufficient.
Do not issue an "AAA finished" promise until the missing professional
functionality, device/format corpus and commercial qualification are genuinely
complete and witnessed.
