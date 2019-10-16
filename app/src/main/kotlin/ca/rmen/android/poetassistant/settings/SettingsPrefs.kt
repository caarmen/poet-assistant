/*
 * Copyright (c) 2016-2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.settings

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import androidx.core.app.NotificationCompat
import ca.rmen.android.poetassistant.main.Tab
import java.util.Locale

class SettingsPrefs(application: Application) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    enum class NotificationPriority(val priority: Int) {
        MAX(NotificationCompat.PRIORITY_MAX),
        HIGH(NotificationCompat.PRIORITY_HIGH),
        DEFAULT(NotificationCompat.PRIORITY_DEFAULT),
        LOW(NotificationCompat.PRIORITY_LOW),
        MIN(NotificationCompat.PRIORITY_MIN)
    }

    enum class Layout {
        CLEAN,
        EFFICIENT
    }

    companion object {
        const val THEME_LIGHT = "Light"
        const val THEME_DARK = "Dark"
        const val THEME_AUTO = "Auto"
        const val VOICE_SYSTEM = "VOICE_SYSTEM"
        const val PREF_VOICE = "PREF_VOICE"
        // v1 for the speed was a string of values 0.25, 0.5, 1.0, 1.5, or 2.0
        // v2 is a float between 0 and 200
        const val PREF_VOICE_SPEED = "PREF_VOICE_SPEED_V3"
        const val PREF_VOICE_PITCH = "PREF_VOICE_PITCH_V3"
        const val PREF_LAYOUT = "PREF_LAYOUT"
        private const val PREF_DEFAULT_LAYOUT = "Efficient"
        private const val VOICE_SPEED_NORMAL = 100
        private const val VOICE_PITCH_NORMAL = 100
        const val PREF_SYSTEM_TTS_SETTINGS = "PREF_SYSTEM_TTS_SETTINGS"
        const val PREF_VOICE_PREVIEW = "PREF_VOICE_PREVIEW"
        const val PREF_THEME = "PREF_THEME"
        const val PREF_SELECTION_LOOKUP = "PREF_SELECTION_LOOKUP"
        const val PREF_WOTD_ENABLED = "PREF_WOTD_ENABLED"
        const val PREF_WOTD_NOTIFICATION_PRIORITY = "PREF_WOTD_NOTIFICATION_PRIORITY"
        private const val PREF_WOTD_NOTIFICATION_PRIORITY_DEFAULT = "default"
        private const val PREF_ALL_RHYMES_ENABLED = "PREF_ALL_RHYMES_ENABLED"
        private const val PREF_MATCH_AO_AA_ENABLED = "PREF_MATCH_AO_AA_ENABLED"
        private const val PREF_MATCH_AOR_AO_ENABLED = "PREF_MATCH_AOR_AO_ENABLED"
        private const val PREF_THESAURUS_REVERSE_LOOKUP_ENABLED = "PREF_THESAURUS_REVERSE_LOOKUP_ENABLED"
        private const val PREF_TAB = "PREF_TAB"
        private const val PREF_TAB_DEFAULT = "RHYMER"

        fun migrateSettings(context: Context) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val speedPrefKeyV1 = "PREF_VOICE_SPEED"
            val speedPrefKeyV2 = "PREF_VOICE_SPEED_V2"
            val pitchPrefKeyV1 = "PREF_VOICE_PITCH"
            // V1: we had speed as an enum (string) value.
            if (prefs.contains(speedPrefKeyV1)) {
                val deprecatedSpeedPrevValueStr = prefs.getString(speedPrefKeyV1, "1.0")
                val deprecatedSpeedPrefValue = deprecatedSpeedPrevValueStr?.toFloat() ?: 1.0f
                val newSpeedPrefValue = (deprecatedSpeedPrefValue * 100).toInt()
                prefs.edit().putInt(PREF_VOICE_SPEED, newSpeedPrefValue).remove(speedPrefKeyV1).apply()
            }
            // V2 speed/V1 pitch: we had a custom SeekBarPreference which saved a float value.
            if (prefs.contains(speedPrefKeyV2)) {
                val deprecatedSpeedPrefValue = prefs.getFloat(speedPrefKeyV2, 100f)
                prefs.edit().putInt(PREF_VOICE_SPEED, deprecatedSpeedPrefValue.toInt()).remove(speedPrefKeyV2).apply()
            }
            if (prefs.contains(pitchPrefKeyV1)) {
                val deprecatedPitchPrefValue = prefs.getFloat(pitchPrefKeyV1, 100f)
                prefs.edit().putInt(PREF_VOICE_PITCH, deprecatedPitchPrefValue.toInt()).remove(pitchPrefKeyV1).apply()
            }
        }

        fun getLayout(prefs: SettingsPrefs): ca.rmen.android.poetassistant.settings.SettingsPrefs.Layout {
            return SettingsPrefs.Layout.valueOf(prefs.layout.toUpperCase(Locale.US))
        }

        fun getTab(prefs: SettingsPrefs): Tab? {
            return Tab.parse(prefs.tab)
        }
    }


    var voice: String
        get() {
            return sharedPreferences.getString(PREF_VOICE, VOICE_SYSTEM) ?: VOICE_SYSTEM
        }
        set(newValue) {
            sharedPreferences.edit().putString(PREF_VOICE, newValue).apply()
        }

    var voiceSpeed: Int
        get() {
            return sharedPreferences.getInt(PREF_VOICE_SPEED, VOICE_SPEED_NORMAL)
        }
        set(newValue) {
            sharedPreferences.edit().putInt(PREF_VOICE_SPEED, newValue).apply()
        }
    var voicePitch: Int
        get() {
            return sharedPreferences.getInt(PREF_VOICE_PITCH, VOICE_PITCH_NORMAL)
        }
        set(newValue) {
            sharedPreferences.edit().putInt(PREF_VOICE_PITCH, newValue).apply()
        }

    var theme: String
        get() {
            return sharedPreferences.getString(PREF_THEME, THEME_LIGHT) ?: THEME_LIGHT
        }
        set(newValue) {
            sharedPreferences.edit().putString(PREF_THEME, newValue).apply()
        }
    var layout: String
        get() {
            return sharedPreferences.getString(PREF_LAYOUT, PREF_DEFAULT_LAYOUT)
                    ?: PREF_DEFAULT_LAYOUT
        }
        set(newValue) {
            sharedPreferences.edit().putString(PREF_LAYOUT, newValue).apply()
        }

    var isSelectionLookupEnabled: Boolean
        get () {
            return sharedPreferences.getBoolean(PREF_SELECTION_LOOKUP, true)
        }
        set(newValue) {
            sharedPreferences.edit().putBoolean(PREF_SELECTION_LOOKUP, newValue).apply()
        }

    var isWotdEnabled: Boolean
        get () {
            return sharedPreferences.getBoolean(PREF_WOTD_ENABLED, true)
        }
        set(newValue) {
            sharedPreferences.edit().putBoolean(PREF_WOTD_ENABLED, newValue).apply()
        }

    var wotdNotificationPriority: String
        get() {
            return sharedPreferences.getString(PREF_WOTD_NOTIFICATION_PRIORITY, PREF_WOTD_NOTIFICATION_PRIORITY_DEFAULT)
                    ?: PREF_WOTD_NOTIFICATION_PRIORITY_DEFAULT
        }
        set(newValue) {
            sharedPreferences.edit().putString(PREF_WOTD_NOTIFICATION_PRIORITY, newValue).apply()
        }

    var isAllRhymesEnabled: Boolean
        get () {
            return sharedPreferences.getBoolean(PREF_ALL_RHYMES_ENABLED, false)
        }
        set(newValue) {
            sharedPreferences.edit().putBoolean(PREF_ALL_RHYMES_ENABLED, newValue).apply()
        }

    var isAOAAMatchEnabled: Boolean
        get () {
            return sharedPreferences.getBoolean(PREF_MATCH_AO_AA_ENABLED, false)
        }
        set(newValue) {
            sharedPreferences.edit().putBoolean(PREF_MATCH_AO_AA_ENABLED, newValue).apply()
        }

    var isAORAOMatchEnabled: Boolean
        get () {
            return sharedPreferences.getBoolean(PREF_MATCH_AOR_AO_ENABLED, false)
        }
        set(newValue) {
            sharedPreferences.edit().putBoolean(PREF_MATCH_AOR_AO_ENABLED, newValue).apply()
        }

    var isThesaurusReverseLookupEnabled: Boolean
        get () {
            return sharedPreferences.getBoolean(PREF_THESAURUS_REVERSE_LOOKUP_ENABLED, false)
        }
        set(newValue) {
            sharedPreferences.edit().putBoolean(PREF_THESAURUS_REVERSE_LOOKUP_ENABLED, newValue).apply()
        }

    var tab: String
        get() {
            return sharedPreferences.getString(PREF_TAB, PREF_TAB_DEFAULT)
                    ?: PREF_TAB_DEFAULT
        }
        set(newValue) {
            sharedPreferences.edit().putString(PREF_TAB, newValue).apply()
        }
}
