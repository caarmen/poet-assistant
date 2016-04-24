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

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.databinding.ListItemDictionaryEntryBinding;


public class DictionaryListAdapter extends ArrayAdapter<DictionaryEntry.DictionaryEntryDetails> {
    public DictionaryListAdapter(Activity activity) {
        super(activity, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DictionaryEntry.DictionaryEntryDetails entry = getItem(position);
        final ListItemDictionaryEntryBinding binding;
        if (convertView == null) {
            binding = DataBindingUtil.inflate(
                    LayoutInflater.from(getContext()),
                    R.layout.list_item_dictionary_entry,
                    parent,
                    false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (ListItemDictionaryEntryBinding) convertView.getTag();
        }
        binding.wordType.setText(entry.partOfSpeech);
        binding.definition.setText(entry.definition);
        return convertView;
    }
}
