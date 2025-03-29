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
import android.app.SearchManager
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SuggestionsViewModel @Inject constructor(
    private val application: Application,
    private val mSuggestions: Suggestions,
    private val dictionary: Dictionary
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
        viewModelScope.launch(Dispatchers.IO) {

            val foundSuggestions = mutableListOf<SearchSuggestion>()
            SuggestionsCursor(
                application,
                suggestions = mSuggestions,
                dictionary = dictionary,
                filter = typedText
            ).use { cursor ->
                val wordColumn = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)
                val iconColumn = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_ICON_1)
                Log.d(TAG, "${cursor.count} results for $typedText")
                while (cursor.moveToNext()) {
                    foundSuggestions.add(
                        SearchSuggestion(
                            cursor.getString(wordColumn),
                            cursor.getInt(iconColumn),
                        )
                    )
                }
            }
            _suggestions.value = foundSuggestions
        }
    }
}