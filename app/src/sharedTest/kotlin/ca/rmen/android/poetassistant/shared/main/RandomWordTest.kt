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

import android.os.Build
import android.view.View
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.CustomChecks.hasNonEmptyText
import ca.rmen.android.poetassistant.main.MainActivity
import ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab
import ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference
import ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.ui.composables.DICTIONARY_HEADER_TEXT_TAG
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import ca.rmen.android.poetassistant.settings.SettingsRepository
import ca.rmen.android.poetassistant.settings.WotdNotificationPriority
import ca.rmen.android.poetassistant.settings.ui.composables.SETTINGS_WOTD_NOTIFICATION_PRIORITY_ITEM_TAG_PREFIX
import ca.rmen.android.poetassistant.settings.ui.composables.SETTINGS_WOTD_NOTIFICATION_PRIORITY_ROW_TAG
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.hamcrest.Matcher
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@LargeTest
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(AndroidJUnit4::class)
class RandomWordTest {

    @get:Rule(order = 0)
    val hiltTestRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createEmptyComposeRule()

    @get:Rule(order = 2)
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> =
        PoetAssistantActivityTestRule(MainActivity::class.java, true)

    @Test
    fun randomWordTest() {
        openMenuItem(R.string.action_random_word)
        checkTitleStripOrTab(activityTestRule.activity, R.string.tab_dictionary)
        composeTestRule.onNode(hasTestTag(DICTIONARY_HEADER_TEXT_TAG) and hasNonEmptyText()).assertIsDisplayed()
    }

    @Test
    fun wotdNotificationPriorityPresenceTest() {
        openMenuItem(R.string.action_settings)
        val prioritySettingMatcher: Matcher<View> = withText(R.string.wotd_setting_system_notification_priority_title)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            onView(prioritySettingMatcher).check(doesNotExist())
        } else {
            // TODO update this to look for the wotd page
            /*
            onView(withId(R.id.wotd_recycler_view))
                .perform(scrollTo<ResultListAdapter.ResultListEntryViewHolder>(hasDescendant(prioritySettingMatcher)))
            onView(prioritySettingMatcher).check(matches(isDisplayed()))
             */
        }
    }

    @Test
    fun wotdNotificationPriorityEnabledTest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return // The setting isn't available on O+
        }
        openMenuItem(R.string.action_settings)
        composeTestRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasTestTag(SETTINGS_WOTD_NOTIFICATION_PRIORITY_ROW_TAG))
        // The priority row is disabled while the wotd switch is off.
        composeTestRule
            .onNode(hasTestTag(SETTINGS_WOTD_NOTIFICATION_PRIORITY_ROW_TAG))
            .assertIsNotEnabled()
        // The priority row is enabled when the wotd switch is on.
        clickPreference(composeTestRule, activityTestRule.activity, R.string.wotd_setting_title)
        composeTestRule
            .onNode(hasTestTag(SETTINGS_WOTD_NOTIFICATION_PRIORITY_ROW_TAG))
            .assertIsEnabled()
    }

    @Test
    fun wotdNotificationPrioritySelectionTest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return // The setting isn't available on O+
        }
        openMenuItem(R.string.action_settings)
        // The priority row is disabled while the wotd switch is off:
        // turn wotd on first.
        clickPreference(composeTestRule, activityTestRule.activity, R.string.wotd_setting_title)

        // The default priority is DEFAULT.
        assertEquals(WotdNotificationPriority.DEFAULT, wotdNotificationPriority())

        // Selecting each dropdown item stores the value.
        for (priority in WotdNotificationPriority.entries) {
            composeTestRule
                .onNode(hasScrollAction())
                .performScrollToNode(hasTestTag(SETTINGS_WOTD_NOTIFICATION_PRIORITY_ROW_TAG))
            composeTestRule
                .onNode(hasTestTag(SETTINGS_WOTD_NOTIFICATION_PRIORITY_ROW_TAG))
                .performClick()
            composeTestRule
                .onNode(hasTestTag(SETTINGS_WOTD_NOTIFICATION_PRIORITY_ITEM_TAG_PREFIX + priority.name))
                .performClick()
            assertEquals(priority, wotdNotificationPriority())
        }
    }

    private fun wotdNotificationPriority(): WotdNotificationPriority =
        SettingsRepository(activityTestRule.activity.application)
            .settingsFlow
            .value
            .wotdNotificationPriority
}
