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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
fun FavoritesScreenContent(
    favorites: List<String>,
    layout: Layout,
    onToggleFavorite: (String) -> Unit,
    onCopy: (String) -> Unit,
    onSearchInTab: (String, Tab) -> Unit,
    onDeleteAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        FavoritesHeader(
            onDeleteAll = onDeleteAll,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Divider below header (matching legacy fragment_result_list.xml)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
        
        if (favorites.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize().padding(bottom=56.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.empty_favorites_list),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(favorites, key = {it}) { word ->
                    FavoriteItem(
                        word = word,
                        layout = layout,
                        onToggleFavorite = { onToggleFavorite(word) },
                        onCopy = { onCopy(word) },
                        onSearchInTab = { tab -> onSearchInTab(word, tab) },
                        modifier = Modifier.fillMaxWidth().animateItem()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FavoritesScreenContentPreview() {
    AppTheme {
        FavoritesScreenContent(
            favorites = listOf("apple", "banana", "cherry"),
            layout = Layout.CLEAN,
            onToggleFavorite = {},
            onCopy = {},
            onSearchInTab = { _, _ -> },
            onDeleteAll = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FavoritesScreenContentEmptyPreview() {
    AppTheme {
        FavoritesScreenContent(
            favorites = emptyList(),
            layout = Layout.CLEAN,
            onToggleFavorite = {},
            onCopy = {},
            onSearchInTab = { _, _ -> },
            onDeleteAll = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
