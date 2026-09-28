package ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ui.composables.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusListItem
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusScreenState
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ui.composables.ThesaurusScreenContent
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.theme.AppTheme

private val ENTRIES = listOf(
    ThesaurusListItem.WordTypeHeading("noun"),
    ThesaurusListItem.RelationTypeHeading("Synonyms:"),
    ThesaurusListItem.Word("love", true),
    ThesaurusListItem.Word("affection", false),
    ThesaurusListItem.WordTypeHeading("verb"),
    ThesaurusListItem.RelationTypeHeading("Antonyms:"),
    ThesaurusListItem.Word("hate", false),
)

@Composable
private fun ThesaurusScreenContentPreviewContent(
    state: ThesaurusScreenState,
    layout: Layout,
    isLargeScreen: Boolean,
) {
    AppTheme {
        ThesaurusScreenContent(
            state = state,
            layout = layout,
            isLargeScreen = isLargeScreen,
            externalAppMenuItemsProducer = { _ -> emptyList() },
            onSetFavorite = { _, _ -> },
            onSpeakWord = {},
            onSearchWeb = {},
            onCopy = {},
            onRhymeFilter = {},
            onClearRhymeFilter = {},
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
            rhymeFilter = null,
            entries = ENTRIES,
        ),
        layout = Layout.EFFICIENT,
        isLargeScreen = false,
    )
}

@Composable
@Preview(showBackground = true)
fun ThesaurusScreenContentSuccessLargeScreenPreview() {
    ThesaurusScreenContentPreviewContent(
        state = ThesaurusScreenState.Success(
            word = "love",
            isFavorite = true,
            rhymeFilter = null,
            entries = ENTRIES,
        ),
        layout = Layout.EFFICIENT,
        isLargeScreen = true,
    )
}

@Composable
@Preview(showBackground = true)
fun ThesaurusScreenContentSuccessWithFilterPreview() {
    ThesaurusScreenContentPreviewContent(
        state = ThesaurusScreenState.Success(
            word = "love",
            isFavorite = false,
            rhymeFilter = "yesterday",
            entries = ENTRIES,
        ),
        layout = Layout.CLEAN,
        isLargeScreen = false,
    )
}

@Composable
@Preview(showBackground = true)
fun ThesaurusScreenContentNotFoundPreview() {
    ThesaurusScreenContentPreviewContent(
        state = ThesaurusScreenState.NotFound(
            word = "asdfgh",
            isFavorite = false,
            rhymeFilter = null,
        ),
        layout = Layout.EFFICIENT,
        isLargeScreen = false,
    )
}

@Composable
@Preview(showBackground = true)
fun ThesaurusScreenContentIdlePreview() {
    ThesaurusScreenContentPreviewContent(
        state = ThesaurusScreenState.Idle,
        layout = Layout.EFFICIENT,
        isLargeScreen = false,
    )
}
