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


import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;

import java.io.File;
import java.util.List;

import ca.rmen.android.poetassistant.UserDb;
import io.reactivex.plugins.RxJavaPlugins;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.registerIdlingResources;
import static junit.framework.Assert.assertTrue;

/**
 * Tested on:
 * - Huawei P9 Lite
 */
class BaseTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            super.beforeActivityLaunched();
            registerIdlingResources(new TtsIdlingResource(getInstrumentation().getTargetContext()));
            cleanup();
            RxJavaPlugins.setIoSchedulerHandler(scheduler -> {
                IdlingScheduler idlingScheduler = new IdlingScheduler(scheduler);
                registerIdlingResources(new RxSchedulerIdlingResource(idlingScheduler));
                return idlingScheduler;
            });
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
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private void deleteFiles(File folder) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) deleteFiles(file);
            else assertTrue("couldn't delete file " + file, file.delete());
        }
    }

}
