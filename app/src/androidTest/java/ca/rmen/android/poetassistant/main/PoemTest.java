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
import android.os.SystemClock;
import android.support.test.espresso.NoMatchingRootException;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.ActionBarContextView;

import junit.framework.AssertionFailedError;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Locale;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.reader.WordCounter;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.CustomViewActions.longTap;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clearPoem;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clickDialogPositiveButton;
import static ca.rmen.android.poetassistant.main.TestAppUtils.typeAndSpeakPoem;
import static ca.rmen.android.poetassistant.main.TestAppUtils.typePoem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab;
import static ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PoemTest {

    @Rule
    public PoetAssistantActivityTestRule<MainActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(MainActivity.class, true);

    @Test
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void exportAudioTest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
        swipeViewPagerLeft(3);
        typeAndSpeakPoem("Will export some text");
        File exportDir = new File(mActivityTestRule.getActivity().getFilesDir(), "export");
        File poemFile = new File(exportDir, "poem.wav");
        assertFalse(poemFile.exists());
        openMenuItem(R.string.share_poem_audio);
        assertTrue(poemFile.exists());
        long length1 = poemFile.length();
        long fileDate1 = poemFile.lastModified();

        // Try another one

        onView(allOf(withId(R.id.tv_text), isDisplayed())).perform(clearText());
        SystemClock.sleep(250);
        typeAndSpeakPoem("Will export some text which is a bit longer");
        openMenuItem(R.string.share_poem_audio);
        assertTrue(poemFile.exists());
        long length2 = poemFile.length();
        long fileDate2 = poemFile.lastModified();
        assertThat("Expected second file to be newer than first file", fileDate1, lessThan(fileDate2));
        assertThat("Expected second file to be larger than first file", length1, lessThan(length2));
    }

    @Test
    public void lookupFromPoemTest() {
        swipeViewPagerLeft(3);
        String poemText = "Here is a poem";
        typeAndSpeakPoem(poemText);

        // Look up in the rhymer
        // Long press on the left part of the EditText, to select the first word
        String firstWord = poemText.substring(0, poemText.indexOf(' ')).toLowerCase(Locale.getDefault());
        onView(withId(R.id.tv_text)).perform(longTap(1, 0));

        // Select the "rhymer" popup
        clickPopupView("rhymer");
        checkTitleStripOrTab(mActivityTestRule.getActivity(), R.string.tab_rhymer);
        onView(allOf(withId(R.id.tv_list_header), isDisplayed())).check(matches(withText(firstWord)));

        // Look up in the thesaurus
        swipeViewPagerLeft(3);
        onView(withId(R.id.tv_text)).perform(longTap(1, 0));
        clickPopupView("thesaurus");
        onView(allOf(withId(R.id.tv_list_header), isDisplayed())).check(matches(withText(firstWord)));

        // Look up in the dictionary
        swipeViewPagerLeft(2);
        onView(withId(R.id.tv_text)).perform(longTap(1, 0));
        clickPopupView("dictionary");
        onView(allOf(withId(R.id.tv_list_header), isDisplayed())).check(matches(withText(firstWord)));
    }

    @Test
    public void testLookupSetting() {
        openMenuItem(R.string.action_settings);
        clickPreference(R.string.pref_selection_lookup_title);
        pressBack();
        swipeViewPagerLeft(3);
        String poemText = "Here is a poem with lookup disabled";
        typePoem(poemText);
        pressBack();

        assertPopupMissing("rhymer");
        assertPopupMissing("thesaurus");
        assertPopupMissing("dictionary");
        pressBack();
        closeSoftKeyboard();

        openMenuItem(R.string.action_settings);
        clickPreference(R.string.pref_selection_lookup_title);
        pressBack();
        swipeViewPagerLeft(3);
        assertPopupVisible("rhymer");
        assertPopupVisible("thesaurus");
        assertPopupVisible("dictionary");
    }

    @Test
    public void testWordCount() {
        swipeViewPagerLeft(3);
        onView(withId(R.id.reader_word_count)).check(matches(not(isDisplayed())));
        String poemText = "Here is some text";
        typePoem(poemText);
        onView(withId(R.id.reader_word_count))
                .check(matches(isDisplayed()))
                .check(matches(withText(WordCounter.INSTANCE.getWordCountText(mActivityTestRule.getActivity(), poemText))))
                .perform(click());
        onView(withText(R.string.word_count_help_title))
                .check(matches(isDisplayed()));
        clickDialogPositiveButton(android.R.string.ok);
        checkTitleStripOrTab(mActivityTestRule.getActivity(), R.string.tab_reader);

        clearPoem();
        onView(withId(R.id.reader_word_count)).check(matches(not(isDisplayed())));
    }

    private void assertPopupVisible(String label) {
        onView(withId(R.id.tv_text)).perform(longTap(1, 0));
        getPopupView(label).check(matches(isDisplayed()));
        pressBack();
    }

    private void assertPopupMissing(String label) {
        onView(withId(R.id.tv_text)).perform(longTap(1, 0));
        boolean exceptionThrown = false;
        try {
            getPopupView(label);
        } catch (NoMatchingViewException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        pressBack();
    }

    private ViewInteraction getPopupView(String label) {
        SystemClock.sleep(220);
        try {
            ViewInteraction result = onView(withText(equalToIgnoringCase(label))).inRoot(isPlatformPopup());
            result.check(matches(isDisplayed()));
            return result;
        } catch (PerformException | NoMatchingRootException | NoMatchingViewException | AssertionFailedError e) {
            // I haven't yet found a better way to handle this :(
            // On smaller screens the items are hidden behind an overflow item with id "overflow" which is inaccessible
            try {
                onView(withContentDescription("More options")).inRoot(isPlatformPopup()).perform(click());
                ViewInteraction result = onView(withText(equalToIgnoringCase(label))).inRoot(isPlatformPopup());
                result.check(matches(isDisplayed()));
                return result;
            } catch (NoMatchingRootException e1) {
                onView(allOf(
                        withContentDescription("More options"),
                        isDescendantOfA(withClassName(is(ActionBarContextView.class.getName())))))
                        .perform(click());
                return onView(withText(equalToIgnoringCase(label)));
            }
        }

    }

    private void clickPopupView(String label) {
        getPopupView(label).perform(click());
    }

}
