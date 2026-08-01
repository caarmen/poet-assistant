## Review instructions
Look for regressions, mistakes, typos, incorrect documentation, bad copy/paste. When reviewing a diff or patch file, focus on the modified lines. Existing issues in surrounding code are lower priority.

## Emulator test instructions
Check if exactly one android device is available with the command `adb devices`.
If exactly one device isn't available, stop and ask the user to make a device available.

To run a single androidTest test method:
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=ca.rmen.android.poetassistant.main.ShareTest#shareFavoritesTest

To run all tests in an androidTest class:
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=ca.rmen.android.poetassistant.main.DbMigrationTest

