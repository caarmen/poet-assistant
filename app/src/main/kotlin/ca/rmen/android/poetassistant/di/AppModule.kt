/*
 * Copyright (c) 2016 - present Carmen Alvarez
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
package ca.rmen.android.poetassistant.di

import android.app.Application
import ca.rmen.android.poetassistant.Favorites
import ca.rmen.android.poetassistant.Theme
import ca.rmen.android.poetassistant.Threading
import ca.rmen.android.poetassistant.Tts
import ca.rmen.android.poetassistant.UserDb
import ca.rmen.android.poetassistant.main.dictionaries.EmbeddedDb
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary
import ca.rmen.android.poetassistant.main.dictionaries.rt.Rhymer
import ca.rmen.android.poetassistant.main.dictionaries.rt.Thesaurus
import ca.rmen.android.poetassistant.main.dictionaries.search.Suggestions
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun providesTts(application: Application, settingsPrefs: SettingsPrefs, threading: Threading): Tts =
        Tts(application, settingsPrefs, threading)

    @Provides
    @Singleton
    fun providesEmbeddedDb(application: Application): EmbeddedDb = EmbeddedDb(application)

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
    fun providesSettingsPrefs(application: Application): SettingsPrefs {
        SettingsPrefs.migrateSettings(application)
        val settingsPrefs = SettingsPrefs(application)
        Theme.setThemeFromSettings(settingsPrefs)
        return settingsPrefs
    }

    @Provides
    @Singleton
    fun providesFavorites(threading: Threading, userDb: UserDb) = Favorites(threading, userDb.favoriteDao())

    @Provides
    @Singleton
    fun providesSuggestions(userDb: UserDb) = Suggestions(userDb.suggestionDao())

}
