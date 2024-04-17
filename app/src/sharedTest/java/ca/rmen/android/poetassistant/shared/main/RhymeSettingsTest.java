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


import androidx.test.filters.LargeTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.MainActivity;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;
import ca.rmen.android.poetassistant.main.rules.RetryTestRule;

import static androidx.test.espresso.Espresso.pressBack;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkRhymes;
import static ca.rmen.android.poetassistant.main.TestAppUtils.search;
import static ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RhymeSettingsTest {

    @Rule
    public PoetAssistantActivityTestRule<MainActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(MainActivity.class, true);

    @Test
    public void testMatchAORAOEnabled() {
        openMenuItem(R.string.action_settings);
        clickPreference(R.string.match_aor_ao_setting_title);
        pressBack();
        search("thorny");
        checkRhymes(mActivityTestRule.getActivity(), "barany", "brawny");
        search("brawny");
        checkRhymes(mActivityTestRule.getActivity(), "barany", "cornie");
    }

    @Test
    public void testMatchAORAODisabled() {
        search("thorny");
        checkRhymes(mActivityTestRule.getActivity(), "cornie", "corny");
        search("brawny");
        checkRhymes(mActivityTestRule.getActivity(), "barany", "scrawny");
    }

    @Test
    public void testMatchAOAAEnabled() {
        openMenuItem(R.string.action_settings);
        clickPreference(R.string.match_ao_aa_setting_title);
        pressBack();
        search("trauma");
        checkRhymes(mActivityTestRule.getActivity(), "bahama", "cama");
        search("across");
        checkRhymes(mActivityTestRule.getActivity(), "alsace", "bos");
    }

    @Test
    public void testMatchAOAADisabled() {
        search("trauma");
        checkRhymes(mActivityTestRule.getActivity(), "abasia", "abila");
        search("across");
        checkRhymes(mActivityTestRule.getActivity(), "boss", "boss'");
    }

    @Test
    public void testRhymesWithDefinitionsOnly() {
        search("faith");
        checkRhymes(mActivityTestRule.getActivity(), "eighth", "interfaith");
    }

    @Test
    public void testRhymesWithoutDefinitions() {
        openMenuItem(R.string.action_settings);
        clickPreference(R.string.all_rhymes_setting_title);
        pressBack();
        search("faith");
        checkRhymes(mActivityTestRule.getActivity(), "eighth", "haith");
    }
}
