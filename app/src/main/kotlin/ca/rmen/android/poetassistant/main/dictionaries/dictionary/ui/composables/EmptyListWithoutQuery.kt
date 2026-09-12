/*
 * Copyright (c) 2026 Carmen Alvarez
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

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import ca.rmen.android.poetassistant.R

/**
 * Display a message indicating that the list is empty and no search query has been entered.
 *
 * @param modifier The modifier for this composable.
 */
@Composable
fun EmptyListWithoutQuery(
    modifier: Modifier = Modifier
) {
    val fullText = stringResource(R.string.empty_list_without_query, "%s")
    val parts = fullText.split("%s")
    val text = buildAnnotatedString {
        append(parts[0])
        appendInlineContent(id = "searchIcon", alternateText = "[icon]")
        append(parts[1])
    }
    val inlineContent = mapOf(
        "searchIcon" to InlineTextContent(
            placeholder = Placeholder(
                width=1.2.em, height=1.2.em,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
            )
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.action_search)
            )
        }
    )
    Text(
        text = text,
        inlineContent = inlineContent,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier.padding(16.dp)
    )
}
