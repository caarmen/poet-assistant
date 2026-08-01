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


import android.app.Activity
import android.content.Intent
import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

import ca.rmen.android.poetassistant.main.CustomChecks.checkFirstDefinition
import ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab
import ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft

@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SelectedTabTest {

    @JvmField
    @Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> = PoetAssistantActivityTestRule(MainActivity::class.java, false)

    @Test
    fun dictionaryDeepLinkAfterThesaurusTabSaved() {
        val intent = Intent()
        activityTestRule.launchActivity(intent)
        val deepLinkUrl = "poetassistant://dictionary/muffin"

        swipeViewPagerLeft(1)
        checkTitleStripOrTab(activityTestRule.activity, R.string.tab_thesaurus)

        // Open a deep link
        getInstrumentation().uiAutomation.executeShellCommand("am start -a android.intent.action.VIEW -d " + deepLinkUrl)
        SystemClock.sleep(500) // :'(

        // Check the results
        val activity: Activity = activityTestRule.activity
        getInstrumentation().waitForIdleSync()
        checkTitleStripOrTab(activity, R.string.tab_dictionary)
        checkFirstDefinition("a sweet quick bread baked in a cup-shaped pan")
    }

    @Test
    fun dictionaryDeepLinkAfterDictionaryTabSaved() {
        val intent = Intent()
        activityTestRule.launchActivity(intent)
        val deepLinkUrl = "poetassistant://dictionary/muffin"

        swipeViewPagerLeft(2)
        checkTitleStripOrTab(activityTestRule.activity, R.string.tab_dictionary)

        // Open a deep link
        getInstrumentation().uiAutomation.executeShellCommand("am start -a android.intent.action.VIEW -d " + deepLinkUrl)
        SystemClock.sleep(500) // :'(

        // Check the results
        val activity: Activity = activityTestRule.activity
        getInstrumentation().waitForIdleSync()
        checkTitleStripOrTab(activity, R.string.tab_dictionary)
        checkFirstDefinition("a sweet quick bread baked in a cup-shaped pan")
    }


    @Test
    fun openAfterLastDictionary() {
        testSaveTab({ swipeViewPagerLeft(2) }, R.string.tab_dictionary, R.string.tab_dictionary)
    }

    @Test
    fun openAfterLastReader() {
        testSaveTab({ swipeViewPagerLeft(3) }, R.string.tab_reader, R.string.tab_reader)
    }

    @Test
    fun openAfterLastFavorites() {
        testSaveTab({ swipeViewPagerLeft(4) }, R.string.tab_favorites, R.string.tab_favorites)
    }

    private fun testSaveTab(openTabAction: Runnable, @StringRes expectedTabBeforeStop: Int, @StringRes expectedTabAfterRestart: Int) {
        val intent = Intent()
        activityTestRule.launchActivity(intent)
        openTabAction.run()
        checkTitleStripOrTab(activityTestRule.activity, expectedTabBeforeStop)
        activityTestRule.relaunch()
        checkTitleStripOrTab(activityTestRule.activity, expectedTabAfterRestart)
    }
}
