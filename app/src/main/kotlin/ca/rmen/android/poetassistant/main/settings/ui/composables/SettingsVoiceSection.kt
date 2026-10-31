/*
 * Copyright (c) 2016 - present Carmen Alvarez
 *
 * This file is part of Poet Assistant.
 *
 * Poet Assistant is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Poet Assistant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Poet Assistant.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.rmen.android.poetassistant.main.settings.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.settings.TtsVoice
import kotlin.math.roundToInt

// Test tags: the legacy preference keys, so the migrated tests address
// the rows as they addressed the preferences.
const val SETTINGS_VOICE_ROW_TAG = "PREF_VOICE"
const val SETTINGS_VOICE_SPEED_SLIDER_TAG = "PREF_VOICE_SPEED_V3"
const val SETTINGS_VOICE_PITCH_SLIDER_TAG = "PREF_VOICE_PITCH_V3"
const val SETTINGS_VOICE_PREVIEW_ROW_TAG = "PREF_VOICE_PREVIEW"
const val SETTINGS_SYSTEM_TTS_SETTINGS_ROW_TAG = "PREF_SYSTEM_TTS_SETTINGS"

const val SETTINGS_VOICE_SELECTION_LIST_TAG = "PREF_VOICE_SELECTION_LIST"

/**
 * The voice section: voice picker, speed and pitch sliders, preview
 * toggle, and the system TTS settings row.
 *
 * - The picker row is hidden when fewer than 2 voices are available
 *   (legacy behavior, spec §1.3.4).
 * - The sliders hold their value locally and commit through the
 *   repository setter only when the drag finishes: no write on every
 *   drag tick (spec §3.7).
 * - The preview row toggles: stop-then-speak is handled by the
 *   ViewModel from the derived isSpeaking flag (spec §1.3.10).
 * - The system TTS settings row is shown only when a system activity
 *   resolves the intent; the tap calls back to the host, which stores
 *   the restart-on-resume flag and launches the intent (spec §3.6).
 */
@Composable
fun SettingsVoiceSection(
    voices: List<TtsVoice>,
    selectedVoiceId: String,
    voiceSpeed: Int,
    voicePitch: Int,
    showSystemTtsSettingsRow: Boolean,
    onVoiceSelected: (String) -> Unit,
    onVoiceSpeedChanged: (Int) -> Unit,
    onVoicePitchChanged: (Int) -> Unit,
    onPreviewClicked: () -> Unit,
    onOpenSystemTtsSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (voices.size > 1) {
            VoicePickerRow(
                voices = voices,
                selectedVoiceId = selectedVoiceId,
                onVoiceSelected = onVoiceSelected,
            )
        }
        VoiceSliderRow(
            title = stringResource(R.string.pref_voice_speed_title),
            value = voiceSpeed,
            onValueChanged = onVoiceSpeedChanged,
            testTag = SETTINGS_VOICE_SPEED_SLIDER_TAG,
        )
        VoiceSliderRow(
            title = stringResource(R.string.pref_voice_pitch_title),
            value = voicePitch,
            onValueChanged = onVoicePitchChanged,
            testTag = SETTINGS_VOICE_PITCH_SLIDER_TAG,
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.pref_voice_preview_title)) },
            supportingContent = { Text(stringResource(R.string.pref_voice_preview_summary)) },
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_preview),
                    contentDescription = null,
                )
            },
            modifier = Modifier
                .testTag(SETTINGS_VOICE_PREVIEW_ROW_TAG)
                .clickable { onPreviewClicked() },
        )
        if (showSystemTtsSettingsRow) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.pref_system_tts_settings_title)) },
                supportingContent = { Text(stringResource(R.string.pref_system_tts_settings_description)) },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings_system_settings),
                        contentDescription = null,
                    )
                },
                modifier = Modifier
                    .testTag(SETTINGS_SYSTEM_TTS_SETTINGS_ROW_TAG)
                    .clickable { onOpenSystemTtsSettings() },
            )
        }
    }
}

@Composable
private fun VoicePickerRow(
    voices: List<TtsVoice>,
    selectedVoiceId: String,
    onVoiceSelected: (String) -> Unit,
) {
    var openDialog by remember { mutableStateOf(false) }
    ListItem(
        headlineContent = { Text(stringResource(R.string.pref_voice_title)) },
        supportingContent = {
            Text(voices.findVoiceLabel(selectedVoiceId))
        },
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_settings_voice),
                contentDescription = null,
            )
        },
        modifier = Modifier
            .testTag(SETTINGS_VOICE_ROW_TAG)
            .clickable { openDialog = true },
    )
    if (openDialog) {
        AlertDialog(
            onDismissRequest = { openDialog = false },
            title = { Text(stringResource(R.string.pref_voice_title)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).testTag(SETTINGS_VOICE_SELECTION_LIST_TAG)
                ) {
                    voices.forEach { voice ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onVoiceSelected(voice.id)
                                    openDialog = false
                                },
                        ) {
                            RadioButton(
                                selected = voice.id == selectedVoiceId,
                                onClick = null,
                            )
                            VoiceLabel(
                                voice = voice,
                                modifier = Modifier.padding(start = 12.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {},
        )
    }
}

/**
 * Renders the voice label with the same formatting as the legacy
 * VoicePreferenceDialogFragment: the formatted name carries a main
 * line and a small secondary line (legacy used html for this).
 */
@Composable
private fun VoiceLabel(voice: TtsVoice, modifier: Modifier = Modifier) {
    val nameLines = voice.name.toString().split('\n')
    Column(modifier = modifier) {
        Text(nameLines.first())
        if (nameLines.size > 1) {
            Text(
                text = nameLines.drop(1).joinToString(separator = " "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun VoiceSliderRow(
    title: String,
    value: Int,
    onValueChanged: (Int) -> Unit,
    testTag: String,
) {
    // Local slider state: commit through the repository setter only
    // when the drag finishes (spec §3.7). The local value resets when
    // the setting changes externally.
    var localValue by remember(value) { mutableFloatStateOf(value.toFloat()) }
    // Like the legacy SeekBarPreference layout: the title on top, the
    // slider full-width below - not in the ListItem trailing slot,
    // which squeezes the title.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Slider(
            value = localValue,
            onValueChange = { localValue = it },
            onValueChangeFinished = { onValueChanged(localValue.roundToInt()) },
            valueRange = 0f..200f,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = title }
                .testTag(testTag),
        )
    }
}

private fun List<TtsVoice>.findVoiceLabel(voiceId: String): String =
    firstOrNull { it.id == voiceId }?.name?.toString() ?: voiceId
