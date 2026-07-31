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

import android.content.Intent
import androidx.annotation.StringRes
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.MainActivity
import ca.rmen.android.poetassistant.main.TestAppUtils.search
import ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab
import ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem
import ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.annotation.Config

@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(AndroidJUnit4::class)
class SelectedTabTest {

    @get:Rule(order = 0)
    val hiltTestRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> =
        PoetAssistantActivityTestRule(MainActivity::class.java, false)

    @Test
    fun openAfterLastRhymer() {
        testSaveTab({}, R.string.tab_rhymer, R.string.tab_rhymer)
    }

    @Test
    fun openAfterLastThesaurus() {
        testSaveTab({ swipeViewPagerLeft(1) }, R.string.tab_thesaurus, R.string.tab_thesaurus)
    }

    @Test
    fun openAfterLastWotd() {
        testSaveTab({ openMenuItem(R.string.action_wotd_history) }, R.string.tab_wotd, R.string.tab_rhymer)
    }

    @Test
    fun openAfterLastPattern() {
        testSaveTab({ search("h*llo") }, R.string.tab_pattern, R.string.tab_rhymer)
    }

    private fun testSaveTab(openTabAction: () -> Unit, @StringRes expectedTabBeforeStop: Int, @StringRes expectedTabAfterRestart: Int) {
        val intent = Intent()
        activityTestRule.launchActivity(intent)
        openTabAction()
        checkTitleStripOrTab(activityTestRule.activity, expectedTabBeforeStop)
        activityTestRule.relaunch()
        checkTitleStripOrTab(activityTestRule.activity, expectedTabAfterRestart)
    }
}
