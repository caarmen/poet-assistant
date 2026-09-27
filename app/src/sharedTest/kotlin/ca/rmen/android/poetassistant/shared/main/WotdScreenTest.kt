/*
 * Copyright (c) 2016-present Carmen Alvarez
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

import android.content.ClipboardManager
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.common.ui.composables.ITEM_DICTIONARY_TAG
import ca.rmen.android.poetassistant.main.common.ui.composables.ITEM_RHYMER_TAG
import ca.rmen.android.poetassistant.main.common.ui.composables.ITEM_THESAURUS_TAG
import ca.rmen.android.poetassistant.main.common.usecases.GetProcessTextMenuItemsUseCase
import ca.rmen.android.poetassistant.main.common.usecases.OpenExternalAppUseCase
import ca.rmen.android.poetassistant.main.common.usecases.ShareUseCase
import ca.rmen.android.poetassistant.main.favorites.data.FavoritesRepository
import ca.rmen.android.poetassistant.main.wotd.WotdHistoryItem
import ca.rmen.android.poetassistant.main.wotd.WotdScreenViewModel
import ca.rmen.android.poetassistant.main.wotd.ui.composables.WOTD_ITEM_ROW_TAG
import ca.rmen.android.poetassistant.main.wotd.ui.composables.WOTD_ITEM_STAR_TAG
import ca.rmen.android.poetassistant.main.wotd.ui.composables.WOTD_SCREEN_CONTENT_EMPTY_TAG
import ca.rmen.android.poetassistant.main.wotd.ui.composables.WOTD_SCREEN_CONTENT_LIST_TAG
import ca.rmen.android.poetassistant.main.wotd.ui.composables.WotdScreen
import ca.rmen.android.poetassistant.main.wotd.usecases.CreateWotdHistoryShareUseCase
import ca.rmen.android.poetassistant.main.wotd.usecases.GetWotdHistoryUseCase
import ca.rmen.android.poetassistant.rules.PoetAssistantComposeTestRule
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.settings.SettingsRepository
import ca.rmen.android.poetassistant.theme.AppTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(AndroidJUnit4::class)
class WotdScreenTest {
    @get:Rule(order = 0)
    val hiltTestRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @get:Rule(order = 2)
    val poetAssistantComposeTestRule = PoetAssistantComposeTestRule(composeTestRule)

    @Inject
    lateinit var favoritesRepository: FavoritesRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var createWotdHistoryShareUseCase: CreateWotdHistoryShareUseCase

    @Inject
    lateinit var getProcessTextMenuItemsUseCase: GetProcessTextMenuItemsUseCase

    @Inject
    lateinit var shareUseCase: ShareUseCase

    @Inject
    lateinit var openExternalAppUseCase: OpenExternalAppUseCase

    private lateinit var context: Context

    // The history use case is faked: the test controls the words and dates.
    // This checks the screen behavior only. A separate use case test checks the algorithm.
    private val getStaticWotdHistoryUseCase = GetStaticWotdHistoryUseCase()

    private lateinit var viewModel: WotdScreenViewModel

    @Before
    fun setUp() {
        hiltTestRule.inject()
        context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel = WotdScreenViewModel(
            settingsRepository = settingsRepository,
            favoritesRepository = favoritesRepository,
            getWotdHistoryUseCase = getStaticWotdHistoryUseCase,
            createWotdHistoryShareUseCase = createWotdHistoryShareUseCase,
            getProcessTextMenuItemsUseCase = getProcessTextMenuItemsUseCase,
        )
    }

    /**
     * Given a history
     * When the history is displayed,
     * Then each entry shows its word and date.
     */
    @Test
    fun testHistoryContent() {
        // Given a history
        getStaticWotdHistoryUseCase.entries = HISTORY
        setContent()

        // Then each entry shows its word and date.
        waitUntilTagExists(WOTD_SCREEN_CONTENT_LIST_TAG)
        HISTORY.forEach { entry ->
            composeTestRule.onNodeWithTag(rowTag(entry), useUnmergedTree = true).assertIsDisplayed()
        }
    }

    /**
     * Given an empty history (for example, the dictionary db is not loaded yet)
     * When the history is displayed,
     * Then the empty state is displayed and no list is displayed.
     */
    @Test
    fun testEmptyHistory() {
        // Given an empty history
        getStaticWotdHistoryUseCase.entries = emptyList()
        setContent()

        // Then the empty state is displayed and no list is displayed.
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(WOTD_SCREEN_CONTENT_EMPTY_TAG).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag(WOTD_SCREEN_CONTENT_EMPTY_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(WOTD_SCREEN_CONTENT_LIST_TAG).assertDoesNotExist()
    }

    /**
     * Given a history entry
     * When the entry is clicked,
     * And the copy item is selected on that entry,
     * Then the word is copied to the clipboard
     * And the expected snackbar text is emitted.
     */
    @Test
    fun testCopy() {
        // Given a history
        getStaticWotdHistoryUseCase.entries = HISTORY
        val expectedWord = HISTORY[0].word
        runBlocking { settingsRepository.setLayout(Layout.EFFICIENT) }
        var snackbarText: String? = null
        setContent(onSnackbarText = { snackbarText = it })

        // When the first entry is clicked,
        waitUntilTagExists(WOTD_SCREEN_CONTENT_LIST_TAG)
        composeTestRule.onNodeWithTag(WOTD_SCREEN_CONTENT_LIST_TAG).onChildren().onFirst()
            .performClick()

        // And the copy item is selected
        val copyLabel = context.getString(R.string.menu_copy)
        waitUntilTextExists(copyLabel)
        composeTestRule.onNodeWithText(copyLabel).assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        // Then the word is copied to the clipboard
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clippedText = clipboardManager.primaryClip
            ?.getItemAt(0)
            ?.coerceToText(context)
            ?.toString()
        assertEquals(expectedWord, clippedText)

        // And the expected snackbar text is emitted
        assertEquals(context.getString(R.string.snackbar_copied_text), snackbarText)
    }

    enum class CleanLookupScenario(@StringRes val lookupLabel: Int, val expectedTab: Tab) {
        RHYMER(R.string.tab_rhymer, Tab.RHYMER),
        THESAURUS(R.string.tab_thesaurus, Tab.THESAURUS),
        DICTIONARY(R.string.tab_dictionary, Tab.DICTIONARY),
    }

    @Test
    fun testLookupCleanRhymer() = testCleanLookup(CleanLookupScenario.RHYMER)

    @Test
    fun testLookupCleanThesaurus() = testCleanLookup(CleanLookupScenario.THESAURUS)

    @Test
    fun testLookupCleanDictionary() = testCleanLookup(CleanLookupScenario.DICTIONARY)

    /**
     * Given the clean layout
     * And a history,
     * When a word of the day is clicked,
     * And a R/T/D lookup is chosen,
     * Then the expected word and tab are forwarded to the onSearchInTab callback.
     */
    private fun testCleanLookup(scenario: CleanLookupScenario) {
        // Given the clean layout and a history
        getStaticWotdHistoryUseCase.entries = HISTORY
        val expectedWord = HISTORY[0].word
        runBlocking { settingsRepository.setLayout(Layout.CLEAN) }
        var selectedWord: String? = null
        var selectedTab: Tab? = null
        setContent(onSearchInTab = { word, tab ->
            selectedWord = word
            selectedTab = tab
        })

        // When the first entry is clicked,
        waitUntilTagExists(WOTD_SCREEN_CONTENT_LIST_TAG)
        composeTestRule.onNodeWithTag(WOTD_SCREEN_CONTENT_LIST_TAG).onChildren().onFirst()
            .performClick()

        // And a R/T/D lookup is chosen
        val lookupLabel = context.getString(scenario.lookupLabel)
        waitUntilTextExists(lookupLabel)
        composeTestRule.onNodeWithText(lookupLabel).assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        // Then the expected word and tab are forwarded to the onSearchInTab callback
        assertEquals(scenario.expectedTab, selectedTab)
        assertEquals(expectedWord, selectedWord)
    }

    enum class EfficientLookupScenario(val testTagPrefix: String, val expectedTab: Tab) {
        RHYMER(ITEM_RHYMER_TAG, Tab.RHYMER),
        THESAURUS(ITEM_THESAURUS_TAG, Tab.THESAURUS),
        DICTIONARY(ITEM_DICTIONARY_TAG, Tab.DICTIONARY),
    }

    @Test
    fun testLookupEfficientRhymer() = testEfficientLookup(EfficientLookupScenario.RHYMER)

    @Test
    fun testLookupEfficientThesaurus() = testEfficientLookup(EfficientLookupScenario.THESAURUS)

    @Test
    fun testLookupEfficientDictionary() = testEfficientLookup(EfficientLookupScenario.DICTIONARY)

    /**
     * Given the efficient layout
     * And a history,
     * When an R/T/D icon in an entry is clicked,
     * Then the expected word and tab are forwarded to the onSearchInTab callback.
     */
    private fun testEfficientLookup(scenario: EfficientLookupScenario) {
        // Given the efficient layout and a history
        getStaticWotdHistoryUseCase.entries = HISTORY
        val expectedWord = HISTORY[0].word
        runBlocking { settingsRepository.setLayout(Layout.EFFICIENT) }
        var selectedWord: String? = null
        var selectedTab: Tab? = null
        setContent(onSearchInTab = { word, tab ->
            selectedWord = word
            selectedTab = tab
        })

        // When the R/T/D icon of the first entry is clicked.
        // The same word appears on several days, so several nodes can have this tag: take the first.
        waitUntilTagExists("${scenario.testTagPrefix}$expectedWord")
        composeTestRule.onAllNodesWithTag("${scenario.testTagPrefix}$expectedWord").onFirst()
            .assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        // Then the expected word and tab are forwarded to the onSearchInTab callback
        assertEquals(scenario.expectedTab, selectedTab)
        assertEquals(expectedWord, selectedWord)
    }

    /**
     * Given a history,
     * When the star icon of an entry is clicked,
     * Then the word of that entry is added to the favorites,
     * And the star icons of the entries with other words are not toggled,
     * And when the star icon is clicked again, the word is removed from the favorites.
     */
    @Test
    fun testFavorites() {
        // Given a history
        getStaticWotdHistoryUseCase.entries = HISTORY
        val starredEntry = HISTORY[0]
        runBlocking { settingsRepository.setLayout(Layout.EFFICIENT) }
        setContent()

        // When the star icon of the first entry is clicked,
        waitUntilTagExists(WOTD_SCREEN_CONTENT_LIST_TAG)
        val starredEntryStarTag = starTag(starredEntry)
        composeTestRule.onNodeWithTag(starredEntryStarTag).assertIsOff().performClick()
        composeTestRule.waitForIdle()

        // Then the word of that entry is added to the favorites,
        runBlocking { assertEquals(setOf(starredEntry.word), favoritesRepository.getFavorites()) }
        // And the star icon of that entry is toggled on.
        composeTestRule.onNodeWithTag(starredEntryStarTag).assertIsOn()

        // And the star icon of the other entry with the same word is toggled on.
        val sameWordEntry = HISTORY[2]
        assertEquals(starredEntry.word, sameWordEntry.word)
        composeTestRule.onNodeWithTag(starTag(sameWordEntry)).assertIsOn()

        // And the star icon of an entry with another word is not toggled.
        composeTestRule.onNodeWithTag(starTag(HISTORY[1])).assertIsOff()
        composeTestRule.onNodeWithTag(starTag(HISTORY[3])).assertIsOff()

        // When the star icon is clicked again,
        composeTestRule.onNodeWithTag(starredEntryStarTag).performClick()
        composeTestRule.waitForIdle()

        // Then the word is removed from the favorites,
        runBlocking { assertTrue(favoritesRepository.getFavorites().isEmpty()) }
        // And the star icon is toggled off, for both entries with that word.
        composeTestRule.onNodeWithTag(starredEntryStarTag).assertIsOff()
        composeTestRule.onNodeWithTag(starTag(sameWordEntry)).assertIsOff()
    }

    private fun setContent(
        onSearchInTab: (String, Tab) -> Unit = { _, _ -> },
        onSnackbarText: (String) -> Unit = { },
    ) {
        composeTestRule.setContent {
            AppTheme {
                WotdScreen(
                    viewModel = viewModel,
                    shareUseCase = shareUseCase,
                    openExternalAppUseCase = openExternalAppUseCase,
                    onSearchInTab = onSearchInTab,
                    onSnackbarText = onSnackbarText,
                )
            }
        }
    }

    private fun rowTag(entry: WotdHistoryItem): String =
        "${WOTD_ITEM_ROW_TAG}${entry.word}_${entry.date}"

    private fun starTag(entry: WotdHistoryItem): String =
        "${WOTD_ITEM_STAR_TAG}${entry.word}_${entry.date}"

    private fun waitUntilTagExists(tag: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitUntilTextExists(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * Fakes the history: the entries are set by the test.
     * Keeps the behavior of the real use case: the favorite flag is set per word.
     */
    private class GetStaticWotdHistoryUseCase : GetWotdHistoryUseCase {

        var entries: List<WotdHistoryItem> = emptyList()

        override suspend operator fun invoke(favoriteWords: Set<String>): List<WotdHistoryItem> =
            entries.map { it.copy(isFavorite = favoriteWords.contains(it.word)) }
    }

    companion object {
        // The word "alpha" appears twice, on different dates. This exercises
        // the composite list key, which must stay unique for repeated words.
        private val HISTORY = listOf(
            WotdHistoryItem("alpha", "Sep 27", false),
            WotdHistoryItem("bravo", "Sep 26", false),
            WotdHistoryItem("alpha", "Sep 25", false),
            WotdHistoryItem("charlie", "Sep 24", false),
            WotdHistoryItem("delta", "Sep 23", false),
        )
    }
}
