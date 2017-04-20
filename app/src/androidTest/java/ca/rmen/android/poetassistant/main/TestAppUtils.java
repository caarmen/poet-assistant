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
import android.support.design.widget.FloatingActionButton;
import android.support.test.espresso.ViewInteraction;
import android.view.View;

import org.hamcrest.Matcher;

import ca.rmen.android.poetassistant.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.CustomViewMatchers.childAtPosition;
import static ca.rmen.android.poetassistant.main.CustomViewMatchers.withChildCount;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.verifyTitleStripCenterTitle;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Utility functions specific to the functionality of this app.
 */
class TestAppUtils {
    private TestAppUtils() {
        // prevent instantiation
    }

    static void clearSearchHistory() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        SystemClock.sleep(1000);


        // click on the settings menu item
        openMenuItem(R.string.action_settings);

        // Scroll down to the bottom of the settings
        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.list),
                        withParent(allOf(withId(android.R.id.list_container),
                                withParent(withId(R.id.settings_fragment)))),
                        isDisplayed()));
        recyclerView.perform(swipeUp(), swipeUp(), swipeUp(), swipeUp());

        // Tap on "clear search history"
        onView(withText(R.string.action_clear_search_history)).perform(click());

        // Tap ok on the confirmation dialog
        onView(allOf(withId(android.R.id.button1), withText(R.string.action_clear))).perform(scrollTo(), click());

        // Exit settings
        pressBack();
    }

    static void search(String query) {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        SystemClock.sleep(1000);

        // Tap on the search icon in the action bar
        onView(allOf(withId(R.id.action_search), withContentDescription(R.string.action_search), isDisplayed())).perform(click());

        // Type the query term and search
        ViewInteraction searchAutoComplete = onView(
                allOf(
                        withId(R.id.search_src_text),
                        withParent(
                                allOf(
                                        withId(R.id.search_plate),
                                        withParent(
                                                withId(R.id.search_edit_frame))
                                )
                        ),
                        isDisplayed()));
        searchAutoComplete.perform(typeText(query), pressImeActionButton());
    }

    static void checkRhymes(Context context, String firstRhyme, String secondRhyme) {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        SystemClock.sleep(100);

        // Make sure we're in the rhymer tab
        verifyTitleStripCenterTitle(context, R.string.tab_rhymer);

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
        verifyTitleStripCenterTitle(context, R.string.tab_pattern);
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

    static void openThesaurus(Context context, String entry, String expectedFirstSynonym) {
        ViewInteraction thesaurusIcon = onView(
                allOf(
                        withId(R.id.btn_thesaurus),
                        withContentDescription(R.string.tab_thesaurus),
                        isDisplayed(),
                        childAtPosition(
                                withChild(withText(entry)),
                                3)));
        thesaurusIcon.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        SystemClock.sleep(100);

        ViewInteraction firstSynonymWord = onView(
                allOf(withId(R.id.text1), withText(expectedFirstSynonym),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.recycler_view),
                                        2),
                                1),
                        isDisplayed()));
        firstSynonymWord.check(matches(withText(expectedFirstSynonym)));

        verifyTitleStripCenterTitle(context, R.string.tab_thesaurus);
    }

    static void openDictionary(Context context, String entry, String expectedFirstDefinition) {
        ViewInteraction dictionaryIcon = onView(
                allOf(
                        withId(R.id.btn_dictionary),
                        withContentDescription(R.string.tab_dictionary),
                        isDisplayed(),
                        childAtPosition(
                                withChild(withText(entry)),
                                4)));

        dictionaryIcon.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        SystemClock.sleep(100);

        verifyTitleStripCenterTitle(context, R.string.tab_dictionary);

        ViewInteraction firstDefinition = onView(
                allOf(withId(R.id.definition), withText(expectedFirstDefinition),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.recycler_view),
                                        0),
                                1),
                        isDisplayed()));
        firstDefinition.check(matches(withText(expectedFirstDefinition)));
    }

    static void starQueryWord() {
        ViewInteraction starIcon = onView(
                allOf(withId(R.id.btn_star_query), isDisplayed()));
        starIcon.check(matches(isNotChecked()));
        starIcon.perform(click());
        starIcon.check(matches(isChecked()));
    }

    static void filter(String filter, String firstExpectedFilteredMatch, String firstExpectedNonFilteredMatch) {
        ViewInteraction filterIcon = onView(
                allOf(withId(R.id.btn_filter), withContentDescription(R.string.filter_title), isDisplayed()));
        filterIcon.perform(click());
        SystemClock.sleep(500);
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.edit), isDisplayed()));
        appCompatEditText.perform(typeText(filter), closeSoftKeyboard());
        ViewInteraction appCompatButton = onView(
                allOf(withId(android.R.id.button1), withText(android.R.string.ok)));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction firstActualMatch = onView(
                allOf(withId(R.id.text1),
                        withText(firstExpectedFilteredMatch),
                        withParent(withParent(withId(R.id.recycler_view))),
                        isDisplayed()));
        firstActualMatch.check(matches(withText(firstExpectedFilteredMatch)));

        ViewInteraction clearFilterIcon = onView(
                allOf(withId(R.id.btn_clear), withContentDescription(R.string.filter_clear), isDisplayed()));
        clearFilterIcon.perform(click());

        firstActualMatch = onView(
                allOf(withId(R.id.text1),
                        withText(firstExpectedNonFilteredMatch),
                        withParent(withParent(withId(R.id.recycler_view))),
                        isDisplayed()));
        firstActualMatch.check(matches(withText(firstExpectedNonFilteredMatch)));

    }

    static void verifyStarredInList(String entry) {
        ViewInteraction star = onView(
                allOf(withId(R.id.btn_star_result),
                        childAtPosition(
                                withChild(withText(entry)),
                                0),
                        isDisplayed()));
        star.check(matches(isChecked()));
    }

    static void verifyAllStarredWords(Context context, String... expectedStarredWords) {
        verifyTitleStripCenterTitle(context, R.string.tab_favorites);
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

    static void clearStarredWords() {
        onView(allOf(withId(R.id.btn_delete), withContentDescription(R.string.action_clear_favorites), isDisplayed())).perform(click());
        // Top ok on the confirmation dialog
        onView(allOf(withId(android.R.id.button1), withText(R.string.action_clear))).perform(scrollTo(), click());
    }

    static void typePoem(String poem) {
        SystemClock.sleep(500);
        // The fab should be disabled until there is text
        ViewInteraction fab = onView(withClassName(is(FloatingActionButton.class.getName())));
        fab.check(matches(not(isEnabled())));
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.tv_text), isDisplayed()));
        appCompatEditText.check(matches(withText("")));
        appCompatEditText.perform(typeText(poem));
        appCompatEditText.check(matches(withText(equalToIgnoringCase(poem))));
        fab.check(matches(isEnabled()));
        speakPoem();
        pressBack();
        SystemClock.sleep(100);
    }

    private static void speakPoem() {
        ViewInteraction fab = onView(allOf(withClassName(is(FloatingActionButton.class.getName())), isEnabled()));
        fab.perform(click());
    }

    static void clearPoem() {
        openMenuItem(R.string.file);
        onView(allOf(withId(R.id.title), withText(R.string.file_new), isDisplayed())).perform(click());
        onView(allOf(withId(android.R.id.button1), withText(R.string.action_clear))).perform(scrollTo(), click());
        onView(allOf(withId(R.id.tv_text), isDisplayed())).check(matches(withText("")));
    }


}
