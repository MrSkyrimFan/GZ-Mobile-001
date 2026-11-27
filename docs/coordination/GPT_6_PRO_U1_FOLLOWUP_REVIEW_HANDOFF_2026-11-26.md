# GPT-6 Pro review handoff — U1 correction and qualification hold

November 26, 2026. Review the U1 corrections and the exact unresolved API35 R1.1 qualification result before the next handset recommendation. [Canonical receipt](../WORK_LEDGER.md#u1-review-correction-and-qualification-hold).

- Production correction merge `b342f0bd656753b3692b741489438c6790d14d8b`, complete tree `046ac9806b7181c78cb1b30cf88505d02354e9cf`. [Application PR #49](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/49).
- Draft qualification candidate `27406f4aed8efa42a6078e1108c53304d4a0043e`, complete tree `2c0f8ffab9a8321f8f9f823c9354c26b5a2fb6c8`, 0.12.15 Test/code27/phonetest.v27. [Qualification PR #50](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/50).
- Last fully qualified application `e6321322526521ace13a45c231b25bb08f91c565`, 0.12.14 Test/code26. [Prior application PR #46](https://github.com/MrSkyrimFan/GZ-Mobile-001/pull/46).

## Corrections and evidence

- **U1R-01:** actual native Layers mask and clone/repair shortcuts after linked finished output, including the matching preview raster, real gesture, target, history and saved-output checks.
- **U1R-02:** exact prepared-handle and bitmap cleanup for named and working-draft Open failure, cancellation, staleness and IO-return delivery, with named/draft/retry custody kept.
- **U1R-V1:** deterministic held Save plus Cancel reproduced a missing acknowledgement. The correction uses the captured saved baseline and exact owner and retains newer dirty work. **U1R-N1** remains deferred.
- [focused candidate](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45907591692) ran 10/10 new cases per API. [Current full candidate run](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45920992792) passed JVM/build/signing, ordinary API29 313/313, ordinary API35 312/313, and the separate actual process lane on both APIs. [Raw API29 archive](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45920992792/artifacts/15165596289) · [Raw API35 archive](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45920992792/artifacts/15165426352).

## Qualification hold and requested verdict

The API35 R1.1 test measured selected_warm_3 at **717.745 ms** against the **650 ms per-Open limit**. The other three samples were 206.769, 191.220 and 182.320 ms. All four opened the exact requested record with one recipe decode. The late sample observed 32.16 MiB managed increase, below its 64 MiB limit. The assertion stopped the test before its reported warm median.

The per-test log records a contemporaneous concurrent ART collection of 551.746 ms with 21.335 ms of recorded pauses. That overlap does not identify how much latency it caused. The R1.1 test and production sources are unchanged. Read-only review found no source correction warranted within U1 and no basis to waive the sample. [Exact test-only delta](https://github.com/MrSkyrimFan/GZ-Mobile-001/compare/b342f0bd656753b3692b741489438c6790d14d8b...27406f4aed8efa42a6078e1108c53304d4a0043e).

1. Review the exact U1 production changes and red-to-green support for all three findings in the ledger.
2. Check all 303 inherited identities and ten additions, the actual separate process proof, and the three instrumentation-only corrections for MediaStore readiness, recovery scheduler and tile-save acknowledgement.
3. Review the 717.745 ms sample and its ART context. Assess what the evidence establishes about the cause and give Lance one bounded next check or repair that preserves the exact 650 ms limit, fixture and assertions. State whether the corrected APK and handset recommendation must remain held.
4. Give Lance a copy-ready instruction to relay. Keep remaining R1.2 next after the qualification disposition and focused review.

PR #50 is draft and has no qualified merged APK. Raw failures and prior qualification remain credited at their exact heads. All 77 families, R1–R11, B2/F03, RV5, wider handset/accessibility, workspace acceptance, and website-before-Play Console sequencing remain.
