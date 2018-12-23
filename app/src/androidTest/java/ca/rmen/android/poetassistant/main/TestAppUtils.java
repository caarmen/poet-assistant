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
import android.os.Build;
import android.os.SystemClock;
import androidx.annotation.StringRes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.test.espresso.ViewInteraction;
import android.text.TextUtils;

import ca.rmen.android.poetassistant.R;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isFocusable;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
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
        getInstrumentation().waitForIdleSync();
        ViewInteraction searchAutoComplete = onView(allOf(withId(R.id.search_src_text), isDisplayed()));
        searchAutoComplete.check(matches(isDisplayed()));
        searchAutoComplete.perform(typeText(query));
        getInstrumentation().waitForIdleSync();
        return searchAutoComplete;
    }

    static void search(String query) {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        SystemClock.sleep(1000);
        openSearchView();

        // Type the query term and search
        ViewInteraction vi = typeQuery(query);
        getInstrumentation().waitForIdleSync();
        vi.perform(pressImeActionButton());
        getInstrumentation().waitForIdleSync();
    }

    static void openThesaurus(Context context, String entry, String expectedFirstSynonym) {
        onView(allOf(withId(R.id.btn_thesaurus),
                hasSibling(withText(entry)),
                isDisplayed()))
                .perform(click());
        CustomChecks.checkFirstSynonym(expectedFirstSynonym);
        checkTitleStripOrTab(context, R.string.tab_thesaurus);
    }

    static void openThesaurusCleanLayout(Context context, String entry, String expectedFirstSynonym) {
        onView(withText(entry)).perform(click());
        onView(withText(R.string.tab_thesaurus)).perform(click());
        CustomChecks.checkFirstSynonym(expectedFirstSynonym);
        checkTitleStripOrTab(context, R.string.tab_thesaurus);
    }

    static void openDictionary(Context context, String entry, String expectedFirstDefinition) {
        onView(allOf(withId(R.id.btn_dictionary),
                hasSibling(withText(entry)),
                isDisplayed()))
                .perform(click());
        checkTitleStripOrTab(context, R.string.tab_dictionary);
        CustomChecks.checkFirstDefinition(expectedFirstDefinition);
    }

    static void openDictionaryCleanLayout(Context context, String entry, String expectedFirstDefinition) {
        onView(withText(entry)).perform(click());
        onView(withText(R.string.tab_dictionary)).perform(click());
        checkTitleStripOrTab(context, R.string.tab_dictionary);
        CustomChecks.checkFirstDefinition(expectedFirstDefinition);
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

    static ViewInteraction openFilter(String expectedPrefilledFilter) {
        getInstrumentation().waitForIdleSync();
        ViewInteraction vi = onView(allOf(withId(R.id.btn_filter), withContentDescription(R.string.filter_title), isDisplayed()));
        vi.check(matches(isDisplayed()));
        vi.perform(click());
        SystemClock.sleep(200);
        ViewInteraction result = onView(allOf(
                withId(R.id.edit),
                isDisplayed()))
                .inRoot(isFocusable());
        result.check(matches(withText(expectedPrefilledFilter)));
        return result;
    }

    static void addFilter(String filter, String firstExpectedFilteredMatch) {
        ViewInteraction filterView = openFilter("");
        filterView.perform(typeText(filter), closeSoftKeyboard());
        clickDialogPositiveButton(android.R.string.ok);

        if (TextUtils.isEmpty(firstExpectedFilteredMatch)) {
            onView(allOf(withId(R.id.empty), hasSibling(withId(R.id.recycler_view)), isDisplayed()))
                    .check(matches(isDisplayed()));
        } else {
            onView(allOf(withId(R.id.empty), hasSibling(allOf(withId(R.id.recycler_view), isDisplayed()))))
                    .check(matches(not(isDisplayed())));
            onView(allOf(withId(R.id.text1),
                    withText(firstExpectedFilteredMatch),
                    withParent(withParent(withId(R.id.recycler_view))),
                    isDisplayed()))
                    .check(matches(withText(firstExpectedFilteredMatch)));
        }

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

    static void clickDialogPositiveButton(@StringRes int labelRes) {
        // Top ok on the confirmation dialog
        SystemClock.sleep(200);
        onView(allOf(withId(android.R.id.button1), withText(labelRes))).perform(scrollTo(), click());
    }

    static void typeAndSpeakPoem(String poem) {
        typePoem(poem);
        speakPoem();
        pressBack();
        getInstrumentation().waitForIdleSync();
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
    }

    static void speakPoem() {
        ViewInteraction fab = onView(allOf(withClassName(is(FloatingActionButton.class.getName())), isEnabled()));
        fab.perform(click());
    }

    static void clearPoem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            openMenuItem(R.string.file);
            onView(allOf(withId(R.id.title), withText(R.string.file_new), isDisplayed())).perform(click());
        } else {
            openMenuItem(R.string.file_clear);
        }
        clickDialogPositiveButton(R.string.action_clear);
        onView(allOf(withId(R.id.tv_text), isDisplayed())).check(matches(withText("")));
    }
}
