/*
 * Copyright (c) 2026-present Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.wotd.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ca.rmen.android.poetassistant.main.wotd.usecases.ShowWotdNotificationUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker which posts the word-of-the-day notification.
 *
 * Runs roughly every 24 h, scheduled by SetWotdEnabledUseCase.
 *
 * Do not add retry logic for the case where the dictionary DB is not
 * loaded: the use case then posts nothing, this worker succeeds, and the
 * next periodic run retries. If the use case throws (for example a
 * SQLite error), let the exception propagate: the worker fails and
 * WorkManager retries it with the backoff criteria set by the scheduler
 * (5 h, exponential — WorkManager's maximum backoff duration).
 */
@HiltWorker
class WotdWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val showWotdNotificationUseCase: ShowWotdNotificationUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        showWotdNotificationUseCase()
        return Result.success()
    }
}
