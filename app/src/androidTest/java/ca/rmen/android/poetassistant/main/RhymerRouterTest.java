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


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.search.RhymerRouterActivity;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RhymerRouterTest {

    @Rule
    public PoetAssistantActivityTestRule<RhymerRouterActivity> mActivityTestRule =
            new PoetAssistantActivityTestRule<>(RhymerRouterActivity.class, false);

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void onTextRouted() {
        Intent intent = new Intent(Intent.ACTION_PROCESS_TEXT);
        intent.putExtra(Intent.EXTRA_PROCESS_TEXT, "polyvalent");
        mActivityTestRule.launchActivity(intent);
        Activity activity = mActivityTestRule.getActivity();
        checkTitleStripOrTab(activity, R.string.tab_rhymer);
        onView(allOf(withId(R.id.empty), isDisplayed()))
                .check(matches(withText(activity.getString(R.string.empty_rhymer_list_with_query, "polyvalent"))));
        swipeViewPagerLeft(1);
        onView(allOf(withId(R.id.empty), isDisplayed(), withText(R.string.empty_list_without_query)))
                .check(matches(isDisplayed()));
        swipeViewPagerLeft(1);
        onView(allOf(withId(R.id.empty), isDisplayed(), withText(R.string.empty_list_without_query)))
                .check(matches(isDisplayed()));
    }

}
