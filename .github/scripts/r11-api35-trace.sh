#!/usr/bin/env bash
set -euo pipefail
out="$GITHUB_WORKSPACE/r11-receipt"
mkdir -p "$out"
cd "$GITHUB_WORKSPACE/source"
device_path=/data/misc/perfetto-traces/r11-api35.pftrace
apk=app/build/outputs/apk/debug/app-debug.apk
test_apk=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
aapt="$ANDROID_HOME/build-tools/36.0.0/aapt"
app_id="$("$aapt" dump badging "$apk" | sed -n "1s/^package: name='\([^']*\)'.*$/\1/p")"
test_id="$("$aapt" dump badging "$test_apk" | sed -n "1s/^package: name='\([^']*\)'.*$/\1/p")"
method='com.greenezone.gzphotoeditor.AndroidRegularProjectListAcceptanceTest#twelveLegacyRecipesMigrateAndTheReadyIndexDoesNoListDecodes'
runner="$test_id/androidx.test.runner.AndroidJUnitRunner"
[[ "$app_id" == com.greenezone.gzphotoeditor.phonetest.v27 && "$test_id" == "$app_id.test" ]]
[[ "$(git rev-parse HEAD)" == "$R11_SOURCE_SHA" ]]
[[ "$(adb shell getprop ro.build.version.sdk | tr -d '\r')" == 35 ]]
[[ "$(adb shell getprop ro.kernel.qemu | tr -d '\r')" == 1 ]]
{
  printf 'candidate=%s\ncandidate_tree=%s\n' "$(git rev-parse HEAD)" "$(git rev-parse HEAD^{tree})"
  printf 'diagnostic_head=%s\nrun=%s attempt=%s\n' "$GITHUB_SHA" "$GITHUB_RUN_ID" "$GITHUB_RUN_ATTEMPT"
  printf 'command=adb shell am instrument -w -r -e class %q %q\n' "$method" "$runner"
  for prop in ro.build.fingerprint ro.build.type ro.product.cpu.abi dalvik.vm.heapgrowthlimit dalvik.vm.heapsize; do
    printf '%s=%s\n' "$prop" "$(adb shell getprop "$prop" | tr -d '\r')"
  done
  adb shell perfetto --version
  sha256sum "$apk" "$test_apk"
} | tee "$out/configuration.txt"
adb install -r "$apk"
adb install -r "$test_apk"
adb shell pm clear "$app_id" | grep -Fx Success
adb shell atrace --list_categories > "$out/atrace-categories.txt"
adb shell perfetto --query > "$out/perfetto-query.txt"
cat > "$out/r11.pbtxt" <<'CONFIG'
duration_ms: 90000
buffers { size_kb: 65536 fill_policy: RING_BUFFER }
data_sources {
  config {
    name: "linux.ftrace"
    ftrace_config {
      ftrace_events: "sched/sched_switch"
      ftrace_events: "sched/sched_waking"
      ftrace_events: "sched/sched_wakeup_new"
      ftrace_events: "sched/sched_blocked_reason"
      ftrace_events: "sched/sched_process_exit"
      ftrace_events: "sched/sched_process_free"
      ftrace_events: "task/task_newtask"
      ftrace_events: "task/task_rename"
      atrace_categories: "dalvik"
      disable_generic_events: true
    }
  }
}
data_sources {
  config {
    name: "linux.process_stats"
    process_stats_config { scan_all_processes_on_start: true }
  }
}
CONFIG
if grep -Fq 'android.log' "$out/perfetto-query.txt"; then
  cat >> "$out/r11.pbtxt" <<'LOG_CONFIG'
data_sources {
  config {
    name: "android.log"
    android_log_config { log_ids: LID_DEFAULT filter_tags: "GzR11Acceptance" }
  }
}
LOG_CONFIG
fi
trace_pid="$(adb shell perfetto --txt -c - -o "$device_path" --background-wait \
  < "$out/r11.pbtxt" 2> "$out/perfetto-start.txt" | tr -d '\r')"
[[ "$trace_pid" =~ ^[1-9][0-9]*$ ]]
printf 'perfetto_pid=%s\n' "$trace_pid" >> "$out/configuration.txt"
adb shell cat /proc/stat > "$out/guest-stat-before.txt"
set +e
timeout 75s adb shell am instrument -w -r -e class "$method" "$runner" | tee "$out/instrumentation.txt"
test_status=${PIPESTATUS[0]}
set -e
adb shell kill -TERM "$trace_pid" > "$out/perfetto-stop.txt" 2>&1 || true
for _ in $(seq 1 38); do
  if ! adb shell "test -d /proc/$trace_pid" >/dev/null 2>&1; then break; fi
  sleep .5
done
adb shell cat /proc/stat > "$out/guest-stat-after.txt"
adb logcat -b main -d -v threadtime -v epoch -v usec \
  | grep -E 'GzR11Acceptance:|TestRunner:|r\.phonetest\.v27:' > "$out/r11-logcat.txt"
adb pull "$device_path" "$out/r11.pftrace"
test -s "$out/r11.pftrace"
printf 'instrumentation_adb_status=%s\n' "$test_status" | tee -a "$out/configuration.txt"
(( test_status == 0 )) && grep -Fq 'OK (1 test)' "$out/instrumentation.txt"
