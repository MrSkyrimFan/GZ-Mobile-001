# W0 navigation correction 001

Status: implementation candidate on `feature/w0-system-back-navigation`; CI and physical-device qualification are not yet credited.

Baseline: `main` at `1b995e8987ae96a841bf56dd22910deb784af054` (tree `026267872f3f1a98fa840effde4357378334a47e`).

## User problem and protected scope

Android Back must return one meaningful internal step without losing or silently applying work. The v0.12.2 shell uses three independent workspace booleans, so Regular → Pro → GIF → Back returns to Regular instead of Pro. Loaded standalone Regular has no internal Back owner. Embedded Collage tile Back calls the same function as Done and therefore commits a dirty tile without a decision.

This correction must preserve the existing dark image-first layout, top-right workspace selector, commands, project formats, renderers, exports and original-media protections. It does not authorize a reskin, a Navigation Compose migration, a Pro grading redesign, RAW work, Video work or W8 onboarding.

## Bounded implementation

- Replace the three top-level workspace flags with one `WorkspaceNavigation` value containing the active `WorkspaceMode` and the actual caller stack.
- Opening the active workspace is a no-op. Disabled Video cannot enter the stack.
- Back preview is read-only; only a committed Back returns a new navigation value.
- Keep all Regular, Pro, Collage and GIF documents in their existing owner. Workspace navigation moves no pixels and converts no document.
- Retain each mounted workspace's stable session state in the host: Pro selected layer/tool/transform/viewport/history and tool preferences; Collage selected tile/control/history/tool visibility; GIF route/tool/frame or tab/playhead/history. Playback resumes paused. Preview bitmaps, pointer drafts, dialogs and jobs remain screen-local.
- Finalize slider/timing history before a route pop, and report a meaningful notice instead of silently swallowing Back while Pro, Collage or GIF is busy/exporting.
- A loaded clean Regular photo returns to the existing empty photo start instead of leaving the task.
- Dirty Regular Back offers Save project, Discard or Keep editing. A successful project save establishes the new clean baseline before closing. A failed or dismissed save keeps the photo intact.
- Embedded Collage tile Back is separate from Done. Its immutable entry `CollagePhoto` is the transaction baseline, so replacement-source loads and standalone project saves cannot make the tile falsely clean. Clean Back cancels without modifying the parent. Dirty Back offers Apply, Discard or Keep editing. Busy/exporting tile work keeps Back ownership and reports why it cannot leave.
- Collage-root system Back, visible Back and workspace switching use the same guard. Preview rendering or export cannot fall through and remove the workspace.
- Pro hidden-chrome Back, Help article → topic list behavior, GIF export ownership, all render/persistence code and all existing project codecs remain unchanged.

## Existing ownership inventory for following slices

The current Pro route is seeded from the in-memory Regular source URI and recipe only when the Pro layer list is empty. An existing Pro document wins. This is one-way implicit seeding, not the explicit saved Regular ↔ Pro exchange required later. The seeded layer receives source aspect while a new Pro document retains its independent default canvas. This PR deliberately preserves that compatibility behavior and only records it.

Pro's Tone & color entry adapts the shared grade deck to a selected Pro object. Existing Pro v1–v5 records embed legacy grade data through the current Pro model/store/renderer. The revision-2 owner rule moves new full-photo development to Regular while requiring old Pro grades to render and remain editable. No shared grade code, stored field or public Pro route is removed in this correction.

Regular remains the owner of photographic Looks, Mix, Grading, Curves, local develop masks, RAW, optics, catalog and batch. Pro remains the owner of layers, selections, masks, transforms, retouching and compositing.

## Automated evidence required

The JVM route-policy suite must prove:

- PHOTO → PRO → GIF commits Back to PRO, then PHOTO.
- Choosing the active destination does not duplicate history.
- Previewing Back does not mutate committed navigation.
- Empty PHOTO root delegates to Android and unavailable Video does not route.

The Android API 29 and API 35 suites must exercise a real system Back event and prove:

- loaded Regular → Pro → GIF → Back → Pro → Back → the same loaded Regular, including Regular dirty history and Pro selected layer, active tool, non-default viewport and Undo history;
- a video-backed GIF returns from Pro to the same active tab and playhead, with playback paused;
- a real orientation change retains the Pro route, selected layer, active tool, viewport and Undo history;
- clean loaded Regular → empty photo start without modifying source bytes;
- dirty loaded Regular supports Keep editing and Discard without modifying source bytes;
- clean Collage tile Back cancels without Apply;
- dirty Collage tile Discard does not invoke Done;
- dirty Collage tile Apply invokes Done exactly once;
- replacing a tile source remains dirty relative to the entry tile;
- replacement Discard leaves the real parent document unchanged, including its neighbor tile and layout;
- Keep editing retains the replacement and a later Apply changes only the selected parent tile.

The complete pre-existing instrumentation suite remains required. With ten added Android tests, the expected suite count is 92 per emulator if no existing test is removed or skipped. The exact XML case list, failures, errors and skips must be inspected at the tested commit.

## Evidence that remains open

Automated key events and a pure side-effect-free preview model do not prove a real predictive edge gesture. Physical-device qualification remains open for both gesture edges, half-cancel, one committed pop, rapid double Back, OEM picker/IME precedence, and Back during an irreversible document-provider copy. The first phone gate also needs matched protected-screen evidence. These items must not be reported as accepted from green CI alone.

Activity recreation/process-death draft recovery, persistent Collage tile IDs, delayed picker/reorder targeting, per-entry snapshots for repeated-mode cycles, explicit saved Regular ↔ Pro exchange and durable autosave are required following slices. The in-memory host sessions in this correction are not serialized into the saved-instance-state bundle. These items are not silently claimed by this bounded correction.
