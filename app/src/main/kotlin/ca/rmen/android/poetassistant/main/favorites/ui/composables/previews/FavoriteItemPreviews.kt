package ca.rmen.android.poetassistant.main.favorites.ui.composables.previews

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.favorites.ui.composables.FavoriteItem
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
@Preview(showBackground = true)
fun FavoriteItemPreview() {
    AppTheme {
        FavoriteItem(
            word = "Example",
            layout = Layout.CLEAN,
            externalAppMenuItemsProducer = { emptyList() },
            onToggleFavorite = {},
            onCopy = {},
            onSearchInTab = {},
            onExternalAppSelected = {_, _ -> },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
@Preview(showBackground = true)
fun FavoriteItemEfficientPreview() {
    AppTheme {
        FavoriteItem(
            word = "Example",
            layout = Layout.EFFICIENT,
            externalAppMenuItemsProducer = { emptyList() },
            onToggleFavorite = {},
            onCopy = {},
            onSearchInTab = {},
            onExternalAppSelected = {_, _ -> },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
