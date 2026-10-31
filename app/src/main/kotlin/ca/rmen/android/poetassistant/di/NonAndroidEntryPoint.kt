package ca.rmen.android.poetassistant.di

import ca.rmen.android.poetassistant.Tts
import ca.rmen.android.poetassistant.main.dictionaries.search.SuggestionsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher

@EntryPoint
@InstallIn(SingletonComponent::class)
/**
 * Provide access to dependencies without having easy access
 * to an AndroidEntryPoint
 */
interface NonAndroidEntryPoint {
    fun tts(): Tts
    fun suggestionsRepository(): SuggestionsRepository
    @IODispatcher fun ioDispatcher(): CoroutineDispatcher
}
