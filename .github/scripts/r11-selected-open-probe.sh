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
app_apk=app/build/outputs/apk/debug/app-debug.apk
test_apk=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
aapt="$ANDROID_HOME/build-tools/36.0.0/aapt"
app_id="$("$aapt" dump badging "$app_apk" | sed -n "1s/^package: name='\([^']*\)'.*$/\1/p")"
test_id="$("$aapt" dump badging "$test_apk" | sed -n "1s/^package: name='\([^']*\)'.*$/\1/p")"
[[ "$app_id" == com.greenezone.gzphotoeditor.phonetest.v* && "$test_id" == "$app_id.test" ]]
sha256sum "$app_apk" "$test_apk" > r11-attribution/apk-sha256.txt
adb install -r "$app_apk"
adb install -r "$test_apk"
adb shell pm clear "$app_id" | grep -Fx Success
adb logcat -c
test_class=com.greenezone.gzphotoeditor.AndroidRegularProjectListAcceptanceTest
status=0
for phase in dense_held dense_repaired varied; do
  method=twelveLegacyRecipesMigrateAndTheReadyIndexDoesNoListDecodes
  if [[ "$phase" == varied ]]; then method=compareVariedInkOnTheSameGuest; fi
  adb shell am force-stop "$app_id"
  if ! adb shell am instrument -w -r -e class "$test_class#$method" -e gzInkProbePhase "$phase" \
    "$test_id/androidx.test.runner.AndroidJUnitRunner" | tee "r11-attribution/$phase.txt"; then
      status=1
  fi
  if ! grep -F 'OK (1 test)' "r11-attribution/$phase.txt"; then status=1; fi
  adb logcat -d -v threadtime 'GzR11Probe:I' 'GzR11Acceptance:I' '*:S' > "r11-attribution/$phase.log"
  cat "r11-attribution/$phase.log"
  adb logcat -c
done
exit "$status"
