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
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkClipboard;
import static ca.rmen.android.poetassistant.main.TestAppUtils.addFilter;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clickDialogPositiveButton;
import static ca.rmen.android.poetassistant.main.TestAppUtils.openFilter;
import static ca.rmen.android.poetassistant.main.TestAppUtils.search;

import android.content.Context;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.MainActivity;
import ca.rmen.android.poetassistant.main.Tab;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.HiltTestApplication;

@LargeTest
@HiltAndroidTest
@Config(application = HiltTestApplication.class)
@RunWith(AndroidJUnit4.class)
public class IntegrationTest {

    @Rule(order = 0)
    public HiltAndroidRule hiltTestRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public PoetAssistantActivityTestRule<MainActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(MainActivity.class, true);


    @Test
    public void copyTest() {
        Context context = mActivityTestRule.getActivity();
        search("donkey");
        String wordToCopy = "swanky";
        onView(allOf(withText(wordToCopy), isDisplayed())).perform(click());
        onView(allOf(withText(endsWith(context.getString(R.string.menu_copy))), isDisplayed())).perform(click());
        getInstrumentation().runOnMainSync(() -> checkClipboard(context, wordToCopy));
    }

    @Test
    public void saveFilterTest() {
        search("pugnacious");
        addFilter(Tab.RHYMER, "vulturous", "rapacious");
        ViewInteraction filterView = openFilter("vulturous");
        filterView.perform(closeSoftKeyboard());
        clickDialogPositiveButton(android.R.string.ok);
    }
}
