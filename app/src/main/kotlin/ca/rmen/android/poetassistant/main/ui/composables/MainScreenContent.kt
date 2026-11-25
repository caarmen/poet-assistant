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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.main.dictionaries.search.suggestions.SuggestionsViewModel.SearchSuggestion
import ca.rmen.android.poetassistant.main.dictionaries.search.ui.composables.MainSearchBar
import ca.rmen.android.poetassistant.main.dictionaries.search.ui.composables.SearchBarMenuItem

const val TABS_TITLE_STRIP_LABEL_TAG = "Tabs_TitleStrip_Label"

/**
 * Stateless app shell UI: a [Scaffold] whose top bar holds the [MainSearchBar]
 * and a [PrimaryTabRow], and whose content area is the caller-provided [content] slot.
 */
@Composable
fun MainScreenContent(
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
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                )
            ) {
                MainSearchBar(
                    isLargeScreen = isLargeScreen,
                    suggestions = suggestions,
                    menuItems = menuItems,
                    onMenuItemClick = onMenuItemClick,
                    onQueryChange = onQueryChange,
                    onSearch = onSearch,
                    onExpandedChange = onExpandedChange,
                )
                // Surface container (M3 default): keeps the primary-colored
                // selected-tab indicator clearly visible against the bar.
                PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, tab ->
                        val tabLabel = tabName(tab)
                        Tab(
                            selected = index == selectedTabIndex,
                            onClick = { onTabSelected(index) },
                            text = if (showTabText) { { Text(tabLabel) } } else null,
                            icon = if (showTabIcons) {
                                { Icon(painterResource(tabIcon(tab)), contentDescription = tabLabel) }
                            } else null,
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("tab_${tabLabel}"),
                        )
                    }
                }
                // Icon-only tabs (showTabText=false): show the selected tab's
                // name as a strip below the tabs, replacing the legacy
                // PagerTitleStrip: same background as the tab row, with a
                // bottom divider.
                if (!showTabText) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = tabs.getOrNull(selectedTabIndex)?.let { tabName(it) }.orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.testTag(TABS_TITLE_STRIP_LABEL_TAG)
                        )
                    }
                    HorizontalDivider()
                }
            }
        },
        modifier = modifier,
    ) { contentPadding ->
        content(contentPadding)
    }
}

@Composable
private fun tabName(tab: Tab): String = when (tab) {
    Tab.PATTERN -> stringResource(R.string.tab_pattern)
    Tab.FAVORITES -> stringResource(R.string.tab_favorites)
    Tab.WOTD -> stringResource(R.string.tab_wotd)
    Tab.RHYMER -> stringResource(R.string.tab_rhymer)
    Tab.THESAURUS -> stringResource(R.string.tab_thesaurus)
    Tab.DICTIONARY -> stringResource(R.string.tab_dictionary)
    Tab.READER -> stringResource(R.string.tab_reader)
}

@DrawableRes
private fun tabIcon(tab: Tab): Int = when (tab) {
    // Use the vector _default variants: the ic_tab_* top-level drawables are
    // state-list selectors, which painterResource cannot load in tests. Tab
    // already tints the icon based on the selected state.
    Tab.PATTERN -> R.drawable.ic_tab_pattern_default
    Tab.FAVORITES -> R.drawable.ic_tab_star_default
    Tab.WOTD -> R.drawable.ic_tab_wotd_default
    Tab.RHYMER -> R.drawable.ic_tab_rhymer_default
    Tab.THESAURUS -> R.drawable.ic_tab_thesaurus_default
    Tab.DICTIONARY -> R.drawable.ic_tab_dictionary_default
    Tab.READER -> R.drawable.ic_tab_reader_default
}
