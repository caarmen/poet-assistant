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
import android.support.test.espresso.NoMatchingRootException;
import android.support.test.espresso.ViewInteraction;
import android.view.View;

import org.hamcrest.Matcher;

import ca.rmen.android.poetassistant.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.CustomViewMatchers.childAtPosition;
import static ca.rmen.android.poetassistant.main.CustomViewMatchers.withChildCount;
import static ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripCenterTitle;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;

class CustomChecks {
    private CustomChecks() {
        // prevent instantiation
    }

    static void checkRhymes(Context context, String firstRhyme, String secondRhyme) {
        // Make sure we're in the rhymer tab
        checkTitleStripCenterTitle(context, R.string.tab_rhymer);

        ViewInteraction firstRhymeWord = onView(
                allOf(withId(R.id.text1), withText(firstRhyme),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.recycler_view),
                                        1),
                                1),
                        isDisplayed()));
        firstRhymeWord.check(matches(withText(firstRhyme)));

        ViewInteraction secondRhymeWord = onView(
                allOf(withId(R.id.text1), withText(secondRhyme),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.recycler_view),
                                        2),
                                1),
                        isDisplayed()));
        secondRhymeWord.check(matches(withText(secondRhyme)));
    }

    static void checkPatterns(Context context, String query, String... patterns) {
        checkTitleStripCenterTitle(context, R.string.tab_pattern);
        Matcher<View> emptyViewMatch = allOf(withId(R.id.empty), withText(context.getString(R.string.empty_pattern_list_with_query, query)));
        ViewInteraction emptyView = onView(emptyViewMatch);
        if (patterns.length > 0) {
            emptyView.check(matches(not(isDisplayed())));
        } else {
            emptyView.check(matches(isDisplayed()));
            Matcher<View> recyclerViewMatch = allOf(withId(R.id.recycler_view), hasSibling(emptyViewMatch));
            onView(recyclerViewMatch).check(matches(withChildCount(patterns.length)));
            for (int i = 0; i < patterns.length; i++) {
                onView(allOf(withId(R.id.text1), withText(patterns[i]),
                        childAtPosition(childAtPosition(recyclerViewMatch, i), 1),
                        isDisplayed()))
                        .check(matches(withText(patterns[i])));
            }
        }
    }

    static void checkStarredInList(String entry) {
        ViewInteraction star = onView(
                allOf(withId(R.id.btn_star_result),
                        childAtPosition(
                                withChild(withText(entry)),
                                0),
                        isDisplayed()));
        star.check(matches(isChecked()));
    }

    static void checkAllStarredWords(Context context, String... expectedStarredWords) {
        checkTitleStripCenterTitle(context, R.string.tab_favorites);
        Matcher<View> emptyViewMatch = allOf(withId(R.id.empty), withText(R.string.empty_favorites_list));
        ViewInteraction emptyView = onView(emptyViewMatch);
        if (expectedStarredWords == null || expectedStarredWords.length == 0) {
            emptyView.check(matches(isCompletelyDisplayed()));
        } else {
            emptyView.check(matches(not(isDisplayed())));
            Matcher<View> recyclerViewMatch = allOf(withId(R.id.recycler_view), hasSibling(emptyViewMatch));
            onView(recyclerViewMatch).check(matches(withChildCount(expectedStarredWords.length)));
            for (String word : expectedStarredWords) {
                onView(allOf(withId(R.id.text1), withParent(withParent(recyclerViewMatch)), withText(word))).check(matches(isCompletelyDisplayed()));
            }
        }
    }

    static void checkSearchSuggestions(String... suggestions) {
        SystemClock.sleep(500);
        Matcher<View> searchListMatcher = withClassName(endsWith("DropDownListView"));
        try {
            ViewInteraction searchSuggestionsList = onView(searchListMatcher)
                    .inRoot(isPlatformPopup());
            searchSuggestionsList.check(matches(withChildCount(suggestions.length)));
            for (int i = 0; i < suggestions.length; i++) {
                onView(allOf(withId(android.R.id.text1), withParent(childAtPosition(searchListMatcher, i))))
                        .inRoot(isPlatformPopup())
                        .check(matches(withText(suggestions[i])));
            }
        } catch (NoMatchingRootException e) {
            if (suggestions.length == 0) {
                // this is correct
                return;
            } else {
                throw e;
            }
        }
        if (suggestions.length == 0) {
            assertTrue("Found search suggestions but didn't expect to", false);
        }
    }

}
