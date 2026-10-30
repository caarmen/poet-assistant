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

import android.app.Application
import android.app.job.JobScheduler
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.preference.PreferenceManager
import androidx.work.ExistingPeriodicWorkPolicy
import ca.rmen.android.poetassistant.settings.SettingsRepository
import javax.inject.Inject
import androidx.core.content.edit

/**
 * Moves the WOTD notification pipeline forward for devices which upgrade
 * from a previous app version, or which restore their data on a new
 * device.
 *
 * Previous app versions scheduled the notification with a persisted
 * JobScheduler job. This migration:
 *
 * - Cancels the old JobScheduler job, in every case.
 * - If the WOTD setting is enabled, enqueues the periodic work with KEEP:
 *   KEEP leaves the schedule untouched for devices already on the new
 *   pipeline, and enqueues it for devices arriving from a previous
 *   version. Without this step, an upgraded device with the setting
 *   enabled would get no notifications.
 *
 * The migration runs at app start, once per app update: it compares the
 * app's last update time with the value it last migrated for, and skips
 * when they are equal. The update time is the gate, not the version code:
 * the stored gate is itself backed up and restored (see backup_scheme.xml),
 * and an update time differs between devices, so a restore on a new
 * device always triggers the migration. A restored device needs it: the
 * WOTD pref is backed up but the WorkManager database is not, so the
 * setting can say enabled while no work is scheduled.
 *
 * @param application The application context.
 * @param settingsRepository The settings repository, for reading the
 * enable state.
 */
class WotdMigration @Inject constructor(
    private val application: Application,
    private val settingsRepository: SettingsRepository,
) {
    companion object {
        /**
         * Job id used by the previous pipeline
         * ("PoetAssistant/" + "WotdJob").
         */
        private val OLD_JOB_SCHEDULER_JOB_ID = "PoetAssistant/WotdJob".hashCode()

        /**
         * Pref key storing the app update time for which the migration
         * last ran.
         */
        private const val PREF_LAST_MIGRATION_UPDATE_TIME = "PREF_WOTD_MIGRATION_LAST_UPDATE_TIME"
    }

    fun migrate() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
        val lastUpdateTime = getLastUpdateTime()
        if (sharedPreferences.getLong(PREF_LAST_MIGRATION_UPDATE_TIME, -1L) == lastUpdateTime) return
        (application.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler?)
            ?.cancel(OLD_JOB_SCHEDULER_JOB_ID)
        if (settingsRepository.settingsFlow.value.isWotdEnabled) {
            WotdPeriodicWork.enqueue(application, ExistingPeriodicWorkPolicy.KEEP)
        }
        sharedPreferences.edit(commit = true) {
            putLong(
                PREF_LAST_MIGRATION_UPDATE_TIME,
                lastUpdateTime
            )
        }
    }

    private fun getLastUpdateTime(): Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            application.packageManager
                .getPackageInfo(application.packageName, PackageManager.PackageInfoFlags.of(0))
                .lastUpdateTime
        } else {
            @Suppress("DEPRECATION")
            application.packageManager.getPackageInfo(application.packageName, 0).lastUpdateTime
        }
}
