package ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.main.dictionaries.patterns.PatternEntry
import ca.rmen.android.poetassistant.main.dictionaries.patterns.PatternScreenState
import ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables.PatternScreenContent
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
@Preview(showBackground = true)
fun PatternScreenContentSuccessPreview() {
    AppTheme {
        PatternScreenContent(
            state = PatternScreenState.Success(
                query = "example*",
                entries = listOf(
                    PatternEntry("example", true),
                    PatternEntry("examples", false),
                    PatternEntry("exampler", false)
                ),
                isCapped = false
            ),
            layout = Layout.EFFICIENT,
            externalAppMenuItemsProducer = { _ -> emptyList() },
            onSetFavorite = { _, _ -> },
            onCopy = {},
            onSearchInTab = { _, _ -> },
            onExternalAppSelected = { _, _ -> },
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PatternScreenContentSuccessCappedPreview() {
    AppTheme {
        PatternScreenContent(
            state = PatternScreenState.Success(
                query = "test*",
                entries = List(Constants.MAX_RESULTS) { index ->
                    PatternEntry("word$index", index % 2 == 0)
                },
                isCapped = true
            ),
            layout = Layout.CLEAN,
            externalAppMenuItemsProducer = { _ -> emptyList() },
            onSetFavorite = { _, _ -> },
            onCopy = {},
            onSearchInTab = { _, _ -> },
            onExternalAppSelected = { _, _ -> },
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PatternScreenContentNotFoundPreview() {
    AppTheme {
        PatternScreenContent(
            state = PatternScreenState.NotFound("nonexistent"),
            layout = Layout.EFFICIENT,
            externalAppMenuItemsProducer = { _ -> emptyList() },
            onSetFavorite = { _, _ -> },
            onCopy = {},
            onSearchInTab = { _, _ -> },
            onExternalAppSelected = { _, _ -> },
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PatternScreenContentIdlePreview() {
    AppTheme {
        PatternScreenContent(
            state = PatternScreenState.Idle,
            layout = Layout.EFFICIENT,
            externalAppMenuItemsProducer = { _ -> emptyList() },
            onSetFavorite = { _, _ -> },
            onCopy = {},
            onSearchInTab = { _, _ -> },
            onExternalAppSelected = { _, _ -> },
        )
    }
}
