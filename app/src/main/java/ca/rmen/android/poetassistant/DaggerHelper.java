/*
 * Copyright (c) 2016 Carmen Alvarez
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

package ca.rmen.android.poetassistant;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import ca.rmen.android.poetassistant.main.MainActivity;
import ca.rmen.android.poetassistant.main.dictionaries.DbHelper;
import ca.rmen.android.poetassistant.main.dictionaries.Favorites;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListFragment;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListHeader;
import ca.rmen.android.poetassistant.main.dictionaries.Search;
import ca.rmen.android.poetassistant.main.dictionaries.SuggestionsCursor;
import ca.rmen.android.poetassistant.main.dictionaries.SuggestionsProvider;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryEntry;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryLoader;
import ca.rmen.android.poetassistant.main.dictionaries.rt.PatternLoader;
import ca.rmen.android.poetassistant.main.dictionaries.rt.RTEntry;
import ca.rmen.android.poetassistant.main.dictionaries.rt.Rhymer;
import ca.rmen.android.poetassistant.main.dictionaries.rt.RhymerLoader;
import ca.rmen.android.poetassistant.main.dictionaries.rt.Thesaurus;
import ca.rmen.android.poetassistant.main.dictionaries.rt.ThesaurusLoader;
import ca.rmen.android.poetassistant.main.reader.ReaderFragment;
import ca.rmen.android.poetassistant.settings.Settings;
import ca.rmen.android.poetassistant.settings.SettingsActivity;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;
import ca.rmen.android.poetassistant.settings.VoicePreference;
import ca.rmen.android.poetassistant.wotd.WotdBootReceiver;
import ca.rmen.android.poetassistant.wotd.WotdBroadcastReceiver;
import ca.rmen.android.poetassistant.wotd.WotdJobService;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

public class DaggerHelper {
    public static AppComponent getAppComponent(Context context) {
        return ((PoetAssistantApplication) context.getApplicationContext()).getAppComponent();
    }

    @Singleton
    @Component(modules=AppModule.class)
    public interface AppComponent {

        // Main screen
        void inject(MainActivity mainActivity);
        void inject(ResultListFragment<RTEntry> resultListFragment);
        void injectDict(ResultListFragment<DictionaryEntry> resultListFragment);
        void inject(ResultListHeader resultListHeader);
        void inject(ReaderFragment readerFragment);
        void inject(RhymerLoader rhymerLoader);
        void inject(ThesaurusLoader thesaurusLoader);
        void inject(DictionaryLoader dictionaryLoader);
        void inject(PatternLoader patternLoader);
        void inject(SuggestionsCursor suggestionsCursor);
        void inject(SuggestionsProvider suggestionsProvider);
        void inject(Favorites favorites);
        void inject(Search search);

        // Settings
        void inject(SettingsActivity settingsActivity);
        void inject(SettingsActivity.GeneralPreferenceFragment generalPreferenceFragment);
        void inject(VoicePreference voicePreference);

        // Wotd
        void inject(WotdBroadcastReceiver wotdBroadcastReceiver);
        void inject(WotdJobService wotdJobService);
        void inject(WotdBootReceiver wotdBootReceiver);

    }

    @Module
    static class AppModule {

        private final Application mApplication;

        AppModule(Application application) {
            mApplication = application;
        }

        @Provides @Singleton Context providesApplicationContext() {
            return mApplication;
        }

        @Provides @Singleton Tts providesTts(Context context, SettingsPrefs settingsPrefs) {
            return new Tts(context, settingsPrefs);
        }

        @Provides @Singleton DbHelper providesDbHelper(Context context) {
            return new DbHelper(context);
        }

        @Provides @Singleton Rhymer providesRhymer(DbHelper dbHelper, SettingsPrefs prefs) {
            return new Rhymer(dbHelper, prefs);
        }

        @Provides @Singleton Thesaurus providesThesaurus(DbHelper dbHelper) {
            return new Thesaurus(dbHelper);
        }

        @Provides @Singleton Dictionary providesDictionary(DbHelper dbHelper) {
            return new Dictionary(dbHelper);
        }

        @Provides @Singleton SettingsPrefs providesSettingsPrefs(Context context) {
            Settings.migrateSettings(context);
            return SettingsPrefs.get(context);
        }

    }
}
