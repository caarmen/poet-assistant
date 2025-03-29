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


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab;
import static ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.scrollToPreference;

import android.os.Build;
import android.view.View;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.MainActivity;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.HiltTestApplication;

@LargeTest
@HiltAndroidTest
@Config(application = HiltTestApplication.class)
@RunWith(AndroidJUnit4.class)
public class RandomWordTest {
    @Rule(order = 0)
    public HiltAndroidRule hiltTestRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public PoetAssistantActivityTestRule<MainActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(MainActivity.class, true);

    @Test
    public void randomWordTest() {
        openMenuItem(R.string.action_random_word);
        checkTitleStripOrTab(mActivityTestRule.getActivity(), R.string.tab_dictionary);
        onView(allOf(withId(R.id.tv_list_header), isDisplayed())).check(matches(withText(not(isEmptyOrNullString()))));
    }

    @Test
    public void wotdNotificationPriorityPresenceTest() {
        openMenuItem(R.string.action_settings);
        Matcher<View> prioritySettingMatcher = withText(R.string.wotd_setting_system_notification_priority_title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            onView(prioritySettingMatcher).check(doesNotExist());
        } else {
            onView(withId(R.id.wotd_recycler_view))
                    .perform(scrollTo(hasDescendant(prioritySettingMatcher)));
            onView(prioritySettingMatcher).check(matches(isDisplayed()));
        }
    }

    @Test
    public void wotdNotificationPriorityEnabledTest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return; // The setting isn't available on O+
        }
        openMenuItem(R.string.action_settings);
        scrollToPreference(R.string.wotd_setting_system_notification_priority_title);
        onView(withText(R.string.wotd_setting_system_notification_priority_title)).check(matches(not(isEnabled())));
        clickPreference(R.string.wotd_setting_title);
        onView(withText(R.string.wotd_setting_system_notification_priority_title)).check(matches(isEnabled()));
    }

    @Test
    public void wotdNotificationPrioritySelectionTest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return; // The setting isn't available on O+
        }
        openMenuItem(R.string.action_settings);
        scrollToPreference(R.string.wotd_setting_system_notification_priority_title);
        clickPreference(R.string.wotd_setting_title);

        clickPreference(R.string.wotd_setting_system_notification_priority_title);
        onView(withId(R.id.select_dialog_listview)).check(matches(hasChildCount(5)));
        onView(withText(R.string.wotd_setting_system_notification_priority_label_default)).check(matches(isChecked()));

        onView(withText(R.string.wotd_setting_system_notification_priority_label_max)).perform(click());
        clickPreference(R.string.wotd_setting_system_notification_priority_title);
        onView(withText(R.string.wotd_setting_system_notification_priority_label_max)).check(matches(isChecked()));

        onView(withText(R.string.wotd_setting_system_notification_priority_label_high)).perform(click());
        clickPreference(R.string.wotd_setting_system_notification_priority_title);
        onView(withText(R.string.wotd_setting_system_notification_priority_label_high)).check(matches(isChecked()));

        onView(withText(R.string.wotd_setting_system_notification_priority_label_low)).perform(click());
        clickPreference(R.string.wotd_setting_system_notification_priority_title);
        onView(withText(R.string.wotd_setting_system_notification_priority_label_low)).check(matches(isChecked()));

        onView(withText(R.string.wotd_setting_system_notification_priority_label_min)).perform(click());
        clickPreference(R.string.wotd_setting_system_notification_priority_title);
        onView(withText(R.string.wotd_setting_system_notification_priority_label_min)).check(matches(isChecked()));
    }
}
