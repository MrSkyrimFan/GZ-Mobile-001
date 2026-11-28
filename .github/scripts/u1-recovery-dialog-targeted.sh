#!/usr/bin/env bash
set -euo pipefail
ordinary_status=0
# Process recovery is an independent verification. A runtime failure above
# must not discard this evidence when the exact instrumentation APK was built.
if [[ -s app/build/outputs/apk/debug/app-debug.apk &&
      -s app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk ]]; then
  apk="app/build/outputs/apk/debug/app-debug.apk"
  test_apk="app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
  aapt="${ANDROID_HOME}/build-tools/36.0.0/aapt"
  app_id="$("$aapt" dump badging "$apk" | sed -n "1s/^package: name='\([^']*\)'.*$/\1/p")"
  test_id="$("$aapt" dump badging "$test_apk" | sed -n "1s/^package: name='\([^']*\)'.*$/\1/p")"
  [[ "$app_id" == com.greenezone.gzphotoeditor.phonetest.v* && "$test_id" == "$app_id.test" ]]
  adb install -r "$apk"
  adb install -r "$test_apk"
  adb shell pm clear "$app_id" | grep -Fx Success

  receipt_dir=app/build/outputs/androidTest-results/connected
  mkdir -p "$receipt_dir"
  test_class='com.greenezone.gzphotoeditor.AndroidU1ProcessRecoveryTest#verifyNativeRecovery'
  runner="$test_id/androidx.test.runner.AndroidJUnitRunner"
  recovery_status=0
  for phase in seed reopen; do
    if ! adb shell am instrument -w -r -e class "$test_class" -e u1ProcessPhase "$phase" "$runner" \
      | tee "$receipt_dir/u1-native-process-$phase.txt"; then
      recovery_status=1
      break
    fi
    if ! grep -F 'OK (1 test)' "$receipt_dir/u1-native-process-$phase.txt"; then
      recovery_status=1
      break
    fi
    if [[ "$phase" == seed ]]; then
      adb shell am force-stop "$app_id"
    fi
  done
  adb logcat -d -s GzU1Process:I | tee "$receipt_dir/u1-native-process-logcat.txt"
  if [[ "$recovery_status" != 0 ]]; then exit "$recovery_status"; fi
elif [[ "$ordinary_status" == 0 ]]; then
  echo "Missing built APKs for process recovery" >&2
  exit 1
fi
exit "$ordinary_status"
