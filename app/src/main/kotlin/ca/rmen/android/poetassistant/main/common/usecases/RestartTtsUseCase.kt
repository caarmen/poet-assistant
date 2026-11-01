/*
 * Copyright (c) 2016 - present Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.common.usecases

import ca.rmen.android.poetassistant.Tts
import javax.inject.Inject

/**
 * Use case for restarting the text-to-speech engine: force a
 * reinitialization of the engine, e.g. after the user changed the
 * default engine in the system TTS settings.
 * Wraps the existing Tts class; the ViewModels and hosts never touch
 * it directly.
 *
 * @param tts The text-to-speech service.
 */
class RestartTtsUseCase @Inject constructor(
    private val tts: Tts,
) {

    /**
     * Restarts the text-to-speech engine.
     */
    operator fun invoke() {
        tts.restart()
    }
}
