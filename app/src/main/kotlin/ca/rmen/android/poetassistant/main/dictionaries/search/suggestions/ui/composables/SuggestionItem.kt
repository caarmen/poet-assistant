/*
 * Copyright (c) 2025 - current Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries.search.suggestions.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.dictionaries.search.suggestions.SuggestionsViewModel.SearchSuggestion

const val SEARCH_SUGGESTION_ITEM_TAG = "Search_SuggestionItem_Tag"
/**
 * One row of the search suggestions dropdown: a leading icon
 * (history or dictionary) and the suggested word.
 *
 * @param suggestion The suggestion to display.
 * @param onClick Callback invoked with the suggested word when the row is clicked.
 * @param modifier The modifier for this composable.
 */
@Composable
fun SuggestionItem(
    suggestion: SearchSuggestion,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(suggestion.word) },
        leadingContent = {
            Icon(
                painter = painterResource(suggestion.iconResource),
                contentDescription = stringResource(R.string.content_description_icon_suggested_word),
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier.testTag(SEARCH_SUGGESTION_ITEM_TAG).clickable { onClick(suggestion.word) },
    )
}
