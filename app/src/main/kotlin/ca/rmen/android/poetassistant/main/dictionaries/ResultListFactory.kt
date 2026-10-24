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

package ca.rmen.android.poetassistant.main.dictionaries

import android.content.Context
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.Tab

object ResultListFactory {

    fun getTabName(context: Context, tab: Tab): String {
        return when (tab) {
            Tab.PATTERN -> context.getString(R.string.tab_pattern)
            Tab.FAVORITES -> context.getString(R.string.tab_favorites)
            Tab.WOTD -> context.getString(R.string.tab_wotd)
            Tab.RHYMER -> context.getString(R.string.tab_rhymer)
            Tab.THESAURUS -> context.getString(R.string.tab_thesaurus)
            Tab.DICTIONARY -> context.getString(R.string.tab_dictionary)
            else -> context.getString(R.string.tab_reader)
        }
    }
}
