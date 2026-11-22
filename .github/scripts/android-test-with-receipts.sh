#!/usr/bin/env bash
set -o pipefail
if gradle --no-daemon --stacktrace :app:connectedDebugAndroidTest; then
  echo "Android instrumentation suite: PASS"
else
  result=$?
  echo "=== Failed Android test XML receipts (redacted to first 220 lines per file) ==="
  while IFS= read -r -d '' file; do
    echo "==== Test receipt: ${file} ===="
    sed -n '1,220p' "$file"
  done < <(find app/build/outputs/androidTest-results/connected -type f -name '*.xml' -print0 2>/dev/null)
  exit "$result"
fi
