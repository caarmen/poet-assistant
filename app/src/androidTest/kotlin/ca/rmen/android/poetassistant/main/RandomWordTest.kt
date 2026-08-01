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
import android.service.notification.StatusBarNotification
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ca.rmen.android.poetassistant.BuildConfig
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertNotNull

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import ca.rmen.android.poetassistant.main.CustomViewMatchers.childAtPosition
import ca.rmen.android.poetassistant.main.CustomViewMatchers.withAdapterItemCount
import ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference
import ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem
import ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft
import ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerRight

@LargeTest
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RandomWordTest {

    @JvmField
    @Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> = PoetAssistantActivityTestRule(MainActivity::class.java, true)

    @Test
    fun openWotdListTest() {
        openMenuItem(R.string.action_wotd_history)
        val latestEntryViewMatcher = childAtPosition(withId(R.id.wotd_recycler_view), 0)
        // Check that the date field in the first (most recent) entry in the Wotd list contains today's date.
        val cal = Calendar.getInstance()
        val dayOfMonth = cal[Calendar.DAY_OF_MONTH]
        onView(allOf(withId(R.id.date), withParent(childAtPosition(latestEntryViewMatcher, 1))))
                .check(matches(withText(containsString(dayOfMonth.toString()))))
        onView(allOf(withId(R.id.btn_rhymer), isDescendantOfA(latestEntryViewMatcher)))
                .perform(click())
        swipeViewPagerLeft(5)
        onView(allOf(withId(R.id.btn_thesaurus), isDescendantOfA(latestEntryViewMatcher)))
                .perform(click())
        swipeViewPagerLeft(4)
        onView(allOf(withId(R.id.btn_dictionary), isDescendantOfA(latestEntryViewMatcher)))
                .perform(click())
        swipeViewPagerLeft(3)
        onView(allOf(withId(R.id.btn_star_result), isDescendantOfA(latestEntryViewMatcher)))
                .perform(click())
        swipeViewPagerRight(1)
        onView(allOf(withId(R.id.favorites_recycler_view), isDisplayed())).check(matches(withAdapterItemCount(1)))
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
