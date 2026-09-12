package ca.rmen.android.poetassistant.main.dictionaries.dictionary.ui.composables.previews

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.ui.composables.DictionaryHeader
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
@Preview(showBackground = true, name="Favorite")
fun DictionaryHeaderFavoritePreview(
) {
    AppTheme {
        DictionaryHeader(
            word = "hello",
            isFavorite = true,
            onToggleFavorite = {},
            onSpeakWord = {},
            onSearchWeb = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
@Preview(showBackground = true, name="Not Favorite")
fun DictionaryHeaderNotFavoritePreview(
) {
    AppTheme {
        DictionaryHeader(
            word = "hello",
            isFavorite = false,
            onToggleFavorite = {},
            onSpeakWord = {},
            onSearchWeb = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
