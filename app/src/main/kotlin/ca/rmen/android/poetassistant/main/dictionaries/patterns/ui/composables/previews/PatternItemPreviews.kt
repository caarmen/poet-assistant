package ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.common.models.ExternalAppMenuItem
import ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables.PatternItem
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
@Preview(showBackground = true)
fun PatternItemPreview() {
    AppTheme {
        PatternItem(
            word = "example",
            isFavorite = true,
            layout = Layout.EFFICIENT,
            externalAppMenuItemsProducer = { _ -> emptyList() },
            onToggleFavorite = {},
            onCopy = {},
            onSearchInTab = {},
            onExternalAppSelected = { _, _ -> }
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PatternItemCleanPreview() {
    AppTheme {
        PatternItem(
            word = "example",
            isFavorite = false,
            layout = Layout.CLEAN,
            externalAppMenuItemsProducer = { _ -> emptyList() },
            onToggleFavorite = {},
            onCopy = {},
            onSearchInTab = {},
            onExternalAppSelected = { _, _ -> }
        )
    }
}
