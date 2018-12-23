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

import android.content.Context;
import android.os.SystemClock;
import androidx.annotation.StringRes;
import androidx.test.espresso.NoMatchingViewException;
import android.util.Log;
import android.view.KeyEvent;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isSelected;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.CustomViewMatchers.childAtPosition;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalToIgnoringCase;

/**
 * Generic utility functions for Ui operations like opening a menu or swiping the screen.
 */
class TestUiUtils {

    private static final String TAG = Constants.TAG + TestUiUtils.class.getSimpleName();

    private TestUiUtils() {
        // prevent instantiation
    }

    static void openMenuItem(@StringRes int titleRes) {
        getInstrumentation().waitForIdleSync();
        swipeDown();
        try {
            onView(allOf(isDisplayed(), withClassName(endsWith("OverflowMenuButton")))).perform(click());
        } catch (NoMatchingViewException e) {
            Log.wtf(TAG, "Couldn't press the menu button in the action bar. Resorting to the menu key event. Is this cheating?", e);
            onView(withId(android.R.id.content)).perform(pressKey(KeyEvent.KEYCODE_MENU));
            getInstrumentation().waitForIdleSync();
            SystemClock.sleep(400); // :(
        }
        onView(allOf(withId(R.id.title), withText(titleRes), isDisplayed())).perform(click());
    }

    static void swipeViewPagerRight(int count) {
        for (int i = 0; i < count; i++) {
            onView(allOf(withId(android.R.id.content), isDisplayed())).perform(swipeRight());
        }
        SystemClock.sleep(200);
    }

    static void swipeViewPagerLeft(int count) {
        for (int i = 0; i < count; i++) {
            onView(allOf(withId(android.R.id.content), isDisplayed())).perform(swipeLeft());
        }
        SystemClock.sleep(200);
    }

    static void checkTitleStripOrTab(Context context, @StringRes int titleRes) {
        if (context.getResources().getBoolean(R.bool.tab_text)) {
            checkSelectedTab(context, titleRes);
        } else {
            checkTitleStripCenterTitle(context, titleRes);
        }
    }

    private static void checkTitleStripCenterTitle(Context context, @StringRes int titleRes) {
        onView(withId(R.id.pager_title_strip)).check(matches(isDisplayed()));
        onView(allOf(withText(equalToIgnoringCase(context.getString(titleRes))),
                childAtPosition(
                        allOf(withId(R.id.pager_title_strip),
                                withParent(withId(R.id.view_pager))),
                        1),
                isCompletelyDisplayed()))
                .check(matches(isDisplayed()));

    }

    private static void checkSelectedTab(Context context, @StringRes int titleRes) {
        onView(allOf(withText(equalToIgnoringCase(context.getString(titleRes))),
                isDescendantOfA(withId(R.id.tabs))))
                .check(matches(isSelected()));
    }

    static void scrollToPreference(@StringRes int prefTitleRes) {
        // Scroll to the preference in case it's not visible
        onView(withId(R.id.recycler_view))
                .perform(scrollTo(hasDescendant(withText(prefTitleRes))));
    }

    static void clickPreference(@StringRes int prefTitleRes) {
        scrollToPreference(prefTitleRes);
        // click on the preference
        onView(withText(prefTitleRes)).perform(click());
    }

}
