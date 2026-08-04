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

abstract class ResultListAdapter<T: Any>(itemCallback: DiffUtilItemCallback<T>) :
        ListAdapter<T, ResultListAdapter.ResultListEntryViewHolder>(itemCallback) {

    fun getAll(): List<T> {
        val result = mutableListOf<T>()
        for (i in 0 until itemCount) {
            result.add(getItem(i))
        }
        return result
    }

    var onFavoriteWordToggle: ((String, Boolean) -> Unit)? = null

    class ResultListEntryViewHolder(val parentView: View, val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

    abstract class DiffUtilItemCallback<U: Any> : DiffUtil.ItemCallback<U>() {
        override fun areItemsTheSame(
                oldItem: U, newItem: U): Boolean {
            // TODO this needs to be improved!
            // But if I use oldItem == newItem (or even just comparing the text of two items as
            // an "id"), this breaks since the item classes became data classes. Before the
            // whole list scrolled to the top at each search, and it's no longer the case.
            // Returning false for now just to keep ISO behavior with before. Maybe this will
            // all be moot if we get far enough in the modernization to migrate to compose.
            if (oldItem === newItem) return true
            if (oldItem.javaClass != newItem.javaClass) return false
            return false
        }
    }
}