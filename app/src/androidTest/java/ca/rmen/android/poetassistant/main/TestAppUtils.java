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
import android.support.design.widget.FloatingActionButton;
import android.support.test.espresso.ViewInteraction;

import ca.rmen.android.poetassistant.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.CustomViewMatchers.childAtPosition;
import static ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab;
import static ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
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
        // click on the settings menu item
        openMenuItem(R.string.action_settings);

        clickPreference(R.string.action_clear_search_history);

        // Tap ok on the confirmation dialog
        clickDialogPositiveButton(R.string.action_clear);

        // Exit settings
        pressBack();
    }

    static void openSearchView() {
        // Tap on the search icon in the action bar
        onView(allOf(withId(R.id.action_search), withContentDescription(R.string.action_search), isDisplayed())).perform(click());
    }

    static ViewInteraction typeQuery(String query) {
        // Type the query term and search
        ViewInteraction searchAutoComplete = onView(allOf(withId(R.id.search_src_text), isDisplayed()));
        searchAutoComplete.check(matches(isDisplayed()));
        searchAutoComplete.perform(typeText(query));
        return searchAutoComplete;
    }

    static void search(String query) {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        SystemClock.sleep(1000);
        openSearchView();

        // Type the query term and search
        typeQuery(query).perform(pressImeActionButton());
    }

    static void openThesaurus(Context context, String entry, String expectedFirstSynonym) {
        onView(allOf(withId(R.id.btn_thesaurus),
                hasSibling(withText(entry)),
                isDisplayed()))
                .perform(click());
        verifyFirstSynonym(expectedFirstSynonym);
        checkTitleStripOrTab(context, R.string.tab_thesaurus);
    }

    static void openThesaurusCleanLayout(Context context, String entry, String expectedFirstSynonym) {
        onView(withText(entry)).perform(click());
        onView(withText(R.string.tab_thesaurus)).perform(click());
        verifyFirstSynonym(expectedFirstSynonym);
        checkTitleStripOrTab(context, R.string.tab_thesaurus);
    }

    static void verifyFirstSynonym(String expectedFirstSynonym) {
        ViewInteraction firstSynonymWord = onView(
                allOf(withId(R.id.text1), withText(expectedFirstSynonym),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.recycler_view),
                                        2),
                                1),
                        isDisplayed()));
        firstSynonymWord.check(matches(withText(expectedFirstSynonym)));
    }

    static void openDictionary(Context context, String entry, String expectedFirstDefinition) {
        onView(allOf(withId(R.id.btn_dictionary),
                hasSibling(withText(entry)),
                isDisplayed()))
                .perform(click());
        checkTitleStripOrTab(context, R.string.tab_dictionary);
        verifyFirstDefinition(expectedFirstDefinition);
    }

    static void openDictionaryCleanLayout(Context context, String entry, String expectedFirstDefinition) {
        onView(withText(entry)).perform(click());
        onView(withText(R.string.tab_dictionary)).perform(click());
        checkTitleStripOrTab(context, R.string.tab_dictionary);
        verifyFirstDefinition(expectedFirstDefinition);
    }

    static void verifyFirstDefinition(String expectedFirstDefinition) {
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

    static void unStarQueryWord() {
        ViewInteraction starIcon = onView(
                allOf(withId(R.id.btn_star_query), isDisplayed()));
        starIcon.check(matches(isChecked()));
        starIcon.perform(click());
        onView(allOf(withId(R.id.btn_star_query), isDisplayed()))
                .check(matches(isNotChecked()));
    }

    static void addFilter(String filter, String firstExpectedFilteredMatch) {
        onView(allOf(withId(R.id.btn_filter), withContentDescription(R.string.filter_title), isDisplayed()))
                .perform(click());
        onView(allOf(withId(R.id.edit), isDisplayed()))
                .perform(typeText(filter), closeSoftKeyboard());
        clickDialogPositiveButton(android.R.string.ok);

        onView(allOf(withId(R.id.text1),
                withText(firstExpectedFilteredMatch),
                withParent(withParent(withId(R.id.recycler_view))),
                isDisplayed()))
                .check(matches(withText(firstExpectedFilteredMatch)));

    }

    static void clearFilter(String firstExpectedNonFilteredMatch) {
        onView(allOf(withId(R.id.btn_clear), withContentDescription(R.string.filter_clear), isDisplayed()))
                .perform(click());

        onView(allOf(withId(R.id.text1),
                withText(firstExpectedNonFilteredMatch),
                withParent(withParent(withId(R.id.recycler_view))),
                isDisplayed()))
                .check(matches(withText(firstExpectedNonFilteredMatch)));
    }

    static void clearStarredWords() {
        onView(allOf(withId(R.id.btn_delete), withContentDescription(R.string.action_clear_favorites), isDisplayed())).perform(click());
        clickDialogPositiveButton(R.string.action_clear);
    }

    private static void clickDialogPositiveButton(@StringRes int labelRes) {
        // Top ok on the confirmation dialog
        SystemClock.sleep(200);
        onView(allOf(withId(android.R.id.button1), withText(labelRes))).perform(scrollTo(), click());
    }

    static void typePoem(String poem) {
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
    }

    private static void speakPoem() {
        ViewInteraction fab = onView(allOf(withClassName(is(FloatingActionButton.class.getName())), isEnabled()));
        fab.perform(click());
    }

    static void clearPoem() {
        openMenuItem(R.string.file);
        onView(allOf(withId(R.id.title), withText(R.string.file_new), isDisplayed())).perform(click());
        clickDialogPositiveButton(R.string.action_clear);
        onView(allOf(withId(R.id.tv_text), isDisplayed())).check(matches(withText("")));
    }
}
