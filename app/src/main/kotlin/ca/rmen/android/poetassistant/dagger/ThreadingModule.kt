package ca.rmen.android.poetassistant.dagger

import ca.rmen.android.poetassistant.AsyncTaskThreading
import ca.rmen.android.poetassistant.Threading
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ThreadingModule {
    @Provides
    @Singleton
    fun providesThreading() : Threading = AsyncTaskThreading()
}