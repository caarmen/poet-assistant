package ca.rmen.android.poetassistant.shared.main

import android.content.ClipboardManager
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.favorites.usecases.CreateFavoritesShareUseCase
import ca.rmen.android.poetassistant.main.favorites.data.FavoritesRepository
import ca.rmen.android.poetassistant.main.favorites.FavoritesScreenViewModel
import ca.rmen.android.poetassistant.main.common.usecases.GetProcessTextMenuItemsUseCase
import ca.rmen.android.poetassistant.main.common.usecases.OpenExternalAppUseCase
import ca.rmen.android.poetassistant.main.common.usecases.ShareUseCase
import ca.rmen.android.poetassistant.main.favorites.ui.composables.CONFIRM_DELETE_DIALOG_CANCEL_BUTTON_TAG
import ca.rmen.android.poetassistant.main.favorites.ui.composables.CONFIRM_DELETE_DIALOG_CONFIRM_BUTTON_TAG
import ca.rmen.android.poetassistant.main.favorites.ui.composables.FAVORITES_HEADER_DELETE_ALL_TAG
import ca.rmen.android.poetassistant.main.favorites.ui.composables.FAVORITES_SCREEN_CONTENT_EMPTY_TAG
import ca.rmen.android.poetassistant.main.favorites.ui.composables.FAVORITES_SCREEN_CONTENT_LIST_TAG
import ca.rmen.android.poetassistant.main.favorites.ui.composables.FAVORITE_ITEM_DICTIONARY_TAG
import ca.rmen.android.poetassistant.main.favorites.ui.composables.FAVORITE_ITEM_RHYMER_TAG
import ca.rmen.android.poetassistant.main.favorites.ui.composables.FAVORITE_ITEM_ROW_TAG
import ca.rmen.android.poetassistant.main.favorites.ui.composables.FAVORITE_ITEM_THESAURUS_TAG
import ca.rmen.android.poetassistant.main.favorites.ui.composables.FavoritesScreen
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
class FavoritesScreenTest {
    @get:Rule(order = 0)
    val hiltTestRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var context: Context

    @Inject
    lateinit var favoritesRepository: FavoritesRepository

    @Inject
    lateinit var createFavoritesShareUseCase: CreateFavoritesShareUseCase

    @Inject
    lateinit var getProcessTextMenuItemsUseCase: GetProcessTextMenuItemsUseCase

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var shareUseCase: ShareUseCase

    @Inject
    lateinit var openExternalAppUseCase: OpenExternalAppUseCase

    private lateinit var viewModel: FavoritesScreenViewModel

    @Before
    fun setUp() {
        hiltTestRule.inject()
        context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel = FavoritesScreenViewModel(
            settingsRepository = settingsRepository,
            favoritesRepository = favoritesRepository,
            createFavoritesShareUseCase = createFavoritesShareUseCase,
            getProcessTextMenuItemsUseCase = getProcessTextMenuItemsUseCase
        )
        runBlocking {
            settingsRepository.setLayout(Layout.EFFICIENT)
            favoritesRepository.saveFavorite("cheesecake", true)
            favoritesRepository.saveFavorite("cookie", true)
        }
    }

