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

package ca.rmen.android.poetassistant.main.dictionaries.rt;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.databinding.ListItemWordBinding;
import ca.rmen.android.poetassistant.main.Tab;


public class RTListAdapter extends ArrayAdapter<RTEntry> {

    private final Context mContext;
    private final OnWordClickedListener mListener;
    private final EntryIconClickListener mEntryIconClickListener;

    public RTListAdapter(Activity activity) {
        super(activity, 0);
        mContext = activity;
        mListener = (OnWordClickedListener) activity;
        mEntryIconClickListener = new EntryIconClickListener();
    }

    @Override
    public int getItemViewType(int position) {
        RTEntry entry = getItem(position);
        return entry.type.ordinal();
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RTEntry entry = getItem(position);
        if (entry.type == RTEntry.Type.HEADING)
            return getHeadingView(entry, convertView);
        else if (entry.type == RTEntry.Type.SUBHEADING)
            return getSubHeadingView(entry, convertView);
        else
            return getWordView(entry, convertView, parent);
    }

    private View getHeadingView(RTEntry entry, View convertView) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.list_item_heading, null);
        }
        TextView text = (TextView) convertView;
        text.setText(entry.text);
        return convertView;
    }


    private View getSubHeadingView(RTEntry entry, View convertView) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.list_item_subheading, null);
        }
        TextView text = (TextView) convertView;
        text.setText(entry.text);
        return convertView;
    }

    private View getWordView(RTEntry entry, View convertView, ViewGroup parent) {
        final ListItemWordBinding binding;
        if (convertView == null) {
            binding = DataBindingUtil.inflate(
                    LayoutInflater.from(getContext()),
                    R.layout.list_item_word,
                    parent,
                    false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (ListItemWordBinding) convertView.getTag();
        }
        binding.text1.setText(entry.text);
        binding.setEntryIconClickListener(mEntryIconClickListener);
        return convertView;
    }

    public class EntryIconClickListener {

        private String getWord(View v) {
            View parentView = (View) v.getParent();
            ListItemWordBinding binding = (ListItemWordBinding) parentView.getTag();
            return binding.text1.getText().toString();
        }

        public void onRhymerIconClicked(View v) {
            mListener.onWordClicked(getWord(v), Tab.RHYMER);
        }

        public void onThesaurusIconClicked(View v) {
            mListener.onWordClicked(getWord(v), Tab.THESAURUS);
        }

        public void onDictionaryIconClicked(View v) {
            mListener.onWordClicked(getWord(v), Tab.DICTIONARY);
        }
    }
}
