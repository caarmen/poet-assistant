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
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.preference.PreferenceManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.TestAppUtils.typeAndSpeakPoem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PoemSaveTest {

    @Rule
    public PoetAssistantActivityTestRule<MainActivity> mActivityTestRule = new PoetAssistantActivityTestRule<MainActivity>(MainActivity.class, true) {
        @Override
        protected void beforeActivityLaunched() {
            super.beforeActivityLaunched();
            fakeExistingPoemFilePref();
        }
    };

    @Test
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void saveTest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
        swipeViewPagerLeft(3);
        File poemFile = getPoemFile();
        assertFalse(poemFile.exists());
        typeAndSpeakPoem("Let's save a file");
        openMenuItem(R.string.file);
        onView(allOf(withId(R.id.title), withText(R.string.file_save), isDisplayed())).perform(click());
        assertTrue(poemFile.exists());
    }

    private void fakeExistingPoemFilePref() {
        Context context = getInstrumentation().getTargetContext();
        File poemFile = getPoemFile();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit()
                .putString("poem_uri", Uri.fromFile(poemFile).toString())
                .putString("poem_name", "testpoem")
                .apply();
    }

    private File getPoemFile() {
        Context context = getInstrumentation().getTargetContext();
        return new File(context.getFilesDir(), "testpoem.txt");
    }


}
