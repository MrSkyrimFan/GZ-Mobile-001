#!/usr/bin/env bash
set -euo pipefail
mkdir -p r11-attribution
{
  git rev-parse HEAD
  uname -a
  lscpu
  adb shell getprop ro.build.fingerprint
  adb shell getprop dalvik.vm.heapgrowthlimit
} > r11-attribution/environment.txt
adb logcat -c
set +e
gradle --no-daemon --stacktrace :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.greenezone.gzphotoeditor.AndroidRegularProjectListAcceptanceTest#twelveLegacyRecipesMigrateAndTheReadyIndexDoesNoListDecodes' 2>&1 | tee r11-attribution/test.txt
status=${PIPESTATUS[0]}
set -e
adb logcat -d -v threadtime 'GzR11Probe:I' 'GzR11Acceptance:I' '*:S' | tee r11-attribution/app.log
exit "$status"
