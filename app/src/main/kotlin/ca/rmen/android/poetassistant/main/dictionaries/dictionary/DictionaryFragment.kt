package ca.rmen.android.poetassistant.main.dictionaries.dictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.TabMenuActions
import ca.rmen.android.poetassistant.main.common.usecases.OpenExternalSearchUseCase
import ca.rmen.android.poetassistant.main.common.usecases.ShareUseCase
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.ui.composables.DictionaryScreen
import ca.rmen.android.poetassistant.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class DictionaryFragment : Fragment(), TabMenuActions {

    companion object {
        private const val ARG_INITIAL_QUERY = "initialQuery"
        fun create(initialQuery: String?): DictionaryFragment = DictionaryFragment().apply {
            arguments = Bundle(1).apply {
                putString(ARG_INITIAL_QUERY, initialQuery)
            }
        }
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
                AppTheme {
                    Text(text="dictionary")
                }
            }
        }
        return view
    }

    override fun onShare() {
    }


}
