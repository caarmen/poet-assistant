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

package ca.rmen.android.poetassistant.main.dictionaries.rt

import android.app.Activity
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.databinding.ListItemHeadingBinding
import ca.rmen.android.poetassistant.databinding.ListItemSubheadingBinding
import ca.rmen.android.poetassistant.databinding.ListItemWordBinding
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.TextPopupMenu
import ca.rmen.android.poetassistant.main.dictionaries.ResultListAdapter

class RTListAdapter(activity: Activity) : ResultListAdapter<RTEntryViewModel>() {
    private val mWordClickedListener: OnWordClickListener = activity as OnWordClickListener
    private val mEntryIconClickListener = EntryIconClickListener()

    override fun getItemViewType(position: Int): Int {
        val entry = getItem(position)
        return entry.type.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultListEntryViewHolder {
        val layoutId = when (viewType) {
            RTEntryViewModel.Type.HEADING.ordinal -> R.layout.list_item_heading
            RTEntryViewModel.Type.SUBHEADING.ordinal -> R.layout.list_item_subheading
            else -> R.layout.list_item_word
        }
        val binding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context),
                layoutId,
                parent,
                false)
        return ResultListAdapter.ResultListEntryViewHolder(parent, binding)
    }

    override fun onBindViewHolder(holder: ResultListEntryViewHolder, position: Int) {
        val viewModel = getItem(position)
        when (viewModel.type) {
            RTEntryViewModel.Type.HEADING -> (holder.binding as ListItemHeadingBinding).viewModel = viewModel
            RTEntryViewModel.Type.SUBHEADING -> (holder.binding as ListItemSubheadingBinding).viewModel = viewModel
            else -> {
                val wordBinding = holder.binding as ListItemWordBinding
                wordBinding.viewModel = viewModel
                wordBinding.entryIconClickListener = mEntryIconClickListener
                TextPopupMenu.addPopupMenu(
                        if (viewModel.showButtons) TextPopupMenu.Style.SYSTEM else TextPopupMenu.Style.FULL,
                        holder.parentView,
                        wordBinding.text1,
                        mWordClickedListener
                )
            }
        }
        holder.binding.executePendingBindings()
    }

    inner class EntryIconClickListener {
        private fun getWord(v: View): String {
            val binding = DataBindingUtil.getBinding<ListItemWordBinding>(v.parent as View)
            return binding.text1.text.toString()
        }

        fun onRhymerIconClicked(v: View) {
            mWordClickedListener.onWordClick(getWord(v), Tab.RHYMER)
        }

        fun onThesaurusIconClicked(v: View) {
            mWordClickedListener.onWordClick(getWord(v), Tab.THESAURUS)
        }

        fun onDictionaryIconClicked(v: View) {
            mWordClickedListener.onWordClick(getWord(v), Tab.DICTIONARY)
        }
    }
}
