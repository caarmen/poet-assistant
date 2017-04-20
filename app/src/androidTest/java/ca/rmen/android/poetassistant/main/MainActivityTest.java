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
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.UserDb;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.Espresso.registerIdlingResources;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.CustomViewActions.longTap;
import static ca.rmen.android.poetassistant.main.CustomViewMatchers.childAtPosition;
import static ca.rmen.android.poetassistant.main.TestAppUtils.checkPatterns;
import static ca.rmen.android.poetassistant.main.TestAppUtils.checkRhymes;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clearPoem;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clearSearchHistory;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clearStarredWords;
import static ca.rmen.android.poetassistant.main.TestAppUtils.filter;
import static ca.rmen.android.poetassistant.main.TestAppUtils.openDictionary;
import static ca.rmen.android.poetassistant.main.TestAppUtils.openThesaurus;
import static ca.rmen.android.poetassistant.main.TestAppUtils.search;
import static ca.rmen.android.poetassistant.main.TestAppUtils.starQueryWord;
import static ca.rmen.android.poetassistant.main.TestAppUtils.typePoem;
import static ca.rmen.android.poetassistant.main.TestAppUtils.verifyAllStarredWords;
import static ca.rmen.android.poetassistant.main.TestAppUtils.verifyStarredInList;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenu;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerRight;
import static ca.rmen.android.poetassistant.main.TestUiUtils.verifyTitleStripCenterTitle;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

