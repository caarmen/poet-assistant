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

import android.util.Log
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext

open class CoroutineThreading(private val background: CoroutineContext, private val foreground: CoroutineContext) : Threading {

    companion object {
        private val TAG = Constants.TAG + CoroutineThreading::class.java.simpleName
    }

    override fun executeForeground(delayMs: Long, body: () -> Unit): Threading.Cancelable {
        val job = launch(foreground) {
            try {
                if (delayMs > 0) delay(delayMs, TimeUnit.MILLISECONDS)
                body.invoke()
            } catch (e: CancellationException) {
                Log.v(TAG, "Task cancelled")
            }
        }
        return CancelableJob(job)
    }

    override fun <T> execute(backgroundTask: () -> T, foregroundTask: ((T) -> Unit)?, errorTask: ((Throwable) -> Unit)?) {
        launch(foreground) {
            val task = async(background) { backgroundTask.invoke() }
            try {
                val result = task.await()
                foregroundTask?.invoke(result)

            } catch (t: Throwable) {
                Log.v(TAG, "Error running background task", t)
                errorTask?.invoke(t)
            }
        }
    }

    private class CancelableJob(val job: Job) : Threading.Cancelable {
        override fun cancel() {
            job.cancel()
        }
    }
}