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
import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isOff
import androidx.compose.ui.test.isOn
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import ca.rmen.android.poetassistant.main.common.ui.composables.ITEM_DICTIONARY_TAG
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ui.composables.THESAURUS_HEADER_FILTER_TAG
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ui.composables.THESAURUS_ITEM_ROW_TAG
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ui.composables.THESAURUS_SCREEN_CONTENT_LIST_TAG
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.common.ui.composables.ITEM_THESAURUS_TAG
import ca.rmen.android.poetassistant.main.common.ui.composables.TEXT_INPUT_DIALOG_TEXT
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.ui.composables.DICTIONARY_FAVORITE_ICON_TAG
import ca.rmen.android.poetassistant.main.dictionaries.rhymes.ui.composables.RHYMER_HEADER_FILTER_TAG
import ca.rmen.android.poetassistant.main.dictionaries.rhymes.ui.composables.RHYMER_ITEM_ROW_TAG
import ca.rmen.android.poetassistant.main.dictionaries.rhymes.ui.composables.RHYMER_SCREEN_CONTENT_LIST_TAG
import ca.rmen.android.poetassistant.main.favorites.ui.composables.CONFIRM_DELETE_DIALOG_CONFIRM_BUTTON_TAG
import ca.rmen.android.poetassistant.main.favorites.ui.composables.FAVORITES_HEADER_DELETE_ALL_TAG
import ca.rmen.android.poetassistant.main.reader.ui.composables.READER_PLAY_BUTTON_TAG
import ca.rmen.android.poetassistant.main.reader.ui.composables.READER_POEM_TEXT_FIELD_TAG
import org.hamcrest.Matchers.allOf

/**
 * Utility functions specific to the functionality of this app.
 */
object TestAppUtils {

    fun clearSearchHistory() {
        // click on the settings menu item
        TestUiUtils.openMenuItem(R.string.action_settings)

        TestUiUtils.clickPreference(R.string.action_clear_search_history)

        // Tap ok on the confirmation dialog
        clickDialogPositiveButton(R.string.action_clear)

        // Exit settings
        pressBack()
    }

    fun openSearchView() {
        // Tap on the search icon in the action bar
        onView(allOf(withId(R.id.open_search_bar_text_view), isDisplayed())).perform(click())
    }

    fun typeQuery(query: String): ViewInteraction {
        // Type the query term and search
        getInstrumentation().waitForIdleSync()
        val searchAutoComplete = onView(allOf(withId(R.id.open_search_view_edit_text), isDisplayed()))
        searchAutoComplete.check(matches(isDisplayed()))
        searchAutoComplete.perform(typeText(query))
        getInstrumentation().waitForIdleSync()
        return searchAutoComplete
    }

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

    fun openThesaurus(composeTestRule: ComposeTestRule, context: Context, entry: String, expectedFirstSynonym: String) {
        composeTestRule.onNodeWithTag("${ITEM_THESAURUS_TAG}$entry").performClick()
        CustomChecks.checkFirstSynonym(composeTestRule, expectedFirstSynonym)
        TestUiUtils.checkTitleStripOrTab(context, R.string.tab_thesaurus)
    }

    fun openThesaurusCleanLayout(composeTestRule: ComposeTestRule, context: Context, entry: String, expectedFirstSynonym: String) {
        composeTestRule.onNodeWithTag("${RHYMER_ITEM_ROW_TAG}$entry").performClick()
        val lookupLabel = context.getString(R.string.tab_thesaurus)
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText(lookupLabel).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(lookupLabel).performClick()
        CustomChecks.checkFirstSynonym(composeTestRule, expectedFirstSynonym)
        TestUiUtils.checkTitleStripOrTab(context, R.string.tab_thesaurus)
    }

    fun openDictionary(composeTestRule: ComposeTestRule, context: Context, entry: String, expectedFirstDefinition: String) {
        // The entry is in the Compose thesaurus list: click its dictionary icon.
        composeTestRule.onNodeWithTag("${ITEM_DICTIONARY_TAG}$entry").performClick()
        TestUiUtils.checkTitleStripOrTab(context, R.string.tab_dictionary)
        CustomChecks.checkFirstDefinition(composeTestRule, expectedFirstDefinition)
    }

