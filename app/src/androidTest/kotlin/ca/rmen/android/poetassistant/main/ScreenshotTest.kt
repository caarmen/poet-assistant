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
package ca.rmen.android.poetassistant.main

import android.Manifest
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.screenshot.BasicScreenCaptureProcessor
import androidx.test.runner.screenshot.ScreenCaptureProcessor
import androidx.test.runner.screenshot.Screenshot
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.Theme.setThemeFromSettings
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.io.IOException


@LargeTest
@RunWith(TestParameterInjector::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ScreenshotTest {
    enum class ThemePreference(val value: String) {
        LIGHT(SettingsPrefs.THEME_LIGHT),
        DARK(SettingsPrefs.THEME_DARK)
    }

    @TestParameter
    lateinit var themePreference: ThemePreference

    @JvmField
    @Rule
    var activityTestRule: PoetAssistantActivityTestRule<MainActivity> = PoetAssistantActivityTestRule(
        MainActivity::class.java, true
    )

    @JvmField
    @Rule
    var writeScreenshotRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Before
    fun setupTheme() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val settingsPrefs =
                SettingsPrefs(ApplicationProvider.getApplicationContext())
            settingsPrefs.theme = themePreference.value
            setThemeFromSettings(settingsPrefs)
        }
    }

    @Test
    fun testTakeScreenshots() {
        starWords(
            "acquiesce",
            "askance",
            "benight",
            "deferential",
            "fractious",
            "implacable",
            "obfuscation",
            "peon",
            "possibleness"
        )
        TestAppUtils.search("chance")
        takeScreenshot("rhymer")
        TestUiUtils.swipeViewPagerLeft(1)
        takeScreenshot("thesaurus")
        TestUiUtils.swipeViewPagerLeft(1)
        takeScreenshot("dictionary")
        TestUiUtils.swipeViewPagerLeft(1)
        TestAppUtils.typePoem("Roses are red.\nViolets are blue.\nIf you are a poet,\nthis app is for you.")
        SystemClock.sleep(1000)
        takeScreenshot("composer")
        TestUiUtils.swipeViewPagerLeft(1)
        takeScreenshot("favorites")
        TestUiUtils.openMenuItem(R.string.action_settings)
        takeScreenshot("settings")
    }

    private fun starWords(vararg words: String) {
        for (word in words) {
            TestAppUtils.search(word)
            Espresso.onIdle()
            TestAppUtils.starQueryWord()
        }
    }

    // https://stackoverflow.com/questions/38519568/how-to-take-screenshot-at-the-point-where-test-fail-in-espresso
    private fun takeScreenshot(filename: String) {
        SystemClock.sleep(500) // :(
        Espresso.onIdle()
        val capture = Screenshot.capture()
        capture.setName(filename)
        capture.setFormat(Bitmap.CompressFormat.PNG)

        val processors = HashSet<ScreenCaptureProcessor>()
        processors.add(BasicScreenCaptureProcessor())

        try {
            capture.process(processors)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
