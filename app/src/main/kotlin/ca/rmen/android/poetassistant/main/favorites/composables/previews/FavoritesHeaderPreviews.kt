package ca.rmen.android.poetassistant.main.favorites.composables.previews

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.favorites.composables.FavoritesHeader
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
@Preview(showBackground = true)
fun FavoritesHeaderPreview() {
    AppTheme {
        FavoritesHeader(
            onDeleteAll = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
