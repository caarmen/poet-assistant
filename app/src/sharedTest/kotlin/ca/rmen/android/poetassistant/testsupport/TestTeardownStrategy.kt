package ca.rmen.android.poetassistant.testsupport


/**
 * Strategy for test cleanup after a test finishes.
 * Put cleanup specific to instrumentation tests (src/androidTest) vs jvm/robolectric tests
 * (src/test) in the concrete implementations in those sourcesets.
 */
interface TestTeardownStrategy {
    fun tearDown()
}