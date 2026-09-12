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
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.WordNotFoundException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
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

    val state: StateFlow<DictionaryScreenState>
        field = MutableStateFlow<DictionaryScreenState>(DictionaryScreenState.Idle)

    val share: StateFlow<Share?>
        field = MutableStateFlow<Share?>(null)

    private var currentQueryJob: Job? = null

    /**
     * Searches for a word in the dictionary.
     *
     * @param query The word to search for.
     */
    fun onWordSearched(query: String) {
        currentQueryJob?.cancel()
        currentQueryJob = viewModelScope.launch {
            try {
                val entry = lookupDictionaryEntryUseCase(query)
                val displayedWord = entry.word
                val isFavorite = favoritesRepository.getIsFavoriteFlow(displayedWord).first()
                state.value = DictionaryScreenState.Success(
                    entries = listOf(entry),
                    displayedWord = displayedWord,
                    isFavorite = isFavorite
                )
                favoritesRepository.getIsFavoriteFlow(displayedWord).collect { isFavorite ->
                    val currentState = state.value
                    if (currentState is DictionaryScreenState.Success &&
                        currentState.displayedWord == displayedWord) {
                        state.value = currentState.copy(isFavorite = isFavorite)
                    }
                }
            } catch (_: WordNotFoundException) {
                state.value = DictionaryScreenState.NotFound(query)
            }
        }
    }

    /**
     * Toggles the favorite status of a word.
     *
     * @param word The word to toggle.
     */
    fun onToggleFavorite(word: String) {
        viewModelScope.launch {
            favoritesRepository.saveFavorite(word, !favoritesRepository.getIsFavoriteFlow(word).first())
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
            if (currentState is DictionaryScreenState.Success && currentState.entries.isNotEmpty()) {
                val shareData = createDictionaryShareUseCase(currentState.entries.first())
                share.value = shareData
            }
        }
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
