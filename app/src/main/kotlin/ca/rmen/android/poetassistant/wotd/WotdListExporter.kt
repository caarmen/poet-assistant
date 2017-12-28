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

import android.content.Context
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.dictionaries.ResultListExporter

class WotdListExporter(private val context: Context) : ResultListExporter<List<WotdEntryViewModel>> {
    override fun export(word: String, filter: String?, entries: List<WotdEntryViewModel>): String {
        val title = context.getString(R.string.share_wotd_title)
        val builder = StringBuilder(title)
        entries.forEach {
            builder.append(context.getString(R.string.share_wotd_entry, it.date, it.text))
        }
        return builder.toString()
    }
}
