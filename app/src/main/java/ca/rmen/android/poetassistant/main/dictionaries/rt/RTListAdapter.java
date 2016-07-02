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
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.databinding.ListItemHeadingBinding;
import ca.rmen.android.poetassistant.databinding.ListItemSubheadingBinding;
import ca.rmen.android.poetassistant.databinding.ListItemWordBinding;
import ca.rmen.android.poetassistant.main.Tab;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListAdapter;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListEntryViewHolder;


public class RTListAdapter extends ResultListAdapter<RTEntry> {

    private final OnWordClickedListener mListener;
    private final EntryIconClickListener mEntryIconClickListener;

    public RTListAdapter(Activity activity) {
        mListener = (OnWordClickedListener) activity;
        mEntryIconClickListener = new EntryIconClickListener();
    }

    @Override
    public int getItemViewType(int position) {
        RTEntry entry = getItem(position);
        return entry.type.ordinal();
    }

    @Override
    public ResultListEntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId;
        if (viewType == RTEntry.Type.HEADING.ordinal())
            layoutId = R.layout.list_item_heading;
        else if (viewType == RTEntry.Type.SUBHEADING.ordinal())
            layoutId = R.layout.list_item_subheading;
        else
            layoutId = R.layout.list_item_word;

        ViewDataBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                layoutId,
                parent,
                false);
        return new ResultListEntryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ResultListEntryViewHolder holder, int position) {
        RTEntry entry = getItem(position);
        if (entry.type == RTEntry.Type.HEADING) {
            bindHeadingView(entry, (ListItemHeadingBinding) holder.binding);
        } else if (entry.type == RTEntry.Type.SUBHEADING) {
            bindSubheadingView(entry, (ListItemSubheadingBinding) holder.binding);
        } else {
            bindWordView(entry, (ListItemWordBinding) holder.binding);
        }
    }

    private void bindHeadingView(RTEntry entry, ListItemHeadingBinding binding) {
        binding.text1.setText(entry.text);
    }


    private void bindSubheadingView(RTEntry entry, ListItemSubheadingBinding binding) {
        binding.text1.setText(entry.text);
    }

    private void bindWordView(RTEntry entry, ListItemWordBinding binding) {
        binding.text1.setText(entry.text);
        binding.setEntryIconClickListener(mEntryIconClickListener);
    }

    public class EntryIconClickListener {

        private String getWord(View v) {
            ListItemWordBinding binding = DataBindingUtil.getBinding((View)v.getParent());
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
