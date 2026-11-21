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

package ca.rmen.android.poetassistant.main.dictionaries.search.suggestions

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.dictionaries.search.suggestions.data.Source
import ca.rmen.android.poetassistant.main.dictionaries.search.suggestions.data.SuggestionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SuggestionsViewModel @Inject constructor(
    private val suggestionsRepository: SuggestionsRepository,
) : ViewModel() {

    data class SearchSuggestion(
        val word: String,
        @DrawableRes val iconResource: Int
    )

    private val typedText = MutableStateFlow("")
    fun setTypedText(typedText: String) {
        this.typedText.value = typedText
    }

    @OptIn(FlowPreview::class)
    val suggestions: StateFlow<List<SearchSuggestion>> = typedText
        .debounce(500.milliseconds).map {
            suggestionsRepository.getSuggestions(it).map { entry ->
                SearchSuggestion(
                    word = entry.word,
                    iconResource = when (entry.source) {
                        Source.HISTORY -> R.drawable.ic_search_history
                        Source.DICTIONARY -> R.drawable.ic_action_search
                    }
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList(),
        )
}