/**
 * Tested on:
 * - emulator Nexus_5_API_25
 * - emulator 280x380
 * - Huawei P9 Lite
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private UiDevice mDevice;
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            super.beforeActivityLaunched();
            registerIdlingResources(new TtsIdlingResource(getInstrumentation().getTargetContext()));
            mDevice = UiDevice.getInstance(getInstrumentation());

            cleanup();
        }

        @Override
        protected void afterActivityFinished() {
            cleanup();
            List<IdlingResource> idlingResourceList = Espresso.getIdlingResources();
            if (idlingResourceList != null) {
                for (int i = 0; i < idlingResourceList.size(); i++) {
                    Espresso.unregisterIdlingResources(idlingResourceList.get(i));
                }
            }
            super.afterActivityFinished();
        }
    };

    private void cleanup() {
        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase db = new UserDb(context).getWritableDatabase();
        db.delete("SUGGESTION", null, null);
        db.delete("FAVORITE", null, null);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().clear().apply();
        File filesDir = context.getFilesDir();
        if (filesDir.exists()) {
            deleteFiles(filesDir);
        }
    }

    private void deleteFiles(File folder) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) deleteFiles(file);
            else assertTrue("couldn't delete file " + file, file.delete());
        }
    }

    @Test
    public void integrationTest1() {
        Context context = mActivityTestRule.getActivity();
        swipeViewPagerLeft(4);
        verifyAllStarredWords(context);
        swipeViewPagerRight(4);
        search("howdy");
        checkRhymes(context, "cloudy", "dowdy");
        openThesaurus(context, "cloudy", "nebulose");
        openDictionary(context, "nebulous", "lacking definite form or limits");
        starQueryWord();
        swipeViewPagerLeft(2);
        verifyAllStarredWords(context, "nebulous");
        swipeViewPagerRight(3);
        verifyStarredInList("nebulous");
        filter("bloody", "muddy", "nebulose");
        swipeViewPagerRight(1);
        filter("bully", "rowdy", "cloudy");
        swipeViewPagerLeft(3);
        typePoem("To be or not to be, that is the question");
        clearPoem();
        // clearing the search history doesn't erase starred words
        clearSearchHistory();
        swipeViewPagerLeft(1);
        verifyAllStarredWords(context, "nebulous");
        clearStarredWords();
        verifyAllStarredWords(context);
    }

    @Test
    public void integrationTest2() {
        Context context = mActivityTestRule.getActivity();
        swipeViewPagerLeft(4);
        verifyAllStarredWords(context);
        swipeViewPagerRight(4);
        search("beholden");
        checkRhymes(context, "embolden", "golden");
        openThesaurus(context, "embolden", "hearten");
        openDictionary(context, "recreate", "create anew");
        starQueryWord();
        swipeViewPagerLeft(2);
        verifyAllStarredWords(context, "recreate");
        swipeViewPagerRight(3);
        verifyStarredInList("recreate");
        filter("beer", "cheer", "hearten");
        swipeViewPagerRight(1);
        filter("wildness", "abandon", "embolden");
        swipeViewPagerLeft(3);
        typePoem("roses are red, violets are blue\nespresso tests will find bugs for you");
        clearPoem();

        clearSearchHistory();
        swipeViewPagerLeft(1);
        verifyAllStarredWords(context, "recreate");
        clearStarredWords();
        verifyAllStarredWords(context);
    }

    @Test
    public void openWotdListTest() {
        openMenu();
        onView(allOf(withId(R.id.title), withText(R.string.action_wotd_history), isDisplayed())).perform(click());
        ViewInteraction latestEntryDateView = onView(allOf(withId(R.id.date), withParent(childAtPosition(childAtPosition(withId(R.id.recycler_view), 0), 1))));
        // Check that the date field in the first (most recent) entry in the Wotd list contains today's date.
        Calendar cal = Calendar.getInstance();
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        latestEntryDateView.check(matches(withText(containsString(String.valueOf(dayOfMonth)))));
    }

    @Test
    public void openAboutScreenTest() {
        openMenu();
        onView(allOf(withId(R.id.title), withText(R.string.action_about), isDisplayed())).perform(click());
        onView(withId(R.id.tv_poet_assistant_license))
                .check(matches(isCompletelyDisplayed()))
                .check(matches(withText(R.string.about_license_app)))
                .perform(click());

        onView(withId(R.id.tv_license_text))
                .check(matches(isDisplayed()))
                .check(matches(withText(containsString("GNU GENERAL"))));
        pressBack();
        onView(withId(R.id.tv_source_code))
                .check(matches(isCompletelyDisplayed()))
                .check(matches(withText(R.string.about_projectUrl)));
    }

    @Test
    public void exportAudioTest() {
        swipeViewPagerLeft(3);
        typePoem("Will export some text");
        File exportDir = new File(mActivityTestRule.getActivity().getFilesDir(), "export");
        File poemFile = new File(exportDir, "poem.wav");
        assertFalse(poemFile.exists());
        openMenu();
        onView(allOf(withId(R.id.title), withText(R.string.share_poem_audio), isDisplayed())).perform(click());
        assertTrue(poemFile.exists());
    }

    @Test
    public void lookupFromPoemTest() {
        swipeViewPagerLeft(3);
        String poemText = "Here is a poem";
        typePoem(poemText);

        // Look up in the rhymer
        // Long press on the left part of the EditText, to select the first word
        String firstWord = poemText.substring(0, poemText.indexOf(' ')).toLowerCase(Locale.getDefault());
        onView(withId(R.id.tv_text)).perform(longTap(1, 0));

        // Select the "rhymer" popup
        mDevice.findObject(By.text("Rhymer")).click();

        verifyTitleStripCenterTitle(mActivityTestRule.getActivity(), R.string.tab_rhymer);
        // Complicated to find the right tv_list_header, as this view appears in a few fragments.
        // We find the root of the fragment layout, then make sure we have the right fragment by looking
        // for the descendant view of the empty text which is hidden and should say "Sorry, no rhymes for 'here'".
        onView(allOf(
                withId(R.id.tv_list_header),
                isDescendantOfA(
                        allOf(
                                withId(R.id.result_list_root),
                                hasDescendant(allOf(withId(R.id.empty), withText(mActivityTestRule.getActivity().getString(R.string.empty_rhymer_list_with_query, firstWord)))))
                )
        )).check(matches(withText(firstWord)));

        // Look up in the thesaurus
        swipeViewPagerLeft(3);
        onView(withId(R.id.tv_text)).perform(longTap(1, 0));
        mDevice.findObject(By.text("Thesaurus")).click();
        onView(allOf(
                withId(R.id.tv_list_header),
                isDescendantOfA(
                        allOf(
                                withId(R.id.result_list_root),
                                hasDescendant(allOf(withId(R.id.empty), withText(mActivityTestRule.getActivity().getString(R.string.empty_thesaurus_list_with_query, firstWord)))))
                )
        )).check(matches(withText(firstWord)));

        // Look up in the dictionary
        swipeViewPagerLeft(2);
        onView(withId(R.id.tv_text)).perform(longTap(1, 0));
        mDevice.findObject(By.text("Dictionary")).click();
        onView(allOf(
                withId(R.id.tv_list_header),
                isDescendantOfA(
                        allOf(
                                withId(R.id.result_list_root),
                                hasDescendant(allOf(withId(R.id.empty), withText(mActivityTestRule.getActivity().getString(R.string.empty_dictionary_list_with_query, firstWord)))))
                )
        )).check(matches(withText(firstWord)));
    }

    @Test
    public void patternSearchTest() {
        search("h*llo");
        checkPatterns(mActivityTestRule.getActivity(), "h*llo", "hello", "hermosillo", "hollo", "hullo");
        search("h*llz");
        checkPatterns(mActivityTestRule.getActivity(), "h*llz");
    }

}
