/*
 * Copyright (c) 2017 - present Carmen Alvarez
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


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkAllStarredWords;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkClipboard;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkRhymes;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkStarredInList;
import static ca.rmen.android.poetassistant.main.TestAppUtils.addFilter;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clearFilter;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clearPoem;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clearSearchHistory;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clearStarredWords;
import static ca.rmen.android.poetassistant.main.TestAppUtils.openDictionary;
import static ca.rmen.android.poetassistant.main.TestAppUtils.openDictionaryCleanLayout;
import static ca.rmen.android.poetassistant.main.TestAppUtils.openThesaurus;
import static ca.rmen.android.poetassistant.main.TestAppUtils.openThesaurusCleanLayout;
import static ca.rmen.android.poetassistant.main.TestAppUtils.search;
import static ca.rmen.android.poetassistant.main.TestAppUtils.starQueryWord;
import static ca.rmen.android.poetassistant.main.TestAppUtils.typeAndSpeakPoem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerRight;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class IntegrationTest {

    @Rule
    public PoetAssistantActivityTestRule<MainActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(MainActivity.class, true);

    private static class IntegrationTestScenario {
        final String query;
        final String firstRhyme;
        final String secondRhyme;
        final String firstSynonymForFirstRhyme;
        final String secondSynonymForFirstRhyme;
        final String firstDefinitionForSecondSynonym;
        final String thesaurusFilter;
        final String thesaurusFilterMatch;
        final String rhymerFilter;
        final String rhymerFilterMatch;
        final String poem;

        IntegrationTestScenario(String query,
                                String firstRhyme, String secondRhyme, String firstSynonymForFirstRhyme, String secondSynonymForFirstRhyme, String firstDefinitionForSecondSynonym,
                                String thesaurusFilter, String thesaurusFilterMatch, String rhymerFilter, String rhymerFilterMatch,
                                String poem) {
            this.query = query;
            this.firstRhyme = firstRhyme;
            this.secondRhyme = secondRhyme;
            this.firstSynonymForFirstRhyme = firstSynonymForFirstRhyme;
            this.secondSynonymForFirstRhyme = secondSynonymForFirstRhyme;
            this.firstDefinitionForSecondSynonym = firstDefinitionForSecondSynonym;
            this.thesaurusFilter = thesaurusFilter;
            this.thesaurusFilterMatch = thesaurusFilterMatch;
            this.rhymerFilter = rhymerFilter;
            this.rhymerFilterMatch = rhymerFilterMatch;
            this.poem = poem;
        }
    }

    private static final IntegrationTestScenario SCENARIO1 =
            new IntegrationTestScenario("howdy", "cloudy", "dowdy", "nebulose", "nebulous", "lacking definite form or limits",
                    "bloody", "muddy", "bully", "rowdy",
                    "Forever is composed of nows"); // Emily Dickinson

    private static final IntegrationTestScenario SCENARIO2 =
            new IntegrationTestScenario("beholden", "embolden", "golden", "hearten", "recreate", "create anew",
                    "beer", "cheer", "wildness", "abandon",
                    "roses are red, violets are blue\nespresso tests will find bugs for you");

    private void runIntegrationTest(IntegrationTestScenario data) {
        Context context = mActivityTestRule.getActivity();
        swipeViewPagerLeft(4);
        checkAllStarredWords(context);
        swipeViewPagerRight(4);
        search(data.query);
        checkRhymes(context, data.firstRhyme, data.secondRhyme);
        openThesaurus(context, data.firstRhyme, data.firstSynonymForFirstRhyme);
        openDictionary(context, data.secondSynonymForFirstRhyme, data.firstDefinitionForSecondSynonym);
        starQueryWord();
        swipeViewPagerLeft(2);
        checkAllStarredWords(context, data.secondSynonymForFirstRhyme);
        swipeViewPagerRight(3);
        checkStarredInList(data.secondSynonymForFirstRhyme);
        addFilter(Tab.THESAURUS, data.thesaurusFilter, data.thesaurusFilterMatch);
        clearFilter(Tab.THESAURUS, data.firstSynonymForFirstRhyme);
        swipeViewPagerRight(1);
        addFilter(Tab.RHYMER, data.rhymerFilter, data.rhymerFilterMatch);
        clearFilter(Tab.RHYMER, data.firstRhyme);
        swipeViewPagerLeft(3);
        typeAndSpeakPoem(data.poem);
        clearPoem();
        // clearing the search history doesn't erase starred words
        clearSearchHistory();
        swipeViewPagerLeft(1);
        checkAllStarredWords(context, data.secondSynonymForFirstRhyme);
        clearStarredWords();
        checkAllStarredWords(context);
    }

    private void runCleanLayoutIntegrationTest(IntegrationTestScenario data) {
        Context context = mActivityTestRule.getActivity();
        useCleanLayout();
        swipeViewPagerLeft(4);
        checkAllStarredWords(context);
        swipeViewPagerRight(4);
        search(data.query);
        checkRhymes(context, data.firstRhyme, data.secondRhyme);
        openThesaurusCleanLayout(context, data.firstRhyme, data.firstSynonymForFirstRhyme);
        openDictionaryCleanLayout(context, data.secondSynonymForFirstRhyme, data.firstDefinitionForSecondSynonym);
        starQueryWord();
        swipeViewPagerLeft(2);
        checkAllStarredWords(context, data.secondSynonymForFirstRhyme);
        swipeViewPagerRight(3);
        checkStarredInList(data.secondSynonymForFirstRhyme);
        addFilter(Tab.THESAURUS, data.thesaurusFilter, data.thesaurusFilterMatch);
        clearFilter(Tab.THESAURUS, data.firstSynonymForFirstRhyme);
        swipeViewPagerRight(1);
        addFilter(Tab.RHYMER, data.rhymerFilter, data.rhymerFilterMatch);
        clearFilter(Tab.RHYMER, data.firstRhyme);
        swipeViewPagerLeft(3);
        typeAndSpeakPoem(data.poem);
        clearPoem();
        // clearing the search history doesn't erase starred words
        clearSearchHistory();
        swipeViewPagerLeft(1);
        checkAllStarredWords(context, data.secondSynonymForFirstRhyme);
        clearStarredWords();
        checkAllStarredWords(context);
    }

    @Test
    public void integrationTest1() {
        runIntegrationTest(SCENARIO1);
    }

    @Test
    public void integrationTest2() {
        runIntegrationTest(SCENARIO2);
    }

    @Test
    public void cleanLayout1Test() {
        runCleanLayoutIntegrationTest(SCENARIO1);
    }

    @Test
    public void cleanLayout2Test() {
        runCleanLayoutIntegrationTest(SCENARIO2);
    }

    @Test
    public void copyCleanLayoutTest() {
        Context context = mActivityTestRule.getActivity();
        useCleanLayout();
        search("donkey");
        String wordToCopy = "swanky";
        onView(allOf(withText(wordToCopy), isDisplayed())).perform(click());
        onView(allOf(withText(endsWith(context.getString(R.string.menu_more))), isDisplayed())).perform(click());
        onView(allOf(withText(endsWith(context.getString(R.string.menu_copy))), isDisplayed())).perform(click());
        getInstrumentation().runOnMainSync(() -> checkClipboard(context, wordToCopy));
    }

    @Test
    public void themeTest() {
        openMenuItem(R.string.action_settings);
        clickPreference(R.string.pref_theme_title);
        onView(withText(R.string.pref_theme_value_auto)).check(matches(isChecked()));
        onView(withText(R.string.pref_theme_value_dark)).perform(click());
        pressBack();

        openMenuItem(R.string.action_settings);
        clickPreference(R.string.pref_theme_title);
        onView(withText(R.string.pref_theme_value_dark)).check(matches(isChecked()));
        onView(withText(R.string.pref_theme_value_auto)).perform(click());
        pressBack();

        openMenuItem(R.string.action_settings);
        clickPreference(R.string.pref_theme_title);
        onView(withText(R.string.pref_theme_value_auto)).check(matches(isChecked()));
        onView(withText(R.string.pref_theme_value_light)).perform(click());
        pressBack();

        openMenuItem(R.string.action_settings);
        clickPreference(R.string.pref_theme_title);
    }

    private void useCleanLayout() {
        openMenuItem(R.string.action_settings);
        clickPreference(R.string.pref_layout_title);
        onView(withText(R.string.pref_layout_value_clean)).perform(click());
        pressBack();
    }
}
