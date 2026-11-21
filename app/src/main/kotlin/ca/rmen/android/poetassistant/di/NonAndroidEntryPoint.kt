package ca.rmen.android.poetassistant.di

import ca.rmen.android.poetassistant.Tts
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
    @IODispatcher fun ioDispatcher(): CoroutineDispatcher
}
