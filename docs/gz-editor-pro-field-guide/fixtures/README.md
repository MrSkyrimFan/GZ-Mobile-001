# Inputs with known answers

These are original synthetic QA inputs made for this guide. They contain no owner media. They can be copied to a phone and imported through the normal Android picker. They supplement the existing repo fixtures and the owner’s original clip. An instrumented path that injects the document directly can isolate an error, but it leaves the picker and persistence paths untested.

## A motion counter readable by eye and by an independent decoder

`portrait_clock_1080p30.mp4` has 150 frames at 30 fps, lasts five seconds, and contains silent, upright 1080 × 1920 H.264 with B-frames and periodic keyframes. Eight black/white squares encode the source frame number, least significant bit at the left. The yellow block changes position. The colored patches distinguish neutral output from deliberate B&W. All three indicators are inside the portrait image. Sample inside fitted content in screen captures.

For a quick experiment choose the full portrait canvas, trim **2.20–4.00 s**, set **10 fps** and **100% speed**, keep the original colors and export. Repeat with only Reverse enabled. This recipe was chosen because its delays are exactly representable by GIF and its sources have distinct IDs. The 1.80-second test is separate from the repository's timed 2.95-second/88-frame workload. If UI precision does not support those exact trim points, use a precisely set test recipe for the counter and exercise the reachable user values separately with expectations derived from those values. Do not change product behavior just to match a helper.

Independent reader (Python + Pillow):

```sh
python3 check_gif.py forward.gif
python3 check_gif.py reversed.gif --reverse
```

For the specified recipe the expected result is 18 frames, 100 ms each, with the IDs traveling approximately 66 through 117 in steps of three. The reader allows one source frame of rounding at each expected sample and checks that successive IDs progress by two to four frames. This catches drift in the middle even when both endpoints match. It will reject reversed order, a frozen run, incorrect count or a missing frame delay. The reader assumes full-frame 9:16 composition and well-separated black and white patches. Severe grading, layout changes or intentional holds need a different expectation. It does not measure interactivity, color fidelity or power. Those require separate observations. An independent viewer should also play the delivered file.

## Four still inputs

| Filename | Phone exercise | Known answer |
|---|---|---|
| `panorama_4000x100.png` | Import in Regular, lock Square, use the full source height, move the box over the pink mark and export losslessly at original crop resolution. | Crop is 100 × 100. The exact optional source rectangle [1900,2000) × [0,100) has background RGB (95,174,87) and the pink patch at [35,65) × [35,65) in the export. A finger can select a different horizontal offset and still pass the *ratio* test. |
| `base_gold.png` | Use as the Pro base. Overlay a separate paint layer and then the blue-strip file above the paint at matching size. Move brush across entire row at mid-height. | Background (240,168,32), dark registration square at upper left. After erase, affected lower-layer pixels show this gold. Undo restores the prior stroke. |
| `upper_blue_strip.png` | Leave at identity, Normal, full opacity. Inspect the center of the stroke, the transparent sides and the same points while the pointer is still down. | Center columns 80–175 inclusive are opaque RGB (0,32,224), both outer sides fully transparent. Painting beneath the strip cannot tint its center. Erasing the lower paint cannot erase the upper strip. |
| `histogram_4_patches.png` | Open neutral, full uncropped, switch Before/Edited and compare channels. | Each of black, white, red and midgray occupies 25%. For the source pixels, R endpoints 25% zero/50% max, G and B endpoints 50% zero/25% max. All-channel black and white are each 25%. Preview resizing can mix boundary pixels. Use exact integer counts on the lossless source and a documented boundary tolerance on the displayed preview. |

The fixtures use lossless PNG for exact RGB/alpha probes. A photo pipeline that intentionally changes profiles or working space should compare in a common defined output space. Reusing the same pixels in two collage tiles is a good check that their edits belong to two tile identities.
