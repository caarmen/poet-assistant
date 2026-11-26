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

package ca.rmen.android.poetassistant.main.reader

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.compat.HtmlCompat
import ca.rmen.android.poetassistant.main.MenuHostProvider
import ca.rmen.android.poetassistant.main.TabMenuActions
import ca.rmen.android.poetassistant.main.common.usecases.ShareUseCase
import ca.rmen.android.poetassistant.main.reader.data.Poem
import ca.rmen.android.poetassistant.main.reader.ui.composables.ReaderScreen
import ca.rmen.android.poetassistant.main.reader.usecases.GetSaveAsFilenameUseCase
import ca.rmen.android.poetassistant.main.reader.usecases.PrintPoemUseCase
import ca.rmen.android.poetassistant.settings.SettingsActivity
import ca.rmen.android.poetassistant.theme.AppTheme
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The reader tab: a Compose screen for writing and playing a poem.
 * The options menu (open/save/save-as files, share) keeps working
 * from the fragment; the share chooser is launched here because it
 * needs a Context.
 */
@AndroidEntryPoint
class ReaderFragment : Fragment(), TabMenuActions {

    companion object {
        private const val ARG_INITIAL_POEM = "initial_poem"

        fun create(initialPoem: String?): ReaderFragment = ReaderFragment().apply {
            arguments = Bundle(1).apply {
                putString(ARG_INITIAL_POEM, initialPoem)
            }
        }
    }

    @Inject
    lateinit var shareUseCase: ShareUseCase

    @Inject
    lateinit var getSaveAsFilenameUseCase: GetSaveAsFilenameUseCase

    @Inject
    lateinit var printPoemUseCase: PrintPoemUseCase

    private val viewModel: ReaderScreenViewModel by viewModels()
    private val isVisibleFlow = MutableStateFlow(false)
    private val isMenuVisible = MutableStateFlow(false)

    // The host activity exposes an unattached toolbar as the MenuHost,
    // since there is no action bar anymore.
    private val menuHost: MenuHost?
        get() = (activity as? MenuHostProvider)?.menuHost

    // Dedicated lifecycle owner for the menu provider registration. Its
    // registry only reaches RESUMED while the reader tab is the visible
    // page (driven by setMenuVisibility), so the provider is active on
    // the reader tab only, not on every tab where this fragment is
    // RESUMED (offscreenPageLimit = 5).
    private val menuVisibilityLifecycleOwner = object : LifecycleOwner {
        private val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
        fun setState(state: Lifecycle.State) {
            registry.currentState = state
        }
    }


    // A canceled picker yields a null uri: do nothing.
    private val openPoemLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let(viewModel::onOpenPoem)
        }

    private val saveAsPoemLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            uri?.let(viewModel::onSavePoemAs)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Apply the poem shared into the app once per fragment instance,
        // then clear the argument: rotation and process death must not
        // re-apply it over the user's edits.
        if (requireArguments().containsKey(ARG_INITIAL_POEM)) {
            val initialPoem = requireArguments().getString(ARG_INITIAL_POEM)
            requireArguments().remove(ARG_INITIAL_POEM)
            if (!initialPoem.isNullOrEmpty()) {
                viewModel.onInitialPoemLoaded(initialPoem)
            }
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        isMenuVisible.value = menuVisible
    }

    override fun onResume() {
        super.onResume()
        isVisibleFlow.value = true
    }

    override fun onPause() {
        // Persist the poem on pause: a tab swipe pauses the reader
        // fragment.
        viewModel.onPersistPoem()
        isVisibleFlow.value = false
        super.onPause()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = ComposeView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setContent {
                val isVisible by isVisibleFlow.collectAsState()
                AppTheme {
                    if (isVisible) {
                        ReaderScreen(
                            viewModel = viewModel,
                            onEvent = ::onEvent,
                            onImeClosed = {
                                // TODO(Step 3): replace with an onRequestAppBarExpand callback;
                                // MainScreen will reset the TopAppBarScrollBehavior state to
                                // force the app bar expanded.
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(rememberNestedScrollInteropConnection()),
                        )
                    }
                }
            }
        }
        return view
    }


    override fun onShare() {
        viewModel.onSharePoem()
    }

    /**
     * The entry point for text shared into the app (ACTION_SEND):
     * appends the shared text to the poem.
     *
     * @param text The shared text.
     */
    fun setPoem(text: String?) {
        text?.let { viewModel.onInitialPoemLoaded(it) }
    }

    private fun onEvent(event: ReaderScreenViewModel.ReaderEvent) {
        val root = view ?: return
        val message: CharSequence = when (event) {
            is ReaderScreenViewModel.ReaderEvent.TtsError -> HtmlCompat.fromHtml(getString(R.string.tts_error))
            is ReaderScreenViewModel.ReaderEvent.PoemOpened -> getString(R.string.file_opened, event.fileName ?: "")
            is ReaderScreenViewModel.ReaderEvent.PoemSaved -> getString(R.string.file_saved, event.fileName ?: "")
            is ReaderScreenViewModel.ReaderEvent.PoemOpenError -> getString(R.string.file_opened_error)
            is ReaderScreenViewModel.ReaderEvent.PoemSaveError -> getString(R.string.file_saved_error)
            is ReaderScreenViewModel.ReaderEvent.PoemSharedAsAudio -> getString(R.string.share_poem_audio_snackbar)
        }
        val snackbar = Snackbar.make(root, message, Snackbar.LENGTH_LONG)
        if (event is ReaderScreenViewModel.ReaderEvent.TtsError) {
            val intent = Intent("com.android.settings.TTS_SETTINGS")
            if (intent.resolveActivity(root.context.packageManager) != null) {
                snackbar.setAction(R.string.tts_error_open_system_settings) { startActivity(intent) }
            } else {
                snackbar.setAction(R.string.tts_error_open_app_settings) {
                    startActivity(Intent(requireContext(), SettingsActivity::class.java))
                }
            }
        }
        snackbar.show()
    }
}
