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

import android.databinding.ViewDataBinding
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.View

abstract class ResultListAdapter<T> : ListAdapter<T, ResultListAdapter.ResultListEntryViewHolder>(DiffUtilItemCallback<T>()) {

    fun getAll(): List<T> {
        val result = mutableListOf<T>()
        for (i in 0 until itemCount) {
            result.add(getItem(i))
        }
        return result
    }

    class ResultListEntryViewHolder(val parentView: View, val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

    class DiffUtilItemCallback<U> : DiffUtil.ItemCallback<U>() {
        override fun areItemsTheSame(
                oldItem: U, newItem: U): Boolean {
            // Normally this would check for ids, but we don't have ids in these lists.
            return oldItem == newItem
        }

        override fun areContentsTheSame(
                oldItem: U, newItem: U): Boolean {
            return oldItem == newItem
        }
    }
}