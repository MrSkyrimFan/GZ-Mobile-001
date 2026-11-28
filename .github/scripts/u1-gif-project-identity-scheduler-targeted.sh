#!/usr/bin/env bash
set -euo pipefail
mkdir -p gif-identity-verification
{
  git rev-parse HEAD
  uname -a
  lscpu
  adb shell getprop ro.build.fingerprint
  adb shell getprop dalvik.vm.heapgrowthlimit
} > gif-identity-verification/environment.txt
adb logcat -c
set +e
gradle --no-daemon --stacktrace :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.greenezone.gzphotoeditor.AndroidGifProjectIdentityTest' 2>&1 | tee gif-identity-verification/test.txt
status=${PIPESTATUS[0]}
set -e
adb logcat -d -v threadtime 'ComposeInternal:E' 'TestRunner:I' '*:S' | tee gif-identity-verification/app.log
exit "$status"
