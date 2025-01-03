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


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static ca.rmen.android.poetassistant.main.TestAppUtils.addFilter;
import static ca.rmen.android.poetassistant.main.TestAppUtils.search;
import static ca.rmen.android.poetassistant.main.TestAppUtils.typeAndSpeakPoem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantIntentsTestRule;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ShareTest {

    @Rule
    public PoetAssistantIntentsTestRule<MainActivity> mActivityTestRule = new PoetAssistantIntentsTestRule<>(MainActivity.class);

    @Test
    public void sharePoemTest() {
        swipeViewPagerLeft(3);
        String poemText = "Let us share a poem";
        typeAndSpeakPoem(poemText);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            openMenuItem(R.string.share_poem_text);
        } else {
            openMenuItem(R.string.share);
        }
        checkShareIntentEquals(poemText);
    }

    @Test
    public void shareThesaurusTest() {
        search("splurge");
        swipeViewPagerLeft(1);
        openMenuItem(R.string.share);
        checkShareIntentContains("flaunt");
    }

    @Test
    public void shareFilteredThesaurusTest() {
        Context context = mActivityTestRule.getActivity();
        search("happy");
        swipeViewPagerLeft(1);
        addFilter(Tab.THESAURUS, "messed", "blessed");
        openMenuItem(R.string.share);
        String expectedContent = context.getString(R.string.share_thesaurus_title_with_filter, "happy", "messed");
        checkShareIntentContains(expectedContent);
    }

    @Test
    public void shareFavoritesTest() {
        search("happy");
        onView(allOf(withId(R.id.btn_star_result), isDisplayed(), hasSibling(withText("snappy")))).perform(click());
        onView(allOf(withId(R.id.btn_star_result), isDisplayed(), hasSibling(withText("crappy")))).perform(click());
        swipeViewPagerLeft(4);
        openMenuItem(R.string.share);
        checkShareIntentContains("snappy");
    }

    // Need to look at this: sometimes the app bar layout is hidden :(
    @Ignore
    @Test
    public void shareRandomWordTest() {
        openMenuItem(R.string.action_random_word);
        openMenuItem(R.string.share);
        checkShareIntentContains("Definitions of");
    }

    private void checkShareIntentContains(String expectedText) {
        intended(allOf(hasAction(Intent.ACTION_CHOOSER),
                hasExtra(is(Intent.EXTRA_INTENT),
                        allOf(hasAction(Intent.ACTION_SEND),
                                hasExtra(containsString(Intent.EXTRA_TEXT),
                                        containsString(expectedText))))));
    }

    private void checkShareIntentEquals(String expectedText) {
        intended(allOf(hasAction(Intent.ACTION_CHOOSER),
                hasExtra(is(Intent.EXTRA_INTENT),
                        allOf(hasAction(Intent.ACTION_SEND),
                                hasExtra(Intent.EXTRA_TEXT, expectedText)))));
    }
}
