package ca.rmen.android.poetassistant.main.settings.ui.composables.previews

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.settings.Theme
import ca.rmen.android.poetassistant.main.settings.ui.composables.SettingsRadioSection
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
@Preview(showBackground = true)
fun SettingsRadioSectionPreview() {
    AppTheme {
        SettingsRadioSection(
            title = "Theme",
            entries = listOf(
                Theme.LIGHT to "Light",
                Theme.DARK to "Dark",
                Theme.AUTO to "Auto",
            ),
            selectedEntry = Theme.AUTO,
            onEntrySelected = {},
            testTag = null,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
