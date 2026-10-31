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

package ca.rmen.android.poetassistant.main.settings

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.TtsState
import ca.rmen.android.poetassistant.main.common.usecases.GetFileDisplayNameUseCase
import ca.rmen.android.poetassistant.main.common.usecases.GetTtsStateUseCase
import ca.rmen.android.poetassistant.main.common.usecases.SpeakTextUseCase
import ca.rmen.android.poetassistant.main.common.usecases.StopTtsUseCase
import ca.rmen.android.poetassistant.main.dictionaries.search.SuggestionsRepository
import ca.rmen.android.poetassistant.main.favorites.data.FavoritesRepository
import ca.rmen.android.poetassistant.main.settings.data.VoiceRepository
import ca.rmen.android.poetassistant.main.settings.usecases.ApplyThemeUseCase
import ca.rmen.android.poetassistant.main.settings.usecases.SetSelectionLookupEnabledUseCase
import ca.rmen.android.poetassistant.main.wotd.usecases.SetWotdEnabledUseCase
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.settings.Settings
import ca.rmen.android.poetassistant.settings.SettingsRepository
import ca.rmen.android.poetassistant.settings.Theme
import ca.rmen.android.poetassistant.settings.WotdNotificationPriority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the settings screen. Replaces the legacy
 * SettingsViewModel. All settings are written through
 * [SettingsRepository]; no legacy preference class is referenced.
 */
