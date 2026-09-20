package ca.rmen.android.poetassistant.di

import ca.rmen.android.poetassistant.testsupport.RobolectricTestTeardownStrategy
import ca.rmen.android.poetassistant.testsupport.TestTeardownStrategy
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TestModule {
    @Binds
    @Singleton
    abstract fun bindTestTeardownStrategy(
        impl: RobolectricTestTeardownStrategy
    ): TestTeardownStrategy
}