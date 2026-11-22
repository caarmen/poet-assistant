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

package ca.rmen.android.poetassistant.main.dictionaries.search.ui.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * The overflow menu content. A submenu replaces the current menu content
 * in place, with a back header to return to the parent level.
 */
@Composable
fun MenuContent(
    items: List<SearchBarMenuItem>,
    onItemClick: (Int) -> Unit,
) {
    // Lives inside the menu content, so it resets when the menu closes.
    var openedParentId by remember { mutableStateOf<Int?>(null) }
    val parent = items.firstOrNull { it.id == openedParentId }

    if (parent == null) {
        items.forEach { item ->
            DropdownMenuItem(
                text = { Text(item.title) },
                enabled = item.enabled,
                trailingIcon = if (item.children.isNotEmpty()) {
                    { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
                } else null,
                onClick = {
                    if (item.children.isEmpty()) onItemClick(item.id)
                    else openedParentId = item.id
                },
            )
        }
    } else {
        DropdownMenuItem(
            text = { Text(parent.title) },
            leadingIcon = {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            },
            onClick = { openedParentId = null },
        )
        HorizontalDivider()
        parent.children.forEach { child ->
            DropdownMenuItem(
                text = { Text(child.title) },
                enabled = child.enabled,
                onClick = { onItemClick(child.id) },
            )
        }
    }
}
