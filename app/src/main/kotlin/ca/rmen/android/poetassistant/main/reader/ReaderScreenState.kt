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

package ca.rmen.android.poetassistant.main.reader

import androidx.compose.ui.text.input.TextFieldValue
import ca.rmen.android.poetassistant.main.reader.data.Poem
/**
 * Word and character counts for the current poem text.
 */
data class WordCount(val words: Int, val characters: Int)

/**
 * Play button state; the composable maps this to an icon.
 */
enum class PlayButtonState { PLAY_ENABLED, PLAY_DISABLED, STOP }

/**
 * UI state for the reader screen.
 *
 * @param textFieldValue The poem text and selection — one source of truth.
 * @param playButtonState Whether the play button is enabled, and whether
 * it shows play or stop.
 * @param wordCount The word and character counts, or null to hide the
 * count.
 * @param poem The current poem, if any: a persisted poem when a file
 * backs it, a temp poem otherwise. Drives save-menu enablement.
 */
data class ReaderScreenState(
    val textFieldValue: TextFieldValue,
    val playButtonState: PlayButtonState,
    val wordCount: WordCount?,
    val poem: Poem?,
)
