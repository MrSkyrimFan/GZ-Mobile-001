#!/usr/bin/env bash
set -euo pipefail

# Only the Android 10 CI job opts in. These are emulator properties, not app flags.
if [[ -n "${GZ_CI_HEAP_MIB:-}" ]]; then
  [[ "$GZ_CI_HEAP_MIB" == "256" ]] || {
    echo "Unsupported CI heap budget: $GZ_CI_HEAP_MIB" >&2
    exit 1
  }
  adb_ci() { timeout 15s adb -s emulator-5554 "$@"; }
  guest_prop() { adb_ci shell getprop "$1" | tr -d '\r'; }
  # pidof can include other processes temporarily sharing the zygote64 name.
  # The framework VM owner is the single zygote64 whose parent is init (PID 1).
  zygote_owner() {
    adb_ci shell ps -A -o PID,PPID,NAME | tr -d '\r' |
      awk '$2 == 1 && $3 == "zygote64" { print $1 }'
  }
  [[ "$(guest_prop ro.kernel.qemu)" == "1" ]] || {
    echo "Heap setup requires the CI emulator." >&2
    exit 1
  }
  [[ "$(guest_prop ro.build.version.sdk)" == "29" ]] || {
    echo "Heap setup is restricted to the Android 10 CI job." >&2
    exit 1
  }
  echo "Android 10 heap before setup:"
  echo "dalvik.vm.heapsize=$(guest_prop dalvik.vm.heapsize)"
  echo "dalvik.vm.heapgrowthlimit=$(guest_prop dalvik.vm.heapgrowthlimit)"
  old_zygote=$(zygote_owner)
  [[ "$old_zygote" =~ ^[0-9]+$ ]] || {
    echo "Expected one running Android 10 zygote64." >&2
    exit 1
  }

  adb_ci root
  adb_ci wait-for-device
  [[ "$(adb_ci shell id -u | tr -d '\r')" == "0" ]] || {
    echo "The CI image must allow adb root to set its heap properties." >&2
    exit 1
  }
  # AndroidRuntime reads these when the zygote starts. Setting them after boot
  # alone cannot change heaps inherited by subsequently launched applications.
  adb_ci shell stop
  stop_deadline=$((SECONDS + 30))
  while [[ "$(guest_prop init.svc.zygote)" != "stopped" ]]; do
    (( SECONDS < stop_deadline )) || {
      echo "Android zygote did not stop within 30 seconds." >&2
      exit 1
    }
    sleep 1
  done
  adb_ci shell setprop dalvik.vm.heapsize 256m
  adb_ci shell setprop dalvik.vm.heapgrowthlimit 256m
  adb_ci shell setprop sys.boot_completed 0
  adb_ci shell start

  deadline=$((SECONDS + 120))
  booted=false
  while (( SECONDS < deadline )); do
    if [[ "$(guest_prop sys.boot_completed)" == "1" ]]; then
      booted=true
      break
    fi
    sleep 2
  done
  [[ "$booted" == "true" ]] || {
    echo "Android framework did not restart within 120 seconds." >&2
    exit 1
  }
  new_zygote=$(zygote_owner)
  zygote_state=$(guest_prop init.svc.zygote)
  heap=$(guest_prop dalvik.vm.heapsize)
  growth=$(guest_prop dalvik.vm.heapgrowthlimit)
  echo "Android 10 heap after framework restart:"
  echo "init-owned zygote64=$old_zygote -> $new_zygote; state=$zygote_state"
  echo "dalvik.vm.heapsize=$heap"
  echo "dalvik.vm.heapgrowthlimit=$growth"
  [[ "$new_zygote" =~ ^[0-9]+$ && "$new_zygote" != "$old_zygote" && "$zygote_state" == "running" ]] || {
    echo "A fresh zygote64 was not verified." >&2
    exit 1
  }
  [[ "$heap" == "256m" && "$growth" == "256m" ]] || {
    echo "Android 10 guest heap preflight failed." >&2
    exit 1
  }
  # The framework restart can re-enable the keyguard; retain runner behavior.
  adb_ci shell input keyevent 82
fi

ordinary_status=0
if gradle --no-daemon --stacktrace :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.greenezone.gzphotoeditor.AndroidRegularDepartureContractTest#u1IdenticalDerivativeBytesDoNotReplayAnAlreadyLinkedRegularFinish'; then
  echo "Android instrumentation suite: PASS"
else
  ordinary_status=$?
  echo "=== Failed Android test XML receipts (redacted to first 220 lines per file) ==="
  while IFS= read -r -d '' file; do
    echo "==== Test receipt: ${file} ===="
    sed -n '1,220p' "$file"
  done < <(find app/build/outputs/androidTest-results/connected -type f -name '*.xml' -print0 2>/dev/null)
fi

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
  for phase in seed reopen; do
    adb shell am instrument -w -r -e class "$test_class" -e u1ProcessPhase "$phase" "$runner" \
      | tee "$receipt_dir/u1-native-process-$phase.txt"
    grep -F 'OK (1 test)' "$receipt_dir/u1-native-process-$phase.txt"
    if [[ "$phase" == seed ]]; then
      adb shell am force-stop "$app_id"
    fi
  done
  adb logcat -d -s GzU1Process:I | tee "$receipt_dir/u1-native-process-logcat.txt"
elif [[ "$ordinary_status" == 0 ]]; then
  echo "Missing built APKs for process recovery" >&2
  exit 1
fi
exit "$ordinary_status"
