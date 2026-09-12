package ca.rmen.android.poetassistant.main.dictionaries.dictionary.ui.composables.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.ui.composables.Definition
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
@Preview(showBackground = true)
fun DefinitionPreview() {
    AppTheme {
        Definition(
            partOfSpeech = "v",
            definition = "to express greetings"
        )
    }
}
