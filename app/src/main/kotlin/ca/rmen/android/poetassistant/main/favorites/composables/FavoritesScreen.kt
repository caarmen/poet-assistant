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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.favorites.FavoritesScreenViewModel
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    viewModel: FavoritesScreenViewModel,
    onSearchInTab: (String, Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboard.current
    val favorites by viewModel.favorites.collectAsStateWithLifecycle(emptyList())
    val layout by viewModel.layout.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    FavoritesScreenContent(
        favorites = favorites,
        layout = layout,
        onToggleFavorite = { word -> viewModel.onToggleFavorite(word) },
        onCopy = { word ->
            coroutineScope.launch {
                clipboard.setClipEntry(
                    ClipEntry(ClipData.newPlainText(word, word)))
            }
        },
        onSearchInTab = onSearchInTab,
        onDeleteAll = { viewModel.onDeleteAll() },
        modifier = modifier
    )
}
