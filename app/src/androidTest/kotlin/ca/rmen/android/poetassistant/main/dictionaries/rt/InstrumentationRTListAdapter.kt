/*
 * Copyright (c) 2018 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries.rt

import android.app.Activity
import android.support.test.espresso.IdlingRegistry
import android.support.test.espresso.idling.CountingIdlingResource
import ca.rmen.android.poetassistant.ResultListIdlingHelper
import ca.rmen.android.poetassistant.main.Tab

class InstrumentationRTListAdapter(tab: Tab, activity: Activity)
    : RTListAdapter(tab, activity) {

    private val countingIdlingResource: CountingIdlingResource = CountingIdlingResource("${tab}ResultIdlingResource", true)
    private var data: MutableList<RTEntryViewModel>? = null

    init {
        IdlingRegistry.getInstance().register(countingIdlingResource)
    }

    override fun submitList(list: MutableList<RTEntryViewModel>?) {
        ResultListIdlingHelper.setupIdlingResource(this, data, list, countingIdlingResource)
        data = list
        super.submitList(list)
    }
}