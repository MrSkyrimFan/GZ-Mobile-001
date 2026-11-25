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
  old_zygote=$(adb_ci shell pidof zygote64 | tr -d '\r')
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
  new_zygote=$(adb_ci shell pidof zygote64 | tr -d '\r')
  heap=$(guest_prop dalvik.vm.heapsize)
  growth=$(guest_prop dalvik.vm.heapgrowthlimit)
  echo "Android 10 heap after framework restart:"
  echo "zygote64=$old_zygote -> $new_zygote"
  echo "dalvik.vm.heapsize=$heap"
  echo "dalvik.vm.heapgrowthlimit=$growth"
  [[ "$new_zygote" =~ ^[0-9]+$ && "$new_zygote" != "$old_zygote" ]] || {
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

if gradle --no-daemon --stacktrace :app:connectedDebugAndroidTest; then
  echo "Android instrumentation suite: PASS"
else
  result=$?
  echo "=== Failed Android test XML receipts (redacted to first 220 lines per file) ==="
  while IFS= read -r -d '' file; do
    echo "==== Test receipt: ${file} ===="
    sed -n '1,220p' "$file"
  done < <(find app/build/outputs/androidTest-results/connected -type f -name '*.xml' -print0 2>/dev/null)
  exit "$result"
fi
