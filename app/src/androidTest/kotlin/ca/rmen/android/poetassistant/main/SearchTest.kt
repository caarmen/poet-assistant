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
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.CustomChecks.checkPatterns
import ca.rmen.android.poetassistant.main.CustomChecks.checkRootViewCount
import ca.rmen.android.poetassistant.main.CustomChecks.checkSearchSuggestions
import ca.rmen.android.poetassistant.main.TestAppUtils.clearSearchHistory
import ca.rmen.android.poetassistant.main.TestAppUtils.openSearchView
import ca.rmen.android.poetassistant.main.TestAppUtils.search
import ca.rmen.android.poetassistant.main.TestAppUtils.starQueryWord
import ca.rmen.android.poetassistant.main.TestAppUtils.typeQuery
import ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab
import ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables.PATTERN_HEADER_HELP_TAG
import ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables.PATTERN_ITEM_STAR_TAG
import ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables.PATTERN_SCREEN_CONTENT_MAX_RESULTS_TAG
import ca.rmen.android.poetassistant.main.dictionaries.search.ui.composables.SEARCH_INPUT_FIELD_TAG
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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

    @get:Rule(order = 1)
    val composeTestRule = createEmptyComposeRule()

    @JvmField
    @Rule(order = 2)
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> = PoetAssistantActivityTestRule(MainActivity::class.java, true)

    @Test
    fun searchSuggestionsTest() {
        openSearchView(composeTestRule)
        typeQuery(composeTestRule, "heavy")
        checkSearchSuggestions(composeTestRule, "heavy", "heavyset", "heavyweight", "heavyweights")

        typeQuery(composeTestRule, "s")
        checkSearchSuggestions(composeTestRule, "heavyset")

        typeQuery(composeTestRule, "z")
        checkRootViewCount(activityTestRule.activity, 2)
    }

    @Test
    fun searchHistoryTest() {
        openSearchView(composeTestRule)
        checkRootViewCount(activityTestRule.activity, 1)

        typeQuery(composeTestRule, "carmen")
        checkRootViewCount(activityTestRule.activity, 2)

        composeTestRule.onNodeWithTag(SEARCH_INPUT_FIELD_TAG).performImeAction()

        getInstrumentation().waitForIdleSync()
        composeTestRule.waitForIdle()
        openSearchView(composeTestRule)
        checkSearchSuggestions(composeTestRule, "carmen")

        typeQuery(composeTestRule, "benoit")
        checkRootViewCount(activityTestRule.activity, 2)
        composeTestRule.onNodeWithTag(SEARCH_INPUT_FIELD_TAG).performImeAction()
        getInstrumentation().waitForIdleSync()

        openSearchView(composeTestRule)
        checkSearchSuggestions(composeTestRule, "benoit", "carmen")

        typeQuery(composeTestRule, "awes")
        checkSearchSuggestions(composeTestRule, "awesome", "awesomely", "awestruck")
        typeQuery(composeTestRule, "o")
        checkSearchSuggestions(composeTestRule, "awesome", "awesomely")

        Espresso.pressBack()
        Espresso.onIdle()
        composeTestRule.onNodeWithTag(SEARCH_INPUT_FIELD_TAG).performTextClearance()
        typeQuery(composeTestRule, "carme")
        checkSearchSuggestions(composeTestRule, "carmen", "carmelite")
        Espresso.pressBack()
        Espresso.pressBack()
        Espresso.onIdle()

        composeTestRule.waitForIdle()
        SystemClock.sleep(500) // :( the previous idle apis aren't enough
        clearSearchHistory(composeTestRule, activityTestRule.activity)

        openSearchView(composeTestRule)
        checkRootViewCount(activityTestRule.activity, 1)
        typeQuery(composeTestRule, "carme")
        checkSearchSuggestions(composeTestRule, "carmelite")
    }

    @Test
    fun patternSearchTest() {
        val context: Context = activityTestRule.activity
        search(composeTestRule, "h*llo")
        checkPatterns(context, composeTestRule, "h*llo", "hello", "hermosillo", "hollo", "hullo")
        search(composeTestRule, "h*llz")
        checkPatterns(context, composeTestRule, "h*llz")
        composeTestRule.onNodeWithTag(PATTERN_HEADER_HELP_TAG).assertExists().performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.pattern_help_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(android.R.string.ok)).performClick()
        onView(withId(android.R.id.content)).perform(swipeDown())
        search(composeTestRule, "hello")
        checkTitleStripOrTab(context, R.string.tab_rhymer)
    }

    @Test
    fun patternSearchWithFavoriteTest() {
        search(composeTestRule, "hello")
        starQueryWord(composeTestRule)
        // For some reason, sometimes the view scrolls up, hiding the search field :(
        // Scroll down so we can search again.
        onView(withId(android.R.id.content)).perform(swipeDown())
        search(composeTestRule, "he*o")
        checkPatterns(activityTestRule.activity, composeTestRule, "he*o", "hello", "head honcho", "hector hugh munro",
                "herero", "hereto", "hermosillo", "hero")
        composeTestRule.onNodeWithTag("${PATTERN_ITEM_STAR_TAG}hello").assertIsOn()
    }

    @Test
    fun patternSearchTooManyResultsTest() {
        search(composeTestRule, "a*")
        composeTestRule.onNodeWithTag(PATTERN_SCREEN_CONTENT_MAX_RESULTS_TAG).assertIsDisplayed()
    }
}
