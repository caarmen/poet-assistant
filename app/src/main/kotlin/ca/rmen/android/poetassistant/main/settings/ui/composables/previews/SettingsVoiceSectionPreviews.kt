package ca.rmen.android.poetassistant.main.settings.ui.composables.previews

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.settings.ui.composables.SettingsVoiceSection
import ca.rmen.android.poetassistant.settings.Settings
import ca.rmen.android.poetassistant.settings.TtsVoice
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
@Preview(showBackground = true)
fun SettingsVoiceSectionPreview() {
    AppTheme {
        SettingsVoiceSection(
            voices = listOf(
                TtsVoice(Settings.VOICE_SYSTEM, "System voice"),
                TtsVoice("en-us-x-1", "English (United States) Female 1"),
            ),
            selectedVoiceId = Settings.VOICE_SYSTEM,
            voiceSpeed = 100,
            voicePitch = 100,
            showSystemTtsSettingsRow = true,
            onVoiceSelected = {},
            onVoiceSpeedChanged = {},
            onVoicePitchChanged = {},
            onPreviewClicked = {},
            onOpenSystemTtsSettings = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
@Preview(showBackground = true)
fun SettingsVoiceSectionNoVoicesPreview() {
    AppTheme {
        SettingsVoiceSection(
            voices = emptyList(),
            selectedVoiceId = Settings.VOICE_SYSTEM,
            voiceSpeed = 100,
            voicePitch = 100,
            showSystemTtsSettingsRow = false,
            onVoiceSelected = {},
            onVoiceSpeedChanged = {},
            onVoicePitchChanged = {},
            onPreviewClicked = {},
            onOpenSystemTtsSettings = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
