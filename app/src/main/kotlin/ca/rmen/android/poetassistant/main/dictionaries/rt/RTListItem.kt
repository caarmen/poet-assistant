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

import androidx.databinding.ObservableBoolean
import ca.rmen.android.poetassistant.databinding.BindingCallbackAdapter

class RTListItem(
    val type: Type, val text: String,
    isFavoriteInitialValue: Boolean, val hasDefinition: Boolean, val showButtons: Boolean,
    onFavoriteToggle: (String, Boolean) -> Unit,
    ) {
    enum class Type {
        HEADING,
        SUBHEADING,
        WORD
    }

    val isFavorite = ObservableBoolean()

    constructor(type: Type, text: String, onFavoriteToggle: (String, Boolean) -> Unit) :
            this(type, text,  false, false, onFavoriteToggle)

    constructor(type: Type, text: String, isFavoriteInitialValue: Boolean, showButtons: Boolean, onFavoriteToggle: (String, Boolean) -> Unit) :
            this(type, text, isFavoriteInitialValue, true, showButtons, onFavoriteToggle)

    init {
        isFavorite.set(isFavoriteInitialValue)
        isFavorite.addOnPropertyChangedCallback(BindingCallbackAdapter(object : BindingCallbackAdapter.Callback {
            override fun onChanged() {
                onFavoriteToggle(text, isFavorite.get())
            }
        }))
    }

    override fun toString(): String {
        return "RTEntryViewModel(text='$text')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RTListItem

        if (type != other.type) return false
        if (text != other.text) return false
        if (hasDefinition != other.hasDefinition) return false
        if (showButtons != other.showButtons) return false
        if (isFavorite != other.isFavorite) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + hasDefinition.hashCode()
        result = 31 * result + showButtons.hashCode()
        result = 31 * result + isFavorite.hashCode()
        return result
    }


}
