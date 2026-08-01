/*
 * Copyright (c) 2017 - present Carmen Alvarez
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
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.CustomChecks.checkAllStarredWords
import ca.rmen.android.poetassistant.main.CustomChecks.checkClipboard
import ca.rmen.android.poetassistant.main.CustomChecks.checkRhymes
import ca.rmen.android.poetassistant.main.CustomChecks.checkStarredInList
import ca.rmen.android.poetassistant.main.TestAppUtils.addFilter
import ca.rmen.android.poetassistant.main.TestAppUtils.clearFilter
import ca.rmen.android.poetassistant.main.TestAppUtils.clearPoem
import ca.rmen.android.poetassistant.main.TestAppUtils.clearSearchHistory
import ca.rmen.android.poetassistant.main.TestAppUtils.clearStarredWords
import ca.rmen.android.poetassistant.main.TestAppUtils.openDictionary
import ca.rmen.android.poetassistant.main.TestAppUtils.openDictionaryCleanLayout
import ca.rmen.android.poetassistant.main.TestAppUtils.openThesaurus
import ca.rmen.android.poetassistant.main.TestAppUtils.openThesaurusCleanLayout
import ca.rmen.android.poetassistant.main.TestAppUtils.search
import ca.rmen.android.poetassistant.main.TestAppUtils.starQueryWord
import ca.rmen.android.poetassistant.main.TestAppUtils.typeAndSpeakPoem
import ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference
import ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem
import ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft
import ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerRight
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.endsWith
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class IntegrationTest {

    @JvmField
    @Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> = PoetAssistantActivityTestRule(MainActivity::class.java, true)

    private data class IntegrationTestScenario(
        val query: String,
        val firstRhyme: String,
        val secondRhyme: String,
        val firstSynonymForFirstRhyme: String,
        val secondSynonymForFirstRhyme: String,
        val firstDefinitionForSecondSynonym: String,
        val thesaurusFilter: String,
        val thesaurusFilterMatch: String,
        val rhymerFilter: String,
        val rhymerFilterMatch: String,
        val poem: String
    )

    private val scenario1 = IntegrationTestScenario(
        "howdy", "cloudy", "dowdy", "nebulose", "nebulous", "lacking definite form or limits",
        "bloody", "muddy", "bully", "rowdy",
        "Forever is composed of nows" // Emily Dickinson
    )

    private val scenario2 = IntegrationTestScenario(
        "beholden", "embolden", "golden", "hearten", "recreate", "create anew",
        "beer", "cheer", "wildness", "abandon",
        "roses are red, violets are blue\nespresso tests will find bugs for you"
    )

    private fun runIntegrationTest(data: IntegrationTestScenario) {
        val context: Context = activityTestRule.activity
        swipeViewPagerLeft(4)
        checkAllStarredWords(context)
        swipeViewPagerRight(4)
        search(data.query)
        checkRhymes(context, data.firstRhyme, data.secondRhyme)
        openThesaurus(context, data.firstRhyme, data.firstSynonymForFirstRhyme)
        openDictionary(context, data.secondSynonymForFirstRhyme, data.firstDefinitionForSecondSynonym)
        starQueryWord()
        swipeViewPagerLeft(2)
        checkAllStarredWords(context, data.secondSynonymForFirstRhyme)
        swipeViewPagerRight(3)
        checkStarredInList(data.secondSynonymForFirstRhyme)
        addFilter(Tab.THESAURUS, data.thesaurusFilter, data.thesaurusFilterMatch)
        clearFilter(Tab.THESAURUS, data.firstSynonymForFirstRhyme)
        swipeViewPagerRight(1)
        addFilter(Tab.RHYMER, data.rhymerFilter, data.rhymerFilterMatch)
        clearFilter(Tab.RHYMER, data.firstRhyme)
        swipeViewPagerLeft(3)
        typeAndSpeakPoem(data.poem)
        clearPoem()
        // clearing the search history doesn't erase starred words
        clearSearchHistory()
        swipeViewPagerLeft(1)
        checkAllStarredWords(context, data.secondSynonymForFirstRhyme)
        clearStarredWords()
        checkAllStarredWords(context)
    }

    private fun runCleanLayoutIntegrationTest(data: IntegrationTestScenario) {
        val context: Context = activityTestRule.activity
        useCleanLayout()
        swipeViewPagerLeft(4)
        checkAllStarredWords(context)
        swipeViewPagerRight(4)
        search(data.query)
        checkRhymes(context, data.firstRhyme, data.secondRhyme)
        openThesaurusCleanLayout(context, data.firstRhyme, data.firstSynonymForFirstRhyme)
        openDictionaryCleanLayout(context, data.secondSynonymForFirstRhyme, data.firstDefinitionForSecondSynonym)
        starQueryWord()
        swipeViewPagerLeft(2)
        checkAllStarredWords(context, data.secondSynonymForFirstRhyme)
        swipeViewPagerRight(3)
        checkStarredInList(data.secondSynonymForFirstRhyme)
        addFilter(Tab.THESAURUS, data.thesaurusFilter, data.thesaurusFilterMatch)
        clearFilter(Tab.THESAURUS, data.firstSynonymForFirstRhyme)
        swipeViewPagerRight(1)
        addFilter(Tab.RHYMER, data.rhymerFilter, data.rhymerFilterMatch)
        clearFilter(Tab.RHYMER, data.firstRhyme)
        swipeViewPagerLeft(3)
        typeAndSpeakPoem(data.poem)
        clearPoem()
        // clearing the search history doesn't erase starred words
        clearSearchHistory()
        swipeViewPagerLeft(1)
        checkAllStarredWords(context, data.secondSynonymForFirstRhyme)
        clearStarredWords()
        checkAllStarredWords(context)
    }

    @Test
    fun integrationTest1() {
        runIntegrationTest(scenario1)
    }

    @Test
    fun integrationTest2() {
        runIntegrationTest(scenario2)
    }

    @Test
    fun cleanLayout1Test() {
        runCleanLayoutIntegrationTest(scenario1)
    }

    @Test
    fun cleanLayout2Test() {
        runCleanLayoutIntegrationTest(scenario2)
    }

    @Test
    fun copyCleanLayoutTest() {
        val context: Context = activityTestRule.activity
        useCleanLayout()
        search("donkey")
        val wordToCopy = "swanky"
        onView(allOf(withText(wordToCopy), isDisplayed())).perform(click())
        onView(allOf(withText(endsWith(context.getString(R.string.menu_more))), isDisplayed())).perform(click())
        onView(allOf(withText(endsWith(context.getString(R.string.menu_copy))), isDisplayed())).perform(click())
        getInstrumentation().runOnMainSync { checkClipboard(context, wordToCopy) }
    }

    @Test
    fun themeTest() {
        openMenuItem(R.string.action_settings)
        clickPreference(R.string.pref_theme_title)
        onView(withText(R.string.pref_theme_value_auto)).check(matches(isChecked()))
        onView(withText(R.string.pref_theme_value_dark)).perform(click())
        pressBack()

        openMenuItem(R.string.action_settings)
        clickPreference(R.string.pref_theme_title)
        onView(withText(R.string.pref_theme_value_dark)).check(matches(isChecked()))
        onView(withText(R.string.pref_theme_value_auto)).perform(click())
        pressBack()

        openMenuItem(R.string.action_settings)
        clickPreference(R.string.pref_theme_title)
        onView(withText(R.string.pref_theme_value_auto)).check(matches(isChecked()))
        onView(withText(R.string.pref_theme_value_light)).perform(click())
        pressBack()

        openMenuItem(R.string.action_settings)
        clickPreference(R.string.pref_theme_title)
    }

    private fun useCleanLayout() {
        openMenuItem(R.string.action_settings)
        clickPreference(R.string.pref_layout_title)
        onView(withText(R.string.pref_layout_value_clean)).perform(click())
        pressBack()
    }
}
