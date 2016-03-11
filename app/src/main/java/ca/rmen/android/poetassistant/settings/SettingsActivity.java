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


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Theme;
import ca.rmen.android.poetassistant.wotd.Wotd;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Theme.checkTheme(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        GeneralPreferenceFragment fragment = new GeneralPreferenceFragment();
        getSupportFragmentManager().
                beginTransaction().
                replace(android.R.id.content, fragment).
                commit();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mListener);
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Context context = getApplicationContext();
            if (Settings.PREF_THEME.equals(key)) {
                // When the theme changes, restart the app
                Intent intent = new Intent(context, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntentWithParentStack(intent);
                stackBuilder.startActivities();
            } else if (Settings.PREF_WOTD_ENABLED.equals(key)) {
                Wotd.setWotdEnabled(context, SettingsPrefs.get(context).getIsWotdEnabled());
            }
        }
    };

    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.pref_general);
        }
    }

}
