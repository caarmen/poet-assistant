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
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import ca.rmen.android.poetassistant.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkAllStarredWords;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkRhymes;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkStarredInList;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clearPoem;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clearSearchHistory;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clearStarredWords;
import static ca.rmen.android.poetassistant.main.TestAppUtils.filter;
import static ca.rmen.android.poetassistant.main.TestAppUtils.openDictionary;
import static ca.rmen.android.poetassistant.main.TestAppUtils.openDictionaryCleanLayout;
import static ca.rmen.android.poetassistant.main.TestAppUtils.openThesaurus;
import static ca.rmen.android.poetassistant.main.TestAppUtils.openThesaurusCleanLayout;
import static ca.rmen.android.poetassistant.main.TestAppUtils.search;
import static ca.rmen.android.poetassistant.main.TestAppUtils.starQueryWord;
import static ca.rmen.android.poetassistant.main.TestAppUtils.typePoem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerRight;
import static org.hamcrest.Matchers.containsString;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class IntegrationTest extends BaseTest {

    @Test
    public void integrationTest1() {
        Context context = mActivityTestRule.getActivity();
        swipeViewPagerLeft(4);
        checkAllStarredWords(context);
        swipeViewPagerRight(4);
        search("howdy");
        checkRhymes(context, "cloudy", "dowdy");
        openThesaurus(context, "cloudy", "nebulose");
        openDictionary(context, "nebulous", "lacking definite form or limits");
        starQueryWord();
        swipeViewPagerLeft(2);
        checkAllStarredWords(context, "nebulous");
        swipeViewPagerRight(3);
        checkStarredInList("nebulous");
        filter("bloody", "muddy", "nebulose");
        swipeViewPagerRight(1);
        filter("bully", "rowdy", "cloudy");
        swipeViewPagerLeft(3);
        typePoem("Forever is composed of nows");
        clearPoem();
        // clearing the search history doesn't erase starred words
        clearSearchHistory();
        swipeViewPagerLeft(1);
        checkAllStarredWords(context, "nebulous");
        clearStarredWords();
        checkAllStarredWords(context);
    }

    @Test
    public void integrationTest2() {
        Context context = mActivityTestRule.getActivity();
        swipeViewPagerLeft(4);
        checkAllStarredWords(context);
        swipeViewPagerRight(4);
        search("beholden");
        checkRhymes(context, "embolden", "golden");
        openThesaurus(context, "embolden", "hearten");
        openDictionary(context, "recreate", "create anew");
        starQueryWord();
        swipeViewPagerLeft(2);
        checkAllStarredWords(context, "recreate");
        swipeViewPagerRight(3);
        checkStarredInList("recreate");
        filter("beer", "cheer", "hearten");
        swipeViewPagerRight(1);
        filter("wildness", "abandon", "embolden");
        swipeViewPagerLeft(3);
        typePoem("roses are red, violets are blue\nespresso tests will find bugs for you");
        clearPoem();

        clearSearchHistory();
        swipeViewPagerLeft(1);
        checkAllStarredWords(context, "recreate");
        clearStarredWords();
        checkAllStarredWords(context);
    }

    @Test
    public void cleanLayout1Test() {
        Context context = mActivityTestRule.getActivity();
        useCleanLayout();

        swipeViewPagerLeft(4);
        checkAllStarredWords(context);
        swipeViewPagerRight(4);
        search("howdy");
        checkRhymes(context, "cloudy", "dowdy");
        openThesaurusCleanLayout(context, "cloudy", "nebulose");
        openDictionaryCleanLayout(context, "nebulous", "lacking definite form or limits");
        starQueryWord();
        swipeViewPagerLeft(2);
        checkAllStarredWords(context, "nebulous");
        swipeViewPagerRight(3);
        checkStarredInList("nebulous");
        filter("bloody", "muddy", "nebulose");
        swipeViewPagerRight(1);
        filter("bully", "rowdy", "cloudy");
        swipeViewPagerLeft(3);
        typePoem("Forever is composed of nows");
        clearPoem();
        // clearing the search history doesn't erase starred words
        clearSearchHistory();
        swipeViewPagerLeft(1);
        checkAllStarredWords(context, "nebulous");
        clearStarredWords();
        checkAllStarredWords(context);
    }

    @Test
    public void cleanLayout2Test() {
        Context context = mActivityTestRule.getActivity();
        useCleanLayout();

        swipeViewPagerLeft(4);
        checkAllStarredWords(context);
        swipeViewPagerRight(4);
        search("beholden");
        checkRhymes(context, "embolden", "golden");
        openThesaurusCleanLayout(context, "embolden", "hearten");
        openDictionaryCleanLayout(context, "recreate", "create anew");
        starQueryWord();
        swipeViewPagerLeft(2);
        checkAllStarredWords(context, "recreate");
        swipeViewPagerRight(3);
        checkStarredInList("recreate");
        filter("beer", "cheer", "hearten");
        swipeViewPagerRight(1);
        filter("wildness", "abandon", "embolden");
        swipeViewPagerLeft(3);
        typePoem("roses are red, violets are blue\nespresso tests will find bugs for you");
        clearPoem();

        clearSearchHistory();
        swipeViewPagerLeft(1);
        checkAllStarredWords(context, "recreate");
        clearStarredWords();
        checkAllStarredWords(context);
    }

    @Test
    public void openAboutScreenTest() {
        openMenuItem(R.string.action_about);
        onView(withId(R.id.tv_poet_assistant_license))
                .check(matches(isCompletelyDisplayed()))
                .check(matches(withText(R.string.about_license_app)))
                .perform(click());

        onView(withId(R.id.tv_license_text))
                .check(matches(isDisplayed()))
                .check(matches(withText(containsString("GNU GENERAL"))));
        pressBack();
        onView(withId(R.id.tv_source_code))
                .check(matches(isCompletelyDisplayed()))
                .check(matches(withText(R.string.about_projectUrl)));
    }

    private void useCleanLayout() {
        openMenuItem(R.string.action_settings);
        clickPreference(R.string.pref_layout_title);
        onView(withText(R.string.pref_layout_value_clean)).perform(click());
        pressBack();
    }
}
