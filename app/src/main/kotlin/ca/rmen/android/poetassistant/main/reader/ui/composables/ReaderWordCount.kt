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

package ca.rmen.android.poetassistant.main.reader.ui.composables

import android.content.res.Resources
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.reader.WordCount

const val READER_WORD_COUNT_TAG = "Reader_Word_Count"

/**
 * The word count text: the word and character counts joined with the
 * reader_word_char_count string.
 *
 * @param resources The resources for the quantity strings.
 * @param wordCount The word and character counts.
 * @return the formatted word count text.
 */
fun formatWordCount(resources: Resources, wordCount: WordCount): String {
    val words = resources.getQuantityString(R.plurals.reader_word_count, wordCount.words, wordCount.words)
    val characters = resources.getQuantityString(R.plurals.reader_char_count, wordCount.characters, wordCount.characters)
    return resources.getString(R.string.reader_word_char_count, words, characters)
}

/**
 * The word and character count of the poem. Hidden when the count is
 * null. Clicking opens the word count help dialog.
 *
 * @param wordCount The word and character counts, or null to hide.
 * @param onClick Called when the count is clicked.
 * @param modifier The modifier for this composable.
 */
@Composable
fun ReaderWordCount(
    wordCount: WordCount?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (wordCount != null) {
        Text(
            text = formatWordCount(LocalContext.current.resources, wordCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
                .testTag(READER_WORD_COUNT_TAG)
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
        )
    }
}
