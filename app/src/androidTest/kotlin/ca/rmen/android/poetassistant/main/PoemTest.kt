/*
 * Copyright (c) 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main


import android.annotation.TargetApi
import android.os.Build
import android.os.SystemClock
import androidx.appcompat.widget.ActionBarContextView
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.NoMatchingRootException
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.PerformException
import androidx.test.espresso.ViewInteraction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.UiDevice
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.reader.WordCounter
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalToIgnoringCase
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.lessThan
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Locale
import junit.framework.AssertionFailedError
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import junit.framework.TestCase.assertFalse

import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ca.rmen.android.poetassistant.main.CustomViewActions.longTap
import ca.rmen.android.poetassistant.main.TestAppUtils.clearPoem
import ca.rmen.android.poetassistant.main.TestAppUtils.clickDialogPositiveButton
import ca.rmen.android.poetassistant.main.TestAppUtils.typeAndSpeakPoem
import ca.rmen.android.poetassistant.main.TestAppUtils.typePoem
import ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab
import ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference
import ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem
import ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft

@LargeTest
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PoemTest {

    @JvmField
    @Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> = PoetAssistantActivityTestRule(MainActivity::class.java, true)

    @Test
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun exportAudioTest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return
        swipeViewPagerLeft(3)
        typeAndSpeakPoem("Will export some text")
        val exportDir = File(activityTestRule.activity.filesDir, "export")
        val poemFile = File(exportDir, "poem.wav")
        assertFalse(poemFile.exists())
        openMenuItem(R.string.share_poem_audio)
        assertTrue(poemFile.exists())
        val length1 = poemFile.length()
        val fileDate1 = poemFile.lastModified()

        // Try another one

        UiDevice.getInstance(getInstrumentation()).pressBack()
        onView(allOf(withId(R.id.tv_text), isDisplayed())).perform(clearText())
        SystemClock.sleep(250)
        typeAndSpeakPoem("Will export some text which is a bit longer")
        openMenuItem(R.string.share_poem_audio)
        assertTrue(poemFile.exists())
        val length2 = poemFile.length()
        val fileDate2 = poemFile.lastModified()
        assertThat("Expected second file to be newer than first file", fileDate1, lessThan(fileDate2))
        assertThat("Expected second file to be larger than first file", length1, lessThan(length2))
    }

    @Test
    fun lookupFromPoemTest() {
        swipeViewPagerLeft(3)
        val poemText = "Here is a poem"
        typeAndSpeakPoem(poemText)

        // Look up in the rhymer
        // Long press on the left part of the EditText, to select the first word
        val firstWord = poemText.substring(0, poemText.indexOf(' ')).lowercase(Locale.getDefault())
        onView(withId(R.id.tv_text)).perform(longTap(1, 0))

        // Select the "rhymer" popup
        clickPopupView("rhymer")
        checkTitleStripOrTab(activityTestRule.activity, R.string.tab_rhymer)
        onView(allOf(withId(R.id.tv_list_header), isDisplayed())).check(matches(withText(firstWord)))

        // Look up in the thesaurus
        swipeViewPagerLeft(3)
        onView(withId(R.id.tv_text)).perform(longTap(1, 0))
        clickPopupView("thesaurus")
        onView(allOf(withId(R.id.tv_list_header), isDisplayed())).check(matches(withText(firstWord)))

        // Look up in the dictionary
        swipeViewPagerLeft(2)
        onView(withId(R.id.tv_text)).perform(longTap(1, 0))
        clickPopupView("dictionary")
        onView(allOf(withId(R.id.tv_list_header), isDisplayed())).check(matches(withText(firstWord)))
    }

    @Test
    fun testLookupSetting() {
        openMenuItem(R.string.action_settings)
        clickPreference(R.string.pref_selection_lookup_title)
        pressBack()
        swipeViewPagerLeft(3)
        val poemText = "Here is a poem with lookup disabled"
        typePoem(poemText)

        assertPopupMissing("rhymer")
        assertPopupMissing("thesaurus")
        assertPopupMissing("dictionary")
        closeSoftKeyboard()

        openMenuItem(R.string.action_settings)
        clickPreference(R.string.pref_selection_lookup_title)
        pressBack()
        assertPopupVisible("rhymer")
        assertPopupVisible("thesaurus")
        assertPopupVisible("dictionary")
    }

    @Test
    fun testWordCount() {
        swipeViewPagerLeft(3)
        onView(withId(R.id.reader_word_count)).check(matches(not(isDisplayed())))
        val poemText = "Here is some text"
        typePoem(poemText)
        // Need to wait for the debounce to finish
        SystemClock.sleep(1000)
        onView(withId(R.id.reader_word_count))
                .check(matches(isDisplayed()))
                .check(matches(withText(WordCounter.getWordCountText(activityTestRule.activity, poemText))))
                .perform(click())
        onView(withText(R.string.word_count_help_title))
                .check(matches(isDisplayed()))
        clickDialogPositiveButton(android.R.string.ok)
        checkTitleStripOrTab(activityTestRule.activity, R.string.tab_reader)

        clearPoem()
        SystemClock.sleep(1000)
        onView(withId(R.id.reader_word_count)).check(matches(not(isDisplayed())))
    }

    private fun assertPopupVisible(label: String) {
        onView(allOf(withId(R.id.tv_text), isDisplayed())).perform(click())
        onView(allOf(withId(R.id.tv_text), hasFocus())).perform(longTap(1, 0))
        getPopupView(label).check(matches(isDisplayed()))
        pressBack()
    }

    private fun assertPopupMissing(label: String) {
        onView(withId(R.id.tv_text)).perform(longTap(1, 0))
        var exceptionThrown = false
        try {
            getPopupView(label)
        } catch (_: NoMatchingViewException) {
            exceptionThrown = true
        }
        assertTrue(exceptionThrown)
        pressBack()
    }

    private fun getPopupView(label: String): ViewInteraction {
        SystemClock.sleep(220)
        try {
            val result = onView(withText(equalToIgnoringCase(label))).inRoot(isPlatformPopup())
            result.check(matches(isDisplayed()))
            return result
        } catch (e: Throwable) {
            when (e) {
                is PerformException,
                is NoMatchingRootException,
                is NoMatchingViewException,
                is AssertionFailedError -> {
                    // I haven't yet found a better way to handle this :(
                    // On smaller screens the items are hidden behind an overflow item with id "overflow" which is inaccessible
                    try {
                        onView(withContentDescription("More options")).inRoot(isPlatformPopup()).perform(click())
                        val result = onView(withText(equalToIgnoringCase(label))).inRoot(isPlatformPopup())
                        result.check(matches(isDisplayed()))
                        return result
                    } catch (_: NoMatchingRootException) {
                        onView(allOf(
                            withContentDescription("More options"),
                            isDescendantOfA(withClassName(`is`(ActionBarContextView::class.java.name)))))
                            .perform(click())
                        return onView(withText(equalToIgnoringCase(label)))
                    }

                }
                else -> throw e
            }
        }
    }

    private fun clickPopupView(label: String) {
        getPopupView(label).perform(click())
    }
}
