# Some manual steps before:
# - Remove the @Ignore annotation from app/src/androidTest/java/ca/rmen/android/poetassistant/main/ScreenshotTest.java

./gradlew assembleDebug assembleDebugAndroidTest
mapfile -t devices < <(adb devices | grep "device$" | sed -e 's/\t.*$//g')

for device in "${devices[@]}"; 
do
  echo "Installing apks to $device"
  adb -s $device install -r ./app/build/outputs/apk/debug/app-debug.apk
  adb -s $device install -r ./app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
done

for device in "${devices[@]}"; 
do
  output="/tmp/$device.log"
  echo "Running tests on $device, output to $output"
  adb -s $device shell settings put global animator_duration_scale 0
  adb -s $device shell settings put global transition_animation_scale 0
  adb -s $device shell settings put global window_animation_scale 0
  adb -s $device shell am instrument -w -m -e debug false -e class ca.rmen.android.poetassistant.main.ScreenshotTest ca.rmen.android.poetassistant.test.test/androidx.test.runner.AndroidJUnitRunner > $output 2>&1 &
done

# Some more manual steps:
# - retrieve the screenshots using the device explorer. They will be in /sdcard/Pictures/screenshots. The /sdcard folder may be found in /mnt.
# - Save them to the different folders (tablet/light, phone/dark, etc).
# - use the rename.sh script to rename the files to strip the uuids from the names.
