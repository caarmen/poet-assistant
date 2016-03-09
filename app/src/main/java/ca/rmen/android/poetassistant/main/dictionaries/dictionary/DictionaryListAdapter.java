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
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.ViewHolder;


public class DictionaryListAdapter extends ArrayAdapter<DictionaryEntryDetails> {
    private static final String TAG = Constants.TAG + DictionaryListAdapter.class.getSimpleName();

    private final Context mContext;

    public DictionaryListAdapter(Activity activity) {
        super(activity, 0);
        mContext = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DictionaryEntryDetails entry = getItem(position);
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.list_item_dictionary_entry, null);
        }
        TextView wordType = ViewHolder.get(convertView, R.id.word_type);
        wordType.setText(entry.partOfSpeech);

        TextView definition = ViewHolder.get(convertView, R.id.definition);
        definition.setText(entry.definition);
        return convertView;
    }

}
