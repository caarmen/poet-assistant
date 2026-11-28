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

package ca.rmen.android.poetassistant.main.ui.composables.previews

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.theme.AppTheme
import ca.rmen.android.poetassistant.main.ui.composables.MainScreenContent

private val previewTabs = listOf(
    Tab.RHYMER,
    Tab.THESAURUS,
    Tab.DICTIONARY,
    Tab.READER,
    Tab.FAVORITES,
)

@Composable
private fun MainScreenContentPreview(
    showTabText: Boolean,
    showTabIcons: Boolean,
    selectedTabIndex: Int,
) {
    AppTheme {
        MainScreenContent(
            tabs = previewTabs,
            selectedTabIndex = selectedTabIndex,
            onTabSelected = {},
            showTabText = showTabText,
            showTabIcons = showTabIcons,
            isLargeScreen = false,
            suggestions = emptyList(),
            menuItems = { emptyList() },
            onMenuItemClick = {},
            onQueryChange = {},
            onSearch = {},
            onExpandedChange = {},
            content = { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Content")
                }
            },
        )
    }
}

@Composable
@Preview(showBackground = true)
fun MainScreenContentIconsOnlyPreview() = MainScreenContentPreview(
    showTabText = false,
    showTabIcons = true,
    selectedTabIndex = 2,
)

@Composable
@Preview(showBackground = true)
fun MainScreenContentTextPreview() = MainScreenContentPreview(
    showTabText = true,
    showTabIcons = false,
    selectedTabIndex = 0,
)

@Composable
@Preview(showBackground = true)
fun MainScreenContentTextAndIconsPreview() = MainScreenContentPreview(
    showTabText = true,
    showTabIcons = true,
    selectedTabIndex = 3,
)
