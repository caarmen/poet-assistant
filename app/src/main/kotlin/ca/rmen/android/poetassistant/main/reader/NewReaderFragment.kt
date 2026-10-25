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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.compat.HtmlCompat
import ca.rmen.android.poetassistant.main.AppBarLayoutHelper
import ca.rmen.android.poetassistant.main.common.usecases.ShareUseCase
import ca.rmen.android.poetassistant.main.dictionaries.ConfirmDialogFragment
import ca.rmen.android.poetassistant.main.reader.data.Poem
import ca.rmen.android.poetassistant.main.reader.ui.composables.ReaderScreen
import ca.rmen.android.poetassistant.main.reader.usecases.GetSaveAsFilenameUseCase
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
class NewReaderFragment : Fragment(), ConfirmDialogFragment.ConfirmDialogListener {

    companion object {
        private const val ARG_INITIAL_POEM = "initial_poem"
        private const val DIALOG_TAG = "dialog"
        private const val ACTION_FILE_NEW = 2

        fun create(initialPoem: String?): NewReaderFragment = NewReaderFragment().apply {
            arguments = Bundle(1).apply {
                putString(ARG_INITIAL_POEM, initialPoem)
            }
        }
    }

    @Inject
    lateinit var shareUseCase: ShareUseCase

    @Inject
    lateinit var getSaveAsFilenameUseCase: GetSaveAsFilenameUseCase

    private val viewModel: ReaderScreenViewModel by viewModels()
    private val isVisibleFlow = MutableStateFlow(false)

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
        setHasOptionsMenu(true)
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
                                AppBarLayoutHelper.forceExpandAppBarLayout(activity)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val owner = viewLifecycleOwner
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Invalidate the options menu only when the menu
                // enablement inputs change, not on every keystroke.
                launch {
                    viewModel.state
                        .map { Pair(it.poem is Poem.PersistedPoem, it.textFieldValue.text.isNotEmpty()) }
                        .distinctUntilChanged()
                        .collect { activity?.invalidateOptionsMenu() }
                }
                // The fragment is the single collector of the share
                // flow: it owns the Context for the chooser.
                launch {
                    viewModel.share.collect { share ->
                        share?.let {
                            shareUseCase(it, requireContext())
                            viewModel.onShareSent()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_tts, menu)
        menu.findItem(R.id.action_share).isVisible = false
        // Printing is reworked in a later step; hidden until then.
        menu.findItem(R.id.action_print).isVisible = false
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val hasSavedPoem = viewModel.state.value.poem is Poem.PersistedPoem
        val hasPoemText = viewModel.state.value.textFieldValue.text.isNotEmpty()
        menu.findItem(R.id.action_save)?.isEnabled = hasSavedPoem
        listOf(
            R.id.action_new,
            R.id.action_save_as,
            R.id.action_share,
            R.id.action_share_poem_text,
            R.id.action_share_poem_audio,
        ).forEach { menu.findItem(it)?.isEnabled = hasPoemText }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_new -> ConfirmDialogFragment.show(
                ACTION_FILE_NEW,
                getString(R.string.file_new_confirm_title),
                getString(R.string.action_clear),
                childFragmentManager,
                DIALOG_TAG,
            )
            R.id.action_open -> openPoemLauncher.launch(arrayOf("text/plain"))
            R.id.action_save -> viewModel.onSavePoem()
            R.id.action_save_as -> saveAsPoemLauncher.launch(
                getSaveAsFilenameUseCase(viewModel.state.value.textFieldValue.text),
            )
            R.id.action_share, R.id.action_share_poem_text -> viewModel.onSharePoem()
            R.id.action_share_poem_audio -> viewModel.onSharePoemAsAudio()
        }
        return true
    }

    override fun onOk(actionId: Int) {
        if (actionId == ACTION_FILE_NEW) {
            viewModel.onClearPoemClicked()
            // Hack for https://github.com/caarmen/poet-assistant/issues/72
            // On some devices, clearing the poem text auto-hides the app bar layout.
            // Let's expand it again.
            AppBarLayoutHelper.forceExpandAppBarLayout(activity)
            activity?.invalidateOptionsMenu()
        }
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
