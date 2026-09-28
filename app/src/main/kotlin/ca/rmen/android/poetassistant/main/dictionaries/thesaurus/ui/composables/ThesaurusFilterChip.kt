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

package ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ca.rmen.android.poetassistant.R

/**
 * The active filter chip: the filter label and value. Tapping the chip
 * opens the filter dialog to edit the filter; tapping the clear button
 * clears it.
 *
 * @param filter The active filter value.
 * @param onFilter Callback to request opening the filter dialog.
 * @param onClearFilter Callback to request clearing the filter.
 * @param modifier The modifier for this composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThesaurusFilterChip(
    filter: String,
    onFilter: () -> Unit,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    InputChip(
        selected = true,
        onClick = onFilter,
        label = {
            Row {
                Text(text = stringResource(R.string.filter_thesaurus_label))
                Text(text = filter)
            }
        },
        trailingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_clear),
                contentDescription = stringResource(R.string.filter_clear),
                // ic_clear carries its own fill color
                // (@color/on_primary, day and night variants).
                tint = Color.Unspecified,
                modifier = Modifier.clickable(onClick = onClearFilter),
            )
        },
        // The chip is always selected, so both
        // the normal and the selected colors are set.
        colors = InputChipDefaults.inputChipColors(
            containerColor = MaterialTheme.colorScheme.primary,
            labelColor = MaterialTheme.colorScheme.onPrimary,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = modifier,
    )
}
