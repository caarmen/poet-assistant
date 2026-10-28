/*
 * Copyright (c) 2026-present Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.wotd.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import java.util.concurrent.TimeUnit

/**
 * The unique periodic work which posts the WOTD notification.
 *
 * Everything that schedules or cancels the periodic work must go through
 * this object: the unique work name must be the same everywhere, otherwise
 * two pipelines with two schedules could run in parallel.
 */
object WotdPeriodicWork {

    /**
     * Name of the unique periodic work for the WOTD notification.
     */
    const val WORK_NAME = "wotd"

    /**
     * The notification repeats every 24 h.
     */
    private const val PERIOD_HOURS = 24L

    /**
     * Enqueues the unique periodic work.
     *
     * @param context A context for obtaining the WorkManager.
     * @param policy KEEP leaves an already scheduled periodic work
     * untouched: use it when the work may already be running. REPLACE
     * resets the 24 h window: use it when the schedule must restart, for
     * example on a settings change.
     */
    fun enqueue(context: Context, policy: ExistingPeriodicWorkPolicy) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, policy, buildRequest())
    }

    /**
     * Cancels the unique periodic work, if scheduled.
     */
    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private fun buildRequest(): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<WotdWorker>(PERIOD_HOURS, TimeUnit.HOURS)
            // WorkManager clamps the backoff duration to its maximum
            // (5 h). Use the maximum: the slowest allowed retry for a
            // failing DB.
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MAX_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()
}
