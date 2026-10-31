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

package ca.rmen.android.poetassistant.main.settings

import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import ca.rmen.android.poetassistant.Tts
import ca.rmen.android.poetassistant.main.settings.ui.composables.SettingsScreen
import ca.rmen.android.poetassistant.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The settings screen: a thin Compose host (the
 * NewReaderFragment-to-ReaderFragment precedent: the legacy
 * SettingsActivity stays untouched as dead code until the Step C
 * deletion PR, which renames this class).
 *
 * Tts is the sanctioned legacy dependency (spec §0): the host holds it
 * for the lifecycle stop/restart only, and calls only its public
 * stop()/restart() methods.
 */
@AndroidEntryPoint
class NewSettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var tts: Tts

    private val viewModel: SettingsScreenViewModel by viewModels()

    /** Set when the user opens the system TTS settings: Tts restarts on return. */
    private var restartTtsOnResume = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        volumeControlStream = AudioManager.STREAM_MUSIC
        setContent {
            AppTheme {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateUp = { onSupportNavigateUp() },
                    onOpenSystemTtsSettings = ::openSystemTtsSettings,
                    onOpenAppDetailsSettings = ::openAppDetailsSettings,
                )
            }
        }
    }

    private fun openSystemTtsSettings() {
        restartTtsOnResume = true
        startActivity(Intent(SYSTEM_TTS_SETTINGS_ACTION))
    }

    private fun openAppDetailsSettings() {
        val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        settingsIntent.data = Uri.fromParts("package", packageName, null)
        startActivity(settingsIntent)
    }

    override fun onResume() {
        super.onResume()
        if (restartTtsOnResume) {
            tts.restart()
            restartTtsOnResume = false
        }
    }

    override fun onPause() {
        tts.stop()
        super.onPause()
    }

    /**
     * Honors the manifest parentActivityName, matching the up
     * semantics the legacy entry preserved (spec §3.6).
     */
    override fun onSupportNavigateUp(): Boolean {
        NavUtils.navigateUpFromSameTask(this)
        return true
    }

    private companion object {
        const val SYSTEM_TTS_SETTINGS_ACTION = "com.android.settings.TTS_SETTINGS"
    }
}
