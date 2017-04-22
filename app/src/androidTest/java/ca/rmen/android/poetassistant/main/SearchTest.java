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


import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import ca.rmen.android.poetassistant.R;

import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeText;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkPatterns;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkRhymes;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkSearchSuggestions;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clearSearchHistory;
import static ca.rmen.android.poetassistant.main.TestAppUtils.openSearchView;
import static ca.rmen.android.poetassistant.main.TestAppUtils.search;
import static ca.rmen.android.poetassistant.main.TestAppUtils.typeQuery;
import static ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;

/**
 * Tested on:
 * - Huawei P9 Lite
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class SearchTest extends BaseTest {

    @Test
    public void searchSuggestionsTest() {
        openSearchView();
        ViewInteraction searchAutoComplete = typeQuery("heavy");
        checkSearchSuggestions("heavy", "heavyset", "heavyweight", "heavyweights");

        searchAutoComplete.perform(typeText("s"));
        checkSearchSuggestions("heavyset");

        searchAutoComplete.perform(typeText("z"));
        checkSearchSuggestions();
    }

    @Test
    public void searchHistoryTest() {
        openSearchView();
        checkSearchSuggestions();

        ViewInteraction searchAutoComplete = typeQuery("carmen");
        checkSearchSuggestions();

        searchAutoComplete.perform(pressImeActionButton());

        openSearchView();
        checkSearchSuggestions("carmen");

        typeQuery("benoit");
        checkSearchSuggestions();
        searchAutoComplete.perform(pressImeActionButton());

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
        checkSearchSuggestions();
        typeQuery("carme");
        checkSearchSuggestions("carmelite");
    }

    @Test
    public void patternSearchTest() {
        search("h*llo");
        checkPatterns(mActivityTestRule.getActivity(), "h*llo", "hello", "hermosillo", "hollo", "hullo");
        search("h*llz");
        checkPatterns(mActivityTestRule.getActivity(), "h*llz");
    }

    @Test
    public void showAllRhymesTest() {
        search("faith");
        checkRhymes(mActivityTestRule.getActivity(), "eighth", "interfaith");
        openMenuItem(R.string.action_settings);
        clickPreference(R.string.all_rhymes_setting_title);
        pressBack();
        search("faith");
        checkRhymes(mActivityTestRule.getActivity(), "eighth", "haith");
    }

}
