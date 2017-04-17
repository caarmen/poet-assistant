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


import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.UserDb;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.endsWith;

/**
 * Tested on:
 * - emulator Nexus_5_API_25
 * - emulator 280x380
 * - Huawei P9 Lite
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    private void cleanup() {
        SQLiteDatabase db = new UserDb(getInstrumentation().getTargetContext()).getWritableDatabase();
        db.delete("SUGGESTION", null, null);
        db.delete("FAVORITE", null, null);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getTargetContext());
        prefs.edit().clear().apply();
    }

    @Before
    public void setup() {
        cleanup();
    }

    @After
    public void tearDown() {
        cleanup();
    }

    @Test
    public void mainActivityTest1() {
        clearSearchHistory();
        search("howdy");
        checkRhymes("cloudy", "dowdy");
        openThesaurus("cloudy", "nebulose");
        openDictionary("nebulous", "lacking definite form or limits");
        starQueryWord();
        swipeViewPagerRight();
        verifyStarredInList("nebulous");
        filter("bloody", "muddy", "nebulose");
        swipeViewPagerRight();
        filter("bully", "rowdy", "cloudy");
        swipeViewPagerLeft();
        swipeViewPagerLeft();
        swipeViewPagerLeft();
        typePoem("To be or not to be, that is the question");
        clearPoem();
    }

    @Test
    public void mainActivityTest2() {
        clearSearchHistory();
        search("beholden");
        checkRhymes("embolden", "golden");
        openThesaurus("embolden", "hearten");
        openDictionary("recreate", "create anew");
        starQueryWord();
        swipeViewPagerRight();
        verifyStarredInList("recreate");
        filter("beer", "cheer", "hearten");
        swipeViewPagerRight();
        filter("wildness", "abandon", "embolden");
        swipeViewPagerLeft();
        swipeViewPagerLeft();
        swipeViewPagerLeft();
        typePoem("roses are red, violets are blue\nespresso tests will find bugs for you");
        clearPoem();
    }

    private void clearSearchHistory() {

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        SystemClock.sleep(1000);

        openMenu();

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.title), withText(R.string.action_settings), isDisplayed()));
        appCompatTextView.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.list),
                        withParent(allOf(withId(android.R.id.list_container),
                                withParent(withId(R.id.settings_fragment)))),
                        isDisplayed()));
        recyclerView.perform(swipeUp(), swipeUp(), swipeUp(), swipeUp());

        onView(withText(R.string.action_clear_search_history)).perform(click());

        ViewInteraction appCompatButton = onView(
                allOf(withId(android.R.id.button1), withText(R.string.action_clear)));
        appCompatButton.perform(scrollTo(), click());
        pressBack();
    }

    private void search(String query) {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        SystemClock.sleep(1000);

        ViewInteraction searchIcon = onView(
                allOf(withId(R.id.action_search), withContentDescription(R.string.action_search), isDisplayed()));
        searchIcon.perform(click());

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

    private void checkRhymes(String firstRhyme, String secondRhyme) {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        SystemClock.sleep(100);

        ViewInteraction titleStrip = onView(
                allOf(withText("RHYMER"),
                        childAtPosition(
                                allOf(withId(R.id.pager_title_strip),
                                        withParent(withId(R.id.view_pager))),
                                1),
                        isDisplayed()));
        titleStrip.check(matches(withText("RHYMER")));

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

    private void openThesaurus(String entry, String expectedFirstSynonym) {
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

        ViewInteraction titleStrip = onView(
                allOf(withText("THESAURUS"),
                        childAtPosition(
                                allOf(withId(R.id.pager_title_strip),
                                        withParent(withId(R.id.view_pager))),
                                1),
                        isDisplayed()));
        titleStrip.check(matches(withText("THESAURUS")));

    }

    private void openDictionary(String entry, String expectedFirstDefinition) {
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

        ViewInteraction titleStrip = onView(
                allOf(withText("DICTIONARY"),
                        childAtPosition(
                                allOf(withId(R.id.pager_title_strip),
                                        withParent(withId(R.id.view_pager))),
                                1),
                        isDisplayed()));
        titleStrip.check(matches(withText("DICTIONARY")));

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

    private void starQueryWord() {
        ViewInteraction starIcon = onView(
                allOf(withId(R.id.btn_star_query), isDisplayed()));
        starIcon.check(matches(isNotChecked()));
        starIcon.perform(click());
        starIcon.check(matches(isChecked()));
    }

    private void filter(String filter, String firstExpectedFilteredMatch, String firstExpectedNonFilteredMatch) {
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

    private void verifyStarredInList(String entry) {
        ViewInteraction star = onView(
                allOf(withId(R.id.btn_star_result),
                        childAtPosition(
                                withChild(withText(entry)),
                                0),
                        isDisplayed()));
        star.check(matches(isChecked()));

    }

    private void typePoem(String poem) {
        SystemClock.sleep(500);
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.tv_text), isDisplayed()));
        appCompatEditText.perform(typeText(poem));
        pressBack();
        SystemClock.sleep(100);
    }

    private void clearPoem() {
        openMenu();

        ViewInteraction menuItem = onView(
                allOf(withId(R.id.title), withText(R.string.file), isDisplayed()));
        menuItem.perform(click());
        menuItem = onView(
                allOf(withId(R.id.title), withText(R.string.file_new), isDisplayed()));
        menuItem.perform(click());
        ViewInteraction appCompatButton = onView(
                allOf(withId(android.R.id.button1), withText(R.string.action_clear)));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.tv_text), isDisplayed()));
        appCompatEditText.check(matches(withText("")));
    }

    private void openMenu() {
        onView(allOf(isDisplayed(), anyOf(withClassName(endsWith("OverflowMenuButton")), withClassName(endsWith("MenuDropDownListView"))))).perform(click());
    }

    private void swipeViewPagerRight() {
        onView(allOf(withId(android.R.id.content), isDisplayed())).perform(swipeRight());
    }

    private void swipeViewPagerLeft() {
        onView(allOf(withId(android.R.id.content), isDisplayed())).perform(swipeLeft());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
