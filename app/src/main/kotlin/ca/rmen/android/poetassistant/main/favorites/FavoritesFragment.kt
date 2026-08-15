package ca.rmen.android.poetassistant.main.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ca.rmen.android.poetassistant.main.favorites.composables.FavoritesScreen
import ca.rmen.android.poetassistant.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesFragment : Fragment() {
    private val viewModel: FavoritesScreenViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    FavoritesScreen(
                        viewModel = viewModel,
                        onSearchInTab = { _, _ -> },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

}
