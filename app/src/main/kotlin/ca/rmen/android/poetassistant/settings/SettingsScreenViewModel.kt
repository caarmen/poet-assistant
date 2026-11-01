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

package ca.rmen.android.poetassistant.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.TtsState
import ca.rmen.android.poetassistant.main.common.usecases.GetFileDisplayNameUseCase
import ca.rmen.android.poetassistant.main.common.usecases.GetTtsStateUseCase
import ca.rmen.android.poetassistant.main.common.usecases.SpeakTextUseCase
import ca.rmen.android.poetassistant.main.common.usecases.StopTtsUseCase
import ca.rmen.android.poetassistant.main.dictionaries.search.SuggestionsRepository
import ca.rmen.android.poetassistant.main.favorites.data.FavoritesRepository
import ca.rmen.android.poetassistant.main.wotd.usecases.SetWotdEnabledUseCase
import ca.rmen.android.poetassistant.settings.data.VoiceRepository
import ca.rmen.android.poetassistant.settings.usecases.ApplyThemeUseCase
import ca.rmen.android.poetassistant.settings.usecases.SetSelectionLookupEnabledUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the settings screen. All settings are written through
 * [SettingsRepository]. The ViewModel uses no Android context: the
 * screen owns the permission flow and the string building.
 */
@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    private val settingsRepository: SettingsRepository,
    private val suggestionsRepository: SuggestionsRepository,
    private val voiceRepository: VoiceRepository,

    private val applyThemeUseCase: ApplyThemeUseCase,
    private val getFileDisplayNameUseCase: GetFileDisplayNameUseCase,
    private val getTtsStateUseCase: GetTtsStateUseCase,
    private val setSelectionLookupEnabledUseCase: SetSelectionLookupEnabledUseCase,
    private val setWotdEnabledUseCase: SetWotdEnabledUseCase,
    private val speakTextUseCase: SpeakTextUseCase,
    private val stopTtsUseCase: StopTtsUseCase,
) : ViewModel() {

    private val ttsState = MutableStateFlow(getTtsStateUseCase.current)
    private val voices = MutableStateFlow<List<TtsVoice>>(emptyList())

    /**
     * True when the voice list must be (re)loaded on the next
     * collected INITIALIZED TTS state: set on a collected UNINITIALIZED
     * state, which only happens on a Tts restart.
     */
    private var voicesStale = true

    /**
     * Current UI state. [SettingsScreenState.isSpeaking] is derived
     * from the TTS state: [TtsState] carries no isSpeaking field, and
     * the synchronous Tts.isSpeaking() must not be injected here.
     */
    val state: StateFlow<SettingsScreenState> = combine(
        settingsRepository.settingsFlow,
        ttsState,
        voices,
    ) { settings, ttsState, voices ->
        SettingsScreenState(
            settings = settings,
            isSpeaking = ttsState?.currentStatus == TtsState.TtsStatus.SPEAKING,
            voices = voices,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsScreenState(
            settings = settingsRepository.settingsFlow.value,
            isSpeaking = getTtsStateUseCase.current?.currentStatus == TtsState.TtsStatus.SPEAKING,
            voices = emptyList(),
        ),
    )

    /**
     * One-shot snackbars.
     * No-replay SharedFlow: collected only while the screen is started,
     * so snackbars do not replay on re-entry.
     */
    val snackbars: SharedFlow<SettingsSnackbar> field = MutableSharedFlow(extraBufferCapacity = 10)

    init {
        // Voice list loading: state-based, with a staleness guard.
        // Loads when the collected TTS state has currentStatus ==
        // INITIALIZED, but only when the loaded list is empty or stale.
        // Without the guard, the voices would be re-enumerated after
        // every preview utterance ends (the utterance listener also
        // emits INITIALIZED then).
        viewModelScope.launch {
            // The TTS state flow replays its last state to new
            // collectors, and the Tts constructor always emits one
            // state: when the screen opens with TTS already
            // initialized, the first collected state is that
            // (INITIALIZED, INITIALIZED) state.
            getTtsStateUseCase().filterNotNull().collect { onTtsState(it) }
        }

        // WOTD side effect: the settings-flow collector is the single
        // trigger. Handlers only write through the repository; they
        // never call the use case directly.
        viewModelScope.launch {
            var previousSettings = settingsRepository.settingsFlow.value
            settingsRepository.settingsFlow.collect { newSettings ->
                if (previousSettings.isWotdEnabled != newSettings.isWotdEnabled ||
                    previousSettings.wotdNotificationPriority != newSettings.wotdNotificationPriority
                ) {
                    setWotdEnabledUseCase()
                }
                previousSettings = newSettings
            }
        }
    }

    private suspend fun onTtsState(newTtsState: TtsState) {
        ttsState.value = newTtsState
        when (newTtsState.currentStatus) {
            TtsState.TtsStatus.UNINITIALIZED -> voicesStale = true
            TtsState.TtsStatus.INITIALIZED -> {
                if (voices.value.isEmpty() || voicesStale) {
                    voices.value = voiceRepository.getVoices()
                    voicesStale = false
                }
            }
            else -> {
                // SPEAKING, UTTERANCE_COMPLETE, UTTERANCE_ERROR: no voice
                // list action.
            }
        }
    }

    /**
     * Preview toggle: if the engine is speaking, the tap stops it;
     * otherwise it speaks the preview. The derived isSpeaking flag
     * becomes true only when the engine fires onStart, so a fast
     * double-tap can start the preview twice instead of stopping it.
     */
    fun onPreviewClicked() {
        if (state.value.isSpeaking) {
            stopTtsUseCase()
        } else {
            speakTextUseCase(R.string.pref_voice_preview_text)
        }
    }

    fun onVoiceSelected(voiceId: String) {
        settingsRepository.voice = voiceId
    }

    fun onVoiceSpeedChanged(voiceSpeed: Int) {
        settingsRepository.voiceSpeed = voiceSpeed
    }

    fun onVoicePitchChanged(voicePitch: Int) {
        settingsRepository.voicePitch = voicePitch
    }

    fun onAllRhymesChanged(enabled: Boolean) {
        settingsRepository.setAllRhymesEnabled(enabled)
    }

    fun onAOAAMatchChanged(enabled: Boolean) {
        settingsRepository.setAOAAMatchEnabled(enabled)
    }

    fun onAORAOMatchChanged(enabled: Boolean) {
        settingsRepository.setAORAOMatchEnabled(enabled)
    }

    fun onThesaurusReverseLookupChanged(enabled: Boolean) {
        settingsRepository.setThesaurusReverseLookupEnabled(enabled)
    }

    fun onThemeSelected(theme: Theme) {
        settingsRepository.theme = theme
        applyThemeUseCase()
    }

    fun onLayoutSelected(layout: Layout) {
        settingsRepository.setLayout(layout)
    }

    fun onSelectionLookupChanged(enabled: Boolean) {
        settingsRepository.setSelectionLookupEnabled(enabled)
        setSelectionLookupEnabledUseCase(enabled)
    }

    /**
     * Sets the word-of-the-day setting. On API 33+ the screen gates the
     * POST_NOTIFICATIONS permission before enabling: with the
     * permission not granted, this is not called with true, the switch
     * reverts by itself, and the permission is requested instead. The
     * settings-flow collector applies the schedule on the write.
     */
    fun onWotdEnabledChanged(enabled: Boolean) {
        settingsRepository.setWotdEnabled(enabled)
    }

    fun onWotdNotificationPrioritySelected(priority: WotdNotificationPriority) {
        // The settings-flow collector re-applies the schedule at the
        // new priority via REPLACE, when WOTD is enabled.
        settingsRepository.setWotdNotificationPriority(priority)
    }

    fun onExportFavorites(uri: Uri) {
        viewModelScope.launch {
            val fileDisplayName = getFileDisplayNameUseCase(uri)
            try {
                favoritesRepository.exportFavorites(uri)
                snackbars.tryEmit(SettingsSnackbar(R.string.export_favorites_success, listOf(fileDisplayName)))
            } catch (_: Exception) {
                snackbars.tryEmit(SettingsSnackbar(R.string.export_favorites_error, listOf(fileDisplayName)))
            }
        }
    }

    /**
     * Import shows a snackbar on error only: there is deliberately no
     * import-success snackbar.
     */
    fun onImportFavorites(uri: Uri) {
        viewModelScope.launch {
            val fileDisplayName = getFileDisplayNameUseCase(uri)
            try {
                favoritesRepository.importFavorites(uri)
            } catch (_: Exception) {
                snackbars.tryEmit(SettingsSnackbar(R.string.import_favorites_error, listOf(fileDisplayName)))
            }
        }
    }

    fun onClearSearchHistory() {
        viewModelScope.launch {
            suggestionsRepository.clear()
            snackbars.tryEmit(SettingsSnackbar(R.string.search_history_cleared))
        }
    }
}
