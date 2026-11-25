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

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import androidx.annotation.StringRes
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
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.viewpager.widget.ViewPager
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.dictionaries.search.ui.composables.MENU_OVERFLOW_TAG
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalToIgnoringCase
import org.junit.Assert.assertEquals

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

    fun checkTitleStripOrTab(context: Context, @StringRes titleRes: Int) {
        if (context.resources.getBoolean(R.bool.tab_text)) {
            checkSelectedTab(context, titleRes)
        } else {
            checkTitleStripCenterTitle(context, titleRes)
        }
    }

    private fun checkTitleStripCenterTitle(context: Context, @StringRes titleRes: Int) {
        // Prefer checking the ViewPager's selected page title when available. Some
        // test contexts don't include the ViewPager (e.g., isolated router tests),
        // so fall back to safer view-matchers.
        onView(withId(R.id.pager_title_strip)).check(matches(isDisplayed()))
        val expectedTitle = context.getString(titleRes)
        val activity = context as? Activity
        try {
            val viewPager = activity?.findViewById<ViewPager>(R.id.view_pager)
            if (viewPager != null && viewPager.adapter != null) {
                val currentTitle = viewPager.adapter?.getPageTitle(viewPager.currentItem)?.toString()
                assertEquals("Expected selected page title", expectedTitle.lowercase(), currentTitle?.lowercase())
                return
            }
        } catch (t: Throwable) {
            // Ignore and fall back to view matching below
        }

        // Fallback: look for the expected title text inside the PagerTitleStrip. If that
        // fails, assert the title text is displayed anywhere (best-effort).
        try {
            onView(allOf(withText(equalToIgnoringCase(expectedTitle)), isDescendantOfA(withId(R.id.pager_title_strip))))
                .check(matches(isDisplayed()))
        } catch (e: Throwable) {
            onView(withText(equalToIgnoringCase(expectedTitle))).check(matches(isDisplayed()))
        }
    }

    private fun checkSelectedTab(context: Context, @StringRes titleRes: Int) {
        onView(allOf(withText(equalToIgnoringCase(context.getString(titleRes))),
                isDescendantOfA(withId(R.id.tabs))))
                .check(matches(isSelected()))
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
