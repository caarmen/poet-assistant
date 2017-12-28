/*
 * Copyright (c) 2016 - 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.dagger

import android.app.Application
import ca.rmen.android.poetassistant.Favorites
import ca.rmen.android.poetassistant.Threading
import ca.rmen.android.poetassistant.Tts
import ca.rmen.android.poetassistant.UserDb
import ca.rmen.android.poetassistant.main.dictionaries.EmbeddedDb
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary
import ca.rmen.android.poetassistant.main.dictionaries.rt.Rhymer
import ca.rmen.android.poetassistant.main.dictionaries.rt.Thesaurus
import ca.rmen.android.poetassistant.main.dictionaries.search.Suggestions
import ca.rmen.android.poetassistant.settings.Settings
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val application: Application) {
    @Provides
    @Singleton
    fun providesTts(settingsPrefs: SettingsPrefs, threading: Threading): Tts = Tts(application, settingsPrefs, threading)

    @Provides
    @Singleton
    fun providesEmbeddedDb(): EmbeddedDb = EmbeddedDb(application)

    @Provides
    @Singleton
    fun providesRhymer(embeddedDb: EmbeddedDb, settingsPrefs: SettingsPrefs) = Rhymer(embeddedDb, settingsPrefs)

    @Provides
    @Singleton
    fun providesThesaurus(embeddedDb: EmbeddedDb) = Thesaurus(embeddedDb)

    @Provides
    @Singleton
    fun providesDictionary(embeddedDb: EmbeddedDb) = Dictionary(embeddedDb)

    @Provides
    @Singleton
    fun providesSettingsPrefs(): SettingsPrefs {
        Settings.migrateSettings(application)
        return SettingsPrefs.get(application)
    }

    @Provides
    @Singleton
    fun providesFavorites(threading: Threading, userDb: UserDb) = Favorites(threading, userDb.favoriteDao())

    @Provides
    @Singleton
    fun providesSuggestions(userDb: UserDb) = Suggestions(userDb.suggestionDao())
}
