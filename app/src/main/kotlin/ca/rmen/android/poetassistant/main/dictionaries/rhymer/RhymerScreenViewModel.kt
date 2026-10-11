/*
 * Copyright (c) 2016-present Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries.rhymer

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.common.models.ExternalAppMenuItem
import ca.rmen.android.poetassistant.main.common.models.Share
import ca.rmen.android.poetassistant.main.common.usecases.GetProcessTextMenuItemsUseCase
import ca.rmen.android.poetassistant.main.common.usecases.SpeakTextUseCase
import ca.rmen.android.poetassistant.main.dictionaries.rhymer.usecases.CreateRhymerShareUseCase
import ca.rmen.android.poetassistant.main.dictionaries.rhymer.usecases.LookupRhymesUseCase
import ca.rmen.android.poetassistant.main.favorites.data.FavoritesRepository
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for the rhymer screen.
 *
 * @param settingsRepository Repository for accessing settings.
 * @param favoritesRepository Repository for managing favorites.
 * @param lookupRhymesUseCase Use case for looking up rhymes.
 * @param createRhymerShareUseCase Use case for creating shareable content.
 * @param speakTextUseCase Use case for speaking words.
 * @param getProcessTextMenuItemsUseCase Use case for getting external app menu items.
 */
@HiltViewModel
class RhymerScreenViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val lookupRhymesUseCase: LookupRhymesUseCase,
    private val createRhymerShareUseCase: CreateRhymerShareUseCase,
    private val speakTextUseCase: SpeakTextUseCase,
    private val getProcessTextMenuItemsUseCase: GetProcessTextMenuItemsUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val synonymFilter = MutableStateFlow<String?>(null)

    private val lookupSettings: Flow<LookupRhymesUseCase.RhymerLookupSettings> =
        settingsRepository.settingsFlow.map {
            LookupRhymesUseCase.RhymerLookupSettings(
                isThesaurusReverseLookupEnabled = it.isThesaurusReverseLookupEnabled,
                isAllRhymesEnabled = it.isAllRhymesEnabled,
                isAOAAMatchEnabled = it.isAOAAMatchEnabled,
                isAORAOMatchEnabled = it.isAORAOMatchEnabled,
            )
        }.distinctUntilChanged()

    /**
     * Current UI state for the rhymer screen.
     * Re-runs the lookup when the query, the synonym filter, or the lookup
     * settings change. Favorites changes rebuild the items without a new
     * lookup.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<RhymerScreenState> = combine(query, synonymFilter, lookupSettings) { query, synonymFilter, lookupSettings ->
        Triple(query, synonymFilter, lookupSettings)
    }.distinctUntilChanged().flatMapLatest { (query, synonymFilter, lookupSettings) ->
        if (query.isBlank()) {
            flowOf(RhymerScreenState.Idle)
        } else {
            lookupRhymesUseCase(query, synonymFilter, lookupSettings).map { result ->
                if (result.items.isEmpty()) {
                    RhymerScreenState.NotFound(
                        word = result.word,
                        isFavorite = result.isFavorite,
                        synonymFilter = synonymFilter,
                    )
                } else {
                    RhymerScreenState.Success(
                        word = result.word,
                        isFavorite = result.isFavorite,
                        synonymFilter = synonymFilter,
                        entries = result.items,
                    )
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RhymerScreenState.Idle
    )

    /**
     * Current layout setting, observed dynamically.
     */
    val layout: StateFlow<Layout> = settingsRepository.settingsFlow.map {
        it.layout
    }.distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = settingsRepository.settingsFlow.value.layout
        )

    /**
     * Snackbar message resource ID to display. Null means no snackbar should be shown.
     */
    @StringRes
    val snackbarTextResId: StateFlow<Int?> field = MutableStateFlow(null)

    /**
     * Share data to be shared. Null means no share action is pending.
     */
    val share: StateFlow<Share?> field = MutableStateFlow(null)

    /**
     * Searches for a word in the rhymer.
     * A new search clears the synonym filter.
     *
     * @param query The word to search for.
     */
    fun onWordSearched(query: String) {
        synonymFilter.value = null
        this.query.value = query
    }

    /**
     * Applies the initial query (deep link).
     *
     * Idempotent: does nothing when a query was already applied. The
     * fragment re-applies the initial query on every view creation, so
     * this must neither reset a later search nor clear an active filter
     * on a configuration change.
     *
     * @param query The initial query.
     */
    fun onInitialQuerySearched(query: String) {
        if (this.query.value.isBlank()) this.query.value = query
    }

    /**
     * Applies a filter to the results: only words which are synonyms of
     * the filter word are shown. A blank input clears the filter.
     *
     * @param input The filter word.
     */
    fun onSynonymFilterSubmitted(input: String) {
        val normalized = input.lowercase(Locale.getDefault()).trim()
        synonymFilter.value = normalized.ifBlank { null }
    }

    /**
     * Clears the synonym filter.
     */
    fun onClearSynonymFilter() {
        synonymFilter.value = null
    }

    /**
     * Sets the favorite status of a word.
     *
     * @param word The word to update.
     * @param isFavorite The new favorite status.
     */
    fun onSetFavorite(word: String, isFavorite: Boolean) {
        viewModelScope.launch {
            favoritesRepository.saveFavorite(word, isFavorite)
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
     * Creates and emits a share object for the current results.
     * No-ops outside the Success state.
     */
    fun onShare() {
        val currentState = state.value
        if (currentState is RhymerScreenState.Success) {
            share.value = createRhymerShareUseCase(currentState.word, currentState.synonymFilter, currentState.entries)
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
     * Called when text is copied to the clipboard.
     * Sets the snackbar message for copy confirmation.
     */
    fun onCopiedText() {
        snackbarTextResId.value = R.string.snackbar_copied_text
    }

    /**
     * Called when the snackbar has been shown.
     * Resets the snackbar message.
     */
    fun onSnackbarShown() {
        snackbarTextResId.value = null
    }

    /**
     * Retrieves external apps that support process text for the given word.
     *
     * @param word The word to process with external apps.
     * @return List of external app menu items that can process the text.
     */
    suspend fun getExternalAppMenuItems(word: String): List<ExternalAppMenuItem> =
        getProcessTextMenuItemsUseCase(word)
}
