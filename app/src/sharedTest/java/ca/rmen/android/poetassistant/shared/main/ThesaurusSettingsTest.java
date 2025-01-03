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


import static androidx.test.espresso.Espresso.pressBack;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkRhyme;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkSynonym;
import static ca.rmen.android.poetassistant.main.TestAppUtils.addFilter;
import static ca.rmen.android.poetassistant.main.TestAppUtils.search;
import static ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.PerformException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.MainActivity;
import ca.rmen.android.poetassistant.main.Tab;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ThesaurusSettingsTest {

    @Rule
    public PoetAssistantActivityTestRule<MainActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(MainActivity.class, true);

    @Test
    public void testReverseLookupEnabled() {
        openMenuItem(R.string.action_settings);
        clickPreference(R.string.thesaurus_reverse_lookup_setting_title);
        pressBack();
        swipeViewPagerLeft(1);
        search("mistake");
        checkSynonym("blunder");
    }

    @Test(expected = PerformException.class)
    public void testReverseLookupDisabled() {
        swipeViewPagerLeft(1);
        search("mistake");
        checkSynonym("blunder");
    }

    @Test
    public void testFilterWithReverseLookupEnabled() {
        openMenuItem(R.string.action_settings);
        clickPreference(R.string.thesaurus_reverse_lookup_setting_title);
        pressBack();
        search("number");
        addFilter(Tab.RHYMER, "mistake", "bloomer");
        checkRhyme("blunder");
    }

    @Test(expected = NoMatchingViewException.class)
    public void testFilterWithReverseLookupDisabled() {
        search("number");
        addFilter(Tab.RHYMER, "mistake", null);
        checkRhyme("blunder");
    }
}