    fun openDictionaryCleanLayout(composeTestRule: ComposeTestRule, context: Context, entry: String, expectedFirstDefinition: String) {
        // The entry is in the Compose thesaurus list: click the word, then
        // the dictionary item of the popup menu.
        composeTestRule.onNodeWithTag("${THESAURUS_ITEM_ROW_TAG}$entry").performClick()
        val lookupLabel = context.getString(R.string.tab_dictionary)
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText(lookupLabel).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(lookupLabel).performClick()
        TestUiUtils.checkTitleStripOrTab(context, R.string.tab_dictionary)
        CustomChecks.checkFirstDefinition(composeTestRule, expectedFirstDefinition)
    }

    fun starQueryWord(composeTestRule: ComposeTestRule) {
        composeTestRule.onNodeWithTag(DICTIONARY_FAVORITE_ICON_TAG).assert(isOff())
        composeTestRule.onNodeWithTag(DICTIONARY_FAVORITE_ICON_TAG).performClick()
        composeTestRule.onNodeWithTag(DICTIONARY_FAVORITE_ICON_TAG).assert(isOn())
    }
    fun unStarQueryWord(composeTestRule: ComposeTestRule, testTag: String) {
        composeTestRule.onNodeWithTag(testTag).assert(isOn())
        composeTestRule.onNodeWithTag(testTag).performClick()
        composeTestRule.onNodeWithTag(testTag).assert(isOff())
    }

    fun openFilter(composeTestRule: ComposeTestRule, expectedPrefilledFilter: String) {
        getInstrumentation().waitForIdleSync()
        composeTestRule.onNodeWithTag(RHYMER_HEADER_FILTER_TAG).performClick()
        SystemClock.sleep(200)
        composeTestRule.onNodeWithTag(TEXT_INPUT_DIALOG_TEXT).assertTextEquals(expectedPrefilledFilter)
            .assertIsFocused()
    }

    fun addFilter(composeTestRule: ComposeTestRule, context: Context, tab: Tab, filter: String, firstExpectedFilteredMatch: String?) {
        if (tab == Tab.THESAURUS) {
            addThesaurusFilter(composeTestRule, context, filter, firstExpectedFilteredMatch)
            return
        }
        addRhymerFilter(composeTestRule, context, filter, firstExpectedFilteredMatch)
    }

