package ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ui.composables.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusListItem
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ui.composables.ThesaurusItem
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
private fun ThesaurusItemPreviewContent(
    item: ThesaurusListItem,
    layout: Layout,
) {
    AppTheme {
        ThesaurusItem(
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
fun ThesaurusItemHeadingPreview() {
    ThesaurusItemPreviewContent(ThesaurusListItem.Heading("noun"), Layout.EFFICIENT)
}

@Composable
@Preview(showBackground = true)
fun ThesaurusItemSubheadingPreview() {
    ThesaurusItemPreviewContent(ThesaurusListItem.Subheading("Synonyms:"), Layout.EFFICIENT)
}

@Composable
@Preview(showBackground = true)
fun ThesaurusItemWordPreview() {
    ThesaurusItemPreviewContent(ThesaurusListItem.Word("love", true), Layout.EFFICIENT)
}

@Composable
@Preview(showBackground = true)
fun ThesaurusItemWordCleanPreview() {
    ThesaurusItemPreviewContent(ThesaurusListItem.Word("hate", false), Layout.CLEAN)
}