@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    application: Application,
    private val settingsRepository: SettingsRepository,
    private val voiceRepository: VoiceRepository,
    private val favoritesRepository: FavoritesRepository,
    private val suggestionsRepository: SuggestionsRepository,
    private val getFileDisplayNameUseCase: GetFileDisplayNameUseCase,
    private val speakTextUseCase: SpeakTextUseCase,
    private val stopTtsUseCase: StopTtsUseCase,
    private val getTtsStateUseCase: GetTtsStateUseCase,
    private val setWotdEnabledUseCase: SetWotdEnabledUseCase,
    private val applyThemeUseCase: ApplyThemeUseCase,
    private val setSelectionLookupEnabledUseCase: SetSelectionLookupEnabledUseCase,
) : AndroidViewModel(application) {

    private val ttsState = MutableStateFlow<TtsState?>(getTtsStateUseCase.current)
    private val voices = MutableStateFlow<List<ca.rmen.android.poetassistant.settings.TtsVoice>>(emptyList())

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
     * One-shot events (snackbars, permission-denied navigation).
     * No-replay SharedFlow: collected only while the screen is started,
     * so events do not replay on re-entry (spec §3.4).
     */
    val events: SharedFlow<SettingsEvent> field = MutableSharedFlow(extraBufferCapacity = 10)

    /**
     * Requests the POST_NOTIFICATIONS permission: collected by the
     * screen, which owns the launcher (launchers need a lifecycle
     * owner). The switch reverts by itself: with the permission not
     * granted, the pref is not written, so the setting stays false.
     */
    val notificationPermissionRequests: SharedFlow<Unit> field = MutableSharedFlow(extraBufferCapacity = 1)

    init {
        // Voice list loading: state-based, with a staleness guard
        // (spec §3.4/§3.7). Loads when the collected TTS state has
        // currentStatus == INITIALIZED, but only when the loaded list is
        // empty or stale. Without the guard, the voices would be
        // re-enumerated after every preview utterance ends (the
        // utterance listener also emits INITIALIZED then).
        viewModelScope.launch {
            // Seed from the current state before the first collection:
            // Tts is application-scoped, so TTS is normally already
            // initialized when the screen opens, and the first collected
            // state is then (INITIALIZED, INITIALIZED).
            getTtsStateUseCase.current?.let { onTtsState(it) }
            getTtsStateUseCase().collect { it?.let { newState -> onTtsState(newState) } }
        }

        // WOTD side effect: the settings-flow collector is the single
        // trigger, exactly as in the legacy SettingsViewModel. Handlers
        // only write through the repository; they never call the use
        // case directly.
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
     * Preview toggle (spec §1.3.10): if the engine is speaking, the
     * tap stops it; otherwise it speaks the preview. The derived
     * isSpeaking flag becomes true only when the engine fires onStart,
     * so a fast double-tap can start the preview twice instead of
     * stopping it: accepted race (spec §3.4).
     */
    fun onPreviewClicked() {
        if (state.value.isSpeaking) {
            stopTtsUseCase()
        } else {
            speakTextUseCase(getApplication<Application>().getString(R.string.pref_voice_preview_text))
        }
    }

    fun onVoiceSelected(voiceId: String) {
        settingsRepository.setVoice(voiceId)
    }

    fun onVoiceSpeedChanged(voiceSpeed: Int) {
        settingsRepository.setVoiceSpeed(voiceSpeed)
    }

    fun onVoicePitchChanged(voicePitch: Int) {
        settingsRepository.setVoicePitch(voicePitch)
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
        settingsRepository.setTheme(theme)
        applyThemeUseCase(theme)
    }

    fun onLayoutSelected(layout: Layout) {
        settingsRepository.setLayout(layout)
    }

    fun onSelectionLookupChanged(enabled: Boolean) {
        settingsRepository.setSelectionLookupEnabled(enabled)
        setSelectionLookupEnabledUseCase(enabled)
    }

    /**
     * WOTD switch. On API 33+ the permission gate runs before the pref
     * write (spec §3.5): with the permission not granted, the pref is
     * not written, the switch reverts by itself, and the permission is
     * requested. On grant, the pref is written and the settings-flow
     * collector applies the schedule.
     */
    fun onWotdEnabledChanged(enabled: Boolean) {
        if (!enabled) {
            settingsRepository.setWotdEnabled(false)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                getApplication<Application>(),
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionRequests.tryEmit(Unit)
            return
        }
        settingsRepository.setWotdEnabled(true)
    }

    /**
     * Handles the POST_NOTIFICATIONS permission result. On grant, the
     * pref is written and the settings-flow collector applies the
     * schedule. On denial, no pref write: the switch reverts by itself
     * (the setting stays false). If the denial is permanent, the
     * app-details settings screen is offered.
     */
    fun onNotificationPermissionResult(granted: Boolean, permanentlyDenied: Boolean) {
        if (granted) {
            settingsRepository.setWotdEnabled(true)
        } else if (permanentlyDenied) {
            events.tryEmit(SettingsEvent.OpenAppDetailsSettings)
        }
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
                favoritesRepository.exportFavorites(getApplication(), uri)
                events.tryEmit(
                    SettingsEvent.ShowSnackbar(
                        getApplication<Application>().getString(R.string.export_favorites_success, fileDisplayName)
                    )
                )
            } catch (_: Exception) {
                events.tryEmit(
                    SettingsEvent.ShowSnackbar(
                        getApplication<Application>().getString(R.string.export_favorites_error, fileDisplayName)
                    )
                )
            }
        }
    }

    /**
     * Legacy parity: import shows a snackbar on error only. There is
     * deliberately no import-success snackbar (spec §1.3.8).
     */
    fun onImportFavorites(uri: Uri) {
        viewModelScope.launch {
            val fileDisplayName = getFileDisplayNameUseCase(uri)
            try {
                favoritesRepository.importFavorites(getApplication(), uri)
            } catch (_: Exception) {
                events.tryEmit(
                    SettingsEvent.ShowSnackbar(
                        getApplication<Application>().getString(R.string.import_favorites_error, fileDisplayName)
                    )
                )
            }
        }
    }

    fun onClearSearchHistory() {
        viewModelScope.launch {
            suggestionsRepository.clear()
            events.tryEmit(
                SettingsEvent.ShowSnackbar(getApplication<Application>().getString(R.string.search_history_cleared))
            )
        }
    }
}
