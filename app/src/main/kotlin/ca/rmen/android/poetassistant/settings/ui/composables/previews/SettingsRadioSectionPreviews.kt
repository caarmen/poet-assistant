package ca.rmen.android.poetassistant.settings.ui.composables.previews

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.settings.Theme
import ca.rmen.android.poetassistant.settings.ui.composables.SettingsChoice
import ca.rmen.android.poetassistant.settings.ui.composables.SettingsRadioSection
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
@Preview(showBackground = true)
fun SettingsRadioSectionPreview() {
    AppTheme {
        SettingsRadioSection(
            title = "Theme",
            choices = listOf(
                SettingsChoice(Theme.LIGHT, "Light"),
                SettingsChoice(Theme.DARK, "Dark"),
                SettingsChoice(Theme.AUTO, "Auto"),
            ),
            selectedEntry = Theme.AUTO,
            onEntrySelected = {},
            testTag = "doesnt matter",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
