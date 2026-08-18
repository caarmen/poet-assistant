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

package ca.rmen.android.poetassistant.main.favorites.composables

import android.content.ClipData
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.favorites.FavoritesScreenViewModel
import ca.rmen.android.poetassistant.main.favorites.OpenExternalAppUseCase
import ca.rmen.android.poetassistant.main.favorites.ShareUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

const val CONFIRM_DELETE_DIALOG_CONFIRM_BUTTON_TAG = "FavoritesScreen_ConfirmDeleteDialog_ConfirmButton"
@Composable
fun FavoritesScreen(
    viewModel: FavoritesScreenViewModel,
    shareUseCase: ShareUseCase,
    openExternalAppUseCase: OpenExternalAppUseCase,
    onSearchInTab: (String, Tab) -> Unit,
    onSnackbarText: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val favorites by viewModel.favorites.collectAsStateWithLifecycle(emptyList())
    val layout by viewModel.layout.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val snackbarResId by viewModel.snackbarTextResId.collectAsStateWithLifecycle(null)
    val snackbarText = snackbarResId?.let { stringResource(it) }

    val shareState by viewModel.share.collectAsStateWithLifecycle(null)

    val openConfirmDeleteDialog = remember { mutableStateOf(false) }
    LaunchedEffect(shareState) {
        shareState?.let {
            shareUseCase( it, context)
            viewModel.onShareSent()
        }
    }

    LaunchedEffect(snackbarResId) {
        snackbarText?.let {
            onSnackbarText(it)
        }
        // Hardcode this duration for now, while we have a mixed integration between views
        // and compose for snackbar. This value comes from views SnackbarManager.LENGTH_LONG
        delay(2750.milliseconds)
        viewModel.onSnackbarShown()
    }


    FavoritesScreenContent(
        favorites = favorites,
        layout = layout,
        externalAppMenuItemsProducer = { word -> viewModel.getExternalAppMenuItems(word) },
        onToggleFavorite = { word -> viewModel.onToggleFavorite(word) },
        onCopy = { word ->
            coroutineScope.launch {
                clipboard.setClipEntry(
                    ClipEntry(ClipData.newPlainText(word, word)))
                viewModel.onCopiedText()
            }
        },
        onSearchInTab = onSearchInTab,
        onDeleteAll = {
            openConfirmDeleteDialog.value = true
        },
        onExternalAppSelected = { word, menuItem ->
            openExternalAppUseCase(word, menuItem, context)
        },
        modifier = modifier
    )

    if (openConfirmDeleteDialog.value) {
        AlertDialog(
            text = {
                Text(stringResource(R.string.action_clear_favorites))
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDeleteAll()
                    openConfirmDeleteDialog.value = false
                }, modifier=Modifier.testTag(CONFIRM_DELETE_DIALOG_CONFIRM_BUTTON_TAG)) {
                    Text(stringResource(R.string.action_clear))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    openConfirmDeleteDialog.value = false
                }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            onDismissRequest = {
                openConfirmDeleteDialog.value = false
            }
        )
    }
}
