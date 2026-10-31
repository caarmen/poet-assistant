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

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.settings.SettingsEvent
import ca.rmen.android.poetassistant.main.settings.SettingsScreenViewModel
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.settings.Theme

// Test tags for the interface-section rows, which are defined here:
// the legacy preference keys, so the migrated tests address the rows
// as they addressed the preferences.
const val SETTINGS_THEME_ROW_TAG = "PREF_THEME"
const val SETTINGS_LAYOUT_ROW_TAG = "PREF_LAYOUT"
const val SETTINGS_SELECTION_LOOKUP_ROW_TAG = "PREF_SELECTION_LOOKUP"

private const val SYSTEM_TTS_SETTINGS_ACTION = "com.android.settings.TTS_SETTINGS"

/**
 * The settings screen: a Compose replacement for the legacy
 * androidx.preference screen, driven by [SettingsScreenViewModel].
 *
 * The one-shot events are collected only while started, so snackbars
 * do not replay on re-entry (spec §3.4).
 *
 * @param viewModel The settings screen ViewModel.
 * @param onNavigateUp Called when the up affordance is tapped.
 * @param onOpenSystemTtsSettings Called when the system TTS settings
 * row is tapped: the host stores the restart-on-resume flag and
 * launches the intent.
 * @param onOpenAppDetailsSettings Called when a notification-permission
 * denial is permanent: the host opens the app-details settings screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsScreenViewModel,
    onNavigateUp: () -> Unit,
    onOpenSystemTtsSettings: () -> Unit,
    onOpenAppDetailsSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // The permission launcher lives in the composable: launchers need
    // a lifecycle owner (spec §3.5). The permanent-denial check needs
    // the activity (shouldShowRequestPermissionRationale).
    val requestNotificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val permanentlyDenied = !granted &&
                    (context as? android.app.Activity)?.shouldShowRequestPermissionRationale(
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == false
            viewModel.onNotificationPermissionResult(granted, permanentlyDenied)
        }
    LaunchedEffect(viewModel) {
        viewModel.notificationPermissionRequests.collect {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Document pickers (spec §3.6): the CreateDocument input passes
    // the suggested file name, as the legacy EXTRA_TITLE did.
    val exportFavoritesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri -> uri?.let(viewModel::onExportFavorites) }
    val importFavoritesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::onImportFavorites) }

    // One-shot events: collected only while started, with no replay.
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is SettingsEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.text)
                    SettingsEvent.OpenAppDetailsSettings -> onOpenAppDetailsSettings()
                }
            }
        }
    }

    // The system TTS settings row is hidden when no system activity
    // resolves the intent (legacy behavior, spec §1.3.3).
    val showSystemTtsSettingsRow = remember {
        Intent(SYSTEM_TTS_SETTINGS_ACTION).resolveActivity(context.packageManager) != null
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_activity_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(androidx.appcompat.R.string.abc_action_bar_up_description),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader(stringResource(R.string.pref_category_voice))
            SettingsVoiceSection(
                voices = state.voices,
                selectedVoiceId = state.settings.voice,
                voiceSpeed = state.settings.voiceSpeed,
                voicePitch = state.settings.voicePitch,
                showSystemTtsSettingsRow = showSystemTtsSettingsRow,
                onVoiceSelected = viewModel::onVoiceSelected,
                onVoiceSpeedChanged = viewModel::onVoiceSpeedChanged,
                onVoicePitchChanged = viewModel::onVoicePitchChanged,
                onPreviewClicked = viewModel::onPreviewClicked,
                onOpenSystemTtsSettings = onOpenSystemTtsSettings,
                modifier = Modifier.fillMaxWidth(),
            )

            SettingsSectionHeader(stringResource(R.string.pref_category_rhyming))
            SettingsSwitchRow(
                title = stringResource(R.string.all_rhymes_setting_title),
                description = stringResource(R.string.all_rhymes_setting_description),
                checked = state.settings.isAllRhymesEnabled,
                onCheckedChange = viewModel::onAllRhymesChanged,
                testTag = "PREF_ALL_RHYMES_ENABLED",
            )
            SettingsSwitchRow(
                title = stringResource(R.string.match_ao_aa_setting_title),
                description = stringResource(R.string.match_ao_aa_setting_description),
                checked = state.settings.isAOAAMatchEnabled,
                onCheckedChange = viewModel::onAOAAMatchChanged,
                testTag = "PREF_MATCH_AO_AA_ENABLED",
            )
            SettingsSwitchRow(
                title = stringResource(R.string.match_aor_ao_setting_title),
                description = stringResource(R.string.match_aor_ao_setting_description),
                checked = state.settings.isAORAOMatchEnabled,
                onCheckedChange = viewModel::onAORAOMatchChanged,
                testTag = "PREF_MATCH_AOR_AO_ENABLED",
            )

            SettingsSectionHeader(stringResource(R.string.pref_category_thesaurus))
            SettingsSwitchRow(
                title = stringResource(R.string.thesaurus_reverse_lookup_setting_title),
                description = stringResource(R.string.thesaurus_reverse_lookup_setting_description),
                checked = state.settings.isThesaurusReverseLookupEnabled,
                onCheckedChange = viewModel::onThesaurusReverseLookupChanged,
                testTag = "PREF_THESAURUS_REVERSE_LOOKUP_ENABLED",
            )

            SettingsSectionHeader(stringResource(R.string.pref_category_ui))
            SettingsRadioSection(
                title = stringResource(R.string.pref_theme_title),
                entries = listOf(
                    Theme.LIGHT to stringResource(R.string.pref_theme_value_light),
                    Theme.DARK to stringResource(R.string.pref_theme_value_dark),
                    Theme.AUTO to stringResource(R.string.pref_theme_value_auto),
                ),
                selectedEntry = state.settings.theme,
                onEntrySelected = viewModel::onThemeSelected,
                testTag = SETTINGS_THEME_ROW_TAG,
            )
            SettingsRadioSection(
                title = stringResource(R.string.pref_layout_title),
                entries = listOf(
                    Layout.CLEAN to stringResource(R.string.pref_layout_value_clean),
                    Layout.EFFICIENT to stringResource(R.string.pref_layout_value_efficient),
                ),
                selectedEntry = state.settings.layout,
                onEntrySelected = viewModel::onLayoutSelected,
                testTag = SETTINGS_LAYOUT_ROW_TAG,
            )
            SettingsSwitchRow(
                title = stringResource(R.string.pref_selection_lookup_title),
                description = stringResource(R.string.pref_selection_lookup_summary),
                checked = state.settings.isSelectionLookupEnabled,
                onCheckedChange = viewModel::onSelectionLookupChanged,
                testTag = SETTINGS_SELECTION_LOOKUP_ROW_TAG,
            )

            SettingsSectionHeader(stringResource(R.string.pref_category_notifications))
            SettingsWotdSection(
                isWotdEnabled = state.settings.isWotdEnabled,
                wotdNotificationPriority = state.settings.wotdNotificationPriority,
                onWotdEnabledChanged = viewModel::onWotdEnabledChanged,
                onWotdNotificationPrioritySelected = viewModel::onWotdNotificationPrioritySelected,
                modifier = Modifier.fillMaxWidth(),
            )

            SettingsSectionHeader(stringResource(R.string.pref_category_user_data))
            SettingsUserDataSection(
                onExportFavoritesClicked = {
                    exportFavoritesLauncher.launch(context.getString(R.string.export_favorites_default_filename))
                },
                onImportFavoritesClicked = { importFavoritesLauncher.launch(arrayOf("text/plain")) },
                onClearSearchHistoryConfirmed = viewModel::onClearSearchHistory,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
    )
}
