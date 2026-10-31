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

package ca.rmen.android.poetassistant.main.settings.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ca.rmen.android.poetassistant.R

// Test tags: the legacy preference keys, so the migrated tests address
// the rows as they addressed the preferences.
const val SETTINGS_EXPORT_FAVORITES_ROW_TAG = "PREF_EXPORT_FAVORITES"
const val SETTINGS_IMPORT_FAVORITES_ROW_TAG = "PREF_IMPORT_FAVORITES"
const val SETTINGS_CLEAR_SEARCH_HISTORY_ROW_TAG = "PREF_CLEAR_SEARCH_HISTORY"
const val SETTINGS_CLEAR_SEARCH_HISTORY_CONFIRM_BUTTON_TAG = "PREF_CLEAR_SEARCH_HISTORY_CONFIRM"

/**
 * The user data section: export and import favorites (through the
 * system document pickers, launched by the host screen), and clear
 * search history behind a confirm dialog, with the same strings as
 * the legacy ConfirmDialogFragment.
 */
@Composable
fun SettingsUserDataSection(
    onExportFavoritesClicked: () -> Unit,
    onImportFavoritesClicked: () -> Unit,
    onClearSearchHistoryConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.action_export_favorites)) },
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_export_favorites),
                    contentDescription = null,
                )
            },
            modifier = Modifier
                .testTag(SETTINGS_EXPORT_FAVORITES_ROW_TAG)
                .clickable { onExportFavoritesClicked() },
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.action_import_favorites)) },
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_import_favorites),
                    contentDescription = null,
                )
            },
            modifier = Modifier
                .testTag(SETTINGS_IMPORT_FAVORITES_ROW_TAG)
                .clickable { onImportFavoritesClicked() },
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.action_clear_search_history)) },
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_clear_search_history),
                    contentDescription = null,
                )
            },
            modifier = Modifier
                .testTag(SETTINGS_CLEAR_SEARCH_HISTORY_ROW_TAG)
                .clickable { showConfirmDialog = true },
        )
    }
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(R.string.confirm_clear_search_history)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        onClearSearchHistoryConfirmed()
                    },
                    modifier = Modifier.testTag(SETTINGS_CLEAR_SEARCH_HISTORY_CONFIRM_BUTTON_TAG),
                ) {
                    Text(stringResource(R.string.action_clear))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
}
