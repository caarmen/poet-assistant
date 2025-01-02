/*
 * Copyright (c) 2025 - current Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ca.rmen.android.poetassistant.R

class SuggestionsAdapter : RecyclerView.Adapter<SuggestionsAdapter.ViewHolder>() {
    val suggestions = mutableListOf<SuggestionsViewModel.SearchSuggestion>()

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(android.R.id.icon1)
        val text: TextView = view.findViewById(android.R.id.text1)
    }

    var listener: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.suggested_word, parent, false)
        val vh = ViewHolder(view)
        view.setOnClickListener {
            listener?.invoke(vh.text.text.toString())
        }
        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text.text = suggestions[position].word
        holder.icon.setImageResource(suggestions[position].iconResource)
    }

    override fun getItemCount() = suggestions.size

}