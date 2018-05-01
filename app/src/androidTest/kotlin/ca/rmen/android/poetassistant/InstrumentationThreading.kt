/*
 * Copyright (c) 2017 Carmen Alvarez
 *
 * This file is part of Poet Assistant.
 *
 * Poet Assistant is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Poet Assistant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Poet Assistant.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.rmen.android.poetassistant

import android.support.test.espresso.idling.CountingIdlingResource
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI

/**
 * Keeps track of all the background and foreground tasks we've submitted, so that tests
 * can pause while we wait for tasks to complete.
 */
class InstrumentationThreading : CoroutineThreading(CommonPool, UI) {

    companion object {
        private val TAG = Constants.TAG + InstrumentationThreading::class.java.simpleName
    }

    private val mCountingIdlingResource: CountingIdlingResource

    init {
        mCountingIdlingResource = CountingIdlingResource(TAG + System.currentTimeMillis(), true)
    }

    fun getCountingIdlingResource() = mCountingIdlingResource

    override fun executeForeground(delayMs: Long, body: () -> Unit) : Threading.Cancelable {
        // We don't count the idling resource if there's a delay. This is to avoid making tests
        // block until the word count of a poem text is calculated. This blocking can slow down
        // tests significantly.
        // Tests that need to test the word count behavior will need to add a sleep to wait
        // for the word count to be available.
        return if (delayMs == 0L) {
            mCountingIdlingResource.increment()
            super.executeForeground(delayMs, decorateForegroundTask(body))
        } else {
            super.executeForeground(delayMs, body)
        }
    }

    override fun <T> execute(backgroundTask: () -> T, foregroundTask: ((T) -> Unit)?, errorTask: ((Throwable) -> Unit)?) {
        mCountingIdlingResource.increment()
        super.execute(backgroundTask, decorateForegroundTask(foregroundTask), decorateErrorTask(errorTask))
    }

    private fun decorateErrorTask(errorTask: ((Throwable) -> Unit)?): ((Throwable) -> Unit)? {
        return { throwable ->
            try {
                errorTask?.invoke(throwable)
            } finally {
                mCountingIdlingResource.decrement()
            }
        }
    }

    private fun <T> decorateForegroundTask(foregroundTask: ((T) -> Unit)?): ((T) -> Unit)? {
        return { value ->
            try {
                foregroundTask?.invoke(value)
            } finally {
                mCountingIdlingResource.decrement()
            }
        }
    }

    private fun decorateForegroundTask(foregroundTask: () -> Unit): (() -> Unit) {
        return {
            try {
                foregroundTask.invoke()
            } finally {
                mCountingIdlingResource.decrement()
            }
        }
    }
}