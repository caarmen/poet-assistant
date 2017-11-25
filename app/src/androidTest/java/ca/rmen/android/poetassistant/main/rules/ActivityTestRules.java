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

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;

import java.io.File;
import java.util.Collection;

import ca.rmen.android.poetassistant.UserDb;
import ca.rmen.android.poetassistant.dagger.AppModule;
import ca.rmen.android.poetassistant.dagger.DaggerHelper;
import ca.rmen.android.poetassistant.dagger.DaggerTestAppComponent;
import ca.rmen.android.poetassistant.dagger.TestAppComponent;
import ca.rmen.android.poetassistant.dagger.TestDbModule;
import ca.rmen.android.poetassistant.main.dictionaries.EmbeddedDb;
import ca.rmen.android.poetassistant.main.dictionaries.search.ProcessTextRouter;
import io.reactivex.plugins.RxJavaPlugins;

import static junit.framework.Assert.assertTrue;

final class ActivityTestRules {

    private ActivityTestRules() {
        // prevent instantiation
    }

    static void beforeActivityLaunched(Context targetContext) {
        IdlingRegistry.getInstance().register(new TtsIdlingResource(targetContext));

        Application application = (Application) targetContext.getApplicationContext();
        TestAppComponent testAppComponent = DaggerTestAppComponent.builder()
                .appModule(new AppModule(application))
                .testDbModule(new TestDbModule(application))
                .build();
        DaggerHelper.INSTANCE.setAppComponent(testAppComponent);
        cleanup(targetContext);
        ProcessTextRouter.INSTANCE.setEnabled(targetContext, true);
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> {
            IdlingScheduler idlingScheduler = new IdlingScheduler(scheduler);
            IdlingRegistry.getInstance().register(new RxSchedulerIdlingResource(idlingScheduler));
            return idlingScheduler;
        });
    }

    static void afterActivityFinished(Context targetContext) {
        cleanup(targetContext);
        Collection<IdlingResource> idlingResourceList = IdlingRegistry.getInstance().getResources();
        for (IdlingResource idlingResource : idlingResourceList) {
            IdlingRegistry.getInstance().unregister(idlingResource);
        }
    }

    private static void cleanup(Context targetContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(targetContext);
        prefs.edit().clear().commit();
        File filesDir = targetContext.getFilesDir();
        if (filesDir.exists()) {
            deleteFiles(filesDir);
        }
        NotificationManager notificationManager = (NotificationManager) targetContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
        TestAppComponent testAppComponent = (TestAppComponent) DaggerHelper.INSTANCE.getAppComponent(targetContext.getApplicationContext());
        UserDb userDb = testAppComponent.getUserDb();
        userDb.close();
        EmbeddedDb embeddedDb = testAppComponent.getEmbeddedDb();
        embeddedDb.close();
    }

    private static void deleteFiles(File folder) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) deleteFiles(file);
            else assertTrue("couldn't delete file " + file, file.delete());
        }
    }
}
