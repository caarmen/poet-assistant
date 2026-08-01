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

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.CustomChecks.checkFirstDefinition
import ca.rmen.android.poetassistant.main.CustomChecks.checkFirstSynonym
import ca.rmen.android.poetassistant.main.CustomChecks.checkRhymes
import ca.rmen.android.poetassistant.main.MainActivity
import ca.rmen.android.poetassistant.main.TestAppUtils
import ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab
import ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft
import ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerRight
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@LargeTest
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(AndroidJUnit4::class)
class IntentTest {

    @get:Rule(order = 0)
    val hiltTestRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> =
        PoetAssistantActivityTestRule(MainActivity::class.java, false)

    @Test
    fun onNewIntentQueryTest() {
        val activity: MainActivity = activityTestRule.launchActivity(Intent())
        launchNewIntent(Intent.ACTION_SEARCH, SearchManager.QUERY, "muffin")
        checkTitleStripOrTab(activity, R.string.tab_rhymer)
        checkRhymes(activity, "mcguffin", "toughen")
        swipeViewPagerLeft(1)
        checkFirstSynonym("quick bread")
        swipeViewPagerLeft(1)
        checkFirstDefinition("a sweet quick bread baked in a cup-shaped pan")
    }

    @Test
    fun onNewIntentUserQueryTest() {
        val activity: MainActivity = activityTestRule.launchActivity(Intent())
        launchNewIntent(Intent.ACTION_SEARCH, SearchManager.USER_QUERY, "muffin")
        checkTitleStripOrTab(activity, R.string.tab_rhymer)
        checkRhymes(activity, "mcguffin", "toughen")
        swipeViewPagerLeft(1)
        checkFirstSynonym("quick bread")
        swipeViewPagerLeft(1)
        checkFirstDefinition("a sweet quick bread baked in a cup-shaped pan")
    }

    @Test
    fun onNewIntentViewQueryTest() {
        val activity: MainActivity = activityTestRule.launchActivity(Intent())
        launchNewIntent(Intent.ACTION_VIEW, "poetassistant://query/muffin")
        checkTitleStripOrTab(activity, R.string.tab_dictionary)
        checkFirstDefinition("a sweet quick bread baked in a cup-shaped pan")
        swipeViewPagerRight(1)
        checkFirstSynonym("quick bread")
        swipeViewPagerRight(1)
        checkRhymes(activity, "mcguffin", "toughen")
    }

    @Test
    fun onNewIntentViewRhymerTest() {
        activityTestRule.launchActivity(Intent())
        launchNewIntent(Intent.ACTION_VIEW, "poetassistant://rhymer/muffin")
        checkRhymerOnly("mcguffin", "toughen")
    }

    @Test
    fun onNewIntentViewThesaurusTest() {
        activityTestRule.launchActivity(Intent())
        launchNewIntent(Intent.ACTION_VIEW, "poetassistant://thesaurus/muffin")
        checkThesaurusOnly("quick bread")
    }

    @Test
    fun onNewIntentViewDictionaryTest() {
        activityTestRule.launchActivity(Intent())
        launchNewIntent(Intent.ACTION_VIEW, "poetassistant://dictionary/muffin")
        checkDictionaryOnly("a sweet quick bread baked in a cup-shaped pan")
    }

    @Test
    fun onNewIntentSendTest() {
        val activity: MainActivity = activityTestRule.launchActivity(Intent())
        val poemText = "Dare to be honest and fear no labor." // Robert Burns
        launchNewIntent(Intent.ACTION_SEND, Intent.EXTRA_TEXT, poemText)
        checkTitleStripOrTab(activity, R.string.tab_reader)
        onView(allOf(withId(R.id.tv_text), isDisplayed()))
            .check(matches(withText(poemText)))
    }

    @Test
    fun onCreateSendTest() {
        val intent = Intent(Intent.ACTION_SEND)
        val poemText = "Dare to be honest and fear no labor." // Robert Burns
        intent.putExtra(Intent.EXTRA_TEXT, poemText)
        val activity: MainActivity = activityTestRule.launchActivity(intent)
        checkTitleStripOrTab(activity, R.string.tab_reader)
        onView(allOf(withId(R.id.tv_text), isDisplayed()))
            .check(matches(withText(poemText)))
    }

