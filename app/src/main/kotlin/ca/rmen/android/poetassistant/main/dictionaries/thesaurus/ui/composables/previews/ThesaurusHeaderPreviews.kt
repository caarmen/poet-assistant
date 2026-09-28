package ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ui.composables.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ui.composables.ThesaurusHeader
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
@Preview(showBackground = true)
fun ThesaurusHeaderPreview() {
    AppTheme {
        ThesaurusHeader(
            word = "love",
            isFavorite = true,
            filter = null,
            onToggleFavorite = {},
            onSpeakWord = {},
            onSearchWeb = {},
            onFilter = {},
            onClearFilter = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
fun ThesaurusHeaderWithFilterPreview() {
    AppTheme {
        ThesaurusHeader(
            word = "love",
            isFavorite = false,
            filter = "yesterday",
            onToggleFavorite = {},
            onSpeakWord = {},
            onSearchWeb = {},
            onFilter = {},
            onClearFilter = {},
        )
    }
}
