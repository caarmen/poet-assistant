/*
 * Copyright (c) 2018 Carmen Alvarez
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

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import ca.rmen.android.poetassistant.Favorites
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.Tts
import ca.rmen.android.poetassistant.TtsState
import ca.rmen.android.poetassistant.databinding.BindingCallbackAdapter
import ca.rmen.android.poetassistant.databinding.LiveDataMapping
import ca.rmen.android.poetassistant.di.NonAndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors

class ResultListHeaderViewModel(application: Application) : AndroidViewModel(application) {
    val query = ObservableField<String>()
    val isMatchedWordSelectable = ObservableField(false)
    val filter = ObservableField<String>()
    val isFavorite = ObservableBoolean()
    val showHeader = ObservableBoolean()

    val snackbarText = MutableLiveData<String>()
    val isFavoriteLiveData: LiveData<Boolean>
    val ttsStateLiveData: LiveData<TtsState>

    private val mFavorites: Favorites
    private val mTts: Tts

    init {
        val entryPoint = EntryPointAccessors.fromApplication(application, NonAndroidEntryPoint::class.java)
        mFavorites = entryPoint.favorites()
        mTts = entryPoint.tts()
        ttsStateLiveData = mTts.getTtsLiveData()
        // Expose a LiveData to the fragment, so it can update the star icon when the favorite
        // value changes in the DB. This is relevant when the favorite value changes because the star
        // was clicked in *another* fragment. If we only had one screen where the user could change
        // the favorites, a simple databinding between the star checkbox and this ViewModel would
        // suffice to sync the db and the UI.
        isFavoriteLiveData =
            LiveDataMapping.fromObservableField(query).switchMap { query -> mFavorites.getIsFavoriteLiveData(query) }
        // When the user taps on the star icon, update the favorite in the DB
        isFavorite.addOnPropertyChangedCallback(BindingCallbackAdapter(object : BindingCallbackAdapter.Callback {
            override fun onChanged() {
                query.get()?.let {
                    mFavorites.saveFavorite(it, isFavorite.get())
                }
            }
        }))
    }

    fun speak() = query.get()?.let { mTts.speak(it) }

    fun clearFilter() = filter.set(null)

    fun webSearch() = query.get()?.let { WebSearch.search(getApplication(), it) }

    fun clearFavorites() {
        mFavorites.clear()
        snackbarText.value = getApplication<Application>().getString(R.string.favorites_cleared)
    }
}

