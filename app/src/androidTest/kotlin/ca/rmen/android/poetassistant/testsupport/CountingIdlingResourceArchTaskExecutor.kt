package ca.rmen.android.poetassistant.testsupport

import androidx.arch.core.executor.DefaultTaskExecutor
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.idling.CountingIdlingResource
import decrementDelayed

class CountingIdlingResourceArchTaskExecutor() : DefaultTaskExecutor() {
    private val idlingResource =
        CountingIdlingResource("CountingIdlingResourceArchTaskExecutor", true)

    override fun executeOnDiskIO(runnable: Runnable) {
        idlingResource.increment()
        try {
            super.executeOnDiskIO(runnable)
        } finally {
            idlingResource.decrementDelayed(200)
        }
    }

    override fun postToMainThread(runnable: Runnable) {
        idlingResource.increment()
        try {
            super.postToMainThread(runnable)
        } finally {
            idlingResource.decrementDelayed(200)
        }
    }

    fun getIdlingResource(): IdlingResource = idlingResource
}