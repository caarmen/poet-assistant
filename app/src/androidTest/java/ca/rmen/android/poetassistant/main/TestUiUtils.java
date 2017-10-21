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
import android.support.annotation.StringRes;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.rules.SearchResultReadyIdlingResource;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isSelected;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.CustomViewMatchers.childAtPosition;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalToIgnoringCase;

/**
 * Generic utility functions for Ui operations like opening a menu or swiping the screen.
 */
class TestUiUtils {

    private TestUiUtils() {
        // prevent instantiation
    }

    static void openMenuItem(@StringRes int titleRes) {
        onView(allOf(isDisplayed(), withClassName(endsWith("OverflowMenuButton")))).perform(click());
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

    static void waitForSearchResultList() {
        IdlingResource waitForSearchResultResource = new SearchResultReadyIdlingResource();
        IdlingRegistry.getInstance().register(waitForSearchResultResource);
        getInstrumentation().waitForIdleSync();
        IdlingRegistry.getInstance().unregister(waitForSearchResultResource);
    }

    private static void checkTitleStripCenterTitle(Context context, @StringRes int titleRes) {
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
        onView(withId(R.id.list))
                .perform(scrollTo(hasDescendant(withText(prefTitleRes))));
    }

    static void clickPreference(@StringRes int prefTitleRes) {
        scrollToPreference(prefTitleRes);
        // click on the preference
        onView(withText(prefTitleRes)).perform(click());
    }

}
