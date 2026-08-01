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


import android.annotation.TargetApi
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers.allOf
import ca.rmen.android.poetassistant.main.TestAppUtils.typeAndSpeakPoem
import ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem
import ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft

@LargeTest
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PoemSaveTest {

    @JvmField
    @Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> = object : PoetAssistantActivityTestRule<MainActivity>(MainActivity::class.java, true) {
        override fun beforeActivityLaunched() {
            super.beforeActivityLaunched()
            fakeExistingPoemFilePref()
        }
    }

    @Test
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun saveTest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return
        swipeViewPagerLeft(3)
        val poemFile = getPoemFile()
        assertFalse(poemFile.exists())
        typeAndSpeakPoem("Let's save a file")
        openMenuItem(R.string.file)
        onView(allOf(withId(R.id.title), withText(R.string.file_save), isDisplayed())).perform(click())
        assertTrue(poemFile.exists())
    }

    private fun fakeExistingPoemFilePref() {
        val context: Context = getInstrumentation().targetContext
        val poemFile = getPoemFile()

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit()
                .putString("poem_uri", Uri.fromFile(poemFile).toString())
                .putString("poem_name", "testpoem")
                .apply()
    }

    private fun getPoemFile(): File {
        val context: Context = getInstrumentation().targetContext
        return File(context.filesDir, "testpoem.txt")
    }
}
