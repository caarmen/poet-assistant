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

package ca.rmen.android.poetassistant.main.dictionaries.rhymer.ui.composables.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.dictionaries.rhymer.RhymerListItem
import ca.rmen.android.poetassistant.main.dictionaries.rhymer.ui.composables.RhymerItem
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
private fun RhymerItemPreviewContent(
    item: RhymerListItem,
    layout: Layout,
) {
    AppTheme {
        RhymerItem(
            item = item,
            layout = layout,
            externalAppMenuItemsProducer = { _ -> emptyList() },
            onSetFavorite = { _, _ -> },
            onCopy = {},
            onSearchInTab = { _, _ -> },
            onExternalAppSelected = { _, _ -> },
            rowBackgroundColor = null,
        )
    }
}

@Composable
@Preview(showBackground = true)
fun RhymerItemVariantHeadingPreview() {
    RhymerItemPreviewContent(RhymerListItem.VariantHeading("dove (2)"), Layout.EFFICIENT)
}

@Composable
@Preview(showBackground = true)
fun RhymerItemSectionHeadingPreview() {
    RhymerItemPreviewContent(RhymerListItem.SectionHeading("Strict rhyme matches:"), Layout.EFFICIENT)
}

@Composable
@Preview(showBackground = true)
fun RhymerItemMaxResultsNoticePreview() {
    RhymerItemPreviewContent(RhymerListItem.MaxResultsNotice("Limited to 500 matches"), Layout.EFFICIENT)
}

@Composable
@Preview(showBackground = true)
fun RhymerItemWordPreview() {
    RhymerItemPreviewContent(RhymerListItem.Word("love", isFavorite = true, hasDefinition = true), Layout.EFFICIENT)
}

@Composable
@Preview(showBackground = true)
fun RhymerItemWordWithoutDefinitionPreview() {
    RhymerItemPreviewContent(RhymerListItem.Word("luv", isFavorite = false, hasDefinition = false), Layout.EFFICIENT)
}

@Composable
@Preview(showBackground = true)
fun RhymerItemWordCleanPreview() {
    RhymerItemPreviewContent(RhymerListItem.Word("hate", isFavorite = false, hasDefinition = true), Layout.CLEAN)
}
