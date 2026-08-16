package ca.rmen.android.poetassistant.main.favorites.composables.previews

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.main.favorites.composables.FavoritesPopupMenu
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
@Preview(showBackground = true)
fun FavoritesPopupMenuPreview() {
    AppTheme {
        FavoritesPopupMenu(
            expanded = true,
            layout = Layout.CLEAN,
            externalAppMenuItems = emptyList(),
            onDismiss = {},
            onCopy = {},
            onSearchInTab = {},
            onExternalAppSelected = {},
            modifier = Modifier.width(200.dp)
        )
    }
}

@Composable
@Preview(showBackground = true)
fun FavoritesPopupMenuEfficientPreview() {
    AppTheme {
        FavoritesPopupMenu(
            expanded = true,
            layout = Layout.EFFICIENT,
            externalAppMenuItems = emptyList(),
            onDismiss = {},
            onCopy = {},
            onSearchInTab = {},
            onExternalAppSelected = {},
            modifier = Modifier.width(200.dp)
        )
    }
}
