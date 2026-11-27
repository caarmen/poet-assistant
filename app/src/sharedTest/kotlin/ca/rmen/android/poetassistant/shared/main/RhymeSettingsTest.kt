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

import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import ca.rmen.android.poetassistant.main.CustomChecks.checkRhymes
import ca.rmen.android.poetassistant.main.MainActivity
import ca.rmen.android.poetassistant.main.TestAppUtils.search
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import ca.rmen.android.poetassistant.settings.SettingsRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject

@LargeTest
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(AndroidJUnit4::class)
class RhymeSettingsTest {

    @get:Rule(order = 0)
    val hiltTestRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createEmptyComposeRule()

    @get:Rule(order = 2)
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> =
        PoetAssistantActivityTestRule(MainActivity::class.java, true)

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        hiltTestRule.inject()
    }

    @Test
    fun testMatchAORAOEnabled() {
        runBlocking {
            settingsRepository.setAORAOMatchEnabled(true)
        }
        composeTestRule.waitForIdle()
        search(composeTestRule, "thorny")
        checkRhymes(composeTestRule, "barany", "brawny")
        composeTestRule.waitForIdle()
        search(composeTestRule, "brawny")
        checkRhymes(composeTestRule, "barany", "cornie")
    }

    @Test
    fun testMatchAORAODisabled() {
        composeTestRule.waitForIdle()
        search(composeTestRule, "thorny")
        checkRhymes(composeTestRule, "cornie", "corny")
        composeTestRule.waitForIdle()
        search(composeTestRule, "brawny")
        checkRhymes(composeTestRule, "barany", "scrawny")
    }

    @Test
    fun testMatchAOAAEnabled() {
        runBlocking {
            settingsRepository.setAOAAMatchEnabled(true)
        }
        search(composeTestRule, "trauma")
        composeTestRule.waitForIdle()
        checkRhymes(composeTestRule, "bahama", "cama")
        search(composeTestRule, "across")
        composeTestRule.waitForIdle()
        checkRhymes(composeTestRule, "alsace", "bos")
    }

    @Test
    fun testMatchAOAADisabled() {
        search(composeTestRule, "trauma")
        checkRhymes(composeTestRule, "abasia", "abila")
        search(composeTestRule, "across")
        checkRhymes(composeTestRule, "boss", "boss'")
    }

    @Test
    fun testRhymesWithDefinitionsOnly() {
        search(composeTestRule, "faith")
        checkRhymes(composeTestRule, "eighth", "interfaith")
    }

    @Test
    fun testRhymesWithoutDefinitions() {
        runBlocking {
            settingsRepository.setAllRhymesEnabled(true)
        }
        composeTestRule.waitForIdle()
        search(composeTestRule, "faith")
        checkRhymes(composeTestRule, "eighth", "haith")
    }
}
