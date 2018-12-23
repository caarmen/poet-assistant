/*
 * Copyright (c) 2016-2018 Carmen Alvarez
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

package ca.rmen.android.poetassistant.wotd

import android.app.Activity
import androidx.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.databinding.ListItemWotdBinding
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.TextPopupMenu
import ca.rmen.android.poetassistant.main.dictionaries.ResultListAdapter
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnWordClickListener

open class WotdAdapter(activity: Activity) : ResultListAdapter<WotdEntryViewModel>() {
    private val mWordClickedListener: OnWordClickListener = activity as OnWordClickListener
    private val mEntryIconClickListener = EntryIconClickListener()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultListEntryViewHolder {
        return ResultListEntryViewHolder(parent, DataBindingUtil.inflate<ListItemWotdBinding>(
                LayoutInflater.from(parent.context),
                R.layout.list_item_wotd,
                parent,
                false))
    }

    override fun onBindViewHolder(holder: ResultListEntryViewHolder, position: Int) {
        val viewModel = getItem(position)
        val binding = holder.binding as ListItemWotdBinding
        binding.viewModel = viewModel
        binding.entryIconClickListener = mEntryIconClickListener
        TextPopupMenu.addPopupMenu(
                if (viewModel.showButtons) TextPopupMenu.Style.SYSTEM else TextPopupMenu.Style.FULL,
                holder.parentView,
                binding.text1,
                mWordClickedListener)
        binding.executePendingBindings()
    }

    inner class EntryIconClickListener {
        private fun getWord(v: View): String {
            val binding = DataBindingUtil.getBinding<ListItemWotdBinding>(v.parent as View)
            return binding?.text1?.text.toString()
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
