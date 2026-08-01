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

package ca.rmen.android.poetassistant.shared.main

import android.annotation.TargetApi
import android.os.Build
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.runner.lifecycle.Stage
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.MainActivity
import ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference
import ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem
import ca.rmen.android.poetassistant.main.rules.ActivityStageIdlingResource
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import ca.rmen.android.poetassistant.settings.SettingsActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.EnumSet

@LargeTest
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(AndroidJUnit4::class)
class FavoritesTest {

    @get:Rule(order = 0)
    val hiltTestRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> =
        PoetAssistantActivityTestRule(MainActivity::class.java, true)

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    fun exportTest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }
        openMenuItem(R.string.action_settings)
        clickPreference(R.string.action_export_favorites)
        checkActivityHidden(SettingsActivity::class.java.name)
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    fun importTest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }
        openMenuItem(R.string.action_settings)
        clickPreference(R.string.action_import_favorites)
        checkActivityHidden(SettingsActivity::class.java.name)
    }

    private fun checkActivityHidden(activityClassName: String) {
        // Wait for the activity to pause
        val stages: Set<Stage> = EnumSet.of(Stage.PAUSED, Stage.STOPPED, Stage.DESTROYED)
        val waitForActivityPause = ActivityStageIdlingResource(activityClassName, stages)
        IdlingRegistry.getInstance().register(waitForActivityPause)
        getInstrumentation().runOnMainSync {
            assertTrue("activity $activityClassName not paused or stopped",
                ActivityStageIdlingResource.isActivityInStages(activityClassName, stages))
            IdlingRegistry.getInstance().unregister(waitForActivityPause)
        }
    }
}
