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


import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ca.rmen.android.poetassistant.R;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkRhymes;
import static ca.rmen.android.poetassistant.main.TestAppUtils.verifyFirstDefinition;
import static ca.rmen.android.poetassistant.main.TestAppUtils.verifyFirstSynonym;
import static ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripCenterTitle;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerRight;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class IntentTest {

    @Rule
    public PoetAssistantActivityTestRule<MainActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(MainActivity.class, false);

    @Test
    public void onNewIntentQueryTest() {
        MainActivity activity = mActivityTestRule.launchActivity(new Intent());
        launchNewIntent(Intent.ACTION_SEARCH, SearchManager.QUERY, "muffin");
        checkTitleStripCenterTitle(activity, R.string.tab_rhymer);
        checkRhymes(activity, "mcguffin", "toughen");
        swipeViewPagerLeft(1);
        verifyFirstSynonym("quick bread");
        swipeViewPagerLeft(1);
        verifyFirstDefinition("a sweet quick bread baked in a cup-shaped pan");
    }

    @Test
    public void onNewIntenthUserQueryTest() {
        MainActivity activity = mActivityTestRule.launchActivity(new Intent());
        launchNewIntent(Intent.ACTION_SEARCH, SearchManager.USER_QUERY, "muffin");
        checkTitleStripCenterTitle(activity, R.string.tab_rhymer);
        checkRhymes(activity, "mcguffin", "toughen");
        swipeViewPagerLeft(1);
        verifyFirstSynonym("quick bread");
        swipeViewPagerLeft(1);
        verifyFirstDefinition("a sweet quick bread baked in a cup-shaped pan");
    }

    @Test
    public void onNewIntenthViewQueryTest() {
        MainActivity activity = mActivityTestRule.launchActivity(new Intent());
        launchNewIntent(Intent.ACTION_VIEW, "poetassistant://query/muffin");
        checkTitleStripCenterTitle(activity, R.string.tab_dictionary);
        verifyFirstDefinition("a sweet quick bread baked in a cup-shaped pan");
        swipeViewPagerRight(1);
        verifyFirstSynonym("quick bread");
        swipeViewPagerRight(1);
        checkRhymes(activity, "mcguffin", "toughen");
    }

    @Test
    public void onNewIntentViewRhymerTest() {
        mActivityTestRule.launchActivity(new Intent());
        launchNewIntent(Intent.ACTION_VIEW, "poetassistant://rhymer/muffin");
        checkRhymerOnly("mcguffin", "toughen");
    }

    @Test
    public void onNewIntentViewThesaurusTest() {
        mActivityTestRule.launchActivity(new Intent());
        launchNewIntent(Intent.ACTION_VIEW, "poetassistant://thesaurus/muffin");
        checkThesaurusOnly("quick bread");
    }

    @Test
    public void onNewIntentViewDictionaryTest() {
        mActivityTestRule.launchActivity(new Intent());
        launchNewIntent(Intent.ACTION_VIEW, "poetassistant://dictionary/muffin");
        checkDictionaryOnly("a sweet quick bread baked in a cup-shaped pan");
    }

    @Test
    public void onNewIntentSendTest() {
        MainActivity activity = mActivityTestRule.launchActivity(new Intent());
        String poemText = "Dare to be honest and fear no labor."; // Robert Burns
        launchNewIntent(Intent.ACTION_SEND, Intent.EXTRA_TEXT, poemText);
        checkTitleStripCenterTitle(activity, R.string.tab_reader);
        onView(allOf(withId(R.id.tv_text), isDisplayed()))
                .check(matches(withText(poemText)));
    }

    @Test
    public void onCreateSendTest() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        String poemText = "Dare to be honest and fear no labor."; // Robert Burns
        intent.putExtra(Intent.EXTRA_TEXT, poemText);
        MainActivity activity = mActivityTestRule.launchActivity(intent);
        checkTitleStripCenterTitle(activity, R.string.tab_reader);
        onView(allOf(withId(R.id.tv_text), isDisplayed()))
                .check(matches(withText(poemText)));
    }

    @Test
    public void onCreatehViewQueryTest() {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse("poetassistant://query/muffin"));
        MainActivity activity = mActivityTestRule.launchActivity(intent);
        checkTitleStripCenterTitle(activity, R.string.tab_dictionary);
        verifyFirstDefinition("a sweet quick bread baked in a cup-shaped pan");
        swipeViewPagerRight(1);
        verifyFirstSynonym("quick bread");
        swipeViewPagerRight(1);
        checkRhymes(activity, "mcguffin", "toughen");
    }

    @Test
    public void onCreateViewRhymerTest() {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse("poetassistant://rhymer/muffin"));
        mActivityTestRule.launchActivity(intent);
        checkRhymerOnly("mcguffin", "toughen");
    }

    @Test
    public void onCreateViewThesaurusTest() {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse("poetassistant://thesaurus/muffin"));
        mActivityTestRule.launchActivity(intent);
        checkThesaurusOnly("quick bread");
    }

    @Test
    public void onCreateViewDictionaryTest() {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse("poetassistant://dictionary/muffin"));
        mActivityTestRule.launchActivity(intent);
        checkDictionaryOnly("a sweet quick bread baked in a cup-shaped pan");
    }

    private void launchNewIntent(String action, String extraKey, String extraValue) {
        Intent intent = new Intent(action);
        intent.putExtra(extraKey, extraValue);
        getInstrumentation().runOnMainSync(() -> mActivityTestRule.getActivity().onNewIntent(intent));
    }

    private void launchNewIntent(String action, String data) {
        Intent intent = new Intent(action);
        intent.setData(Uri.parse(data));
        getInstrumentation().runOnMainSync(() -> mActivityTestRule.getActivity().onNewIntent(intent));
    }

    private void checkRhymerOnly(String expectedRhyme1, String expectedRhyme2) {
        Activity activity = mActivityTestRule.getActivity();
        checkTitleStripCenterTitle(activity, R.string.tab_rhymer);
        checkRhymes(activity, expectedRhyme1, expectedRhyme2);
        swipeViewPagerLeft(1);
        onView(allOf(withId(R.id.empty), isDisplayed(), withText(R.string.empty_list_without_query)))
                .check(matches(isDisplayed()));
        swipeViewPagerLeft(1);
        onView(allOf(withId(R.id.empty), isDisplayed(), withText(R.string.empty_list_without_query)))
                .check(matches(isDisplayed()));
    }

    private void checkThesaurusOnly(String expectedFirstSynonym) {
        Activity activity = mActivityTestRule.getActivity();
        checkTitleStripCenterTitle(activity, R.string.tab_thesaurus);
        verifyFirstSynonym(expectedFirstSynonym);
        swipeViewPagerLeft(1);
        onView(allOf(withId(R.id.empty), isDisplayed(), withText(R.string.empty_list_without_query)))
                .check(matches(isDisplayed()));
        swipeViewPagerRight(2);
        onView(allOf(withId(R.id.empty), isDisplayed(), withText(R.string.empty_list_without_query)))
                .check(matches(isDisplayed()));
    }

    private void checkDictionaryOnly(String expectedFirstDefinition) {
        Activity activity = mActivityTestRule.getActivity();
        checkTitleStripCenterTitle(activity, R.string.tab_dictionary);
        verifyFirstDefinition(expectedFirstDefinition);
        swipeViewPagerRight(1);
        onView(allOf(withId(R.id.empty), isDisplayed(), withText(R.string.empty_list_without_query)))
                .check(matches(isDisplayed()));
        swipeViewPagerRight(1);
        onView(allOf(withId(R.id.empty), isDisplayed(), withText(R.string.empty_list_without_query)))
                .check(matches(isDisplayed()));
    }


}
