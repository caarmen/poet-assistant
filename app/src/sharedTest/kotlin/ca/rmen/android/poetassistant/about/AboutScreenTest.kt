/*
 * Copyright (c) 2017 - present Carmen Alvarez
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

package ca.rmen.android.poetassistant.about

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.MainActivity
import ca.rmen.android.poetassistant.main.TestUiUtils
import ca.rmen.android.poetassistant.main.rules.RetryTestRule
import ca.rmen.android.poetassistant.rules.PoetAssistantComposeTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@HiltAndroidTest
@Config(
    application = HiltTestApplication::class,
    qualifiers = "w600dp-h800dp",
)
@RunWith(AndroidJUnit4::class)
class AboutScreenTest {
    @get:Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    // This is the first test in the suite. Sometimes there's some timing issues for the first
    // test regarding compose + espresso:
    // androidx.compose.ui.test.junit4.android.ComposeNotIdleException: Idling resource timed out: possibly due to compose being busy.
    // IdlingResourceRegistry has the following idling resources registered:
    // - [busy] ComposeIdlingResource is busy due to pending measure/layout.
    val retry = RetryTestRule()

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 3)
    val poetAssistantComposeTestRule = PoetAssistantComposeTestRule(composeTestRule)

    @Test
    fun openAboutScreenTest() {
        composeTestRule.waitForIdle()
        TestUiUtils.openMenuItem(composeTestRule, R.string.action_about)
        checkLicense(ABOUT_LICENSE_APP_ROW_TAG, R.string.about_license_app, "GNU GENERAL")
        checkLicense(ABOUT_LICENSE_RHYMER_ROW_TAG, R.string.about_license_rhyming_dictionary, "Carnegie Mellon University")
        checkLicense(ABOUT_LICENSE_THESAURUS_ROW_TAG, R.string.about_license_thesaurus, "WordNet Release 2.1")
        checkLicense(ABOUT_LICENSE_DICTIONARY_ROW_TAG, R.string.about_license_dictionary, "WordNet 3.0")
        checkLicense(
            ABOUT_LICENSE_GOOGLE_NGRAM_ROW_TAG,
            R.string.about_license_google_ngram_dataset,
            "Google Ngram Viewer"
        )
        composeTestRule.onNode(hasScrollAction())
            .performScrollToNode(hasTestTag(ABOUT_SOURCE_CODE_ROW_TAG))
        composeTestRule.onNodeWithTag(ABOUT_SOURCE_CODE_ROW_TAG)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.about_projectUrl))
            .assertIsDisplayed()
    }

    private fun checkLicense(rowTag: String, @StringRes linkTitle: Int, licenseContent: String) {
        composeTestRule.onNode(hasScrollAction())
            .performScrollToNode(hasTestTag(rowTag))
        composeTestRule.onNodeWithTag(rowTag)
            .assertIsDisplayed()
            .assertTextContains(composeTestRule.activity.getString(linkTitle))
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(text = licenseContent, substring = true).assertIsDisplayed()
        Espresso.pressBack()
    }

}
