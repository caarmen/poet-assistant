package ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.dictionaries.patterns.ui.composables.PatternHeader
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
@Preview(showBackground = true)
fun PatternHeaderPreview() {
    AppTheme {
        PatternHeader(
            pattern = "example*",
            onHelp = {}
        )
    }
}
