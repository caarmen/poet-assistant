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

package ca.rmen.android.poetassistant.main.settings;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowPackageManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import ca.rmen.android.poetassistant.Environment;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.settings.SettingsActivity;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.HiltTestApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

@HiltAndroidTest
@Config(application = HiltTestApplication.class)
@RunWith(RobolectricTestRunner.class)
public class SettingsActivityTest {
    @Rule(order = 0)
    public HiltAndroidRule hiltTestRule = new HiltAndroidRule(this);
    private static final String SYSTEM_TTS_SETTINGS_INTENT = "com.android.settings.TTS_SETTINGS";
    @Test
    @Config(sdk=27)
    public void testSystemSettings() {
        mockSystemSettingsApp();
        ActivityController<SettingsActivity> activityController = Robolectric.buildActivity(SettingsActivity.class);
        SettingsActivity settingsActivity = activityController.create().start().resume().visible().get();
        PreferenceFragmentCompat settingsFragment = (PreferenceFragmentCompat) settingsActivity.getSupportFragmentManager().findFragmentById(R.id.settings_fragment);
        assertNotNull(settingsFragment);
        PreferenceCategory preferenceCategory = (PreferenceCategory) settingsFragment.findPreference(SettingsActivity.GeneralPreferenceFragment.PREF_CATEGORY_VOICE);
        Preference preference = preferenceCategory.findPreference(SettingsPrefs.PREF_SYSTEM_TTS_SETTINGS);
        preference.performClick();
        Intent nextIntent =  shadowOf(Environment.getApplication()).getNextStartedActivity();
        assertNotNull(nextIntent);
        assertEquals(SYSTEM_TTS_SETTINGS_INTENT, nextIntent.getAction());
        activityController.pause().stop().destroy();
    }

    // https://stackoverflow.com/questions/21638455/add-resolve-info-to-robolectric-package-manager
    private void mockSystemSettingsApp() {
        ShadowPackageManager packageManager = shadowOf(Environment.getApplication().getPackageManager());
        Intent intent = new Intent(SYSTEM_TTS_SETTINGS_INTENT);

        ResolveInfo info = new ResolveInfo();
        info.isDefault = true;

        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.packageName = "com.example";
        info.activityInfo = new ActivityInfo();
        info.activityInfo.applicationInfo = applicationInfo;
        info.activityInfo.name = "Example";

        packageManager.addResolveInfoForIntent(intent, info);
    }
}
