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


import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantIntentsTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.TestAppUtils.search;
import static ca.rmen.android.poetassistant.main.TestAppUtils.typePoem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ShareTest {

    @Rule
    public PoetAssistantIntentsTestRule<MainActivity> mActivityTestRule = new PoetAssistantIntentsTestRule<>(MainActivity.class, true);

    @Test
    public void sharePoemTest() {
        swipeViewPagerLeft(3);
        String poemText = "Let's share a poem";
        typePoem(poemText);
        openMenuItem(R.string.share_poem_text);
        intended(allOf(hasAction(Intent.ACTION_CHOOSER),
                hasExtra(is(Intent.EXTRA_INTENT),
                        allOf(hasAction(Intent.ACTION_SEND),
                                hasExtra(Intent.EXTRA_TEXT, poemText)))));
    }

    @Test
    public void shareRhymesTest() {
        search("merge");
        openMenuItem(R.string.share);
        verifyShareIntentContains("upsurge");
    }

    @Test
    public void shareThesaurusTest() {
        search("splurge");
        swipeViewPagerLeft(1);
        openMenuItem(R.string.share);
        verifyShareIntentContains("flaunt");
    }

    @Test
    public void shareDictionaryTest() {
        search("a");
        swipeViewPagerLeft(2);
        openMenuItem(R.string.share);
        verifyShareIntentContains("the blood group whose red cells carry the A antigen");
    }

    @Test
    public void shareFavoritesTest() {
        search("happy");
        onView(allOf(withId(R.id.btn_star_result), isDisplayed(), hasSibling(withText("snappy")))).perform(click());
        onView(allOf(withId(R.id.btn_star_result), isDisplayed(), hasSibling(withText("crappy")))).perform(click());
        swipeViewPagerLeft(4);
        openMenuItem(R.string.share);
        verifyShareIntentContains("snappy");
    }

    @Test
    public void shareRandomWordTest() {
        openMenuItem(R.string.action_random_word);
        SystemClock.sleep(500);
        openMenuItem(R.string.share);
        verifyShareIntentContains("Definitions of");
    }

    @Test
    public void sharePatternTest() {
        search("ho?t");
        openMenuItem(R.string.share);
        verifyShareIntentContains("host");
    }

    @Test
    public void shareWotdTest() {
        Context context = mActivityTestRule.getActivity();
        openMenuItem(R.string.action_wotd_history);
        openMenuItem(R.string.share);
        verifyShareIntentContains(context.getString(R.string.share_wotd_title));
    }

    private void verifyShareIntentContains(String expectedText) {
        intended(allOf(hasAction(Intent.ACTION_CHOOSER),
                hasExtra(is(Intent.EXTRA_INTENT),
                        allOf(hasAction(Intent.ACTION_SEND),
                                hasExtra(containsString(Intent.EXTRA_TEXT),
                                        containsString(expectedText))))));
    }
}
