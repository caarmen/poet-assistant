package ca.rmen.android.poetassistant.main.dictionaries.dictionary.ui.composables.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryEntry
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryScreenState
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.ui.composables.DictionaryScreenContent
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
@Preview(showBackground = true)
fun DictionaryScreenContentSuccessPreview() {
    AppTheme {
        DictionaryScreenContent(
            state = DictionaryScreenState.Success(
                entry = DictionaryEntry(
                    word = "hello",
                    details = listOf(
                        DictionaryEntry.DictionaryEntryDetails(
                            partOfSpeech = "v",
                            definition = "to express greetings"
                        ),
                        DictionaryEntry.DictionaryEntryDetails(
                            partOfSpeech = "n",
                            definition = "an expression of greeting"
                        )
                    )
                ),
                isFavorite = false
            ),
            onToggleFavorite = {_, _ ->},
            onSpeakWord = {},
            onSearchWeb = {}
        )
    }
}

@Composable
@Preview(showBackground = true)
fun DictionaryScreenContentNotFoundPreview() {
    AppTheme {
        DictionaryScreenContent(
            state = DictionaryScreenState.NotFound("test"),
            onToggleFavorite = {_, _ ->},
            onSpeakWord = {},
            onSearchWeb = {}
        )
    }
}

@Composable
@Preview(showBackground = true)
fun DictionaryScreenContentIdlePreview() {
    AppTheme {
        DictionaryScreenContent(
            state = DictionaryScreenState.Idle,
            onToggleFavorite = {_, _ ->},
            onSpeakWord = {},
            onSearchWeb = {}
        )
    }
}
