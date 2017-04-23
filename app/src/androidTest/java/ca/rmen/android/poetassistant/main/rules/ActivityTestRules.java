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
package ca.rmen.android.poetassistant.main.rules;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;

import java.io.File;
import java.util.List;

import ca.rmen.android.poetassistant.UserDb;
import io.reactivex.plugins.RxJavaPlugins;

import static junit.framework.Assert.assertTrue;

final class ActivityTestRules {

    private ActivityTestRules() {
        // prevent instantiation
    }

    static void beforeActivityLaunched(Context targetContext) {
        Espresso.registerIdlingResources(new TtsIdlingResource(targetContext));
        cleanup(targetContext);
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> {
            IdlingScheduler idlingScheduler = new IdlingScheduler(scheduler);
            Espresso.registerIdlingResources(new RxSchedulerIdlingResource(idlingScheduler));
            return idlingScheduler;
        });
    }

    static void afterActivityFinished(Context targetContext) {
        cleanup(targetContext);
        List<IdlingResource> idlingResourceList = Espresso.getIdlingResources();
        if (idlingResourceList != null) {
            for (int i = 0; i < idlingResourceList.size(); i++) {
                Espresso.unregisterIdlingResources(idlingResourceList.get(i));
            }
        }
    }

    private static void cleanup(Context targetContext) {
        SQLiteDatabase db = new UserDb(targetContext).getWritableDatabase();
        db.delete("SUGGESTION", null, null);
        db.delete("FAVORITE", null, null);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(targetContext);
        prefs.edit().clear().apply();
        File filesDir = targetContext.getFilesDir();
        if (filesDir.exists()) {
            deleteFiles(filesDir);
        }
        NotificationManager notificationManager = (NotificationManager) targetContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private static void deleteFiles(File folder) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) deleteFiles(file);
            else assertTrue("couldn't delete file " + file, file.delete());
        }
    }
}
