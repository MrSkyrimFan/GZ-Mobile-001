# Reproduce the evidence

Extract the whole ZIP and open a terminal in the folder containing README.md. Paths in commands below are relative to that folder. The archived receipts run offline. The fixture checks need Python 3 and Pillow, GIF geometry controls also use ffmpeg, the Pro numeric checks use a Java 17+ source launcher, and the CI reproduction uses Bash. On Windows, Git Bash is sufficient for the shell example with Python and Java available. The Android implementation and Gradle suite belong in the app checkout.

| Command | What success establishes | Evidence generated in this audit |
|---|---|---|
| `python proofs/verify_ci.py` | Rehashes the four original archives, checks ZIP CRCs, the exact 135-case set in every XML, zero exit and matching log files. | [Four-lane replay](evidence/final_ci_replay.txt) |
| `python proofs/check_regular_cover_flip.py` | Independently accepts a correctly protected image and rejects four wrong images, including a completely black image that could fool a one-pixel test. | [Pixel controls](evidence/regular_cover_controls.txt) |
| `java proofs/ProBoundaryControls.java` | JVM coordinate-string lengths straddle the read/write thresholds, and geometry and output-size examples distinguish the expected cases. | [Three numerical controls](evidence/ProBoundaryControls.txt) |
| `python proofs/gif_host_controls.py` | Accepts two good outputs and rejects three defective ones covering delay round trip, display geometry and visible shape. | [GIF controls](evidence/gif_controls.txt) |
| `python proofs/verify_prior_ci_guard.py` | Runs the baseline shell checks against four disposable inputs and demonstrates a false green in the brand-only case. Add `--workflow path/to/android-ci.yml` to check the exact original commands. | [Shell outcomes](evidence/ci_guard_controls.txt) |

## Use a real Android export as input

- **Privacy cover.** Import `proofs/regular_cover_source.png`, cover the magenta rectangle with the opaque Black bar, mirror horizontally and export Original-size PNG. Run `python proofs/check_regular_cover_flip.py exported.png`. [Coordinates and expected pixels](chapters/30_REGULAR_COLLAGE.md)
- **GIF shape.** Import `proofs/gif_inputs/gif_aspect_square.mp4` and `gif_aspect_anamorphic.mp4` separately. Both have a 16:9 intended display. Export Small at original aspect with no look/crop. Run `python proofs/gif_check_display_aspect.py output.gif` on each. Inspect the bright moving circle as well as the width and height. [Source metadata and exact conditions](chapters/20_GIF.md)
- **GIF hold.** Import `proofs/gif_inputs/gif_long_hold.gif`, set speed to 50%, export and run `python proofs/gif_hold_fixture.py check output.gif`. The checker distinguishes encoded duration from the app's short-delay interpretation. Inspect the reopened time in the app too. The two-frame input is tiny despite its long hold. [Expected timing](chapters/20_GIF.md)
- **Project store.** `proofs/ProStoreRoundTripInstrumented.kt.example` is a proposed test using the project's Android types. Codex can adapt it into an isolated test checkout. It creates its own named project, tests save -> reload or rejection preserving prior data, and cleans up that project. It has not been compiled or executed in Android in this audit. Add the normal UI two-name scenario in the Pro chapter because the store-level assertion cannot detect a wrong name supplied by the screen. [Pro repair cases](chapters/10_PRO.md)
- **Original regression footage.** The repository's instrumented 88-frame portrait workload, the field guide's 18-frame clock, and the owner's original clip are three different inputs. Reuse the first two from Git and keep raw data from the owner's phone private. The earlier guide's checker was re-run separately in this review. [Field-guide control results](evidence/field_guide_controls.txt) · [Pinned fixture recipe](https://github.com/MrSkyrimFan/GZ-Mobile-001/blob/14df73926c6f7063a488f84e367268168e8e5444/docs/gz-editor-pro-field-guide/fixtures/README.md)

## Repeat the APK identity check

Retrieve the candidate archive via the exact artifact link in the README. Verify the ZIP hash, extract `app-debug.apk`, verify its separate hash, and run `apksigner verify --verbose --print-certs --min-sdk-version 29 app-debug.apk` from Android SDK Build Tools. `apkanalyzer manifest print app-debug.apk` can show its package, SDKs, version and label. For a new APK use the new job's expected identities. An old known digest cannot qualify a new build. [Expected bytes and hashes](evidence/final_artifacts.json) · [Pinned binary verification](evidence/verified_apk.txt) · [SDK apksigner](https://developer.android.com/tools/apksigner)

## How to interpret failure

For a checker failure, retain the exported file, app version, precise edit sequence and checker output. First check the positive and negative controls. Then compare the actual preview with the independently decoded export. A bad file with a good preview points to a different place than a bad preview with a good file. A green host control validates the measuring instrument; only an Android output can validate that app path. For a time budget, record the device/API, timestamps, resolution and frame count, and whether the UI already showed a prepared frame. A field report should survive beyond the chat as a linked test, artifact or red receipt.
