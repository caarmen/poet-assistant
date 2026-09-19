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

package ca.rmen.android.poetassistant.main.dictionaries.patterns

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.common.models.ExternalAppMenuItem
import ca.rmen.android.poetassistant.main.common.models.Share
import ca.rmen.android.poetassistant.main.common.usecases.GetProcessTextMenuItemsUseCase
import ca.rmen.android.poetassistant.main.dictionaries.patterns.usecases.CreatePatternShareUseCase
import ca.rmen.android.poetassistant.main.dictionaries.patterns.usecases.LookupPatternUseCase
import ca.rmen.android.poetassistant.main.favorites.data.FavoritesRepository
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Pattern screen.
 * Manages UI state and handles actions for pattern search.
 *
 * @param settingsRepository Repository for accessing settings.
 * @param lookupPatternUseCase Use case for looking up pattern matches.
 * @param createPatternShareUseCase Use case for creating share content.
 * @param favoritesRepository Repository for managing favorites.
 * @param getProcessTextMenuItemsUseCase Use case for getting external app menu items.
 */
@HiltViewModel
class PatternScreenViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val lookupPatternUseCase: LookupPatternUseCase,
    private val createPatternShareUseCase: CreatePatternShareUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val getProcessTextMenuItemsUseCase: GetProcessTextMenuItemsUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")


    /**
     * Share data to be shared. Null means no share action is pending.
     */
    val share: StateFlow<Share?> field = MutableStateFlow(null)

    /**
     * Current layout setting from SharedPreferences, observed dynamically.
     * Used for UI display only (efficient vs clean mode), not for sorting.
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
     * Performs a pattern search.
     * Sets the query and triggers lookup via LookupPatternUseCase.
     *
     * @param pattern The pattern to search for.
     */
    fun onPatternSearched(pattern: String) {
        query.value = pattern
    }

    /**
     * Current UI state for the pattern screen.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<PatternScreenState> = query.flatMapLatest { pattern ->
        if (pattern.isBlank()) {
            flowOf(PatternScreenState.Idle)
        } else {
            val result = lookupPatternUseCase(pattern)
            result.entries.map { entries ->
                if (entries.isEmpty()) {
                    PatternScreenState.NotFound(pattern)
                } else {
                    PatternScreenState.Success(
                        query = pattern,
                        entries = entries,
                        isCapped = result.isCapped
                    )
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PatternScreenState.Idle
    )

    /**
     * Sets the favorite status for a word.
     *
     * @param word The word to update.
     * @param isFavorite The new favorite status.
     */
    fun onSetFavorite(word: String, isFavorite: Boolean) {
        viewModelScope.launch {
            favoritesRepository.saveFavorite(word, isFavorite)
            // After updating favorite, re-search if there's a current state with query
            val currentState = state.value
            if (currentState is PatternScreenState.Success) {
                onPatternSearched(currentState.query)
            }
        }
    }

    /**
     * Initiates sharing of the pattern search results.
     * Creates share content and exposes it through the share StateFlow.
     */
    fun onShare() {
        viewModelScope.launch {
            val currentState = state.value
            if (currentState is PatternScreenState.Success) {
                share.value = createPatternShareUseCase(currentState.query, currentState.entries)
            }
        }
    }

    /**
     * The share content was shared.
     * Reset the share to null.
     */
    fun onShareSent() {
        share.value = null
    }

    /**
     * Called when text is copied to clipboard.
     * Sets snackbar message for copy confirmation.
     */
    fun onCopiedText() {
        snackbarTextResId.value = R.string.snackbar_copied_text
    }

    /**
     * Called when snackbar has been shown.
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
