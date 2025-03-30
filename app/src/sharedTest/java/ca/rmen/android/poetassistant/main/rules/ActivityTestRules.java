/*
 * Copyright (c) 2017 - present Carmen Alvarez
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

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collection;

import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.idling.CountingIdlingResource;

import ca.rmen.android.poetassistant.InstrumentationThreading;
import ca.rmen.android.poetassistant.Theme;
import ca.rmen.android.poetassistant.Threading;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.UserDb;
import ca.rmen.android.poetassistant.main.dictionaries.EmbeddedDb;
import ca.rmen.android.poetassistant.main.dictionaries.search.ProcessTextRouter;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;
import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertTrue;

public final class ActivityTestRules {

    private ActivityTestRules() {
        // prevent instantiation
    }

    @EntryPoint
    @InstallIn(SingletonComponent.class)
    public interface ActivityTestRulesEntryPoint {
        Threading threading();
        Tts tts();
        UserDb userDb();
        EmbeddedDb embeddedDb();
    }
    public static void beforeActivityLaunched(Context targetContext) {
        IdlingRegistry.getInstance().register(new TtsIdlingResource(targetContext));

        InstrumentationThreading threading = (InstrumentationThreading) EntryPointAccessors.fromApplication(targetContext.getApplicationContext(), ActivityTestRulesEntryPoint.class).threading();
        CountingIdlingResource threadingCountingIdlingResource = threading.getCountingIdlingResource();
        if(threadingCountingIdlingResource != null) {
           IdlingRegistry.getInstance().register(threadingCountingIdlingResource);
        }
        cleanup(targetContext);
        ProcessTextRouter.INSTANCE.setEnabled(targetContext, true);
    }

    public static void afterActivityFinished(Context targetContext) {
        cleanup(targetContext);
        Collection<IdlingResource> idlingResourceList = IdlingRegistry.getInstance().getResources();
        for (IdlingResource idlingResource : idlingResourceList) {
            IdlingRegistry.getInstance().unregister(idlingResource);
        }
        Tts tts =  EntryPointAccessors.fromApplication(targetContext.getApplicationContext(), ActivityTestRulesEntryPoint.class).tts();
        getInstrumentation().runOnMainSync(tts::shutdown);
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
        ActivityTestRulesEntryPoint entryPoint =   EntryPointAccessors.fromApplication(targetContext.getApplicationContext(), ActivityTestRulesEntryPoint.class);
        UserDb userDb = entryPoint.userDb();
        userDb.close();
        EmbeddedDb embeddedDb = entryPoint.embeddedDb();
        embeddedDb.close();
        getInstrumentation().runOnMainSync(() -> {
            Theme.INSTANCE.setThemeFromSettings(new SettingsPrefs((Application) targetContext.getApplicationContext()));
        });
        Espresso.onIdle();
        // https://github.com/robolectric/robolectric/issues/6251
        try {
            Field instance = ViewModelProvider.AndroidViewModelFactory.class.getDeclaredField("_instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalStateException("Could not clear AndroidViewModelFactory instance");
        }
    }

    private static void deleteFiles(File folder) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) deleteFiles(file);
            else assertTrue("couldn't delete file " + file, file.delete());
        }
    }
}
