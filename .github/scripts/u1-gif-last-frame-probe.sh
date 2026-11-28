#!/usr/bin/env bash
set -euo pipefail
mkdir -p gif-seek-probe
{
  git rev-parse HEAD
  adb shell getprop ro.build.fingerprint
  adb shell getprop dalvik.vm.heapgrowthlimit
} > gif-seek-probe/environment.txt
app_apk=app/build/outputs/apk/debug/app-debug.apk
test_apk=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
aapt="$ANDROID_HOME/build-tools/36.0.0/aapt"
app_id="$("$aapt" dump badging "$app_apk" | sed -n "1s/^package: name='\([^']*\)'.*$/\1/p")"
test_id="$("$aapt" dump badging "$test_apk" | sed -n "1s/^package: name='\([^']*\)'.*$/\1/p")"
[[ "$app_id" == com.greenezone.gzphotoeditor.phonetest.v* && "$test_id" == "$app_id.test" ]]
sha256sum "$app_apk" "$test_apk" > gif-seek-probe/apk-sha256.txt
adb install -r "$app_apk"
adb install -r "$test_apk"
adb shell pm clear "$app_id" | grep -Fx Success
adb logcat -c
cls=com.greenezone.gzphotoeditor.AndroidGifWorkspacePlaybackTest
expected_edited=editedVideoKeepsAdvancingAcrossReverseLookAndSpeedChanges
expected_seek=mp4PlaysPausesAndSeeksThroughActualEditorControls
set +e
adb shell am instrument -w -r -e class "$cls#$expected_edited,$cls#$expected_seek" \
    "$test_id/androidx.test.runner.AndroidJUnitRunner" | tee gif-seek-probe/test.txt
status=${PIPESTATUS[0]}
set -e
adb logcat -d -v threadtime 'GzGifSeekProbe:I' 'GzGifTiming:I' '*:S' | tee gif-seek-probe/app.log
grep -Fx "INSTRUMENTATION_STATUS: test=$expected_edited" gif-seek-probe/test.txt
grep -Fx "INSTRUMENTATION_STATUS: test=$expected_seek" gif-seek-probe/test.txt
grep -Fx 'OK (2 tests)' gif-seek-probe/test.txt
exit "$status"
