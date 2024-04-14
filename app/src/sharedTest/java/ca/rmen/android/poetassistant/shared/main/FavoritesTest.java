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

package ca.rmen.android.poetassistant.shared.main;


import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertTrue;
import static ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;

import android.annotation.TargetApi;
import android.os.Build;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.runner.lifecycle.Stage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.EnumSet;
import java.util.Set;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.MainActivity;
import ca.rmen.android.poetassistant.main.rules.ActivityStageIdlingResource;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;
import ca.rmen.android.poetassistant.main.rules.RetryTestRule;
import ca.rmen.android.poetassistant.settings.SettingsActivity;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FavoritesTest {

    @Rule
    public RetryTestRule retry = new RetryTestRule();

    @Rule
    public PoetAssistantActivityTestRule<MainActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(MainActivity.class, true);

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void exportTest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        openMenuItem(R.string.action_settings);
        clickPreference(R.string.action_export_favorites);
        checkActivityHidden(SettingsActivity.class.getName());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void importTest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        openMenuItem(R.string.action_settings);
        clickPreference(R.string.action_import_favorites);
        checkActivityHidden(SettingsActivity.class.getName());
    }

    private void checkActivityHidden(String activityClassName) {
        // Wait for the activity to pause
        Set<Stage> stages = EnumSet.of(Stage.PAUSED, Stage.STOPPED, Stage.DESTROYED);
        ActivityStageIdlingResource waitForActivityPause =
                new ActivityStageIdlingResource(activityClassName, stages);
        IdlingRegistry.getInstance().register(waitForActivityPause);
        getInstrumentation().runOnMainSync(() -> {
            assertTrue("activity " + activityClassName + " not paused or stopped", ActivityStageIdlingResource.isActivityInStages(activityClassName, stages));
            IdlingRegistry.getInstance().unregister(waitForActivityPause);
        });
    }
}

