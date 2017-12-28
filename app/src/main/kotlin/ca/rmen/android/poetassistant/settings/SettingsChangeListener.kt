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
import android.content.Intent
import android.content.SharedPreferences
import android.support.v4.app.TaskStackBuilder
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.Theme
import ca.rmen.android.poetassistant.dagger.DaggerHelper
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary
import ca.rmen.android.poetassistant.main.dictionaries.search.ProcessTextRouter
import ca.rmen.android.poetassistant.wotd.Wotd
import javax.inject.Inject

class SettingsChangeListener(private val context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        private val TAG = Constants.TAG + SettingsChangeListener::class.java.simpleName
    }

    @Inject lateinit var mSettingsPrefs: SettingsPrefs
    @Inject lateinit var mDictionary: Dictionary

    init {
        DaggerHelper.getSettingsComponent(context).inject(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        Log.v(TAG, "onSharedPreferenceChanged: key = $key")
        when (key) {
        // When the theme changes, restart the activity
            Settings.PREF_THEME -> {
                Theme.setThemeFromSettings(mSettingsPrefs)
                restartSettingsActivity()
            }
            Settings.PREF_WOTD_ENABLED, Settings.PREF_WOTD_NOTIFICATION_PRIORITY -> {
                Wotd.setWotdEnabled(context, mDictionary, mSettingsPrefs.isWotdEnabled)
            }
            Settings.PREF_SELECTION_LOOKUP -> {
                ProcessTextRouter.setEnabled(context, mSettingsPrefs.isSelectionLookupEnabled)
                restartSettingsActivity()
            }
        }
    }

    private fun restartSettingsActivity() {
        val intent = Intent(context, SettingsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(intent)
                .startActivities()
    }

}
