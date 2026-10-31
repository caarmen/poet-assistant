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

import androidx.annotation.StringRes

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
 * A one-shot snackbar from the settings screen. Collected only while
 * the screen is started, with no replay: a snackbar which arrives
 * while the screen is stopped is not shown when you come back.
 *
 * @param resId The string resource of the snackbar text.
 * @param formatArgs The format arguments for the text. The screen
 * builds the string.
 */
data class SettingsSnackbar(
    @StringRes val resId: Int,
    val formatArgs: List<String?> = emptyList(),
)
