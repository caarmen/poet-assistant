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

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab
import ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft
import ca.rmen.android.poetassistant.main.dictionaries.search.RhymerRouterActivity
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@LargeTest
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(AndroidJUnit4::class)
class RhymerRouterTest {

    @get:Rule(order = 0)
    val hiltTestRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityTestRule: PoetAssistantActivityTestRule<RhymerRouterActivity> =
        PoetAssistantActivityTestRule(RhymerRouterActivity::class.java, false)

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    fun onTextRouted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val intent = Intent(Intent.ACTION_PROCESS_TEXT).apply {
            putExtra(Intent.EXTRA_PROCESS_TEXT, "polyvalent")
        }
        activityTestRule.launchActivity(intent)
        val activity: Activity = activityTestRule.activity
        checkTitleStripOrTab(activity, R.string.tab_rhymer)
        onView(allOf(withId(R.id.empty), isDisplayed()))
            .check(matches(withText(activity.getString(R.string.empty_rhymer_list_with_query, "polyvalent"))))
        swipeViewPagerLeft(1)
        onView(allOf(withId(R.id.empty), isDisplayed(), withText(R.string.empty_list_without_query)))
            .check(matches(isDisplayed()))
        swipeViewPagerLeft(1)
        onView(allOf(withId(R.id.empty), isDisplayed(), withText(R.string.empty_list_without_query)))
            .check(matches(isDisplayed()))
    }
}
