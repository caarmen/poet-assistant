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

package ca.rmen.android.poetassistant.main.dictionaries.rt

import android.content.Context
import android.text.TextUtils
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.dictionaries.ResultListExporter

class ThesaurusListExporter(private val context: Context) : ResultListExporter<List<RTEntryViewModel>> {
    override fun export(word: String, filter: String?, entries: List<RTEntryViewModel>): String {
        val title = if (TextUtils.isEmpty(filter)) context.getString(R.string.share_thesaurus_title, word)
        else context.getString(R.string.share_thesaurus_title_with_filter, word, filter)
        val builder = StringBuilder(title)
        entries.forEach { entry ->
            val entryResId = when (entry.type) {
                RTEntryViewModel.Type.HEADING -> R.string.share_rt_heading
                RTEntryViewModel.Type.SUBHEADING -> R.string.share_rt_subheading
                else -> R.string.share_rt_entry
            }
            builder.append(context.getString(entryResId, entry.text))
        }
        return builder.toString()
    }
}
