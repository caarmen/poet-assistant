/*
 * Copyright (c) 2018 Carmen Alvarez
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

import android.text.Selection
import android.text.SpannableStringBuilder
import androidx.test.core.app.ApplicationProvider
import ca.rmen.android.poetassistant.Tts
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class TestReaderViewModel {

    @get:Rule
    val hiltTestRule = HiltAndroidRule(this)

    @Inject lateinit var tts: Tts

    private lateinit var viewModel: ReaderViewModel

    @Before
    fun setUp() {
        hiltTestRule.inject()
        viewModel = ReaderViewModel(ApplicationProvider.getApplicationContext(), tts)
    }

    @After
    fun tearDown() {
        // Would like to clear the viewModel, but onCleared() is protected.
    }

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
    fun testGetTextToSpeakCursorNearEndFollowedByWhitespace() {
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
        testGetTextToSpeak("Hello there", -1, -1, "Hello there")
    }

    @Test
    fun testGetTextToSpeakSpaceText() {
        testGetTextToSpeak(" ", -1, -1, " ")
    }

    @Test
    fun testGetTextToSpeakEmptyText() {
        testGetTextToSpeak("", -1, -1, "")
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
        val spannableStringBuilder = SpannableStringBuilder(text)
        Selection.setSelection(spannableStringBuilder, selectionStart, selectionEnd)
        val textToSpeak = viewModel.getTextToSpeak(spannableStringBuilder)
        assertEquals(expectedTextToSpeak, textToSpeak)
    }
}
