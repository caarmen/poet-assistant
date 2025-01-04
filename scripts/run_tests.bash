adb lolcat -v threadtime > /tmp/lolcat.log 2>&1 &

adb shell settings put global animator_duration_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global window_animation_scale 0

./gradlew --no-daemon testDebugUnitTest cAT jacocoTestReport -Pandroid.testInstrumentationRunnerArguments.notClass="ca.rmen.android.poetassistant.main.ScreenshotTest"