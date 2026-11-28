#!/usr/bin/env bash
set -euo pipefail

bash -n .github/scripts/android-test-with-receipts.sh
export ORG_GRADLE_PROJECT_gzPhoneTestInstall=true
receipt_dir=r12-standalone-receipts
mkdir -p "$receipt_dir"
: > "$receipt_dir/checkout.txt"
git rev-parse HEAD | tee "$receipt_dir/checkout.txt"
adb shell getprop dalvik.vm.heapsize | tee "$receipt_dir/guest-heap.txt"
gradle --no-daemon --stacktrace :app:assembleDebug :app:assembleDebugAndroidTest
apk=app/build/outputs/apk/debug/app-debug.apk
test_apk=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
aapt="$ANDROID_HOME/build-tools/36.0.0/aapt"
app_id="$("$aapt" dump badging "$apk" | sed -n "1s/^package: name='\([^']*\)'.*$/\1/p")"
test_id="$("$aapt" dump badging "$test_apk" | sed -n "1s/^package: name='\([^']*\)'.*$/\1/p")"
[[ "$app_id" == com.greenezone.gzphotoeditor.phonetest.v* && "$test_id" == "$app_id.test" ]]
sha256sum "$apk" "$test_apk" | tee "$receipt_dir/apk-sha256.txt"
adb install -r "$apk"
adb install -r "$test_apk"
test_class='com.greenezone.gzphotoeditor.AndroidU1ProcessRecoveryTest#verifyNativeRecovery'
runner="$test_id/androidx.test.runner.AndroidJUnitRunner"
standalone_status=0
if adb shell pm clear "$app_id" | grep -Fx Success; then
  for phase in standalone-seed standalone-reopen; do
    if ! adb shell am instrument -w -r -e class "$test_class" -e u1ProcessPhase "$phase" "$runner" \
      | tee "$receipt_dir/r12-regular-process-$phase.txt"; then
      standalone_status=1
      break
    fi
    if ! grep -F 'OK (1 test)' "$receipt_dir/r12-regular-process-$phase.txt"; then
      standalone_status=1
      break
    fi
    if [[ "$phase" == standalone-seed ]]; then
      # Record whether instrumentation left a live target before the external stop.
      before_pid="$(adb shell ps -A -o PID,NAME | awk -v app="$app_id" '$2 == app { print $1 }')"
      adb shell am force-stop "$app_id"
      after_pid="$(adb shell ps -A -o PID,NAME | awk -v app="$app_id" '$2 == app { print $1 }')"
      printf 'external force-stop package=%s before=%s after=%s\n' \
        "$app_id" "${before_pid:-absent}" "${after_pid:-absent}" \
        | tee "$receipt_dir/r12-regular-process-force-stop.txt"
      if [[ -n "$after_pid" ]]; then
        standalone_status=1
        break
      fi
    fi
  done
else
  standalone_status=1
fi
adb logcat -d -s GzR12Process:I | tee "$receipt_dir/r12-regular-process-logcat.txt"
adb logcat -d | tail -n 700 > "$receipt_dir/last-logcat.txt"
exit "$standalone_status"
