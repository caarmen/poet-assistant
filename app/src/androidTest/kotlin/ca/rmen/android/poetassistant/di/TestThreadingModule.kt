/*
 * Copyright (c) 2016 - current Carmen Alvarez
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

package ca.rmen.android.poetassistant.di

import android.os.Handler
import android.os.Looper
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.test.espresso.IdlingRegistry
import ca.rmen.android.poetassistant.testsupport.CountingIdlingResourceArchTaskExecutor
import ca.rmen.android.poetassistant.testsupport.CountingIdlingResourceDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ThreadingModule::class]
)
@Module
class TestThreadingModule {

    private val testIODispatcher = CountingIdlingResourceDispatcher(
        "IO",
        Dispatchers.IO,
    )
    private val testMainDispatcher = CountingIdlingResourceDispatcher(
        "Main",
        Handler(Looper.getMainLooper()).asCoroutineDispatcher(),
    )
    private val archExecutor = CountingIdlingResourceArchTaskExecutor()

    init {
        Dispatchers.setMain(testMainDispatcher)
        IdlingRegistry.getInstance().register(testIODispatcher.getIdlingResource())
        IdlingRegistry.getInstance().register(testMainDispatcher.getIdlingResource())
        IdlingRegistry.getInstance().register(archExecutor.getIdlingResource())
        ArchTaskExecutor.getInstance().setDelegate(archExecutor)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Provides
    @Singleton
    @IODispatcher
    fun providesIODispatcher(): CoroutineDispatcher = testIODispatcher
}
