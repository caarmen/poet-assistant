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

import android.content.Context
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import ca.rmen.android.poetassistant.main.Tab
import org.jraf.android.prefs.DefaultBoolean
import org.jraf.android.prefs.DefaultInt
import org.jraf.android.prefs.DefaultString
import org.jraf.android.prefs.Name
import org.jraf.android.prefs.Prefs
import java.util.Locale

@Prefs
class Settings {
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
                val deprecatedSpeedPrefValue = prefs.getString(speedPrefKeyV1, "1.0").toFloat()
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

        fun getLayout(prefs: SettingsPrefs): Settings.Layout {
            return Settings.Layout.valueOf(prefs.layout.toUpperCase(Locale.US))
        }

        fun getTab(prefs: SettingsPrefs): Tab? {
            return Tab.parse(prefs.tab)
        }
    }


    @Name(PREF_VOICE)
    @DefaultString(VOICE_SYSTEM)
    var voice: String = VOICE_SYSTEM

    @Suppress("unused", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @Name(PREF_VOICE_SPEED)
    @DefaultInt(VOICE_SPEED_NORMAL)
    var voiceSpeed: Integer = VOICE_SPEED_NORMAL as Integer

    @Suppress("unused", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @Name(PREF_VOICE_PITCH)
    @DefaultInt(VOICE_PITCH_NORMAL)
    var voicePitch: Integer = VOICE_PITCH_NORMAL as Integer

    @Name(PREF_THEME)
    var theme: String = THEME_LIGHT

    @Name(PREF_LAYOUT)
    @DefaultString(PREF_DEFAULT_LAYOUT)
    var layout: String = PREF_DEFAULT_LAYOUT

    @Suppress("unused")
    @Name(PREF_SELECTION_LOOKUP)
    @DefaultBoolean(true)
    var selectionLookupEnabled: Boolean? = true

    @Suppress("unused")
    @Name(PREF_WOTD_ENABLED)
    @DefaultBoolean(true)
    var isWotdEnabled: Boolean? = true

    @Suppress("unused")
    @Name(PREF_WOTD_NOTIFICATION_PRIORITY)
    @DefaultString(PREF_WOTD_NOTIFICATION_PRIORITY_DEFAULT)
    var wotdNotificationPriority: String = PREF_WOTD_NOTIFICATION_PRIORITY_DEFAULT

    @Suppress("unused")
    @Name(PREF_ALL_RHYMES_ENABLED)
    @DefaultBoolean(false)
    var isAllRhymesEnabled: Boolean? = false

    @Suppress("unused")
    @Name(PREF_MATCH_AO_AA_ENABLED)
    @DefaultBoolean(false)
    var isAOAAMatchEnabled: Boolean? = false

    @Suppress("unused")
    @Name(PREF_MATCH_AOR_AO_ENABLED)
    @DefaultBoolean(false)
    var isAORAOMatchEnabled: Boolean? = false

    @Name(PREF_THESAURUS_REVERSE_LOOKUP_ENABLED)
    @Suppress("unused")
    @DefaultBoolean(false)
    var isThesaurusReverseLookupEnabled: Boolean? = false

    @Name(PREF_TAB)
    @DefaultString(PREF_TAB_DEFAULT)
    var tab: String = PREF_TAB_DEFAULT
}
