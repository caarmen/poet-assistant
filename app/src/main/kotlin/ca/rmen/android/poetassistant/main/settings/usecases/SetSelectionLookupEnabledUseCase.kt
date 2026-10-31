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

package ca.rmen.android.poetassistant.main.settings.usecases

import android.content.Context
import ca.rmen.android.poetassistant.main.dictionaries.search.ProcessTextRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Enables or disables the process-text routers (the "look up selected
 * text" behavior). Replaces the legacy SettingsChangeListener handling
 * of the selection-lookup setting. The legacy settings-activity restart
 * on this change is deliberately dropped: no UI state depends on it.
 *
 * @param context The application context.
 */
class SetSelectionLookupEnabledUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Enables or disables the process-text router activities.
     */
    operator fun invoke(enabled: Boolean) {
        ProcessTextRouter.setEnabled(context, enabled)
    }
}
