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

package ca.rmen.android.poetassistant.main.dictionaries

import android.app.Activity
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryListAdapter
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnWordClickListener
import ca.rmen.android.poetassistant.main.dictionaries.rt.RTListAdapter
import ca.rmen.android.poetassistant.wotd.WotdAdapter

class ResultListAdapterFactoryImpl : ResultListAdapterFactory {

    override fun createAdapter(activity: Activity, tab: Tab): ResultListAdapter<out Any> {
        return when (tab) {
            Tab.PATTERN, Tab.FAVORITES, Tab.RHYMER, Tab.THESAURUS -> RTListAdapter(tab, activity)
            Tab.WOTD -> WotdAdapter(activity)
            else -> DictionaryListAdapter(activity as OnWordClickListener)
        }
    }
}
