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
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static ca.rmen.android.poetassistant.main.TestAppUtils.search;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;

import android.content.Context;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.MainActivity;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantIntentsTestRule;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.HiltTestApplication;

@LargeTest
@HiltAndroidTest
@Config(application = HiltTestApplication.class)
@RunWith(AndroidJUnit4.class)
public class ShareTest {

    @Rule(order = 0)
    public HiltAndroidRule hiltTestRule = new HiltAndroidRule(this);
    @Rule(order = 1)
    public PoetAssistantIntentsTestRule<MainActivity> mActivityTestRule = new PoetAssistantIntentsTestRule<>(MainActivity.class);


    @Test
    public void shareRhymesTest() {
        search("merge");
        openMenuItem(R.string.share);
        checkShareIntentContains("upsurge");
    }


    @Test
    public void shareDictionaryTest() {
        search("a");
        swipeViewPagerLeft(2);
        openMenuItem(R.string.share);
        checkShareIntentContains("the blood group whose red cells carry the A antigen");
    }

    @Test
    public void sharePatternTest() {
        search("ho?t");
        openMenuItem(R.string.share);
        checkShareIntentContains("host");
    }

    @Test
    public void shareWotdTest() {
        Context context = mActivityTestRule.getActivity();
        openMenuItem(R.string.action_wotd_history);
        openMenuItem(R.string.share);
        checkShareIntentContains(context.getString(R.string.share_wotd_title));
    }

    @Test
    public void sharePopupTest() {
        search("strawberry");
        Context context = mActivityTestRule.getActivity();
        onView(allOf(withText("adversary"), isDisplayed())).perform(click());
        onView(allOf(withText(endsWith(context.getString(R.string.share))), isDisplayed())).perform(click());
        checkShareIntentEquals("adversary");
    }

    private void checkShareIntentContains(String expectedText) {
        intended(allOf(hasAction(Intent.ACTION_CHOOSER),
                hasExtra(is(Intent.EXTRA_INTENT),
                        allOf(hasAction(Intent.ACTION_SEND),
                                hasExtra(containsString(Intent.EXTRA_TEXT),
                                        containsString(expectedText))))));
    }

    private void checkShareIntentEquals(String expectedText) {
        intended(allOf(hasAction(Intent.ACTION_CHOOSER),
                hasExtra(is(Intent.EXTRA_INTENT),
                        allOf(hasAction(Intent.ACTION_SEND),
                                hasExtra(Intent.EXTRA_TEXT, expectedText)))));
    }
}
