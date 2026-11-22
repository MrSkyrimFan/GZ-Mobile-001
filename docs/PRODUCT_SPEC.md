# GZ Editor Pro 0.6.0 engineering specification

**Owner approval (2026-11-21):** Rename to **GZ Editor Pro**, remove all marketing slogans, replace Open photo with Open media, display only © 2026 Greenezone Software LLC bottom left on empty main screen. Preserve no ads, runtime AI, accounts or watermarks.

## Field evidence and UX
Five actual Android screenshots show clipped Reset on Effects and Mask brushes, large brush-control dock hiding the area being painted, and green brush guide overlay separate from the real dark brush result. The existing full-image canvas and toggle are retained, but the **selected brush/rectangle must remain interactive after hiding chrome**. All tool lists must stay horizontally scrollable, with one active adjustable control at a time.

## Major feature wave
1. Brand update in launcher, main screen and workflow. Preserve existing applicationId for compatibility; increment version 6, name 0.6.0. Only the official empty/main page shows company copyright.
2. 50 different built-in Looks, with previous 12 kept at the front, allowing selecting None first. Preview thumbnails are generated from imported photo in the background.
3. Dedicated Blur & cover tab. Each operation stored as versioned, bounded non-destructive edit data in `EditDocument.privacy`. Each can be undone/redone and reopened in a saved project.
4. Black bar and solid covers paint **opaque** pixels into the final export. Blur and pixelation are soft/cosmetic, cannot guarantee concealment. Paintable blur and black cover use bounded spatial mask paths.
5. Privacy rects can be repositioned by direct photo drag. Brushes remain active when editing chrome is hidden; brush guide display is independent of paint availability.
6. Current exposure, HSL, grading, curves, crop, brush masks, snapshots, project files, and no-network/export guarantees remain.
7. Fix the Reset row dimensions and horizontal layout. Use two-stage preview to improve perceived responsiveness; full-resolution preview and export always use the same source-rendering implementation, but spatial detail can differ due to scaling.
8. Source image and URI cannot be overwritten intentionally; export creates new PNG/JPEG, no watermark or captions are burned into output.

## Tests and release gate
- JVM tests for privacy geometry, deterministic codec, bounded operation counts, and backwards compatibility with older version-1 projects.
- Android emulator bitmap tests verify opaque black pixel output (including JPEG), outside-region preservation, blur/pixelation and brush output.
- Compose tests cover main screen text, navigation, Reset visibility, and privacy tool access.
- Branch + PR build, emulator and merged-main CI required before user gets the v0.6 test APK. One combined phone session tests all major systems.
- Formal color fidelity, multiple Android phones, low-memory exports, different color profiles, accessibility, signing, privacy policy and Google Play eligibility remain commercial release gates.

**Not implemented:** video editing despite the neutral Open media label; RAW/16-bit, healing, content aware fill, GPU tiled renderer, batch editing, legal trademark clearance. No claims of professional final parity.
