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

package ca.rmen.android.poetassistant.settings.ui.composables

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ca.rmen.android.poetassistant.settings.SettingsScreenViewModel
import ca.rmen.android.poetassistant.settings.SettingsSnackbar
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.settings.Theme

// Test tags for the interface-section rows, which are defined here.
const val SETTINGS_THEME_ROW_TAG = "PREF_THEME"
const val SETTINGS_LAYOUT_ROW_TAG = "PREF_LAYOUT"
const val SETTINGS_SELECTION_LOOKUP_ROW_TAG = "PREF_SELECTION_LOOKUP"

private const val SYSTEM_TTS_SETTINGS_ACTION = "com.android.settings.TTS_SETTINGS"

// A compile-time inlined string constant: safe to reference on any API
// level. Every use is guarded by an SDK_INT check.
@SuppressLint("InlinedApi")
private const val POST_NOTIFICATIONS = Manifest.permission.POST_NOTIFICATIONS

/**
 * The settings screen, driven by [SettingsScreenViewModel].
 *
 * The one-shot events are collected only while started, so snackbars
 * do not replay on re-entry.
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

    // The permission flow lives in the composable: launchers need a
    // lifecycle owner, the permission check needs a context, and the
    // permanent-denial check needs the activity
    // (shouldShowRequestPermissionRationale).
    val requestNotificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                viewModel.onWotdEnabledChanged(true)
            } else {
                // No pref write: the switch reverts by itself (the
                // setting stays false).
                val permanentlyDenied =
                    (context as? Activity)?.shouldShowRequestPermissionRationale(POST_NOTIFICATIONS) == false
                if (permanentlyDenied) onOpenAppDetailsSettings()
            }
        }

    // The permission gate runs before the pref write: with the
    // permission not granted, the pref is not written, the switch
    // reverts by itself, and the permission is requested instead.
    fun onWotdEnabledChanged(enabled: Boolean) {
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (enabled && !permissionGranted) {
            requestNotificationPermissionLauncher.launch(POST_NOTIFICATIONS)
        } else {
            viewModel.onWotdEnabledChanged(enabled)
        }
    }

    // Document pickers: the CreateDocument input passes the suggested
    // file name to the system picker.
    val exportFavoritesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri -> uri?.let(viewModel::onExportFavorites) }
    val importFavoritesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::onImportFavorites) }

    // The suggested file name passed to the export document picker.
    val exportFavoritesDefaultFilename = stringResource(R.string.export_favorites_default_filename)

    // One-shot snackbars: collected only while started, with no
    // replay. The pending snackbar is hoisted to state so its text is
    // resolved in composition, where stringResource is
    // configuration-aware.
    var pendingSnackbar by remember { mutableStateOf<SettingsSnackbar?>(null) }
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.snackbars.collect { snackbar ->
                pendingSnackbar = snackbar
            }
        }
    }
    pendingSnackbar?.let { snackbar ->
        // Nulls format as "null", like context.getString would.
        val snackbarText = stringResource(
            snackbar.resId,
            *snackbar.formatArgs.map { it ?: "null" }.toTypedArray()
        )
        LaunchedEffect(snackbar) {
            snackbarHostState.showSnackbar(snackbarText)
            pendingSnackbar = null
        }
    }

    // The system TTS settings row is hidden when no system activity
    // resolves the intent.
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
                            contentDescription = stringResource(R.string.abc_action_bar_up_description),
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
                choices = listOf(
                    SettingsChoice(Theme.LIGHT, stringResource(R.string.pref_theme_value_light)),
                    SettingsChoice(Theme.DARK, stringResource(R.string.pref_theme_value_dark)),
                    SettingsChoice(Theme.AUTO, stringResource(R.string.pref_theme_value_auto)),
                ),
                selectedEntry = state.settings.theme,
                onEntrySelected = viewModel::onThemeSelected,
                testTag = SETTINGS_THEME_ROW_TAG,
            )
            SettingsRadioSection(
                title = stringResource(R.string.pref_layout_title),
                choices = listOf(
                    SettingsChoice(Layout.CLEAN, stringResource(R.string.pref_layout_value_clean)),
                    SettingsChoice(Layout.EFFICIENT, stringResource(R.string.pref_layout_value_efficient)),
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
                onWotdEnabledChanged = { onWotdEnabledChanged(it) },
                onWotdNotificationPrioritySelected = viewModel::onWotdNotificationPrioritySelected,
                modifier = Modifier.fillMaxWidth(),
            )

            SettingsSectionHeader(stringResource(R.string.pref_category_user_data))
            SettingsUserDataSection(
                onExportFavoritesClicked = {
                    exportFavoritesLauncher.launch(exportFavoritesDefaultFilename)
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
