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

package ca.rmen.android.poetassistant.main.ui.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.dictionaries.search.suggestions.SuggestionsViewModel.SearchSuggestion
import ca.rmen.android.poetassistant.main.dictionaries.search.ui.composables.SearchBarMenuItem

/**
 * Stateful app shell: top bar with [MainSearchBar] and a [PrimaryTabRow], plus a
 * caller-provided [content] slot. In Step 1 this is a thin forwarder with
 * plain data + callbacks (no ViewModel yet); Step 2 adds a MainScreenViewModel.
 *
 * This composable must not reference legacy View/Fragment types — the [content]
 * slot lets the caller decide what to render in the content area.
 */
@Composable
fun MainScreen(
    tabs: List<Tab>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    showTabText: Boolean,
    showTabIcons: Boolean,
    isLargeScreen: Boolean,
    suggestions: List<SearchSuggestion>,
    menuItems: () -> List<SearchBarMenuItem>,
    onMenuItemClick: (Int) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    modifier: Modifier = Modifier,
) {
    MainScreenContent(
        tabs = tabs,
        selectedTabIndex = selectedTabIndex,
        onTabSelected = onTabSelected,
        showTabText = showTabText,
        showTabIcons = showTabIcons,
        isLargeScreen = isLargeScreen,
        suggestions = suggestions,
        menuItems = menuItems,
        onMenuItemClick = onMenuItemClick,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        onExpandedChange = onExpandedChange,
        content = content,
        modifier = modifier,
    )
}
