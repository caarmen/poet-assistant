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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.R

/**
 * Header composable for the Dictionary screen.
 * Displays the word with star icon next to it, plus action buttons on the right.
 *
 * @param word The word being displayed.
 * @param isFavorite Whether the word is a favorite.
 * @param onToggleFavorite Callback when the favorite star is clicked.
 * @param onSpeakWord Callback when the play button is clicked.
 * @param onSearchWeb Callback when the web search button is clicked.
 * @param modifier The modifier for this composable.
 */
@Composable
fun DictionaryHeader(
    word: String,
    isFavorite: Boolean,
    onToggleFavorite: (Boolean) -> Unit,
    onSpeakWord: () -> Unit,
    onSearchWeb: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Word text with star icon next to it
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = word,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            IconToggleButton(
                checked = isFavorite,
                onCheckedChange = { onToggleFavorite(it) },
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = stringResource(R.string.content_description_toggle_favorite),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Action buttons on the right
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Web search button
            IconButton(onClick = onSearchWeb) {
                Image(
                    painter = painterResource(R.drawable.ic_web_search),
                    contentDescription = stringResource(R.string.action_search)
                )
            }

            // Play button for TTS
            IconButton(onClick = onSpeakWord) {
                Image(
                    painter = painterResource(R.drawable.ic_play_circle),
                    contentDescription = stringResource(R.string.tts_play)
                )
            }
        }
    }
}

