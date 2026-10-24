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

package ca.rmen.android.poetassistant.main.reader.ui.composables.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.TextFieldValue
import ca.rmen.android.poetassistant.main.reader.PlayButtonState
import ca.rmen.android.poetassistant.main.reader.ReaderScreenState
import ca.rmen.android.poetassistant.main.reader.WordCount
import ca.rmen.android.poetassistant.main.reader.ui.composables.ReaderScreenContent
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
@Preview(showBackground = true)
fun ReaderScreenContentPreview() {
    AppTheme {
        ReaderScreenContent(
            state = ReaderScreenState(
                textFieldValue = TextFieldValue("Roses are red.\nViolets are blue."),
                playButtonState = PlayButtonState.PLAY_ENABLED,
                wordCount = WordCount(words = 6, characters = 31),
                poem = null,
            ),
            onPoemEdited = {},
            onPlayClicked = {},
            onWordCountClicked = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
fun ReaderScreenContentEmptyPreview() {
    AppTheme {
        ReaderScreenContent(
            state = ReaderScreenState(
                textFieldValue = TextFieldValue(""),
                playButtonState = PlayButtonState.PLAY_DISABLED,
                wordCount = null,
                poem = null,
            ),
            onPoemEdited = {},
            onPlayClicked = {},
            onWordCountClicked = {},
        )
    }
}
