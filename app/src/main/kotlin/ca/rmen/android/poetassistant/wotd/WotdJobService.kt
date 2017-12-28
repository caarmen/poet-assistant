/*
 * Copyright (c) 2016-2017 Carmen Alvarez
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
import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.dagger.DaggerHelper

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class WotdJobService : JobService() {

    companion object {
        private val TAG = Constants.TAG + WotdJobService::class.java.simpleName
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.v(TAG, "onStartJob: params=$params")
        val dictionary = DaggerHelper.getWotdComponent(application).getDictionary()
        val threading = DaggerHelper.getWotdComponent(application).getThreading()
        threading.execute({
            Wotd.notifyWotd(applicationContext, dictionary)
            jobFinished(params, false)
        })
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }
}
