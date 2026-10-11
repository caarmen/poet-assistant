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

package ca.rmen.android.poetassistant.main.dictionaries.rhymes.ui.composables.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.dictionaries.rhymes.RhymerListItem
import ca.rmen.android.poetassistant.main.dictionaries.rhymes.RhymerScreenState
import ca.rmen.android.poetassistant.main.dictionaries.rhymes.ui.composables.RhymerScreenContent
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.theme.AppTheme

private val ENTRIES = listOf(
    RhymerListItem.SectionHeading(id = "section:fav", text = "Favorites:"),
    RhymerListItem.Word(id = "w:fav:love", word = "love", isFavorite = true, hasDefinition = true),
    RhymerListItem.VariantHeading(id = "variant:1", text = "dove (2)"),
    RhymerListItem.SectionHeading(id = "section:1:strict", text = "Strict rhyme matches:"),
    RhymerListItem.Word(id = "w:1:strict:above", word = "above", isFavorite = false, hasDefinition = true),
    RhymerListItem.SectionHeading(id = "section:1:one", text = "Last syllable:"),
    RhymerListItem.Word(id = "w:1:one:glove", word = "glove", isFavorite = false, hasDefinition = false),
    RhymerListItem.MaxResultsNotice(id = "max:1:one", text = "Limited to 500 matches"),
)

@Composable
private fun RhymerScreenContentPreviewContent(
    state: RhymerScreenState,
    layout: Layout,
    isLargeScreen: Boolean,
) {
    AppTheme {
        RhymerScreenContent(
            state = state,
            layout = layout,
            isLargeScreen = isLargeScreen,
            externalAppMenuItemsProducer = { _ -> emptyList() },
            onSetFavorite = { _, _ -> },
            onSpeakWord = {},
            onSearchWeb = {},
            onCopy = {},
            onSynonymFilter = {},
            onClearSynonymFilter = {},
            onSearchInTab = { _, _ -> },
            onExternalAppSelected = { _, _ -> },
        )
    }
}

@Composable
@Preview(showBackground = true)
fun RhymerScreenContentSuccessPreview() {
    RhymerScreenContentPreviewContent(
        state = RhymerScreenState.Success(
            word = "dove",
            isFavorite = true,
            synonymFilter = null,
            entries = ENTRIES,
        ),
        layout = Layout.EFFICIENT,
        isLargeScreen = false,
    )
}

@Composable
@Preview(showBackground = true)
fun RhymerScreenContentSuccessLargeScreenPreview() {
    RhymerScreenContentPreviewContent(
        state = RhymerScreenState.Success(
            word = "dove",
            isFavorite = true,
            synonymFilter = null,
            entries = ENTRIES,
        ),
        layout = Layout.EFFICIENT,
        isLargeScreen = true,
    )
}

@Composable
@Preview(showBackground = true)
fun RhymerScreenContentSuccessWithFilterPreview() {
    RhymerScreenContentPreviewContent(
        state = RhymerScreenState.Success(
            word = "dove",
            isFavorite = false,
            synonymFilter = "love",
            entries = ENTRIES,
        ),
        layout = Layout.CLEAN,
        isLargeScreen = false,
    )
}

@Composable
@Preview(showBackground = true)
fun RhymerScreenContentNotFoundPreview() {
    RhymerScreenContentPreviewContent(
        state = RhymerScreenState.NotFound(
            word = "asdfgh",
            isFavorite = false,
            synonymFilter = null,
        ),
        layout = Layout.EFFICIENT,
        isLargeScreen = false,
    )
}

@Composable
@Preview(showBackground = true)
fun RhymerScreenContentIdlePreview() {
    RhymerScreenContentPreviewContent(
        state = RhymerScreenState.Idle,
        layout = Layout.EFFICIENT,
        isLargeScreen = false,
    )
}
