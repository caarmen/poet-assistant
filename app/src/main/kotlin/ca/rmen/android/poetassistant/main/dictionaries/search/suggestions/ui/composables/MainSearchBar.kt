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

package ca.rmen.android.poetassistant.main.dictionaries.search.suggestions.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.dictionaries.search.suggestions.SuggestionsViewModel.SearchSuggestion
import ca.rmen.android.poetassistant.theme.AppTheme
import kotlinx.coroutines.launch

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
@OptIn(ExperimentalMaterial3Api::class)
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
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()
    var showMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Forward the query text to the suggestions view model.
    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }.collect(onQueryChange)
    }

    val expanded = searchBarState.currentValue == SearchBarValue.Expanded
    LaunchedEffect(expanded) { onExpandedChange(expanded) }

    fun searchTermSelected(term: String) {
        if (term.isNotBlank()) {
            onSearch(term)
        }
        // Clear the query text after a search is performed.
        textFieldState.edit { replace(0, length, "") }
        coroutineScope.launch { searchBarState.animateToCollapsed() }
    }

    AppTheme {
        val inputField: @Composable (Modifier) -> Unit = { fieldModifier ->
            SearchBarDefaults.InputField(
                modifier = fieldModifier,
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                onSearch = { searchTermSelected(textFieldState.text.toString()) },
                placeholder = { Text(stringResource(R.string.search_hint)) },
                // M3 pattern: back arrow while expanded, search icon when collapsed.
                leadingIcon = {
                    if (expanded) {
                        IconButton(onClick = { coroutineScope.launch { searchBarState.animateToCollapsed() } }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.content_description_collapse_search),
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.content_description_search),
                            modifier = Modifier.testTag(SEARCH_ICON_TAG),
                        )
                    }
                },
                // The overflow menu is hidden while the search view is expanded.
                trailingIcon = {
                    if (!expanded) {
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
                },
            )
        }
        // Collapsed bar in the app bar, with 16dp horizontal margins.
        SearchBar(
            state = searchBarState,
            inputField = {inputField(Modifier)},
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp),
        )

        // Expanded search in its own full-screen window. The input field tag
        // goes on this instance only, so tests find exactly one node.
        ExpandedFullScreenSearchBar(
            state = searchBarState,
            inputField = { inputField(Modifier.testTag(SEARCH_INPUT_FIELD_TAG)) },
        ) {
            LazyColumn {
                // A word can appear twice (history + dictionary), so the key
                // must include the source icon, not just the word.
                items(suggestions, key = { "${it.word}-${it.iconResource}" }) { suggestion ->
                    SuggestionItem(
                        suggestion = suggestion,
                        onClick = ::searchTermSelected,
                    )
                }
            }
        }
    }
}

/**
 * Renders overflow menu [items] inside a [DropdownMenu], recursing into
 * submenus (e.g. the reader tab's "File" menu). Leaf clicks are reported
 * through [onItemClick] with the item id.
 */
@Composable
private fun MenuContent(
    items: List<SearchBarMenuItem>,
    onItemClick: (Int) -> Unit,
) {
    for (item in items) {
        if (item.children.isEmpty()) {
            DropdownMenuItem(
                text = { Text(item.title) },
                enabled = item.enabled,
                onClick = { onItemClick(item.id) },
            )
        } else {
            var showSubMenu by remember(item.id) { mutableStateOf(false) }
            DropdownMenuItem(
                text = { Text(item.title) },
                enabled = item.enabled,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                    )
                },
                onClick = { showSubMenu = true },
            )
            DropdownMenu(
                expanded = showSubMenu,
                onDismissRequest = { showSubMenu = false },
            ) {
                MenuContent(items = item.children, onItemClick = onItemClick)
            }
        }
    }
}
