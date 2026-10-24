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

package ca.rmen.android.poetassistant.main.common.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource

const val TEXT_INPUT_DIALOG_TEXT = "TextInput_DialogText"
/**
 * A text input dialog.
 */
@Composable
fun TextInputDialog(
    title: String,
    message: String,
    currentTextInput: String?,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var textInput by remember(currentTextInput) { mutableStateOf(currentTextInput ?: "") }
    // Focus the input when the dialog opens.
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    AlertDialog(
        title = {
            Text(title)
        },
        text = {
            // The dialog message is shown above the text field.
            Column {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag(TEXT_INPUT_DIALOG_TEXT),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(textInput) }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        onDismissRequest = onDismiss,
    )
}
