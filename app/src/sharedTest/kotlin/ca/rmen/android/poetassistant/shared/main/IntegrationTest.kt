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
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.CustomChecks.checkClipboard
import ca.rmen.android.poetassistant.main.MainActivity
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.TestAppUtils.addFilter
import ca.rmen.android.poetassistant.main.TestAppUtils.openFilter
import ca.rmen.android.poetassistant.main.TestAppUtils.search
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@LargeTest
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(AndroidJUnit4::class)
class IntegrationTest {

    @get:Rule(order = 0)
    val hiltTestRule: HiltAndroidRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val composeTestRule = createEmptyComposeRule()

    @get:Rule(order = 2)
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> =
        PoetAssistantActivityTestRule(MainActivity::class.java, true)

    @Test
    fun copyTest() {
        val context: Context = activityTestRule.activity
        search("donkey")
        val wordToCopy = "swanky"

        composeTestRule.onNodeWithText(wordToCopy).assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.menu_copy)).assertIsDisplayed().performClick()
        getInstrumentation().runOnMainSync { checkClipboard(context, wordToCopy) }
    }

    @Test
    fun saveFilterTest() {
        search("pugnacious")
        addFilter(composeTestRule, activityTestRule.activity, Tab.RHYMER, "vulturous", "rapacious")
        openFilter(composeTestRule, "vulturous")
        Espresso.closeSoftKeyboard()
        composeTestRule.onNodeWithText(activityTestRule.activity.getString(android.R.string.ok)).performClick()
    }
}
