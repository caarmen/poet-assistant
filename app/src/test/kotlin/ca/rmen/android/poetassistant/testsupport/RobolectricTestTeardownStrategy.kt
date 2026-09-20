package ca.rmen.android.poetassistant.testsupport

import ca.rmen.android.poetassistant.UserDb
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RobolectricTestTeardownStrategy @Inject constructor(val userDb: UserDb) :
    TestTeardownStrategy {

    override fun tearDown() {
        // In Robolectric, each test gets a fresh Application instance, so the
        // @Singleton UserDb can be closed to avoid leaking SQLiteConnectionPools.
        userDb.close()
    }
}