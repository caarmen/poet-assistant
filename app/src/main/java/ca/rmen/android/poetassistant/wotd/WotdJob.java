/*
 * Copyright (c) 2016 Carmen Alvarez
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

package ca.rmen.android.poetassistant.wotd;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.List;

import ca.rmen.android.poetassistant.Constants;

/**
 * Word of the day task for API levels Lollipop and later.
 * This uses JobScheduler.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
final class WotdJob {
    private static final String TAG = Constants.TAG + WotdJob.class.getSimpleName();

    private WotdJob() {
        // prevent instantiation
    }

    static void reschedule(Context context) {
        Log.d(TAG, "reschedule() called with: " + "context = [" + context + "]");
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> jobs = jobScheduler.getAllPendingJobs();
        Log.v(TAG, "Pending jobs: " + jobs);
        if (jobs == null || jobs.isEmpty()) schedule(context);
    }

    static void schedule(Context context) {
        Log.d(TAG, "schedule() called with: " + "context = [" + context + "]");
        JobInfo jobInfo = new JobInfo.Builder(TAG.hashCode(), new ComponentName(context, WotdJobService.class))
                .setBackoffCriteria(30, JobInfo.BACKOFF_POLICY_EXPONENTIAL)
                .setRequiresDeviceIdle(false)
                .setPeriodic(Wotd.NOTIFICATION_FREQUENCY_MS)
                .setPersisted(true)
                .setRequiresCharging(false)
                .build();
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
    }

    static void cancel(Context context) {
        Log.d(TAG, "cancel() called with: " + "context = [" + context + "]");
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(TAG.hashCode());
    }
}
