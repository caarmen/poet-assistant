package ca.rmen.android.poetassistant.main.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnWordClickListener
import ca.rmen.android.poetassistant.main.favorites.composables.FavoritesScreen
import ca.rmen.android.poetassistant.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

@AndroidEntryPoint
class FavoritesFragment : Fragment() {


    @Inject
    lateinit var shareUseCase: ShareUseCase

    @Inject
    lateinit var openExternalAppUseCase: OpenExternalAppUseCase

    private val viewModel: FavoritesScreenViewModel by viewModels()
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
                    FavoritesScreen(
                        viewModel = viewModel,
                        shareUseCase = shareUseCase,
                        openExternalAppUseCase = openExternalAppUseCase,
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

}
