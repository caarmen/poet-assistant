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


import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkFirstDefinition;
import static ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;

import android.app.Activity;
import android.content.Intent;
import android.os.SystemClock;

import androidx.annotation.StringRes;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;

@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class SelectedTabTest {

    @Rule
    public PoetAssistantActivityTestRule<MainActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(MainActivity.class, false);

    @Test
    public void dictionaryDeepLinkAfterThesaurusTabSaved() {
        Intent intent = new Intent();
        mActivityTestRule.launchActivity(intent);
        String deepLinkUrl = "poetassistant://dictionary/muffin";

        swipeViewPagerLeft(1);
        checkTitleStripOrTab(mActivityTestRule.getActivity(), R.string.tab_thesaurus);

        // Open a deep link
        getInstrumentation().getUiAutomation().executeShellCommand("am start -a android.intent.action.VIEW -d " + deepLinkUrl);
        SystemClock.sleep(500); // :'(

        // Check the results
        Activity activity = mActivityTestRule.getActivity();
        getInstrumentation().waitForIdleSync();
        checkTitleStripOrTab(activity, R.string.tab_dictionary);
        checkFirstDefinition("a sweet quick bread baked in a cup-shaped pan");
    }

    @Test
    public void dictionaryDeepLinkAfterDictionaryTabSaved() {
        Intent intent = new Intent();
        mActivityTestRule.launchActivity(intent);
        String deepLinkUrl = "poetassistant://dictionary/muffin";

        swipeViewPagerLeft(2);
        checkTitleStripOrTab(mActivityTestRule.getActivity(), R.string.tab_dictionary);

        // Open a deep link
        getInstrumentation().getUiAutomation().executeShellCommand("am start -a android.intent.action.VIEW -d " + deepLinkUrl);
        SystemClock.sleep(500); // :'(

        // Check the results
        Activity activity = mActivityTestRule.getActivity();
        getInstrumentation().waitForIdleSync();
        checkTitleStripOrTab(activity, R.string.tab_dictionary);
        checkFirstDefinition("a sweet quick bread baked in a cup-shaped pan");
    }


    @Test
    public void openAfterLastDictionary() {
        testSaveTab(() -> swipeViewPagerLeft(2), R.string.tab_dictionary, R.string.tab_dictionary);
    }

    @Test
    public void openAfterLastReader() {
        testSaveTab(() -> swipeViewPagerLeft(3), R.string.tab_reader, R.string.tab_reader);
    }

    @Test
    public void openAfterLastFavorites() {
        testSaveTab(() -> swipeViewPagerLeft(4), R.string.tab_favorites, R.string.tab_favorites);
    }

    private void testSaveTab(Runnable openTabAction, @StringRes int expectedTabBeforeStop, @StringRes int expectedTabAfterRestart) {
        Intent intent = new Intent();
        mActivityTestRule.launchActivity(intent);
        openTabAction.run();
        checkTitleStripOrTab(mActivityTestRule.getActivity(), expectedTabBeforeStop);
        mActivityTestRule.relaunch();
        checkTitleStripOrTab(mActivityTestRule.getActivity(), expectedTabAfterRestart);
    }

}
