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
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.CustomChecks.checkAllStarredWords
import ca.rmen.android.poetassistant.main.TestAppUtils.clearStarredWords
import ca.rmen.android.poetassistant.main.TestAppUtils.search
import ca.rmen.android.poetassistant.main.TestAppUtils.starQueryWord
import ca.rmen.android.poetassistant.main.TestAppUtils.unStarQueryWord
import ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft
import ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerRight
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FavoritesTest {

    @JvmField
    @Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> = PoetAssistantActivityTestRule(MainActivity::class.java, true)

    @Test
    fun favoritesTest() {
        val context: Context = activityTestRule.activity
        search("cheesecake")
        starQueryWord()
        onView(allOf(withId(R.id.btn_star_result), hasSibling(withText("ache")))).perform(click())
        swipeViewPagerLeft(4)
        checkAllStarredWords(context, "cheesecake", "ache")
        swipeViewPagerRight(3)
        unStarQueryWord()
        swipeViewPagerLeft(3)
        checkAllStarredWords(context, "ache")
        onView(allOf(withId(R.id.btn_star_result), hasSibling(withText("ache")), isDisplayed())).perform(click())
        checkAllStarredWords(context)
        swipeViewPagerRight(2)
        starQueryWord()
        swipeViewPagerLeft(2)
        checkAllStarredWords(context, "cheesecake")
        clearStarredWords()
        checkAllStarredWords(context)
    }
}
