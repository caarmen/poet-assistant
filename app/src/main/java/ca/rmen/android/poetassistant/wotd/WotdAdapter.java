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

package ca.rmen.android.poetassistant.wotd;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.databinding.ListItemWotdBinding;
import ca.rmen.android.poetassistant.main.Tab;
import ca.rmen.android.poetassistant.main.TextPopupMenu;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListAdapter;
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnWordClickListener;

public class WotdAdapter extends ResultListAdapter<WotdEntryViewModel> {

    private final OnWordClickListener mWordClickedListener;
    private final EntryIconClickListener mEntryIconClickListener;

    public WotdAdapter(Activity activity) {
        mWordClickedListener = (OnWordClickListener) activity;
        mEntryIconClickListener = new EntryIconClickListener();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public ResultListEntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ListItemWotdBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.list_item_wotd,
                parent,
                false);
        return new ResultListEntryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ResultListEntryViewHolder holder, int position) {
        WotdEntryViewModel viewModel = getItem(position);
        ListItemWotdBinding binding = (ListItemWotdBinding) holder.binding;
        binding.setViewModel(viewModel);
        binding.setEntryIconClickListener(mEntryIconClickListener);
        TextPopupMenu.addPopupMenu(
                viewModel.showButtons ? TextPopupMenu.Style.SYSTEM : TextPopupMenu.Style.FULL,
                binding.text1,
                mWordClickedListener);
        binding.executePendingBindings();
    }

    public class EntryIconClickListener {

        private String getWord(View v) {
            ListItemWotdBinding binding = DataBindingUtil.getBinding((View) v.getParent());
            return binding.text1.getText().toString();
        }

        public void onRhymerIconClicked(View v) {
            mWordClickedListener.onWordClick(getWord(v), Tab.RHYMER);
        }

        public void onThesaurusIconClicked(View v) {
            mWordClickedListener.onWordClick(getWord(v), Tab.THESAURUS);
        }

        public void onDictionaryIconClicked(View v) {
            mWordClickedListener.onWordClick(getWord(v), Tab.DICTIONARY);
        }
    }

}
