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

package ca.rmen.android.poetassistant.main.wotd.usecases

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import ca.rmen.android.poetassistant.main.wotd.work.WotdPeriodicWork
import ca.rmen.android.poetassistant.settings.SettingsRepository
import javax.inject.Inject

/**
 * Applies the WOTD-enabled setting:
 *
 * - enabled: posts the notification immediately, then schedules a
 *   periodic worker every 24 h, starting from the moment of enabling (not
 *   aligned to midnight). Replacing existing periodic work also re-applies
 *   the schedule on a priority change while enabled.
 * - disabled: cancels the periodic work.
 *
 * @param application The application context.
 * @param showWotdNotificationUseCase The use case which posts the
 * notification.
 * @param settingsRepository The settings repository, for reading the
 * enable state.
 */
class SetWotdEnabledUseCase @Inject constructor(
    private val application: Application,
    private val showWotdNotificationUseCase: ShowWotdNotificationUseCase,
    private val settingsRepository: SettingsRepository,
) {

    /**
     * Applies the current WOTD-enabled setting.
     *
     * The [SettingsRepository.settingsFlow] read is safe against stale
     * values because this use case runs from the settings flow collector:
     * by the time the collector receives a new value, the flow has been
     * updated, so the read returns the new value or a newer one.
     */
    suspend operator fun invoke() {
        if (settingsRepository.settingsFlow.value.isWotdEnabled) {
            showWotdNotificationUseCase()
            // Periodic work may run once shortly after being enqueued. It
            // posts with the same notification id as the direct post above,
            // so the second post replaces the first. Do not add
            // deduplication logic.
            WotdPeriodicWork.enqueue(application, ExistingPeriodicWorkPolicy.REPLACE)
        } else {
            WotdPeriodicWork.cancel(application)
        }
    }
}
