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

package ca.rmen.android.poetassistant.main.dictionaries.thesaurus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.common.usecases.OpenExternalAppUseCase
import ca.rmen.android.poetassistant.main.common.usecases.OpenExternalSearchUseCase
import ca.rmen.android.poetassistant.main.common.usecases.ShareUseCase
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnWordClickListener
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ui.composables.ThesaurusScreen
import ca.rmen.android.poetassistant.theme.AppTheme
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class ThesaurusFragment : Fragment() {

    companion object {
        private const val ARG_INITIAL_QUERY = "initialQuery"
        fun create(initialQuery: String?): ThesaurusFragment = ThesaurusFragment().apply {
            arguments = Bundle(1).apply {
                putString(ARG_INITIAL_QUERY, initialQuery)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @Inject
    lateinit var shareUseCase: ShareUseCase

    @Inject
    lateinit var openExternalAppUseCase: OpenExternalAppUseCase

    @Inject
    lateinit var openExternalSearchUseCase: OpenExternalSearchUseCase

    private val viewModel: ThesaurusScreenViewModel by viewModels()
    private val isVisibleFlow = MutableStateFlow(false)

    override fun onResume() {
        super.onResume()
        isVisibleFlow.value = true
    }

    override fun onPause() {
        super.onPause()
        isVisibleFlow.value = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
                        ThesaurusScreen(
                            viewModel = viewModel,
                            shareUseCase = shareUseCase,
                            openExternalAppUseCase = openExternalAppUseCase,
                            openExternalSearchUseCase = openExternalSearchUseCase,
                            onSearchInTab = (requireActivity() as OnWordClickListener)::onWordClick,
                            onSnackbarText = {
                                Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(rememberNestedScrollInteropConnection())
                        )
                    }
                }
            }

            // Set the initial query if one was provided
            if (requireArguments().containsKey(ARG_INITIAL_QUERY)) {
                requireArguments().getString(ARG_INITIAL_QUERY)?.let {
                    viewModel.onWordSearched(it)
                }
            }
        }
        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_share) {
            viewModel.onShare()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun query(word: String) {
        viewModel.onWordSearched(word)
    }
}
