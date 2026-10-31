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


import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.Tts
import ca.rmen.android.poetassistant.TtsState
import ca.rmen.android.poetassistant.di.NonAndroidEntryPoint
import ca.rmen.android.poetassistant.main.TestAppUtils.clearPoem
import ca.rmen.android.poetassistant.main.TestAppUtils.speakPoem
import ca.rmen.android.poetassistant.main.TestAppUtils.typeAndSpeakPoem
import ca.rmen.android.poetassistant.main.TestAppUtils.typePoem
import ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference
import ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem
import ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule
import ca.rmen.android.poetassistant.main.settings.ui.composables.SETTINGS_VOICE_PITCH_SLIDER_TAG
import ca.rmen.android.poetassistant.main.settings.ui.composables.SETTINGS_VOICE_SELECTION_LIST_TAG
import ca.rmen.android.poetassistant.main.settings.ui.composables.SETTINGS_VOICE_SPEED_SLIDER_TAG
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.lessThan
import org.junit.Assert.assertThat
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import kotlin.time.Duration.Companion.seconds

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
    val composeTestRule = createEmptyComposeRule()

    @JvmField
    @Rule(order = 2)
    val activityTestRule: PoetAssistantActivityTestRule<MainActivity> = PoetAssistantActivityTestRule(MainActivity::class.java, true)

    val sliderEdgeMargin = 32f

    @Test
    fun aVoiceSelectionTest() {
        openMenuItem(R.string.action_settings)
        clickPreference(composeTestRule, activityTestRule.activity, R.string.pref_voice_title)
        // We don't know what voices will be available on the device.  Just select the last one.
        composeTestRule
            .onNodeWithTag(SETTINGS_VOICE_SELECTION_LIST_TAG)
            .onChildren()
            .onLast()
            .performScrollTo()
            .performClick()
        clickPreference(composeTestRule, activityTestRule.activity, R.string.pref_voice_preview_title)
        pressBack()
        swipeViewPagerLeft(3)
        typeAndSpeakPoem(composeTestRule, "Do I have an accent?")
    }


    @Test
    fun voicePitchTest() {
        openMenuItem(R.string.action_settings)
        clickPreference(composeTestRule, activityTestRule.activity, R.string.pref_voice_preview_title)
        composeTestRule
            .onNodeWithTag(SETTINGS_VOICE_PITCH_SLIDER_TAG)
            .performTouchInput { click(centerRight)}
        slideSeekbar(SETTINGS_VOICE_PITCH_SLIDER_TAG) { centerRight }
        clickPreference(composeTestRule, activityTestRule.activity, R.string.pref_voice_preview_title)
        slideSeekbar(SETTINGS_VOICE_PITCH_SLIDER_TAG) { centerLeft }
        clickPreference(composeTestRule, activityTestRule.activity, R.string.pref_voice_preview_title)
    }

    @Test
    fun voiceSpeedTest() {
        openMenuItem(R.string.action_settings)

        val scope = CoroutineScope(SupervisorJob())

        val defaultSpeechTime = timeTtsPreview(scope)

        composeTestRule
            .onNodeWithTag(SETTINGS_VOICE_SPEED_SLIDER_TAG)
            .performTouchInput {
                swipeRight(startX = centerX, endX = centerRight.x - sliderEdgeMargin)
            }
        val fastSpeechTime = timeTtsPreview(scope)
        assertThat("expected speech time to be faster after scrolling seekbar to the right",
                fastSpeechTime,
                lessThan(defaultSpeechTime))


        composeTestRule
            .onNodeWithTag(SETTINGS_VOICE_SPEED_SLIDER_TAG)
            .performTouchInput {
                swipeLeft(startX = centerRight.x - sliderEdgeMargin, endX = centerLeft.x + sliderEdgeMargin)
            }
        val slowSpeechTime = timeTtsPreview(scope)
        assertThat("expected speech time to be slower after scrolling seekbar to the left",
                slowSpeechTime,
                greaterThan(defaultSpeechTime))
    }

    @Test
    fun pauseTest() {
        swipeViewPagerLeft(3)
        val scope = CoroutineScope(SupervisorJob())
        val timeWithoutPause = timePoem("Hello. Bonjour", scope)
        pressBack()
        clearPoem(composeTestRule)
        val timeWithPause = timePoem("Hello....... Bonjour", scope)
        assertThat("expected paused poem ($timeWithPause) to be longer than non-paused poem ($timeWithoutPause)",
                timeWithPause - timeWithoutPause,
                greaterThan(2000L))
    }

    private fun getTts(): Tts {
        lateinit var tts: Tts
        getInstrumentation().runOnMainSync {
            tts = EntryPointAccessors.fromApplication(activityTestRule.activity, NonAndroidEntryPoint::class.java).tts()
        }
        return tts
    }

    @OptIn(FlowPreview::class)
    private fun timePoem(poem: String, scope: CoroutineScope): Long {
        var timeUtteranceCompleted: Long = 0
        typePoem(composeTestRule, poem)
        val before = System.currentTimeMillis()

        val tts = getTts()
        val job = scope.launch {
            tts.ttsFlow
                .filter { it?.currentStatus == TtsState.TtsStatus.UTTERANCE_COMPLETE  }
                .timeout(30.seconds)
                .first()
            timeUtteranceCompleted = System.currentTimeMillis()
        }
        speakPoem(composeTestRule)
        Espresso.onIdle()
        composeTestRule.waitForIdle()
        job.cancel()
        val poemSpeechTime = timeUtteranceCompleted - before
        return poemSpeechTime
    }

    @OptIn(FlowPreview::class)
    private fun timeTtsPreview(scope: CoroutineScope): Long {
        val before = System.currentTimeMillis()
        var timeUtteranceCompleted: Long = 0
        val tts = getTts()
        val job = scope.launch {
            tts.ttsFlow.filter { it?.currentStatus == TtsState.TtsStatus.UTTERANCE_COMPLETE  }
                .timeout(30.seconds)
                .first()
            timeUtteranceCompleted = System.currentTimeMillis()
        }
        clickPreference(composeTestRule, activityTestRule.activity, R.string.pref_voice_preview_title)
        Espresso.onIdle()
        composeTestRule.waitForIdle()
        job.cancel()
        val defaultSpeechTime = timeUtteranceCompleted - before
        return defaultSpeechTime
    }

    private fun slideSeekbar(testTag: String, position: TouchInjectionScope.() -> Offset) {
        composeTestRule
            .onNodeWithTag(testTag)
            .performTouchInput { click(position()) }
    }
}
