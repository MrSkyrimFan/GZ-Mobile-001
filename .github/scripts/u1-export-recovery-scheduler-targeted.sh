#!/usr/bin/env bash
set -euo pipefail
mkdir -p export-recovery-verification
{
  git rev-parse HEAD
  uname -a
  lscpu
  adb shell getprop ro.build.fingerprint
  adb shell getprop dalvik.vm.heapgrowthlimit
} > export-recovery-verification/environment.txt
adb logcat -c
set +e
gradle --no-daemon --stacktrace :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.greenezone.gzphotoeditor.AndroidExportRecoveryTest' 2>&1 | tee export-recovery-verification/test.txt
status=${PIPESTATUS[0]}
set -e
adb logcat -d -v threadtime 'ComposeInternal:E' 'TestRunner:I' '*:S' | tee export-recovery-verification/app.log
exit "$status"
