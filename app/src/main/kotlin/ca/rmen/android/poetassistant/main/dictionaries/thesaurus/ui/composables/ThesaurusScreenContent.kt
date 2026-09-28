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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.common.models.ExternalAppMenuItem
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.ui.composables.EmptyListWithoutQuery
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusScreenState
import ca.rmen.android.poetassistant.settings.Layout

const val THESAURUS_SCREEN_CONTENT_EMPTY_TAG = "ThesaurusScreen_Empty"
const val THESAURUS_SCREEN_CONTENT_LIST_TAG = "ThesaurusScreen_List"

/**
 * Content of the thesaurus screen, based on the current state.
 *
 * @param state The current thesaurus screen state.
 * @param layout The current layout setting.
 * @param externalAppMenuItemsProducer Produces external app menu items for a word.
 * @param onSetFavorite Callback to request a change of the favorite status.
 * @param onSpeakWord Callback to request speaking a word.
 * @param onSearchWeb Callback to request a web search for a word.
 * @param onCopy Callback to request copying a word to the clipboard.
 * @param onFilter Callback to request opening the filter dialog.
 * @param onClearFilter Callback to request clearing the filter.
 * @param onSearchInTab Callback to request searching a word in another tab.
 * @param onExternalAppSelected Callback to request opening a word in an external app.
 * @param modifier The modifier for this composable.
 */
@Composable
fun ThesaurusScreenContent(
    state: ThesaurusScreenState,
    layout: Layout,
    externalAppMenuItemsProducer: suspend (String) -> List<ExternalAppMenuItem>,
    onSetFavorite: (String, Boolean) -> Unit,
    onSpeakWord: (String) -> Unit,
    onSearchWeb: (String) -> Unit,
    onCopy: (String) -> Unit,
    onFilter: () -> Unit,
    onClearFilter: () -> Unit,
    onSearchInTab: (String, Tab) -> Unit,
    onExternalAppSelected: (String, ExternalAppMenuItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val alternatingColorsEnabled = booleanResource(R.bool.enable_row_alternating_colors)

    when (state) {
        ThesaurusScreenState.Idle -> EmptyListWithoutQuery(
            modifier = modifier.testTag(THESAURUS_SCREEN_CONTENT_EMPTY_TAG),
        )

        is ThesaurusScreenState.NotFound -> Column(modifier = modifier.fillMaxWidth()) {
            ThesaurusHeader(
                word = state.word,
                isFavorite = state.isFavorite,
                filter = state.filter,
                onToggleFavorite = { onSetFavorite(state.word, it) },
                onSpeakWord = { onSpeakWord(state.word) },
                onSearchWeb = { onSearchWeb(state.word) },
                onFilter = onFilter,
                onClearFilter = onClearFilter,
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
            )
            HorizontalDivider()
            Box(
                modifier = Modifier.fillMaxSize().padding(bottom = 56.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.empty_thesaurus_list_with_query, state.word),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        is ThesaurusScreenState.Success -> LazyColumn(
            modifier = modifier.fillMaxWidth().testTag(THESAURUS_SCREEN_CONTENT_LIST_TAG),
        ) {
            stickyHeader {
                Column {
                    ThesaurusHeader(
                        word = state.word,
                        isFavorite = state.isFavorite,
                        filter = state.filter,
                        onToggleFavorite = { onSetFavorite(state.word, it) },
                        onSpeakWord = { onSpeakWord(state.word) },
                        onSearchWeb = { onSearchWeb(state.word) },
                        onFilter = onFilter,
                        onClearFilter = onClearFilter,
                        modifier = Modifier.background(MaterialTheme.colorScheme.background),
                    )
                    HorizontalDivider()
                }
            }
            itemsIndexed(state.entries) { index, item ->
                // The alternating row background uses the position in the
                // full item list: headings and subheadings count toward it.
                val rowBackgroundColor = if (alternatingColorsEnabled) {
                    colorResource(
                        if (index % 2 == 0) R.color.row_background_color_even else R.color.row_background_color_odd
                    )
                } else {
                    null
                }
                ThesaurusItem(
                    item = item,
                    layout = layout,
                    externalAppMenuItemsProducer = externalAppMenuItemsProducer,
                    onSetFavorite = onSetFavorite,
                    onCopy = onCopy,
                    onSearchInTab = onSearchInTab,
                    onExternalAppSelected = onExternalAppSelected,
                    rowBackgroundColor = rowBackgroundColor,
                )
            }
        }
    }
}
