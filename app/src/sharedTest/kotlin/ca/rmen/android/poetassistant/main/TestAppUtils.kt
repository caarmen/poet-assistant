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
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.text.TextUtils
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isFocusable
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.dictionaries.ResultListFactory
import com.google.android.material.button.MaterialButton
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalToIgnoringCase
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not

/**
 * Utility functions specific to the functionality of this app.
 */
object TestAppUtils {

    @JvmStatic
    fun clearSearchHistory() {
        // click on the settings menu item
        TestUiUtils.openMenuItem(R.string.action_settings)

        TestUiUtils.clickPreference(R.string.action_clear_search_history)

        // Tap ok on the confirmation dialog
        clickDialogPositiveButton(R.string.action_clear)

        // Exit settings
        pressBack()
    }

    @JvmStatic
    fun openSearchView() {
        // Tap on the search icon in the action bar
        onView(allOf(withId(R.id.open_search_bar_text_view), isDisplayed())).perform(click())
    }

    @JvmStatic
    fun typeQuery(query: String): ViewInteraction {
        // Type the query term and search
        getInstrumentation().waitForIdleSync()
        val searchAutoComplete = onView(allOf(withId(R.id.open_search_view_edit_text), isDisplayed()))
        searchAutoComplete.check(matches(isDisplayed()))
        searchAutoComplete.perform(typeText(query))
        getInstrumentation().waitForIdleSync()
        return searchAutoComplete
    }

    @JvmStatic
    fun search(query: String) {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        SystemClock.sleep(1000)
        openSearchView()

        // Type the query term and search
        val vi = typeQuery(query)
        getInstrumentation().waitForIdleSync()
        vi.perform(pressImeActionButton())
        getInstrumentation().waitForIdleSync()
    }

    @JvmStatic
    fun openThesaurus(context: Context, entry: String, expectedFirstSynonym: String) {
        onView(allOf(withId(R.id.btn_thesaurus),
                hasSibling(withText(entry)),
                isDisplayed()))
                .perform(click())
        CustomChecks.checkFirstSynonym(expectedFirstSynonym)
        TestUiUtils.checkTitleStripOrTab(context, R.string.tab_thesaurus)
    }

    @JvmStatic
    fun openThesaurusCleanLayout(context: Context, entry: String, expectedFirstSynonym: String) {
        onView(withText(entry)).perform(click())
        onView(withText(R.string.tab_thesaurus)).perform(click())
        CustomChecks.checkFirstSynonym(expectedFirstSynonym)
        TestUiUtils.checkTitleStripOrTab(context, R.string.tab_thesaurus)
    }

    @JvmStatic
    fun openDictionary(context: Context, entry: String, expectedFirstDefinition: String) {
        onView(allOf(withId(R.id.btn_dictionary),
                hasSibling(withText(entry)),
                isDisplayed()))
                .perform(click())
        TestUiUtils.checkTitleStripOrTab(context, R.string.tab_dictionary)
        CustomChecks.checkFirstDefinition(expectedFirstDefinition)
    }

    @JvmStatic
    fun openDictionaryCleanLayout(context: Context, entry: String, expectedFirstDefinition: String) {
        onView(withText(entry)).perform(click())
        onView(withText(R.string.tab_dictionary)).perform(click())
        TestUiUtils.checkTitleStripOrTab(context, R.string.tab_dictionary)
        CustomChecks.checkFirstDefinition(expectedFirstDefinition)
    }

    @JvmStatic
    fun starQueryWord() {
        val starIcon = onView(
                allOf(withId(R.id.btn_star_query), isDisplayed()))
        starIcon.check(matches(isNotChecked()))
        starIcon.perform(click())
        starIcon.check(matches(isChecked()))
    }

    @JvmStatic
    fun unStarQueryWord() {
        val starIcon = onView(
                allOf(withId(R.id.btn_star_query), isDisplayed()))
        starIcon.check(matches(isChecked()))
        starIcon.perform(click())
        onView(allOf(withId(R.id.btn_star_query), isDisplayed()))
                .check(matches(isNotChecked()))
    }

