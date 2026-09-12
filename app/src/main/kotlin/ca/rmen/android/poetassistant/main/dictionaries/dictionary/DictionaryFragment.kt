package ca.rmen.android.poetassistant.main.dictionaries.dictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
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
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.common.usecases.OpenExternalSearchUseCase
import ca.rmen.android.poetassistant.main.common.usecases.ShareUseCase
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.ui.composables.DictionaryScreen
import ca.rmen.android.poetassistant.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class DictionaryFragment : Fragment() {

    companion object {
        // Remove this as soon as we have test coverage of the new code.
        // Then we can definitively use this new fragment.
        const val TEMP_USE_ME = false
        private const val ARG_INITIAL_QUERY = "initialQuery"
        fun create(initialQuery: String?): DictionaryFragment = DictionaryFragment().apply {
            arguments = Bundle(1).apply {
                putString(ARG_INITIAL_QUERY, initialQuery)
            }
        }
    }

    @Inject
    lateinit var shareUseCase: ShareUseCase

    @Inject
    lateinit var openExternalSearchUseCase: OpenExternalSearchUseCase

    private val viewModel: DictionaryScreenViewModel by viewModels()
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
                        DictionaryScreen(
                            viewModel = viewModel,
                            shareUseCase = shareUseCase,
                            openExternalSearchUseCase = openExternalSearchUseCase,
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.action_share) {
                    viewModel.onShare()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner)
    }

    fun query(word: String) {
        viewModel.onWordSearched(word)
    }

}