    /**
     * Given some favorite words
     * When one of the words is clicked
     * And the copy item is selected
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
                FavoritesScreen(
                    viewModel = viewModel,
                    shareUseCase = shareUseCase,
                    openExternalAppUseCase = openExternalAppUseCase,
                    onSearchInTab = { _, _ -> },
                    onSnackbarText = { snackbarText = it },
                )
            }
        }
        // When one of the words is clicked
        waitUntilTagExists("${FAVORITE_ITEM_ROW_TAG}cookie")
        composeTestRule.onNodeWithTag("${FAVORITE_ITEM_ROW_TAG}cookie", useUnmergedTree = true)
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
        assertEquals("cookie", clippedText)

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
                FavoritesScreen(
                    viewModel = viewModel,
                    shareUseCase = shareUseCase,
                    openExternalAppUseCase = openExternalAppUseCase,
                    onSearchInTab = { word, tab ->
                        selectedWord = word
                        selectedTab = tab
                    },
                    onSnackbarText = { },
                )
            }
        }
        // When one of the words is clicked
        waitUntilTagExists("${FAVORITE_ITEM_ROW_TAG}cookie")
        composeTestRule.onNodeWithTag("${FAVORITE_ITEM_ROW_TAG}cookie", useUnmergedTree = true)
            .performClick()

        // And a R/T/D lookup is chosen
        val lookupLabel = context.getString(scenario.lookupLabel)
        waitUntilTextExists(lookupLabel)
        composeTestRule.onNodeWithText(lookupLabel).assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        // Then the expected word and tab are forwarded to the onSearchInTab callback
        assertEquals(scenario.expectedTab, selectedTab)
        assertEquals("cookie", selectedWord)
    }

    enum class EfficientLookupScenario(val testTagPrefix: String, val expectedTab: Tab) {
        RHYMER(FAVORITE_ITEM_RHYMER_TAG, Tab.RHYMER),
        THESAURUS(FAVORITE_ITEM_THESAURUS_TAG, Tab.THESAURUS),
        DICTIONARY(FAVORITE_ITEM_DICTIONARY_TAG, Tab.DICTIONARY),
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
                FavoritesScreen(
                    viewModel = viewModel,
                    shareUseCase = shareUseCase,
                    openExternalAppUseCase = openExternalAppUseCase,
                    onSearchInTab = { word, tab ->
                        selectedWord = word
                        selectedTab = tab
                    },
                    onSnackbarText = { },
                )
            }
        }
        // When an R/T/D icon in the list item is clicked
        waitUntilTagExists("${scenario.testTagPrefix}cookie")
        composeTestRule.onNodeWithTag("${scenario.testTagPrefix}cookie").assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()

        // Then the expected word and tab are forwarded to the onSearchInTab callback
        assertEquals(scenario.expectedTab, selectedTab)
        assertEquals("cookie", selectedWord)

    }

    /**
     * Given some favorite words,
     * When the dialog to delete all favorite words is confirmed,
     * Then all words are deleted and the empty state is displayed
     */
    @Test
    fun testDeleteAll() {
        // Given some favorite words
        composeTestRule.setContent {
            AppTheme {
                FavoritesScreen(
                    viewModel = viewModel,
                    shareUseCase = shareUseCase,
                    openExternalAppUseCase = openExternalAppUseCase,
                    onSearchInTab = { _, _ -> },
                    onSnackbarText = { },
                )
            }
        }

        // When the dialog to delete all favorite words is confirmed,
        waitUntilTagExists(FAVORITES_HEADER_DELETE_ALL_TAG)
        composeTestRule.onNodeWithTag(FAVORITES_HEADER_DELETE_ALL_TAG).assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CONFIRM_DELETE_DIALOG_CONFIRM_BUTTON_TAG).assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()

        // Then all words are deleted and the empty state is displayed
        composeTestRule.onNodeWithTag(FAVORITES_SCREEN_CONTENT_LIST_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(FAVORITES_SCREEN_CONTENT_EMPTY_TAG).assertIsDisplayed()
    }

    /**
     * Given some favorite words
     * When the dialog to delete all favorite words is canceled,
     * Then all words are still present and the list is displayed
     */
    @Test
    fun testCancelDeleteAll() {
        // Given some favorite words
        composeTestRule.setContent {
            AppTheme {
                FavoritesScreen(
                    viewModel = viewModel,
                    shareUseCase = shareUseCase,
                    openExternalAppUseCase = openExternalAppUseCase,
                    onSearchInTab = { _, _ -> },
                    onSnackbarText = { },
                )
            }
        }

        // When the dialog to delete all favorite words is canceled,
        waitUntilTagExists(FAVORITES_HEADER_DELETE_ALL_TAG)
        composeTestRule.onNodeWithTag(FAVORITES_HEADER_DELETE_ALL_TAG).assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CONFIRM_DELETE_DIALOG_CANCEL_BUTTON_TAG).assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()

        // Then all words are still present and the list is displayed.
        composeTestRule.onNodeWithTag(FAVORITES_SCREEN_CONTENT_EMPTY_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(FAVORITES_SCREEN_CONTENT_LIST_TAG).assertIsDisplayed()
            .onChildren().assertCountEquals(2)
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