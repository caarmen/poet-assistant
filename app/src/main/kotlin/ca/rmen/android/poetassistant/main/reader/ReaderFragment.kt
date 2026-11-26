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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.rmen.android.poetassistant.main.TabMenuActions
import ca.rmen.android.poetassistant.main.common.usecases.ShareUseCase
import ca.rmen.android.poetassistant.main.reader.usecases.GetSaveAsFilenameUseCase
import ca.rmen.android.poetassistant.main.reader.usecases.PrintPoemUseCase
import ca.rmen.android.poetassistant.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
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

    fun onMenuNew() {
        viewModel.onClearPoemRequestClicked()
        // Hack for https://github.com/caarmen/poet-assistant/issues/72
        // On some devices, showing the clear poem dialog auto-hides the app bar layout.
        // TODO(Step 3): replace with an onRequestAppBarExpand callback; MainScreen
        // will reset the TopAppBarScrollBehavior state to force the app bar expanded.
    }

    fun onMenuOpen() {
        openPoemLauncher.launch(arrayOf("text/plain"))
    }

    fun onMenuSave() {
       viewModel.onSavePoem()
    }

    fun onMenuSaveAs() {
        saveAsPoemLauncher.launch(
            getSaveAsFilenameUseCase(viewModel.state.value.textFieldValue.text),
        )
    }

    fun onMenuShareAudio() {
        viewModel.onSharePoemAsAudio()
    }

    fun onMenuPrint() {
        val activity = requireActivity()
        val poemText = viewModel.state.value.textFieldValue.text
        lifecycleScope.launch {
            printPoemUseCase(activity, poemText)
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
        isVisibleFlow.value = menuVisible
    }


    override fun onPause() {
        // Persist the poem on pause: a tab swipe pauses the reader
        // fragment.
        viewModel.onPersistPoem()
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
                        Text("hello")
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

}
