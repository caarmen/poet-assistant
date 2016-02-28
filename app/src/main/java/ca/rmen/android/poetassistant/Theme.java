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

package ca.rmen.android.poetassistant;

import android.app.Activity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import ca.rmen.android.poetassistant.settings.Settings;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;

public class Theme {
    private static final String TAG = Constants.TAG + Theme.class.getSimpleName();

    /**
     * If the app isn't using the theme in the shared preferences, this
     * will restart the activity and set the global flag to use the right theme.
     * Logically, this might make more sense in an application class.
     */
    public static void checkTheme(Activity activity) {

        SettingsPrefs settings = SettingsPrefs.get(activity);
        if (Settings.THEME_DARK.equals(settings.getTheme())) {
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
                Log.v(TAG, "Restarting in dark mode");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                activity.recreate();
            }
        } else {
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
                Log.v(TAG, "Restarting in light mode");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                activity.recreate();
            }
        }
    }
}
