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
import androidx.arch.core.executor.DefaultTaskExecutor
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.idling.CountingIdlingResource
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
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ThreadingModule::class]
)
@Module
class TestThreadingModule {

    private val testDispatcher = TrackingTestDispatcher("IO")
    private val testMainDispatcher = TrackingTestDispatcher("Main",
        Handler(Looper.getMainLooper()).asCoroutineDispatcher()
    )
    private val archIdlingResource = CountingIdlingResource("ArchIdlingResource", true)

    init {
        IdlingRegistry.getInstance().register(testDispatcher.getIdlingResource())
        IdlingRegistry.getInstance().register(testMainDispatcher.getIdlingResource())
        IdlingRegistry.getInstance().register(archIdlingResource)
        Dispatchers.setMain(testMainDispatcher)
        ArchTaskExecutor.getInstance().setDelegate(object : DefaultTaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) {
                archIdlingResource.increment()
                try {
                    super.executeOnDiskIO(runnable)
                } finally {
                    archIdlingResource.decrement()
                }
            }

            override fun postToMainThread(runnable: Runnable) {
                archIdlingResource.increment()
                try {
                    super.postToMainThread(runnable)
                } finally {
                    archIdlingResource.decrement()
                }
            }
        })
    }

    class TrackingTestDispatcher(
        label: String,
        private val delegate: CoroutineDispatcher = Dispatchers.IO
    ) : CoroutineDispatcher() {

        private val idlingResource = CountingIdlingResource("TrackingTestDispatcher-$label", true)

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            idlingResource.increment()
            delegate.dispatch(context) {
                try {
                    block.run()
                } finally {
                    idlingResource.decrement()
                }
            }
        }

        fun getIdlingResource(): IdlingResource = idlingResource
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Provides
    @Singleton
    @IODispatcher
    fun providesIODispatcher(): CoroutineDispatcher = testDispatcher
}
