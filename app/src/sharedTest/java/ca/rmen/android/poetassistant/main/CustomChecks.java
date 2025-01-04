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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingRootException;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;

import android.view.View;
import android.view.WindowManager;

import org.fest.reflect.core.Reflection;
import org.hamcrest.Matcher;

import java.util.List;

import ca.rmen.android.poetassistant.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.CustomViewMatchers.childAtPosition;
import static ca.rmen.android.poetassistant.main.CustomViewMatchers.withChildCount;
import static ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class CustomChecks {
    private CustomChecks() {
        // prevent instantiation
    }

    public static void checkRhymes(Context context, String firstRhyme, String secondRhyme) {
        // Make sure we're in the rhymer tab
        checkTitleStripOrTab(context, R.string.tab_rhymer);

        ViewInteraction firstRhymeWord = onView(
                allOf(withId(R.id.text1), withText(firstRhyme),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.rhymer_recycler_view),
                                        1),
                                1),
                        isDisplayed()));
        firstRhymeWord.check(matches(withText(firstRhyme)));

        ViewInteraction secondRhymeWord = onView(
                allOf(withId(R.id.text1), withText(secondRhyme),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.rhymer_recycler_view),
                                        2),
                                1),
                        isDisplayed()));
        secondRhymeWord.check(matches(withText(secondRhyme)));
    }

    public static void checkRhyme(String expectedRhyme) {
        // Scroll to the item in case it's not visible
        onView(allOf(withId(R.id.rhymer_recycler_view), isDisplayed()))
                .perform(scrollTo(hasDescendant(withText(expectedRhyme))));
    }

    public static void checkPatterns(Context context, String query, String... patterns) {
        checkTitleStripOrTab(context, R.string.tab_pattern);
        Matcher<View> emptyViewMatch = allOf(withId(R.id.empty), withText(context.getString(R.string.empty_pattern_list_with_query, query)));
        ViewInteraction emptyView = onView(emptyViewMatch);
        Matcher<View> recyclerViewMatch = allOf(withId(R.id.pattern_recycler_view), hasSibling(emptyViewMatch));
        if (patterns.length > 0) {
            emptyView.check(matches(not(isDisplayed())));
            onView(recyclerViewMatch).check(matches(withChildCount(patterns.length)));
            for (int i = 0; i < patterns.length; i++) {
                onView(allOf(withId(R.id.text1), withText(patterns[i]),
                        childAtPosition(childAtPosition(recyclerViewMatch, i), 1),
                        isDisplayed()))
                        .check(matches(withText(patterns[i])));
            }
        } else {
            emptyView.check(matches(isDisplayed()));
        }
    }

    public static void checkStarredInList(String entry) {
        ViewInteraction star = onView(
                allOf(withId(R.id.btn_star_result),
                        childAtPosition(
                                withChild(withText(entry)),
                                0),
                        isDisplayed()));
        star.check(matches(isChecked()));
    }

    public static void checkAllStarredWords(Context context, String... expectedStarredWords) {
        checkTitleStripOrTab(context, R.string.tab_favorites);
        Matcher<View> emptyViewMatch = allOf(withId(R.id.empty), withText(R.string.empty_favorites_list));
        ViewInteraction emptyView = onView(emptyViewMatch);
        if (expectedStarredWords == null || expectedStarredWords.length == 0) {
            emptyView.check(matches(isCompletelyDisplayed()));
        } else {
            emptyView.check(matches(not(isDisplayed())));
            Matcher<View> recyclerViewMatch = allOf(withId(R.id.favorites_recycler_view), hasSibling(emptyViewMatch));
            onView(recyclerViewMatch).check(matches(withChildCount(expectedStarredWords.length)));
            for (String word : expectedStarredWords) {
                onView(allOf(withId(R.id.text1), withParent(withParent(recyclerViewMatch)), withText(word))).check(matches(isDisplayed()));
            }
        }
    }

    public static void checkSingleRootView(Context context) {
        SystemClock.sleep(500);
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Object impl = Reflection.field("mWindowManager").ofType(Object.class).in(windowManager).get();
            View[] views = Reflection.field("mViews").ofType(View[].class).in(impl).get();
            assertEquals(1, views.length);
        } else {
            Object impl = Reflection.field("mGlobal").ofType(Object.class).in(windowManager).get();
            List views = Reflection.field("mViews").ofType(List.class).in(impl).get();
            assertEquals(1, views.size());
        }
    }

    public static void checkSearchSuggestions(String... suggestions) {
        SystemClock.sleep(1000);
        Espresso.onIdle();
        Matcher<View> searchListMatcher = withId(R.id.search_suggestions_list);
        try {
            ViewInteraction searchSuggestionsList = onView(searchListMatcher);
            searchSuggestionsList.check(matches(withChildCount(suggestions.length)));
            for (int i = 0; i < suggestions.length; i++) {
                onView(allOf(withId(android.R.id.text1), withParent(childAtPosition(searchListMatcher, i))))
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
            fail("Found search suggestions but didn't expect to");
        }
    }

    public static void checkClipboard(Context context, String clipboardContent) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        assertNotNull(clipboard);
        assertTrue("Expected to find " + clipboardContent + " in the clipboard", clipboard.hasPrimaryClip());
        ClipData primaryClip = clipboard.getPrimaryClip();
        assertNotNull(primaryClip);
        ClipData.Item item = primaryClip.getItemAt(primaryClip.getItemCount() - 1);
        assertNotNull(item);
        assertEquals(clipboardContent, item.getText());
    }

    public static void checkFirstDefinition(String expectedFirstDefinition) {
        ViewInteraction firstDefinition = onView(
                allOf(withId(R.id.definition), withText(expectedFirstDefinition),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.dictionary_recycler_view),
                                        0),
                                1),
                        isDisplayed()));
        firstDefinition.check(matches(withText(expectedFirstDefinition)));
    }

    public static void checkFirstSynonym(String expectedFirstSynonym) {
        ViewInteraction firstSynonymWord = onView(
                allOf(withId(R.id.text1), withText(expectedFirstSynonym),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.thesaurus_recycler_view),
                                        2),
                                1),
                        isDisplayed()));
        firstSynonymWord.check(matches(withText(expectedFirstSynonym)));
    }

    public static void checkSynonym(String expectedSynonym) {
        // Scroll to the item in case it's not visible
        onView(allOf(withId(R.id.thesaurus_recycler_view), isDisplayed()))
                .perform(RecyclerViewActions.scrollTo(withChild(withText(expectedSynonym))));
    }
}
