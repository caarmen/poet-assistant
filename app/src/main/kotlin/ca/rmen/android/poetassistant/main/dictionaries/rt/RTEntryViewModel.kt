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

import android.content.Context
import androidx.databinding.ObservableBoolean
import ca.rmen.android.poetassistant.dagger.DaggerHelper
import ca.rmen.android.poetassistant.databinding.BindingCallbackAdapter

class RTEntryViewModel(context: Context, val type: Type, val text: String,
                       isFavoriteInitialValue: Boolean, val hasDefinition: Boolean, val showButtons: Boolean) {
    enum class Type {
        HEADING,
        SUBHEADING,
        WORD
    }

    val isFavorite = ObservableBoolean()

    constructor(context: Context, type: Type, text: String) :
            this(context, type, text,  false, false)

    constructor(context: Context, type: Type, text: String, isFavoriteInitialValue: Boolean, showButtons: Boolean) :
            this(context, type, text, isFavoriteInitialValue, true, showButtons)

    init {
        val favorites = DaggerHelper.getMainScreenComponent(context).getFavorites()
        isFavorite.set(isFavoriteInitialValue)
        isFavorite.addOnPropertyChangedCallback(BindingCallbackAdapter(object : BindingCallbackAdapter.Callback {
            override fun onChanged() {
                favorites.saveFavorite(text, isFavorite.get())
            }
        }))
    }

    override fun toString(): String {
        return "RTEntryViewModel(text='$text')"
    }

}
