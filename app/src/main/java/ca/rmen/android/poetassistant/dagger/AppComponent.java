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

import javax.inject.Singleton;

import ca.rmen.android.poetassistant.main.MainActivity;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListFragment;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListHeaderFragment;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryEntry;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryLoader;
import ca.rmen.android.poetassistant.main.dictionaries.rt.FavoritesLoader;
import ca.rmen.android.poetassistant.main.dictionaries.rt.PatternLoader;
import ca.rmen.android.poetassistant.main.dictionaries.rt.RTEntry;
import ca.rmen.android.poetassistant.main.dictionaries.rt.RhymerLoader;
import ca.rmen.android.poetassistant.main.dictionaries.rt.ThesaurusLoader;
import ca.rmen.android.poetassistant.main.dictionaries.search.Search;
import ca.rmen.android.poetassistant.main.dictionaries.search.SuggestionsCursor;
import ca.rmen.android.poetassistant.main.dictionaries.search.SuggestionsProvider;
import ca.rmen.android.poetassistant.main.reader.ReaderFragment;
import ca.rmen.android.poetassistant.settings.SettingsActivity;
import ca.rmen.android.poetassistant.settings.VoicePreference;
import ca.rmen.android.poetassistant.wotd.WotdBootReceiver;
import ca.rmen.android.poetassistant.wotd.WotdBroadcastReceiver;
import ca.rmen.android.poetassistant.wotd.WotdEntry;
import ca.rmen.android.poetassistant.wotd.WotdJobService;
import ca.rmen.android.poetassistant.wotd.WotdLoader;
import dagger.Component;
import dagger.Subcomponent;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    MainScreenComponent getMainScreenComponent();
    SettingsComponent getSettingsComponent();
    WotdComponent getWotdComponent();

    @Subcomponent
    interface MainScreenComponent {
        void inject(MainActivity mainActivity);
        void inject(ResultListFragment<RTEntry> resultListFragment);
        void injectWotd(ResultListFragment<WotdEntry> resultListFragment);
        void injectDict(ResultListFragment<DictionaryEntry> resultListFragment);
        void inject(ResultListHeaderFragment resultListHeaderFragment);
        void inject(ReaderFragment readerFragment);
        void inject(RhymerLoader rhymerLoader);
        void inject(ThesaurusLoader thesaurusLoader);
        void inject(DictionaryLoader dictionaryLoader);
        void inject(PatternLoader patternLoader);
        void inject(FavoritesLoader favoritesLoader);
        void inject(SuggestionsCursor suggestionsCursor);
        void inject(SuggestionsProvider suggestionsProvider);
        void inject(Search search);
    }

    @Subcomponent
    interface SettingsComponent {
        void inject(SettingsActivity settingsActivity);
        void inject(SettingsActivity.GeneralPreferenceFragment generalPreferenceFragment);
        void inject(VoicePreference voicePreference);
    }

    @Subcomponent
    interface WotdComponent {
        void inject(WotdBroadcastReceiver wotdBroadcastReceiver);
        void inject(WotdJobService wotdJobService);
        void inject(WotdBootReceiver wotdBootReceiver);
        void inject(WotdLoader wotdLoader);
    }
}
