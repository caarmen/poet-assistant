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

package ca.rmen.android.poetassistant.main.dictionaries.dictionary;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.databinding.ListItemDictionaryEntryBinding;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListAdapter;


public class DictionaryListAdapter extends ResultListAdapter<DictionaryEntry.DictionaryEntryDetails> {

    @Override
    public ResultListAdapter.ResultListEntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ListItemDictionaryEntryBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.list_item_dictionary_entry,
                parent,
                false);
        return new ResultListAdapter.ResultListEntryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ResultListAdapter.ResultListEntryViewHolder holder, int position) {
        DictionaryEntry.DictionaryEntryDetails entry = getItem(position);
        ListItemDictionaryEntryBinding binding = (ListItemDictionaryEntryBinding) holder.binding;
        binding.setEntry(entry);
        binding.executePendingBindings();
    }
}
