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

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.favorites.ExternalAppMenuItem
import ca.rmen.android.poetassistant.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun FavoriteItem(
    word: String,
    layout: Layout,
    externalAppMenuItemsProducer: suspend (String) -> List<ExternalAppMenuItem>,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit,
    onSearchInTab: (Tab) -> Unit,
    onExternalAppSelected: (String, ExternalAppMenuItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPopupMenu by remember { mutableStateOf(false) }
    var externalAppMenuItems by remember { mutableStateOf<List<ExternalAppMenuItem>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp)
            .clickable(onClick = {
                coroutineScope.launch {
                    externalAppMenuItems = externalAppMenuItemsProducer(word)
                    showPopupMenu = true
                }
            }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Star icon on the left - clickable to remove from favorites
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = stringResource(R.string.content_description_toggle_favorite),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {

            Text(
                text = word,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            // DropdownMenu anchored to the word Text
            FavoritesPopupMenu(
                expanded = showPopupMenu,
                layout = layout,
                externalAppMenuItems = externalAppMenuItems,
                onDismiss = { showPopupMenu = false },
                onCopy = {
                    onCopy()
                    showPopupMenu = false
                },
                onSearchInTab = { tab ->
                    onSearchInTab(tab)
                    showPopupMenu = false
                },
                onExternalAppSelected = {onExternalAppSelected(word, it)}
            )
        }

        // R/T/D icons for EFFICIENT layout
        if (layout == Layout.EFFICIENT) {
            Image(
                painter = painterResource(R.drawable.ic_rhymer),
                contentDescription = stringResource(R.string.tab_rhymer),
                modifier = Modifier
                    .size(44.dp)
                    .padding(horizontal = 4.dp)
                    .clickable(onClick = { onSearchInTab(Tab.RHYMER) }),
            )
            Image(
                painter = painterResource(R.drawable.ic_thesaurus),
                contentDescription = stringResource(R.string.tab_thesaurus),
                modifier = Modifier
                    .size(44.dp)
                    .padding(horizontal = 4.dp)
                    .clickable(onClick = { onSearchInTab(Tab.THESAURUS) }),
            )
            Image(
                painter = painterResource(R.drawable.ic_dictionary),
                contentDescription = stringResource(R.string.tab_dictionary),
                modifier = Modifier
                    .size(44.dp)
                    .padding(horizontal = 4.dp)
                    .clickable(onClick = { onSearchInTab(Tab.DICTIONARY) }),
            )
        }

    }

}

@Preview(showBackground = true)
@Composable
fun FavoriteItemPreview() {
    AppTheme {
        FavoriteItem(
            word = "Example",
            layout = Layout.CLEAN,
            externalAppMenuItemsProducer = { emptyList() },
            onToggleFavorite = {},
            onCopy = {},
            onSearchInTab = {},
            onExternalAppSelected = {_, _ -> },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FavoriteItemEfficientPreview() {
    AppTheme {
        FavoriteItem(
            word = "Example",
            layout = Layout.EFFICIENT,
            externalAppMenuItemsProducer = { emptyList() },
            onToggleFavorite = {},
            onCopy = {},
            onSearchInTab = {},
            onExternalAppSelected = {_, _ -> },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
