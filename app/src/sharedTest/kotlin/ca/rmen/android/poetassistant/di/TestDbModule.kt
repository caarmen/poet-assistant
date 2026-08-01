package ca.rmen.android.poetassistant.di

import android.app.Application
import androidx.room.Room
import ca.rmen.android.poetassistant.UserDb
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@TestInstallIn(components = [SingletonComponent::class], replaces = [DbModule::class])
@Module
class TestDbModule {
    @Provides
    @Singleton
    fun providesUserDb(application: Application): UserDb {
        return Room.inMemoryDatabaseBuilder(
            application,
            UserDb::class.java
        ).allowMainThreadQueries().build()
    }
}