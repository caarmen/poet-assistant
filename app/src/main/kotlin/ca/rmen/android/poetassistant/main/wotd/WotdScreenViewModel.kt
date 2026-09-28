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

package ca.rmen.android.poetassistant.main.wotd

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.common.models.ExternalAppMenuItem
import ca.rmen.android.poetassistant.main.common.models.Share
import ca.rmen.android.poetassistant.main.common.usecases.GetProcessTextMenuItemsUseCase
import ca.rmen.android.poetassistant.main.favorites.data.FavoritesRepository
import ca.rmen.android.poetassistant.main.wotd.usecases.CreateWotdHistoryShareUseCase
import ca.rmen.android.poetassistant.main.wotd.usecases.GetWotdHistoryUseCase
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the WOTD history screen.
 * Exposes the history state derived from the favorites flow, the layout
 * setting, and handles share, favorite and clipboard actions.
 *
 * @param settingsRepository Repository for accessing settings.
 * @param favoritesRepository Repository for managing favorites.
 * @param getWotdHistoryUseCase Use case for generating the history.
 * @param createWotdHistoryShareUseCase Use case for creating share content.
 * @param getProcessTextMenuItemsUseCase Use case for getting external app menu items.
 */
@HiltViewModel
class WotdScreenViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val getWotdHistoryUseCase: GetWotdHistoryUseCase,
    private val createWotdHistoryShareUseCase: CreateWotdHistoryShareUseCase,
    private val getProcessTextMenuItemsUseCase: GetProcessTextMenuItemsUseCase,
) : ViewModel() {

    /**
     * Current UI state for the WOTD history screen.
     * Re-runs the history use case when the favorites change. Room re-emits
     * the current favorites on each re-subscription, so re-entering the tab
     * always recomputes the history.
     */
    val state: StateFlow<WotdScreenState> = favoritesRepository.getFavoritesFlow()
        .map { favorites -> favorites.map { it.getWord() }.toSet() }
        .map { favoriteWords -> WotdScreenState.Success(getWotdHistoryUseCase(favoriteWords)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WotdScreenState.Idle
        )

    /**
     * Current layout setting from SharedPreferences, observed dynamically.
     * Used for UI display only (efficient vs clean mode).
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
     * Observed by the Fragment to show/hide snackbar in the legacy View system.
     */
    @StringRes val snackbarTextResId: StateFlow<Int?> field = MutableStateFlow(null)

    /**
     * Share data to be shared. Null means no share action is pending.
     * Observed by the Fragment to trigger the share intent.
     */
    val share: StateFlow<Share?> field = MutableStateFlow(null)

    /**
     * Sets the favorite status for a word.
     * The WOTD star can both add and remove favorites.
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
     * Initiates sharing of the history.
     * Creates share content via CreateWotdHistoryShareUseCase and exposes it
     * through the share StateFlow.
     */
    fun onShare() {
        val currentState = state.value
        if (currentState is WotdScreenState.Success) {
            share.value = createWotdHistoryShareUseCase(currentState.entries)
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
