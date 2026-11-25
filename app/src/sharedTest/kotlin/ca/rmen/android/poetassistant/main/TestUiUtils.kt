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

import android.content.Context
import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.swipeRight
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.dictionaries.search.ui.composables.MENU_OVERFLOW_TAG
import ca.rmen.android.poetassistant.main.ui.composables.TABS_TITLE_STRIP_LABEL_TAG
import org.hamcrest.Matchers.allOf

/**
 * Generic utility functions for Ui operations like opening a menu or swiping the screen.
 */
object TestUiUtils {

    fun openMenuItem(composeTestRule: ComposeTestRule, @StringRes titleRes: Int) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(MENU_OVERFLOW_TAG).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(getInstrumentation().targetContext.getString(titleRes)).performClick()
        composeTestRule.waitForIdle()
    }

    fun swipeViewPagerRight(count: Int) {
        for (i in 0 until count) {
            onView(allOf(withId(android.R.id.content), isDisplayed())).perform(swipeRight())
        }
        SystemClock.sleep(200)
    }

    fun swipeViewPagerLeft(count: Int) {
        for (i in 0 until count) {
            onView(allOf(withId(android.R.id.content), isDisplayed())).perform(swipeLeft())
        }
        SystemClock.sleep(200)
    }

    fun checkTitleStripOrTab(composeTestRule: ComposeTestRule, @StringRes titleRes: Int) {
        val context = getInstrumentation().targetContext
        val tabTitle = context.getString(titleRes)
        if (context.resources.getBoolean(R.bool.tab_text)) {
            composeTestRule.onNodeWithTag("tab_$tabTitle").assertIsSelected()
        } else {
            composeTestRule.onNodeWithTag(TABS_TITLE_STRIP_LABEL_TAG).assertIsDisplayed()
                .assertTextEquals(tabTitle)
        }
    }


    fun clickPreference(composeTestRule: ComposeTestRule, context: Context, @StringRes prefTitleRes: Int) {
        // Scroll to the preference in case it's not visible
        composeTestRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasText(context.getString(prefTitleRes)))

        // Click on the preference
        composeTestRule
            .onNodeWithText(context.getString(prefTitleRes))
            .performClick()
    }
}
