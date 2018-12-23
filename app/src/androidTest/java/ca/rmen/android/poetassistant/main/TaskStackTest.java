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

package ca.rmen.android.poetassistant.main;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.filters.LargeTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.runner.lifecycle.Stage;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.rules.ActivityStageIdlingResource;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;
import ca.rmen.android.poetassistant.main.rules.RetryTestRule;
import ca.rmen.android.poetassistant.settings.SettingsActivity;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkFirstDefinition;
import static ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab;
import static ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference;

/**
 * Test for these issues:
 * https://github.com/caarmen/poet-assistant/issues/19
 * https://github.com/caarmen/poet-assistant/issues/81
 */
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class TaskStackTest {

    @Rule
    public RetryTestRule retry = new RetryTestRule();

    @Rule
    public PoetAssistantActivityTestRule<SettingsActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(SettingsActivity.class, false);

    @Test
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void dictionaryDeepLinkAfterThemeChangeTest() {
        deepLinkAfterThemeChangeTest("poetassistant://dictionary/muffin");
    }

    @Test
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void queryDeepLinkAfterThemeChangeTest() {
        deepLinkAfterThemeChangeTest("poetassistant://query/muffin");
    }

    private void deepLinkAfterThemeChangeTest(String deepLinkUrl) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        // Change the theme
        Intent intent = new Intent();
        mActivityTestRule.launchActivity(intent);
        clickPreference(R.string.pref_theme_title);
        onView(withText(R.string.pref_theme_value_light)).check(matches(isChecked()));
        onView(withText(R.string.pref_theme_value_dark)).perform(click());

        // Open a deep link
        getInstrumentation().getUiAutomation().executeShellCommand("am start -a android.intent.action.VIEW -d " + deepLinkUrl);

        // Wait for the MainActivity to appear
        ActivityStageIdlingResource waitForMainActivity = new ActivityStageIdlingResource(
                MainActivity.class.getName(),
                Stage.RESUMED);
        IdlingRegistry.getInstance().register(waitForMainActivity);

        // Check the results
        Activity activity = mActivityTestRule.getActivity();
        checkTitleStripOrTab(activity, R.string.tab_dictionary);
        checkFirstDefinition("a sweet quick bread baked in a cup-shaped pan");
        IdlingRegistry.getInstance().unregister(waitForMainActivity);
    }

}
