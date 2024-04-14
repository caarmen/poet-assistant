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


import static ca.rmen.android.poetassistant.main.TestAppUtils.search;
import static ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;

import android.content.Intent;

import androidx.annotation.StringRes;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.MainActivity;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;
import ca.rmen.android.poetassistant.main.rules.RetryTestRule;

@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class SelectedTabTest {

    @Rule
    public RetryTestRule retry = new RetryTestRule();

    @Rule
    public PoetAssistantActivityTestRule<MainActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(MainActivity.class, false);


    @Test
    public void openAfterLastRhymer() {
        testSaveTab(() -> {
        }, R.string.tab_rhymer, R.string.tab_rhymer);
    }

    @Test
    public void openAfterLastThesaurus() {
        testSaveTab(() -> swipeViewPagerLeft(1), R.string.tab_thesaurus, R.string.tab_thesaurus);
    }

    @Test
    public void openAfterLastWotd() {
        testSaveTab(() -> openMenuItem(R.string.action_wotd_history), R.string.tab_wotd, R.string.tab_rhymer);
    }

    @Test
    public void openAfterLastPattern() {
        testSaveTab(() -> search("h*llo"), R.string.tab_pattern, R.string.tab_rhymer);
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
