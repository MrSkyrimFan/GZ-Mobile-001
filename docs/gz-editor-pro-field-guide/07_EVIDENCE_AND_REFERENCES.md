# Evidence and reference index

## Project evidence

The original Rev 4 correction handoff is available in the private delivery identified in the [repository README](README.md). The public guide includes the independently generated fixtures. Its SHA-256 is `f84511e3e1286e1e5e2e12f489fa6a9b4bb3e1d677f0ca2fc50f36c7246de6b8`. It includes 16 field screenshots, their provenance and the Rev 3.1 handoff at `evidence/prior_handoff/GZ_EDITOR_PRO_CODEX_FIELD_HANDOFF_REV3_1.zip`. That inner archive contains the 33 earlier supplied images and the design catalog at `GZEPro_Codex_Field_Handoff_V3_1/tables/STICKER_LIBRARY_DESIGN_CATALOG.csv`. Its 648 concepts across 27 categories are design work. A finished catalog consists of usable artwork, accurate metadata, licenses, and assets actually rendered by the app.

The historic archives are reference copies of the owner's original material. Their old branch starting points, progress labels and starter prompts were written before the repairs in [the engineering checkpoint](01_ENGINEERING_CHECKPOINT.md). Use the current [start page](00_START_HERE.md) when resuming. The user’s field photos document visible states; by themselves they cannot establish elapsed time, byte content, exact installed commit or the cause of a freeze. The generated fixtures in this bundle answer narrower questions with known source pixels and times. We did not have the owner's original camera clip or access to their phone during this review.

PRs, Actions jobs and receipt links are recorded beside each result in chapter 01. The app’s project files and APK are maintained and built in its authorized repository. This repository copy contains product guidance, historical references and synthetic test media. The quality targets, worked examples and proposed experiments in chapters 02–06 are engineering guidance and product intent unless a dated result is identified.

## Primary technical references and how to apply them

| Reference | Engineering use |
|---|---|
| [NASA Systems Engineering Handbook, Appendix D, Requirements Verification Matrix](https://www.nasa.gov/reference/appendix-d-requirements-verification-matrix/) | Connect a requirement to a verification method and a result. Here the method might be an independently decoded GIF or a physical predictive gesture. This app has its own acceptance criteria. |
| [Android accessibility](https://developer.android.com/guide/topics/ui/accessibility/apps) · [Compose accessibility defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults) | Choose touch-target behavior and semantics that remain useful at high density, on a crowded canvas and with assistive technology. |
| [Android predictive Back](https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture) · [Android saved UI states](https://developer.android.com/topic/libraries/architecture/saving-states) | Keep system navigation, transient UI restoration and durable project recovery distinct. |
| [Android documents and providers](https://developer.android.com/training/data-storage/shared/documents-files) | Test real URIs, grants, picker and provider writes, including cancellation and relink. |
| [Android slow rendering](https://developer.android.com/topic/performance/issues/render) · [Macrobenchmark](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview) | Profile full interaction and attach traces for slow frames, separating input feedback from media frame cadence and encoding. |
| [GIF89a specification](https://www.w3.org/Graphics/GIF/spec-gif89a.txt) | Interpret frame delays, transparency, color tables and disposal when verifying exported animations. |
| [Android MediaCodec](https://developer.android.com/reference/android/media/MediaCodec) · [Media3 multi-asset editing](https://developer.android.com/media/media3/transformer/multi-asset) | Reason about decode lifetimes and capabilities; evaluate later video composition against the project’s installed dependency version and actual devices. |
| [Adobe Digital Negative specification](https://helpx.adobe.com/camera-raw/digital-negative.html) · [International Color Consortium specifications](https://www.color.org/icc_specs2.xalter) | Specify an interoperable RAW and color path with explicit transforms, output precision and profile handling. |
| [W3C Compositing and Blending](https://www.w3.org/TR/compositing-1/) | Derive independent reference colors for selected blend and alpha cases in the documented working space. |

Competing creative apps are benchmarks for user jobs such as developing a negative, restoring texture or mixing several audio tracks. Implement the behavior with independently designed UI and code plus assets and dependencies appropriate to the intended commercial distribution. Maintain actual license and asset records during development; final consumer agreements belong to the end of the project.
