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

import ca.rmen.android.poetassistant.settings.Settings
import ca.rmen.android.poetassistant.settings.TtsVoice

/**
 * UI state for the settings screen.
 *
 * @param settings The current settings.
 * @param isSpeaking True when the text-to-speech engine is speaking
 * (derived from the TTS state, for the preview toggle).
 * @param voices The voices available on the device, empty until they
 * are loaded.
 */
data class SettingsScreenState(
    val settings: Settings,
    val isSpeaking: Boolean,
    val voices: List<TtsVoice>,
)

/**
 * One-shot events from the settings screen. Collected only while the
 * screen is started, with no replay: an event which arrives while the
 * screen is stopped is not shown when you come back (accepted
 * tradeoff, spec §3.4).
 */
sealed interface SettingsEvent {

    /**
     * Shows a snackbar with the given text.
     */
    data class ShowSnackbar(val text: String) : SettingsEvent

    /**
     * Opens the app details settings screen: offered when a
     * notification-permission denial is permanent.
     */
    data object OpenAppDetailsSettings : SettingsEvent
}
