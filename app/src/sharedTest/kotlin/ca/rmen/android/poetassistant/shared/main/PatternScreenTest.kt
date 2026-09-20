package ca.rmen.android.poetassistant.shared.main

import android.content.ClipboardManager
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
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
import ca.rmen.android.poetassistant.main.dictionaries.patterns.PatternScreenViewModel
import ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables.PATTERNS_SCREEN_CONTENT_EMPTY_TAG
import ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables.PATTERNS_SCREEN_CONTENT_LIST_TAG
import ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables.PATTERN_HEADER_HELP_TAG
import ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables.PATTERN_ITEM_ROW_TAG
import ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables.PATTERN_ITEM_STAR_TAG
import ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables.PATTERN_SCREEN_CONTENT_MAX_RESULTS_TAG
import ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables.PatternScreen
import ca.rmen.android.poetassistant.main.dictionaries.patterns.usecases.CreatePatternShareUseCase
import ca.rmen.android.poetassistant.main.dictionaries.patterns.usecases.LookupPatternUseCase
import ca.rmen.android.poetassistant.main.favorites.data.FavoritesRepository
import ca.rmen.android.poetassistant.rules.PoetAssistantComposeTestRule
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.settings.SettingsRepository
import ca.rmen.android.poetassistant.theme.AppTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(AndroidJUnit4::class)
class PatternScreenTest {
    @get:Rule(order = 0)
    val hiltTestRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @get:Rule(order = 2)
    val poetAssistantComposeTestRule = PoetAssistantComposeTestRule(composeTestRule)

    private lateinit var context: Context

    @Inject
    lateinit var favoritesRepository: FavoritesRepository

    @Inject
    lateinit var createPatternShareUseCase: CreatePatternShareUseCase

    @Inject
    lateinit var lookupPatternUseCase: LookupPatternUseCase

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var shareUseCase: ShareUseCase

    @Inject
    lateinit var getProcessTextMenuItemsUseCase: GetProcessTextMenuItemsUseCase

    @Inject
    lateinit var openExternalAppUseCase: OpenExternalAppUseCase

    private lateinit var viewModel: PatternScreenViewModel

    @Before
    fun setUp() {
        hiltTestRule.inject()
        context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel = PatternScreenViewModel(
            settingsRepository = settingsRepository,
            lookupPatternUseCase = lookupPatternUseCase,
            favoritesRepository = favoritesRepository,
            createPatternShareUseCase = createPatternShareUseCase,
            getProcessTextMenuItemsUseCase = getProcessTextMenuItemsUseCase,
        )
    }

