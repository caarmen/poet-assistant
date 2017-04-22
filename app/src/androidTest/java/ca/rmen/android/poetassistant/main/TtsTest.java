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


import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.junit.Test;
import org.junit.runner.RunWith;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Tts;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class TtsTest extends BaseTest {


    public static class EventBusReceiver {
        long timeUtteranceCompleted;

        @Subscribe
        public void onUtteranceCompleted(Tts.OnUtteranceCompleted event) {
            timeUtteranceCompleted = System.currentTimeMillis();
        }
    }

    @Test
    public void voiceSpeedTest() {
        openMenuItem(R.string.action_settings);

        long defaultSpeechTime = timeTtsPreview();
        //http://stackoverflow.com/questions/23659367/espresso-set-seekbar
        onView(allOf(withId(R.id.seekbar), withParent(withParent(hasDescendant(withText(R.string.pref_voice_speed_title))))))
                .perform(new GeneralClickAction(Tap.SINGLE, GeneralLocation.CENTER_RIGHT, Press.FINGER));

        long fastSpeechTime = timeTtsPreview();
        assertThat("expected speech time to be faster after scrolling seekbar to the right",
                fastSpeechTime,
                lessThan(defaultSpeechTime));

        onView(allOf(withId(R.id.seekbar), withParent(withParent(hasDescendant(withText(R.string.pref_voice_speed_title))))))
                .perform(new GeneralClickAction(Tap.SINGLE, GeneralLocation.CENTER_LEFT, Press.FINGER));

        long slowSpeechTime = timeTtsPreview();
        assertThat("expected speech time to be slower after scrolling seekbar to the left",
                slowSpeechTime,
                greaterThan(defaultSpeechTime));
    }

    private long timeTtsPreview() {
        EventBusReceiver receiver = new EventBusReceiver();
        EventBus.getDefault().register(receiver);
        long before = System.currentTimeMillis();
        clickPreference(R.string.pref_voice_preview_title);
        long defaultSpeechTime = receiver.timeUtteranceCompleted - before;
        EventBus.getDefault().unregister(receiver);
        return defaultSpeechTime;
    }

}
