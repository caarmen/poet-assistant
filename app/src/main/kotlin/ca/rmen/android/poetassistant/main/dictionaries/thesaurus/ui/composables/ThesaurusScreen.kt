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

package ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ui.composables

import android.content.ClipData
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.common.ui.composables.TextInputDialog
import ca.rmen.android.poetassistant.main.common.usecases.OpenExternalAppUseCase
import ca.rmen.android.poetassistant.main.common.usecases.OpenExternalSearchUseCase
import ca.rmen.android.poetassistant.main.common.usecases.ShareUseCase
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusScreenViewModel
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusScreenState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Top-level composable for the thesaurus screen.
 * Observes the ViewModel state, handles sharing, the snackbar interop,
 * clipboard copies, and the filter dialog.
 *
 * @param viewModel The ThesaurusScreenViewModel.
 * @param shareUseCase Use case for sharing content.
 * @param openExternalAppUseCase Use case for opening external apps.
 * @param openExternalSearchUseCase Use case for opening a web search.
 * @param isLargeScreen Whether the screen is large (e.g. tablet), for
 * the alternating row colors in the result list.
 * @param onSearchInTab Callback to request searching a word in another tab.
 * @param onSnackbarText Callback to request showing a snackbar message.
 * @param modifier The modifier for this composable.
 */
@Composable
fun ThesaurusScreen(
    viewModel: ThesaurusScreenViewModel,
    shareUseCase: ShareUseCase,
    openExternalAppUseCase: OpenExternalAppUseCase,
    openExternalSearchUseCase: OpenExternalSearchUseCase,
    isLargeScreen: Boolean,
    onSearchInTab: (String, Tab) -> Unit,
    onSnackbarText: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val layout by viewModel.layout.collectAsStateWithLifecycle()
    val shareState by viewModel.share.collectAsStateWithLifecycle()
    val snackbarTextResId by viewModel.snackbarTextResId.collectAsStateWithLifecycle()
    val snackbarText = snackbarTextResId?.let { stringResource(it) }
    var showRhymeFilterDialog by remember { mutableStateOf(false) }

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

    ThesaurusScreenContent(
        state = state,
        layout = layout,
        isLargeScreen = isLargeScreen,
        externalAppMenuItemsProducer = { word -> viewModel.getExternalAppMenuItems(word) },
        onSetFavorite = { word, isFavorite -> viewModel.onSetFavorite(word, isFavorite) },
        onSpeakWord = { word -> viewModel.onSpeakWord(word) },
        onSearchWeb = { word -> openExternalSearchUseCase(word, context) },
        onCopy = { word ->
            coroutineScope.launch {
                clipboard.setClipEntry(
                    ClipEntry(ClipData.newPlainText(word, word)))
                viewModel.onCopiedText()
            }
        },
        onRhymeFilter = { showRhymeFilterDialog = true },
        onClearRhymeFilter = { viewModel.onClearRhymeFilter() },
        onSearchInTab = onSearchInTab,
        onExternalAppSelected = { word, menuItem ->
            openExternalAppUseCase(word, menuItem, context)
        },
        modifier = modifier,
    )

    val currentRhymeFilter = when (val currentState = state) {
        is ThesaurusScreenState.Success -> currentState.rhymeFilter
        is ThesaurusScreenState.NotFound -> currentState.rhymeFilter
        else -> null
    }
    if (showRhymeFilterDialog) {
        TextInputDialog(
            title = stringResource(R.string.filter_title),
            message = stringResource(R.string.filter_thesaurus_message),
            currentTextInput = currentRhymeFilter,
            onSubmit = { input ->
                viewModel.onRhymeFilterSubmitted(input)
                showRhymeFilterDialog = false
            },
            onDismiss = { showRhymeFilterDialog = false },
        )
    }
}

