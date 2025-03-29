#!/usr/bin/env bash

./gradlew assembleDebug assembleDebugAndroidTest
mapfile -t devices < <(adb devices | grep "device$" | sed -e 's/\t.*$//g')

# Install the app and test apk to the devices.
for device in "${devices[@]}"; 
do
  echo "Installing apks to ${device}"
  adb -s "${device}" install -r ./app/build/outputs/apk/debug/app-debug.apk
  adb -s "${device}" install -r ./app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
done

run_tests() {
  device="$1"
  output="/tmp/${device}.log"
  echo "Running tests on ${device}, output to ${output}"
  adb -s "${device}" shell settings put global animator_duration_scale 0
  adb -s "${device}" shell settings put global transition_animation_scale 0
  adb -s "${device}" shell settings put global window_animation_scale 0
  adb -s "${device}" shell am instrument -w -m -e debug false -e class ca.rmen.android.poetassistant.main.ScreenshotTest ca.rmen.android.poetassistant.test.test/ca.rmen.android.poetassistant.di.CustomTestRunner > "${output}" 2>&1

  echo "pulling screenshots to ${device}-light and ${device}-dark"
  screenshots_local_folder="screenshots"
  mkdir -p "${screenshots_local_folder}"
  if [ -e "${screenshots_local_folder}/${device}-light" ]
  then
    rm -rf "${screenshots_local_folder}/${device}-light"
  fi
  if [ -e "${screenshots_local_folder}/${device}-dark" ]
  then
    rm -rf "${screenshots_local_folder}/${device}-dark"
  fi
  base_folder=/storage/emulated/0/Android/data/ca.rmen.android.poetassistant.test/files
  adb -s "${device}" pull "${base_folder}/screenshots-Light" "${screenshots_local_folder}/${device}-light"
  adb -s "${device}" pull "${base_folder}/screenshots-Dark" "${screenshots_local_folder}/${device}-dark"
}

device_count=${#devices[@]}
for device in "${devices[@]}";
do
  if [ "${device_count}" = 1 ]
  # Run blocking if there's only one device
  then
    run_tests "$device"
  # Else spawn the test run so we can run multiple devices in parallel
  else
    run_tests "$device" &
  fi
done

