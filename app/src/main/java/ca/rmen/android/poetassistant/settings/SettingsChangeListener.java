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
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.Theme;
import ca.rmen.android.poetassistant.dagger.DaggerHelper;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary;
import ca.rmen.android.poetassistant.main.dictionaries.search.ProcessTextRouter;
import ca.rmen.android.poetassistant.wotd.Wotd;

public class SettingsChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = Constants.TAG + SettingsChangeListener.class.getSimpleName();
    @Inject
    SettingsPrefs mSettingsPrefs;
    @Inject
    Dictionary mDictionary;
    private final Context mContext;

    SettingsChangeListener(Context context) {
        mContext = context.getApplicationContext();
        DaggerHelper.INSTANCE.getSettingsComponent(mContext).inject(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.v(TAG, "onSharedPreferenceChanged: key = " + key);
        if (Settings.PREF_THEME.equals(key)) {
            // When the theme changes, restart the activity
            Theme.INSTANCE.setThemeFromSettings(mSettingsPrefs);
            restartSettingsActivity();
        } else if (Settings.PREF_WOTD_ENABLED.equals(key) || Settings.PREF_WOTD_NOTIFICATION_PRIORITY.equals(key)) {
            Wotd.INSTANCE.setWotdEnabled(mContext, mDictionary, mSettingsPrefs.getIsWotdEnabled());
        } else if (Settings.PREF_SELECTION_LOOKUP.equals(key)) {
            ProcessTextRouter.INSTANCE.setEnabled(mContext, mSettingsPrefs.isSelectionLookupEnabled());
            restartSettingsActivity();
        }
    }

    private void restartSettingsActivity(){
        Intent intent = new Intent(mContext, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addNextIntentWithParentStack(intent);
        stackBuilder.startActivities();
    }
}
