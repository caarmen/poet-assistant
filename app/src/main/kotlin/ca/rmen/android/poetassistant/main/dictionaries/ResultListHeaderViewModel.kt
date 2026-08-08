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
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import ca.rmen.android.poetassistant.FavoritesRepository
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.Tts
import ca.rmen.android.poetassistant.TtsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultListHeaderViewModel @Inject constructor(application: Application, val mFavoritesRepository: FavoritesRepository, val mTts: Tts) : AndroidViewModel(application) {
    val query : StateFlow<String?>
    field = MutableStateFlow<String?>(null)

    val isFavorite: StateFlow<Boolean>
    field = MutableStateFlow(false)

    val isMatchedWordSelectable = ObservableField(false)
    val filter = ObservableField<String>()
    val showHeader = ObservableBoolean()

    val snackbarText: StateFlow<String>
    field = MutableStateFlow("")
    // Expose a Flow to the fragment, so it can update the star icon when the favorite
    // value changes in the DB. This is relevant when the favorite value changes because the star
    // was clicked in *another* fragment. If we only had one screen where the user could change
    // the favorites, a simple databinding between the star checkbox and this ViewModel would
    // suffice to sync the db and the UI.
    @OptIn(ExperimentalCoroutinesApi::class)
    val isFavoriteFlow: StateFlow<Boolean> = query.flatMapLatest { queryWord ->
        mFavoritesRepository.getIsFavoriteFlow(queryWord ?: "")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false,
    )

    val ttsStateLiveData: LiveData<TtsState>

    init {
        ttsStateLiveData = mTts.getTtsLiveData()
    }

    fun setQuery(query: String?) {
        this.query.value = query
    }

    fun setIsFavorite(isFavorite: Boolean) {
        // When the user taps on the star icon, update the favorite in the DB
        query.value?.let { word ->
            viewModelScope.launch {
                mFavoritesRepository.saveFavorite(word, isFavorite)
            }
        }
    }

    fun speak() = query.value?.let { mTts.speak(it) }

    fun clearFilter() = filter.set(null)

    fun webSearch() = query.value?.let { WebSearch.search(getApplication(), it) }

    fun clearFavorites() {
        viewModelScope.launch {
            mFavoritesRepository.clear()
        }
        snackbarText.value = getApplication<Application>().getString(R.string.favorites_cleared)
    }
}

