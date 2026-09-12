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

package ca.rmen.android.poetassistant.main.dictionaries.dictionary.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryScreenState

/**
 * Content composable for the Dictionary screen.
 * Displays the header and dictionary entry definitions based on the current state.
 *
 * @param state The current dictionary screen state.
 * @param onToggleFavorite Callback when the favorite star is clicked.
 * @param onSpeakWord Callback when the play button is clicked.
 * @param onSearchWeb Callback when the web search button is clicked.
 * @param modifier The modifier for this composable.
 */
@Composable
fun DictionaryScreenContent(
    state: DictionaryScreenState,
    onToggleFavorite: (String, Boolean) -> Unit,
    onSpeakWord: (String) -> Unit,
    onSearchWeb: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        when (state) {
            DictionaryScreenState.Idle -> {
                EmptyListWithoutQuery(modifier = Modifier.fillMaxWidth())
            }
            is DictionaryScreenState.NotFound -> {
                Text(
                    text = stringResource(R.string.empty_dictionary_list_with_query, state.query),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            is DictionaryScreenState.Success -> {
                DictionaryHeader(
                    word = state.entry.word,
                    isFavorite = state.isFavorite,
                    onToggleFavorite = { onToggleFavorite(state.entry.word, it) },
                    onSpeakWord = { onSpeakWord(state.entry.word) },
                    onSearchWeb = { onSearchWeb(state.entry.word) }
                )
                HorizontalDivider()
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    items(state.entry.details) { detail ->
                        Definition(
                            partOfSpeech = detail.partOfSpeech,
                            definition = detail.definition,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