    /**
     * Given some words
     * When a pattern matching these words is searched,
     * And one of the matching words is clicked,
     * And the copy item is selected on that word,
     * Then the word is copied to the clipboard
     * And the expected snackbar text is emitted
     */
    @Test
    fun testCopy() {
        // Given some favorite words
        runBlocking {
            settingsRepository.setLayout(Layout.EFFICIENT)
        }
        var snackbarText: String? = null
        composeTestRule.setContent {
            AppTheme {
                PatternScreen(
                    viewModel = viewModel,
                    shareUseCase = shareUseCase,
                    openExternalAppUseCase = openExternalAppUseCase,
                    onSearchInTab = { _, _ -> },
                    onShare = {},
                    onSnackbarText = { snackbarText = it },
                )
            }
        }
        // When a pattern matching these words is searched,
        viewModel.onPatternSearched("hell*")

        // And one of the matching words is clicked,
        waitUntilTagExists("${PATTERN_ITEM_ROW_TAG}hell")
        composeTestRule.onNodeWithTag("${PATTERN_ITEM_ROW_TAG}hell", useUnmergedTree = true)
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
        assertEquals("hell", clippedText)

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
     * And some favorite words
     * When one of the words is clicked
     * And a R/T/D lookup is chosen
     * Then the expected word and tab are forwarded to the onSearchInTab callback
     */
    private fun testCleanLookup(scenario: CleanLookupScenario) {
        // Given the clean layout
        // And some favorite words
        runBlocking {
            settingsRepository.setLayout(Layout.CLEAN)
        }
        var selectedWord: String? = null
        var selectedTab: Tab? = null
        composeTestRule.setContent {
            AppTheme {
                PatternScreen(
                    viewModel = viewModel,
                    shareUseCase = shareUseCase,
                    openExternalAppUseCase = openExternalAppUseCase,
                    onSearchInTab = { word, tab ->
                        selectedWord = word
                        selectedTab = tab
                    },
                    onShare = {},
                    onSnackbarText = { },
                )
            }
        }
        viewModel.onPatternSearched("hell*")
        // When one of the words is clicked
        waitUntilTagExists("${PATTERN_ITEM_ROW_TAG}hell")
        composeTestRule.onNodeWithTag("${PATTERN_ITEM_ROW_TAG}hell", useUnmergedTree = true)
            .performClick()

        // And a R/T/D lookup is chosen
        val lookupLabel = context.getString(scenario.lookupLabel)
        waitUntilTextExists(lookupLabel)
        composeTestRule.onNodeWithText(lookupLabel).assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        // Then the expected word and tab are forwarded to the onSearchInTab callback
        assertEquals(scenario.expectedTab, selectedTab)
        assertEquals("hell", selectedWord)
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
     * And some favorite words
     * When an R/T/D icon in the list item is clicked
     * Then the expected word and tab are forwarded to the onSearchInTab callback
     */
    private fun testEfficientLookup(scenario: EfficientLookupScenario) {
        // Given the efficient layout
        // And some favorite words
        runBlocking {
            settingsRepository.setLayout(Layout.EFFICIENT)
        }
        var selectedWord: String? = null
        var selectedTab: Tab? = null
        composeTestRule.setContent {
            AppTheme {
                PatternScreen(
                    viewModel = viewModel,
                    shareUseCase = shareUseCase,
                    openExternalAppUseCase = openExternalAppUseCase,
                    onSearchInTab = { word, tab ->
                        selectedWord = word
                        selectedTab = tab
                    },
                    onShare = {},
                    onSnackbarText = { },
                )
            }
        }
        viewModel.onPatternSearched("hell*")
        // When an R/T/D icon in the list item is clicked
        waitUntilTagExists("${scenario.testTagPrefix}hell")
        composeTestRule.onNodeWithTag("${scenario.testTagPrefix}hell").assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()

        // Then the expected word and tab are forwarded to the onSearchInTab callback
        assertEquals(scenario.expectedTab, selectedTab)
        assertEquals("hell", selectedWord)
    }

    /**
     * Given a database of words
     * When a pattern term is searched which matches no words,
     * Then an empty result list is displayed.
     */
    @Test
    fun testNoMatchFound() {
        // Given a database of words
        composeTestRule.setContent {
            AppTheme {
                PatternScreen(
                    viewModel = viewModel,
                    shareUseCase = shareUseCase,
                    openExternalAppUseCase = openExternalAppUseCase,
                    onSearchInTab = { _, _ -> },
                    onShare = {},
                    onSnackbarText = { },
                )
            }
        }

        // When a pattern term is searched which matches no words,
        viewModel.onPatternSearched("am stram gram*")

        // Then an empty result list is displayed.
        composeTestRule.waitForIdle()
        waitUntilTagExists(PATTERNS_SCREEN_CONTENT_EMPTY_TAG)
        composeTestRule.onNodeWithTag(PATTERNS_SCREEN_CONTENT_EMPTY_TAG).assertIsDisplayed()
    }

    /**
     * Given a database of words,
     * When a pattern term is searched which matches a huge number of words,
     * Then a limitation is indicated.
     */
    @Test
    fun testLargeResult() {
        // Given a database of words
        composeTestRule.setContent {
            AppTheme {
                PatternScreen(
                    viewModel = viewModel,
                    shareUseCase = shareUseCase,
                    openExternalAppUseCase = openExternalAppUseCase,
                    onSearchInTab = { _, _ -> },
                    onShare = {},
                    onSnackbarText = { },
                )
            }
        }

        // When a pattern term is searched which matches a huge number of words,
        viewModel.onPatternSearched("a*")

        // Then a limitation is indicated
        composeTestRule.waitForIdle()
        waitUntilTagExists(PATTERN_SCREEN_CONTENT_MAX_RESULTS_TAG)
        composeTestRule.onNodeWithTag(PATTERN_SCREEN_CONTENT_MAX_RESULTS_TAG).assertIsDisplayed()
    }

    /**
     * Given a list of words matching a searched pattern,
     * When the help icon in the header is clicked,
     * Then a help dialog appears
     */
    @Test
    fun testHelpDialog() {
        // Given a list of words matching a searched pattern,
        composeTestRule.setContent {
            AppTheme {
                PatternScreen(
                    viewModel = viewModel,
                    shareUseCase = shareUseCase,
                    openExternalAppUseCase = openExternalAppUseCase,
                    onSearchInTab = { _, _ -> },
                    onShare = {},
                    onSnackbarText = { },
                )
            }
        }
        viewModel.onPatternSearched("hell*")
        composeTestRule.waitForIdle()

        // When the help icon in the header is clicked
        waitUntilTagExists(PATTERN_HEADER_HELP_TAG)
        composeTestRule.onNodeWithTag(PATTERN_HEADER_HELP_TAG).assertIsDisplayed().performClick()

        // Then a help dialog appears
        composeTestRule.onNodeWithText(context.getString(R.string.pattern_help_title))
            .assertIsDisplayed()

        // Dismiss the dialog
        composeTestRule.onNodeWithText(context.getString(android.R.string.ok)).assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.pattern_help_title))
            .assertDoesNotExist()
    }

    /**
     * Given a list of words matching a searched pattern,
     * When the star icon of a matching word is clicked,
     * Then the word moved to the top of the list.
     * And the word's favorite icon is toggled,
     */
    @Test
    fun testFavorites() {
        // Given a list of words matching a searched pattern,
        composeTestRule.setContent {
            AppTheme {
                PatternScreen(
                    viewModel = viewModel,
                    shareUseCase = shareUseCase,
                    openExternalAppUseCase = openExternalAppUseCase,
                    onSearchInTab = { _, _ -> },
                    onShare = {},
                    onSnackbarText = { },
                )
            }
        }
        viewModel.onPatternSearched("hell*")
        composeTestRule.waitForIdle()

        // When the star icon of a matching word not at the top of the list is clicked,
        waitUntilTagExists(PATTERNS_SCREEN_CONTENT_LIST_TAG)
        composeTestRule.onNodeWithTag(PATTERNS_SCREEN_CONTENT_LIST_TAG).onChildren().onFirst()
            .assert(!hasText("hell to pay"))
        composeTestRule.onNodeWithTag("${PATTERN_ITEM_STAR_TAG}hell to pay").assertIsDisplayed()
            .assertIsOff()
            .performClick()

        // Then the word moved to the top of the list.
        composeTestRule.onNodeWithTag(PATTERNS_SCREEN_CONTENT_LIST_TAG).performScrollToIndex(0)
        composeTestRule.onNodeWithTag(PATTERNS_SCREEN_CONTENT_LIST_TAG).onChildren().onFirst()
            .assert(hasText("hell to pay"))

        // And the word's favorite icon is toggled
        composeTestRule.onNodeWithTag("${PATTERN_ITEM_STAR_TAG}hell to pay").assertIsDisplayed()
            .assertIsOn()
    }

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
}