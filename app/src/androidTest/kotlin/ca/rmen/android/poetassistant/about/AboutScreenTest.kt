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

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.MainActivity
import ca.rmen.android.poetassistant.main.TestUiUtils
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutScreenTest {
    @JvmField
    @Rule
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> = PoetAssistantActivityTestRule(
        MainActivity::class.java, true
    )

    @Test
    fun openAboutScreenTest() {
        TestUiUtils.openMenuItem(R.string.action_about)
        checkLicense(R.id.tv_poet_assistant_license, R.string.about_license_app, "GNU GENERAL")
        checkLicense(R.id.tv_rhymer_license, R.string.about_license_rhyming_dictionary, "Carnegie Mellon University")
        checkLicense(R.id.tv_thesaurus_license, R.string.about_license_thesaurus, "WordNet Release 2.1")
        checkLicense(R.id.tv_dictionary_license, R.string.about_license_dictionary, "WordNet 3.0")
        checkLicense(
            R.id.tv_google_ngram_dataset_license,
            R.string.about_license_google_ngram_dataset,
            "Google Ngram Viewer"
        )
        Espresso.onView(withId(R.id.tv_source_code))
            .check(ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()))
            .check(ViewAssertions.matches(withText(R.string.about_projectUrl)))
    }

    private fun checkLicense(@IdRes linkResId: Int, @StringRes linkTitle: Int, licenseContent: String) {
        Espresso.onView(ViewMatchers.withId(linkResId))
            .check(ViewAssertions.matches(ViewMatchers.withText(linkTitle)))
            .perform(ViewActions.scrollTo())
            .check(ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()))
            .perform(ViewActions.click())

        Espresso.onView(withId(R.id.tv_license_text))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.withText(Matchers.containsString(licenseContent))))
        Espresso.pressBack()
    }

}