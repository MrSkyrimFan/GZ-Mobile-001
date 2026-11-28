#!/usr/bin/env bash
set -euo pipefail
mkdir -p gif-paused-request
app_apk=app/build/outputs/apk/debug/app-debug.apk
test_apk=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
aapt="$ANDROID_HOME/build-tools/36.0.0/aapt"
app_id="$("$aapt" dump badging "$app_apk" | sed -n "1s/^package: name='\([^']*\)'.*$/\1/p")"
test_id="$("$aapt" dump badging "$test_apk" | sed -n "1s/^package: name='\([^']*\)'.*$/\1/p")"
[[ "$app_id" == com.greenezone.gzphotoeditor.phonetest.v* && "$test_id" == "$app_id.test" ]]
{
  git rev-parse HEAD
  adb shell getprop ro.build.fingerprint
  adb shell getprop dalvik.vm.heapgrowthlimit
  sha256sum "$app_apk" "$test_apk"
} > gif-paused-request/environment.txt
adb install -r "$app_apk"
adb install -r "$test_apk"
adb shell pm clear "$app_id" | grep -Fx Success
class=com.greenezone.gzphotoeditor.AndroidGifWorkspacePlaybackTest
status=0
for method in mp4PlaysPausesAndSeeksThroughActualEditorControls \
    mp4FinalSeekSurvivesAnOlderPausedReadCompleting \
    mp4FinalSeekSurvivesAnOlderPausedReadFailing; do
  adb shell am force-stop "$app_id"
  adb logcat -c
  if ! adb shell am instrument -w -r -e class "$class#$method" \
      "$test_id/androidx.test.runner.AndroidJUnitRunner" | tee "gif-paused-request/$method.txt"; then
    status=1
  fi
  adb logcat -d -v threadtime 'GzGifSeekProbe:I' 'GzGifTiming:I' '*:S' |
    tee "gif-paused-request/$method.log"
  if ! grep -Fx "INSTRUMENTATION_STATUS: test=$method" "gif-paused-request/$method.txt" ||
      ! grep -Fx 'OK (1 test)' "gif-paused-request/$method.txt"; then
    status=1
  fi
done
exit "$status"
