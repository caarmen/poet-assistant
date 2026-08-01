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
import android.util.Log
import android.view.KeyEvent
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.PerformException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressKey
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.swipeRight
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.CustomViewMatchers.childAtPosition
import ca.rmen.android.poetassistant.main.dictionaries.ResultListAdapter
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalToIgnoringCase

/**
 * Generic utility functions for Ui operations like opening a menu or swiping the screen.
 */
object TestUiUtils {

    private val TAG = Constants.TAG + TestUiUtils::class.simpleName

    fun openMenuItem(@StringRes titleRes: Int) {
        getInstrumentation().waitForIdleSync()
        swipeDown()
        try {
            onView(allOf(isDisplayed(), withClassName(endsWith("OverflowMenuButton")))).perform(click())
        } catch (e: NoMatchingViewException) {
            Log.wtf(TAG, "Couldn't press the menu button in the action bar. Resorting to the menu key event. Is this cheating?", e)
            onView(withId(android.R.id.content)).perform(pressKey(KeyEvent.KEYCODE_MENU))
            getInstrumentation().waitForIdleSync()
            SystemClock.sleep(400) // :(
        } catch (e: PerformException) {
            Log.wtf(TAG, "Couldn't press the menu button in the action bar. Resorting to the menu key event. Is this cheating?", e)
            onView(withId(android.R.id.content)).perform(pressKey(KeyEvent.KEYCODE_MENU))
            getInstrumentation().waitForIdleSync()
            SystemClock.sleep(400) // :(
        }
        onView(allOf(withId(R.id.title), withText(titleRes), isDisplayed())).perform(click())
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
        onView(withId(R.id.pager_title_strip)).check(matches(isDisplayed()))
        onView(allOf(withText(equalToIgnoringCase(context.getString(titleRes))),
                childAtPosition(
                        allOf(withId(R.id.pager_title_strip),
                                withParent(withId(R.id.view_pager))),
                        1),
                isCompletelyDisplayed()))
                .check(matches(isDisplayed()))

    }

    private fun checkSelectedTab(context: Context, @StringRes titleRes: Int) {
        onView(allOf(withText(equalToIgnoringCase(context.getString(titleRes))),
                isDescendantOfA(withId(R.id.tabs))))
                .check(matches(isSelected()))
    }

    fun scrollToPreference(@StringRes prefTitleRes: Int) {
        // Scroll to the preference in case it's not visible
        onView(withId(R.id.recycler_view))
                .perform(scrollTo<ResultListAdapter.ResultListEntryViewHolder>(hasDescendant(withText(prefTitleRes))))
    }

    fun clickPreference(@StringRes prefTitleRes: Int) {
        scrollToPreference(prefTitleRes)
        // click on the preference
        onView(withText(prefTitleRes)).perform(click())
    }
}
