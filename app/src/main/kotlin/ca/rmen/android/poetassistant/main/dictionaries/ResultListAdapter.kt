/*
 * Copyright (c) 2016 - 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class ResultListAdapter<T>(itemCallback: DiffUtilItemCallback<T>) :
        ListAdapter<T, ResultListAdapter.ResultListEntryViewHolder>(itemCallback) {

    fun getAll(): List<T> {
        val result = mutableListOf<T>()
        for (i in 0 until itemCount) {
            result.add(getItem(i))
        }
        return result
    }

    class ResultListEntryViewHolder(val parentView: View, val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

    abstract class DiffUtilItemCallback<U> : DiffUtil.ItemCallback<U>() {
        override fun areItemsTheSame(
                oldItem: U, newItem: U): Boolean {
            // Normally this would check for ids, but we don't have ids in these lists.
            return oldItem == newItem
        }
    }
}