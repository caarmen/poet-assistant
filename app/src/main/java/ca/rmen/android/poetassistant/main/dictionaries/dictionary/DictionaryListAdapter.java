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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Arrays;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.ViewHolder;


class DictionaryListAdapter extends BaseAdapter {
    private static final String TAG = Constants.TAG + DictionaryListAdapter.class.getSimpleName();

    private final Context mContext;
    private Dictionary.DictionaryEntry[] mData = new Dictionary.DictionaryEntry[0];

    public DictionaryListAdapter(Activity activity) {
        super();
        mContext = activity;
    }

    public void setData(Dictionary.DictionaryEntry[] data) {
        Log.d(TAG, "setData() called with: " + "data = [" + Arrays.toString(data) + "]");
        if (data != null) mData = data;
        else mData = new Dictionary.DictionaryEntry[0];
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.length;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public Object getItem(int position) {
        return mData[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Dictionary.DictionaryEntry entry = mData[position];
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.list_item_dictionary_entry, null);
        }
        TextView wordType = ViewHolder.get(convertView, R.id.word_type);
        wordType.setText(entry.wordType);

        TextView definition = ViewHolder.get(convertView, R.id.definition);
        definition.setText(entry.definition);
        return convertView;
    }

}
