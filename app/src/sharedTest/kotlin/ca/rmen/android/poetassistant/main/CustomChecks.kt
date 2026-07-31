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

import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.view.WindowManager
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingRootException
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.CustomViewMatchers.childAtPosition
import ca.rmen.android.poetassistant.main.CustomViewMatchers.withChildCount
import ca.rmen.android.poetassistant.main.dictionaries.ResultListAdapter
import org.fest.reflect.core.Reflection
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

object CustomChecks {

    @JvmStatic
    fun checkRhymes(context: Context, firstRhyme: String, secondRhyme: String) {
        // Make sure we're in the rhymer tab
        TestUiUtils.checkTitleStripOrTab(context, R.string.tab_rhymer)

        val firstRhymeWord = onView(
                allOf(withId(R.id.text1), withText(firstRhyme),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.rhymer_recycler_view),
                                        1),
                                1),
                        isDisplayed()))
        firstRhymeWord.check(matches(withText(firstRhyme)))

        val secondRhymeWord = onView(
                allOf(withId(R.id.text1), withText(secondRhyme),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.rhymer_recycler_view),
                                        2),
                                1),
                        isDisplayed()))
        secondRhymeWord.check(matches(withText(secondRhyme)))
    }

    @JvmStatic
    fun checkRhyme(expectedRhyme: String) {
        // Scroll to the item in case it's not visible
        onView(allOf(withId(R.id.rhymer_recycler_view), isDisplayed()))
                .perform(scrollTo<ResultListAdapter.ResultListEntryViewHolder>(hasDescendant(withText(expectedRhyme))))
    }

    @JvmStatic
    fun checkPatterns(context: Context, query: String, vararg patterns: String) {
        checkTitleStripOrTab(context, R.string.tab_pattern)
        val emptyViewMatch: Matcher<View> = allOf(withId(R.id.empty), withText(context.getString(R.string.empty_pattern_list_with_query, query)))
        val emptyView = onView(emptyViewMatch)
        val recyclerViewMatch: Matcher<View> = allOf(withId(R.id.pattern_recycler_view), hasSibling(emptyViewMatch))
        if (patterns.isNotEmpty()) {
            emptyView.check(matches(not(isDisplayed())))
            onView(recyclerViewMatch).check(matches(withChildCount(patterns.size)))
            for (i in patterns.indices) {
                onView(allOf(withId(R.id.text1), withText(patterns[i]),
                        childAtPosition(childAtPosition(recyclerViewMatch, i), 1),
                        isDisplayed()))
                        .check(matches(withText(patterns[i])))
            }
        } else {
            emptyView.check(matches(isDisplayed()))
        }
    }

    @JvmStatic
    fun checkStarredInList(entry: String) {
        val star = onView(
                allOf(withId(R.id.btn_star_result),
                        childAtPosition(
                                withChild(withText(entry)),
                                0),
                        isDisplayed()))
        star.check(matches(isChecked()))
    }

    @JvmStatic
    fun checkAllStarredWords(context: Context, vararg expectedStarredWords: String) {
        checkTitleStripOrTab(context, R.string.tab_favorites)
        val emptyViewMatch: Matcher<View> = allOf(withId(R.id.empty), withText(R.string.empty_favorites_list))
        val emptyView = onView(emptyViewMatch)
        if (expectedStarredWords == null || expectedStarredWords.isEmpty()) {
            emptyView.check(matches(isCompletelyDisplayed()))
        } else {
            emptyView.check(matches(not(isDisplayed())))
            val recyclerViewMatch: Matcher<View> = allOf(withId(R.id.favorites_recycler_view), hasSibling(emptyViewMatch))
            onView(recyclerViewMatch).check(matches(withChildCount(expectedStarredWords.size)))
            for (word in expectedStarredWords) {
                onView(allOf(withId(R.id.text1), withParent(withParent(recyclerViewMatch)), withText(word))).check(matches(isDisplayed()))
            }
        }
    }

    @JvmStatic
    fun checkSingleRootView(context: Context) {
        SystemClock.sleep(500)
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val impl = Reflection.field("mWindowManager").ofType(Any::class.java).`in`(windowManager).get()
            val views = Reflection.field("mViews").ofType(Array<View>::class.java).`in`(impl).get() as Array<View>
            assertEquals(1, views.size)
        } else {
            val impl = Reflection.field("mGlobal").ofType(Any::class.java).`in`(windowManager).get()
            val views = Reflection.field("mViews").ofType(List::class.java).`in`(impl).get() as List<*>
            assertEquals(1, views.size)
        }
    }

    @JvmStatic
    fun checkSearchSuggestions(vararg suggestions: String) {
        SystemClock.sleep(1000)
        Espresso.onIdle()
        val searchListMatcher: Matcher<View> = withId(R.id.search_suggestions_list)
        try {
            val searchSuggestionsList = onView(searchListMatcher)
            searchSuggestionsList.check(matches(withChildCount(suggestions.size)))
            for (i in suggestions.indices) {
                onView(allOf(withId(android.R.id.text1), withParent(childAtPosition(searchListMatcher, i))))
                        .check(matches(withText(suggestions[i])))
            }
        } catch (e: NoMatchingRootException) {
            if (suggestions.isEmpty()) {
                // this is correct
                return
            } else {
                throw e
            }
        }
        if (suggestions.isEmpty()) {
            fail("Found search suggestions but didn't expect to")
        }
    }

    @JvmStatic
    fun checkClipboard(context: Context, clipboardContent: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        assertNotNull(clipboard)
        assertTrue("Expected to find $clipboardContent in the clipboard", clipboard.hasPrimaryClip())
        val primaryClip = clipboard.primaryClip
        assertNotNull(primaryClip)
        val item = primaryClip!!.getItemAt(primaryClip.itemCount - 1)
        assertNotNull(item)
        assertEquals(clipboardContent, item.text)
    }

    @JvmStatic
    fun checkFirstDefinition(expectedFirstDefinition: String) {
        val firstDefinition = onView(
                allOf(withId(R.id.definition), withText(expectedFirstDefinition),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.dictionary_recycler_view),
                                        0),
                                1),
                        isDisplayed()))
        firstDefinition.check(matches(withText(expectedFirstDefinition)))
    }

    @JvmStatic
    fun checkFirstSynonym(expectedFirstSynonym: String) {
        val firstSynonymWord = onView(
                allOf(withId(R.id.text1), withText(expectedFirstSynonym),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.thesaurus_recycler_view),
                                        2),
                                1),
                        isDisplayed()))
        firstSynonymWord.check(matches(withText(expectedFirstSynonym)))
    }

    @JvmStatic
    fun checkSynonym(expectedSynonym: String) {
        // Scroll to the item in case it's not visible
        onView(allOf(withId(R.id.thesaurus_recycler_view), isDisplayed()))
                .perform(scrollTo<ResultListAdapter.ResultListEntryViewHolder>(withChild(withText(expectedSynonym))))
    }
}
