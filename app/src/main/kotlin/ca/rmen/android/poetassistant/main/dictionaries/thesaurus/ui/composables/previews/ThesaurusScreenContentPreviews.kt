package ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ui.composables.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusListItem
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusScreenState
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ui.composables.ThesaurusScreenContent
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.theme.AppTheme

private val ENTRIES = listOf(
    ThesaurusListItem.Heading("noun"),
    ThesaurusListItem.Subheading("Synonyms:"),
    ThesaurusListItem.Word("love", true),
    ThesaurusListItem.Word("affection", false),
    ThesaurusListItem.Heading("verb"),
    ThesaurusListItem.Subheading("Antonyms:"),
    ThesaurusListItem.Word("hate", false),
)

@Composable
private fun ThesaurusScreenContentPreviewContent(
    state: ThesaurusScreenState,
    layout: Layout,
) {
    AppTheme {
        ThesaurusScreenContent(
            state = state,
            layout = layout,
            externalAppMenuItemsProducer = { _ -> emptyList() },
            onSetFavorite = { _, _ -> },
            onSpeakWord = {},
            onSearchWeb = {},
            onCopy = {},
            onFilter = {},
            onClearFilter = {},
            onSearchInTab = { _, _ -> },
            onExternalAppSelected = { _, _ -> },
        )
    }
}

@Composable
@Preview(showBackground = true)
fun ThesaurusScreenContentSuccessPreview() {
    ThesaurusScreenContentPreviewContent(
        state = ThesaurusScreenState.Success(
            word = "love",
            isFavorite = true,
            filter = null,
            entries = ENTRIES,
        ),
        layout = Layout.EFFICIENT,
    )
}

@Composable
@Preview(showBackground = true)
fun ThesaurusScreenContentSuccessWithFilterPreview() {
    ThesaurusScreenContentPreviewContent(
        state = ThesaurusScreenState.Success(
            word = "love",
            isFavorite = false,
            filter = "yesterday",
            entries = ENTRIES,
        ),
        layout = Layout.CLEAN,
    )
}

@Composable
@Preview(showBackground = true)
fun ThesaurusScreenContentNotFoundPreview() {
    ThesaurusScreenContentPreviewContent(
        state = ThesaurusScreenState.NotFound(
            word = "asdfgh",
            isFavorite = false,
            filter = null,
        ),
        layout = Layout.EFFICIENT,
    )
}

@Composable
@Preview(showBackground = true)
fun ThesaurusScreenContentIdlePreview() {
    ThesaurusScreenContentPreviewContent(
        state = ThesaurusScreenState.Idle,
        layout = Layout.EFFICIENT,
    )
}