    @Test
    fun onCreateViewQueryTest() {
        val intent = Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse("poetassistant://query/muffin"))
        val activity: MainActivity = activityTestRule.launchActivity(intent)
        checkTitleStripOrTab(activity, R.string.tab_dictionary)
        checkFirstDefinition("a sweet quick bread baked in a cup-shaped pan")
        swipeViewPagerRight(1)
        checkFirstSynonym("quick bread")
        swipeViewPagerRight(1)
        checkRhymes(activity, "mcguffin", "toughen")
    }

    @Test
    fun onCreateViewRhymerTest() {
        val intent = Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse("poetassistant://rhymer/muffin"))
        activityTestRule.launchActivity(intent)
        checkRhymerOnly("mcguffin", "toughen")
    }

    @Test
    @Config(qualifiers = "w360dp-h640dp")
    fun onCreateViewThesaurusTest() {
        val intent = Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse("poetassistant://thesaurus/muffin"))
        activityTestRule.launchActivity(intent)
        checkThesaurusOnly("quick bread")
    }

    @Test
    fun onCreateViewDictionaryTest() {
        val intent = Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse("poetassistant://dictionary/muffin"))
        activityTestRule.launchActivity(intent)
        checkDictionaryOnly("a sweet quick bread baked in a cup-shaped pan")
    }

    private fun launchNewIntent(action: String, extraKey: String, extraValue: String) {
        val intent = Intent(action)
        intent.putExtra(extraKey, extraValue)
        getInstrumentation().runOnMainSync {
            TestAppUtils.onNewIntent(activityTestRule.activity, intent)
        }
    }

    private fun launchNewIntent(action: String, data: String) {
        val intent = Intent(action)
        intent.setData(Uri.parse(data))
        getInstrumentation().runOnMainSync {
            TestAppUtils.onNewIntent(activityTestRule.activity, intent)
        }
    }

    private fun checkRhymerOnly(expectedRhyme1: String, expectedRhyme2: String) {
        val activity: Activity = activityTestRule.activity
        checkTitleStripOrTab(activity, R.string.tab_rhymer)
        checkRhymes(activity, expectedRhyme1, expectedRhyme2)
        swipeViewPagerLeft(1)
        onView(allOf(withId(R.id.empty), isDisplayed(), withText(R.string.empty_list_without_query)))
            .check(matches(isDisplayed()))
        swipeViewPagerLeft(1)
        onView(allOf(withId(R.id.empty), isDisplayed(), withText(R.string.empty_list_without_query)))
            .check(matches(isDisplayed()))
    }

    private fun checkThesaurusOnly(expectedFirstSynonym: String) {
        val activity: Activity = activityTestRule.activity
        checkTitleStripOrTab(activity, R.string.tab_thesaurus)
        checkFirstSynonym(expectedFirstSynonym)
        swipeViewPagerLeft(1)
        onView(allOf(withId(R.id.empty), isDisplayed(), withText(R.string.empty_list_without_query)))
            .check(matches(isDisplayed()))
        swipeViewPagerRight(2)
        onView(allOf(withId(R.id.empty), isDisplayed(), withText(R.string.empty_list_without_query)))
            .check(matches(isDisplayed()))
    }

    private fun checkDictionaryOnly(expectedFirstDefinition: String) {
        val activity: Activity = activityTestRule.activity
        checkTitleStripOrTab(activity, R.string.tab_dictionary)
        checkFirstDefinition(expectedFirstDefinition)
        swipeViewPagerRight(1)
        onView(allOf(withId(R.id.empty), isDisplayed(), withText(R.string.empty_list_without_query)))
            .check(matches(isDisplayed()))
        swipeViewPagerRight(1)
        onView(allOf(withId(R.id.empty), isDisplayed(), withText(R.string.empty_list_without_query)))
            .check(matches(isDisplayed()))
    }
}
