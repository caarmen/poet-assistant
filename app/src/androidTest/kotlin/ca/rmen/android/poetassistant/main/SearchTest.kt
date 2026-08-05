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
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.CustomChecks.checkPatterns
import ca.rmen.android.poetassistant.main.CustomChecks.checkSearchSuggestions
import ca.rmen.android.poetassistant.main.CustomChecks.checkSingleRootView
import ca.rmen.android.poetassistant.main.CustomViewMatchers.withAdapterItemCount
import ca.rmen.android.poetassistant.main.TestAppUtils.clearSearchHistory
import ca.rmen.android.poetassistant.main.TestAppUtils.clickDialogPositiveButton
import ca.rmen.android.poetassistant.main.TestAppUtils.openSearchView
import ca.rmen.android.poetassistant.main.TestAppUtils.search
import ca.rmen.android.poetassistant.main.TestAppUtils.starQueryWord
import ca.rmen.android.poetassistant.main.TestAppUtils.typeQuery
import ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab
import ca.rmen.android.poetassistant.main.TestUiUtils.withCustomConstraints
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
class SearchTest {

    @JvmField
    @Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> = PoetAssistantActivityTestRule(MainActivity::class.java, true)

    @Test
    fun searchSuggestionsTest() {
        openSearchView()
        val searchAutoComplete = typeQuery("heavy")
        checkSearchSuggestions("heavy", "heavyset", "heavyweight", "heavyweights")

        searchAutoComplete.perform(typeText("s"))
        checkSearchSuggestions("heavyset")

        searchAutoComplete.perform(typeText("z"))
        checkSingleRootView(activityTestRule.activity)
    }

    @Test
    fun searchHistoryTest() {
        openSearchView()
        checkSingleRootView(activityTestRule.activity)

        val searchAutoComplete = typeQuery("carmen")
        checkSingleRootView(activityTestRule.activity)

        searchAutoComplete.perform(pressImeActionButton())

        getInstrumentation().waitForIdleSync()
        openSearchView()
        checkSearchSuggestions("carmen")

        typeQuery("benoit")
        checkSingleRootView(activityTestRule.activity)
        searchAutoComplete.perform(pressImeActionButton())
        getInstrumentation().waitForIdleSync()

        openSearchView()
        checkSearchSuggestions("benoit", "carmen")

        typeQuery("awes")
        checkSearchSuggestions("awesome", "awesomely", "awestruck")
        searchAutoComplete.perform(typeText("o"))
        checkSearchSuggestions("awesome", "awesomely")

        Espresso.pressBack()
        Espresso.onIdle()
        searchAutoComplete.perform(clearText())
        searchAutoComplete.perform(typeText("carme"))
        checkSearchSuggestions("carmen", "carmelite")
        Espresso.pressBack()
        Espresso.pressBack()
        Espresso.onIdle()

        clearSearchHistory()

        openSearchView()
        checkSingleRootView(activityTestRule.activity)
        typeQuery("carme")
        checkSearchSuggestions("carmelite")
    }

    @Test
    fun patternSearchTest() {
        val context: Context = activityTestRule.activity
        search("h*llo")
        checkPatterns(context, "h*llo", "hello", "hermosillo", "hollo", "hullo")
        search("h*llz")
        checkPatterns(context, "h*llz")
        onView(allOf(withId(R.id.btn_help), isDisplayed())).perform(click())
        onView(withText(R.string.pattern_help_title))
                .check(matches(isDisplayed()))
        clickDialogPositiveButton(android.R.string.ok)
        search("hello")
        checkTitleStripOrTab(context, R.string.tab_rhymer)
    }

    @Test
    fun patternSearchWithFavoriteTest() {
        search("hello")
        starQueryWord()
        // For some reason, sometimes the view scrolls up, hiding the search field :(
        // Scroll down so we can search again.
        onView(withId(R.id.rhymer_recycler_view))
            .perform(withCustomConstraints(swipeDown(), isDisplayingAtLeast(10)))
        search("he*o")
        checkPatterns(activityTestRule.activity, "he*o", "hello", "head honcho", "hector hugh munro",
                "herero", "hereto", "hermosillo", "hero")
        onView(allOf(withId(R.id.btn_star_result), hasSibling(withText("hello")), isDisplayed())).check(matches(isChecked()))
    }

    @Test
    fun patternSearchTooManyResultsTest() {
        search("a*")
        onView(allOf(withId(R.id.pattern_recycler_view), isDisplayed()))
                .check(matches(withAdapterItemCount(501)))
    }
}
