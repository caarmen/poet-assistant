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

package ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.common.models.ExternalAppMenuItem
import ca.rmen.android.poetassistant.main.dictionaries.patterns.PatternScreenState
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.settings.Layout

const val PATTERN_SCREEN_CONTENT_MAX_RESULTS_TAG = "PatternScreenContent_MaxResults"
const val PATTERNS_SCREEN_CONTENT_EMPTY_TAG = "PatternScreenContent_Empty"
const val PATTERNS_SCREEN_CONTENT_LIST_TAG = "PatternScreenContent_List"

@Composable
fun PatternScreenContent(
    state: PatternScreenState,
    layout: Layout,
    externalAppMenuItemsProducer: suspend (String) -> List<ExternalAppMenuItem>,
    onSetFavorite: (String, Boolean) -> Unit,
    onCopy: (String) -> Unit,
    onSearchInTab: (String, Tab) -> Unit,
    onExternalAppSelected: (String, ExternalAppMenuItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showHelp by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        when (state) {
            PatternScreenState.Idle -> {}
            is PatternScreenState.NotFound -> {
                PatternHeader(
                    pattern = state.query,
                    onHelp = {showHelp = true},
                )
                HorizontalDivider()
                Box(
                    modifier = Modifier.fillMaxSize().padding(bottom = 56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.empty_pattern_list_with_query, state.query),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag(PATTERNS_SCREEN_CONTENT_EMPTY_TAG)
                    )
                }
            }
            is PatternScreenState.Success -> {
                PatternHeader(
                    pattern = state.query,
                    onHelp = {showHelp = true},
                )
                HorizontalDivider()
                if (state.isCapped) {
                    Text(
                        text = stringResource(R.string.max_results, Constants.MAX_RESULTS),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .testTag(PATTERN_SCREEN_CONTENT_MAX_RESULTS_TAG)
                    )
                }
                LazyColumn(modifier = Modifier.fillMaxWidth().testTag(PATTERNS_SCREEN_CONTENT_LIST_TAG)) {
                    items(state.entries, key = {"${it.word}-${it.isFavorite}"}) { entry ->
                        PatternItem(
                            word = entry.word,
                            isFavorite = entry.isFavorite,
                            layout = layout,
                            externalAppMenuItemsProducer = externalAppMenuItemsProducer,
                            onToggleFavorite = { newIsFavorite -> onSetFavorite(entry.word, newIsFavorite) },
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
    if (showHelp) {
        PatternHelpDialog(onDismiss = { showHelp = false })
    }
}
