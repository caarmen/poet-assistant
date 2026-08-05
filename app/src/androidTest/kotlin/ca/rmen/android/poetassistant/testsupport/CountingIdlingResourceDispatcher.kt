package ca.rmen.android.poetassistant.testsupport

import androidx.test.espresso.IdlingResource
import androidx.test.espresso.idling.CountingIdlingResource
import decrementDelayed
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.CoroutineContext

class CountingIdlingResourceDispatcher(
    label: String,
    private val delegate: CoroutineDispatcher,
) : CoroutineDispatcher() {

    private val idlingResource = CountingIdlingResource("TrackingTestDispatcher-$label", true)

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        idlingResource.increment()
        delegate.dispatch(context) {
            try {
                block.run()
            } finally {
                idlingResource.decrementDelayed(20)
            }
        }
    }

    fun getIdlingResource(): IdlingResource = idlingResource
}


