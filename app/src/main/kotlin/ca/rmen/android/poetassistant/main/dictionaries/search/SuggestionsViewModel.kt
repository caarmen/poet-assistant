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

import android.app.Application
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.di.IODispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SuggestionsViewModel @Inject constructor(
    application: Application,
    private val mSuggestions: Suggestions,
    @IODispatcher val ioDispatcher: CoroutineDispatcher,
) : AndroidViewModel(application) {

    data class SearchSuggestion(
        val word: String,
        @DrawableRes val iconResource: Int
    )

    companion object {
        private val TAG = Constants.TAG + SuggestionsViewModel::class.java.simpleName
    }

    private val _suggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
    val suggestions: Flow<List<SearchSuggestion>> = _suggestions

    fun fetchSuggestions(typedText: String) {
        viewModelScope.launch(ioDispatcher) {
            _suggestions.value = mSuggestions.getSuggestions(filter = typedText).map { entry ->
                SearchSuggestion(
                    word = entry.word,
                    iconResource = when (entry.source) {
                        Source.HISTORY -> R.drawable.ic_search_history
                        Source.DICTIONARY -> R.drawable.ic_action_search
                    }
                )
            }
            Log.d(TAG, "${_suggestions.value.size} results for $typedText")
        }
    }
}