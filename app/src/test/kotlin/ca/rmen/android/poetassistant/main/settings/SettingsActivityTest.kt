/*
 * Copyright (c) 2017 Carmen Alvarez
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
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import ca.rmen.android.poetassistant.Environment
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.settings.GeneralPreferenceFragmentImpl.Companion.PREF_CATEGORY_VOICE
import ca.rmen.android.poetassistant.settings.SettingsActivity
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.Shadows.shadowOf

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class SettingsActivityTest {
    @Rule
    @JvmField
    val hiltTestRule = HiltAndroidRule(this)

    companion object {
        private const val SYSTEM_TTS_SETTINGS_INTENT = "com.android.settings.TTS_SETTINGS"
    }

    @Test
    @Config(sdk = [27])
    fun testSystemSettings() {
        mockSystemSettingsApp()
        val activityController: ActivityController<SettingsActivity> = Robolectric.buildActivity(SettingsActivity::class.java)
        val settingsActivity = activityController.create().start().resume().visible().get()
        val settingsFragment = settingsActivity.supportFragmentManager.findFragmentById(R.id.settings_fragment) as PreferenceFragmentCompat
        assertNotNull(settingsFragment)
        val preferenceCategory = settingsFragment.findPreference<Preference>(PREF_CATEGORY_VOICE)!! as PreferenceCategory
        val preference = preferenceCategory.findPreference<Preference>(SettingsPrefs.PREF_SYSTEM_TTS_SETTINGS)!!
        preference.performClick()
        val nextIntent = shadowOf(Environment.getApplication()).nextStartedActivity
        assertNotNull(nextIntent)
        assertEquals(SYSTEM_TTS_SETTINGS_INTENT, nextIntent.action)
        activityController.pause().stop().destroy()
    }

    // https://stackoverflow.com/questions/21638455/add-resolve-info-to-robolectric-package-manager
    private fun mockSystemSettingsApp() {
        val packageManager = shadowOf(Environment.getApplication().packageManager)
        val intent = Intent(SYSTEM_TTS_SETTINGS_INTENT)

        val info = ResolveInfo()
        info.isDefault = true

        val applicationInfo = ApplicationInfo()
        applicationInfo.packageName = "com.example"
        info.activityInfo = ActivityInfo()
        info.activityInfo.applicationInfo = applicationInfo
        info.activityInfo.name = "Example"

        packageManager.addResolveInfoForIntent(intent, info)
    }
}
