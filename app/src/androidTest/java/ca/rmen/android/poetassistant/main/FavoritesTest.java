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


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkAllStarredWords;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clearStarredWords;
import static ca.rmen.android.poetassistant.main.TestAppUtils.search;
import static ca.rmen.android.poetassistant.main.TestAppUtils.starQueryWord;
import static ca.rmen.android.poetassistant.main.TestAppUtils.unStarQueryWord;
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
public class FavoritesTest {

    @Rule
    public PoetAssistantActivityTestRule<MainActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(MainActivity.class, true);

    @Test
    public void favoritesTest() {
        Context context = mActivityTestRule.getActivity();
        search("cheesecake");
        starQueryWord();
        onView(allOf(withId(R.id.btn_star_result), hasSibling(withText("ache")))).perform(click());
        swipeViewPagerLeft(4);
        checkAllStarredWords(context, "cheesecake", "ache");
        swipeViewPagerRight(3);
        unStarQueryWord();
        swipeViewPagerLeft(3);
        checkAllStarredWords(context, "ache");
        onView(allOf(withId(R.id.btn_star_result), hasSibling(withText("ache")), isDisplayed())).perform(click());
        checkAllStarredWords(context);
        swipeViewPagerRight(2);
        starQueryWord();
        swipeViewPagerLeft(2);
        checkAllStarredWords(context, "cheesecake");
        clearStarredWords();
        checkAllStarredWords(context);
    }
}

