package ca.rmen.android.poetassistant.main.favorites

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ca.rmen.android.poetassistant.di.IODispatcher
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.settings.SettingsRepository
import ca.rmen.android.poetassistant.settings.Theme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
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
class FavoritesScreenViewModelTest {

    @get:Rule(order = 0)
    val hiltTestRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val archRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var favoritesRepository: FavoritesRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @IODispatcher
    @Inject
    lateinit var testDispatcher: CoroutineDispatcher

    private lateinit var viewModel: FavoritesScreenViewModel

    @Before
    fun setUp() {
        hiltTestRule.inject()
        viewModel = FavoritesScreenViewModel(
            settingsRepository = settingsRepository,
            favoritesRepository = favoritesRepository,
        )
    }

    /**
     * Given a few favorites in the repository,
     * When the viewmodel's favorites flow is accessed,
     * Then it returns the favorites from the repository, sorted.
     */
    @Test
    fun testFavoritesAreSorted() = runTest(testDispatcher) {
        // Given a few favorites in the repository,
        favoritesRepository.saveFavorite("bananas", true)
        favoritesRepository.saveFavorite("apples", true)
        favoritesRepository.saveFavorite("dogs", true)

        // When the viewmodel's favorites flow is accessed,
        val actualFavorites = viewModel.favorites.first { it.isNotEmpty() }

        // Then it returns the favorites from the repository, sorted.
        val expectedFavorites = listOf("apples", "bananas", "dogs")
        assertEquals(expectedFavorites, actualFavorites)
    }

    /**
     * Given the viewmodel is initialized,
     * When the layout flow is accessed,
     * Then it returns the current layout setting from preferences.
     */
    @Test
    fun testLayoutFlowEmitsCurrentLayout() = runTest(testDispatcher) {
        // When the viewmodel's layout flow is accessed,
        val layout = viewModel.layout.first()

        // Then it returns the current layout setting from preferences.
        val expectedLayout = settingsRepository.settingsFlow.value.layout
        assertEquals(expectedLayout, layout)
    }

    /**
     * Given the viewmodel
     * When settings change
     * Then the viewmodel emits layout changes.
     */
    @Test
    fun testEmitsLayoutChanges() = runTest(testDispatcher) {
        // Given the viewmodel
        val emittedLayouts = mutableListOf<Layout>()
        val observeLayoutJob = launch {
            viewModel.layout.collect {
                emittedLayouts.add(it)
            }
        }

        // When the settings change
        settingsRepository.setLayout(Layout.EFFICIENT)
        settingsRepository.setTheme(Theme.AUTO)
        settingsRepository.setLayout(Layout.CLEAN)
        settingsRepository.setTheme(Theme.DARK)
        observeLayoutJob.cancel()

        // Then the viewmodel emits layout changes.
        val expectedLayoutChanges =
            listOf(Layout.EFFICIENT, Layout.CLEAN)
        assertEquals(expectedLayoutChanges, emittedLayouts)
    }

    /**
     * Given a word is in favorites,
     * When onToggleFavorite is called with that word,
     * Then the word is removed from favorites.
     */
    @Test
    fun testOnToggleFavoriteRemovesWord() = runTest(testDispatcher) {
        // Given a word is in favorites
        favoritesRepository.saveFavorite("test", true)
        favoritesRepository.saveFavorite("apple", true)

        // Wait for the initial favorites to be emitted
        viewModel.favorites.first { it.contains("test") }

        // When onToggleFavorite is called with that word
        viewModel.onToggleFavorite("test")

        // Then the word is removed from favorites
        val updatedFavorites = viewModel.favorites.first { !it.contains("test") }
        assertEquals(listOf("apple"), updatedFavorites)
    }

    /**
     * Given there are favorites in the repository,
     * When onDeleteAll is called,
     * Then all favorites are cleared.
     */
    @Test
    fun testOnDeleteAllClearsFavorites() = runTest(testDispatcher) {
        // Given there are favorites in the repository
        favoritesRepository.saveFavorite("apple", true)
        favoritesRepository.saveFavorite("banana", true)
        favoritesRepository.saveFavorite("cherry", true)

        // Wait for the initial favorites to be emitted
        viewModel.favorites.first { it.size == 3 }

        // When onDeleteAll is called
        viewModel.onDeleteAll()

        // Then all favorites are cleared
        val clearedFavorites = viewModel.favorites.first { it.isEmpty() }
        assertEquals(emptyList<String>(), clearedFavorites)
    }
}
