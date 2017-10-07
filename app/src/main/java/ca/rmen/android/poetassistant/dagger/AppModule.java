/*
 * Copyright (c) 2016-2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.dagger;

import android.app.Application;
import android.arch.persistence.room.Room;

import javax.inject.Singleton;

import ca.rmen.android.poetassistant.Favorites;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.UserDb;
import ca.rmen.android.poetassistant.main.dictionaries.EmbeddedDb;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary;
import ca.rmen.android.poetassistant.main.dictionaries.rt.Rhymer;
import ca.rmen.android.poetassistant.main.dictionaries.rt.Thesaurus;
import ca.rmen.android.poetassistant.main.dictionaries.search.Suggestions;
import ca.rmen.android.poetassistant.settings.Settings;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;
import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private final Application mApplication;
    private final UserDb mUserDb;

    public AppModule(Application application) {
        mApplication = application;
        mUserDb = Room.databaseBuilder(application,
                UserDb.class, "userdata.db")
                .addMigrations(UserDb.MIGRATION_1_2).build();
    }

    @Provides @Singleton Tts providesTts(SettingsPrefs settingsPrefs) {
        return new Tts(mApplication, settingsPrefs);
    }

    @Provides @Singleton EmbeddedDb providesDbHelper() {
        return new EmbeddedDb(mApplication);
    }

    @Provides @Singleton Rhymer providesRhymer(EmbeddedDb embeddedDb, SettingsPrefs prefs) {
        return new Rhymer(embeddedDb, prefs);
    }

    @Provides @Singleton Thesaurus providesThesaurus(EmbeddedDb embeddedDb) {
        return new Thesaurus(embeddedDb);
    }

    @Provides @Singleton Dictionary providesDictionary(EmbeddedDb embeddedDb) {
        return new Dictionary(embeddedDb);
    }

    @Provides @Singleton SettingsPrefs providesSettingsPrefs() {
        Settings.migrateSettings(mApplication);
        return SettingsPrefs.get(mApplication);
    }

    @Provides Favorites providesFavorites() {
        return new Favorites(mUserDb.favoriteDao());
    }

    @Provides Suggestions providesSuggestions() {
        return new Suggestions(mUserDb.suggestionDao());
    }
}
