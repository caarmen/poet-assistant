/*
 * Copyright (c) 2016 - 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.wotd

import android.annotation.TargetApi
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import ca.rmen.android.poetassistant.Constants

/**
 * Word of the day task for API levels Lollipop and later.
 * This uses JobScheduler.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
object WotdJob {
    private val TAG = Constants.TAG + WotdJob::class.java.simpleName

    fun reschedule(context: Context) {
        Log.d(TAG, "reschedule $context")
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler?
        if (jobScheduler != null) {
            val jobs = jobScheduler.allPendingJobs
            Log.v(TAG, "PendingJobs : $jobs")
            if (jobs.isEmpty()) schedule(context)
        }
    }

    fun schedule(context: Context) {
        Log.d(TAG, "schedule $context")
        val jobInfo = JobInfo.Builder(TAG.hashCode(), ComponentName(context, WotdJobService::class.java))
                .setBackoffCriteria(Wotd.NOTIFICATION_FREQUENCY_MS, JobInfo.BACKOFF_POLICY_EXPONENTIAL)
                .setRequiresDeviceIdle(false)
                .setPeriodic(Wotd.NOTIFICATION_FREQUENCY_MS)
                .setPersisted(true)
                .setRequiresCharging(false)
                .build()
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler?
        jobScheduler?.schedule(jobInfo)
    }

    fun cancel(context: Context) {
        Log.d(TAG, "cancel $context")
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler?
        jobScheduler?.cancel(TAG.hashCode())
    }
}
