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
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.Text
import androidx.compose.ui.platform.ComposeView
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
import ca.rmen.android.poetassistant.main.MenuHostProvider
import ca.rmen.android.poetassistant.main.TabMenuActions
import ca.rmen.android.poetassistant.main.common.usecases.ShareUseCase
import ca.rmen.android.poetassistant.main.reader.data.Poem
import ca.rmen.android.poetassistant.main.reader.usecases.GetSaveAsFilenameUseCase
import ca.rmen.android.poetassistant.main.reader.usecases.PrintPoemUseCase
import ca.rmen.android.poetassistant.theme.AppTheme
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

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            TODO("Not yet implemented")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as? MenuHostProvider)?.menuHost?.addMenuProvider(
            menuProvider,
            menuVisibilityLifecycleOwner,
            Lifecycle.State.RESUMED,
        )
        // Drive the visibility-gated lifecycle from the visible-tab signal:
        // setMenuVisibility is only called with true for the primary page.
        lifecycleScope.launch {
            isMenuVisible.collect { menuVisible ->
                menuVisibilityLifecycleOwner.setState(
                    if (menuVisible) Lifecycle.State.RESUMED else Lifecycle.State.CREATED,
                )
            }
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        isMenuVisible.value = menuVisible
    }

    override fun onDestroy() {
        menuHost?.removeMenuProvider(menuProvider)
        super.onDestroy()
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
                AppTheme {
                    Text(text="reader")
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
                        .collect { menuHost?.invalidateMenu() }
                }
            }
        }
    }

    override fun onShare() {
    }

    /**
     * The entry point for text shared into the app (ACTION_SEND):
     * appends the shared text to the poem.
     *
     * @param text The shared text.
     */
    fun setPoem(text: String?) {
    }

}
