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

package ca.rmen.android.poetassistant.main.rules

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ca.rmen.android.poetassistant.Tts
import ca.rmen.android.poetassistant.UserDb
import ca.rmen.android.poetassistant.main.dictionaries.EmbeddedDb
import ca.rmen.android.poetassistant.main.dictionaries.search.ProcessTextRouter
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import ca.rmen.android.poetassistant.Theme
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.junit.Assert.assertTrue
import java.io.File

object ActivityTestRules {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ActivityTestRulesEntryPoint {
        fun tts(): Tts
        fun userDb(): UserDb
        fun embeddedDb(): EmbeddedDb
    }

    fun beforeActivityLaunched(targetContext: Context) {
        IdlingRegistry.getInstance().register(TtsIdlingResource(targetContext))

        cleanup(targetContext)
        ProcessTextRouter.setEnabled(targetContext, true)
    }

    fun afterActivityFinished(targetContext: Context) {
        cleanup(targetContext)
        val idlingResourceList: Collection<IdlingResource> = IdlingRegistry.getInstance().resources
        for (idlingResource in idlingResourceList) {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
        val tts = EntryPointAccessors.fromApplication(targetContext.applicationContext, ActivityTestRulesEntryPoint::class.java).tts()
        getInstrumentation().runOnMainSync { tts.shutdown() }
    }

    private fun cleanup(targetContext: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(targetContext)
        prefs.edit().clear().commit()
        val filesDir = targetContext.filesDir
        if (filesDir.exists()) {
            deleteFiles(filesDir)
        }
        val notificationManager = targetContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
        val entryPoint = EntryPointAccessors.fromApplication(targetContext.applicationContext, ActivityTestRulesEntryPoint::class.java)
        val embeddedDb = entryPoint.embeddedDb()
        embeddedDb.close()
        getInstrumentation().runOnMainSync {
            Theme.setThemeFromSettings(SettingsPrefs(targetContext.applicationContext as Application))
        }
        Espresso.onIdle()
        // https://github.com/robolectric/robolectric/issues/6251
        try {
            val instance = ViewModelProvider.AndroidViewModelFactory::class.java.getDeclaredField("_instance")
            instance.isAccessible = true
            instance.set(null, null)
        } catch (_: IllegalAccessException) {
            throw IllegalStateException("Could not clear AndroidViewModelFactory instance")
        } catch (_: NoSuchFieldException) {
            throw IllegalStateException("Could not clear AndroidViewModelFactory instance")
        }
    }

    private fun deleteFiles(folder: File) {
        val files = folder.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isDirectory) deleteFiles(file)
                else assertTrue("couldn't delete file $file", file.delete())
            }
        }
    }
}
