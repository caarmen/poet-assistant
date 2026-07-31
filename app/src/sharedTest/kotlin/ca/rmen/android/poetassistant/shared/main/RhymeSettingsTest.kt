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

package ca.rmen.android.poetassistant.shared.main

import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.CustomChecks.checkRhymes
import ca.rmen.android.poetassistant.main.MainActivity
import ca.rmen.android.poetassistant.main.TestAppUtils.search
import ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference
import ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@LargeTest
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(AndroidJUnit4::class)
class RhymeSettingsTest {

    @get:Rule(order = 0)
    val hiltTestRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> =
        PoetAssistantActivityTestRule(MainActivity::class.java, true)

    @Test
    fun testMatchAORAOEnabled() {
        openMenuItem(R.string.action_settings)
        clickPreference(R.string.match_aor_ao_setting_title)
        pressBack()
        search("thorny")
        checkRhymes(activityTestRule.activity, "barany", "brawny")
        search("brawny")
        checkRhymes(activityTestRule.activity, "barany", "cornie")
    }

    @Test
    fun testMatchAORAODisabled() {
        search("thorny")
        checkRhymes(activityTestRule.activity, "cornie", "corny")
        search("brawny")
        checkRhymes(activityTestRule.activity, "barany", "scrawny")
    }

    @Test
    fun testMatchAOAAEnabled() {
        openMenuItem(R.string.action_settings)
        clickPreference(R.string.match_ao_aa_setting_title)
        pressBack()
        search("trauma")
        checkRhymes(activityTestRule.activity, "bahama", "cama")
        search("across")
        checkRhymes(activityTestRule.activity, "alsace", "bos")
    }

    @Test
    fun testMatchAOAADisabled() {
        search("trauma")
        checkRhymes(activityTestRule.activity, "abasia", "abila")
        search("across")
        checkRhymes(activityTestRule.activity, "boss", "boss'")
    }

    @Test
    fun testRhymesWithDefinitionsOnly() {
        search("faith")
        checkRhymes(activityTestRule.activity, "eighth", "interfaith")
    }

    @Test
    fun testRhymesWithoutDefinitions() {
        openMenuItem(R.string.action_settings)
        clickPreference(R.string.all_rhymes_setting_title)
        pressBack()
        search("faith")
        checkRhymes(activityTestRule.activity, "eighth", "haith")
    }
}
