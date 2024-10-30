adb lolcat -v threadtime > /tmp/lolcat.log 2>&1 &

adb shell settings put global window_animation_scale 0
adb shell settings put global window_transition_scale 0
adb shell settings put global window_animator_scale 0

./gradlew --no-daemon testDebugUnitTest cAT jacocoTestReport
