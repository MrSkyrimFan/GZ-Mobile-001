#!/usr/bin/env bash
set -euo pipefail
mkdir -p gif-seek-probe
{
  git rev-parse HEAD
  uname -a
  lscpu
  adb shell getprop ro.build.fingerprint
  adb shell getprop dalvik.vm.heapgrowthlimit
} > gif-seek-probe/environment.txt
adb logcat -c
set +e
gradle --no-daemon --stacktrace :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.greenezone.gzphotoeditor.AndroidGifWorkspacePlaybackTest#editedVideoKeepsAdvancingAcrossReverseLookAndSpeedChanges,com.greenezone.gzphotoeditor.AndroidGifWorkspacePlaybackTest#mp4PlaysPausesAndSeeksThroughActualEditorControls' 2>&1 | tee gif-seek-probe/test.txt
status=${PIPESTATUS[0]}
set -e
adb logcat -d -v threadtime 'GzGifSeekProbe:I' 'GzGifTiming:I' '*:S' | tee gif-seek-probe/app.log
exit "$status"
