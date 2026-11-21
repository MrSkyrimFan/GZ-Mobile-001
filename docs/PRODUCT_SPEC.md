# GZ Photo Editor Pro — authorized product contract

**Company:** Greenezone Software LLC
**Platform:** Android (native Kotlin/Compose)
**Planned price:** $1.99 one-time paid Google Play purchase
**Approved name:** GZ Photo Editor Pro
**Status:** source under active engineering, NOT commercially released.

## Non-negotiable business / privacy terms

Offline/on-device operations only. No runtime AI, ads, in-app purchases, subscriptions, trackers, account, telemetry, clouds, network calls, or overwrite of the original photo. Import via Android's scoped photo picker; export explicitly to a new user-selected file. No broad photo permissions. Greenezone retains rights to its own code, with Android dependency license review before release.

## Current candidate (subject to CI + physical handset qualification)

- 53 global sliders (20 foundational, nine 3-zone color grade, 24 HSL color mixer).
- Per-channel RGB/master tone curves with five fixed horizontal anchors each. Vertically adjustable through on-screen sliders and visual curve preview, sampled to a 256-entry table.
- Up to four independent radial and linear masks. User can configure center/tilt, size/spread, radial feather, exposure, contrast, saturation. They are mathematically implemented in the same pipeline as the full-res export.
- Twelve presets, up to twenty device-local named grade presets (including saved curves). Masks belong to an edit document and are **not** saved into presets.
- Non-destructive undo/redo (40) of all curves, masks, geometry and grade changes.
- Crop to nine *centered* aspect presets, rotate left/right 90°, mirror horizontally or vertically, compare and reset.
- Responsive bounded preview, zoom/pan, optional brightness histogram.
- JPEG quality 85/90/95/100 and PNG new-file export. sRGB 8-bit output, 24MP decoded pixel safety cap.
- CPU scanline renderer with precalculated grade constants and optional per-row cancellation checkpoints.

## Technical truth and caveats

- Curves have fixed x positions, editable y values. Not arbitrary multi-anchor drag.
- Color pipeline is 8-bit sRGB. It is not RAW development, scene-linear/HDR, profile-managed wide-gamut output, or a replacement for professional 16-bit darkroom software.
- All masks are gradients, not AI/object selections or arbitrary brush-painted masks.
- Linear masks are tilted by the horizontal-position control; they are not perspective correction.
- Preview resolution may differ from export for sharpening, grain, geometry rounding; investigate any materially divergent appearance.
- A 24MP cap reduces enormous images; original megapixel counts above this are not preserved.
- Memory can still spike from input, geometry, output bitmaps; device tests must include 12MP, 24MP and >24MP sources.
- No guarantee of source EXIF metadata retention, ICC profile preservation or transparent PNG fidelity without actual device evidence.
- No Android UI/instrumentation tests or production signed AAB exist.

## Long-term professional parity work (not included)

Freehand crop/straightening; painted masks and brush retouch; heal/clone and content-aware tools without AI; true curve point drag/levels; higher-order color-space engine and ICC; RAW/DNG; high-bit-depth output; denoise and selective sharpening; optical correction/perspective; tiled rendering; GPU acceleration; batch presets/exports; sophisticated phone UX and accessibility.

Full quality qualification is an explicit release gate. No fictitious feature counts or production-readiness claims.
