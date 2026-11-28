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
status=0
prefix=com.greenezone.gzphotoeditor
selectors="$prefix.AndroidGifFidelityTest,$prefix.AndroidGifPreparationProfileTest,$prefix.AndroidGifProjectIdentityTest,$cls#$expected_edited,$cls#$expected_seek"
for trial in stock_prefix same_dispatch; do
  adb logcat -c
  if [[ "$trial" == same_dispatch ]]; then selectors="$cls#$expected_seek"; fi
  if ! adb shell am instrument -w -r -e class "$selectors" -e gzGifPauseSeekMode "$trial" \
      "$test_id/androidx.test.runner.AndroidJUnitRunner" | tee "gif-seek-probe/$trial.txt"; then
    status=1
  fi
  adb logcat -d -v threadtime 'GzGifSeekProbe:I' 'GzGifTiming:I' '*:S' | tee "gif-seek-probe/$trial.log"
  if ! grep -Fx "INSTRUMENTATION_STATUS: test=$expected_seek" "gif-seek-probe/$trial.txt" || \
      ! grep -E '^OK \([1-9][0-9]* tests?\)$' "gif-seek-probe/$trial.txt"; then status=1; fi
  if [[ "$trial" == stock_prefix ]]; then adb shell am force-stop "$app_id"; fi
done
exit "$status"
