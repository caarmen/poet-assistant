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

import ca.rmen.android.poetassistant.PoemAudioExport;
import ca.rmen.android.poetassistant.main.MainActivity;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListHeaderViewModel;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListViewModel;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryEntry;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryLiveData;
import ca.rmen.android.poetassistant.main.dictionaries.rt.FavoritesLiveData;
import ca.rmen.android.poetassistant.main.dictionaries.rt.PatternLiveData;
import ca.rmen.android.poetassistant.main.dictionaries.rt.RTEntryViewModel;
import ca.rmen.android.poetassistant.main.dictionaries.rt.RhymerLiveData;
import ca.rmen.android.poetassistant.main.dictionaries.rt.ThesaurusLiveData;
import ca.rmen.android.poetassistant.main.dictionaries.search.Search;
import ca.rmen.android.poetassistant.main.dictionaries.search.Suggestions;
import ca.rmen.android.poetassistant.main.dictionaries.search.SuggestionsCursor;
import ca.rmen.android.poetassistant.main.reader.ReaderViewModel;
import ca.rmen.android.poetassistant.settings.SettingsActivity;
import ca.rmen.android.poetassistant.settings.SettingsChangeListener;
import ca.rmen.android.poetassistant.settings.SettingsViewModel;
import ca.rmen.android.poetassistant.settings.VoicePreference;
import ca.rmen.android.poetassistant.wotd.WotdBootReceiver;
import ca.rmen.android.poetassistant.wotd.WotdBroadcastReceiver;
import ca.rmen.android.poetassistant.wotd.WotdEntryViewModel;
import ca.rmen.android.poetassistant.wotd.WotdJobService;
import ca.rmen.android.poetassistant.wotd.WotdLiveData;
import dagger.Component;
import dagger.Subcomponent;

@Singleton
@Component(modules = {AppModule.class, DbModule.class})
public interface AppComponent {

    MainScreenComponent getMainScreenComponent();
    SettingsComponent getSettingsComponent();
    WotdComponent getWotdComponent();

    @Subcomponent
    interface MainScreenComponent {
        void inject(MainActivity mainActivity);
        void inject(ResultListViewModel<RTEntryViewModel> resultListViewModel);
        void injectWotd(ResultListViewModel<WotdEntryViewModel> resultListViewModel);
        void injectDict(ResultListViewModel<DictionaryEntry> resultListViewModel);
        void inject(RTEntryViewModel rtEntry);
        void inject(ResultListHeaderViewModel resultListHeaderViewModel);
        void inject(ReaderViewModel readerViewModel);
        void inject(PoemAudioExport poemAudioExport);
        void inject(RhymerLiveData rhymerLiveData);
        void inject(ThesaurusLiveData thesaurusLiveData);
        void inject(DictionaryLiveData dictionaryLiveData);
        void inject(PatternLiveData patternLiveData);
        void inject(FavoritesLiveData favoritesLiveData);
        void inject(SuggestionsCursor suggestionsCursor);
        void inject(Search search);
        Suggestions getSuggestions();
    }

    @Subcomponent
    interface SettingsComponent {
        void inject(SettingsViewModel settingsViewModel);
        void inject(SettingsChangeListener settingsChangeListener);
        void inject(SettingsActivity.GeneralPreferenceFragment generalPreferenceFragment);
        void inject(VoicePreference voicePreference);
    }

    @Subcomponent
    interface WotdComponent {
        void inject(WotdBroadcastReceiver wotdBroadcastReceiver);
        void inject(WotdJobService wotdJobService);
        void inject(WotdBootReceiver wotdBootReceiver);
        void inject(WotdLiveData wotdLiveData);
        void inject(WotdEntryViewModel entry);
    }
}
