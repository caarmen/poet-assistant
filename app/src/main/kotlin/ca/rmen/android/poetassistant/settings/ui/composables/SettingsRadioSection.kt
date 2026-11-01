/*
 * Copyright (c) 2016 - present Carmen Alvarez
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

package ca.rmen.android.poetassistant.settings.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * One selectable choice of a [SettingsRadioSection].
 *
 * @param value The choice value, written through the repository when
 * selected.
 * @param label The choice label shown in the row and the dialog.
 */
data class SettingsChoice<T>(val value: T, val label: String)

/**
 * A settings row with radio-button choices: the row shows the title
 * and the selected entry; the tap opens a dialog with the radio
 * list. Used for the theme and layout choices.
 *
 * @param title The row title.
 * @param choices The available choices.
 * @param selectedEntry The currently selected value.
 * @param onEntrySelected Called with the chosen value.
 * @param testTag Tag for UI tests, applied to the row.
 * @param modifier The modifier for this composable.
 */
@Composable
fun <T> SettingsRadioSection(
    title: String,
    choices: List<SettingsChoice<T>>,
    selectedEntry: T,
    onEntrySelected: (T) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    var openDialog by remember { mutableStateOf(false) }
    val selectedLabel = choices.firstOrNull { it.value == selectedEntry }?.label.orEmpty()
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(selectedLabel) },
        modifier = modifier
            .testTag(testTag)
            .clickable { openDialog = true },
    )
    if (openDialog) {
        AlertDialog(
            onDismissRequest = { openDialog = false },
            title = { Text(title) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    choices.forEach { choice ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = choice.value == selectedEntry,
                                    role = Role.RadioButton,
                                    onClick = {
                                        onEntrySelected(choice.value)
                                        openDialog = false
                                    },
                                )
                                // A bit of breathing room between the
                                // choices.
                                .padding(vertical = 6.dp),
                        ) {
                            RadioButton(
                                selected = choice.value == selectedEntry,
                                onClick = null,
                            )
                            Text(
                                text = choice.label,
                                modifier = Modifier.padding(start = 12.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {},
        )
    }
}
