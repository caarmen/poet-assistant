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


import android.Manifest;
import android.graphics.Bitmap;
import android.os.SystemClock;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.HashSet;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.screenshot.BasicScreenCaptureProcessor;
import androidx.test.runner.screenshot.ScreenCapture;
import androidx.test.runner.screenshot.ScreenCaptureProcessor;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;
import ca.rmen.android.poetassistant.main.rules.RetryTestRule;

import static androidx.test.espresso.Espresso.onIdle;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.TestAppUtils.search;
import static ca.rmen.android.poetassistant.main.TestAppUtils.starQueryWord;
import static ca.rmen.android.poetassistant.main.TestAppUtils.typePoem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;

@LargeTest
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ScreenshotTest {

    @Rule
    public RetryTestRule retry = new RetryTestRule(1);

    @Rule
    public PoetAssistantActivityTestRule<MainActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(MainActivity.class, true);

    @Rule
    public GrantPermissionRule writeScreenshotRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void takeLightScreenshotsTest() {
        takeSreenshots();
    }

    @Test
    public void takeDarkScreenshotsTest() {
        openMenuItem(R.string.action_settings);
        clickPreference(R.string.pref_theme_title);
        onView(withText(R.string.pref_theme_value_dark)).perform(click());
        pressBack();
        takeSreenshots();
    }

    private void takeSreenshots() {
        starWords("acquiesce", "askance", "benight", "deferential", "fractious", "implacable", "obfuscation", "peon", "possibleness");
        search("chance");
        onIdle();
        takeScreenshot("rhymer");
        swipeViewPagerLeft(1);
        takeScreenshot("thesaurus");
        swipeViewPagerLeft(1);
        takeScreenshot("dictionary");
        swipeViewPagerLeft(1);
        typePoem("Roses are red.\nViolets are blue.\nIf you are a poet,\nthis app is for you.");
        SystemClock.sleep(1000);
        onIdle();
        takeScreenshot("composer");
        swipeViewPagerLeft(1);
        takeScreenshot("favorites");
        openMenuItem(R.string.action_settings);
        takeScreenshot("settings");
    }

    private void starWords(String... words) {
        for (String word : words) {
            search(word);
            starQueryWord();
        }
    }

    // https://stackoverflow.com/questions/38519568/how-to-take-screenshot-at-the-point-where-test-fail-in-espresso
    private void takeScreenshot(String filename) {
        ScreenCapture capture = androidx.test.runner.screenshot.Screenshot.capture();
        capture.setName(filename);
        capture.setFormat(Bitmap.CompressFormat.PNG);

        HashSet<ScreenCaptureProcessor> processors = new HashSet<>();
        processors.add(new BasicScreenCaptureProcessor());

        try {
            capture.process(processors);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
