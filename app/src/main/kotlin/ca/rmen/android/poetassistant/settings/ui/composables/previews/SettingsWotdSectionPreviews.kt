package ca.rmen.android.poetassistant.settings.ui.composables.previews

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.settings.WotdNotificationPriority
import ca.rmen.android.poetassistant.settings.ui.composables.SettingsWotdSection
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
@Preview(showBackground = true)
fun SettingsWotdSectionPreview() {
    AppTheme {
        SettingsWotdSection(
            isWotdEnabled = true,
            wotdNotificationPriority = WotdNotificationPriority.DEFAULT,
            onWotdEnabledChanged = {},
            onWotdNotificationPrioritySelected = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
