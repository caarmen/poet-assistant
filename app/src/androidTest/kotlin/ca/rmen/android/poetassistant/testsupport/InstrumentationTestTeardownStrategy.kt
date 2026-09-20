package ca.rmen.android.poetassistant.testsupport

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstrumentationTestTeardownStrategy @Inject constructor() : TestTeardownStrategy {
    // For now, no cleanup specific only to instrumentation tests
    override fun tearDown() = Unit
}