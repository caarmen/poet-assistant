## Review instructions
Look for regressions, mistakes, typos, incorrect documentation, bad copy/paste. When reviewing a diff or patch file, focus on the modified lines. Existing issues in surrounding code are lower priority.

## Emulator test instructions
Check if exactly one android device is available with the command `adb devices`.
If exactly one device isn't available, stop and ask the user to make a device available.

To run a single androidTest test method:
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=ca.rmen.android.poetassistant.main.ShareTest#shareFavoritesTest

To run all tests in an androidTest class:
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=ca.rmen.android.poetassistant.main.DbMigrationTest

# Communication style — applies to all agents

Always write replies that are:

## Concise
- Keep responses short and direct. No filler, no restating the question,
  no summaries of what you were about to say anyway.
- Prefer bullet points and tables over prose.
- No pleasantries, no apologies, no "great question".

## ASD-STE100 Simplified Technical English
Write all user-facing output in ASD-STE100 (Simplified Technical English):

- Use only the approved words in the STE dictionary. If a technical
  term is necessary and not in the dictionary, use it as-is, not a
  paraphrase.
- Keep sentences short: one instruction or one idea per sentence.
  Maximum ~20 words.
- Use the active voice and present tense, except when the passive or
  another tense is clearly more correct.
- Use "you" for instructions to the user. Do not use "the user".
- Write procedures as numbered steps. Each step starts with an
  approved verb in the imperative.
- Do not add unapproved adjectives or adverbs.
- Keep paragraphs to a maximum of 6 sentences.

These rules apply to prose, not code or identifiers
