package ca.rmen.android.poetassistant.di

import ca.rmen.android.poetassistant.Favorites
import ca.rmen.android.poetassistant.Threading
import ca.rmen.android.poetassistant.Tts
import ca.rmen.android.poetassistant.main.dictionaries.ResultListAdapterFactory
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary
import ca.rmen.android.poetassistant.main.dictionaries.rt.Rhymer
import ca.rmen.android.poetassistant.main.dictionaries.rt.Thesaurus
import ca.rmen.android.poetassistant.main.dictionaries.search.Suggestions
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
/**
 * Provide access to dependencies without having easy access
 * to an AndroidEntryPoint
 */
interface NonAndroidEntryPoint {
    fun tts(): Tts
    fun rhymer(): Rhymer
    fun thesaurus(): Thesaurus
    fun dictionary(): Dictionary
    fun favorites(): Favorites
    fun suggestions(): Suggestions
    fun prefs(): SettingsPrefs
    fun threading(): Threading
    fun resultListAdapterFactory(): ResultListAdapterFactory
}