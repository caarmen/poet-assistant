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
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkPatterns;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkSingleRootView;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkSearchSuggestions;
import static ca.rmen.android.poetassistant.main.CustomViewMatchers.withAdapterItemCount;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clearSearchHistory;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clickDialogPositiveButton;
import static ca.rmen.android.poetassistant.main.TestAppUtils.openSearchView;
import static ca.rmen.android.poetassistant.main.TestAppUtils.search;
import static ca.rmen.android.poetassistant.main.TestAppUtils.starQueryWord;
import static ca.rmen.android.poetassistant.main.TestAppUtils.typeQuery;
import static ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SearchTest {
    @Rule
    public PoetAssistantActivityTestRule<MainActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(MainActivity.class, true);

    @Test
    public void searchSuggestionsTest() {
        openSearchView();
        ViewInteraction searchAutoComplete = typeQuery("heavy");
        checkSearchSuggestions("heavy", "heavyset", "heavyweight", "heavyweights");

        searchAutoComplete.perform(typeText("s"));
        checkSearchSuggestions("heavyset");

        searchAutoComplete.perform(typeText("z"));
        checkSingleRootView(mActivityTestRule.getActivity());
    }

    @Test
    public void searchHistoryTest() {
        openSearchView();
        checkSingleRootView(mActivityTestRule.getActivity());

        ViewInteraction searchAutoComplete = typeQuery("carmen");
        checkSingleRootView(mActivityTestRule.getActivity());

        searchAutoComplete.perform(pressImeActionButton());

        getInstrumentation().waitForIdleSync();
        openSearchView();
        checkSearchSuggestions("carmen");

        typeQuery("benoit");
        checkSingleRootView(mActivityTestRule.getActivity());
        searchAutoComplete.perform(pressImeActionButton());
        getInstrumentation().waitForIdleSync();

        openSearchView();
        checkSearchSuggestions("benoit", "carmen");

        typeQuery("awes");
        checkSearchSuggestions("awesome", "awesomely", "awestruck");
        searchAutoComplete.perform(typeText("o"));
        checkSearchSuggestions("awesome", "awesomely");

        searchAutoComplete.perform(clearText());
        searchAutoComplete.perform(typeText("carme"));
        checkSearchSuggestions("carmen", "carmelite");

        clearSearchHistory();

        openSearchView();
        checkSingleRootView(mActivityTestRule.getActivity());
        typeQuery("carme");
        checkSearchSuggestions("carmelite");
    }

    @Test
    public void patternSearchTest() {
        Context context = mActivityTestRule.getActivity();
        search("h*llo");
        checkPatterns(context, "h*llo", "hello", "hermosillo", "hollo", "hullo");
        search("h*llz");
        checkPatterns(context, "h*llz");
        onView(allOf(withId(R.id.btn_help), isDisplayed())).perform(click());
        onView(withText(R.string.pattern_help_title))
                .check(matches(isDisplayed()));
        clickDialogPositiveButton(android.R.string.ok);
        search("hello");
        checkTitleStripOrTab(context, R.string.tab_rhymer);
    }

    @Test
    public void patternSearchWithFavoriteTest() {
        search("hello");
        starQueryWord();
        search("he*o");
        checkPatterns(mActivityTestRule.getActivity(), "he*o", "hello", "head honcho", "hector hugo munro",
                "herero", "hereto", "hermosillo", "hero");
        onView(allOf(withId(R.id.btn_star_result), hasSibling(withText("hello")), isDisplayed())).check(matches(isChecked()));
    }

    @Test
    public void patternSearchTooManyResultsTest() {
        search("a*");
        onView(allOf(withId(R.id.recycler_view), isDisplayed()))
                .check(matches(withAdapterItemCount(501)));
    }

}
