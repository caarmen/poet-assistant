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
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Theme;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.wotd.Wotd;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = Constants.TAG + SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(mListener);
        super.onDestroy();
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener mListener = (sharedPreferences, key) -> {
        Log.v(TAG, "onSharedPreferenceChanged: key = " + key);
        Context context = getApplicationContext();
        if (Settings.PREF_THEME.equals(key)) {
            // When the theme changes, restart the activity
            Theme.setThemeFromSettings(getApplicationContext());
            Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(SettingsActivity.this);
            stackBuilder.addNextIntentWithParentStack(intent);
            stackBuilder.startActivities();
        } else if (Settings.PREF_WOTD_ENABLED.equals(key)) {
            Wotd.setWotdEnabled(context, SettingsPrefs.get(context).getIsWotdEnabled());
        } else if (Settings.PREF_VOICE.equals(key)) {
            Tts.getInstance(SettingsActivity.this).useVoice(SettingsPrefs.get(context).getVoice());
        }
    };

    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat {

        private static final String DIALOG_TAG = "dialog_tag";

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.pref_general);
            VoicePreference voicePreference = (VoicePreference) findPreference(Settings.PREF_VOICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                voicePreference.loadVoices();
            }
            if (voicePreference.getEntries() == null || voicePreference.getEntries().length < 2) {
                getPreferenceScreen().removePreference(voicePreference);
            }
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            if (Settings.PREF_VOICE.equals(preference.getKey())) {
                if (getFragmentManager().findFragmentByTag(DIALOG_TAG) != null) {
                    return;
                }
                VoicePreferenceDialogFragment fragment = VoicePreferenceDialogFragment.newInstance(preference.getKey());
                fragment.setTargetFragment(this, 0);
                fragment.show(getFragmentManager(), DIALOG_TAG);
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }
    }

}
