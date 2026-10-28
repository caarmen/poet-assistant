/*
 * Copyright (c) 2016-present Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.reader.usecases

import ca.rmen.android.poetassistant.Tts
import ca.rmen.android.poetassistant.di.IODispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for exporting the poem to an audio file.
 *
 * The use case owns its coroutine scope. The scope is never cancelled:
 * an audio write runs to completion, even if the fragment, the
 * ViewModel, or the activity is destroyed meanwhile.
 *
 * @param tts The text-to-speech service.
 * @param ioDispatcher The coroutine dispatcher for IO operations.
 */
@Singleton
class SpeakPoemToFileUseCase @Inject constructor(
    private val tts: Tts,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    /**
     * Exports the given poem text to an audio file.
     *
     * @param text The poem text to export.
     */
    operator fun invoke(text: String) {
        tts.speakToFile(text, scope, ioDispatcher)
    }
}
