/*
 * Copyright (c) 2016 Carmen Alvarez
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

import org.jraf.android.prefs.DefaultBoolean;
import org.jraf.android.prefs.DefaultString;
import org.jraf.android.prefs.DefaultStringSet;
import org.jraf.android.prefs.Name;
import org.jraf.android.prefs.Prefs;

import java.util.Set;

@Prefs
public class Settings {
    @SuppressWarnings("unused")
    public static final String THEME_LIGHT = "Light";
    public static final String THEME_DARK = "Dark";
    public static final String THEME_AUTO = "Auto";
    public static final String VOICE_SYSTEM = "VOICE_SYSTEM";
    static final String PREF_VOICE = "PREF_VOICE";
    public static final String PREF_VOICE_SPEED = "PREF_VOICE_SPEED";
    @SuppressWarnings("unused")
    private static final String VOICE_SPEED_NORMAL = "1.0";
    static final String PREF_SYSTEM_TTS_SETTINGS = "PREF_SYSTEM_TTS_SETTINGS";
    static final String PREF_THEME = "PREF_THEME";
    static final String PREF_WOTD_ENABLED = "PREF_WOTD_ENABLED";
    @SuppressWarnings("unused")
    private static final String PREF_FAVORITE_WORDS = "PREF_FAVORITE_WORDS";
    @SuppressWarnings("unused")
    private static final String PREF_SUGGESTED_WORDS = "pref_suggestions";
    @SuppressWarnings("unused")
    private static final String PREF_ALL_RHYMES_ENABLED = "PREF_ALL_RHYMES_ENABLED";

    @SuppressWarnings("unused")
    @Name(PREF_VOICE)
    @DefaultString(VOICE_SYSTEM)
    String voice;

    @SuppressWarnings("unused")
    @Name(PREF_VOICE_SPEED)
    @DefaultString(VOICE_SPEED_NORMAL)
    String voiceSpeed;

    @SuppressWarnings("unused")
    @Name(PREF_THEME)
    String theme;

    @SuppressWarnings("unused")
    @Name(PREF_WOTD_ENABLED)
    @DefaultBoolean(true)
    Boolean isWotdEnabled;

    @SuppressWarnings("unused")
    @Name(PREF_SUGGESTED_WORDS)
    @DefaultStringSet({})
    Set<String> suggestedWords;

    @SuppressWarnings("unused")
    @Name(PREF_FAVORITE_WORDS)
    @DefaultStringSet({})
    Set<String> favoriteWords;

    @SuppressWarnings("unused")
    @Name(PREF_ALL_RHYMES_ENABLED)
    @DefaultBoolean(false)
    Boolean isAllRhymesEnabled;
}
