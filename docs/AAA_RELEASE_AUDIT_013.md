> **Historical checkpoint — current route updated November 25, 2026.** Follow the [GPT-6.2 Sol Ultra successor handoff](NEW_CHAT_HANDOFF_2026-11-25.md), [current ledger](WORK_LEDGER.md) and [PR #37 / 0.12.11 Test evidence and focused phone script](B1_WORKSPACE_VERIFICATION.md). Current task: read-only successor onboarding, then wait for Lance's ongoing GPT-6 Pro review. Original text below retains its dated evidence; its “current”, “next”, delivery, engineering and test commands are historical, not fresh instructions. Do not replay completed intake, question exchanges, builds or phone scripts. The [current review brief](coordination/GPT_6_PRO_REVIEW_BRIEF_2026-11-25.md) supersedes older review prompts.

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
- Replace Pro project's non-atomic save/copy fallback with `AtomicFile`
  write-ahead backup semantics, bounded reads, backup-only discovery and delete.
  Add emulator tests for overwrite, crash backup recovery and malformed isolation.
- Run both Android API29 (advertised minimum) and API35 emulator suites, plus
  signing verification. Add multicolor gradient and moving-alpha GIF tests.

## Ship/no-ship decision
Full AAA commercial launch: **NO-GO** while the open feature, performance,
device matrix, source recovery, legal and production signing gates remain.
An internal v0.12.1 phone-test APK, if automation passes, is a *QA candidate*
only and cannot be marketed as complete Photoshop/GIMP/Lightroom parity.

**Release discipline:** Green emulator tests are necessary but not sufficient.
Do not issue an "AAA finished" promise until the missing professional
functionality, device/format corpus and commercial qualification are genuinely
complete and witnessed.

## Additional fidelity repair
- Requested FPS previously silently clamped to 150,000 frames on long clips,
  reducing user-requested frame rate. The new scheduler rejects unsupported
  long-clip FPS and disables that option in the UI instead of changing it secretly.

## Android 10 red-team failure and diagnosis
- First complete API29 run: 65 Android tests, three failures. The Pro
  malformed-document test provoked a genuine OutOfMemoryError in `ProStore.load`
  before size rejection. Added pre-open file-length and max-heap size bound,
  with a streamed stress fixture.
- GIF transparency test reused an external Bitmap when calling Android
  `Movie.draw`. That draw performs source-over blending and does not clear
  prior external canvas pixels. Clear destination before drawing each decoded
  animation frame, then verify the underlying GIF's own disposal.
- GIF saved-file reopen in Compose timed out on low-memory API29. Stop
  off-screen frame decoding and release the preview cache during source open
  and export; require full requalification rather than increase test timeout.
- No marketing or release certification until both API29 and API35 report
  all tests green again after these corrections.

## GIF re-open UI race identified on API35 and API29
- The "GIF saved" notice was set while `exporting` was still true.
  The "Edit saved GIF" action appeared immediately, but `openSource`
  quietly returned while `enabled=false`. The automated test intermittently
  timed out waiting for a state update after the ignored click.
- Gate the button on `enabled` so it cannot be clicked until the job's
  `finally` clears export state. Verify bytes with an Android GIF decoder
  and re-inspect saved content before simulating button press.
