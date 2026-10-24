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

package ca.rmen.android.poetassistant.main.reader.usecases

import androidx.compose.ui.text.TextRange
import ca.rmen.android.poetassistant.Tts
import ca.rmen.android.poetassistant.TtsState
import ca.rmen.android.poetassistant.main.common.usecases.SpeakTextUseCase
import javax.inject.Inject

/**
 * Use case for toggling the text-to-speech playback of the poem:
 * stop when speaking, speak the selected poem text otherwise.
 *
 * The text extraction rules:
 * - No or collapsed selection: speak from the cursor to the end
 *   (the whole text when the cursor is at 0 or at the end).
 * - Blank selection: speak from the selection start to the end.
 * - Non-blank selection: speak the selection.
 *
 * @param tts The text-to-speech service (stop and isSpeaking checks).
 * @param speakTextUseCase Use case for speaking the extracted text.
 */
class ToggleReaderSpeechUseCase @Inject constructor(
    private val tts: Tts,
    private val speakTextUseCase: SpeakTextUseCase,
) {

    /**
     * The outcome of toggling the speech playback.
     */
    sealed class Result {
        object Stopped : Result()
        object Spoken : Result()
        object TtsError : Result()
    }

    /**
     * Toggles the speech playback of the given poem text.
     *
     * @param text The poem text.
     * @param selection The current selection within the poem text.
     * @return the outcome: stopped, spoken, or a TTS error.
     */
    operator fun invoke(text: String, selection: TextRange): Result {
        return if (tts.isSpeaking()) {
            tts.stop()
            Result.Stopped
        } else {
            val ttsState = tts.getTtsState()
            if (ttsState?.currentStatus == TtsState.TtsStatus.INITIALIZED) {
                speakTextUseCase(extractTextToSpeak(text, selection))
                Result.Spoken
            } else {
                Result.TtsError
            }
        }
    }

    companion object {

        /**
         * Extracts the portion of the poem text to speak, based on the
         * selection. Compose selections are never -1; a collapsed
         * selection is the default (start == end).
         *
         * @param text The poem text.
         * @param selection The selection within the poem text.
         * @return the text to speak.
         */
        fun extractTextToSpeak(text: String, selection: TextRange): String {
            var start = selection.min
            var end = selection.max
            // A cursor at the end of the text reads the whole text.
            if (start == text.length) start = 0
            // A collapsed cursor or a blank selection reads to the end
            // of the text.
            if (start == end || text.substring(start, end).trim().isEmpty()) {
                end = text.length
            }
            val result = text.substring(start, end)
            // A blank result (all-whitespace poem) reads the whole text.
            return if (result.trim().isEmpty()) text else result
        }
    }
}