    /**
     * Applies a filter in the Compose thesaurus screen: opens the filter
     * dialog, types the filter, submits it, then waits for the filtered
     * results.
     */
    private fun addThesaurusFilter(composeTestRule: ComposeTestRule, context: Context, filter: String, firstExpectedFilteredMatch: String?) {
        composeTestRule.onNodeWithTag(THESAURUS_HEADER_FILTER_TAG).performClick()
        composeTestRule.onNodeWithTag(TEXT_INPUT_DIALOG_TEXT).performTextInput(filter)
        composeTestRule.onNodeWithText(context.getString(android.R.string.ok)).performClick()
        if (firstExpectedFilteredMatch == null) {
            composeTestRule.waitUntil(timeoutMillis = 5_000) {
                composeTestRule.onAllNodesWithTag(THESAURUS_SCREEN_CONTENT_LIST_TAG).fetchSemanticsNodes().isEmpty()
            }
            composeTestRule.onNodeWithTag(THESAURUS_SCREEN_CONTENT_LIST_TAG).assertDoesNotExist()
        } else {
            composeTestRule.waitUntil(timeoutMillis = 5_000) {
                composeTestRule.onAllNodesWithTag("${THESAURUS_ITEM_ROW_TAG}$firstExpectedFilteredMatch").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithTag("${THESAURUS_ITEM_ROW_TAG}$firstExpectedFilteredMatch").assertIsDisplayed()
        }
    }

    private fun addRhymerFilter(composeTestRule: ComposeTestRule, context: Context, filter: String, firstExpectedFilteredMatch: String?) {
        composeTestRule.onNodeWithTag(RHYMER_HEADER_FILTER_TAG).performClick()
        composeTestRule.onNodeWithTag(TEXT_INPUT_DIALOG_TEXT).performTextInput(filter)
        composeTestRule.onNodeWithText(context.getString(android.R.string.ok)).performClick()
        if (firstExpectedFilteredMatch == null) {
            composeTestRule.waitUntil(timeoutMillis = 5_000) {
                composeTestRule.onAllNodesWithTag(RHYMER_SCREEN_CONTENT_LIST_TAG).fetchSemanticsNodes().isEmpty()
            }
            composeTestRule.onNodeWithTag(RHYMER_SCREEN_CONTENT_LIST_TAG).assertDoesNotExist()
        } else {
            composeTestRule.waitUntil(timeoutMillis = 5_000) {
                composeTestRule.onAllNodesWithTag("${RHYMER_ITEM_ROW_TAG}$firstExpectedFilteredMatch").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithTag("${RHYMER_ITEM_ROW_TAG}$firstExpectedFilteredMatch").assertIsDisplayed()
        }
    }

    fun clearFilter(composeTestRule: ComposeTestRule, context: Context, tab: Tab, firstExpectedNonFilteredMatch: String) {
        if (tab == Tab.THESAURUS) {
            clearThesaurusFilter(composeTestRule, context, firstExpectedNonFilteredMatch)
            return
        }
        clearRhymerFilter(composeTestRule, context, firstExpectedNonFilteredMatch)
    }

    /**
     * Clears the filter in the Compose rhymer screen, then waits for the
     * unfiltered results.
     */
    private fun clearRhymerFilter(composeTestRule: ComposeTestRule, context: Context, firstExpectedNonFilteredMatch: String) {
        composeTestRule.onNode(hasContentDescription(context.getString(R.string.filter_clear))).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag("${RHYMER_ITEM_ROW_TAG}$firstExpectedNonFilteredMatch").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("${RHYMER_ITEM_ROW_TAG}$firstExpectedNonFilteredMatch").assertIsDisplayed()
    }

    /**
     * Clears the filter in the Compose thesaurus screen, then waits for the
     * unfiltered results.
     */
    private fun clearThesaurusFilter(composeTestRule: ComposeTestRule, context: Context, firstExpectedNonFilteredMatch: String) {
        composeTestRule.onNode(hasContentDescription(context.getString(R.string.filter_clear))).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag("${THESAURUS_ITEM_ROW_TAG}$firstExpectedNonFilteredMatch").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("${THESAURUS_ITEM_ROW_TAG}$firstExpectedNonFilteredMatch").assertIsDisplayed()
    }

    fun clearStarredWords(composeTestRule: ComposeTestRule) {
        composeTestRule.onNodeWithTag(FAVORITES_HEADER_DELETE_ALL_TAG).performClick()
        SystemClock.sleep(200)
        composeTestRule.onNodeWithTag(CONFIRM_DELETE_DIALOG_CONFIRM_BUTTON_TAG).performClick()
    }

    fun clickDialogPositiveButton(@StringRes labelRes: Int) {
        // Top ok on the confirmation dialog
        SystemClock.sleep(200)
        onView(allOf(withId(android.R.id.button1), withText(labelRes))).perform(scrollTo(), click())
    }

    fun typeAndSpeakPoem(composeTestRule: ComposeTestRule, poem: String) {
        typePoem(composeTestRule, poem)
        speakPoem(composeTestRule)
        pressBack()
        getInstrumentation().waitForIdleSync()
    }

    fun typePoem(composeTestRule: ComposeTestRule, poem: String) {
        // The fab should be disabled until there is text
        val playButton = composeTestRule.onNodeWithTag(READER_PLAY_BUTTON_TAG)
        val poemText = composeTestRule.onNodeWithTag(READER_POEM_TEXT_FIELD_TAG)
        playButton.assertIsNotEnabled()
        poemText.assertIsDisplayed()
            // Check that the text is empty.
            // A simple assertTextEquals("") fails because the text has a placeholder.
            .assert(
                SemanticsMatcher.expectValue(
                SemanticsProperties.EditableText, AnnotatedString("")))
            .performTextInput(poem)
        poemText.assertTextContains(poem, ignoreCase=true)
        playButton.assertIsEnabled()
    }

    fun speakPoem(composeTestRule: ComposeTestRule) {
        composeTestRule.onNodeWithTag(READER_PLAY_BUTTON_TAG).assertIsEnabled().performClick()
    }

    fun clearPoem(composeTestRule: ComposeTestRule) {
        TestUiUtils.openMenuItem(R.string.file)
        onView(allOf(withId(R.id.title), withText(R.string.file_new), isDisplayed())).perform(click())
        clickDialogPositiveButton(R.string.action_clear)
        composeTestRule.onNodeWithTag(READER_POEM_TEXT_FIELD_TAG).assertIsDisplayed()
            // Check that the text is empty.
            // A simple assertTextEquals("") fails because the text has a placeholder.
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.EditableText, AnnotatedString("")))
    }

    fun onNewIntent(activity: MainActivity, intent: Intent) {
        activity.handleNewIntent(intent)
    }
}
