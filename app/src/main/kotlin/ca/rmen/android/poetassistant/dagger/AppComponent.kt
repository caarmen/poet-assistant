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

import ca.rmen.android.poetassistant.Favorites
import ca.rmen.android.poetassistant.PoemAudioExport
import ca.rmen.android.poetassistant.main.MainActivity
import ca.rmen.android.poetassistant.main.dictionaries.ResultListHeaderViewModel
import ca.rmen.android.poetassistant.main.dictionaries.ResultListViewModel
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryEntry
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryLiveData
import ca.rmen.android.poetassistant.main.dictionaries.rt.FavoritesLiveData
import ca.rmen.android.poetassistant.main.dictionaries.rt.PatternLiveData
import ca.rmen.android.poetassistant.main.dictionaries.rt.RTEntryViewModel
import ca.rmen.android.poetassistant.main.dictionaries.rt.RhymerLiveData
import ca.rmen.android.poetassistant.main.dictionaries.rt.ThesaurusLiveData
import ca.rmen.android.poetassistant.main.dictionaries.search.Search
import ca.rmen.android.poetassistant.main.dictionaries.search.Suggestions
import ca.rmen.android.poetassistant.main.dictionaries.search.SuggestionsCursor
import ca.rmen.android.poetassistant.main.reader.ReaderViewModel
import ca.rmen.android.poetassistant.settings.SettingsActivity
import ca.rmen.android.poetassistant.settings.SettingsChangeListener
import ca.rmen.android.poetassistant.settings.SettingsViewModel
import ca.rmen.android.poetassistant.settings.VoicePreference
import ca.rmen.android.poetassistant.wotd.WotdBootReceiver
import ca.rmen.android.poetassistant.wotd.WotdBroadcastReceiver
import ca.rmen.android.poetassistant.wotd.WotdEntryViewModel
import ca.rmen.android.poetassistant.wotd.WotdJobService
import ca.rmen.android.poetassistant.wotd.WotdLiveData
import dagger.Component
import dagger.Subcomponent
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class), (DbModule::class)])
interface AppComponent {
    fun getMainScreenComponent(): AppComponent.MainScreenComponent
    fun getSettingsComponent(): AppComponent.SettingsComponent
    fun getWotdComponent(): AppComponent.WotdComponent

    @Subcomponent
    interface MainScreenComponent {
        fun inject(mainActivity: MainActivity)
        fun inject(resultListViewModel: ResultListViewModel<RTEntryViewModel>)
        fun injectWotd(resultListViewModel: ResultListViewModel<WotdEntryViewModel>)
        fun injectDict(resultListViewModel: ResultListViewModel<DictionaryEntry>)
        fun inject(rtEntry: RTEntryViewModel)
        fun inject(resultListHeaderViewModel: ResultListHeaderViewModel)
        fun inject(readerViewModel: ReaderViewModel)
        fun inject(poemAudioExport: PoemAudioExport)
        fun inject(rhymerLiveData: RhymerLiveData)
        fun inject(thesaurusLiveData: ThesaurusLiveData)
        fun inject(dictionaryLiveData: DictionaryLiveData)
        fun inject(patternLiveData: PatternLiveData)
        fun inject(favoritesLiveData: FavoritesLiveData)
        fun inject(suggestionsCursor: SuggestionsCursor)
        fun inject(search: Search)
        fun getSuggestions(): Suggestions
        fun getFavorites(): Favorites
    }

    @Subcomponent
    interface SettingsComponent {
        fun inject(settingsViewModel: SettingsViewModel)
        fun inject(settingsChangeListener: SettingsChangeListener)
        fun inject(generalPreferenceFragment: SettingsActivity.GeneralPreferenceFragment)
        fun inject(voicePreference: VoicePreference)
    }

    @Subcomponent
    interface WotdComponent {
        fun inject(wotdBroadcastReceiver: WotdBroadcastReceiver)
        fun inject(wotdJobService: WotdJobService)
        fun inject(wotdBootReceiver: WotdBootReceiver)
        fun inject(wotdLiveData: WotdLiveData)
        fun inject(wotdEntryViewModel: WotdEntryViewModel)
    }

}
