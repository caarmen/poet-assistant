package ca.rmen.android.poetassistant.main.settings.ui.composables.previews

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.settings.ui.composables.SettingsSwitchRow
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
@Preview(showBackground = true)
fun SettingsSwitchRowPreview() {
    AppTheme {
        SettingsSwitchRow(
            title = "All rhymes",
            description = "Return all rhymes",
            checked = true,
            onCheckedChange = {},
            testTag = null,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
@Preview(showBackground = true)
fun SettingsSwitchRowNoDescriptionPreview() {
    AppTheme {
        SettingsSwitchRow(
            title = "Selection lookup",
            description = null,
            checked = false,
            onCheckedChange = {},
            testTag = null,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
