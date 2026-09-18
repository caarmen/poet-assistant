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

package ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables

import android.content.ClipData
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.rmen.android.poetassistant.main.common.usecases.OpenExternalAppUseCase
import ca.rmen.android.poetassistant.main.common.usecases.OpenExternalSearchUseCase
import ca.rmen.android.poetassistant.main.common.usecases.ShareUseCase
import ca.rmen.android.poetassistant.main.dictionaries.patterns.PatternScreenViewModel
import ca.rmen.android.poetassistant.main.Tab
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Top-level composable for the Pattern screen.
 * Observes the ViewModel state and displays the appropriate UI.
 * Collects share flow and triggers share actions.
 *
 * @param viewModel The PatternScreenViewModel.
 * @param shareUseCase Use case for sharing content.
 * @param openExternalAppUseCase Use case for opening external apps.
 * @param onSearchInTab Callback when searching in another tab.
 * @param onShare Callback when a share action is triggered.
 * @param onSnackbarText Callback when a snackbar message should be shown.
 * @param modifier The modifier for this composable.
 */
@Composable
fun PatternScreen(
    viewModel: PatternScreenViewModel,
    shareUseCase: ShareUseCase,
    openExternalAppUseCase: OpenExternalAppUseCase,
    onSearchInTab: (String, Tab) -> Unit,
    onShare: () -> Unit,
    onSnackbarText: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val shareState by viewModel.share.collectAsStateWithLifecycle()
    val layout by viewModel.layout.collectAsStateWithLifecycle()
    val snackbarTextResId by viewModel.snackbarTextResId.collectAsStateWithLifecycle()
    val snackbarText = snackbarTextResId?.let { stringResource(it) }

    LaunchedEffect(shareState) {
        shareState?.let { share ->
            shareUseCase(share, context)
            viewModel.onShareSent()
        }
    }

    LaunchedEffect(snackbarTextResId) {
        snackbarText?.let {
            onSnackbarText(it)
        }
        // Hardcode this duration for now, while we have a mixed integration between views
        // and compose for snackbar. This value comes from views SnackbarManager.LENGTH_LONG
        delay(2750.milliseconds)
        viewModel.onSnackbarShown()
    }

    PatternScreenContent(
        state = state,
        layout = layout,
        externalAppMenuItemsProducer = { word -> viewModel.getExternalAppMenuItems(word) },
        onSetFavorite = { word, isFavorite -> viewModel.onSetFavorite(word, isFavorite) },
        onCopy = { word ->
            coroutineScope.launch {
                clipboard.setClipEntry(
                    ClipEntry(ClipData.newPlainText(word, word)))
                viewModel.onCopiedText()
            }
         },
        onSearchInTab = onSearchInTab,
        onExternalAppSelected = { word, menuItem ->
            openExternalAppUseCase(word, menuItem, context)
        },
        modifier = modifier
    )
}
