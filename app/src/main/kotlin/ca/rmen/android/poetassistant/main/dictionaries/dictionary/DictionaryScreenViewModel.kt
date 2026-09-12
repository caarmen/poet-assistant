/*
 * Copyright (c) 2016 - present Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.common.models.Share
import ca.rmen.android.poetassistant.main.common.usecases.SpeakTextUseCase
import ca.rmen.android.poetassistant.main.favorites.data.FavoritesRepository
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.usecases.CreateDictionaryShareUseCase
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.usecases.LookupDictionaryEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for the Dictionary screen.
 * Manages UI state and handles user actions.
 *
 * @param lookupDictionaryEntryUseCase Use case for looking up dictionary entries.
 * @param createDictionaryShareUseCase Use case for creating shareable content.
 * @param speakTextUseCase Use case for triggering text-to-speech.
 * @param favoritesRepository Repository for managing favorites.
 */
@HiltViewModel
class DictionaryScreenViewModel @Inject constructor(
    private val lookupDictionaryEntryUseCase: LookupDictionaryEntryUseCase,
    private val createDictionaryShareUseCase: CreateDictionaryShareUseCase,
    private val speakTextUseCase: SpeakTextUseCase,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<DictionaryScreenState> = query.flatMapLatest { query ->
        if (query.isBlank()) {
            flowOf(DictionaryScreenState.Idle)
        } else {
            try {
                val entry = lookupDictionaryEntryUseCase(query)
                favoritesRepository.getIsFavoriteFlow(entry.word).map { isFav ->
                    DictionaryScreenState.Success(
                        entry = entry,
                        isFavorite = isFav,
                    )
                }
            } catch (_: WordNotFoundException) {
                flowOf(DictionaryScreenState.NotFound(query))
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DictionaryScreenState.Idle
    )

    val share: StateFlow<Share?>
        field = MutableStateFlow<Share?>(null)

    /**
     * Searches for a word in the dictionary.
     *
     * @param query The word to search for.
     */
    fun onWordSearched(query: String) {
        this.query.value = query
    }

    /**
     * Sets the favorite status of a word.
     *
     * @param word The word to set favorite status for.
     * @param isFavorite The new favorite status of the word.
     */
    fun onSetFavorite(word: String, isFavorite: Boolean) {
        viewModelScope.launch {
            favoritesRepository.saveFavorite(
                word,
                isFavorite,
            )
        }
    }

    /**
     * Speaks the given word using text-to-speech.
     *
     * @param word The word to speak.
     */
    fun onSpeakWord(word: String) {
        speakTextUseCase(word)
    }

    /**
     * Creates and emits a share object for the current dictionary entry.
     */
    fun onShare() {
        viewModelScope.launch {
            val currentState = state.value
            if (currentState is DictionaryScreenState.Success) {
                val shareData = createDictionaryShareUseCase(currentState.entry)
                share.value = shareData
            }
        }
    }

    /**
     * The share content was shared.
     *
     * Reset the share to null.
     */
    fun onShareSent() {
        share.value = null
    }

    /**
     * Navigates to search the word in a specific tab.
     *
     * @param word The word to search.
     * @param tab The tab to search in.
     */
    fun onSearchInTab(word: String, tab: Tab) {
        // This would typically trigger navigation via some callback
        // Implementation depends on the navigation system
    }
}
