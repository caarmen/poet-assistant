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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
fun FavoritesPopupMenu(
    expanded: Boolean,
    layout: Layout,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onSearchInTab: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier.width(200.dp)
    ) {
        // For CLEAN layout, show R/T/D in popup before Copy
        if (layout == Layout.CLEAN) {
            DropdownMenuItem(
                leadingIcon = {
                    Image(
                        painter = painterResource(R.drawable.ic_rhymer),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                text = { Text(stringResource(R.string.tab_rhymer)) },
                onClick = {
                    onSearchInTab(Tab.RHYMER)
                    onDismiss()
                }
            )
            
            DropdownMenuItem(
                leadingIcon = {
                    Image(
                        painter = painterResource(R.drawable.ic_thesaurus),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                text = { Text(stringResource(R.string.tab_thesaurus)) },
                onClick = {
                    onSearchInTab(Tab.THESAURUS)
                    onDismiss()
                }
            )
            
            DropdownMenuItem(
                leadingIcon = {
                    Image(
                        painter = painterResource(R.drawable.ic_dictionary),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                text = { Text(stringResource(R.string.tab_dictionary)) },
                onClick = {
                    onSearchInTab(Tab.DICTIONARY)
                    onDismiss()
                }
            )
        }
        
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            text = { Text(stringResource(R.string.menu_copy)) },
            onClick = {
                onCopy()
                onDismiss()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FavoritesPopupMenuPreview() {
    AppTheme {
        FavoritesPopupMenu(
            expanded = true,
            layout = Layout.CLEAN,
            onDismiss = {},
            onCopy = {},
            onSearchInTab = {},
            modifier = Modifier.width(200.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FavoritesPopupMenuEfficientPreview() {
    AppTheme {
        FavoritesPopupMenu(
            expanded = true,
            layout = Layout.EFFICIENT,
            onDismiss = {},
            onCopy = {},
            onSearchInTab = {},
            modifier = Modifier.width(200.dp)
        )
    }
}
