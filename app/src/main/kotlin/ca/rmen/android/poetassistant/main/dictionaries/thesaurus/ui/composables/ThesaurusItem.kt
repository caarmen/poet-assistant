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

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.common.models.ExternalAppMenuItem
import ca.rmen.android.poetassistant.main.common.ui.composables.Rtd
import ca.rmen.android.poetassistant.main.common.ui.composables.WordPopupMenu
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusListItem
import ca.rmen.android.poetassistant.settings.Layout
import kotlinx.coroutines.launch

const val THESAURUS_ITEM_STAR_TAG = "ThesaurusItem_Star_"
const val THESAURUS_ITEM_ROW_TAG = "ThesaurusItem_Row_"

/**
 * One item of the thesaurus result list: a heading, a subheading, or a
 * result word.
 *
 * @param item The list item to render.
 * @param layout The current layout setting.
 * @param externalAppMenuItemsProducer Produces external app menu items for a word.
 * @param onSetFavorite Callback to request a change of the favorite status.
 * @param onCopy Callback to request copying a word to the clipboard.
 * @param onSearchInTab Callback to request searching the word in another tab.
 * @param onExternalAppSelected Callback to request opening the word in an external app.
 * @param rowBackgroundColor The alternating row background, if enabled.
 * @param modifier The modifier for this composable.
 */
@Composable
fun ThesaurusItem(
    item: ThesaurusListItem,
    layout: Layout,
    externalAppMenuItemsProducer: suspend (String) -> List<ExternalAppMenuItem>,
    onSetFavorite: (String, Boolean) -> Unit,
    onCopy: (String) -> Unit,
    onSearchInTab: (String, Tab) -> Unit,
    onExternalAppSelected: (String, ExternalAppMenuItem) -> Unit,
    rowBackgroundColor: Color?,
    modifier: Modifier = Modifier,
) {
    when (item) {
        is ThesaurusListItem.Heading -> Text(
            text = item.text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        is ThesaurusListItem.Subheading -> Text(
            text = item.text,
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        is ThesaurusListItem.Word -> ThesaurusWordItem(
            word = item.word,
            isFavorite = item.isFavorite,
            layout = layout,
            externalAppMenuItemsProducer = externalAppMenuItemsProducer,
            onSetFavorite = onSetFavorite,
            onCopy = onCopy,
            onSearchInTab = onSearchInTab,
            onExternalAppSelected = onExternalAppSelected,
            rowBackgroundColor = rowBackgroundColor,
            modifier = modifier,
        )
    }
}

/**
 * A result word row: a star toggle, the word, and the R/T/D buttons in the
 * efficient layout.
 */
@Composable
private fun ThesaurusWordItem(
    word: String,
    isFavorite: Boolean,
    layout: Layout,
    externalAppMenuItemsProducer: suspend (String) -> List<ExternalAppMenuItem>,
    onSetFavorite: (String, Boolean) -> Unit,
    onCopy: (String) -> Unit,
    onSearchInTab: (String, Tab) -> Unit,
    onExternalAppSelected: (String, ExternalAppMenuItem) -> Unit,
    rowBackgroundColor: Color?,
    modifier: Modifier = Modifier,
) {
    // Keyed on the word: the popup state cannot attach to the wrong
    // word if the list composition changes.
    var showPopupMenu by remember(word) { mutableStateOf(false) }
    var externalAppMenuItems by remember(word) { mutableStateOf<List<ExternalAppMenuItem>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    val rowModifier = if (rowBackgroundColor != null) {
        modifier.background(rowBackgroundColor)
    } else {
        modifier
    }

    Row(
        modifier = rowModifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp)
            .clickable(onClick = {
                coroutineScope.launch {
                    externalAppMenuItems = externalAppMenuItemsProducer(word)
                    showPopupMenu = true
                }
            })
            .testTag("${THESAURUS_ITEM_ROW_TAG}$word"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconToggleButton(
            checked = isFavorite,
            onCheckedChange = { onSetFavorite(word, it) },
            modifier = Modifier
                .size(40.dp)
                .testTag("${THESAURUS_ITEM_STAR_TAG}$word"),
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = stringResource(R.string.content_description_toggle_favorite),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        ) {
            Text(
                text = word,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            // DropdownMenu anchored to the word text
            WordPopupMenu(
                expanded = showPopupMenu,
                layout = layout,
                externalAppMenuItems = externalAppMenuItems,
                onDismiss = { showPopupMenu = false },
                onCopy = {
                    onCopy(word)
                    showPopupMenu = false
                },
                onSearchInTab = { tab ->
                    onSearchInTab(word, tab)
                    showPopupMenu = false
                },
                onExternalAppSelected = { onExternalAppSelected(word, it) },
            )
        }

        // R/T/D icons for EFFICIENT layout
        if (layout == Layout.EFFICIENT) {
            Rtd(
                word = word,
                onSearchInTab = { tab -> onSearchInTab(word, tab) },
            )
        }
    }
}
