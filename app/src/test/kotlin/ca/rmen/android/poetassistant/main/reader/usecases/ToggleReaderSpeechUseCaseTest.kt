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
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests the text extraction rules of ToggleReaderSpeechUseCase.
 * Compose selections are never negative; the tests cover the
 * "negative selection" cases with a collapsed selection at 0.
 */
class ToggleReaderSpeechUseCaseTest {

    @Test
    fun testGetTextToSpeakWholeTextSelected() {
        testGetTextToSpeak("Hello there", 0, 11, "Hello there")
    }

    @Test
    fun testGetTextToSpeakCursorAtBeginning() {
        testGetTextToSpeak("Hello there", 0, 0, "Hello there")
    }

    @Test
    fun testGetTextToSpeakCursorAtEnd() {
        testGetTextToSpeak("Hello there", 11, 11, "Hello there")
    }

    @Test
    fun testGetTextToSpeakSelectionNearEndFollowedByWhitespace() {
        // The selection 11..17 is whitespace-only: the extracted range
        // extends to the end, the result is blank, the whole text is
        // returned.
        testGetTextToSpeak("Hello there       ", 11, 17, "Hello there       ")
    }

    @Test
    fun testGetTextToSpeakCursorInMiddleBeforeSpace() {
        testGetTextToSpeak("Hello there", 5, 11, " there")
    }

    @Test
    fun testGetTextToSpeakCursorInMiddleAfterSpace() {
        testGetTextToSpeak("Hello there", 6, 11, "there")
    }

    @Test
    fun testGetTextToSpeakNegativeSelection() {
        // Compose selections are never negative; this covers the
        // collapsed default at 0.
        testGetTextToSpeak("Hello there", 0, 0, "Hello there")
    }

    @Test
    fun testGetTextToSpeakSpaceText() {
        testGetTextToSpeak(" ", 0, 0, " ")
    }

    @Test
    fun testGetTextToSpeakEmptyText() {
        testGetTextToSpeak("", 0, 0, "")
    }

    @Test
    fun testGetTextToSpeakMiddleSelection() {
        testGetTextToSpeak("Hello there", 3, 9, "lo the")
    }

    @Test
    fun testGetTextToSpeakMiddleSelectionSpaces() {
        testGetTextToSpeak("Hello there", 5, 6, " there")
    }

    @Test
    fun testGetTextToSpeakMiddleSelectionEmpty() {
        testGetTextToSpeak("Hello there", 3, 3, "lo there")
    }

    private fun testGetTextToSpeak(text: String, selectionStart: Int, selectionEnd: Int, expectedTextToSpeak: String) {
        val textToSpeak = ToggleReaderSpeechUseCase.extractTextToSpeak(text, TextRange(selectionStart, selectionEnd))
        assertEquals(expectedTextToSpeak, textToSpeak)
    }
}
