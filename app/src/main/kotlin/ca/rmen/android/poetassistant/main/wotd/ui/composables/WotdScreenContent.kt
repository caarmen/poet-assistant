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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.common.models.ExternalAppMenuItem
import ca.rmen.android.poetassistant.main.wotd.WotdScreenState
import ca.rmen.android.poetassistant.settings.Layout

const val WOTD_SCREEN_CONTENT_EMPTY_TAG = "WotdScreen_Empty"
const val WOTD_SCREEN_CONTENT_LIST_TAG = "WotdScreen_List"

/**
 * Content of the WOTD history screen: header, empty state, and history list.
 *
 * @param state The current screen state.
 * @param layout The current layout setting.
 * @param externalAppMenuItemsProducer Produces external app menu items for a word.
 * @param onSetFavorite Callback to request a change of the favorite status of a word.
 * @param onCopy Callback to request copying a word to the clipboard.
 * @param onSearchInTab Callback to request searching a word in another tab.
 * @param onExternalAppSelected Callback to request opening a word in an external app.
 * @param modifier The modifier for this composable.
 */
@Composable
fun WotdScreenContent(
    state: WotdScreenState,
    layout: Layout,
    externalAppMenuItemsProducer: suspend (String) -> List<ExternalAppMenuItem>,
    onSetFavorite: (String, Boolean) -> Unit,
    onCopy: (String) -> Unit,
    onSearchInTab: (String, Tab) -> Unit,
    onExternalAppSelected: (String, ExternalAppMenuItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        WotdHeader(
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider()
        when (state) {
            WotdScreenState.Idle -> {}
            is WotdScreenState.Success -> {
                if (state.entries.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize().padding(bottom = 56.dp).testTag(WOTD_SCREEN_CONTENT_EMPTY_TAG),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.empty_wotd_list),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().testTag(WOTD_SCREEN_CONTENT_LIST_TAG)
                    ) {
                        // The same word can be the word of the day on several days.
                        // The date is unique per row (100 consecutive days), so
                        // keying on date and word keeps LazyColumn keys unique.
                        items(state.entries, key = { "${it.date}_${it.word}" }) { entry ->
                            WotdItem(
                                word = entry.word,
                                date = entry.date,
                                isFavorite = entry.isFavorite,
                                layout = layout,
                                externalAppMenuItemsProducer = externalAppMenuItemsProducer,
                                onSetFavorite = { isFavorite -> onSetFavorite(entry.word, isFavorite) },
                                onCopy = { onCopy(entry.word) },
                                onSearchInTab = { tab -> onSearchInTab(entry.word, tab) },
                                onExternalAppSelected = onExternalAppSelected,
                                modifier = Modifier.fillMaxWidth().animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

