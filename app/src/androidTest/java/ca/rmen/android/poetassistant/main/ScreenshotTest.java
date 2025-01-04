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


import static androidx.test.espresso.Espresso.onIdle;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static ca.rmen.android.poetassistant.main.TestAppUtils.search;
import static ca.rmen.android.poetassistant.main.TestAppUtils.starQueryWord;
import static ca.rmen.android.poetassistant.main.TestAppUtils.typePoem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.SystemClock;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.screenshot.BasicScreenCaptureProcessor;
import androidx.test.runner.screenshot.ScreenCapture;
import androidx.test.runner.screenshot.ScreenCaptureProcessor;

import com.google.testing.junit.testparameterinjector.TestParameter;
import com.google.testing.junit.testparameterinjector.TestParameterInjector;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.HashSet;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Theme;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;

@LargeTest
@RunWith(TestParameterInjector.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ScreenshotTest {

    @TestParameter({SettingsPrefs.THEME_LIGHT, SettingsPrefs.THEME_DARK})
    String themePreference;

    @Rule
    public PoetAssistantActivityTestRule<MainActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(MainActivity.class, true);

    @Rule
    public GrantPermissionRule writeScreenshotRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Before
    public void setupTheme() {
        getInstrumentation().runOnMainSync(() -> {
            SettingsPrefs settingsPrefs = new SettingsPrefs(ApplicationProvider.getApplicationContext());
            settingsPrefs.setTheme(themePreference);
            Theme.INSTANCE.setThemeFromSettings(settingsPrefs);
        });
    }

    @Test
    public void testTakeScreenshots() {
        starWords("acquiesce", "askance", "benight", "deferential", "fractious", "implacable", "obfuscation", "peon", "possibleness");
        search("chance");
        takeScreenshot("rhymer");
        swipeViewPagerLeft(1);
        takeScreenshot("thesaurus");
        swipeViewPagerLeft(1);
        takeScreenshot("dictionary");
        swipeViewPagerLeft(1);
        typePoem("Roses are red.\nViolets are blue.\nIf you are a poet,\nthis app is for you.");
        SystemClock.sleep(1000);
        takeScreenshot("composer");
        swipeViewPagerLeft(1);
        takeScreenshot("favorites");
        openMenuItem(R.string.action_settings);
        takeScreenshot("settings");
    }

    private void starWords(String... words) {
        for (String word : words) {
            search(word);
            onIdle();
            starQueryWord();
        }
    }

    // https://stackoverflow.com/questions/38519568/how-to-take-screenshot-at-the-point-where-test-fail-in-espresso
    private void takeScreenshot(String filename) {
        SystemClock.sleep(500); // :(
        onIdle();
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
