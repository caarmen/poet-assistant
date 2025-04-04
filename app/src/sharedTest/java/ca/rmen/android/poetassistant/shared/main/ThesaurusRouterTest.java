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

package ca.rmen.android.poetassistant.shared.main;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkFirstSynonym;
import static ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerRight;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.search.ThesaurusRouterActivity;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.HiltTestApplication;

@LargeTest
@HiltAndroidTest
@Config(application = HiltTestApplication.class)
@RunWith(AndroidJUnit4.class)
public class ThesaurusRouterTest {

    @Rule(order = 0)
    public HiltAndroidRule hiltTestRule = new HiltAndroidRule(this);
    @Rule(order = 1)
    public PoetAssistantActivityTestRule<ThesaurusRouterActivity> mActivityTestRule =
            new PoetAssistantActivityTestRule<>(ThesaurusRouterActivity.class, false);

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    @Config(qualifiers = "w360dp-h640dp")
    public void onTextRouted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        Intent intent = new Intent(Intent.ACTION_PROCESS_TEXT);
        intent.putExtra(Intent.EXTRA_PROCESS_TEXT, "almostic");
        mActivityTestRule.launchActivity(intent);
        Activity activity = mActivityTestRule.getActivity();
        checkTitleStripOrTab(activity, R.string.tab_thesaurus);
        onView(allOf(withId(R.id.tv_list_header), isDisplayed()))
                .check(matches(withText("almost")));
        checkFirstSynonym("about");
        swipeViewPagerLeft(1);
        onView(allOf(withId(R.id.empty), isDisplayed(), withText(R.string.empty_list_without_query)))
                .check(matches(isDisplayed()));
        swipeViewPagerRight(2);
        onView(allOf(withId(R.id.empty), isDisplayed(), withText(R.string.empty_list_without_query)))
                .check(matches(isDisplayed()));
    }

}
