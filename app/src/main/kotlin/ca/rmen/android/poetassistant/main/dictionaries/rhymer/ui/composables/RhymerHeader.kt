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

package ca.rmen.android.poetassistant.main.dictionaries.rhymer.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.common.ui.composables.ActiveFilterChip

// TODO iconsistent undersscore
const val RHYMER_HEADER_TEXT_TAG = "Rhymer_Header_Text"
const val RHYMER_HEADER_FILTER_TAG = "RhymerHeader_Filter"
const val RHYMER_HEADER_STAR_TAG = "RhymerHeader_Star"

/**
 * Header for the rhymer screen: the matched word with its favorite star,
 * play and web-search buttons, the filter button, and the active filter
 * chip.
 *
 * @param word The matched word.
 * @param isFavorite Whether the matched word is a favorite.
 * @param synonymFilter The active synonym filter, if any.
 * @param onToggleFavorite Callback to request a change of the favorite status.
 * @param onSpeakWord Callback to request speaking the word.
 * @param onSearchWeb Callback to request a web search for the word.
 * @param onSynonymFilter Callback to request opening the filter dialog.
 * @param onClearSynonymFilter Callback to request clearing the filter.
 * @param modifier The modifier for this composable.
 */
@Composable
fun RhymerHeader(
    word: String,
    isFavorite: Boolean,
    synonymFilter: String?,
    onToggleFavorite: (Boolean) -> Unit,
    onSpeakWord: () -> Unit,
    onSearchWeb: () -> Unit,
    onSynonymFilter: () -> Unit,
    onClearSynonymFilter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SelectionContainer {
                    Text(
                        text = word,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag(RHYMER_HEADER_TEXT_TAG),
                    )
                }
                IconToggleButton(
                    checked = isFavorite,
                    onCheckedChange = onToggleFavorite,
                    modifier = Modifier.testTag(RHYMER_HEADER_STAR_TAG),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = stringResource(R.string.content_description_toggle_favorite),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            IconButton(onClick = onSynonymFilter, modifier = Modifier.testTag(RHYMER_HEADER_FILTER_TAG)) {
                Image(
                    painter = painterResource(R.drawable.ic_filter_list),
                    contentDescription = stringResource(R.string.filter_hint),
                )
            }
            IconButton(onClick = onSearchWeb) {
                Image(
                    painter = painterResource(R.drawable.ic_web_search),
                    contentDescription = stringResource(R.string.action_search),
                )
            }
            IconButton(onClick = onSpeakWord) {
                Image(
                    painter = painterResource(R.drawable.ic_play_circle),
                    contentDescription = stringResource(R.string.tts_play),
                )
            }
        }
        if (synonymFilter != null) {
            ActiveFilterChip(
                title = stringResource(R.string.filter_rhymer_label),
                filter = synonymFilter,
                onFilter = onSynonymFilter,
                onClearFilter = onClearSynonymFilter,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
            )
        }
    }
}
