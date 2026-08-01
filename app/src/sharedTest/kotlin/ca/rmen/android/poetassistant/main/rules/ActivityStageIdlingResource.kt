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

package ca.rmen.android.poetassistant.main.rules

import android.util.Log
import androidx.test.espresso.IdlingResource
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import ca.rmen.android.poetassistant.Constants
import java.util.EnumSet

class ActivityStageIdlingResource(
    private val activityClassName: String,
    private val matchingStages: Set<Stage>
) : IdlingResource {

    constructor(activityClassName: String, stage: Stage) : this(activityClassName, EnumSet.of(stage))

    companion object {
        private val TAG = Constants.TAG + ActivityStageIdlingResource::class.simpleName

        /**
         * Becomes idle when the given activity enters the given stage.
         */

        /**
         * @return true if the given activity is in one of the given stages.
         */
        fun isActivityInStages(activityClassName: String, stages: Set<Stage>): Boolean {
            for (stage in stages) {
                val activitiesInStage = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage)
                for (matchedActivity in activitiesInStage) {
                    if (activityClassName == matchedActivity::class.java.name) {
                        return true
                    }
                }
            }
            return false
        }
    }

    private var callback: IdlingResource.ResourceCallback? = null

    override fun getName(): String {
        return TAG
    }

    override fun isIdleNow(): Boolean {
        val isInStages = isActivityInStages(activityClassName, matchingStages)
        Log.v(TAG, "$activityClassName in $matchingStages? $isInStages")
        if (isInStages && callback != null) callback?.onTransitionToIdle()
        return isInStages
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        this.callback = callback
    }
}
