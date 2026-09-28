package ca.rmen.android.poetassistant.main.wotd.ui.composables.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.wotd.ui.composables.WotdItem
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
private fun WotdItemPreviewContent(
    isFavorite: Boolean,
    layout: Layout,
) {
    AppTheme {
        WotdItem(
            word = "example",
            date = "Sep 27",
            isFavorite = isFavorite,
            layout = layout,
            externalAppMenuItemsProducer = { _ -> emptyList() },
            onSetFavorite = {},
            onCopy = {},
            onSearchInTab = {},
            onExternalAppSelected = { _, _ -> }
        )
    }
}

@Composable
@Preview(showBackground = true)
fun WotdItemPreview() = WotdItemPreviewContent(isFavorite = true, layout = Layout.EFFICIENT)

@Composable
@Preview(showBackground = true)
fun WotdItemCleanPreview() = WotdItemPreviewContent(isFavorite = false, layout = Layout.CLEAN)
