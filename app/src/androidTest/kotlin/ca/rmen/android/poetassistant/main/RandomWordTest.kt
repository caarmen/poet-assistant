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

package ca.rmen.android.poetassistant.main


import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ca.rmen.android.poetassistant.BuildConfig
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertNotNull

import ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference
import ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem
import ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft
import ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerRight
import ca.rmen.android.poetassistant.main.favorites.ui.composables.FAVORITES_SCREEN_CONTENT_LIST_TAG
import ca.rmen.android.poetassistant.main.wotd.ui.composables.WOTD_SCREEN_CONTENT_LIST_TAG

@LargeTest
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RandomWordTest {

    @JvmField
    @Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val composeTestRule = createEmptyComposeRule()

    @JvmField
    @Rule(order = 2)
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> = PoetAssistantActivityTestRule(MainActivity::class.java, true)

    @Test
    fun openWotdListTest() {
        openMenuItem(R.string.action_wotd_history)
        val context = activityTestRule.activity
        val wotdListMatcher = hasTestTag(WOTD_SCREEN_CONTENT_LIST_TAG)
        val listNode = composeTestRule.onNodeWithTag(WOTD_SCREEN_CONTENT_LIST_TAG)
        listNode.assertIsDisplayed()
        // Check that the date in the first (most recent) entry in the Wotd list contains today's date.
        val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString()
        listNode.onChildren().onFirst().assertTextContains(dayOfMonth, substring = true)
        // The row buttons are identified by their content descriptions, not their test tags:
        // the tags contain the word of the day (and the date for the star), which is not
        // known in this test. The content descriptions are shared with other screens,
        // so they are scoped to the Wotd list, and onFirst() selects the most recent row.
        composeTestRule.onAllNodes(
            hasContentDescription(context.getString(R.string.tab_rhymer)) and hasAnyAncestor(wotdListMatcher)
        ).onFirst().performClick()
        swipeViewPagerLeft(5)
        composeTestRule.onAllNodes(
            hasContentDescription(context.getString(R.string.tab_thesaurus)) and hasAnyAncestor(wotdListMatcher)
        ).onFirst().performClick()
        swipeViewPagerLeft(4)
        composeTestRule.onAllNodes(
            hasContentDescription(context.getString(R.string.tab_dictionary)) and hasAnyAncestor(wotdListMatcher)
        ).onFirst().performClick()
        swipeViewPagerLeft(3)
        // Star the word of the day
        composeTestRule.onAllNodes(
            hasContentDescription(context.getString(R.string.content_description_toggle_favorite)) and hasAnyAncestor(wotdListMatcher)
        ).onFirst().performClick()
        swipeViewPagerRight(1)
        composeTestRule.onNodeWithTag(FAVORITES_SCREEN_CONTENT_LIST_TAG)
                .onChildren().assertCountEquals(1)
    }

    @Test
    fun wotdNotificationTest() {
        openMenuItem(R.string.action_settings)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(BuildConfig.APPLICATION_ID, Manifest.permission.POST_NOTIFICATIONS)
        }
        clickPreference(R.string.wotd_setting_title)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = getInstrumentation().targetContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            assertNotNull(notificationManager)
            val activeNotifications = notificationManager.activeNotifications
            var foundNotification = false
            for (statusBarNotification in activeNotifications) {
                statusBarNotification.notification
                val title = statusBarNotification.notification.extras.getCharSequence("android.title")
                if (title != null && title.toString().startsWith(activityTestRule.activity.getString(R.string.wotd_setting_title))) {
                    foundNotification = true
                    break
                }
            }
            assertTrue("Didn't find a Wotd notification", foundNotification)
        }

        // Disable it again
        clickPreference(R.string.wotd_setting_title)
    }
}
