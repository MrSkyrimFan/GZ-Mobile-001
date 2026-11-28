#!/usr/bin/env bash
set -euo pipefail
mkdir -p gif-seek-context
{
  git rev-parse HEAD
  uname -a
  lscpu
  adb shell getprop ro.build.fingerprint
  adb shell getprop dalvik.vm.heapgrowthlimit
} > gif-seek-context/environment.txt
adb logcat -c
set +e
gradle --no-daemon --stacktrace :app:connectedDebugAndroidTest 2>&1 | tee gif-seek-context/test.txt
status=${PIPESTATUS[0]}
set -e
adb logcat -d -v threadtime 'GzGifSeekProbe:I' 'GzGifTiming:I' '*:S' | tee gif-seek-context/app.log
exit "$status"
