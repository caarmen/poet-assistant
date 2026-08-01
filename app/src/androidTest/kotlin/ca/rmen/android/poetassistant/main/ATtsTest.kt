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
import android.os.Build
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.Tts
import ca.rmen.android.poetassistant.TtsState
import ca.rmen.android.poetassistant.di.NonAndroidEntryPoint
import ca.rmen.android.poetassistant.main.CustomViewActions.clickLastChild
import ca.rmen.android.poetassistant.main.CustomViewActions.scrollToEnd
import ca.rmen.android.poetassistant.main.TestAppUtils.clearPoem
import ca.rmen.android.poetassistant.main.TestAppUtils.speakPoem
import ca.rmen.android.poetassistant.main.TestAppUtils.typeAndSpeakPoem
import ca.rmen.android.poetassistant.main.TestAppUtils.typePoem
import ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference
import ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem
import ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import ca.rmen.android.poetassistant.main.rules.RetryTestRule
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.lessThan
import org.junit.Assert.assertThat
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@LargeTest
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ATtsTest {

    @JvmField
    @Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val retry = RetryTestRule()

    @JvmField
    @Rule(order = 2)
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> = PoetAssistantActivityTestRule(MainActivity::class.java, true)

    private class TtsObserver : Observer<TtsState> {
        var timeUtteranceCompleted: Long = 0

        override fun onChanged(ttsState: TtsState) {
            timeUtteranceCompleted = System.currentTimeMillis()
        }
    }

    @Test
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun aVoiceSelectionTest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
        openMenuItem(R.string.action_settings)
        clickPreference(R.string.pref_voice_title)
        // We don't know what voices will be available on the device.  Just select the last one.
        onView(withClassName(endsWith("RecycleListView")))
                .perform(scrollToEnd(), clickLastChild())
        clickPreference(R.string.pref_voice_preview_title)
        pressBack()
        swipeViewPagerLeft(3)
        typeAndSpeakPoem("Do I have an accent?")
    }


    @Test
    fun voicePitchTest() {
        openMenuItem(R.string.action_settings)
        clickPreference(R.string.pref_voice_preview_title)
        slideSeekbar(R.string.pref_voice_pitch_title, GeneralLocation.CENTER_RIGHT)
        clickPreference(R.string.pref_voice_preview_title)
        slideSeekbar(R.string.pref_voice_pitch_title, GeneralLocation.CENTER_LEFT)
        clickPreference(R.string.pref_voice_preview_title)
    }

    @Test
    fun voiceSpeedTest() {
        openMenuItem(R.string.action_settings)

        val defaultSpeechTime = timeTtsPreview()
        slideSeekbar(R.string.pref_voice_speed_title, GeneralLocation.CENTER_RIGHT)

        val fastSpeechTime = timeTtsPreview()
        assertThat("expected speech time to be faster after scrolling seekbar to the right",
                fastSpeechTime,
                lessThan(defaultSpeechTime))

        slideSeekbar(R.string.pref_voice_speed_title, GeneralLocation.CENTER_LEFT)

        val slowSpeechTime = timeTtsPreview()
        assertThat("expected speech time to be slower after scrolling seekbar to the left",
                slowSpeechTime,
                greaterThan(defaultSpeechTime))
    }

    @Test
    fun pauseTest() {
        swipeViewPagerLeft(3)
        val timeWithoutPause = timePoem("Hello. Bonjour")
        pressBack()
        clearPoem()
        val timeWithPause = timePoem("Hello....... Bonjour")
        assertThat("expected paused poem to be longer than non-paused poem",
                timeWithPause - timeWithoutPause,
                greaterThan(2000L))
    }

    private fun getTts(): Tts {
        return EntryPointAccessors.fromApplication(activityTestRule.activity, NonAndroidEntryPoint::class.java).tts()
    }

    private fun timePoem(poem: String): Long {
        val receiver = TtsObserver()
        getInstrumentation().runOnMainSync {
            getTts().getTtsLiveData().removeObserver(receiver)
            getTts().getTtsLiveData().observeForever(receiver)
        }
        typePoem(poem)
        val before = System.currentTimeMillis()
        speakPoem()
        val poemSpeechTime = receiver.timeUtteranceCompleted - before
        getInstrumentation().runOnMainSync { getTts().getTtsLiveData().removeObserver(receiver) }
        return poemSpeechTime
    }

    private fun timeTtsPreview(): Long {
        val receiver = TtsObserver()
        getInstrumentation().runOnMainSync { getTts().getTtsLiveData().observeForever(receiver) }
        val before = System.currentTimeMillis()
        clickPreference(R.string.pref_voice_preview_title)
        val defaultSpeechTime = receiver.timeUtteranceCompleted - before
        getInstrumentation().runOnMainSync { getTts().getTtsLiveData().removeObserver(receiver) }
        return defaultSpeechTime
    }

    private fun slideSeekbar(@StringRes prefTitleId: Int, location: GeneralLocation) {
        //http://stackoverflow.com/questions/23659367/espresso-set-seekbar
        onView(allOf(withId(R.id.seekbar), withParent(withParent(hasDescendant(withText(prefTitleId))))))
                .perform(GeneralClickAction(Tap.SINGLE, location, Press.FINGER, 0, 0))
    }
}
