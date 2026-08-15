package ca.rmen.android.poetassistant.main.favorites

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.rmen.android.poetassistant.R
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

@HiltViewModel
class FavoritesScreenViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val createFavoritesShareUseCase: CreateFavoritesShareUseCase,
) : ViewModel() {

    /**
     * Sorted list of favorite words as StateFlow.
     * Sorted alphabetically to match FavoritesLiveData behavior.
     */
    val favorites: StateFlow<List<String>> = favoritesRepository.getFavoritesFlow()
        .map { favEntities -> favEntities.map { it.getWord() }.sorted() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /**
     * Current layout setting from SharedPreferences, observed dynamically.
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
     * Initiates sharing of the favorites list.
     * Creates share content via CreateFavoritesShareUseCase and exposes it through the share StateFlow.
     */
    fun onShare() {
        viewModelScope.launch {
            share.value = createFavoritesShareUseCase.invoke()
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
     * Toggle a word's favorite status.
     * In the Favorites tab, this will always remove the word (set isFavorite to false).
     */
    fun onToggleFavorite(word: String) {
        viewModelScope.launch {
            favoritesRepository.saveFavorite(word, false)
        }
    }

    /**
     * Called when text is copied to clipboard.
     * Sets snackbarTextResId to trigger showing the "Copied to clipboard" message.
     */
    fun onCopiedText() {
        snackbarTextResId.value = R.string.snackbar_copied_text
    }

    /**
     * Called when the snackbar has been shown.
     * Resets snackbarTextResId to null to hide the snackbar.
     */
    fun onSnackbarShown() {
        snackbarTextResId.value = null
    }

    /**
     * Clear all favorites.
     */
    fun onDeleteAll() {
        viewModelScope.launch {
            favoritesRepository.clear()
        }
    }
}
