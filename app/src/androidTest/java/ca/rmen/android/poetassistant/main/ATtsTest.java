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

package ca.rmen.android.poetassistant.main;


import android.annotation.TargetApi;
import android.os.Build;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.Observer;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.TtsState;
import ca.rmen.android.poetassistant.dagger.DaggerHelper;
import ca.rmen.android.poetassistant.dagger.TestAppComponent;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;
import ca.rmen.android.poetassistant.main.rules.RetryTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static ca.rmen.android.poetassistant.main.CustomViewActions.clickLastChild;
import static ca.rmen.android.poetassistant.main.CustomViewActions.scrollToEnd;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clearPoem;
import static ca.rmen.android.poetassistant.main.TestAppUtils.speakPoem;
import static ca.rmen.android.poetassistant.main.TestAppUtils.typeAndSpeakPoem;
import static ca.rmen.android.poetassistant.main.TestAppUtils.typePoem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

@LargeTest
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ATtsTest {

    @Rule
    public RetryTestRule retry = new RetryTestRule();

    @Rule
    public PoetAssistantActivityTestRule<MainActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(MainActivity.class, true);

    public static class TtsObserver implements Observer<TtsState> {
        long timeUtteranceCompleted;

        @Override
        public void onChanged(@Nullable TtsState ttsState) {
            timeUtteranceCompleted = System.currentTimeMillis();
        }
    }


    @Test
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void aVoiceSelectionTest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        openMenuItem(R.string.action_settings);
        clickPreference(R.string.pref_voice_title);
        // We don't know what voices will be available on the device.  Just select the last one.
        onView(withClassName(endsWith("RecycleListView")))
                .perform(scrollToEnd(), clickLastChild());
        clickPreference(R.string.pref_voice_preview_title);
        pressBack();
        swipeViewPagerLeft(3);
        typeAndSpeakPoem("Do I have an accent?");
    }


    @Test
    public void voicePitchTest() {
        openMenuItem(R.string.action_settings);
        clickPreference(R.string.pref_voice_preview_title);
        slideSeekbar(R.string.pref_voice_pitch_title, GeneralLocation.CENTER_RIGHT);
        clickPreference(R.string.pref_voice_preview_title);
        slideSeekbar(R.string.pref_voice_pitch_title, GeneralLocation.CENTER_LEFT);
        clickPreference(R.string.pref_voice_preview_title);
    }

    @Test
    public void voiceSpeedTest() {
        openMenuItem(R.string.action_settings);

        long defaultSpeechTime = timeTtsPreview();
        slideSeekbar(R.string.pref_voice_speed_title, GeneralLocation.CENTER_RIGHT);

        long fastSpeechTime = timeTtsPreview();
        assertThat("expected speech time to be faster after scrolling seekbar to the right",
                fastSpeechTime,
                lessThan(defaultSpeechTime));

        slideSeekbar(R.string.pref_voice_speed_title, GeneralLocation.CENTER_LEFT);

        long slowSpeechTime = timeTtsPreview();
        assertThat("expected speech time to be slower after scrolling seekbar to the left",
                slowSpeechTime,
                greaterThan(defaultSpeechTime));
    }

    @Test
    public void pauseTest() {
        swipeViewPagerLeft(3);
        long timeWithoutPause = timePoem("Hello. Bonjour");
        pressBack();
        clearPoem();
        long timeWithPause = timePoem("Hello....... Bonjour");
        assertThat("expected paused poem to be longer than non-paused poem",
                timeWithPause - timeWithoutPause,
                greaterThan(2000L));
    }

    private Tts getTts() {
        return ((TestAppComponent) DaggerHelper.INSTANCE.getAppComponent(mActivityTestRule.getActivity())).getTts();
    }
    private long timePoem(String poem) {
        TtsObserver receiver = new TtsObserver();
        getInstrumentation().runOnMainSync(() -> {
            getTts().getTtsLiveData().removeObserver(receiver);
            getTts().getTtsLiveData().observeForever(receiver);
        });
        typePoem(poem);
        long before = System.currentTimeMillis();
        speakPoem();
        long poemSpeechTime = receiver.timeUtteranceCompleted - before;
        getInstrumentation().runOnMainSync(() -> getTts().getTtsLiveData().removeObserver(receiver));
        return poemSpeechTime;
    }

    private long timeTtsPreview() {
        TtsObserver receiver = new TtsObserver();
        getInstrumentation().runOnMainSync(() -> getTts().getTtsLiveData().observeForever(receiver));
        long before = System.currentTimeMillis();
        clickPreference(R.string.pref_voice_preview_title);
        long defaultSpeechTime = receiver.timeUtteranceCompleted - before;
        getInstrumentation().runOnMainSync(() -> getTts().getTtsLiveData().removeObserver(receiver));
        return defaultSpeechTime;
    }

    private void slideSeekbar(@StringRes int prefTitleId, GeneralLocation location) {
        //http://stackoverflow.com/questions/23659367/espresso-set-seekbar
        onView(allOf(withId(R.id.seekbar), withParent(withParent(hasDescendant(withText(prefTitleId))))))
                .perform(new GeneralClickAction(Tap.SINGLE, location, Press.FINGER, 0, 0));
    }

}
