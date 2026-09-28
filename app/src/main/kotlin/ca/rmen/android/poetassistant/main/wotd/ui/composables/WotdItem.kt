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

package ca.rmen.android.poetassistant.main.wotd.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.common.models.ExternalAppMenuItem
import ca.rmen.android.poetassistant.main.common.ui.composables.Rtd
import ca.rmen.android.poetassistant.main.common.ui.composables.WordPopupMenu
import ca.rmen.android.poetassistant.settings.Layout
import kotlinx.coroutines.launch

const val WOTD_ITEM_STAR_TAG = "WotdItem_Star_"
const val WOTD_ITEM_ROW_TAG = "WotdItem_Row_"

/**
 * One row of the WOTD history: a star toggle, the date and the word,
 * and the R/T/D buttons in the efficient layout.
 *
 * @param word The word of the day.
 * @param date The pre-formatted display date.
 * @param isFavorite Whether the word is marked as a favorite.
 * @param layout The current layout setting.
 * @param externalAppMenuItemsProducer Produces external app menu items for the word.
 * @param onSetFavorite Callback to request a change of the favorite status.
 * @param onCopy Callback to request copying the word to the clipboard.
 * @param onSearchInTab Callback to request searching the word in another tab.
 * @param onExternalAppSelected Callback to request opening the word in an external app.
 * @param modifier The modifier for this composable.
 */
@Composable
fun WotdItem(
    word: String,
    date: String,
    isFavorite: Boolean,
    layout: Layout,
    externalAppMenuItemsProducer: suspend (String) -> List<ExternalAppMenuItem>,
    onSetFavorite: (Boolean) -> Unit,
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
            .height(64.dp)
            .padding(horizontal = 16.dp)
            .clickable(onClick = {
                coroutineScope.launch {
                    externalAppMenuItems = externalAppMenuItemsProducer(word)
                    showPopupMenu = true
                }
            })
            // The date suffix keeps the tag unique when a word repeats on several days
            .testTag("${WOTD_ITEM_ROW_TAG}${word}_$date"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Star icon on the left - clickable to add or remove from favorites
        IconToggleButton(
            checked = isFavorite,
            onCheckedChange = { onSetFavorite(it) },
            modifier = Modifier
                .size(40.dp)
                .testTag("$WOTD_ITEM_STAR_TAG${word}_$date")
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = stringResource(R.string.content_description_toggle_favorite),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Column {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = word,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            // DropdownMenu anchored to the word texts
            WordPopupMenu(
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
                onExternalAppSelected = { onExternalAppSelected(word, it) }
            )
        }

        // R/T/D icons for EFFICIENT layout
        if (layout == Layout.EFFICIENT) {
            Rtd(
                word = word,
                onSearchInTab = onSearchInTab,
            )
        }
    }
}
