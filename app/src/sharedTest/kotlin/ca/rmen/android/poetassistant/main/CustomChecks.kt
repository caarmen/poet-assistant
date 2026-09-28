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
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ui.composables.THESAURUS_ITEM_ROW_TAG
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ui.composables.THESAURUS_SCREEN_CONTENT_LIST_TAG
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ui.composables.THESAURUS_ITEM_STAR_TAG
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingRootException
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.CustomViewMatchers.atPosition
import ca.rmen.android.poetassistant.main.CustomViewMatchers.childAtPosition
import ca.rmen.android.poetassistant.main.CustomViewMatchers.withChildCount
import ca.rmen.android.poetassistant.main.dictionaries.ResultListAdapter
import org.fest.reflect.core.Reflection
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.ui.composables.DICTIONARY_ITEM_ROW_TAG
import ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables.PATTERNS_SCREEN_CONTENT_EMPTY_TAG
import ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables.PATTERNS_SCREEN_CONTENT_LIST_TAG
import ca.rmen.android.poetassistant.main.favorites.ui.composables.FAVORITES_SCREEN_CONTENT_EMPTY_TAG
import ca.rmen.android.poetassistant.main.favorites.ui.composables.FAVORITES_SCREEN_CONTENT_LIST_TAG
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

object CustomChecks {

    fun checkRhymes(context: Context, firstRhyme: String, secondRhyme: String) {
        // Make sure we're in the rhymer tab
        TestUiUtils.checkTitleStripOrTab(context, R.string.tab_rhymer)
        // Assert the first item (index 1, since index 0 is the "Strict rhyme matches:" header) has firstRhyme
        onView(withId(R.id.rhymer_recycler_view))
            .check(matches(atPosition(1, hasDescendant(withText(firstRhyme)))))

        // Assert the second item (index 2) has secondRhyme
        onView(withId(R.id.rhymer_recycler_view))
            .check(matches(atPosition(2, hasDescendant(withText(secondRhyme)))))

    }

    fun checkRhyme(expectedRhyme: String) {
        // Scroll to the item in case it's not visible
        onView(allOf(withId(R.id.rhymer_recycler_view), isDisplayed()))
                .perform(scrollTo<ResultListAdapter.ResultListEntryViewHolder>(hasDescendant(withText(expectedRhyme))))
    }

    fun checkPatterns(context: Context, composeTestRule: ComposeTestRule, query: String, vararg patterns: String) {
        checkTitleStripOrTab(context, R.string.tab_pattern)
        val emptyNode = composeTestRule.onNodeWithTag(PATTERNS_SCREEN_CONTENT_EMPTY_TAG)
        val listNode = composeTestRule.onNodeWithTag(PATTERNS_SCREEN_CONTENT_LIST_TAG)
        if (patterns.isNotEmpty()) {
            emptyNode.assertDoesNotExist()
            listNode.assertIsDisplayed()
            for (word in patterns) {
                composeTestRule.onNode(hasText(word) and hasAnyAncestor(hasTestTag(PATTERNS_SCREEN_CONTENT_LIST_TAG)), useUnmergedTree = true)
                    .assertIsDisplayed()
            }
        } else {
            emptyNode.assertIsDisplayed()
            listNode.assertDoesNotExist()
        }
    }

    fun checkStarredInList(composeTestRule: ComposeTestRule, entry: String) {
        composeTestRule
            .onNodeWithTag("${THESAURUS_ITEM_STAR_TAG}$entry")
            .assertIsOn()
    }

    fun checkAllStarredWords(context: Context, composeTestRule: ComposeTestRule, vararg expectedStarredWords: String) {
        checkTitleStripOrTab(context, R.string.tab_favorites)
        val emptyNode = composeTestRule.onNodeWithTag(FAVORITES_SCREEN_CONTENT_EMPTY_TAG)
        val listNode = composeTestRule.onNodeWithTag(FAVORITES_SCREEN_CONTENT_LIST_TAG)
        if (expectedStarredWords.isEmpty()) {
            emptyNode.assertIsDisplayed()
            listNode.assertDoesNotExist()
        } else {
            emptyNode.assertDoesNotExist()
            listNode.assertIsDisplayed()
            listNode.onChildren().assertCountEquals(expectedStarredWords.size)
            for (word in expectedStarredWords) {
                composeTestRule.onNode(hasText(word) and hasAnyAncestor(hasTestTag(FAVORITES_SCREEN_CONTENT_LIST_TAG)), useUnmergedTree = true)
                    .assertIsDisplayed()
            }
        }
    }

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

    fun checkFirstDefinition(composeTestRule: ComposeTestRule, expectedFirstDefinition: String) {
        composeTestRule
            .onNode(hasTestTag("${DICTIONARY_ITEM_ROW_TAG}0") and hasAnyDescendant(hasText(expectedFirstDefinition)))
            .assertExists()
    }

    fun checkFirstSynonym(composeTestRule: ComposeTestRule, expectedFirstSynonym: String) {
        // The synonym may be below the visible area of the lazy list: scroll to it.
        composeTestRule
            .onNodeWithTag(THESAURUS_SCREEN_CONTENT_LIST_TAG)
            .performScrollToNode(hasTestTag("${THESAURUS_ITEM_ROW_TAG}$expectedFirstSynonym"))
        composeTestRule
            .onNodeWithTag("${THESAURUS_ITEM_ROW_TAG}$expectedFirstSynonym")
            .assertIsDisplayed()
        // Wait for the list scroll to settle: a pager swipe during the
        // scroll animation can desynchronize the pager title strip.
        composeTestRule.waitForIdle()
    }

    /**
     * Checks that the synonym is in the thesaurus result list.
     */
    fun checkSynonym(composeTestRule: ComposeTestRule, expectedSynonym: String) {
        checkFirstSynonym(composeTestRule, expectedSynonym)
    }

    fun hasNonEmptyText(): SemanticsMatcher = SemanticsMatcher("has non-empty text") { node ->
        node.config.getOrNull(SemanticsProperties.Text)
            ?.any { it.text.isNotBlank() } == true
    }
}
