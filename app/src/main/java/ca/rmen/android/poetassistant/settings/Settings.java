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

package ca.rmen.android.poetassistant.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.NotificationCompat;

import org.jraf.android.prefs.DefaultBoolean;
import org.jraf.android.prefs.DefaultInt;
import org.jraf.android.prefs.DefaultString;
import org.jraf.android.prefs.Name;
import org.jraf.android.prefs.Prefs;

import java.util.Locale;

@Prefs
public class Settings {

    @SuppressWarnings("unused")
    public enum NotificationPriority {
        MAX(NotificationCompat.PRIORITY_MAX),
        HIGH(NotificationCompat.PRIORITY_HIGH),
        DEFAULT(NotificationCompat.PRIORITY_DEFAULT),
        LOW(NotificationCompat.PRIORITY_LOW),
        MIN(NotificationCompat.PRIORITY_MIN);
        public final int priority;
        NotificationPriority(int priority) {
            this.priority = priority;
        }
    }

    @SuppressWarnings("unused")
    public static final String THEME_LIGHT = "Light";
    public static final String THEME_DARK = "Dark";
    public static final String THEME_AUTO = "Auto";
    public static final String VOICE_SYSTEM = "VOICE_SYSTEM";
    public static final String PREF_VOICE = "PREF_VOICE";
    // v1 for the speed was a string of values 0.25, 0.5, 1.0, 1.5, or 2.0
    // v2 is a float between 0 and 200
    public static final String PREF_VOICE_SPEED = "PREF_VOICE_SPEED_V3";
    public static final String PREF_VOICE_PITCH = "PREF_VOICE_PITCH_V3";
    public static final String PREF_LAYOUT = "PREF_LAYOUT";
    @SuppressWarnings("unused")
    private static final int VOICE_SPEED_NORMAL = 100;
    @SuppressWarnings("unused")
    private static final int VOICE_PITCH_NORMAL = 100;
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public static final String PREF_SYSTEM_TTS_SETTINGS = "PREF_SYSTEM_TTS_SETTINGS";
    static final String PREF_VOICE_PREVIEW = "PREF_VOICE_PREVIEW";
    static final String PREF_THEME = "PREF_THEME";
    static final String PREF_SELECTION_LOOKUP = "PREF_SELECTION_LOOKUP";
    static final String PREF_WOTD_ENABLED = "PREF_WOTD_ENABLED";
    static final String PREF_WOTD_NOTIFICATION_PRIORITY = "PREF_WOTD_NOTIFICATION_PRIORITY";
    @SuppressWarnings("unused")
    private static final String PREF_WOTD_NOTIFICATION_PRIORITY_DEFAULT = "default";
    @SuppressWarnings("unused")
    private static final String PREF_ALL_RHYMES_ENABLED = "PREF_ALL_RHYMES_ENABLED";
    @SuppressWarnings("unused")
    private static final String PREF_MATCH_AO_AA_ENABLED = "PREF_MATCH_AO_AA_ENABLED";
    @SuppressWarnings("unused")
    private static final String PREF_MATCH_AOR_AO_ENABLED = "PREF_MATCH_AOR_AO_ENABLED";
    @SuppressWarnings("unused")
    private static final String PREF_THESAURUS_REVERSE_LOOKUP_ENABLED = "PREF_THESAURUS_REVERSE_LOOKUP_ENABLED";


    @SuppressWarnings("unused")
    @Name(PREF_VOICE)
    @DefaultString(VOICE_SYSTEM)
    String voice;

    @SuppressWarnings("unused")
    @Name(PREF_VOICE_SPEED)
    @DefaultInt(VOICE_SPEED_NORMAL)
    Integer voiceSpeed;

    @SuppressWarnings("unused")
    @Name(PREF_VOICE_PITCH)
    @DefaultInt(VOICE_PITCH_NORMAL)
    Integer voicePitch;

    @SuppressWarnings("unused")
    @Name(PREF_THEME)
    String theme;

    @SuppressWarnings("unused")
    @Name(PREF_LAYOUT)
    @DefaultString("Efficient")
    String layout;

    @SuppressWarnings("unused")
    @Name(PREF_SELECTION_LOOKUP)
    @DefaultBoolean(true)
    Boolean selectionLookupEnabled;

    @SuppressWarnings("unused")
    @Name(PREF_WOTD_ENABLED)
    @DefaultBoolean(true)
    Boolean isWotdEnabled;

    @SuppressWarnings("unused")
    @Name(PREF_WOTD_NOTIFICATION_PRIORITY)
    @DefaultString(PREF_WOTD_NOTIFICATION_PRIORITY_DEFAULT)
    String wotdNotificationPriority;

    @SuppressWarnings("unused")
    @Name(PREF_ALL_RHYMES_ENABLED)
    @DefaultBoolean(false)
    Boolean isAllRhymesEnabled;

    @SuppressWarnings("unused")
    @Name(PREF_MATCH_AO_AA_ENABLED)
    @DefaultBoolean(false)
    Boolean isAOAAMatchEnabled;

    @SuppressWarnings("unused")
    @Name(PREF_MATCH_AOR_AO_ENABLED)
    @DefaultBoolean(false)
    Boolean isAORAOMatchEnabled;

    @SuppressWarnings("unused")
    @Name(PREF_THESAURUS_REVERSE_LOOKUP_ENABLED)
    @DefaultBoolean(false)
    Boolean isThesaurusReverseLookupEnabled;

    public static void migrateSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String speedPrefKeyV1 = "PREF_VOICE_SPEED";
        String speedPrefKeyV2 = "PREF_VOICE_SPEED_V2";
        String pitchPrefKeyV1 = "PREF_VOICE_PITCH";
        // V1: we had speed as an enum (string) value.
        if (prefs.contains(speedPrefKeyV1)) {
            float deprecatedSpeedPrefValue = Float.valueOf(prefs.getString(speedPrefKeyV1, "1.0"));
            int newSpeedPrefValue = (int) (deprecatedSpeedPrefValue * 100);
            prefs.edit()
                    .putInt(PREF_VOICE_SPEED, newSpeedPrefValue)
                    .remove(speedPrefKeyV1)
                    .apply();
        }
        // V2 speed/V1 pitch: we had a custom SeekBarPreference which saved a float value.
        if (prefs.contains(speedPrefKeyV2)) {
            float deprecatedSpeedPrefValue = prefs.getFloat(speedPrefKeyV2, 100f);
            prefs.edit()
                    .putInt(PREF_VOICE_SPEED, (int) deprecatedSpeedPrefValue)
                    .remove(speedPrefKeyV2)
                    .apply();

        }
        if (prefs.contains(pitchPrefKeyV1)) {
            float deprecatedPitchPrefValue = prefs.getFloat(pitchPrefKeyV1, 100f);
            prefs.edit()
                    .putInt(PREF_VOICE_PITCH, (int) deprecatedPitchPrefValue)
                    .remove(pitchPrefKeyV1)
                    .apply();

        }
    }

    public static Layout getLayout(SettingsPrefs prefs) {
        return Layout.valueOf(prefs.getLayout().toUpperCase(Locale.US));
    }

    public enum Layout {
        @SuppressWarnings("unused")CLEAN,
        EFFICIENT
    }

}