    @JvmStatic
    fun openFilter(expectedPrefilledFilter: String): ViewInteraction {
        getInstrumentation().waitForIdleSync()
        val vi = onView(allOf(withId(R.id.btn_filter), withContentDescription(R.string.filter_title), isDisplayed()))
        vi.check(matches(isDisplayed()))
        vi.perform(click())
        SystemClock.sleep(200)
        val result = onView(allOf(
                withId(R.id.edit),
                isDisplayed()))
                .inRoot(isFocusable())
        result.check(matches(withText(expectedPrefilledFilter)))
        return result
    }

    @JvmStatic
    fun addFilter(tab: Tab, filter: String, firstExpectedFilteredMatch: String?) {
        @IdRes val recyclerViewId = ResultListFactory.getRecyclerViewId(tab)
        val filterView = openFilter("")
        filterView.perform(typeText(filter), closeSoftKeyboard())
        clickDialogPositiveButton(android.R.string.ok)

        if (TextUtils.isEmpty(firstExpectedFilteredMatch)) {
            onView(allOf(withId(R.id.empty), hasSibling(withId(recyclerViewId)), isDisplayed()))
                    .check(matches(isDisplayed()))
        } else {
            onView(allOf(withId(R.id.empty), hasSibling(allOf(withId(recyclerViewId), isDisplayed()))))
                    .check(matches(not(isDisplayed())))
            onView(allOf(withId(R.id.text1),
                    withText(firstExpectedFilteredMatch),
                    withParent(withParent(withId(recyclerViewId))),
                    isDisplayed()))
                    .check(matches(withText(firstExpectedFilteredMatch)))
        }

    }

    @JvmStatic
    fun clearFilter(tab: Tab, firstExpectedNonFilteredMatch: String) {
        @IdRes val recyclerViewId = ResultListFactory.getRecyclerViewId(tab)
        onView(allOf(withId(R.id.btn_clear), withContentDescription(R.string.filter_clear), isDisplayed()))
                .perform(click())

        onView(allOf(withId(R.id.text1),
                withText(firstExpectedNonFilteredMatch),
                withParent(withParent(withId(recyclerViewId))),
                isDisplayed()))
                .check(matches(withText(firstExpectedNonFilteredMatch)))
    }

    @JvmStatic
    fun clearStarredWords() {
        onView(allOf(withId(R.id.btn_delete), withContentDescription(R.string.action_clear_favorites), isDisplayed())).perform(click())
        clickDialogPositiveButton(R.string.action_clear)
    }

    @JvmStatic
    fun clickDialogPositiveButton(@StringRes labelRes: Int) {
        // Top ok on the confirmation dialog
        SystemClock.sleep(200)
        onView(allOf(withId(android.R.id.button1), withText(labelRes))).perform(scrollTo(), click())
    }

    @JvmStatic
    fun typeAndSpeakPoem(poem: String) {
        typePoem(poem)
        speakPoem()
        pressBack()
        getInstrumentation().waitForIdleSync()
    }

    @JvmStatic
    fun typePoem(poem: String) {
        // The fab should be disabled until there is text
        val fab = onView(allOf(
                withId(R.id.btn_play),
                withClassName(`is`(MaterialButton::class.java.name))
        ))
        fab.check(matches(not(isEnabled())))
        val appCompatEditText = onView(
                allOf(withId(R.id.tv_text), isDisplayed()))
        appCompatEditText.check(matches(withText("")))
        appCompatEditText.perform(typeText(poem))
        appCompatEditText.check(matches(withText(equalToIgnoringCase(poem))))
        fab.check(matches(isEnabled()))
    }

    @JvmStatic
    fun speakPoem() {
        val fab = onView(allOf(withClassName(`is`(MaterialButton::class.java.name)), isEnabled()))
        fab.perform(click())
    }

    @JvmStatic
    fun clearPoem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TestUiUtils.openMenuItem(R.string.file)
            onView(allOf(withId(R.id.title), withText(R.string.file_new), isDisplayed())).perform(click())
        } else {
            TestUiUtils.openMenuItem(R.string.file_clear)
        }
        clickDialogPositiveButton(R.string.action_clear)
        onView(allOf(withId(R.id.tv_text), isDisplayed())).check(matches(withText("")))
    }

    @JvmStatic
    fun onNewIntent(activity: MainActivity, intent: Intent) {
        activity.handleNewIntent(intent)
    }
}
