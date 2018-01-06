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
import android.support.v7.widget.RecyclerView
import android.view.View

abstract class ResultListAdapter<T> : RecyclerView.Adapter<ResultListAdapter.ResultListEntryViewHolder>() {

    private val mData = ArrayList<T>()

    fun clear() {
        mData.clear()
        notifyDataSetChanged()
    }

    fun addAll(data: List<T>?) {
        if (data != null) mData.addAll(data)
        notifyDataSetChanged()
    }

    fun getAll() = mData

    protected fun getItem(position: Int): T = mData[position]

    override fun getItemCount(): Int = mData.size

    class ResultListEntryViewHolder(val parentView: View, val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)
}