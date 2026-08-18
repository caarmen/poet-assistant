package ca.rmen.android.poetassistant.main.favorites.composables.previews

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.favorites.composables.FavoritesScreenContent
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
@Preview(showBackground = true)
fun FavoritesScreenContentPreview() {
    AppTheme {
        FavoritesScreenContent(
            favorites = listOf("apple", "banana", "cherry"),
            layout = Layout.CLEAN,
            externalAppMenuItemsProducer = { emptyList() },
            onToggleFavorite = {},
            onCopy = {},
            onSearchInTab = { _, _ -> },
            onExternalAppSelected = { _, _ -> },
            onDeleteAll = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
@Preview(showBackground = true)
fun FavoritesScreenContentEmptyPreview() {
    AppTheme {
        FavoritesScreenContent(
            favorites = emptyList(),
            layout = Layout.CLEAN,
            externalAppMenuItemsProducer = { emptyList() },
            onToggleFavorite = {},
            onCopy = {},
            onSearchInTab = { _, _ -> },
            onExternalAppSelected = { _, _ -> },
            onDeleteAll = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
