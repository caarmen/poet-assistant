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

package ca.rmen.android.poetassistant.main.dictionaries.dictionary

import androidx.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.databinding.ListItemDictionaryEntryBinding
import ca.rmen.android.poetassistant.main.TextPopupMenu
import ca.rmen.android.poetassistant.main.dictionaries.ResultListAdapter
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnWordClickListener

open class DictionaryListAdapter(private val listener: OnWordClickListener) : ResultListAdapter<DictionaryEntry.DictionaryEntryDetails>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultListEntryViewHolder {
        val binding = DataBindingUtil.inflate<ListItemDictionaryEntryBinding>(
                LayoutInflater.from(parent.context),
                R.layout.list_item_dictionary_entry,
                parent,
                false)
        return ResultListAdapter.ResultListEntryViewHolder(parent, binding)
    }

    override fun onBindViewHolder(holder: ResultListEntryViewHolder, position: Int) {
        val entry = getItem(position)
        val binding = holder.binding as ListItemDictionaryEntryBinding
        TextPopupMenu.addSelectionPopupMenu(holder.parentView, binding.definition, listener)
        binding.entry = entry
        binding.executePendingBindings()
    }
}
