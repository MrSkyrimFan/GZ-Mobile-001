# Four original Android CI receipt ZIPs retained on GitHub Actions

The audit's original 3.0 MB delivery ZIP included four unchanged raw CI receipt ZIPs. They were **not duplicated** into the Git tree. They already live on this very repository's GitHub Actions, with stable artifact IDs and hashes below. All the audit prose, JSON extracts, executable proof scripts and synthetic media are in the Git tree.

| Baseline | Run | API | Artifact | Original ZIP size | Exact SHA-256 |
|---|---|---|---|---:|---|
| Candidate `1825bcb…` | [45125499836](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45125499836) | 29 | [14796316382](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45125499836/artifacts/14796316382) | 509284 | `3fec021df9c8dc7ddb72959c41de39c3251e237f4644edd9d4172b2a2b0b56e5` |
| Candidate `1825bcb…` | [45125499836](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45125499836) | 35 | [14796246301](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45125499836/artifacts/14796246301) | 1151744 | `176057cde433d6a04d3d4e33119225a4de0588567cbae8e38fbc1d335387c28e` |
| Merged `14df739…` | [45126868951](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45126868951) | 29 | [14796482327](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45126868951/artifacts/14796482327) | 513576 | `54cfc58519a09bcfd6c66614792768a1b5d74a8d8e95e2df9b5400d9485854b2` |
| Merged `14df739…` | [45126868951](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45126868951) | 35 | [14796696168](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45126868951/artifacts/14796696168) | 1070736 | `3d7b2d68f4a13aa544505e507402990fec48dd75ed2a49a474e46be66693600d` |

GitHub artifact retention currently lists expiry **2027-02-21**, so preserve a separate cold archive when appropriate. The original audit ZIP also remains in the owner's ChatGPT Library. A GitHub Actions artifact page may require an authenticated GitHub session for download even when the source repository is visible.

## Rehydrate the original offline lab

In the audit folder in a GitHub-authenticated shell with `gh` installed, run:

```bash
mkdir -p evidence/receipts
gh api repos/MrSkyrimFan/GZ-Mobile-001/actions/artifacts/14796316382/zip > evidence/receipts/14796316382.zip
gh api repos/MrSkyrimFan/GZ-Mobile-001/actions/artifacts/14796246301/zip > evidence/receipts/14796246301.zip
gh api repos/MrSkyrimFan/GZ-Mobile-001/actions/artifacts/14796482327/zip > evidence/receipts/14796482327.zip
gh api repos/MrSkyrimFan/GZ-Mobile-001/actions/artifacts/14796696168/zip > evidence/receipts/14796696168.zip
python proofs/verify_ci.py
```

Before trusting the replay, compare `sha256sum evidence/receipts/*.zip` to the exact hashes above and to `evidence/final_artifacts.json`. The script checks matching case sets, failed/error/skipped counts, receipt CRCs, log identities and zero exit statuses. An HTTP error or GitHub login page is not a valid ZIP. These receipts **do not** close device-only acceptance.

Candidate handset-test APK: [artifact 14796246293](https://github.com/MrSkyrimFan/GZ-Mobile-001/actions/runs/45125499836/artifacts/14796246293), app-debug.apk SHA256 `618e3182e0e19f191c08b58078feb5e158ad8f894fe85ead0ea1ee56bae9b5e4`. It is a debug QA APK, not a production-signed release.
