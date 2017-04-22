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
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.CustomViewMatchers.childAtPosition;
import static ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripCenterTitle;
import static ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RandomWordTest {
    @Rule
    public PoetAssistantActivityTestRule<MainActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(MainActivity.class, true);

    @Test
    public void openWotdListTest() {
        openMenuItem(R.string.action_wotd_history);
        ViewInteraction latestEntryDateView = onView(allOf(withId(R.id.date), withParent(childAtPosition(childAtPosition(withId(R.id.recycler_view), 0), 1))));
        // Check that the date field in the first (most recent) entry in the Wotd list contains today's date.
        Calendar cal = Calendar.getInstance();
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        latestEntryDateView.check(matches(withText(containsString(String.valueOf(dayOfMonth)))));
    }

    @Test
    public void randomWordTest() {
        openMenuItem(R.string.action_random_word);
        checkTitleStripCenterTitle(mActivityTestRule.getActivity(), R.string.tab_dictionary);
        onView(allOf(withId(R.id.tv_list_header), isDisplayed())).check(matches(withText(not(isEmptyOrNullString()))));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void wotdNotificationTest() {
        openMenuItem(R.string.action_settings);
        clickPreference(R.string.wotd_setting_title);
        NotificationManager notificationManager = (NotificationManager) getInstrumentation().getTargetContext().getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
        boolean foundNotification = false;
        for (StatusBarNotification statusBarNotification : activeNotifications) {
            statusBarNotification.getNotification();
            CharSequence title = statusBarNotification.getNotification().extras.getCharSequence("android.title");
            if (title != null && title.toString().startsWith(mActivityTestRule.getActivity().getString(R.string.wotd_setting_title))) {
                foundNotification = true;
                break;
            }
        }
        assertTrue("Didn't find a Wotd notification", foundNotification);

    }


}
