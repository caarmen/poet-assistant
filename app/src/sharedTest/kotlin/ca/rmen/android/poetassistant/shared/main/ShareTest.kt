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

package ca.rmen.android.poetassistant.shared.main

import android.content.Context
import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.MainActivity
import ca.rmen.android.poetassistant.main.TestAppUtils.search
import ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem
import ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft
import ca.rmen.android.poetassistant.main.rules.PoetAssistantIntentsTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.`is`
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@LargeTest
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(AndroidJUnit4::class)
class ShareTest {

    @get:Rule(order = 0)
    val hiltTestRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityTestRule: PoetAssistantIntentsTestRule<MainActivity> =
        PoetAssistantIntentsTestRule(MainActivity::class.java)

    @Test
    fun shareRhymesTest() {
        search("merge")
        openMenuItem(R.string.share)
        checkShareIntentContains("upsurge")
    }

    @Test
    fun shareDictionaryTest() {
        search("a")
        swipeViewPagerLeft(2)
        openMenuItem(R.string.share)
        checkShareIntentContains("the blood group whose red cells carry the A antigen")
    }

    @Test
    fun sharePatternTest() {
        search("ho?t")
        openMenuItem(R.string.share)
        checkShareIntentContains("host")
    }

    @Test
    fun shareWotdTest() {
        val context: Context = activityTestRule.activity
        openMenuItem(R.string.action_wotd_history)
        openMenuItem(R.string.share)
        checkShareIntentContains(context.getString(R.string.share_wotd_title))
    }

    @Test
    fun sharePopupTest() {
        search("strawberry")
        val context: Context = activityTestRule.activity
        onView(allOf(withText("adversary"), isDisplayed())).perform(click())
        onView(allOf(withText(endsWith(context.getString(R.string.share))), isDisplayed())).perform(click())
        checkShareIntentEquals("adversary")
    }

    private fun checkShareIntentContains(expectedText: String) {
        intended(allOf(
            hasAction(Intent.ACTION_CHOOSER),
            hasExtra(`is`(Intent.EXTRA_INTENT),
                allOf(
                    hasAction(Intent.ACTION_SEND),
                    hasExtra(containsString(Intent.EXTRA_TEXT), containsString(expectedText))
                )
            )
        ))
    }

    private fun checkShareIntentEquals(expectedText: String) {
        intended(allOf(
            hasAction(Intent.ACTION_CHOOSER),
            hasExtra(`is`(Intent.EXTRA_INTENT),
                allOf(
                    hasAction(Intent.ACTION_SEND),
                    hasExtra(Intent.EXTRA_TEXT, expectedText)
                )
            )
        ))
    }
}
