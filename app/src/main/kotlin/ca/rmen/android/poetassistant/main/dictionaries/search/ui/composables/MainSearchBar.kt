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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.dictionaries.search.suggestions.SuggestionsViewModel.SearchSuggestion
import ca.rmen.android.poetassistant.theme.AppTheme

const val MENU_OVERFLOW_TAG = "Menu_Overflow_Tag"
const val SEARCH_ICON_TAG = "Search_Icon_Tag"
const val SEARCH_INPUT_FIELD_TAG = "Search_Input_Field_Tag"

/**
 * One entry of the overflow menu shown in the trailing icon, as a plain
 * immutable snapshot of an `android.view.MenuItem` (submenus become children).
 */
data class SearchBarMenuItem(
    val id: Int,
    val title: String,
    val enabled: Boolean = true,
    val children: List<SearchBarMenuItem> = emptyList(),
)

/**
 * The single top row of the main screen: a Material3 collapsed [SearchBar] in
 * the app bar, plus an [ExpandedFullScreenSearchBar] that renders the expanded
 * search (input field + suggestions) in its own full-screen window, so the
 * app bar layout is never affected by the expanded state.
 *
 * The query and expanded state survive config changes (both states are
 * remembered saveable).
 *
 * @param suggestions The current search suggestions to show in the dropdown.
 * @param menuItems Produces the current overflow menu items; read when the
 * dropdown opens so menu provider updates (visibility, enabled) are reflected.
 * @param onMenuItemClick Callback invoked with the item id when a leaf item is clicked.
 * @param onQueryChange Callback invoked when the query text changes.
 * @param onSearch Callback invoked with a non-blank term on IME search or suggestion click.
 * @param onExpandedChange Callback invoked when the expanded state changes.
 * @param modifier The modifier for this composable.
 */
@Composable
fun MainSearchBar(
    suggestions: List<SearchSuggestion>,
    menuItems: () -> List<SearchBarMenuItem>,
    onMenuItemClick: (Int) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    AppTheme {

        IconButton(
            onClick = { showMenu = true },
            modifier = Modifier.testTag(MENU_OVERFLOW_TAG),
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.content_description_open_menu),
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            MenuContent(
                items = menuItems(),
                onItemClick = { id ->
                    showMenu = false
                    onMenuItemClick(id)
                },
            )
        }
    }
}

